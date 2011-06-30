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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

public abstract class DataGridExtension implements Extension, DescriptionProvider {

    private final String subsystemName;
    private final String namespaceUri;
    private final DataGridSubsystemParser parser;
    private final DataGridSubsystemAdd subsystemAdd;
    private final DataGridSubsystemDescribe subsystemDescribe;

    public DataGridExtension(ServiceName serviceName, String namespaceUri, String defaultConfigFileName) {
        subsystemName = serviceName.getSimpleName();
        this.namespaceUri = namespaceUri;
        parser = new DataGridSubsystemParser(subsystemName, namespaceUri);
        subsystemAdd = new DataGridSubsystemAdd(serviceName, defaultConfigFileName) {
            @Override
            protected DataGridService<?> createService() {
                return DataGridExtension.this.createService();
            }

            @Override
            protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
                super.buildService(service, builder);
                DataGridExtension.this.buildService(service, builder);
            }
        };
        subsystemDescribe = new DataGridSubsystemDescribe();
    }

    protected abstract DataGridService<?> createService();

    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        // Do nothing by default.
    }

    @Override
    public final void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(subsystemName);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(this);
        registration.registerOperationHandler(ADD, subsystemAdd, subsystemAdd, false);
        registration.registerOperationHandler(DESCRIBE, subsystemDescribe, subsystemDescribe, false, OperationEntry.EntryType.PRIVATE);

        subsystem.registerXMLElementWriter(parser);
    }

    @Override
    public final void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(namespaceUri, parser);
    }
}