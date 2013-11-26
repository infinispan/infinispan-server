package org.infinispan.persistence.cluster;

import java.util.Properties;

import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.parsing.XmlConfigHelper;

public class BrnoCacheStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<BrnoCacheStoreConfiguration, BrnoCacheStoreConfigurationBuilder> {
    private int brnoProperty;

    public BrnoCacheStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
        super(builder);
    }

    @Override
    public BrnoCacheStoreConfigurationBuilder self() {
        return this;
    }

    public BrnoCacheStoreConfigurationBuilder brnoProperty(int brnoProperty) {
        this.brnoProperty = brnoProperty;
        return this;
    }

    @Override
    public BrnoCacheStoreConfigurationBuilder withProperties(Properties p) {
        this.properties = p;
        XmlConfigHelper.setValues(this, properties, false, true);
        return this;
    }

    @Override
    public void validate() {
    }

    @Override
    public BrnoCacheStoreConfiguration create() {
        return new BrnoCacheStoreConfiguration(purgeOnStartup, fetchPersistentState, ignoreModifications, async.create(),
                singletonStore.create(), preload, shared, properties, brnoProperty);
    }

    @Override
    public BrnoCacheStoreConfigurationBuilder read(BrnoCacheStoreConfiguration template) {
        this.brnoProperty = template.brnoProperty();
        this.properties = template.properties();
        return this;
    }
}