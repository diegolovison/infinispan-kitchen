package com.diegolovison.github.infinispan.kitchen.ginfinispanrpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class GInfinispanRpcCacheManager {

   private static final String CACHE_CONFIG_NAME = "___grpc_config";

   private static class InstanceHolder {
      private static final GInfinispanRpcCacheManager INSTANCE = new GInfinispanRpcCacheManager();
   }

   private static EmbeddedCacheManager cacheManager;

   private GInfinispanRpcCacheManager() {
   }

   public static GInfinispanRpcCacheManager getInstance() {
      return InstanceHolder.INSTANCE;
   }

   public void start(String clusterName) {
      cacheManager = new DefaultCacheManager(GlobalConfigurationBuilder.defaultClusteredBuilder()
            .transport().clusterName(clusterName)
            .build());
   }

   public <K, V> Cache<K, V> getOrCreateCache(String cacheName) {
      if (!cacheManager.cacheExists(cacheName)) {
         return cacheManager.createCache(cacheName, new ConfigurationBuilder()
               .clustering()
               .cacheMode(CacheMode.DIST_SYNC)
               .hash().numOwners(2)
               .build());
      } else {
         return cacheManager.getCache(cacheName);
      }
   }

   public void putConfig(String key, String value) {
      getOrCreateCache(CACHE_CONFIG_NAME).put(key, value);
   }

   public CompletableFuture<Object> putAsync(String cacheName, String key, String value) {
      return getOrCreateCache(cacheName).putAsync(key, value);
   }

   public CompletableFuture<Object> getAsync(String cacheName, String key) {
      return getOrCreateCache(cacheName).getAsync(key);
   }

   public String getAddress() {
      return cacheManager.getAddress().toString();
   }
}
