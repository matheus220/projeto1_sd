package com.sd.sensors;

//import com.sd.sensors.SensorServiceGRPCGrpc.SensorServiceGRPCImplBase;

import io.grpc.stub.StreamObserver;

public class MyGrpcServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {

    private Activator act;

    public MyGrpcServiceImpl(){

    }

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

        //this.act.Do();

        Sensor s = Sensor.newBuilder().setData("Sensor Ativado").build();
        Message response = Message.newBuilder().addSensors(s).build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    /*
    public void send(Object request, StreamObserver<Object> responseObserver){
        System.out.println(request);
        System.out.println("someone called me");



        String comm = request.getCommand();

        Object s = Sensor.newBuilder().setData("Sensor Ativado").build();



        Message response = Message.newBuilder().addSensors(s).build();

        this.act.Do();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
*/
}
