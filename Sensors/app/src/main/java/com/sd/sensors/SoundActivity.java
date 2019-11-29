package com.sd.sensors;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.media.AudioManager;

import android.provider.Settings;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class SoundActivity extends AppCompatActivity {

    final String STRING_SENSOR_TYPE = "SOUND";

    private String deviceID;

    private double frequency = 0.5;

    private boolean currentValue = false;
    private int currentVolume = 0;

    private boolean active = false;

    private TextView mTextLastSent;
    private TextView mTextCurrentValue;
    private TextView mTextFrequency;
    private TextView mTextConnection;

    private ArrayList<InetAddress> gatewayAddr = new ArrayList<java.net.InetAddress>();

    private String groupAddr = "239.0.1.2";

    private int gatewayPort = 5003;

    private int groupPort = 20480;

    private WifiManager.MulticastLock lock;

    DatagramSocket aSocket;

    private int localPort;

    private int length;

    MediaPlayer mediaPlayer = null;
    AudioManager audioManager = null;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static final String EXCHANGE_NAME = "topic_logs";
    ConnectionFactory factory = null;
    Connection connection = null;
    Channel channel = null;

    Connection connection2 = null;
    Channel channel2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaPlayer = MediaPlayer.create(SoundActivity.this, R.raw.sound);
        mediaPlayer.setLooping(true);

        mTextLastSent = findViewById(R.id.label_last_time);
        mTextLastSent.setText("No messages sent");

        mTextCurrentValue = findViewById(R.id.label_current_value);
        mTextCurrentValue.setText("OFF");

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        active = true;

        mTextConnection = (TextView) findViewById(R.id.textConnection);

        try {
            aSocket = new DatagramSocket();
            localPort = aSocket.getLocalPort();
        } catch (SocketException e) {
            System.out.println("SE: " + e.getMessage());
        }

        mTextCurrentValue.post(new Runnable() {
            public void run() {
                mTextCurrentValue.setText("OFF\nVOLUME: " + currentVolume);
            }
        });

        UDPListener();
        establishConnection();

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            lock = wifi.createMulticastLock("HelloAndroid");
            lock.setReferenceCounted(false);
            lock.acquire();
        }

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        frequency = (double)1/(seekBar.getProgress() + 1);
        mTextFrequency = findViewById(R.id.textView);
        mTextFrequency.setText("Data sent every "+(seekBar.getProgress() + 1)+" second(s)");

        publishToAMQP();
        RPCServer();
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            frequency = (double)1/(progress + 1);
            mTextFrequency.setText("Data sent every "+(progress + 1)+" second(s)");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    private void volumeUp() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);

        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mTextLastSent.post(() -> {
            mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
        });
        mTextCurrentValue.post(() -> {
            if(currentValue) {
                mTextCurrentValue.setText("ON\nVOLUME: " + currentVolume);
            } else {
                mTextCurrentValue.setText("OFF\nVOLUME: " + currentVolume);
            }
        });
    }

    private void volumeDown() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mTextLastSent.post(() -> {
            mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
        });
        mTextCurrentValue.post(() -> {
            if(currentValue) {
                mTextCurrentValue.setText("ON\nVOLUME: " + currentVolume);
            } else {
                mTextCurrentValue.setText("OFF\nVOLUME: " + currentVolume);
            }
        });
    }

    private void toggle() {
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentValue = !currentValue;
        if (currentValue) {
            mediaPlayer.seekTo(length);
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
        }

        mTextLastSent.post(() -> {
            mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
        });
        mTextCurrentValue.post(() -> {
            if(currentValue) {
                mTextCurrentValue.setText("ON\nVOLUME: " + currentVolume);
            } else {
                mTextCurrentValue.setText("OFF\nVOLUME: " + currentVolume);
            }
        });
    }

    @Override
    protected void onStart() {
        active = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        active = false;
        if (mediaPlayer != null) mediaPlayer.release();
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        lock.release();
        active = false;
        if (mediaPlayer != null) mediaPlayer.release();
        closeConnection();
        super.onDestroy();
    }

    private void closeConnection() {
        Thread thread = new Thread(() -> {
            try {
                channel2.close();
                connection2.close();
                channel.close();
                connection.close();
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } catch (java.util.concurrent.TimeoutException e) {
                System.out.println("TimeoutException: " + e.getMessage());
            } catch (java.lang.NullPointerException e) {
                System.out.println("NullPointerException: " + e.getMessage());
            } catch (com.rabbitmq.client.AlreadyClosedException e) {
                System.out.println("AlreadyClosedException: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void establishConnection() {

        Thread thread = new Thread(() -> {
            int attemptsCounter = 0;
            while(gatewayAddr.isEmpty() && attemptsCounter < 3) {
                try {
                    Thread.sleep(2000 + attemptsCounter*500);
                    attemptsCounter += 1;
                } catch (InterruptedException e) {
                    System.out.println("Interrupted: " + e.getMessage());
                }
            }

            if(gatewayAddr.isEmpty())
                return;

            if (factory == null) {
                factory = new ConnectionFactory();
                factory.setAutomaticRecoveryEnabled(false);
                String addr = gatewayAddr.get(0).toString();
                factory.setHost(addr.replace("/", ""));
                factory.setUsername("projetosd");
                factory.setPassword("projetosd");
            }

            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                System.out.println("IOException: " + e.getMessage());
            } catch (java.util.concurrent.TimeoutException e) {
                Log.e("TimeoutException", e.getMessage());
                System.out.println("TimeoutException: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void publishToAMQP() {
        Thread publishThread = new Thread(() -> {
            String routingKey = deviceID + "." + STRING_SENSOR_TYPE.toLowerCase();
            while (active) {
                try {
                    if(channel != null && channel.isOpen()) {
                        String message;
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if (currentValue) {
                            message = "ON," + currentVolume;
                        } else {
                            message = "OFF," + currentVolume;
                        }
                        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                        mTextLastSent.post(() -> mTextLastSent.setText(
                                "Last message sent at " + sdf.format(new Date())));
                        mTextConnection.post(() -> mTextConnection.setText(
                                "CONNECTED"));
                        Thread.sleep((int)(1/frequency)*1000);
                    } else {
                        mTextConnection.post(() -> mTextConnection.setText(
                                "NOT CONNECTED"));
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.out.println("Connection broken: " + e.getClass().getName());
                }
            }
        });
        publishThread.setDaemon(true);
        publishThread.start();
    }

    private void RPCServer() {
        String rpc_queue = "rpc_" + deviceID + "_sound";

        Thread rpcThread = new Thread(() -> {
            try {
                while(factory == null) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted: " + e.getMessage());
                    }
                }
                connection2 = factory.newConnection();
                channel2 = connection.createChannel();

                channel2.queueDeclare(rpc_queue, false, true, true, null);
                Log.e("RPC", " [*] Waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(delivery.getProperties().getCorrelationId())
                            .build();
                    String message = new String(delivery.getBody(), "UTF-8");
                    if (active) {
                        if (message.equals("toggle")) {
                            toggle();
                        } else if (message.equals("volup")) {
                            volumeUp();
                        } else if (message.equals("voldown")) {
                            volumeDown();
                        }
                        String msg_out;
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if (currentValue) {
                            msg_out = "ON," + currentVolume;
                        } else {
                            msg_out = "OFF," + currentVolume;
                        }
                        String routingKey = deviceID + "." + STRING_SENSOR_TYPE.toLowerCase();
                        channel2.basicPublish(EXCHANGE_NAME, routingKey,
                                null, msg_out.getBytes("UTF-8"));
                    }
                };
                channel2.basicConsume(rpc_queue, true, deliverCallback, consumerTag -> { });
            } catch(IOException | TimeoutException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        });
        rpcThread.setDaemon(true);
        rpcThread.start();
    }

    private void UDPListener() {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                MulticastSocket mSocket = null;
                InetAddress groupAddress;
                try {
                    mSocket = new MulticastSocket(groupPort);
                    mSocket.setTimeToLive(15);
                    groupAddress = InetAddress.getByName(groupAddr);
                    mSocket.joinGroup(groupAddress);
                    String multicast_msg = "SENSOR";
                    DatagramPacket messageOut = new DatagramPacket(multicast_msg.getBytes(),
                            multicast_msg.length(), groupAddress, groupPort);
                    mSocket.send(messageOut);
                    mTextLastSent.post(() -> mTextLastSent.setText(
                            "Last message sent at " + sdf.format(new Date())));
                    byte[] buffer = new byte[32];
                    while (active) {
                        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                        mSocket.receive(messageIn);
                        if(!active) break;
                        String msg
                                = new String(messageIn.getData(), 0, messageIn.getLength());
                        InetAddress addr = messageIn.getAddress();
                        int port = messageIn.getPort();
                        if(msg.equals("SERVER")){
                            if(!gatewayAddr.contains(addr))
                                gatewayAddr.add(addr);
                            String presentation_msg = "SENSOR_"+deviceID+"_"+STRING_SENSOR_TYPE+"_"+localPort;
                            DatagramPacket DPPresentation = new DatagramPacket(
                                    presentation_msg.getBytes(),
                                    presentation_msg.length(),
                                    addr, port);
                            mSocket.send(DPPresentation);
                        }
                    }
                    mSocket.leaveGroup(groupAddress);
                } catch (IOException e) {
                    System.out.println("SE: " + e.getMessage());
                } finally {
                    if (mSocket != null) mSocket.close();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


}