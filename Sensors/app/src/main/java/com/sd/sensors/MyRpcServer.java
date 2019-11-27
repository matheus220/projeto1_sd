package com.sd.sensors;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class MyRpcServer {
    private static final Logger logger = Logger.getLogger(MyRpcServer.class.getName());
    private int port = 50051;
    private Server server;

    public void start(){
        try{
            server = ServerBuilder.forPort(port)
                    .addService(new MyGrpcServiceImpl())
                    .build()
                    .start();
            logger.info("Server started, listening on " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    MyRpcServer.this.stop();
                }
            });
        }catch(Exception e){

        }
    }

    public void stop(){
        if(server != null){
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final MyRpcServer my = new MyRpcServer();
        my.start();
        my.blockUntilShutdown();
    }
}
