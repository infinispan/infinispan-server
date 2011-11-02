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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import java.util.Locale;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

import com.redhat.datagrid.DataGridConstants;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
public class EndpointExtension implements Extension, DescriptionProvider {

   private final ServiceName serviceName = DataGridConstants.SN_ENDPOINT;
   private final String subsystemName = serviceName.getSimpleName();
   private final String namespaceUri = DataGridConstants.NS_ENDPOINT_1_0;
   private final EndpointSubsystemParser parser = new EndpointSubsystemParser(subsystemName,
            namespaceUri);

   @Override
   public final void initialize(ExtensionContext context) {
      final SubsystemRegistration registration = context.registerSubsystem(subsystemName);
      registration.registerXMLElementWriter(parser);

      final ManagementResourceRegistration subsystem = registration.registerSubsystemModel(this);
      subsystem.registerOperationHandler(ADD, EndpointSubsystemAdd.INSTANCE,
               EndpointSubsystemAdd.INSTANCE, false);
      subsystem.registerOperationHandler(DESCRIBE, EndpointSubsystemDescribe.INSTANCE,
               EndpointSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);

   }

   @Override
   public final void initializeParsers(ExtensionParsingContext context) {
      context.setSubsystemXmlMapping(namespaceUri, parser);
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      return EndpointSubsystemProviders.SUBSYSTEM.getModelDescription(locale);
   }
}