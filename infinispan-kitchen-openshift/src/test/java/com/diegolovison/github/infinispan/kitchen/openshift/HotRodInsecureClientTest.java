package com.diegolovison.github.infinispan.kitchen.openshift;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.arquillian.cube.openshift.api.Template;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.client.OpenShiftClient;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@RunWith(ArquillianConditionalRunner.class)
public class HotRodInsecureClientTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(HotRodInsecureClientTest.class);

   @ArquillianResource
   OpenShiftAssistant assistant;

   @ArquillianResource
   OpenShiftClient client;

   @RouteURL(value = "infinispan-server")
   //@AwaitRoute(timeoutUnit = TimeUnit.SECONDS, timeout = 15) //we are not able to connect because hotrod won't return HTTP200
   private URL url;

   @Test
   @Template(url = "classpath:ispn-template.yaml")
   public void testCustomerRoute() {

      RemoteCacheManager remoteCacheManager =
            new RemoteCacheManager(new ConfigurationBuilder()
                  .clientIntelligence(ClientIntelligence.BASIC)
                  .nearCache().mode(NearCacheMode.INVALIDATED).maxEntries(100)
                  .statistics().enabled(true).jmxDisable()
                  .addServer()
                  .host(url.getHost())
                  .port(80)
                  .build());

      RemoteCache<String, String> remoteCache =
            remoteCacheManager.administration()
                  .createCache("hello-cache", new org.infinispan.configuration.cache.ConfigurationBuilder()
                        .build());

      remoteCache.put("foo", "bar");
   }

}
