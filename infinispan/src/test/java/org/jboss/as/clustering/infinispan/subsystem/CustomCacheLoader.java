package org.jboss.as.clustering.infinispan.subsystem;

import java.util.Set;

import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.AbstractCacheLoader;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;

public class CustomCacheLoader extends AbstractCacheLoader {

    @Override
    public Class<? extends CacheLoaderConfig> getConfigurationClass() {
        return null;
    }

    @Override
    public InternalCacheEntry load(Object arg0) throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<InternalCacheEntry> load(int arg0) throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<InternalCacheEntry> loadAll() throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<Object> loadAllKeys(Set<Object> arg0) throws CacheLoaderException {
        return null;
    }

    @Override
    public void start() throws CacheLoaderException {
    }

    @Override
    public void stop() throws CacheLoaderException {
    }

}
