/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.clustering.infinispan.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.interceptors.ActivationInterceptor;
import org.infinispan.interceptors.CacheMgmtInterceptor;
import org.infinispan.interceptors.InvalidationInterceptor;
import org.infinispan.interceptors.PassivationInterceptor;
import org.infinispan.interceptors.TxInterceptor;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.remoting.rpc.RpcManagerImpl;
import org.infinispan.util.concurrent.locks.LockManagerImpl;
import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

public class CacheMetricsHandler extends AbstractRuntimeOnlyHandler {
    public static final CacheMetricsHandler INSTANCE = new CacheMetricsHandler();

    public enum CacheMetrics {
        CACHE_STATUS(MetricKeys.CACHE_STATUS, ModelType.STRING, true),
        // LockManager
        NUMBER_OF_LOCKS_AVAILABLE(MetricKeys.NUMBER_OF_LOCKS_AVAILABLE, ModelType.INT, true),
        NUMBER_OF_LOCKS_HELD(MetricKeys.NUMBER_OF_LOCKS_HELD, ModelType.INT, true),
        CONCURRENCY_LEVEL(MetricKeys.CONCURRENCY_LEVEL, ModelType.INT, true),
        // CacheMgmtInterceptor
        AVERAGE_READ_TIME(MetricKeys.AVERAGE_READ_TIME, ModelType.LONG, true),
        AVERAGE_WRITE_TIME(MetricKeys.AVERAGE_WRITE_TIME, ModelType.LONG, true),
        ELAPSED_TIME(MetricKeys.ELAPSED_TIME, ModelType.LONG, true),
        EVICTIONS(MetricKeys.EVICTIONS, ModelType.LONG, true),
        HIT_RATIO(MetricKeys.HIT_RATIO, ModelType.DOUBLE, true),
        HITS(MetricKeys.HITS, ModelType.LONG, true),
        MISSES(MetricKeys.MISSES, ModelType.LONG, true),
        NUMBER_OF_ENTRIES(MetricKeys.NUMBER_OF_ENTRIES, ModelType.INT, true),
        READ_WRITE_RATIO(MetricKeys.READ_WRITE_RATIO, ModelType.DOUBLE, true),
        REMOVE_HITS(MetricKeys.REMOVE_HITS, ModelType.LONG, true),
        REMOVE_MISSES(MetricKeys.REMOVE_MISSES, ModelType.LONG, true),
        STORES(MetricKeys.STORES, ModelType.LONG, true),
        TIME_SINCE_RESET(MetricKeys.TIME_SINCE_RESET, ModelType.LONG, true),
        // RpcManager
        AVERAGE_REPLICATION_TIME(MetricKeys.AVERAGE_REPLICATION_TIME, ModelType.LONG, true, true),
        REPLICATION_COUNT(MetricKeys.REPLICATION_COUNT, ModelType.LONG, true, true),
        REPLICATION_FAILURES(MetricKeys.REPLICATION_FAILURES, ModelType.LONG, true, true),
        SUCCESS_RATIO(MetricKeys.SUCCESS_RATIO, ModelType.DOUBLE, true, true),
        // TxInterceptor
        COMMITS(MetricKeys.COMMITS, ModelType.LONG, true),
        PREPARES(MetricKeys.PREPARES, ModelType.LONG, true),
        ROLLBACKS(MetricKeys.ROLLBACKS, ModelType.LONG, true),
        // InvalidationInterceptor
        INVALIDATIONS(MetricKeys.INVALIDATIONS, ModelType.LONG, true),
        // PassivationInterceptor
        PASSIVATIONS(MetricKeys.PASSIVATIONS, ModelType.STRING, true),
        // ActivationInterceptor
        ACTIVATIONS(MetricKeys.ACTIVATIONS, ModelType.STRING, true),
        CACHE_LOADER_LOADS(MetricKeys.CACHE_LOADER_LOADS, ModelType.LONG, true),
        CACHE_LOADER_MISSES(MetricKeys.CACHE_LOADER_MISSES, ModelType.LONG, true),
        ;

        private static final Map<String, CacheMetrics> MAP = new HashMap<String, CacheMetrics>();
        static {
            for (CacheMetrics metric : CacheMetrics.values()) {
                MAP.put(metric.toString(), metric);
            }
        }

        final AttributeDefinition definition;
        final boolean clustered;

        private CacheMetrics(final AttributeDefinition definition, final boolean clustered) {
            this.definition = definition;
            this.clustered = clustered;
        }

        private CacheMetrics(String attributeName, ModelType type, boolean allowNull) {
            this(new SimpleAttributeDefinitionBuilder(attributeName, type, allowNull).setStorageRuntime().build(), false);
        }

        private CacheMetrics(String attributeName, ModelType type, boolean allowNull, final boolean clustered) {
            this(new SimpleAttributeDefinitionBuilder(attributeName, type, allowNull).setStorageRuntime().build(), clustered);
        }

        @Override
        public final String toString() {
            return definition.getName();
        }

        public static CacheMetrics getStat(final String stringForm) {
            return MAP.get(stringForm);
        }
    }

