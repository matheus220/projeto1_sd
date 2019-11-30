package com.sd.sensors;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP.BasicProperties;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class LedActivity extends AppCompatActivity implements Activator {

    private CameraManager mCameraManager;
    private String mCameraId;
    // private ToggleButton toggleButton;

    final String STRING_SENSOR_TYPE = "LED";

    private String deviceID;

    private boolean currentValue = false;

    private boolean active = false;

    private double frequency = 0.5;

    private TextView mTextSensor;

    private TextView mTextFrequency;

    private TextView mTextLastSent;
    private TextView mTextCurrentValue;
    private TextView mTextConnection;

    private ArrayList<InetAddress> gatewayAddr = new ArrayList<java.net.InetAddress>();

    private String groupAddr = "239.0.1.2";

    private int gatewayPort = 5003;

    private int groupPort = 20480;

    private WifiManager.MulticastLock lock;

    DatagramSocket aSocket;

    private int localPort;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static final String EXCHANGE_NAME = "topic_logs";
    ConnectionFactory factory = null;
    Connection connection = null;
    Channel channel = null;

    Connection connection2 = null;
    Channel channel2 = null;

    final MyGrpcServer myServer = new MyGrpcServer();
    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);



        mTextLastSent = findViewById(R.id.label_last_time);
        mTextLastSent.setText("No messages sent");

        mTextCurrentValue = findViewById(R.id.label_current_value);
        mTextCurrentValue.setText("OFF");

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        active = true;

        try {
            aSocket = new DatagramSocket();
            localPort = aSocket.getLocalPort();
        } catch (SocketException e) {
            System.out.println("SE: " + e.getMessage());
        }

        try{
            myServer.start(this);
        }catch(Exception e){
            logger.warning(e.getMessage());
        }

        UDPListener();
        establishConnection();
        
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        active = true;

        mTextConnection = (TextView) findViewById(R.id.textConnection);

        // Initialize all view variables.
        mTextSensor = (TextView) findViewById(R.id.label_magnetic);

        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            showNoFlashError();
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        /*toggleButton = findViewById(R.id.togglebutton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchFlashLight(isChecked);
            }
        });*/

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

        UDPListener();
        establishConnection();
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

    @Override
    protected void onStart() {
        active = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        active = false;
        switchFlashLight(false);
        currentValue = false;
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        lock.release();
        active = false;
        closeConnection();
        myServer.stop();
        super.onDestroy();
    }

    public void showNoFlashError() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .create();
        alert.setTitle("Oops!");
        alert.setMessage("Flash not available in this device...");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    public void switchFlashLight(boolean status) {
        try {
            mCameraManager.setTorchMode(mCameraId, status);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void toggle() {
        currentValue = !currentValue;
        switchFlashLight(currentValue);

        mTextLastSent.post(() -> {
            mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
        });
        mTextCurrentValue.post(() -> {
            if(currentValue) {
                mTextCurrentValue.setText("ON");
            } else {
                mTextCurrentValue.setText("OFF");
            }
        });
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
            mTextConnection.post(() -> mTextConnection.setText(
                    "CONNECTING..."));
            while(gatewayAddr.isEmpty() && attemptsCounter < 10) {
                try {
                    Thread.sleep(1500 + attemptsCounter*500);
                    attemptsCounter += 1;
                } catch (InterruptedException e) {
                    System.out.println("Interrupted: " + e.getMessage());
                }
            }

            if(gatewayAddr.isEmpty()) {
                mTextConnection.post(() -> mTextConnection.setText(
                        "NO GATEWAY FOUND"));
                return;
            }

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
                        if(currentValue) {
                            message = "ON";
                        } else {
                            message = "OFF";
                        }
                        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                        mTextLastSent.post(() -> mTextLastSent.setText(
                                "Last message sent at " + sdf.format(new Date())));
                        mTextConnection.post(() -> mTextConnection.setText(
                                "CONNECTED"));
                        Thread.sleep((int)(1/frequency)*1000);
                    } else {
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
        String rpc_queue = "rpc_" + deviceID + "_led";

        Thread rpcThread = new Thread(() -> {
            try {
                while(factory == null) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted: " + e.getMessage());
                    }
                }
                connection2 = factory.newConnection();
                channel2 = connection.createChannel();

                channel2.queueDeclare(rpc_queue, false, true, true, null);
                channel2.basicQos(1);
                Log.e("RPC", " [*] Waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    BasicProperties replyProps = new BasicProperties
                            .Builder()
                            .correlationId(delivery.getProperties().getCorrelationId())
                            .build();
                    String message = new String(delivery.getBody(), "UTF-8");
                    if (message.equals("toggle") && active) {
                        toggle();
                        String msg_out;
                        if(currentValue) {
                            msg_out = "ON";
                        } else {
                            msg_out = "OFF";
                        }
                        String routingKey = deviceID + "." + STRING_SENSOR_TYPE.toLowerCase();
                        channel2.basicPublish(EXCHANGE_NAME, routingKey,
                                null, msg_out.getBytes("UTF-8"));
                        channel2.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                };
                channel2.basicConsume(rpc_queue, false, deliverCallback, consumerTag -> { });
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

    @Override
    public void Do(String... params) {
        toggle();
    }
/*
    private class GrpcTask extends AsyncTask<Void, Void, String> {

        private String mHost = "";
        private int mPort = 0;
        private ManagedChannel mChannel;

        private void SendCommand(ManagedChannel channel){
            SensorServiceGRPCGrpc.SensorServiceGRPCBlockingStub stub = SensorServiceGRPCGrpc.newBlockingStub(channel);

            Sensor.Builder sensorBuilder = Sensor.newBuilder();
            sensorBuilder.setId("Meu id");

            Command.Builder comm = Command.newBuilder();
            comm.setCommand("COMANDO");
            comm.setId("#ID");

            stub.send(comm.build());
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                mChannel = ManagedChannelBuilder.forAddress(mHost,mPort)
                            .build();
                SendCommand(mChannel);
                return null;
            }catch(Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            }catch(Exception e){
                Thread.currentThread().interrupt();
            }
        }
    }
*/

}
