package org.jboss.datagrid.endpoint.memcached;

import org.jboss.datagrid.DataGridConstants;
import org.jboss.datagrid.DataGridExtension;
import org.jboss.datagrid.DataGridService;

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
}