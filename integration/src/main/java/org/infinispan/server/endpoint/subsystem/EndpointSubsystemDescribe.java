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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
class EndpointSubsystemDescribe implements OperationStepHandler, DescriptionProvider {

   static final EndpointSubsystemDescribe INSTANCE = new EndpointSubsystemDescribe();

   @Override
   public ModelNode getModelDescription(Locale locale) {
       return CommonDescriptions.getSubsystemDescribeOperation(locale);
   }

   @Override
   public void execute(OperationContext context, ModelNode operation)
            throws OperationFailedException {

      final ModelNode result = context.getResult();
      final PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(OP_ADDR)).getLastElement());
      final ModelNode subModel = context.readModel(PathAddress.EMPTY_ADDRESS);

      final ModelNode subsystemAdd = new ModelNode();
      subsystemAdd.get(OP).set(ADD);
      subsystemAdd.get(OP_ADDR).set(rootAddress.toModelNode());

      result.add(subsystemAdd);
 
      for(String connectorType : ModelKeys.CONNECTORS) {
         if(subModel.hasDefined(connectorType)) {
            for (final Property connector : subModel.get(connectorType).asPropertyList()) {
               final ModelNode address = rootAddress.toModelNode();
               address.add(connectorType, connector.getName());
               final ModelNode addOperation = Util.getEmptyOperation(ADD, address);
               for(String connectorAttribute : ModelKeys.CONNECTOR_ATTRIBUTES) {
                  if(connector.getValue().hasDefined(connectorAttribute)) {
                     addOperation.get(connectorAttribute).set(connector.getValue().get(connectorAttribute));
                  }
               }
               
               result.add(addOperation);
            }
         }
      }

      context.completeStep();

   }
}
