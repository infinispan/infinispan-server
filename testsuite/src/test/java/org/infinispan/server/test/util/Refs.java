package org.infinispan.server.test.util;

import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.arquillian.core.RemoteInfinispanServers;
import org.infinispan.arquillian.model.RemoteInfinispanCache;
import org.infinispan.arquillian.model.RemoteInfinispanCacheManager;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * Aggregate objects containing multiple test object references.
 * 
 * @author <a href="mailto:mlinhard@redhat.com">Michal Linhard</a>
 * 
 */
public class Refs {

    /**
     * Factory for test reference objects.
     * 
     */
    public static class RefsFactory {
        private RemoteInfinispanServers serverManager;

        public RefsFactory(RemoteInfinispanServers serverManager) {
            super();
            this.serverManager = serverManager;
        }

        public ServerRefs createServerRefs(String serverName, ConfigurationBuilder configBuilder) {
            ServerRefs refs = new ServerRefs();
            refs.serverName = serverName;
            refs.server = serverManager.getServer(serverName);
            refs.manager = new RemoteCacheManager(configBuilder.build());
            return refs;
        }

        public ServerRefs createServerRefs(String serverName) {
            ServerRefs refs = new ServerRefs();
            refs.serverName = serverName;
            refs.server = serverManager.getServer(serverName);
            refs.manager = TestUtil.createCacheManager(refs.server);
            return refs;
        }

        public CacheRefs createCacheRefs(ServerRefs serverRefs, String cacheName, String managerName) {
            CacheRefs refs = new CacheRefs();
            refs.server = serverRefs.server;
            refs.manager = serverRefs.manager;
            refs.cacheName = cacheName;
            refs.managerName = managerName;
            refs.managerInfo = refs.server.getCacheManager(refs.managerName);
            refs.cacheInfo = refs.managerInfo.getCache(cacheName);
            refs.cache = refs.manager.getCache(cacheName);
            return refs;
        }

        public CacheRefs createCacheRefs(String serverName, String cacheName, String managerName) {
            return createCacheRefs(createServerRefs(serverName), cacheName, managerName);
        }

        public CacheRefs createCacheRefs(String serverName, String cacheName, String managerName,
            ConfigurationBuilder configBuilder) {
            return createCacheRefs(createServerRefs(serverName, configBuilder), cacheName, managerName);
        }

    }

    /**
     * Reference to both infinispan server and cache manager.
     * 
     */
    public static class ServerRefs {
        public String serverName;
        public RemoteInfinispanServer server;
        public RemoteCacheManager manager;
    }

    /**
     * References to cache objects.
     */
    public static class CacheRefs extends ServerRefs {
        public String cacheName;
        public String managerName;
        public RemoteCache<Object, Object> cache;
        public RemoteInfinispanCache cacheInfo;
        public RemoteInfinispanCacheManager managerInfo;
    }

}
