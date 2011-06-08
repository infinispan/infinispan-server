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
package org.jboss.datagrid.endpoint.rest;

import java.lang.reflect.Method;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 */
public class ServiceActivator implements org.jboss.msc.service.ServiceActivator {

    private static final ServiceName SN_CACHEMANAGER =
        ServiceName.JBOSS.append("datagrid", "cachemanager");

    private static final ServiceName SN_REST =
        ServiceName.JBOSS.append("datagrid", "rest");

    @Override
    public void activate(ServiceActivatorContext ctx)
            throws ServiceRegistryException {
        final Method setter;
        try {
            Class<?> cls = Class.forName(
                    "org.infinispan.rest.ManagerInstance", true, getClass().getClassLoader());
            setter = cls.getMethod("instance_$eq", EmbeddedCacheManager.class);
        } catch (Exception e) {
            throw new ServiceRegistryException(
                    "failed to locate ManagerInstance.instance", e);
        }

        ServiceTarget serviceTarget = ctx.getServiceTarget();
        ServiceBuilder<Void> serviceBuilder = serviceTarget.addService(SN_REST, new DummyService());
        serviceBuilder.addDependency(SN_CACHEMANAGER, EmbeddedCacheManager.class, new Injector<EmbeddedCacheManager>() {
            @Override
            public void inject(EmbeddedCacheManager value)
                    throws InjectionException {
                if (value == null) {
                    throw new InjectionException("value is null.");
                }
                try {
                    setter.invoke(null, value);
                } catch (Exception e) {
                    throw new InjectionException("failed to inject ManagerInstance.instance", e);
                }
            }

            @Override
            public void uninject() {
                try {
                    setter.invoke(null, (Object) null );
                } catch (Exception e) {
                    throw new InjectionException("failed to uninject ManagerInstance.instance", e);
                }
            }
        }).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }
}
