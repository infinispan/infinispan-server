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

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.services.net.SocketBinding;
import org.jboss.datagrid.DataGridConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
class EndpointSubsystemAdd implements ModelAddOperationHandler {

    private final ServiceName serviceName;

    EndpointSubsystemAdd(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {

        final ModelNode compensatingOperation = Util.getResourceRemoveOperation(operation.require(OP_ADDR));

        // Populate subModel
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();
        if (operation.hasDefined(DataGridConstants.CONNECTOR)) {
            subModel.get(DataGridConstants.CONNECTOR).set(operation.get(DataGridConstants.CONNECTOR));
        }
        if (operation.hasDefined(DataGridConstants.TOPOLOGY_STATE_TRANSFER)) {
            subModel.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER).set(operation.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER));
        }

        if (context.getRuntimeContext() != null) {
            context.getRuntimeContext().setRuntimeTask(new RuntimeTask() {
                @Override
                public void execute(RuntimeTaskContext context) throws OperationFailedException {
                    // Create the service
                    final EndpointService service = new EndpointService(operation);

                    // Add and install the service
                    ServiceBuilder<?> builder = context.getServiceTarget().addService(serviceName, service);
                    builder.addDependency(
                            ServiceBuilder.DependencyType.REQUIRED,
                            DataGridConstants.SN_CACHEMANAGER,
                            EmbeddedCacheManager.class,
                            service.getCacheManager());
                    
                    for (final String socketBinding: service.getRequiredSocketBindingNames()) {
                        final ServiceName socketName = SocketBinding.JBOSS_BINDING_NAME.append(socketBinding);
                        builder.addDependency(socketName, SocketBinding.class, service.getSocketBinding(socketBinding));
                    }
                    
                    builder.install();

                    resultHandler.handleResultComplete();
                }
            });
        } else {
            resultHandler.handleResultComplete();
        }
        return new BasicOperationResult(compensatingOperation);
    }
}
