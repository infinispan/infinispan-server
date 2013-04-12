package org.infinispan.server.endpoint.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.infinispan.server.endpoint.Constants;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * The root resource of the Infinispan subsystem.
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class EndpointSubsystemRootResource extends SimpleResourceDefinition {

    private final boolean runtimeRegistration;

    public EndpointSubsystemRootResource(boolean runtimeRegistration) {
        super(PathElement.pathElement(SUBSYSTEM, Constants.SUBSYSTEM_NAME),
                EndpointExtension.getResourceDescriptionResolver(Constants.SUBSYSTEM_NAME),
                EndpointSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
        this.runtimeRegistration = runtimeRegistration;
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(new HotRodConnectorResource(isRuntimeRegistration()));
        resourceRegistration.registerSubModel(new MemcachedConnectorResource(isRuntimeRegistration()));
        resourceRegistration.registerSubModel(new RestConnectorResource(isRuntimeRegistration()));
    }

    public boolean isRuntimeRegistration() {
        return runtimeRegistration;
    }
}
