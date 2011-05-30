package org.jboss.datagrid.endpoint;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridService;
import org.jboss.datagrid.SecurityActions;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import javax.management.MBeanServer;
import java.io.File;
import java.io.FileNotFoundException;

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
