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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Test example-configuration file clustered-two-nodes.xml. Simply create cluster of 2 nodes and use
 * defined default distributed cache.
 *
 * @author <a href="mailto:tsykora@redhat.com">Tomas Sykora</a>
 */
@RunWith(Arquillian.class)
public class TwoNodesConfigExampleTest {

   final String CONTAINER1 = "container1";
   final String CONTAINER2 = "container2";

   final String DEFAULT_CACHE_NAME = "default";
   final String CACHE_MANAGER_NAME = "clustered";

   @InfinispanResource(CONTAINER1)
   RemoteInfinispanServer server1;

   @InfinispanResource(CONTAINER2)
   RemoteInfinispanServer server2;

   RemoteCacheManager rcm1;
   RemoteCacheManager rcm2;

   @Before
   public void setUp() {
      rcm1 = new RemoteCacheManager(server1.getHotrodEndpoint().getInetAddress().getHostName(), server1.getHotrodEndpoint()
            .getPort());
      rcm2 = new RemoteCacheManager(server2.getHotrodEndpoint().getInetAddress().getHostName(), server2.getHotrodEndpoint()
            .getPort());
   }

   @Test
   public void tesTwoNodesConfigExample() throws Exception {
      RemoteInfinispanCache ric = server1.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteInfinispanCache ric2 = server2.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc1 = rcm1.getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc2 = rcm2.getCache(DEFAULT_CACHE_NAME);
      assertEquals(0, ric.getNumberOfEntries());
      assertEquals(0, ric2.getNumberOfEntries());
      Assert.assertEquals(2, server1.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
      Assert.assertEquals(2, server2.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
      rc1.put("k", "v");
      rc1.put("k2", "v2");
      assertTrue(rc1.get("k").equals("v"));
      assertTrue(rc1.get("k2").equals("v2"));
      assertEquals(2, ric.getNumberOfEntries());
      rc2.put("k3", "v3");
      assertEquals(3, ric2.getNumberOfEntries());
      assertEquals("v", rc1.get("k"));
      assertEquals("v", rc2.get("k"));
      assertEquals("v2", rc1.get("k2"));
      assertEquals("v2", rc2.get("k2"));
      assertEquals("v3", rc1.get("k3"));
      assertEquals("v3", rc2.get("k3"));
   }
}
