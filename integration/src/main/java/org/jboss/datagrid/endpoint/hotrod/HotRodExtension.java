package org.jboss.datagrid.endpoint.hotrod;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.datagrid.DataGridNamespaces;
import org.jboss.datagrid.endpoint.ServerSubsystemParser;
import org.jboss.logging.Logger;

public class HotRodExtension implements Extension {

    private static final Logger log = Logger.getLogger(HotRodExtension.class.getPackage().getName());

    static final String SUBSYSTEM_NAME = "hotrod";

    private static final ServerSubsystemParser PARSER =
        new ServerSubsystemParser(SUBSYSTEM_NAME, DataGridNamespaces.HOTROD_1_0);

    @Override
    public void initialize(ExtensionContext context) {
        log.info("XXXXXXXXXXXXXXXXX");
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        final ModelNodeRegistration registration = subsystem.registerSubsystemModel(HotRodSubsystemProviders.SUBSYSTEM);
        registration.registerOperationHandler(ADD, HotRodSubsystemAdd.INSTANCE, HotRodSubsystemProviders.SUBSYSTEM_ADD, false);
        registration.registerOperationHandler(DESCRIBE, HotRodSubsystemDescribe.INSTANCE, HotRodSubsystemProviders.SUBSYSTEM_DESCRIBE, false, OperationEntry.EntryType.PRIVATE);

        subsystem.registerXMLElementWriter(PARSER);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        log.info("YYYYYYYYYYYYYYYYYYY");
        context.setSubsystemXmlMapping(DataGridNamespaces.HOTROD_1_0, PARSER);
    }

}