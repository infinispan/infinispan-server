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

import static org.infinispan.server.endpoint.subsystem.EndpointUtils.copyIfSet;

import java.util.List;
import java.util.Locale;

import org.infinispan.server.memcached.MemcachedServer;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
class MemcachedSubsystemAdd extends AbstractAddStepHandler implements DescriptionProvider {

   static final MemcachedSubsystemAdd INSTANCE = new MemcachedSubsystemAdd();

   static ModelNode createOperation(ModelNode address, ModelNode existing) {
      ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      populate(existing, operation);
      return operation;
   }

   private static void populate(ModelNode source, ModelNode target) {
      target.setEmptyObject();

      copyIfSet(ModelKeys.NAME, source, target);
      copyIfSet(ModelKeys.CACHE_CONTAINER, source, target);
      copyIfSet(ModelKeys.SOCKET_BINDING, source, target);
      copyIfSet(ModelKeys.IDLE_TIMEOUT, source, target);
      copyIfSet(ModelKeys.TCP_NODELAY, source, target);
      copyIfSet(ModelKeys.RECEIVE_BUFFER_SIZE, source, target);
      copyIfSet(ModelKeys.SEND_BUFFER_SIZE, source, target);
      copyIfSet(ModelKeys.WORKER_THREADS, source, target);
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      return EndpointSubsystemProviders.SUBSYTEM_ADD.getModelDescription(locale);
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
      // Create the service
      final ProtocolServerService service = new ProtocolServerService(operation, MemcachedServer.class);

      // Setup the various dependencies with injectors and install the service
      ServiceBuilder<?> builder = context.getServiceTarget().addService(EndpointUtils.getServiceName(operation, "memcached"), service);
      EndpointUtils.addCacheContainerDependency(context, builder, service.getCacheContainerName(), service.getCacheManager());
      EndpointUtils.addCacheDependency(context, builder, service.getCacheContainerName(), "memcachedCache");
      EndpointUtils.addSocketBindingDependency(builder, service.getRequiredSocketBindingName(), service.getSocketBinding());
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
