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
package org.infinispan.server.endpoint.subsystem;

/**
 * @author Tristan Tarrant
 */
public class ModelKeys {

   public static final String HOTROD_CONNECTOR = "hotrod-connector";
   public static final String MEMCACHED_CONNECTOR = "memcached-connector";
   public static final String REST_CONNECTOR = "rest-connector";

   public static final String NAME = "name"; // string
   public static final String SOCKET_BINDING = "socket-binding"; // string
   public static final String CACHE_CONTAINER = "cache-container"; // string
   public static final String WORKER_THREADS = "worker-threads"; // integer
   public static final String IDLE_TIMEOUT = "idle-timeout"; // integer
   public static final String TCP_NODELAY = "tcp-nodelay"; // boolean
   public static final String SEND_BUFFER_SIZE = "send-buffer-size"; // integer
   public static final String RECEIVE_BUFFER_SIZE = "receive-buffer-size"; // integer
   public static final String VIRTUAL_SERVER = "virtual-server"; // string
   public static final String CONTEXT_PATH = "context-path"; // string
   public static final String SECURITY_DOMAIN = "security-domain"; // string
   public static final String AUTH_METHOD = "auth-method"; // string
   public static final String SECURITY_MODE = "security-mode"; // string

   public static final String TOPOLOGY_STATE_TRANSFER = "topology-state-transfer";
   public static final String LOCK_TIMEOUT = "lock-timeout"; // integer
   public static final String REPLICATION_TIMEOUT = "replication-timeout"; // integer
   public static final String UPDATE_TIMEOUT = "update-timeout"; // integer
   public static final String EXTERNAL_HOST = "external-host"; // string
   public static final String EXTERNAL_PORT = "external-port"; // integer
   public static final String LAZY_RETRIEVAL = "lazy-retrieval"; // boolean


   public static final String[] CONNECTORS = { HOTROD_CONNECTOR, MEMCACHED_CONNECTOR, REST_CONNECTOR };
   public static final String[] CONNECTOR_ATTRIBUTES = { SOCKET_BINDING, NAME, CACHE_CONTAINER, WORKER_THREADS, IDLE_TIMEOUT, TCP_NODELAY, SEND_BUFFER_SIZE, RECEIVE_BUFFER_SIZE, VIRTUAL_SERVER, CONTEXT_PATH, SECURITY_DOMAIN, AUTH_METHOD, SECURITY_MODE };

   public static final String[] TOPOLOGY_ATTRIBUTES = { LOCK_TIMEOUT, REPLICATION_TIMEOUT, UPDATE_TIMEOUT, EXTERNAL_HOST, EXTERNAL_PORT, LAZY_RETRIEVAL };
   public static final String CONNECTOR = "connector";
}
