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

import java.util.ArrayList;
import java.util.List;

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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test clustered-topology.xml configuration example. The jgroups topology for node 1 and node 2 is
 * configured in arquillian.xml
 *
 * @author <a href="mailto:jmarkos@redhat.com">Jakub Markos</a>
 * @author <a href="mailto:jvilkola@redhat.com">Jozef Vilkolak</a>
 */
@RunWith(Arquillian.class)
public class TopologyConfigExampleTest {

    final String CONTAINER1 = "container1";
    final String CONTAINER2 = "container2";
    final String CONTAINER3 = "container3";

    @InfinispanResource(CONTAINER1)
    RemoteInfinispanServer server1;

    @InfinispanResource(CONTAINER2)
    RemoteInfinispanServer server2;

    @InfinispanResource(CONTAINER3)
    RemoteInfinispanServer server3;

    RemoteCacheManager rcm0;
    RemoteCacheManager rcm1;
    RemoteCacheManager rcm2;

    @Before
    public void setUp() throws Exception {
        rcm0 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server1.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server1.getHotrodEndpoint().getPort())
                .build());
        rcm1 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server2.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server2.getHotrodEndpoint().getPort())
                .build());
        rcm2 = new RemoteCacheManager(new ConfigurationBuilder().addServer()
                .host(server3.getHotrodEndpoint().getInetAddress().getHostName())
                .port(server3.getHotrodEndpoint().getPort())
                .build());
        assertEquals(3, server1.getCacheManager("clustered").getClusterSize());
        assertEquals(3, server2.getCacheManager("clustered").getClusterSize());
        assertEquals(3, server3.getCacheManager("clustered").getClusterSize());
    }

    @Test
    public void testRackAttribute() throws Exception {
        int total_elements = 0;
        RemoteCache<String, String> rc0 = rcm0.getCache("default");
        RemoteCache<String, String> rc1 = rcm1.getCache("default");
        RemoteCache<String, String> rc2 = rcm2.getCache("default");
        rc0.clear();
        rc1.clear();
        rc2.clear();

        long s0Entries = 0;
        long s1Entries = 0;
        long s2Entries = 0;
        List<String> s1Bulk = new ArrayList<String>();
        List<String> s2Bulk = new ArrayList<String>();

        //By using topology information we divide our 3 nodes into 2 groups and generate enough elements so there
        //is at least 1 element in each group and at least 5 elements total,
        //and keep track of elements that went to server 2 and 3
        while (s0Entries == 0 || s1Entries == 0 || s2Entries == 0 || total_elements < 5) {
            rc0.put("machine" + total_elements, "machine");

            if (s1Entries + 1 == server2.getCacheManager("clustered").getCache("default").getNumberOfEntries()) {
                s1Bulk.add("machine" + total_elements);
            }
            if (s2Entries + 1 == server3.getCacheManager("clustered").getCache("default").getNumberOfEntries()) {
                s2Bulk.add("machine" + total_elements);
            }

            total_elements++;
            s1Entries = server2.getCacheManager("clustered").getCache("default").getNumberOfEntries();
            s2Entries = server3.getCacheManager("clustered").getCache("default").getNumberOfEntries();
            s0Entries = server1.getCacheManager("clustered").getCache("default").getNumberOfEntries();
            if (total_elements > 10) break; // in case something goes wrong - do not cycle forever
        }

        assertTrue("Unexpected number of entries in server1: " + s0Entries, s0Entries > 0);
        assertTrue("Unexpected number of entries in server2: " + s1Entries, s1Entries > 0);
        assertTrue("Instead of " + total_elements * 2 + " total elements there were " + (s0Entries + s1Entries + s2Entries), s0Entries + s1Entries + s2Entries == total_elements * 2);
        assertTrue("Server 1 elements are not contained in server 2", s2Bulk.containsAll(s1Bulk));

        //Now we remove the keys from server 2 therefore they should be removed from server 3 and that should imply
        //that server 3 and server 1 have the same elements
        for (String key : s1Bulk) {
            rc1.remove(key);
        }
        s0Entries = server1.getCacheManager("clustered").getCache("default").getNumberOfEntries();
        s1Entries = server2.getCacheManager("clustered").getCache("default").getNumberOfEntries();
        s2Entries = server3.getCacheManager("clustered").getCache("default").getNumberOfEntries();

        assertEquals("There were " + s1Entries + " left in the 2nd server", 0, s1Entries);
        assertEquals(s0Entries, s2Entries);
        assertNotEquals(s0Entries, s1Entries);
        assertEquals(rc0.getBulk(), rc2.getBulk());
    }

}
