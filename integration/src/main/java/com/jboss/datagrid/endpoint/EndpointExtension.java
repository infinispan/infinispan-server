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
package com.jboss.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

import com.jboss.datagrid.DataGridConstants;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
public class EndpointExtension implements Extension {

   private final String namespaceUri = DataGridConstants.NS_DATAGRID_1_0;
   private final EndpointSubsystemParser parser = new EndpointSubsystemParser(DataGridConstants.SUBSYSTEM_NAME, namespaceUri);

   @Override
   public final void initialize(ExtensionContext context) {
      final SubsystemRegistration registration = context.registerSubsystem(DataGridConstants.SUBSYSTEM_NAME, 1, 0);
      registration.registerXMLElementWriter(parser);

      final ManagementResourceRegistration subsystem = registration.registerSubsystemModel(EndpointSubsystemProviders.SUBSYSTEM);
      subsystem.registerOperationHandler(ADD, EndpointSubsystemAdd.INSTANCE, EndpointSubsystemAdd.INSTANCE, false);
      subsystem.registerOperationHandler(DESCRIBE, EndpointSubsystemDescribe.INSTANCE, EndpointSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);

      final ManagementResourceRegistration hotrodConnector = subsystem.registerSubModel(PathElement.pathElement(ModelKeys.HOTROD_CONNECTOR), EndpointSubsystemProviders.HOTROD_CONNECTOR_DESC);
      hotrodConnector.registerOperationHandler(ADD, HotRodSubsystemAdd.INSTANCE, EndpointSubsystemProviders.ADD_HOTROD_CONNECTOR_DESC, false);
      hotrodConnector.registerOperationHandler(REMOVE, HotRodSubsystemRemove.INSTANCE, EndpointSubsystemProviders.REMOVE_HOTROD_CONNECTOR_DESC, false);

      final ManagementResourceRegistration memcachedConnector = subsystem.registerSubModel(PathElement.pathElement(ModelKeys.MEMCACHED_CONNECTOR), EndpointSubsystemProviders.MEMCACHED_CONNECTOR_DESC);
      memcachedConnector.registerOperationHandler(ADD, MemcachedSubsystemAdd.INSTANCE, EndpointSubsystemProviders.ADD_MEMCACHED_CONNECTOR_DESC, false);
      memcachedConnector.registerOperationHandler(REMOVE, MemcachedSubsystemRemove.INSTANCE, EndpointSubsystemProviders.REMOVE_MEMCACHED_CONNECTOR_DESC, false);

      final ManagementResourceRegistration restConnector = subsystem.registerSubModel(PathElement.pathElement(ModelKeys.REST_CONNECTOR), EndpointSubsystemProviders.REST_CONNECTOR_DESC);
      restConnector.registerOperationHandler(ADD, RestSubsystemAdd.INSTANCE, EndpointSubsystemProviders.ADD_REST_CONNECTOR_DESC, false);
      restConnector.registerOperationHandler(REMOVE, RestSubsystemRemove.INSTANCE, EndpointSubsystemProviders.REMOVE_REST_CONNECTOR_DESC, false);

   }

   @Override
   public final void initializeParsers(ExtensionParsingContext context) {
      context.setSubsystemXmlMapping(DataGridConstants.SUBSYSTEM_NAME, namespaceUri, parser);
   }
}