package org.jboss.datagrid.endpoint.hotrod;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridConstants;
import org.jboss.datagrid.DataGridExtension;
import org.jboss.datagrid.DataGridService;
import org.jboss.msc.service.ServiceBuilder;

public class HotRodExtension extends DataGridExtension {

    public HotRodExtension() {
        super(
                DataGridConstants.SN_HOTROD,
                DataGridConstants.NS_HOTROD_1_0,
                DataGridConstants.CF_HOTROD);
    }

    @Override
    protected DataGridService<?> createService() {
        return new HotRodService();
    }

    @Override
    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        super.buildService(service, builder);
        builder.addDependency(
                ServiceBuilder.DependencyType.REQUIRED,
                DataGridConstants.SN_CACHEMANAGER,
                EmbeddedCacheManager.class,
                ((HotRodService) service).getCacheManager());
    }
}