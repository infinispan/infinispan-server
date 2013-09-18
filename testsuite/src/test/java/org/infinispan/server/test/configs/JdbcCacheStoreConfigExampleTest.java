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
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests example configuration standalong-jdbc-clustered.
 *
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 */
@RunWith(Arquillian.class)
public class JdbcCacheStoreConfigExampleTest {

    final String CONTAINER1 = "container1";

    @InfinispanResource(CONTAINER1)
    RemoteInfinispanServer server;

    RemoteCacheManager rcm;

    @Before
    public void setUp() throws Exception {
        rcm = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server.getHotrodEndpoint().getPort())
                .build());
    }

    @Test
    public void testNamedCache() throws Exception {
        RemoteCache<String, String> cache = rcm.getCache("namedCache");
        cache.put("key", "value");
        cache.put("key2", "value2");
        assertEquals("value", cache.get("key"));
        assertEquals("value2", cache.get("key2"));
    }

    @Test
    public void testDefaultCache() throws Exception {
        RemoteCache<String, String> cache = rcm.getCache("default");
        // 1001, so we are 100% sure that at least 1 entry is evicted and thus stored (passivation = true)
        for (int i = 0; i < 1001; i++) {
            cache.put("k" + i, "v" + i);
        }
        for (int i = 0; i < 1001; i++) {
            assertEquals("v" + i, cache.get("k" + i));
        }
    }

}