    @Override
    protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String cacheContainerName = address.getElement(address.size() - 2).getValue();
        final String cacheName = address.getLastElement().getValue();
        final String attrName = operation.require(ModelDescriptionConstants.NAME).asString();
        CacheMetrics metric = CacheMetrics.getStat(attrName);
        if (metric == null) {
            context.getFailureDescription().set(String.format("Unknown metric %s", attrName));
        } else {
            final ServiceController<?> controller = context.getServiceRegistry(false).getService(
                    CacheService.getServiceName(cacheContainerName, cacheName));
            Cache<?, ?> cache = (Cache<?, ?>) controller.getValue();
            ModelNode result = new ModelNode();
            switch (metric) {
                case CACHE_STATUS:
                    result.set(cache.getAdvancedCache().getStatus().toString());
                    break;
                case CONCURRENCY_LEVEL:
                    result.set(((LockManagerImpl) cache.getAdvancedCache().getLockManager()).getConcurrencyLevel());
                    break;
                case NUMBER_OF_LOCKS_AVAILABLE:
                    result.set(((LockManagerImpl) cache.getAdvancedCache().getLockManager()).getNumberOfLocksAvailable());
                    break;
                case NUMBER_OF_LOCKS_HELD:
                    result.set(((LockManagerImpl) cache.getAdvancedCache().getLockManager()).getNumberOfLocksHeld());
                    break;
                case AVERAGE_READ_TIME:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getAverageReadTime());
                    break;
                case AVERAGE_WRITE_TIME:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getAverageWriteTime());
                    break;
                case ELAPSED_TIME:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getElapsedTime());
                    break;
                case EVICTIONS:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getEvictions());
                    break;
                case HIT_RATIO:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getHitRatio());
                    break;
                case HITS:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getHits());
                    break;
                case MISSES:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getMisses());
                    break;
                case NUMBER_OF_ENTRIES:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getNumberOfEntries());
                    break;
                case READ_WRITE_RATIO:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getReadWriteRatio());
                    break;
                case REMOVE_HITS:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getRemoveHits());
                    break;
                case REMOVE_MISSES:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getRemoveMisses());
                    break;
                case STORES:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getStores());
                    break;
                case TIME_SINCE_RESET:
                    result.set(getFirstInterceptorWhichExtends(cache.getAdvancedCache().getInterceptorChain(),
                            CacheMgmtInterceptor.class).getTimeSinceReset());
                    break;
                case AVERAGE_REPLICATION_TIME:
                    result.set(((RpcManagerImpl) cache.getAdvancedCache().getRpcManager()).getAverageReplicationTime());
                    break;
                case REPLICATION_COUNT:
                    result.set(((RpcManagerImpl) cache.getAdvancedCache().getRpcManager()).getReplicationCount());
                    break;
                case REPLICATION_FAILURES:
                    result.set(((RpcManagerImpl) cache.getAdvancedCache().getRpcManager()).getReplicationFailures());
                    break;
                case SUCCESS_RATIO:
                    result.set(((RpcManagerImpl) cache.getAdvancedCache().getRpcManager()).getSuccessRatioFloatingPoint());
                    break;
                case COMMITS: {
                    TxInterceptor txInterceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), TxInterceptor.class);
                    result.set(txInterceptor!=null?txInterceptor.getCommits():0);
                    break;
                }
                case PREPARES: {
                    TxInterceptor txInterceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), TxInterceptor.class);
                    result.set(txInterceptor!=null?txInterceptor.getPrepares():0);
                    break;
                }
                case ROLLBACKS: {
                    TxInterceptor txInterceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), TxInterceptor.class);
                    result.set(txInterceptor!=null?txInterceptor.getRollbacks():0);
                    break;
                }
                case INVALIDATIONS: {
                    InvalidationInterceptor invInterceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), InvalidationInterceptor.class);
                    result.set(invInterceptor!=null?invInterceptor.getInvalidations():0);
                    break;
                }
                case PASSIVATIONS: {
                    PassivationInterceptor interceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), PassivationInterceptor.class);
                    result.set(interceptor!=null?interceptor.getPassivations():"");
                    break;
                }
                case ACTIVATIONS: {
                    ActivationInterceptor interceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), ActivationInterceptor.class);
                    result.set(interceptor!=null?interceptor.getActivations():"");
                    break;
                }
                case CACHE_LOADER_LOADS: {
                    ActivationInterceptor interceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), ActivationInterceptor.class);
                    result.set(interceptor!=null?interceptor.getCacheLoaderLoads():0);
                    break;
                }
                case CACHE_LOADER_MISSES: {
                    ActivationInterceptor interceptor = getFirstInterceptorWhichExtends(cache.getAdvancedCache()
                            .getInterceptorChain(), ActivationInterceptor.class);
                    result.set(interceptor!=null?interceptor.getCacheLoaderMisses():0);
                    break;
                }
            }
            context.getResult().set(result);
        }
        context.completeStep();
    }

    public void registerCommonMetrics(ManagementResourceRegistration container) {
        for (CacheMetrics metric : CacheMetrics.values()) {
            if (!metric.clustered) {
                container.registerMetric(metric.definition, this);
            }
        }
    }

    public void registerClusteredMetrics(ManagementResourceRegistration container) {
        for (CacheMetrics metric : CacheMetrics.values()) {
            if (metric.clustered) {
                container.registerMetric(metric.definition, this);
            }
        }
    }

    private static <T extends CommandInterceptor> T getFirstInterceptorWhichExtends(List<CommandInterceptor> interceptors,
            Class<T> interceptorClass) {
        for (CommandInterceptor interceptor : interceptors) {
            boolean isSubclass = interceptorClass.isAssignableFrom(interceptor.getClass());
            if (isSubclass) {
                Collections.emptyList();
                return (T) interceptor;
            }
        }
        return null;
    }
}
