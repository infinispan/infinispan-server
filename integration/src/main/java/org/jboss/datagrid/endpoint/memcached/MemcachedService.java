package org.jboss.datagrid.endpoint.memcached;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.infinispan.server.memcached.MemcachedServer;
import org.jboss.datagrid.endpoint.EndpointService;

class MemcachedService extends EndpointService<MemcachedServer> {

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
        server.start(props, getCacheManager().getValue());
        return server;
    }

    @Override
    protected void doStop(MemcachedServer server) throws Exception {
        server.stop();
    }
}
