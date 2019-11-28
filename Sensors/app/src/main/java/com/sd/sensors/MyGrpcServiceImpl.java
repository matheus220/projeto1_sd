package com.sd.sensors;

import com.sd.sensors.SensorServiceGRPCGrpc.SensorServiceGRPCImplBase;

import io.grpc.stub.StreamObserver;

public class MyGrpcServiceImpl extends SensorServiceGRPCImplBase {

    private Activator act;

    public MyGrpcServiceImpl(){

    }

    public MyGrpcServiceImpl(Activator activator){
        this.act = activator;
    }

    @Override
    public void send(Command request, StreamObserver<Message> responseObserver){
        System.out.println(request);
        System.out.println("someone called me");

        String comm = request.getCommand();

        Sensor s = Sensor.newBuilder().setData("Sensor Ativado").build();

        Message response = Message.newBuilder().setSensors(999, s).build();

        this.act.Do();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

}
