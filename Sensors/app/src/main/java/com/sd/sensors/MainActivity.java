package com.sd.sensors;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.GridLayout;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class MainActivity extends AppCompatActivity {

    GridLayout mainGrid;
    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainGrid = (GridLayout) findViewById(R.id.mainGrid);
        /*
        Server server = ServerBuilder.forPort(8094)
                                     .addService(new MyGrpcServiceImpl()).build();

        try{
            server.start();

            server.awaitTermination();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }*/

        //Set Event
        setSingleEvent(mainGrid);
    }

    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (finalI == 0) {
                        Intent intent = new Intent(MainActivity.this, SoundActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 1) {
                        Intent intent = new Intent(MainActivity.this, LedActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 2) {
                        Intent intent = new Intent(MainActivity.this, MagneticActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 3) {
                        Intent intent = new Intent(MainActivity.this, LightActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /*
    public static class MyGrpcServiceImpl extends SensorServiceGRPCGrpc.SensorServiceGRPCImplBase {
        @Override
        public void send(Command request, StreamObserver<Message> responseObserver){
            System.out.println(request);
            logger.info("someone called me");

            String comm = request.getCommand();

            Sensor s = Sensor.newBuilder().setData("Hello").build();

            Message response = Message.newBuilder().setSensors(0, s).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }
    }*/

}
