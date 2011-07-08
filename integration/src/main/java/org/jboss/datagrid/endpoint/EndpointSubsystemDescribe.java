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
package org.jboss.datagrid.endpoint;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
class EndpointSubsystemDescribe implements OperationStepHandler, DescriptionProvider {

    static final EndpointSubsystemDescribe INSTANCE = new EndpointSubsystemDescribe();

    @Override
    public ModelNode getModelDescription(Locale locale) {
        return new ModelNode();
    }

    @Override
    public void execute(OperationContext context, ModelNode operation)
            throws OperationFailedException {

        ModelNode result = context.getResult();

        PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(ModelDescriptionConstants.OP_ADDR)).getLastElement());

        @SuppressWarnings("deprecation")
        ModelNode subModel = context.readModel(PathAddress.EMPTY_ADDRESS);

        result.add(EndpointSubsystemAdd.createOperation(rootAddress.toModelNode(), subModel));

        context.completeStep();

    }
}
