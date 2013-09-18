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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Code duplication! All cache store tests should be abstracted somehwere else, however...
 * Wait for JDG functional tests to be migrated here...
 * <p/>
 * Tests example configuration file standalone-leveldb-cs-local.xml.
 * There are 3 caches tested: default, memcachedCache, namedCache with their respective cache store names: dc, mc, nc
 *
 * @author Galder Zamarreño
 */
@RunWith(Arquillian.class)
public class LevelDBCacheStoreConfigExampleTest {

    final String DEFAULT_CACHE_MANAGER = "local";
    final String DEFAULT_CACHE = "default";
    final String MEMCACHED_CACHE = "memcachedCache";
    final String NAMED_CACHE = "namedCache";

    @InfinispanResource
    RemoteInfinispanServer server;

    RemoteCacheManager m;

    String relativeCacheStorePath = System.getProperty("server1.dist") + File.separator + "standalone" + File.separator + "data";

    @Before
    public void setUp() {
        m = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server.getHotrodEndpoint().getPort())
                .build());
    }

    @Test
    public void testDefaultCache() throws Exception {
        doPutGet(DEFAULT_CACHE, "level-dc");
    }

    @Test
    public void testMemcachedCache() throws Exception {
        doPutGet(MEMCACHED_CACHE, "level-mc");
    }

    @Test
    public void testNamedCache() throws Exception {
        RemoteCache<String, String> cache = m.getCache(NAMED_CACHE);
        assertEquals(0, numEntries(NAMED_CACHE));
        for (int i = 0; i < 10; i++) {
            cache.put("k" + i, "v" + i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("v" + i, cache.get("k" + i));
        }
        // all entries are in store (passivation=false)
        assertEquals(10, numEntries(NAMED_CACHE));

        Thread.sleep(2100); //the lifespan is 2000ms so we need to wait longer

        // entries expired
        for (int i = 0; i < 10; i++) {
            assertNull(cache.get("k" + i));
        }
        File f = new File(relativeCacheStorePath, "leveldb-nc");
        assertTrue(f.isDirectory());
    }

    private void doPutGet(String cacheName, String filePath) {
        RemoteCache<String, String> cache = m.getCache(cacheName);
        assertEquals(0, numEntries(cacheName));
        for (int i = 0; i < 10; i++) {
            cache.put("k" + i, "v" + i);
        }
        assertTrue(numEntries(cacheName) <= 10);
        for (int i = 0; i < 10; i++) {
            assertEquals("v" + i, cache.get("k" + i));
        }
        File f = new File(relativeCacheStorePath, filePath);
        assertTrue(f.isDirectory());
    }

    private long numEntries(String cacheName) {
        return server.getCacheManager(DEFAULT_CACHE_MANAGER).getCache(cacheName).getNumberOfEntries();
    }
}
