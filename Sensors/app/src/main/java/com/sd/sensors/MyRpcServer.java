package com.sd.sensors;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;

public class MyRpcServer {
    private static final Logger logger = Logger.getLogger(MyRpcServer.class.getName());
    private int port = 50051;
    private Server server;

    public void start(Activator act){
        try{

            ServerInterceptor si = new ServerInterceptor() {
                @Override
                public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                    logger.info("ativando algo");
                    act.Do();
                    return next.startCall(call,headers);
                }
            };
            server = ServerBuilder.forPort(port)
                    .addService(new MyGrpcServiceImpl(act))
                    .addService(ServerInterceptors.intercept(new MyGrpcServiceImpl(act),si))
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
        my.start(null);
        my.blockUntilShutdown();
    }
}
