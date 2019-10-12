package com.sd.sensors;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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

public class LedActivity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private String mCameraId;
    // private ToggleButton toggleButton;

    final String STRING_SENSOR_TYPE = "LED";

    private String deviceID;

    private boolean currentValue = false;

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

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

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

        DataSendThread();
        UDPListener();

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
    }

    @Override
    protected void onStart() {
        active = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        active = false;
        switchFlashLight(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lock.release();
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
                        if (msg_in.equals("TOGGLE")) {
                            currentValue = !currentValue;
                            switchFlashLight(currentValue);
                            if(currentValue) {
                                msg_out = "ON";
                            } else {
                                msg_out = "OFF";
                            }
                            if(gatewayAddr.size() > 0) {
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
                                            mTextCurrentValue.setText("ON");
                                        } else {
                                            mTextCurrentValue.setText("OFF");
                                        }

                                    }
                                });
                            }
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
                        if(msg.equals("SERVER") && !gatewayAddr.contains(addr)){
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
