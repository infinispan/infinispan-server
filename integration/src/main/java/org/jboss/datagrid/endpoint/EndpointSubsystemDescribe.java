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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.datagrid.DataGridConstants;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
class EndpointSubsystemDescribe implements ModelQueryOperationHandler {

    static final EndpointSubsystemDescribe INSTANCE = new EndpointSubsystemDescribe();

    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {

        final ModelNode subsystemAdd = new ModelNode();
        final ModelNode subModel = context.getSubModel();
        PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(OP_ADDR)).getLastElement());
        subsystemAdd.get(OP).set(ADD);
        subsystemAdd.get(OP_ADDR).set(rootAddress.toModelNode());

        if(subModel.hasDefined(DataGridConstants.CONNECTOR)) {
            subsystemAdd.get(DataGridConstants.CONNECTOR).set(subModel.get(DataGridConstants.CONNECTOR));
        }
        if(subModel.hasDefined(DataGridConstants.TOPOLOGY_STATE_TRANSFER)) {
            subsystemAdd.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER).set(subModel.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER));
        }

        final ModelNode result = new ModelNode();
        result.add(subsystemAdd);
        resultHandler.handleResultFragment(Util.NO_LOCATION, result);
        resultHandler.handleResultComplete();
        return new BasicOperationResult();
    }
}
