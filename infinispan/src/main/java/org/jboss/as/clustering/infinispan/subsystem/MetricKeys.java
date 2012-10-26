/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.clustering.infinispan.subsystem;

public class MetricKeys {
    public static final String CACHE_MANAGER_STATUS = "cacheManagerStatus";
    public static final String IS_COORDINATOR = "isCoordinator";
    public static final String COORDINATOR_ADDRESS = "coordinatorAddress";
    public static final String LOCAL_ADDRESS = "localAddress";
    public static final String CLUSTER_NAME = "clusterName";

    public static final String BYTES_READ = "bytesRead";
    public static final String BYTES_WRITTEN = "bytesWritten";

    public static final String CACHE_STATUS = "cacheStatus";
    public static final String CONCURRENCY_LEVEL = "concurrencyLevel";
    public static final String NUMBER_OF_LOCKS_AVAILABLE = "numberOfLocksAvailable";
    public static final String NUMBER_OF_LOCKS_HELD = "numberOfLocksHeld";

    public static final String TOTAL_NUMBER_OF_DETECTED_DEADLOCKS = "totalNumberOfDetectedDeadlocks";
    public static final String NUMBER_OF_LOCAL_DETECTED_DEADLOCKS = "numberOfLocalDetectedDeadlocks";
    public static final String NUMBER_OF_REMOTE_DETECTED_DEADLOCKS = "numberOfRemoteDetectedDeadlocks";
    public static final String NUMBER_OF_UNSOLVABLE_DEADLOCKS = "numberOfUnsolvableDeadlocks";

    public static final String AVERAGE_READ_TIME = "averageReadTime";
    public static final String AVERAGE_WRITE_TIME = "averageWriteTime";
    public static final String ELAPSED_TIME = "elapsedTime";
    public static final String EVICTIONS = "evictions";
    public static final String HIT_RATIO = "hitRatio";
    public static final String HITS = "hits";
    public static final String MISSES = "misses";
    public static final String NUMBER_OF_ENTRIES = "numberOfEntries";
    public static final String READ_WRITE_RATIO = "readWriteRatio";
    public static final String REMOVE_HITS = "removeHits";
    public static final String REMOVE_MISSES = "removeMisses";
    public static final String STORES = "stores";
    public static final String TIME_SINCE_RESET = "timeSinceReset";
    public static final String JOIN_COMPLETE = "joinComplete";
    public static final String STATE_TRANSFER_IN_PROGRESS = "stateTransferInProgress";
    public static final String AVERAGE_REPLICATION_TIME = "averageReplicationTime";
    public static final String REPLICATION_COUNT = "replicationCount";
    public static final String REPLICATION_FAILURES = "replicationFailures";
    public static final String SUCCESS_RATIO = "successRatio";

    public static final String COMMITS = "commits";
    public static final String PREPARES = "prepares";
    public static final String ROLLBACKS = "rollbacks";

    public static final String INVALIDATIONS = "invalidations";
    public static final String PASSIVATIONS = "passivations";

    public static final String ACTIVATIONS = "activations";
    public static final String CACHE_LOADER_LOADS = "cacheLoaderLoads";
    public static final String CACHE_LOADER_MISSES = "cacheLoaderMisses";

    public static final String CACHE_LOADER_STORES = "cacheLoaderStores";
}
