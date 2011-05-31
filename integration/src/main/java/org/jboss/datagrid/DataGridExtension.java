package org.jboss.datagrid;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;

public abstract class DataGridExtension implements Extension {

    private final String subsystemName;
    private final String namespaceUri;
    private final DataGridSubsystemParser parser;
    private final DataGridSubsystemProviders providers;
    private final DataGridSubsystemAdd subsystemAdd;
    private final DataGridSubsystemDescribe subsystemDescribe;

    public DataGridExtension(ServiceName serviceName, String namespaceUri, String defaultConfigFileName) {
        subsystemName = serviceName.getSimpleName();
        this.namespaceUri = namespaceUri;
        parser = new DataGridSubsystemParser(subsystemName, namespaceUri);
        providers = new DataGridSubsystemProviders(subsystemName, getClass());
        subsystemAdd = new DataGridSubsystemAdd(serviceName, defaultConfigFileName) {
            @Override
            protected DataGridService<?> createService() {
                return DataGridExtension.this.createService();
            }

            @Override
            protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
                super.buildService(service, builder);
                DataGridExtension.this.buildService(service, builder);
            }
        };
        subsystemDescribe = new DataGridSubsystemDescribe();
    }

    protected abstract DataGridService<?> createService();

    protected void buildService(DataGridService<?> service, ServiceBuilder<?> builder) {
        // Do nothing by default.
    }

    @Override
    public final void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(subsystemName);
        final ModelNodeRegistration registration = subsystem.registerSubsystemModel(providers.subsystem());
        registration.registerOperationHandler(ADD, subsystemAdd, providers.subsystemAdd(), false);
        registration.registerOperationHandler(DESCRIBE, subsystemDescribe, providers.subsystemDescribe(), false, OperationEntry.EntryType.PRIVATE);

        subsystem.registerXMLElementWriter(parser);
    }

    @Override
    public final void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(namespaceUri, parser);
    }
}