package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {

   public static void main(String[] args) throws IOException, InterruptedException {

      // start cache cluster
      String clusterName = required(args.length > 0 ? args[0] : null);
      GInfinispanRpcCacheManager.getInstance().start(clusterName);

      // grpc server config
      int port = ThreadLocalRandom.current().nextInt(8080, 8090);

      // start grpc server
      Server server = ServerBuilder
            .forPort(port)
            .addService(new HelloServiceImpl())
            .addService(new CacheServiceImpl())
            .build();
      server.start();
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            System.err.println("*** server shut down");
         }
      });

      // store config
      GInfinispanRpcCacheManager.getInstance().putConfig(GInfinispanRpcCacheManager.getInstance().getAddress(), String.valueOf(port));

      System.out.println("Server up and running: " + server);
   }

   private static final String required(String param) {
      if (param == null || param.trim().length() == 0) {
         throw new IllegalStateException("Cannot be null");
      }
      return param;
   }
}
