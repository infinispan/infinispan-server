package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.transaction.LockingMode;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.jboss.datagrid.server.common.OperationDefinition;
import com.jboss.datagrid.server.common.SimpleOperationDefinitionBuilder;

/**
 * Resource description for the addressable resource /subsystem=infinispan/cache-container=X/cache=Y/transaction=TRANSACTION
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 * @author Tristan Tarrant
 */
public class TransactionResource extends SimpleResourceDefinition {

    private static final PathElement TRANSACTION_PATH = PathElement.pathElement(ModelKeys.TRANSACTION, ModelKeys.TRANSACTION_NAME);

    // attributes
    // cache mode required, txn mode not
    static final SimpleAttributeDefinition LOCKING =
            new SimpleAttributeDefinitionBuilder(ModelKeys.LOCKING, ModelType.STRING, true)
                    .setXmlName(Attribute.LOCKING.getLocalName())
                    .setAllowExpression(false)
                    .setValidator(new EnumValidator<LockingMode>(LockingMode.class, true, false))
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode().set(LockingMode.OPTIMISTIC.name()))
                    .build();
    static final SimpleAttributeDefinition MODE =
            new SimpleAttributeDefinitionBuilder(ModelKeys.MODE, ModelType.STRING, true)
                    .setXmlName(Attribute.MODE.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new EnumValidator<TransactionMode>(TransactionMode.class, true, true))
                    .setDefaultValue(new ModelNode().set(TransactionMode.NONE.name()))
                    .build();
    static final SimpleAttributeDefinition STOP_TIMEOUT =
            new SimpleAttributeDefinitionBuilder(ModelKeys.STOP_TIMEOUT, ModelType.LONG, true)
                    .setXmlName(Attribute.STOP_TIMEOUT.getLocalName())
                    .setMeasurementUnit(MeasurementUnit.MILLISECONDS)
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode().set(30000))
                    .build();

    static final AttributeDefinition[] TRANSACTION_ATTRIBUTES = {MODE, STOP_TIMEOUT, LOCKING};

    // operation parameters
    static final SimpleAttributeDefinition TX_INTERNAL_ID =
            new SimpleAttributeDefinitionBuilder(ModelKeys.TX_INTERNAL_ID, ModelType.LONG, true)
                .setXmlName(ModelKeys.TX_INTERNAL_ID)
                .setAllowExpression(false)
                .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                .build();

    // operations
    static final OperationDefinition RESET_TX_STATISTICS =
            new SimpleOperationDefinitionBuilder("reset-transaction-statistics", InfinispanExtension.getResourceDescriptionResolver("transaction"))
                .build();
    static final OperationDefinition LIST_IN_DOUBT_TRANSACTIONS =
            new SimpleOperationDefinitionBuilder("list-in-doubt-transactions", InfinispanExtension.getResourceDescriptionResolver("transaction"))
                .build();
    static final OperationDefinition TRANSACTION_FORCE_COMMIT =
            new SimpleOperationDefinitionBuilder("force-commit-transaction", InfinispanExtension.getResourceDescriptionResolver("transaction.recovery"))
                .addParameter(TX_INTERNAL_ID)
                .build();
    static final OperationDefinition TRANSACTION_FORCE_ROLLBACK =
            new SimpleOperationDefinitionBuilder("force-rollback-transaction", InfinispanExtension.getResourceDescriptionResolver("transaction.recovery"))
                .addParameter(TX_INTERNAL_ID)
                .build();
    static final OperationDefinition TRANSACTION_FORGET =
            new SimpleOperationDefinitionBuilder("forget-transaction", InfinispanExtension.getResourceDescriptionResolver("transaction.recovery"))
                .addParameter(TX_INTERNAL_ID)
                .build();

    private final boolean runtimeRegistration;

    public TransactionResource(boolean runtimeRegistration) {
        super(TRANSACTION_PATH,
                InfinispanExtension.getResourceDescriptionResolver(ModelKeys.TRANSACTION),
                CacheConfigOperationHandlers.TRANSACTION_ADD,
                ReloadRequiredRemoveStepHandler.INSTANCE);
        this.runtimeRegistration = runtimeRegistration;
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);

        // check that we don't need a special handler here?
        final OperationStepHandler writeHandler = new ReloadRequiredWriteAttributeHandler(TRANSACTION_ATTRIBUTES);
        for (AttributeDefinition attr : TRANSACTION_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, writeHandler);
        }
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        if (runtimeRegistration) {
            resourceRegistration.registerOperationHandler(TransactionResource.RESET_TX_STATISTICS.getName(), CacheCommands.ResetTxStatisticsCommand.INSTANCE, TransactionResource.RESET_TX_STATISTICS.getDescriptionProvider());
            resourceRegistration.registerOperationHandler(TransactionResource.LIST_IN_DOUBT_TRANSACTIONS.getName(), CacheCommands.TransactionListInDoubtCommand.INSTANCE, TransactionResource.LIST_IN_DOUBT_TRANSACTIONS.getDescriptionProvider());
            resourceRegistration.registerOperationHandler(TransactionResource.TRANSACTION_FORCE_COMMIT.getName(), CacheCommands.TransactionForceCommitCommand.INSTANCE, TransactionResource.TRANSACTION_FORCE_COMMIT.getDescriptionProvider());
            resourceRegistration.registerOperationHandler(TransactionResource.TRANSACTION_FORCE_ROLLBACK.getName(), CacheCommands.TransactionForceRollbackCommand.INSTANCE, TransactionResource.TRANSACTION_FORCE_ROLLBACK.getDescriptionProvider());
            resourceRegistration.registerOperationHandler(TransactionResource.TRANSACTION_FORGET.getName(), CacheCommands.TransactionForgetCommand.INSTANCE, TransactionResource.TRANSACTION_FORGET.getDescriptionProvider());

        }
    }

    public boolean isRuntimeRegistration() {
        return runtimeRegistration;
    }
}
