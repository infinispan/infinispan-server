package org.jboss.datagrid.endpoint.hotrod;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.jboss.datagrid.DataGridService;

class HotRodService extends DataGridService<HotRodServer> {

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
        server.start(props, new DefaultCacheManager(true));
        return server;
    }

    @Override
    protected void doStop(HotRodServer server) throws Exception {
        server.stop();
    }
}
