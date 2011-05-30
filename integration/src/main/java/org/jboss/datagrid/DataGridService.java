package org.jboss.datagrid;

import java.io.File;
import java.io.FileNotFoundException;

import javax.management.MBeanServer;

import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * Service configuring and starting the {@code HornetQService}.
 *
 * @author scott.stark@jboss.org
 * @author Emanuel Muckenhuber
 */
public abstract class DataGridService<S> implements Service<S> {

    String configPath;

    private S server;
    private final InjectedValue<MBeanServer> mbeanServer = new InjectedValue<MBeanServer>();

    public Injector<String> getConfigPathInjector() {
        return new Injector<String>() {

            @Override
            public void inject(String configPath) throws InjectionException {
                DataGridService.this.configPath = configPath;
            }

            @Override
            public void uninject() {
                DataGridService.this.configPath = null;
            }
        };
    }

    public InjectedValue<MBeanServer> getMBeanServer() {
        return mbeanServer;
    }

    @Override
    public synchronized void start(final StartContext context) throws StartException {
        ClassLoader origTCCL = SecurityActions.getContextClassLoader();
        boolean done = false;
        try {
            if (!new File(configPath).exists()) {
                throw new FileNotFoundException(
                        "Endpoint configuration file not found: " + configPath);
            }

            if (server == null) {
                server = doStart(configPath);
                done = true;
            }
        } catch (Exception e) {
            throw new StartException("Failed to start service", e);
        } finally {
            if (!done) {
                server = null;
            }

            SecurityActions.setContextClassLoader(origTCCL);
        }
    }

    protected abstract S doStart(String configPath) throws Exception;

    @Override
    public synchronized void stop(final StopContext context) {
        try {
            if (server != null) {
                doStop(server);
                server = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to shutdown HornetQ server", e);
        }
    }

    protected abstract void doStop(S server) throws Exception;

    @Override
    public synchronized S getValue() throws IllegalStateException {
        final S server = this.server;
        if (server == null) {
            throw new IllegalStateException();
        }
        return server;
    }
}
