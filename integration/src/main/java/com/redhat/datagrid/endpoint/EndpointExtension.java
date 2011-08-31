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
package com.redhat.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Locale;

import com.redhat.datagrid.DataGridConstants;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

public class EndpointExtension implements Extension, DescriptionProvider {

    private final ServiceName serviceName = DataGridConstants.SN_ENDPOINT;
    private final String subsystemName = serviceName.getSimpleName();
    private final String namespaceUri = DataGridConstants.NS_ENDPOINT_1_0;
    private final EndpointSubsystemParser parser = new EndpointSubsystemParser(subsystemName, namespaceUri);
    private final EndpointSubsystemAdd subsystemAdd = new EndpointSubsystemAdd(serviceName);
    private final EndpointSubsystemDescribe subsystemDescribe = new EndpointSubsystemDescribe();

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

    @Override
    public ModelNode getModelDescription(Locale arg0) {
        return new ModelNode();
    }
}