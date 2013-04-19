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

import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.arquillian.model.RemoteInfinispanCache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test example-configuration file clustered-xsite.xml.
 * Create 2 clusters (=sites), LON (node0, node1) and NYC (node2), and check that data put to LON/node0 is backupped
 * in NYC and replicated in LON/node1. SFO site is not used.
 *
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 */
@RunWith(Arquillian.class)
public class XsiteConfigExampleTest {

   final String DEFAULT_CACHE_NAME = "default";
   final String CACHE_MANAGER_NAME = "clustered";

   final String CONTAINER1 = "container1";
   final String CONTAINER2 = "container2";
   final String CONTAINER3 = "container3";

   @InfinispanResource(CONTAINER1)
   RemoteInfinispanServer server1;

   @InfinispanResource(CONTAINER2)
   RemoteInfinispanServer server2;

   @InfinispanResource(CONTAINER3)
   RemoteInfinispanServer server3;

   RemoteCacheManager rcm1;
   RemoteCacheManager rcm2;
   RemoteCacheManager rcm3;

   @Before
   public void setUp() {
      rcm1 = new RemoteCacheManager(server1.getHotrodEndpoint().getInetAddress().getHostName(), server1.getHotrodEndpoint()
            .getPort());
      rcm2 = new RemoteCacheManager(server2.getHotrodEndpoint().getInetAddress().getHostName(), server2.getHotrodEndpoint()
            .getPort());
      rcm3 = new RemoteCacheManager(server3.getHotrodEndpoint().getInetAddress().getHostName(), server3.getHotrodEndpoint()
            .getPort());
   }

   @Test
   public void testXsiteConfig() throws Exception {
      RemoteInfinispanCache ric1 = server1.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteInfinispanCache ric2 = server2.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteInfinispanCache ric3 = server3.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc1 = rcm1.getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc2 = rcm2.getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc3 = rcm3.getCache(DEFAULT_CACHE_NAME);
      assertEquals(0, ric1.getNumberOfEntries());
      assertEquals(0, ric2.getNumberOfEntries());

      assertEquals(2, server1.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
      assertEquals(2, server2.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
      assertEquals(1, server3.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());

      rc1.put("k1", "v1");
      rc1.put("k2", "v2");
      assertEquals(2, ric1.getNumberOfEntries());
      assertEquals(2, ric2.getNumberOfEntries());
      assertEquals(2, ric3.getNumberOfEntries());

      assertTrue(rc1.get("k1").equals("v1"));
      assertTrue(rc2.get("k1").equals("v1"));
      assertTrue(rc3.get("k1").equals("v1"));
      assertTrue(rc1.get("k2").equals("v2"));
      assertTrue(rc2.get("k2").equals("v2"));
      assertTrue(rc3.get("k2").equals("v2"));
   }
}
