package com.redhat.datagrid.endpoint;

public class ModelKeys {

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

}
