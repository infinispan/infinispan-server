package org.jboss.datagrid.endpoint.hotrod;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.infinispan.server.hotrod.HotRodServer;
import org.jboss.datagrid.endpoint.EndpointService;

class HotRodService extends EndpointService<HotRodServer> {

    @Override
    protected HotRodServer doStart(String configPath) throws Exception {
        Properties props = new Properties();
        InputStream in = new FileInputStream(configPath);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        HotRodServer server = new HotRodServer();
        server.start(props, getCacheManager().getValue());
        return server;
    }

    @Override
    protected void doStop(HotRodServer server) throws Exception {
        server.stop();
    }
}
