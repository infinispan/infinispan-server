package org.jboss.datagrid.cachemanager;

import org.infinispan.config.Configuration;
import org.infinispan.config.ConfigurationValidatingVisitor;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.config.InfinispanConfiguration;
import org.infinispan.jmx.MBeanServerLookup;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridService;
import org.jboss.datagrid.SecurityActions;

import javax.management.MBeanServer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

class CacheManagerService extends DataGridService<EmbeddedCacheManager> {

    @Override
    protected EmbeddedCacheManager doStart(String configPath) throws Exception {
        SecurityActions.setContextClassLoader(DefaultCacheManager.class.getClassLoader());
        InputStream in = new FileInputStream(configPath);
        EmbeddedCacheManager cacheManager;
        try {
            InfinispanConfiguration configuration =
                    InfinispanConfiguration.newInfinispanConfiguration(
                            in, InfinispanConfiguration.findSchemaInputStream(),
                            new ConfigurationValidatingVisitor());

            GlobalConfiguration gcfg = configuration.parseGlobalConfiguration();
            Configuration dcfg = configuration.parseDefaultConfiguration();
            gcfg.fluent().globalJmxStatistics().mBeanServerLookup(new MBeanServerLookup() {
                @Override
                public MBeanServer getMBeanServer(Properties properties) {
                    return CacheManagerService.this.getMBeanServer().getValue();
                }
            });

            cacheManager = new DefaultCacheManager(gcfg, dcfg);
        } finally {
            in.close();
        }
        return cacheManager;
    }

    @Override
    protected void doStop(EmbeddedCacheManager cacheManager) throws Exception {
        cacheManager.stop();
    }
}
