/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */
package org.jboss.datagrid.cachemanager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.management.MBeanServer;

import org.infinispan.config.Configuration;
import org.infinispan.config.ConfigurationValidatingVisitor;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.config.InfinispanConfiguration;
import org.infinispan.jmx.MBeanServerLookup;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridService;
import org.jboss.datagrid.SecurityActions;

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
