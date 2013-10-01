package org.infinispan.server.test.util;

import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * Often repeated test code routines.
 * 
 * @author <a href="mailto:mlinhard@redhat.com">Michal Linhard</a>
 * 
 */
public class TestUtil {
    /**
     * Create {@link RemoteCacheManager} for given server.
     * 
     * @param server
     * @return the RCM
     */
    public static RemoteCacheManager createCacheManager(RemoteInfinispanServer server) {
        return new RemoteCacheManager(new ConfigurationBuilder().addServer()
            .host(server.getHotrodEndpoint().getInetAddress().getHostName()).port(server.getHotrodEndpoint().getPort()).build());

    }

}
