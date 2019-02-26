package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class HelloServiceClient {

   private static final Logger logger = Logger.getLogger(HelloServiceClient.class.getName());

   public static void main(String[] args) throws InterruptedException {

      ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8088)
            .usePlaintext()
            .build();

      HelloServiceGrpc.HelloServiceStub async
            = HelloServiceGrpc.newStub(channel);

      final CountDownLatch finishLatch = new CountDownLatch(1);
      StreamObserver<HelloRequest> requestObserver = async.hello(new StreamObserver<HelloResponse>() {
         @Override
         public void onNext(HelloResponse helloResponse) {
            System.out.println(helloResponse.toString());
         }

         @Override
         public void onError(Throwable t) {
            logger.log(Level.WARNING, "route client failed: {0}", Status.fromThrowable(t));
            finishLatch.countDown();
         }

         @Override
         public void onCompleted() {
            finishLatch.countDown();
         }
      });
      requestObserver.onNext(HelloRequest.newBuilder()
            .setFirstName("Diego")
            .setLastName("Lovison")
            .build());

      requestObserver.onCompleted();

      finishLatch.await(1, TimeUnit.MINUTES);

      channel.shutdown();
   }
}
