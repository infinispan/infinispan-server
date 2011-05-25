package org.jboss.datagrid.endpoint.hotrod;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.jboss.datagrid.endpoint.EndpointService;

class HotRodService extends EndpointService<HotRodServer> {

    @Override
    protected HotRodServer startServer(String configPath) throws Exception {
        Properties props = new Properties();
        InputStream in = new FileInputStream(configPath);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        HotRodServer server = new HotRodServer();
        server.start(props, new DefaultCacheManager(true));
        return server;
    }

    @Override
    protected void stopServer(HotRodServer server) throws Exception {
        server.stop();
    }
}
