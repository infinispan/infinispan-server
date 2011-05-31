package org.jboss.datagrid.endpoint;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridService;
import org.jboss.msc.value.InjectedValue;

/**
 * Service configuring and starting the {@code HornetQService}.
 *
 * @author scott.stark@jboss.org
 * @author Emanuel Muckenhuber
 */
public abstract class EndpointService<S> extends DataGridService<S> {

    private final InjectedValue<EmbeddedCacheManager> cacheManager = new InjectedValue<EmbeddedCacheManager>();

    public InjectedValue<EmbeddedCacheManager> getCacheManager() {
        return cacheManager;
    }
}
