package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

   private static final Logger logger = Logger.getLogger(HelloServiceImpl.class.getName());

   @Override
   public StreamObserver<HelloRequest> hello(StreamObserver<HelloResponse> responseObserver) {
      return new StreamObserver<>() {

         @Override
         public void onNext(HelloRequest request) {
            String greeting = new StringBuilder()
                  .append("Hello, ")
                  .append(request.getFirstName())
                  .append(" ")
                  .append(request.getLastName())
                  .toString();

            HelloResponse response = HelloResponse.newBuilder()
                  .setGreeting(greeting)
                  .build();

            responseObserver.onNext(response);
         }

         @Override
         public void onError(Throwable t) {
            logger.log(Level.WARNING, "route server canceled: {0}", Status.fromThrowable(t));
         }

         @Override
         public void onCompleted() {
            responseObserver.onCompleted();
         }
      };
   }
}
