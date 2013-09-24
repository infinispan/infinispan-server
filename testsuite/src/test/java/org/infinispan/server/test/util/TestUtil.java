package org.infinispan.server.test.util;

import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * Often repeated test code routines.
 * 
 * @author Michal Linhard (mlinhard@redhat.com)
 * 
 */
public class TestUtil {
    /**
     * Create {@link RemoteCacheManager} for given server.
     * 
     * @param server The server
     * @return New {@link RemoteCacheManager}
     */
    public static RemoteCacheManager createCacheManager(RemoteInfinispanServer server) {
        return new RemoteCacheManager(createConfigBuilder(server.getHotrodEndpoint().getInetAddress().getHostName(),
            server.getHotrodEndpoint().getPort()).build());

    }

    public static ConfigurationBuilder createConfigBuilder(String hostName, int port) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(hostName).port(port);
        return builder;
    }

    /**
     * Create cache manager for given {@link RemoteInfinispanMBeans}.
     * 
     * @param serverBeans The server MBeans.
     * @return New {@link RemoteCacheManager}
     */
    public static RemoteCacheManager createCacheManager(RemoteInfinispanMBeans serverBeans) {
        return createCacheManager(serverBeans.server);
    }

}
