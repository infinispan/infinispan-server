package org.infinispan.server.test.configs;

import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.HttpMethod;
import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.commons.httpclient.HttpClient;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

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

   /**
    * A Really simple Memcached client.
    *
    * @author Michal Linhard
    * @author Martin Gencur
    */
   static class MemcachedClient {

      private static final int DEFAULT_TIMEOUT = 10000;
      private static final String DEFAULT_ENCODING = "UTF-8";

      private String encoding;
      private Socket socket;
      private PrintWriter out;
      private InputStream input;

      public MemcachedClient(String host, int port) throws IOException {
         this(DEFAULT_ENCODING, host, port, DEFAULT_TIMEOUT);
      }

      public MemcachedClient(String enc, String host, int port, int timeout) throws IOException {
         encoding = enc;
         socket = new Socket(host, port);
         socket.setSoTimeout(timeout);
         out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), encoding));
         input = socket.getInputStream();
      }

      public String get(String key) throws IOException {
         byte[] data = getBytes(key);
         return (data == null ) ? null : new String(data, encoding);
      }

      public byte[] getBytes(String key) throws IOException {
         writeln("get " + key);
         flush();
         String valueStr = readln();
         if (valueStr.startsWith("VALUE")) {
            String[] value = valueStr.split(" ");
            assertEquals(key, value[1]);
            int size = new Integer(value[3]);
            byte[] ret = read(size);
            assertEquals('\r', read());
            assertEquals('\n', read());
            assertEquals("END", readln());
            return ret;
         } else {
            return null;
         }
      }

      public void set(String key, String value) throws IOException {
         writeln("set " + key + " 0 0 " + value.getBytes(encoding).length);
         writeln(value);
         flush();
         assertEquals("STORED", readln());
      }

      public String getCasId(String aKey) throws IOException {
         writeln("gets " + aKey);
         flush();
         String[] valueline = readln().split(" ");
         assertEquals("VALUE", valueline[0]);
         assertEquals(aKey, valueline[1]);
         read(new Integer(valueline[3]));
         assertEquals("", readln());
         assertEquals("END", readln());
         return valueline[4];
      }

      private byte[] read(int len) throws IOException {
         try {
            byte[] ret = new byte[len];
            input.read(ret, 0, len);
            return ret;
         } catch (SocketTimeoutException ste) {
            return null;
         }
      }

      private byte read() throws IOException {
         try {
            return (byte) input.read();
         } catch (SocketTimeoutException ste) {
            return -1;
         }
      }

      private String readln() throws IOException {
         byte[] buf = new byte[512];
         int maxlen = 512;
         int read = 0;
         buf[read] = read();
         while (buf[read] != '\n') {
            read++;
            if (read == maxlen) {
               maxlen += 512;
               buf = Arrays.copyOf(buf, maxlen);
            }
            buf[read] = read();
         }
         if (read == 0) {
            return "";
         }
         if (buf[read - 1] == '\r') {
            read--;
         }
         buf = Arrays.copyOf(buf, read);
         return new String(buf, encoding);
      }

      private void writeln(String str) {
         out.print(str + "\r\n");
      }

      private void write(byte[] data) throws IOException {
         socket.getOutputStream().write(data);
      }

      private void flush() {
         out.flush();
      }

      private void close() throws IOException {
         socket.close();
      }
   }
}
