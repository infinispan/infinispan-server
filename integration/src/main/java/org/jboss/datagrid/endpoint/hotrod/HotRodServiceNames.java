package org.jboss.datagrid.endpoint.hotrod;

import org.jboss.datagrid.endpoint.EndpointServiceNames;
import org.jboss.msc.service.ServiceName;

public class HotRodServiceNames {
    public static final ServiceName HOTROD =
        EndpointServiceNames.ENDPOINT.append("hotrod");
}
