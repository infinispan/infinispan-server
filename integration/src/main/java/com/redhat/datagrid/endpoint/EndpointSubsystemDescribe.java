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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

import com.redhat.datagrid.DataGridConstants;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
class EndpointSubsystemDescribe implements OperationStepHandler, DescriptionProvider {

   static final EndpointSubsystemDescribe INSTANCE = new EndpointSubsystemDescribe();

   @Override
   public ModelNode getModelDescription(Locale locale) {
      return EndpointSubsystemProviders.SUBSYSTEM_DESCRIBE.getModelDescription(locale);
   }

   private static ModelNode createEmptyAddOperation() {
      final ModelNode subsystem = new ModelNode();
      subsystem.get(OP).set(ADD);
      subsystem.get(OP_ADDR).add(SUBSYSTEM, DataGridConstants.SN_ENDPOINT.getSimpleName());
      return subsystem;
   }

   @Override
   public void execute(OperationContext context, ModelNode operation)
            throws OperationFailedException {

      ModelNode add = createEmptyAddOperation();

      final ModelNode model = context.readModel(PathAddress.EMPTY_ADDRESS);
 
      if (model.hasDefined(ModelKeys.CONNECTOR)) {
         add.get(ModelKeys.CONNECTOR).set(model.get(ModelKeys.CONNECTOR));
      }
      if (model.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER)) {
         add.get(ModelKeys.TOPOLOGY_STATE_TRANSFER).set(
                  model.get(ModelKeys.TOPOLOGY_STATE_TRANSFER));
      }
      
      context.getResult().add(add);

      context.completeStep();

   }
}
