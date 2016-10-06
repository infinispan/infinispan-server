package org.jboss.as.clustering.infinispan.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

import com.jboss.datagrid.server.common.GenericSubsystemDescribeHandler;

/**
 * The root resource of the Infinispan subsystem.
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class InfinispanSubsystemRootResource extends SimpleResourceDefinition {

    private final boolean runtimeRegistration;

    public InfinispanSubsystemRootResource(boolean runtimeRegistration) {
        super(PathElement.pathElement(SUBSYSTEM, InfinispanExtension.SUBSYSTEM_NAME),
                InfinispanExtension.getResourceDescriptionResolver(),
                InfinispanSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
        this.runtimeRegistration = runtimeRegistration;
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION.getName(), GenericSubsystemDescribeHandler.INSTANCE, GenericSubsystemDescribeHandler.DEFINITION.getDescriptionProvider());
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        // resourceRegistration.registerReadWriteAttribute(DEFAULT_STACK, null, SubsystemWriteAttributeHandler.INSTANCE);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(new CacheContainerResource(isRuntimeRegistration()));
    }

    public boolean isRuntimeRegistration() {
        return runtimeRegistration;
    }
}
