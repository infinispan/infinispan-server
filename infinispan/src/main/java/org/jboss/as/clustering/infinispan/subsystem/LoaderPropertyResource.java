package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * @author Tristan Tarrant
 */
public class LoaderPropertyResource extends SimpleResourceDefinition {

    private static final PathElement LOADER_PROPERTY_PATH = PathElement.pathElement(ModelKeys.PROPERTY);

    // attributes
    static final SimpleAttributeDefinition VALUE =
            new SimpleAttributeDefinitionBuilder("value", ModelType.STRING, false)
                    .setXmlName("value")
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    public LoaderPropertyResource() {
        super(LOADER_PROPERTY_PATH,
                InfinispanExtension.getResourceDescriptionResolver(ModelKeys.PROPERTY),
                CacheConfigOperationHandlers.LOADER_PROPERTY_ADD,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);

        // do we need a special handler here?
        final OperationStepHandler writeHandler = new ReloadRequiredWriteAttributeHandler(VALUE);
        resourceRegistration.registerReadWriteAttribute(VALUE, null, writeHandler);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
    }
}
