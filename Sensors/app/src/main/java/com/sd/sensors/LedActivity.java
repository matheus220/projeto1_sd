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
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class LedActivity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private String mCameraId;
    private ToggleButton toggleButton;

    final String STRING_SENSOR_TYPE = "LED";

    private String deviceID;

    private boolean currentValue = false;

    private boolean active = false;

    private ArrayList<InetAddress> gatewayAddr = new ArrayList<java.net.InetAddress>();

    private String groupAddr = "239.0.1.2";

    private int gatewayPort = 5003;

    private int groupPort = 20480;

    private WifiManager.MulticastLock lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        active = true;

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

        toggleButton = findViewById(R.id.togglebutton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentValue = isChecked;
                switchFlashLight(isChecked);
            }
        });

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
                    String message = "SENSOR";
                    DatagramPacket messageOut = new DatagramPacket(message.getBytes(),
                            message.length(), groupAddress, groupPort);
                    mSocket.send(messageOut);
                    byte[] buffer = new byte[6];
                    while (active) {
                        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                        mSocket.receive(messageIn);
                        String msg = new String(buffer);
                        InetAddress addr = messageIn.getAddress();
                        int port = messageIn.getPort();
                        if(msg.equals("SERVER") && !gatewayAddr.contains(addr)){
                            gatewayAddr.add(addr);
                            String presentation = deviceID + "_" + STRING_SENSOR_TYPE;
                            DatagramPacket DPPresentation = new DatagramPacket(presentation.getBytes(),
                                    presentation.length(), addr, port);
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
