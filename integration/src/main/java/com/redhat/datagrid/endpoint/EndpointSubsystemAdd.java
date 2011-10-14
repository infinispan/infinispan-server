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

import java.util.List;
import java.util.Locale;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import com.redhat.datagrid.DataGridConstants;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
class EndpointSubsystemAdd extends AbstractAddStepHandler implements DescriptionProvider {

   static final EndpointSubsystemAdd INSTANCE = new EndpointSubsystemAdd(
            DataGridConstants.SN_ENDPOINT);

   static ModelNode createOperation(ModelNode address, ModelNode existing) {
      ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      populate(existing, operation);
      return operation;
   }

   private static void populate(ModelNode source, ModelNode target) {
      target.setEmptyObject();
      if (source.hasDefined(ModelKeys.CONNECTOR)) {
         target.get(ModelKeys.CONNECTOR).set(source.get(ModelKeys.CONNECTOR));
      }
      if (source.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER)) {
         target.get(ModelKeys.TOPOLOGY_STATE_TRANSFER).set(
                  source.get(ModelKeys.TOPOLOGY_STATE_TRANSFER));
      }
   }

   private final ServiceName serviceName;

   EndpointSubsystemAdd(ServiceName serviceName) {
      this.serviceName = serviceName;
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      return EndpointSubsystemProviders.SUBSYTEM_ADD.getModelDescription(locale);
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

      // Create the service
      final EndpointService service = new EndpointService(operation);

      // Add and install the service
      ServiceBuilder<?> builder = context.getServiceTarget().addService(serviceName, service);

      String cacheContainerName = service.getCacheContainerName();
      ServiceName cacheContainerServiceName = ServiceName.JBOSS.append("infinispan");
      if (cacheContainerName != null) {
         cacheContainerServiceName = cacheContainerServiceName.append(cacheContainerName);
      }

      builder.addDependency(ServiceBuilder.DependencyType.REQUIRED, cacheContainerServiceName,
               EmbeddedCacheManager.class, service.getCacheManager());

      for (final String socketBinding : service.getRequiredSocketBindingNames()) {
         final ServiceName socketName = SocketBinding.JBOSS_BINDING_NAME.append(socketBinding);
         builder.addDependency(socketName, SocketBinding.class,
                  service.getSocketBinding(socketBinding));
      }

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
