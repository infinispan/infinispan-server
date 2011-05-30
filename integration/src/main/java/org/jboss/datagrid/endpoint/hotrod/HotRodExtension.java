package org.jboss.datagrid.endpoint.hotrod;

import org.jboss.datagrid.*;

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
}