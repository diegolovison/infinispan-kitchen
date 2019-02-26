package com.diegolovison.github.infinispan.kitchen.openshift;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.arquillian.cube.openshift.api.Template;
import org.arquillian.cube.openshift.api.Templates;
import org.arquillian.cube.openshift.impl.client.OpenShiftAssistant;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.test.api.ArquillianResource;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.openshift.client.OpenShiftClient;

@Category(RequiresOpenshift.class)
@RequiresOpenshift
@RunWith(ArquillianConditionalRunner.class)
@Templates(templates = {
      @Template(url = "classpath:ispn-template.yaml")
})
public class HotRodSslClientTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(HotRodSslClientTest.class);

   @ArquillianResource
   OpenShiftAssistant assistant;

   @ArquillianResource
   OpenShiftClient client;

   @RouteURL(value = "hotrod-router")
   private URL hotrodUrl;

   @RouteURL(value = "rest-router", path = "/rest")
   private URL resturl;

   @Test
   public void testHotRodwithSsl() {

      final ConfigMap keyStoreServerConfigMap = new ConfigMapBuilder().withNewMetadata().withName("keystore-server").endMetadata()
            .withBinaryData(Collections.singletonMap("keystore_server.jks", encodeFileToBase64Binary(HotRodSslClientTest.class.getResource("/keystore_server.jks").getFile())))
            .build();
      final ConfigMap clusteredConfigMap = new ConfigMapBuilder().withNewMetadata().withName("custom-clustered").endMetadata()
            .withData(Collections.singletonMap("custom-clustered.xml", getFileContent(HotRodSslClientTest.class.getResource("/custom-clustered.xml").getFile())))
            .build();

      client.configMaps().create(keyStoreServerConfigMap);
      client.configMaps().create(clusteredConfigMap);

      // we should wait here because we need to create the configMap, otherwise the server won't start TODO ?
      assistant.awaitUrl(resturl, 401);

      RemoteCacheManager remoteCacheManager =
            new RemoteCacheManager(new ConfigurationBuilder()
                  .security().ssl().sniHostName(hotrodUrl.getHost()).trustStoreFileName(HotRodSslClientTest.class.getResource("/keystore_client.jks").getFile()).trustStorePassword("publicpws".toCharArray()).enable()
                  .clientIntelligence(ClientIntelligence.BASIC)
                  .addServer()
                  .host(hotrodUrl.getHost())
                  .port(443)
                  .build());

      RemoteCache<String, String> remoteCache = remoteCacheManager.administration()
                     .createCache("hello-cache", new org.infinispan.configuration.cache.ConfigurationBuilder()
                           .build());

      remoteCache.put("foo", "bar");

      assertEquals("bar", remoteCache.get("foo"));

      // clean TODO it is calling the test 4 times ??
      client.configMaps().delete();
      remoteCacheManager.administration().removeCache("hello-cache");
   }

   private String getFileContent(String fileName) {
      Path filePath = Paths.get(fileName);
      try {
         return new String(Files.readAllBytes(filePath));
      } catch (IOException e) {
         throw new IllegalStateException("Unable to read file at path: " + filePath.toAbsolutePath().toString());
      }
   }

   private String encodeFileToBase64Binary(String fileName) {
      Path filePath = Paths.get(fileName);
      try {
         byte[] bytes = Files.readAllBytes(filePath);
         byte[] encoded = Base64.encodeBase64(bytes);
         String encodedString = new String(encoded);
         return encodedString;
      } catch (IOException e) {
         throw new IllegalStateException("Unable to read file at path: " + filePath.toAbsolutePath().toString());
      }
   }
}
