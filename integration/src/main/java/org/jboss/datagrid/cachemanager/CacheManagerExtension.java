package org.jboss.datagrid.cachemanager;

import org.jboss.datagrid.DataGridConstants;
import org.jboss.datagrid.DataGridExtension;
import org.jboss.datagrid.DataGridService;

public class CacheManagerExtension extends DataGridExtension {

    public CacheManagerExtension() {
        super(
                DataGridConstants.SN_CACHEMANAGER,
                DataGridConstants.NS_CACHEMANAGER_1_0,
                DataGridConstants.CF_CACHEMANAGER);
    }

    @Override
    protected DataGridService<?> createService() {
        return new CacheManagerService();
    }
}