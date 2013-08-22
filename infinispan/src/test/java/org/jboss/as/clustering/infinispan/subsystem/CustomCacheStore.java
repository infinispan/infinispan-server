package org.jboss.as.clustering.infinispan.subsystem;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.AbstractCacheStore;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;

public class CustomCacheStore extends AbstractCacheStore {

    @Override
    public void clear() throws CacheLoaderException {
        //FIXME implement me
    }

    @Override
    public void fromStream(ObjectInput arg0) throws CacheLoaderException {
        //FIXME implement me
    }

    @Override
    public boolean remove(Object arg0) throws CacheLoaderException {
        return false;
    }

    @Override
    public void store(InternalCacheEntry arg0) throws CacheLoaderException {
        //FIXME implement me
    }

    @Override
    public void toStream(ObjectOutput arg0) throws CacheLoaderException {
        //FIXME implement me
    }

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
    protected void purgeInternal() throws CacheLoaderException {
    }

}
