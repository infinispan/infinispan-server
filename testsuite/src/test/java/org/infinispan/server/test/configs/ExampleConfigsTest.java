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

import static org.infinispan.server.test.client.rest.RESTHelper.KEY_A;
import static org.infinispan.server.test.client.rest.RESTHelper.KEY_B;
import static org.infinispan.server.test.client.rest.RESTHelper.KEY_C;
import static org.infinispan.server.test.client.rest.RESTHelper.delete;
import static org.infinispan.server.test.client.rest.RESTHelper.fullPathKey;
import static org.infinispan.server.test.client.rest.RESTHelper.get;
import static org.infinispan.server.test.client.rest.RESTHelper.head;
import static org.infinispan.server.test.client.rest.RESTHelper.post;
import static org.infinispan.server.test.client.rest.RESTHelper.put;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.log4j.Logger;
import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RESTEndpoint;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.arquillian.core.RemoteInfinispanServers;
import org.infinispan.arquillian.core.WithRunningServer;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.TransportException;
import org.infinispan.server.test.client.memcached.MemcachedClient;
import org.infinispan.server.test.client.rest.RESTHelper;
import org.infinispan.server.test.util.Refs.CacheRefs;
import org.infinispan.server.test.util.Refs.RefsFactory;
import org.infinispan.server.test.util.Refs.ServerRefs;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for example configurations.
 * 
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 * @author <a href="mailto:galderz@redhat.com">Galder Zamarreño</a>
 * @author <a href="mailto:mlinhard@redhat.com">Michal Linhard</a>
 */
@RunWith(Arquillian.class)
public class ExampleConfigsTest {

    private static final Logger log = Logger.getLogger(ExampleConfigsTest.class);

    @InfinispanResource
    RemoteInfinispanServers serverManager;

    static final String DEFAULT_CACHE_NAME = "default";
    static final String NAMED_CACHE_NAME = "namedCache";
    static final String MEMCACHED_CACHE_NAME = "memcachedCache";
    static final String SERVER_DATA_DIR = System.getProperty("server1.dist") + File.separator + "standalone" + File.separator
        + "data";
    static final String SERVER_CONFIG_DIR = System.getProperty("server1.dist") + File.separator + "standalone" + File.separator
        + "configuration";

    @ArquillianResource
    ContainerController controller;

    private List<ServerRefs> refsToStop = new ArrayList<ServerRefs>();

    /**
     * Create a 2 node cluster and check that state transfer does not take place.
     * 
     */
    @Test
    public void testClusterCacheLoaderConfigExample() throws Exception {
        controller.start("clustered-ccl-1");
        CacheRefs s1 = createRefs("clustered-ccl-1", "clustered", DEFAULT_CACHE_NAME);
        s1.cache.put("key", "value");
        assertEquals(1, s1.cacheInfo.getNumberOfEntries());
        assertEquals(1, s1.managerInfo.getClusterSize());

        controller.start("clustered-ccl-2");
        CacheRefs s2 = createRefs("clustered-ccl-2", "clustered", DEFAULT_CACHE_NAME);

        assertEquals(2, s2.managerInfo.getClusterSize());
        // state transfer didn't happen
        assertEquals(0, s2.cacheInfo.getNumberOfEntries());
        s2.cache.get("key");
        // the entry is obtained
        assertEquals(1, s2.cacheInfo.getNumberOfEntries());
        s2.cache.put("key2", "value2");
        assertEquals(2, s1.cacheInfo.getNumberOfEntries());
        assertEquals(2, s2.cacheInfo.getNumberOfEntries());
        controller.stop("clustered-ccl-1");
        controller.stop("clustered-ccl-2");
    }

