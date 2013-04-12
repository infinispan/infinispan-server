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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import static org.infinispan.server.endpoint.subsystem.ModelKeys.*;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author Tristan Tarrant
 */
class EndpointSubsystemAdd extends AbstractAddStepHandler {

   static final EndpointSubsystemAdd INSTANCE = new EndpointSubsystemAdd();
   private static final String[] CONNECTORS = { HOTROD_CONNECTOR, MEMCACHED_CONNECTOR, REST_CONNECTOR };

   static ModelNode createOperation(ModelNode address, ModelNode existing) {
      ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      populate(existing, operation);
      return operation;
   }

   private static void populate(ModelNode source, ModelNode target) {
      for(String connectorType : CONNECTORS) {
         target.get(connectorType).setEmptyObject();
      }
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
