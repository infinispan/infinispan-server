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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.as.controller.services.path.PathManagerService;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * RestSubsystemAdd.
 *
 * @author Tristan Tarrant
 * @since 5.1
 */
class RestSubsystemAdd extends AbstractAddStepHandler {

   static final RestSubsystemAdd INSTANCE = new RestSubsystemAdd();

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
      // Read the full model
      ModelNode config = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));

      // Create the service
      final RestService service = new RestService(config);

      // Setup the various dependencies with injectors and install the service
      ServiceBuilder<?> builder = context.getServiceTarget().addService(EndpointUtils.getServiceName(operation, "rest"), service);
      EndpointUtils.addCacheContainerDependency(builder, service.getCacheContainerName(), service.getCacheManager());
      builder.addDependency(PathManagerService.SERVICE_NAME, PathManager.class, service.getPathManagerInjector());
      builder.addDependency(WebSubsystemServices.JBOSS_WEB_HOST.append(service.getVirtualServer()), VirtualHost.class, service.getHostInjector());
      if (service.getSecurityDomain()!=null) {
         EndpointUtils.addSecurityDomainDependency(builder, service.getSecurityDomain(), service.getSecurityDomainContextInjector());
      }
      builder.addListener(verificationHandler);
      builder.setInitialMode(ServiceController.Mode.ACTIVE);
      builder.install();
   }

   @Override
   protected void populateModel(ModelNode source, ModelNode target) throws OperationFailedException {
      populate(source, target);
   }

   private static void populate(ModelNode source, ModelNode target) throws OperationFailedException {
      for(AttributeDefinition attr : ProtocolServerConnectorResource.COMMON_CONNECTOR_ATTRIBUTES) {
         attr.validateAndSet(source, target);
      }
      for(AttributeDefinition attr : RestConnectorResource.REST_ATTRIBUTES) {
         attr.validateAndSet(source, target);
      }
   }

   @Override
   protected boolean requiresRuntimeVerification() {
      return false;
   }
}