    @Test
    @WithRunningServer("standalone-compatibility-mode")
    public void testCompatibilityModeConfig() throws Exception {
        MemcachedClient memcachedClient = null;
        HttpClient restClient = null;
        try {
            CacheRefs s1 = createRefs("standalone-compatibility-mode", "local", DEFAULT_CACHE_NAME);

            restClient = new HttpClient();
            String restUrl = "http://" + s1.server.getHotrodEndpoint().getInetAddress().getHostName() + ":8080"
                + s1.server.getRESTEndpoint().getContextPath() + "/" + DEFAULT_CACHE_NAME;
            memcachedClient = new MemcachedClient(s1.server.getMemcachedEndpoint().getInetAddress().getHostName(), s1.server
                .getMemcachedEndpoint().getPort());
            String key = "1";

            // 1. Put with Hot Rod
            assertEquals(null, s1.cache.withFlags(Flag.FORCE_RETURN_VALUE).put(key, "v1".getBytes()));
            assertArrayEquals("v1".getBytes(), (byte[]) s1.cache.get(key));

            // 2. Get with REST
            HttpMethod get = new GetMethod(restUrl + "/" + key);
            restClient.executeMethod(get);
            assertEquals(HttpServletResponse.SC_OK, get.getStatusCode());
            assertArrayEquals("v1".getBytes(), get.getResponseBody());

            // 3. Get with Memcached
            assertArrayEquals("v1".getBytes(), readWithMemcachedAndDeserialize(key, memcachedClient));

            key = "2";

            // 1. Put with REST
            EntityEnclosingMethod put = new PutMethod(restUrl + "/" + key);
            put.setRequestEntity(new ByteArrayRequestEntity("<hey>ho</hey>".getBytes(), "application/octet-stream"));
            restClient.executeMethod(put);
            assertEquals(HttpServletResponse.SC_OK, put.getStatusCode());

            // 2. Get with Hot Rod
            assertArrayEquals("<hey>ho</hey>".getBytes(), (byte[]) s1.cache.get(key));

            // 3. Get with Memcached
            assertArrayEquals("<hey>ho</hey>".getBytes(), readWithMemcachedAndDeserialize(key, memcachedClient));
        } finally {
            if (restClient != null) {
                restClient.getHttpConnectionManager().closeIdleConnections(0);
            }
            if (memcachedClient != null) {
                memcachedClient.close();
            }
        }

    }

    @Test
    @WithRunningServer("standalone-fcs-local")
    public void testFileCacheStoreConfig() throws Exception {
        CacheRefs sDefault = createRefs("standalone-fcs-local", "local", DEFAULT_CACHE_NAME);
        doPutGetCheckPath(sDefault, "dc", -1);
        doPutGetCheckPath(createRefs(sDefault, "local", MEMCACHED_CACHE_NAME), "mc", -1);
        doPutGetCheckPath(createRefs(sDefault, "local", NAMED_CACHE_NAME), "nc", 2100);
    }

