package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloServiceClient {

   public static void main(String[] args) {

      ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();

      HelloServiceGrpc.HelloServiceBlockingStub stub
            = HelloServiceGrpc.newBlockingStub(channel);

      HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
            .setFirstName("Diego")
            .setLastName("Lovison")
            .build());

      System.out.println(helloResponse.toString());

      channel.shutdown();
   }
}
