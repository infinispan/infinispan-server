package org.infinispan.server.test.client.rest;

import java.net.Inet6Address;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.server.test.category.RESTClustered;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.infinispan.server.test.client.rest.RESTHelper.KEY_A;
import static org.infinispan.server.test.client.rest.RESTHelper.KEY_B;
import static org.infinispan.server.test.client.rest.RESTHelper.KEY_C;
import static org.infinispan.server.test.client.rest.RESTHelper.delete;
import static org.infinispan.server.test.client.rest.RESTHelper.fullPathKey;
import static org.infinispan.server.test.client.rest.RESTHelper.get;
import static org.infinispan.server.test.client.rest.RESTHelper.head;
import static org.infinispan.server.test.client.rest.RESTHelper.post;
import static org.infinispan.server.test.client.rest.RESTHelper.put;
import static org.junit.Assert.*;

/**
 * Tests for the REST client.
 *
 * @author <a href="mailto:jvilkola@redhat.com">Jozef Vilkolak</a>
 * @author <a href="mailto:mlinhard@redhat.com">Michal Linhard</a>
 * @version August 2011
 */
@RunWith(Arquillian.class)
@Category({ RESTClustered.class })
public class RESTReplicationTest {

    @InfinispanResource("container1")
    RemoteInfinispanServer server1;

    @InfinispanResource("container2")
    RemoteInfinispanServer server2;

    @Before
    public void setUp() throws Exception {
        // IPv6 addresses should be in square brackets, otherwise http client does not understand it
        if (server1.getRESTEndpoint().getInetAddress() instanceof Inet6Address) {
            RESTHelper.addServer("[" + server1.getRESTEndpoint().getInetAddress().getHostName() + "]", server1.getRESTEndpoint().getContextPath());
        } else { // otherwise should be IPv4
            RESTHelper.addServer(server1.getRESTEndpoint().getInetAddress().getHostName(), server1.getRESTEndpoint().getContextPath());
        }

        if (server2.getRESTEndpoint().getInetAddress() instanceof Inet6Address) {
            RESTHelper.addServer("[" + server2.getRESTEndpoint().getInetAddress().getHostName() + "]", server2.getRESTEndpoint().getContextPath());
        } else { // otherwise should be IPv4
            RESTHelper.addServer(server2.getRESTEndpoint().getInetAddress().getHostName(), server2.getRESTEndpoint().getContextPath());
        }

        delete(fullPathKey(KEY_A));
        delete(fullPathKey(KEY_B));
        delete(fullPathKey(KEY_C));

        head(fullPathKey(KEY_A), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(KEY_B), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(KEY_C), HttpServletResponse.SC_NOT_FOUND);
    }

    @After
    public void tearDown() throws Exception {
        delete(fullPathKey(KEY_A));
        delete(fullPathKey(KEY_B));
        delete(fullPathKey(KEY_C));
    }

    @Test
    public void testReplicationPut() throws Exception {
        put(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
    }

    @Test
    public void testReplicationPost() throws Exception {
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
    }

    @Test
    public void testReplicationDelete() throws Exception {
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        get(fullPathKey(1, KEY_A), "data");
        delete(fullPathKey(0, KEY_A));
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testReplicationWipeCache() throws Exception {
        post(fullPathKey(0, KEY_A), "data", "text/plain");
        post(fullPathKey(0, KEY_B), "data", "text/plain");
        head(fullPathKey(0, KEY_A));
        head(fullPathKey(0, KEY_B));
        delete(fullPathKey(0, null));
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
        head(fullPathKey(1, KEY_B), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testReplicationTTL() throws Exception {
        post(fullPathKey(0, KEY_A), "data", "application/text", HttpServletResponse.SC_OK,
                // headers
                "Content-Type", "application/text", "timeToLiveSeconds", "2");
        head(fullPathKey(1, KEY_A));
        Thread.sleep(2100);
        // should be evicted
        head(fullPathKey(1, KEY_A), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testGetAllKeysInCluster() throws Exception {
        String fullPathKeyA = fullPathKey(0, KEY_A);
        String fullPathKeyB = fullPathKey(1, KEY_B);
        String fullPathKeyC = fullPathKey(1, KEY_C);

        post(fullPathKeyA, "valueA", "application/text");
        post(fullPathKeyB, "valueB", "application/text");
        post(fullPathKeyC, "valueC", "application/text");

        HttpResponse allKeys = get((fullPathKey(0, null) + "?global"), null, HttpServletResponse.SC_OK, false, "Accept", "application/json");
        String response = EntityUtils.toString(allKeys.getEntity());
        assertTrue(response.contains("\"" + KEY_A + "\""));
        assertTrue(response.contains("\"" + KEY_B + "\""));
        assertTrue(response.contains("\"" + KEY_C + "\""));

        allKeys = get((fullPathKey(0, null) + "?global"), null, HttpServletResponse.SC_OK, false, "Accept", "application/xml");
        response = EntityUtils.toString(allKeys.getEntity());
        assertTrue(response.contains("<key>" + KEY_A + "</key>"));
        assertTrue(response.contains("<key>" + KEY_B + "</key>"));
        assertTrue(response.contains("<key>" + KEY_C + "</key>"));
    }
}
