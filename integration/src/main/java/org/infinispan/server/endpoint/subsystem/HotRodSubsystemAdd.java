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
package org.infinispan.server.endpoint.subsystem;

import java.util.List;

import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * @author Tristan Tarrant
 */
class HotRodSubsystemAdd extends ProtocolServiceSubsystemAdd {

   static final ProtocolServiceSubsystemAdd INSTANCE = new HotRodSubsystemAdd();

   @Override
   protected void populateModel(ModelNode source, ModelNode target) throws OperationFailedException {
      populate(source, target);
   }

   private static void populate(ModelNode source, ModelNode target) throws OperationFailedException {
      for(AttributeDefinition attr : ProtocolServerConnectorResource.COMMON_CONNECTOR_ATTRIBUTES) {
         attr.validateAndSet(source, target);
      }
      for(AttributeDefinition attr : ProtocolServerConnectorResource.PROTOCOL_SERVICE_ATTRIBUTES) {
         attr.validateAndSet(source, target);
      }
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
      // Read the full model
      ModelNode config = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
      // Create the builder
      HotRodServerConfigurationBuilder configurationBuilder = new HotRodServerConfigurationBuilder();
      configureProtocolServer(configurationBuilder, config);
      configureProtocolServerSecurity(configurationBuilder, config);
      configureProtocolServerTopology(configurationBuilder, config);
      // Create the service
      final ProtocolServerService service = new ProtocolServerService(getServiceName(operation), HotRodServer.class, configurationBuilder);

      // Setup the various dependencies with injectors and install the service
      ServiceBuilder<?> builder = context.getServiceTarget().addService(EndpointUtils.getServiceName(operation, "hotrod"), service);
      EndpointUtils.addCacheContainerDependency(builder, getCacheContainerName(operation), service.getCacheManager());
      EndpointUtils.addSocketBindingDependency(builder, getSocketBindingName(operation), service.getSocketBinding());
      if (config.hasDefined(ModelKeys.SECURITY) && config.get(ModelKeys.SECURITY, ModelKeys.SECURITY_NAME).isDefined()) {
         EndpointUtils.addSecurityRealmDependency(builder, config.get(ModelKeys.SECURITY, ModelKeys.SECURITY_NAME, ModelKeys.SECURITY_REALM).asString(), service.getSecurityRealm());
      }

      builder.install();
   }

   private void configureProtocolServerTopology(HotRodServerConfigurationBuilder builder, ModelNode config) {
      if (config.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER) && config.get(ModelKeys.TOPOLOGY_STATE_TRANSFER, ModelKeys.TOPOLOGY_STATE_TRANSFER_NAME).isDefined()) {
         config = config.get(ModelKeys.TOPOLOGY_STATE_TRANSFER, ModelKeys.TOPOLOGY_STATE_TRANSFER_NAME);
         if (config.hasDefined(ModelKeys.LOCK_TIMEOUT)) {
            builder.topologyLockTimeout(config.get(ModelKeys.LOCK_TIMEOUT).asLong());
         }
         if (config.hasDefined(ModelKeys.REPLICATION_TIMEOUT)) {
            builder.topologyReplTimeout(config.get(ModelKeys.REPLICATION_TIMEOUT).asLong());
         }
         if (config.hasDefined(ModelKeys.UPDATE_TIMEOUT)) {
            builder.topologyUpdateTimeout(config.get(ModelKeys.UPDATE_TIMEOUT).asLong());
         }
         if (config.hasDefined(ModelKeys.EXTERNAL_HOST)) {
            builder.proxyHost(config.get(ModelKeys.EXTERNAL_HOST).asString());
         }
         if (config.hasDefined(ModelKeys.EXTERNAL_PORT)) {
            builder.proxyPort(config.get(ModelKeys.EXTERNAL_PORT).asInt());
         }
         if (config.hasDefined(ModelKeys.LAZY_RETRIEVAL)) {
            builder.topologyStateTransfer(!config.get(ModelKeys.LAZY_RETRIEVAL).asBoolean(false));
         }
      }
   }


   private void configureProtocolServerSecurity(HotRodServerConfigurationBuilder builder, ModelNode config) {
      if (config.hasDefined(ModelKeys.SECURITY) && config.get(ModelKeys.SECURITY, ModelKeys.SECURITY_NAME).isDefined()) {
         config = config.get(ModelKeys.SECURITY, ModelKeys.SECURITY_NAME);
         if (config.hasDefined(ModelKeys.SSL)) {
            builder.ssl().enabled(config.get(ModelKeys.SSL).asBoolean());
         }
         if (config.hasDefined(ModelKeys.REQUIRE_SSL_CLIENT_AUTH)) {
            builder.ssl().requireClientAuth(config.get(ModelKeys.REQUIRE_SSL_CLIENT_AUTH).asBoolean());
         }
      }
   }

   @Override
   protected boolean requiresRuntimeVerification() {
      return false;
   }
}
