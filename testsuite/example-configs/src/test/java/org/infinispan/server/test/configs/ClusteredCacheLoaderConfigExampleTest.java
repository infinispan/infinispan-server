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
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


/**
 * Test for example-configuration clustered-ccl.xml. Create a 2 node cluster and check
 * that state transfer does not take place.
 *
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 */
@RunWith(Arquillian.class)
public class ClusteredCacheLoaderConfigExampleTest {

   final String CONTAINER1 = "container1";
   final String CONTAINER2 = "container2";
   final String DEFAULT_CACHE_NAME = "default";
   final String CACHE_MANAGER_NAME = "clustered";

   @InfinispanResource(CONTAINER1)
   RemoteInfinispanServer server1;

   @InfinispanResource(CONTAINER2)
   RemoteInfinispanServer server2;

   @ArquillianResource
   ContainerController controller;

   RemoteCacheManager rcm1;
   RemoteCacheManager rcm2;

   @Test
   public void testClusterCacheLoaderConfigExample() throws Exception {
      controller.start(CONTAINER1);
      rcm1 = new RemoteCacheManager(server1.getHotrodEndpoint().getInetAddress().getHostName(),
                                    server1.getHotrodEndpoint().getPort());
      RemoteInfinispanCache ric1 = server1.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc1 = rcm1.getCache(DEFAULT_CACHE_NAME);
      rc1.put("key", "value");
      assertEquals(1, ric1.getNumberOfEntries());
      assertEquals(1, server1.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());

      controller.start(CONTAINER2);
      rcm2 = new RemoteCacheManager(server2.getHotrodEndpoint().getInetAddress().getHostName(),
                                    server2.getHotrodEndpoint().getPort());
      assertEquals(2, server2.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
      RemoteInfinispanCache ric2 = server2.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
      RemoteCache<String, String> rc2 = rcm2.getCache(DEFAULT_CACHE_NAME);
      // state transfer didn't happen
      assertEquals(0, ric2.getNumberOfEntries());
      rc2.get("key");
      // the entry is obtained
      assertEquals(1, ric2.getNumberOfEntries());
      rc2.put("key2", "value2");
      assertEquals(2, ric1.getNumberOfEntries());
      assertEquals(2, ric2.getNumberOfEntries());
      controller.stop(CONTAINER1);
      controller.stop(CONTAINER2);
   }
}
