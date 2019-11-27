package com.sd.sensors;

import com.sd.sensors.SensorServiceGRPCGrpc.SensorServiceGRPCImplBase;

import io.grpc.stub.StreamObserver;

public class MyGrpcServiceImpl extends SensorServiceGRPCImplBase {

    @Override
    public void send(Command request, StreamObserver<Message> responseObserver){
        System.out.println(request);
        System.out.println("someone called me");

        String comm = request.getCommand();

        Sensor s = Sensor.newBuilder().setData("Hello").build();

        Message response = Message.newBuilder().setSensors(0, s).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

}