    @Test
    @WithRunningServer("clustered-jdbc")
    public void testJDBCCacheStoreConfig() throws Exception {
        CacheRefs sDefault = createRefs("clustered-jdbc", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs sNamed = createRefs(sDefault, "clustered", NAMED_CACHE_NAME);
        sNamed.cache.put("key", "value");
        sNamed.cache.put("key2", "value2");
        assertEquals("value", sNamed.cache.get("key"));
        assertEquals("value2", sNamed.cache.get("key2"));

        // 1001, so we are 100% sure that at least 1 entry is evicted and thus stored (passivation = true)
        for (int i = 0; i < 1001; i++) {
            sDefault.cache.put("k" + i, "v" + i);
        }
        for (int i = 0; i < 1001; i++) {
            assertEquals("v" + i, sDefault.cache.get("k" + i));
        }
    }

    @Test
    @WithRunningServer("standalone-leveldb-cs-local")
    public void testLevelDBCacheStoreConfig() throws Exception {
        CacheRefs sDefault = createRefs("standalone-leveldb-cs-local", "local", DEFAULT_CACHE_NAME);
        doPutGetCheckPath(sDefault, "level-dcdefault", -1);
        doPutGetCheckPath(createRefs(sDefault, "local", MEMCACHED_CACHE_NAME), "level-mcmemcachedCache", -1);
        doPutGetCheckPath(createRefs(sDefault, "local", NAMED_CACHE_NAME), "leveldb-ncnamedCache", 2100);
    }

    @Test
    @WithRunningServer("standalone-hotrod-multiple")
    public void testHotrodMultipleConfig() throws Exception {
        ConfigurationBuilder builder1 = new ConfigurationBuilder();
        builder1.addServer().host("127.0.0.1").port(11222);
        CacheRefs s1 = createRefs("standalone-hotrod-multiple", "local", DEFAULT_CACHE_NAME, builder1);
        ConfigurationBuilder builder2 = new ConfigurationBuilder();
        builder2.addServer().host("127.0.0.1").port(11223);
        CacheRefs s2 = createRefs("standalone-hotrod-multiple", "local", DEFAULT_CACHE_NAME, builder2);
        assertEquals(0, s1.cacheInfo.getNumberOfEntries());
        for (int i = 0; i < 10; i++) {
            s1.cache.put("k" + i, "v" + i);
        }
        assertTrue(s1.cacheInfo.getNumberOfEntries() <= 10);
        for (int i = 0; i < 10; i++) {
            assertEquals("v" + i, s2.cache.get("k" + i));
        }
    }

    @Test
    @WithRunningServer({ "standalone-rcs-local-2", "standalone-rcs-local-1" })
    public void testRemoteCacheStoreConfig() throws Exception {
        CacheRefs sRemoteStoreDefault = createRefs("standalone-rcs-local-2", "local", DEFAULT_CACHE_NAME);
        CacheRefs sRemoteStoreMemcached = createRefs("standalone-rcs-local-2", "local", MEMCACHED_CACHE_NAME);
        CacheRefs sRemoteStoreNamed = createRefs("standalone-rcs-local-2", "local", NAMED_CACHE_NAME);
        CacheRefs s1Default = createRefs("standalone-rcs-local-1", "local", DEFAULT_CACHE_NAME);
        CacheRefs s1Memcached = createRefs("standalone-rcs-local-1", "local", MEMCACHED_CACHE_NAME);
        CacheRefs s1Named = createRefs("standalone-rcs-local-1", "local", NAMED_CACHE_NAME);

        doPutGetRemove(s1Default, sRemoteStoreDefault);
        s1Default.cache.clear();
        sRemoteStoreDefault.cache.clear();

        doPutGetRemove(s1Memcached, sRemoteStoreMemcached);
        s1Memcached.cache.clear();
        sRemoteStoreMemcached.cache.clear();

        doPutGetWithExpiration(s1Named, sRemoteStoreNamed);
    }

    @Test
    @WithRunningServer("standalone-hotrod-ssl")
    public void testSSLHotRodConfig() throws Exception {
        RemoteInfinispanServer server = serverManager.getServer("standalone-hotrod-ssl");

        doPutGet(createRefs("standalone-hotrod-ssl", "local", DEFAULT_CACHE_NAME,
            securityConfig("keystore_client.jks", "truststore_client.jks", server)));
        try {
            doPutGet(createRefs("standalone-hotrod-ssl", "local", DEFAULT_CACHE_NAME,
                securityConfig("keystore_server.jks", "truststore_client.jks", server)));
            Assert.fail();
        } catch (TransportException e) {
            // ok
        }
        try {
            doPutGet(createRefs("standalone-hotrod-ssl", "local", DEFAULT_CACHE_NAME,
                securityConfig("keystore_client.jks", "truststore_server.jks", server)));
            Assert.fail();
        } catch (TransportException e) {
            // ok
        }
    }

    @Test
    @WithRunningServer({ "clustered-storage-only-1", "clustered-storage-only-2" })
    public void testStorageOnlyConfig() throws Exception {
        CacheRefs s1 = createRefs("clustered-storage-only-1", "clustered", DEFAULT_CACHE_NAME);
        assertEquals(0, s1.cacheInfo.getNumberOfEntries());
        assertEquals(2, s1.managerInfo.getClusterSize());
        s1.cache.put("k", "v");
        s1.cache.put("k2", "v2");
        assertEquals(s1.cache.get("k"), "v");
        assertEquals(s1.cache.get("k2"), "v2");
        assertEquals(2, s1.cacheInfo.getNumberOfEntries());
        s1.cache.put("k3", "v3");
        assertEquals(3, s1.cacheInfo.getNumberOfEntries());
        assertEquals("v", s1.cache.get("k"));
        assertEquals("v2", s1.cache.get("k2"));
        assertEquals("v3", s1.cache.get("k3"));
        try {
            createRefs("clustered-storage-only-2", "clustered", DEFAULT_CACHE_NAME);
            assert false;
        } catch (Exception e) {
            // OK - we are not able to access HotRod endpoint of storage-only node
        }
    }

    @Test
    @WithRunningServer({ "clustered-topology-1", "clustered-topology-2", "clustered-topology-3" })
    public void testTopologyConfig() throws Exception {
        CacheRefs s1 = createRefs("clustered-topology-1", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs s2 = createRefs("clustered-topology-2", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs s3 = createRefs("clustered-topology-3", "clustered", DEFAULT_CACHE_NAME);

        assertEquals(3, s1.managerInfo.getClusterSize());
        assertEquals(3, s2.managerInfo.getClusterSize());
        assertEquals(3, s3.managerInfo.getClusterSize());
        int total_elements = 0;
        s1.cache.clear();
        s1.cache.clear();
        s1.cache.clear();

        long s0Entries = 0;
        long s1Entries = 0;
        long s2Entries = 0;
        List<String> s1Bulk = new ArrayList<String>();
        List<String> s2Bulk = new ArrayList<String>();

        // By using topology information we divide our 3 nodes into 2 groups and generate enough elements so there
        // is at least 1 element in each group and at least 5 elements total,
        // and keep track of elements that went to server 2 and 3
        while (s0Entries == 0 || s1Entries == 0 || s2Entries == 0 || total_elements < 5) {
            s1.cache.put("machine" + total_elements, "machine");

            if (s1Entries + 1 == s2.cacheInfo.getNumberOfEntries()) {
                s1Bulk.add("machine" + total_elements);
            }
            if (s2Entries + 1 == s3.cacheInfo.getNumberOfEntries()) {
                s2Bulk.add("machine" + total_elements);
            }

            total_elements++;
            s1Entries = s2.cacheInfo.getNumberOfEntries();
            s2Entries = s3.cacheInfo.getNumberOfEntries();
            s0Entries = s1.cacheInfo.getNumberOfEntries();
            if (total_elements > 10)
                break; // in case something goes wrong - do not cycle forever
        }

        assertTrue("Unexpected number of entries in server1: " + s0Entries, s0Entries > 0);
        assertTrue("Unexpected number of entries in server2: " + s1Entries, s1Entries > 0);
        assertTrue("Instead of " + total_elements * 2 + " total elements there were " + (s0Entries + s1Entries + s2Entries),
            s0Entries + s1Entries + s2Entries == total_elements * 2);
        assertTrue("Server 1 elements are not contained in server 2", s2Bulk.containsAll(s1Bulk));

        // Now we remove the keys from server 2 therefore they should be removed from server 3 and that should imply
        // that server 3 and server 1 have the same elements
        for (String key : s1Bulk) {
            s2.cache.remove(key);
        }
        s0Entries = s1.cacheInfo.getNumberOfEntries();
        s1Entries = s2.cacheInfo.getNumberOfEntries();
        s2Entries = s3.cacheInfo.getNumberOfEntries();

        assertEquals("There were " + s1Entries + " left in the 2nd server", 0, s1Entries);
        assertEquals(s0Entries, s2Entries);
        assertNotEquals(s0Entries, s1Entries);
        assertEquals(s1.cache.getBulk(), s3.cache.getBulk());
    }

    @Test
    @WithRunningServer({ "clustered-two-nodes-1", "clustered-two-nodes-2" })
    public void testTwoNodesConfig() throws Exception {
        CacheRefs s1 = createRefs("clustered-two-nodes-1", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs s2 = createRefs("clustered-two-nodes-2", "clustered", DEFAULT_CACHE_NAME);
        addServer(s1.server);
        addServer(s2.server);
        setUpREST(s1.server, s2.server);
        assertEquals(0, s1.cacheInfo.getNumberOfEntries());
        assertEquals(0, s2.cacheInfo.getNumberOfEntries());
        Assert.assertEquals(2, s1.managerInfo.getClusterSize());
        Assert.assertEquals(2, s2.managerInfo.getClusterSize());
        s1.cache.put("k", "v");
        s1.cache.put("k2", "v2");
        assertEquals(s1.cache.get("k"), "v");
        assertEquals(s1.cache.get("k2"), "v2");
        assertEquals(2, s1.cacheInfo.getNumberOfEntries());
        s2.cache.put("k3", "v3");
        assertEquals(3, s2.cacheInfo.getNumberOfEntries());
        assertEquals("v", s1.cache.get("k"));
        assertEquals("v", s2.cache.get("k"));
        assertEquals("v2", s1.cache.get("k2"));
        assertEquals("v2", s2.cache.get("k2"));
        assertEquals("v3", s1.cache.get("k3"));
        assertEquals("v3", s2.cache.get("k3"));
        setUpREST(s1.server, s2.server);
        put(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
        setUpREST(s1.server, s2.server);
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
        setUpREST(s1.server, s2.server);
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
        delete(fullPathKey(0, KEY_A));
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
        setUpREST(s1.server, s2.server);
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        post(fullPathKey(0, KEY_B), "data", "text/plain");
        head(fullPathKey(0, KEY_A));
        head(fullPathKey(0, KEY_B));
        delete(fullPathKey(0, null));
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(1, KEY_B), HttpServletResponse.SC_NOT_FOUND);
        setUpREST(s1.server, s2.server);
        post(fullPathKey(0, KEY_A), "data", "application/text", HttpServletResponse.SC_OK,
        // headers
            "Content-Type", "application/text", "timeToLiveSeconds", "2");
        head(fullPathKey(1, KEY_A));
        Thread.sleep(2100);
        // should be evicted
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @WithRunningServer({ "clustered-xsite-1", "clustered-xsite-2", "clustered-xsite-3" })
    public void testXsiteConfig() throws Exception {
        CacheRefs s1 = createRefs("clustered-xsite-1", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs s2 = createRefs("clustered-xsite-2", "clustered", DEFAULT_CACHE_NAME);
        CacheRefs s3 = createRefs("clustered-xsite-3", "clustered", DEFAULT_CACHE_NAME);
        assertEquals(0, s1.cacheInfo.getNumberOfEntries());
        assertEquals(0, s2.cacheInfo.getNumberOfEntries());

        assertEquals(2, s1.managerInfo.getClusterSize());
        assertEquals(2, s2.managerInfo.getClusterSize());
        assertEquals(1, s3.managerInfo.getClusterSize());

        s1.cache.put("k1", "v1");
        s1.cache.put("k2", "v2");
        assertEquals(2, s1.cacheInfo.getNumberOfEntries());
        assertEquals(2, s2.cacheInfo.getNumberOfEntries());
        assertEquals(2, s3.cacheInfo.getNumberOfEntries());

        assertEquals(s1.cache.get("k1"), "v1");
        assertEquals(s2.cache.get("k1"), "v1");
        assertEquals(s3.cache.get("k1"), "v1");
        assertEquals(s1.cache.get("k2"), "v2");
        assertEquals(s2.cache.get("k2"), "v2");
        assertEquals(s3.cache.get("k2"), "v2");
    }

    private void setUpREST(RemoteInfinispanServer server1, RemoteInfinispanServer server2) throws Exception {
        delete(fullPathKey(KEY_A));
        delete(fullPathKey(KEY_B));
        delete(fullPathKey(KEY_C));
        delete(fullPathKey(NAMED_CACHE_NAME, KEY_A));

        head(fullPathKey(KEY_A), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(KEY_B), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(KEY_C), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(NAMED_CACHE_NAME, KEY_A), HttpServletResponse.SC_NOT_FOUND);
    }

    private void addServer(RemoteInfinispanServer server) {
        RESTEndpoint endpoint = server.getRESTEndpoint();
        // IPv6 addresses should be in square brackets, otherwise http client does not understand it
        // otherwise should be IPv4
        String inetHostName = endpoint.getInetAddress().getHostName();
        String realHostName = endpoint.getInetAddress() instanceof Inet6Address ? "[" + inetHostName + "]" : inetHostName;
        RESTHelper.addServer(realHostName, endpoint.getContextPath());
    }

    private ConfigurationBuilder securityConfig(final String keystoreName, final String truststoreName,
        RemoteInfinispanServer server) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(server.getHotrodEndpoint().getInetAddress().getHostName())
            .port(server.getHotrodEndpoint().getPort()).ssl().enable()
            .keyStoreFileName(SERVER_CONFIG_DIR + File.separator + keystoreName).keyStorePassword("secret".toCharArray())
            .trustStoreFileName(SERVER_CONFIG_DIR + File.separator + truststoreName).trustStorePassword("secret".toCharArray());
        return builder;
    }

    protected interface Condition {
        public boolean isSatisfied() throws Exception;
    }

    protected void eventually(Condition ec, long timeout) {
        eventually(ec, timeout, 10);
    }

    protected void eventually(Condition ec, long timeout, int loops) {
        if (loops <= 0) {
            throw new IllegalArgumentException("Number of loops must be positive");
        }
        long sleepDuration = timeout / loops;
        if (sleepDuration == 0) {
            sleepDuration = 1;
        }
        try {
            for (int i = 0; i < loops; i++) {

                if (ec.isSatisfied())
                    return;
                Thread.sleep(sleepDuration);
            }
            assertTrue(ec.isSatisfied());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected!", e);
        }
    }

    private void doPutGetRemove(final CacheRefs sMainCache, final CacheRefs sRemoteStore) {
        assertEquals(0, sMainCache.cacheInfo.getNumberOfEntries());
        assertEquals(0, sRemoteStore.cacheInfo.getNumberOfEntries());

        for (int i = 0; i < 1100; i++) {
            sMainCache.cache.put("key" + i, "value" + i);
        }
        assertTrue(sMainCache.cacheInfo.getNumberOfEntries() <= 1000);
        eventually(new Condition() {
            @Override
            public boolean isSatisfied() throws Exception {
                log.debug("Num entries: Main cache: " + sMainCache.cacheInfo.getNumberOfEntries() + " Remote store: "
                    + sRemoteStore.cacheInfo.getNumberOfEntries() + " Total: "
                    + (sMainCache.cacheInfo.getNumberOfEntries() + sRemoteStore.cacheInfo.getNumberOfEntries()));
                return sMainCache.cacheInfo.getNumberOfEntries() + sRemoteStore.cacheInfo.getNumberOfEntries() == 1100;
            }
        }, 10000);

        for (int i = 0; i < 1100; i++) {
            assertNotNull(sMainCache.cache.get("key" + i));
            sMainCache.cache.remove("key" + i);
            assertNull(sMainCache.cache.get("key" + i));
        }
        assertEquals(0, sMainCache.cacheInfo.getNumberOfEntries());
        assertEquals(0, sRemoteStore.cacheInfo.getNumberOfEntries());
    }

    private void doPutGetWithExpiration(CacheRefs s1, CacheRefs s2) throws Exception {
        assertEquals(0, s2.cacheInfo.getNumberOfEntries());
        doPutGet(s1);
        // all entries are in store (passivation=false)
        assertEquals(10, s2.cacheInfo.getNumberOfEntries());

        Thread.sleep(2100); // the lifespan is 2000ms so we need to wait more

        // entries expired
        for (int i = 0; i < 10; i++) {
            assertNull(s1.cache.get("key" + i));
        }
    }

    private void doPutGet(CacheRefs s) {
        assertEquals(0, s.cacheInfo.getNumberOfEntries());
        for (int i = 0; i < 10; i++) {
            s.cache.put("k" + i, "v" + i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("v" + i, s.cache.get("k" + i));
        }
        assertEquals(10, s.cacheInfo.getNumberOfEntries());
    }

    private void doPutGetCheckPath(CacheRefs s, String filePath, long sleepTime) throws Exception {
        doPutGet(s);
        if (sleepTime >= 0) {
            Thread.sleep(sleepTime);

            // entries expired
            for (int i = 0; i < 10; i++) {
                assertNull(s.cache.get("k" + i));
            }
        }
        File f = new File(SERVER_DATA_DIR, filePath);
        assertTrue(f.isDirectory());
    }

    /*
     * Need to de-serialize the object as the default JavaSerializationMarshaller is used by Memcached endpoint.
     */
    private byte[] readWithMemcachedAndDeserialize(String key, MemcachedClient memcachedClient) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(memcachedClient.getBytes(key));
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (byte[]) ois.readObject();
    }

    @After
    public void destroyRcms() {
        for (ServerRefs refs : refsToStop) {
            refs.manager.stop();
        }
        refsToStop.clear();
    }

    // creates a new remote cache manager
    protected CacheRefs createRefs(String serverName, String managerName, String cacheName) {
        CacheRefs refs = new RefsFactory(serverManager).createCacheRefs(serverName, cacheName, managerName);
        refsToStop.add(refs);
        return refs;
    }

    // creates a new remote cache manager
    protected CacheRefs createRefs(String serverName, String managerName, String cacheName, ConfigurationBuilder builder) {
        CacheRefs refs = new RefsFactory(serverManager).createCacheRefs(serverName, cacheName, managerName, builder);
        refsToStop.add(refs);
        return refs;
    }

    // reuses remote cache manager from existing refs
    protected CacheRefs createRefs(ServerRefs refs, String managerName, String cacheName) {
        return new RefsFactory(serverManager).createCacheRefs(refs, cacheName, managerName);
    }
}
