/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.infinispan.server.test.configs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests example configuration file standalone-hotrod-multiple.xml.
 *
 * @author Tristan Tarrant
 */
@RunWith(Arquillian.class)
public class MultiHotRodConfigExampleTest {
   final String DEFAULT_CACHE_MANAGER = "local";
   final String DEFAULT_CACHE = "default";
   final String NAMED_CACHE = "namedCache";

   @InfinispanResource
   RemoteInfinispanServer server;

   RemoteCacheManager m1;
   RemoteCacheManager m2;

   String serverConfigPath = System.getProperty("server1.dist") + File.separator + "standalone" + File.separator + "configuration";

   @Before
   public void setUp() {
      ConfigurationBuilder builder1 = new ConfigurationBuilder();
      builder1
         .addServer()
            .host("127.0.0.1") // HACK until Infinispan Arquillian supports named endpoints
            .port(11222);
      m1 = new RemoteCacheManager(builder1.build());
      ConfigurationBuilder builder2 = new ConfigurationBuilder();
      builder2
         .addServer()
            .host("127.0.0.1") // HACK until Infinispan Arquillian supports named endpoints
            .port(11223);
      m2 = new RemoteCacheManager(builder2.build());
   }

   @Test
   public void testDefaultCache() throws Exception {
      doPutGet(DEFAULT_CACHE);
   }

   private void doPutGet(String cacheName) {
      RemoteCache<String, String> cache1 = m1.getCache(cacheName);
      RemoteCache<String, String> cache2 = m2.getCache(cacheName);
      assertEquals(0, numEntries(cacheName));
      for (int i = 0; i < 10; i++) {
         cache1.put("k" + i, "v" + i);
      }
      assertTrue(numEntries(cacheName) <= 10);
      for (int i = 0; i < 10; i++) {
         assertEquals("v" + i, cache2.get("k" + i));
      }
   }

   private long numEntries(String cacheName) {
      return server.getCacheManager(DEFAULT_CACHE_MANAGER).getCache(cacheName).getNumberOfEntries();
   }
}
