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
package org.jboss.datagrid.endpoint.memcached;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.datagrid.DataGridConstants;
import org.jboss.datagrid.DataGridExtension;
import org.jboss.datagrid.DataGridService;
import org.jboss.msc.service.ServiceBuilder;

public class MemcachedExtension extends DataGridExtension {

    public MemcachedExtension() {
        super(
                DataGridConstants.SN_MEMCACHED,
                DataGridConstants.NS_MEMCACHED_1_0,
                DataGridConstants.CF_MEMCACHED);
    }

    @Override
    protected DataGridService<?> createService() {
        return new MemcachedService();
    }

    @Override
    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        super.buildService(service, builder);
        builder.addDependency(
                ServiceBuilder.DependencyType.REQUIRED,
                DataGridConstants.SN_CACHEMANAGER,
                EmbeddedCacheManager.class,
                ((MemcachedService) service).getCacheManager());
    }
}