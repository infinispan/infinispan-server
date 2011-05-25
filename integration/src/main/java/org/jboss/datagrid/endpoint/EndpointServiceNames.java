package org.jboss.datagrid.endpoint;

import org.jboss.datagrid.DataGridServiceNames;
import org.jboss.msc.service.ServiceName;

public class EndpointServiceNames {
    public static final ServiceName ENDPOINT =
        DataGridServiceNames.DATAGRID.append("endpoint");
}
