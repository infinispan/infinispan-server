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
package com.redhat.datagrid;

import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 */
public class DataGridConstants {

    private static final ServiceName REDHAT = ServiceName.of("redhat");

    public static final ServiceName DATAGRID = REDHAT.append("datagrid");

    // Service names (SN_*)

    public static final ServiceName SN_ENDPOINT = DATAGRID.append("endpoint");

    // XML namespaces (NS_*)

    public static final String NS_ENDPOINT_1_0 = "urn:redhat:domain:datagrid:endpoint:1.0";

    // XML elements and attributes

    public static final String SUBSYSTEM ="subsystem";

    public static final String CONNECTOR = "connector";
    public static final String PROTOCOL = "protocol"; // 'hotrod' or 'memcached'
    public static final String SOCKET_BINDING = "socket-binding"; // string
    public static final String CACHE_CONTAINER = "cache-container"; // string
    public static final String WORKER_THREADS = "worker-threads"; // integer
    public static final String IDLE_TIMEOUT = "idle-timeout"; // integer
    public static final String TCP_NODELAY = "tcp-nodelay"; // 'true' or 'false'
    public static final String SEND_BUFFER_SIZE = "send-buffer-size"; // integer
    public static final String RECEIVE_BUFFER_SIZE = "receive-buffer-size"; // integer

    public static final String TOPOLOGY_STATE_TRANSFER = "topology-state-transfer";
    public static final String LOCK_TIMEOUT = "lock-timeout"; // integer
    public static final String REPLICATION_TIMEOUT = "replication-timeout"; // integer
    public static final String UPDATE_TIMEOUT = "update-timeout"; // integer
    public static final String EXTERNAL_HOST = "external-host"; // string
    public static final String EXTERNAL_PORT = "external-port"; // integer
    public static final String LAZY_RETRIEVAL = "lazy-retrieval"; // 'true' or 'false'

    private DataGridConstants() {
        // Constant table
    }
}
