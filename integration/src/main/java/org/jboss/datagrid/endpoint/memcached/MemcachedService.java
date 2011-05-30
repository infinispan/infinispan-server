package org.jboss.datagrid.endpoint.memcached;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.memcached.MemcachedServer;
import org.jboss.datagrid.DataGridService;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

class MemcachedService extends DataGridService<MemcachedServer> {

    @Override
    protected MemcachedServer doStart(String configPath) throws Exception {
        Properties props = new Properties();
        InputStream in = new FileInputStream(configPath);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        MemcachedServer server = new MemcachedServer();
        server.start(props, new DefaultCacheManager(true));
        return server;
    }

    @Override
    protected void doStop(MemcachedServer server) throws Exception {
        server.stop();
    }
}
