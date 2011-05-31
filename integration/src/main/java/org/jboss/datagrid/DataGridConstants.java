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
package org.jboss.datagrid;

import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 */
public class DataGridConstants {

    private static final ServiceName DATAGRID = ServiceName.JBOSS.append("datagrid");
    private static final ServiceName ENDPOINT = DATAGRID.append("endpoint");

    // Service names (SN_*)

    public static final ServiceName SN_CACHEMANAGER = DATAGRID.append("cachemanager");
    public static final ServiceName SN_HOTROD = ENDPOINT.append("hotrod");
    public static final ServiceName SN_MEMCACHED = ENDPOINT.append("memcached");

    // XML namespaces (NS_*)

    public static final String NS_CACHEMANAGER_1_0 = "urn:jboss:domain:datagrid:cachemanager:1.0";
    public static final String NS_HOTROD_1_0 = "urn:jboss:domain:datagrid:endpoint:hotrod:1.0";
    public static final String NS_MEMCACHED_1_0 = "urn:jboss:domain:datagrid:endpoint:memcached:1.0";

    // XML elements and attributes

    public static final String CONFIG_PATH = "config-path";
    public static final String PATH ="path";
    public static final String RELATIVE_TO ="relative-to";
    public static final String SUBSYSTEM ="subsystem";

    // Default configuration file names

    public static final String CF_CACHEMANAGER = "infinispan-configuration.xml";
    public static final String CF_HOTROD = "datagrid-endpoint-hotrod.properties";
    public static final String CF_MEMCACHED = "datagrid-endpoint-memcached.properties";

    private DataGridConstants() {
        // Constant table
    }
}
