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
package org.jboss.datagrid.endpoint.hotrod;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.infinispan.server.hotrod.HotRodServer;
import org.jboss.datagrid.endpoint.EndpointService;

class HotRodService extends EndpointService<HotRodServer> {

    @Override
    protected HotRodServer doStart(String configPath) throws Exception {
        Properties props = new Properties();
        InputStream in = new FileInputStream(configPath);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        HotRodServer server = new HotRodServer();
        server.start(props, getCacheManager().getValue());
        return server;
    }

    @Override
    protected void doStop(HotRodServer server) throws Exception {
        server.stop();
    }
}
