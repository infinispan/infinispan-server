/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.infinispan.server.test.core.memcached;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * A Really simple Memcached client/helper.
 *
 * @author <a href="mailto:mlinhard@redhat.com">Michal Linhard</a>
 * @version May 2011
 */
public class MemcachedHelper {

   public static final int DEFAULT_MEMCACHED_PORT = 11211;
   public static final int DEFAULT_TIMEOUT = 10000;

   /**
    * is there any standard replacement for this ? something that can read both binary data and also strings when needed.
    */
   public static class StringAndBytesReader {
      private InputStream input;
      private String encoding;
      private byte[] TEMP = new byte[1];

      public StringAndBytesReader(InputStream input, String encoding) {
         super();
         this.input = input;
         this.encoding = encoding;
      }

      public byte[] read(int len) throws IOException {
         try {
            byte[] ret = new byte[len];
            input.read(ret, 0, len);
            return ret;
         } catch (SocketTimeoutException ste) {
            Assert.fail("Read timeout");
            return null;
         }
      }

      public byte read() throws IOException {
         try {
            input.read(TEMP, 0, 1);
            return TEMP[0];
         } catch (SocketTimeoutException ste) {
            Assert.fail("Read timeout");
            return -1;
         }
      }

      public String readln() throws IOException {
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
   }

   private String encoding;
   private Socket socket;
   private PrintWriter out;
   private StringAndBytesReader in;

   public MemcachedHelper() throws IOException {
      this("UTF-8", "localhost", DEFAULT_MEMCACHED_PORT, DEFAULT_TIMEOUT);
   }

   public MemcachedHelper(String enc, String host, int port, int timeout) throws IOException {
      encoding = enc;
      socket = new Socket(host, port);
      socket.setSoTimeout(timeout);
      out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), encoding));
      in = new StringAndBytesReader(socket.getInputStream(), encoding);
   }

   public MemcachedHelper(String host, int port) throws IOException {
      this("UTF-8", host, port, DEFAULT_TIMEOUT);
   }

   public void writeln(String str) {
      out.print(str + "\r\n");
   }

   public void write(byte[] data) throws IOException {
      socket.getOutputStream().write(data);
   }

   public String readln() throws IOException {
      return in.readln();
   }

   public void flush() {
      out.flush();
   }

   public void close() throws IOException {
      socket.close();
   }

   /**
    * Returns the value for the key.
    */
   public String get(String key) throws IOException {
      byte[] data = getBytes(key);
      if (data == null) {
         return null;
      }
      return new String(data, encoding);
   }

   /**
    * Returns the value for the key.
    */
   public byte[] getBytes(String key) throws IOException {
      writeln("get " + key);
      flush();
      String valueStr = readln();
      if ("END".equals(valueStr)) {
         return null;
      }
      if (valueStr.startsWith("VALUE")) {
         String[] value = valueStr.split(" ");
         assertEquals(key, value[1]);
         int size = new Integer(value[3]);
         byte[] ret = in.read(size);
         assertEquals('\r', in.read());
         assertEquals('\n', in.read());
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

   public void set(String key, String value, int lifespan, int maxidle) throws IOException {
      writeln("set " + key + " " + lifespan + " " + maxidle + " " + value.getBytes(encoding).length);
      writeln(value);
      flush();
      assertEquals("STORED", readln());
   }

   public void setNoReadln(String key, String value) throws IOException {
      writeln("set " + key + " 0 0 " + value.getBytes(encoding).length);
      writeln(value);
      flush();
   }

   /**
    * returns "DELETED" or "NOT_FOUND" depending whether the key existed.
    */
   public String delete(String key) throws IOException {
      writeln("delete " + key);
      flush();
      return readln();
   }

   public Map<String, String> getStats() throws IOException {
      writeln("stats");
      flush();
      String statline = readln();
      Map<String, String> stats = new HashMap<String, String>();
      while (statline.startsWith("STAT")) {
         String[] stat = statline.split(" ");
         stats.put(stat[1], stat[2]);
         statline = readln();
      }
      assertEquals("END", statline);
      return stats;
   }

   /**
    * returns server time retrieved via stats command -1 if time stat is not returned.
    */
   public long getServerTime() throws IOException {
      writeln("stats");
      flush();
      String statline = readln();
      long time = -1;
      while (statline.startsWith("STAT")) {
         String[] stat = statline.split(" ");
         if (stat[1].equals("time")) {
            time = new Long(stat[2]);
         }
         statline = readln();
      }
      assertEquals("END", statline);
      return time;
   }

   public String getCasId(String aKey) throws IOException {
      writeln("gets " + aKey);
      flush();
      String[] valueline = readln().split(" ");
      assertEquals("VALUE", valueline[0]);
      assertEquals(aKey, valueline[1]);
      in.read(new Integer(valueline[3]));
      assertEquals("", readln());
      assertEquals("END", readln());
      return valueline[4];
   }
}