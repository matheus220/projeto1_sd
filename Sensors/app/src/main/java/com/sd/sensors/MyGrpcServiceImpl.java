package com.sd.sensors;

import io.grpc.stub.StreamObserver;

public class MyGrpcServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {

    private Activator act;

    public MyGrpcServiceImpl(){  }

    public MyGrpcServiceImpl(Activator activator){
        this.act = activator;
    }

    @Override
    public void send(Command request, StreamObserver<Message> responseObserver) {
        System.out.println(request);
        System.out.println("GRPC call.");

        String command = request.getCommand();
        String[] infos = command.split(",");

        if(infos.length > 1){

            String act = infos[1];
            this.act.Do(new String[] { act });
        }else{
            this.act.Do();
        }
        Sensor s = Sensor.newBuilder().setData("Sensor Ativado").build();
        Message response = Message.newBuilder().addSensors(s).build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
