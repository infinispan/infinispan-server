package org.infinispan.server.test.configs;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.server.test.client.memcached.MemcachedClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test standalone-compatibility-mode.xml example configuration file.
 *
 * Writing via Memcached is not tested as this requires a custom marshaller to be used.
 * Memcached is used only for reading values written by REST and HotRod.
 *
 * @author Martin Gencur
 */
@RunWith(Arquillian.class)
public class CompatibilityModeConfigExampleTest {

   final String DEFAULT_CACHE = "default";

   @InfinispanResource
   RemoteInfinispanServer server;

   RemoteCache<String, byte[]> hotrodCache;
   HttpClient restClient;
   MemcachedClient memcachedClient;
   String restUrl;

   @Before
   public void setUp() throws Exception {
      hotrodCache = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                                                 .host(server.getHotrodEndpoint().getInetAddress().getHostName())
                                                 .port(server.getHotrodEndpoint().getPort())
                                                 .build()).getCache();
      restClient = new HttpClient();
      restUrl = "http://" + server.getHotrodEndpoint().getInetAddress().getHostName() + ":8080" + server.getRESTEndpoint().getContextPath() + "/" + DEFAULT_CACHE;
      memcachedClient = new MemcachedClient(server.getMemcachedEndpoint().getInetAddress().getHostName(), server.getMemcachedEndpoint().getPort());
   }

   @After
   public void tearDown() throws Exception {
      memcachedClient.close();
   }

   @Test
   public void testHotRodPutRestMemcachedGet() throws Exception {
      final String key = "1";

      // 1. Put with Hot Rod
      assertEquals(null, hotrodCache.withFlags(Flag.FORCE_RETURN_VALUE).put(key, "v1".getBytes()));
      assertArrayEquals("v1".getBytes(), hotrodCache.get(key));

      // 2. Get with REST
      HttpMethod get = new GetMethod(restUrl + "/" + key);
      restClient.executeMethod(get);
      assertEquals(HttpServletResponse.SC_OK, get.getStatusCode());
      assertArrayEquals("v1".getBytes(), get.getResponseBody());

      // 3. Get with Memcached
      assertArrayEquals("v1".getBytes(), readWithMemcachedAndDeserialize(key));
   }

   @Test
   public void testRestPutHotRodMemcachedGet() throws Exception {
      final String key = "2";

      // 1. Put with REST
      EntityEnclosingMethod put = new PutMethod(restUrl + "/" + key);
      put.setRequestEntity(new ByteArrayRequestEntity(
            "<hey>ho</hey>".getBytes(), "application/octet-stream"));
      restClient.executeMethod(put);
      assertEquals(HttpServletResponse.SC_OK, put.getStatusCode());

      // 2. Get with Hot Rod
      assertArrayEquals("<hey>ho</hey>".getBytes(), hotrodCache.get(key));

      // 3. Get with Memcached
      assertArrayEquals("<hey>ho</hey>".getBytes(), readWithMemcachedAndDeserialize(key));
   }

   /*
    * Need to de-serialize the object as the default JavaSerializationMarshaller is used by Memcached endpoint.
    */
   private byte[] readWithMemcachedAndDeserialize(String key) throws Exception {
      ByteArrayInputStream bais = new ByteArrayInputStream(memcachedClient.getBytes(key));
      ObjectInputStream ois = new ObjectInputStream(bais);
      return (byte[]) ois.readObject();
   }
}
