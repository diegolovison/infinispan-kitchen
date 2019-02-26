package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import io.grpc.stub.StreamObserver;

public class CacheServiceImpl extends CacheServiceGrpc.CacheServiceImplBase {

   @Override
   public StreamObserver<CacheRequest> put(StreamObserver<CacheResponse> responseObserver) {
      return new StreamObserver<>() {

         @Override
         public void onNext(CacheRequest cacheRequest) {
            GInfinispanRpcCacheManager.getInstance()
                  .putAsync(cacheRequest.getName(), cacheRequest.getKey(), cacheRequest.getValue())
                  .thenAccept((v) -> responseObserver.onNext(CacheResponse.newBuilder().setValue(v != null ? v.toString() : null).build()));
         }

         @Override
         public void onError(Throwable throwable) {
            // TODO
         }

         @Override
         public void onCompleted() {
            responseObserver.onCompleted();
         }
      };
   }

   @Override
   public StreamObserver<CacheRequest> get(StreamObserver<CacheResponse> responseObserver) {
      return new StreamObserver<>() {
         @Override
         public void onNext(CacheRequest cacheRequest) {
            GInfinispanRpcCacheManager.getInstance()
                  .getAsync(cacheRequest.getName(), cacheRequest.getKey())
                  .thenAccept((v) -> responseObserver.onNext(CacheResponse.newBuilder().setValue(v != null ? v.toString() : null).build()));
         }

         @Override
         public void onError(Throwable throwable) {
            // TODO
         }

         @Override
         public void onCompleted() {
            responseObserver.onCompleted();
         }
      };
   }
}
