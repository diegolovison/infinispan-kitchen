package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class CacheClient {

   public static void main(String[] args) throws InterruptedException {

      Integer port = Integer.valueOf(args[0]);

      ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
            .usePlaintext()
            .build();

      CacheServiceGrpc.CacheServiceStub async
            = CacheServiceGrpc.newStub(channel);

      int times = 1000;
      putSampleData(async, times);
      getData(async, times);

      channel.shutdown();
   }

   private static void putSampleData(CacheServiceGrpc.CacheServiceStub async, int times) throws InterruptedException {
      final CountDownLatch finishLatch = new CountDownLatch(1);
      StreamObserver<CacheRequest> putRequestObserver = async.put(new StreamObserver<>() {
         @Override
         public void onNext(CacheResponse response) {
            System.out.println("put: " + response.getValue());
         }

         @Override
         public void onError(Throwable t) {
            // TODO
            finishLatch.countDown();
         }

         @Override
         public void onCompleted() {
            finishLatch.countDown();
         }
      });

      for (int i = 0; i < times; i++) {
         putRequestObserver.onNext(CacheRequest.newBuilder()
               .setName("fooCache")
               .setKey("fooKey" + i)
               .setValue("fooValue" + i)
               .build());
      }
      putRequestObserver.onCompleted();
      finishLatch.await(15, TimeUnit.SECONDS);
   }

   private static void getData(CacheServiceGrpc.CacheServiceStub async, int times) throws InterruptedException {
      final CountDownLatch finishLatch = new CountDownLatch(1);
      StreamObserver<CacheRequest> getRequestObserver = async.get(new StreamObserver<>() {

         @Override
         public void onNext(CacheResponse response) {
            System.out.println("get: " + response.getValue());
         }

         @Override
         public void onError(Throwable throwable) {
            // TODO
            finishLatch.countDown();
         }

         @Override
         public void onCompleted() {
            finishLatch.countDown();
         }
      });
      for (int i = 0; i < times; i++) {
         getRequestObserver.onNext(CacheRequest.newBuilder()
               .setName("fooCache")
               .setKey("fooKey" + i)
               .build());
      }
      getRequestObserver.onCompleted();
      finishLatch.await(15, TimeUnit.SECONDS);
   }
}
