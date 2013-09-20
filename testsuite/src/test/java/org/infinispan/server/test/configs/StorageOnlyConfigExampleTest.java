package org.infinispan.server.test.configs;

import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.arquillian.model.RemoteInfinispanCache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test the example-configuration clustered-storage-only.xml. Just form the cluster of 2 nodes. The
 * only difference is that we are NOT able to access the storage-only node via hotrod/memcached/rest endpoints. Cluster
 * should be formed normally.
 *
 * @author <a href="mailto:tsykora@redhat.com">Tomas Sykora</a>
 */
@RunWith(Arquillian.class)
public class StorageOnlyConfigExampleTest {

    final String DEFAULT_CACHE_NAME = "default";
    final String CACHE_MANAGER_NAME = "clustered";

    final String CONTAINER1 = "container1";
    final String CONTAINER2 = "container2";

    @InfinispanResource(CONTAINER1)
    RemoteInfinispanServer server1;

    @InfinispanResource(CONTAINER2)
    RemoteInfinispanServer server2;

    RemoteCacheManager rcm1;
    RemoteCacheManager rcm2;

    @Test
    public void testStorageOnlyConfigExample() throws Exception {
        rcm1 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server1.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server1.getHotrodEndpoint().getPort())
                .build());
        RemoteInfinispanCache ricDist = server1.getCacheManager(CACHE_MANAGER_NAME).getCache(DEFAULT_CACHE_NAME);
        RemoteCache<String, String> rc1 = rcm1.getCache(DEFAULT_CACHE_NAME);
        assertEquals(0, ricDist.getNumberOfEntries());
        assertEquals(2, server1.getCacheManager(CACHE_MANAGER_NAME).getClusterSize());
        rc1.put("k", "v");
        rc1.put("k2", "v2");
        assertEquals(rc1.get("k"), "v");
        assertEquals(rc1.get("k2"), "v2");
        assertEquals(2, ricDist.getNumberOfEntries());
        rc1.put("k3", "v3");
        assertEquals(3, ricDist.getNumberOfEntries());
        assertEquals("v", rc1.get("k"));
        assertEquals("v2", rc1.get("k2"));
        assertEquals("v3", rc1.get("k3"));
        try {
            rcm2 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                    .host(server2.getHotrodEndpoint().getInetAddress().getHostName())
                    .port(server2.getHotrodEndpoint().getPort())
                    .build());
            assert false;
        } catch (Exception e) {
            // OK - we are not able to access HotRod endpoint of storage-only node
        }
    }
}
