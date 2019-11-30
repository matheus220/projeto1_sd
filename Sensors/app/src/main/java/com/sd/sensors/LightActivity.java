package com.sd.sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.net.wifi.WifiManager;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.widget.TextView;
import android.widget.SeekBar;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.provider.Settings;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import android.util.Log;

public class LightActivity extends AppCompatActivity implements SensorEventListener {

    final String STRING_SENSOR_TYPE = "LIGHT";

    final int SENSOR_TYPE = Sensor.TYPE_LIGHT;

    private String deviceID;

    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Light sensors, as retrieved from the sensor manager.
    private Sensor mSensor;

    // TextViews to display current sensor values.
    private TextView mTextSensor;

    private TextView mTextFrequency;

    private TextView mTextLastSent;

    private TextView mTextConnection;

    private boolean active = false;

    private double frequency = 0.5;

    private float currentValue = 0;

    private ArrayList<java.net.InetAddress>  gatewayAddr = new ArrayList<java.net.InetAddress>();

    private String groupAddr = "239.0.1.2";

    private int gatewayPort = 5003;

    private int groupPort = 20480;

    private int localPort;

    private WifiManager.MulticastLock lock;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static final String EXCHANGE_NAME = "topic_logs";
    ConnectionFactory factory = null;
    Connection connection = null;
    Channel channel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);

        mTextLastSent = findViewById(R.id.label_last_time);
        mTextLastSent.setText("No messages sent");

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        active = true;

        mTextConnection = (TextView) findViewById(R.id.textConnection);

        // Initialize all view variables.
        mTextSensor = (TextView) findViewById(R.id.label_magnetic);

        // Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get light and proximity sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);

        // Get the error message from string resources.
        String sensor_error = getResources().getString(R.string.error_no_sensor);

        // If either mSensor or mSensorProximity are null, those sensors
        // are not available in the device.  Set the text to the error message
        if (mSensor == null) { mTextSensor.setText(sensor_error); }

        // You must use the WifiManager to create a multicast lock in order to receive
        // multicast packets. Only do this while you're actively receiving data, because
        // it decreases battery life.
        // See: https://bugreports.qt.io/browse/QTBUG-34111
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
    protected void onDestroy() {
        lock.release();
        active = false;
        closeConnection();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        active = true;
        super.onStart();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onPause().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL)
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        active = false;
        super.onStop();
        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is paused.
        mSensorManager.unregisterListener(this);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // The sensor type (as defined in the Sensor class).
        int sensorType = sensorEvent.sensor.getType();

        // The new data value of the sensor.  Both the light and proximity
        // sensors report one value at a time, which is always the first
        // element in the values array.
        float currentValue = sensorEvent.values[0];

        // Event came from the light sensor.
        if(sensorType == SENSOR_TYPE) {
            // Set the light sensor text view to the light sensor string
            // from the resources, with the placeholder filled in.
            this.currentValue = currentValue;
            mTextSensor.setText(getResources().getString(
                    R.string.label_light, currentValue));
        }
    }

    /**
     * Abstract method in SensorEventListener.  It must be implemented, but is
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void closeConnection() {
        Thread thread = new Thread(() -> {
            try {
                channel.close();
                connection.close();
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } catch (java.util.concurrent.TimeoutException e) {
                System.out.println("TimeoutException: " + e.getMessage());
            } catch (java.lang.NullPointerException e) {
                System.out.println("NullPointerException: " + e.getMessage());
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
                        String message = String.valueOf(currentValue);
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
