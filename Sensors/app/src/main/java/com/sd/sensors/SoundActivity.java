package com.sd.sensors;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.media.AudioManager;

import android.provider.Settings;
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

import android.util.Log;

public class SoundActivity extends AppCompatActivity {

    final String STRING_SENSOR_TYPE = "SOUND";

    private String deviceID;

    private boolean currentValue = false;
    private int currentVolume = 0;

    private boolean active = false;

    private TextView mTextLastSent;
    private TextView mTextCurrentValue;

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

        DataSendThread();
        UDPListener();

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            lock = wifi.createMulticastLock("HelloAndroid");
            lock.setReferenceCounted(false);
            lock.acquire();
        }
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
    }

    @Override
    protected void onDestroy() {
        lock.release();
        if (mediaPlayer != null) mediaPlayer.release();
        super.onDestroy();
    }

    private void DataSendThread() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[32];
                    DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                    while (active) {
                        aSocket.receive(data);
                        if(!active) break;
                        String msg_in
                                = new String(data.getData(), 0, data.getLength());
                        String msg_out;
                        boolean valid = false;
                        if (msg_in.equals("TOGGLE")) {
                            valid = true;
                            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            currentValue = !currentValue;
                            if (currentValue) {
                                mediaPlayer.seekTo(length);
                                mediaPlayer.start();
                            } else {
                                mediaPlayer.pause();
                                length = mediaPlayer.getCurrentPosition();
                            }
                        } else if(msg_in.equals("VOLUP")) {
                            valid = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                        } else if(msg_in.equals("VOLDOWN")) {
                            valid = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                        }
                        if(valid && gatewayAddr.size() > 0) {
                            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            if (currentValue) {
                                msg_out = "ON," + currentVolume;
                            } else {
                                msg_out = "OFF," + currentVolume;
                            }
                            for(int i=0; i<gatewayAddr.size(); i++) {
                                aSocket.send(new DatagramPacket(msg_out.getBytes(),
                                        msg_out.length(), gatewayAddr.get(i), gatewayPort));
                            }
                            mTextLastSent.post(new Runnable() {
                                public void run() {
                                    mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
                                }
                            });
                            mTextCurrentValue.post(new Runnable() {
                                public void run() {
                                    if(currentValue) {
                                        mTextCurrentValue.setText("ON\nVOLUME: " + currentVolume);
                                    } else {
                                        mTextCurrentValue.setText("OFF\nVOLUME: " + currentVolume);
                                    }
                                }
                            });
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("SE: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("IO: " + e.getMessage());
                } finally {
                    if (aSocket != null) aSocket.close();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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
                    mTextLastSent.post(new Runnable() {
                        public void run() {
                            mTextLastSent.setText("Last message sent at " + sdf.format(new Date()));
                        }
                    });
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