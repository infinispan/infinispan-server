package org.infinispan.persistence.cluster;

import java.util.Properties;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;
import org.infinispan.configuration.cache.SingletonStoreConfiguration;

/**
 * Test configuration for BrnoCacheStore.
 * Copy of ClusterLoaderConfiguration with a new brnoProperty.
 *
 * @author Jakub Markos
 */
@ConfigurationFor(BrnoCacheStore.class)
@BuiltBy(BrnoCacheStoreConfigurationBuilder.class)
public class BrnoCacheStoreConfiguration extends AbstractStoreConfiguration {

    private int brnoProperty;

    public BrnoCacheStoreConfiguration(boolean purgeOnStartup, boolean fetchPersistentState,
                                       boolean ignoreModifications, AsyncStoreConfiguration async,
                                       SingletonStoreConfiguration singletonStore, boolean preload, boolean shared, Properties properties,
                                       int brnoProperty) {
        super(purgeOnStartup, fetchPersistentState, ignoreModifications, async, singletonStore, preload, shared, properties);
        this.brnoProperty = brnoProperty;
    }

    public int brnoProperty() {
        return brnoProperty;
    }

    @Override
    public String toString() {
        return "BrnoCacheStoreConfiguration [brnoProperty=" + brnoProperty + "]";
    }
}
