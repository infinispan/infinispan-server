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
package org.jboss.datagrid;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.List;
import java.util.Locale;

import javax.management.MBeanServer;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.services.path.RelativePathService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Emanuel Muckenhuber
 */
abstract class DataGridSubsystemAdd extends AbstractAddStepHandler implements DescriptionProvider {

    private static final String DEFAULT_RELATIVE_TO = "jboss.server.base.dir";

    static ModelNode createOperation(ModelNode address, ModelNode existing) {
        ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
        populate(existing, operation);
        return operation;
    }
    
    private static void populate(ModelNode source, ModelNode target) {
        target.get(DataGridConstants.CONFIG_PATH).set(source.require(DataGridConstants.CONFIG_PATH));
    }
    
    private final ServiceName serviceName;
    private final ServiceName pathBase;
    private final String defaultPath;

    DataGridSubsystemAdd(ServiceName serviceName, String defaultConfigFileName) {
        this.serviceName = serviceName;
        pathBase = serviceName.append("paths");
        defaultPath = "configuration/" + defaultConfigFileName;
    }

    @Override
    protected boolean requiresRuntimeVerification() {
        return false;
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        // TODO Generate localized model description.
        return new ModelNode();
    }

    @Override
    protected void populateModel(ModelNode source, ModelNode target)
            throws OperationFailedException {
        populate(source, target);
    }

    @Override
    protected void performRuntime(OperationContext context,
            ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        final ServiceTarget serviceTarget = context.getServiceTarget();
        // Create the service
        final DataGridService<?> service = createService();

        // Add the service
        final ServiceBuilder<?> serviceBuilder = serviceTarget.addService(serviceName, service)
                .addDependency(DependencyType.REQUIRED, ServiceName.JBOSS.append("mbean", "server"), MBeanServer.class, service.getMBeanServer());

        // Create path services
        serviceBuilder.addDependency(createConfigPathService(operation, serviceTarget),
                String.class, service.getConfigPathInjector());

        buildService(service, serviceBuilder);

        // Install the service
        serviceBuilder.install();
    }

    protected abstract DataGridService<?> createService();

    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        // Do nothing by default
    }

    private ServiceName createConfigPathService(final ModelNode operation, final ServiceTarget serviceTarget) {
        ModelNode path = operation.get(DataGridConstants.CONFIG_PATH);
        final ServiceName serviceName = pathBase.append(DataGridConstants.CONFIG_PATH);
        final String relativeTo = path.hasDefined(RELATIVE_TO) ? path.get(RELATIVE_TO).asString() : DEFAULT_RELATIVE_TO;
        final String pathName = path.hasDefined(PATH) ? path.get(PATH).asString() : defaultPath;
        RelativePathService.addService(serviceName, pathName, relativeTo, serviceTarget);
        return serviceName;
    }
}
