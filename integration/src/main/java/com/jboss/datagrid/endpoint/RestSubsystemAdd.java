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

import static com.jboss.datagrid.endpoint.EndpointUtils.copyIfSet;

import java.util.List;
import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.services.path.AbstractPathService;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
class RestSubsystemAdd extends AbstractAddStepHandler implements DescriptionProvider {

   static final RestSubsystemAdd INSTANCE = new RestSubsystemAdd();

   static ModelNode createOperation(ModelNode address, ModelNode existing) {
      ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      populate(existing, operation);
      return operation;
   }

   private static void populate(ModelNode source, ModelNode target) {
      target.setEmptyObject();

      copyIfSet(ModelKeys.NAME, source, target);
      copyIfSet(ModelKeys.CACHE_CONTAINER, source, target);
      copyIfSet(ModelKeys.VIRTUAL_SERVER, source, target);
      copyIfSet(ModelKeys.CONTEXT_PATH, source, target);
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      return EndpointSubsystemProviders.ADD_REST_CONNECTOR_DESC.getModelDescription(locale);
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

      // Create the service
      final RestService service = new RestService(operation);

      // Setup the various dependencies with injectors and install the service
      ServiceBuilder<?> builder = context.getServiceTarget().addService(EndpointUtils.getServiceName(operation, "rest"), service);
      EndpointUtils.addCacheContainerDependency(context, builder, service.getCacheContainerName(), service.getCacheManager());
      builder.addDependency(AbstractPathService.pathNameOf(ServerEnvironment.HOME_DIR), String.class, service.getPathInjector());
      builder.addDependency(WebSubsystemServices.JBOSS_WEB_HOST.append(service.getVirtualServer()), VirtualHost.class, service.getHostInjector());
      builder.addListener(verificationHandler);
      builder.setInitialMode(ServiceController.Mode.ACTIVE);
      builder.install();
   }

   @Override
   protected void populateModel(ModelNode source, ModelNode target) throws OperationFailedException {
      populate(source, target);
   }

   @Override
   protected boolean requiresRuntimeVerification() {
      return false;
   }
}
