package org.jboss.datagrid.endpoint.memcached;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridConstants;
import org.jboss.datagrid.DataGridExtension;
import org.jboss.datagrid.DataGridService;
import org.jboss.msc.service.ServiceBuilder;

public class MemcachedExtension extends DataGridExtension {

    public MemcachedExtension() {
        super(
                DataGridConstants.SN_MEMCACHED,
                DataGridConstants.NS_MEMCACHED_1_0,
                DataGridConstants.CF_MEMCACHED);
    }

    @Override
    protected DataGridService<?> createService() {
        return new MemcachedService();
    }

    @Override
    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        super.buildService(service, builder);
        builder.addDependency(
                ServiceBuilder.DependencyType.REQUIRED,
                DataGridConstants.SN_CACHEMANAGER,
                EmbeddedCacheManager.class,
                ((MemcachedService) service).getCacheManager());
    }
}