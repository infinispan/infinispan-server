/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */
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
