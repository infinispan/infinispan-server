package org.infinispan.server.test.configs;

import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the example configuration standalone-rcs-local.xml. Tests standard put/remove operations and eviction and
 * verifies that the number of entries in local caches and cache stores is correct.
 *
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 */
@RunWith(Arquillian.class)
public class RemoteCacheStoreConfigExampleTest {

    final String CONTAINER1 = "container1"; // manual container
    final String CONTAINER2 = "container2"; // suite container
    final String DEFAULT_CACHE_NAME = "default";
    final String MEMCACHED_CACHE_NAME = "memcachedCache";
    final String NAMED_CACHE_NAME = "namedCache";
    final String DEFAULT_CACHE_MANAGER = "local";

    RemoteCache<Object, Object> cache;

    @InfinispanResource(CONTAINER1)
    RemoteInfinispanServer server1;

    @InfinispanResource(CONTAINER2)
    RemoteInfinispanServer server2;

    @ArquillianResource
    ContainerController controller;

    RemoteCacheManager rcm1;
    RemoteCacheManager rcm2;

    @Before
    public void setUp() throws Exception {
        controller.start(CONTAINER1);
        rcm1 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server1.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server1.getHotrodEndpoint().getPort())
                .build());
        rcm2 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server2.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server2.getHotrodEndpoint().getPort())
                .build());
    }

    @After
    public void tearDown() throws Exception {
        controller.stop(CONTAINER1);
        rcm2.getCache(DEFAULT_CACHE_NAME).clear();
        rcm2.getCache(MEMCACHED_CACHE_NAME).clear();
        rcm2.getCache(NAMED_CACHE_NAME).clear();
    }

    @Test
    public void testDefaultCache() throws Exception {
        doPutGetRemove(rcm1, DEFAULT_CACHE_NAME);
    }

    @Test
    public void testMemcachedCache() throws Exception {
        doPutGetRemove(rcm1, MEMCACHED_CACHE_NAME);
    }

    @Test
    public void testNamedCache() throws Exception {
        doPutGetWithExpiration(rcm1, NAMED_CACHE_NAME);
    }

    private void doPutGetRemove(RemoteCacheManager rm, String cacheName) {
        RemoteCache<String, String> cache = rm.getCache(cacheName);
        assertEquals(0, numEntries(server1, cacheName));
        assertEquals(0, numEntries(server2, cacheName));

        for (int i = 0; i < 1100; i++) {
            cache.put("key" + i, "value" + i);
        }
        assertTrue(numEntries(server1, cacheName) <= 1000);
        assertEquals(1100, numEntries(server1, cacheName) + numEntries(server2, cacheName));

        for (int i = 0; i < 1100; i++) {
            assertNotNull(cache.get("key" + i));
            cache.remove("key" + i);
            assertNull(cache.get("key" + i));
        }
        assertEquals(0, numEntries(server1, cacheName));
        assertEquals(0, numEntries(server2, cacheName));
    }

    /*
    * This is only for namedCache, which has expiration max-idle set to 1000 - we can't afford to do
    * much in that time
    */
    private void doPutGetWithExpiration(RemoteCacheManager rm, String cacheName) throws Exception {
        RemoteCache<String, String> cache = rm.getCache(cacheName);
        assertEquals(0, numEntries(server1, cacheName));
        assertEquals(0, numEntries(server2, cacheName));

        for (int i = 0; i < 10; i++) {
            cache.put("key" + i, "value" + i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, cache.get("key" + i));
        }
        // all entries are in store (passivation=false)
        assertEquals(10, numEntries(server2, cacheName));

        Thread.sleep(2100); //the lifespan is 2000ms so we need to wait more

        // entries expired
        for (int i = 0; i < 10; i++) {
            assertNull(cache.get("key" + i));
        }
    }

    private long numEntries(RemoteInfinispanServer server, String cacheName) {
        return server.getCacheManager(DEFAULT_CACHE_MANAGER).getCache(cacheName).getNumberOfEntries();
    }
}
