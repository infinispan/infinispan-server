/**
 *
 */
package org.jboss.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;


/**
 * Detyped descriptions of Messaging subsystem resources and operations.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class EndpointDescriptions {

    private final String subsystemName;
    private final String resourceName;

    public EndpointDescriptions(String subsystemName, Package pkg) {
        this.subsystemName = subsystemName;
        resourceName = pkg.getName() + ".LocalDescriptions";
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return ResourceBundle.getBundle(resourceName, locale);
    }

    public ModelNode getRootResource(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        node.get(DESCRIPTION).set(bundle.getString(subsystemName));

        node.get(ATTRIBUTES, EndpointAttributes.CONFIG_PATH).set(
                getPathDescription(EndpointAttributes.CONFIG_PATH, bundle));

        return node;
    }

    public ModelNode getSubsystemAdd(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(ADD);
        node.get(DESCRIPTION).set(bundle.getString(subsystemName + ".add"));
        node.get(REQUEST_PROPERTIES, EndpointAttributes.CONFIG_PATH).set(
                getPathDescription(EndpointAttributes.CONFIG_PATH, bundle));

        return node;
    }

    public ModelNode getSubsystemRemove(@SuppressWarnings("unused") Locale locale) {
        return new ModelNode();
    }

    public ModelNode getSubsystemDescribe(@SuppressWarnings("unused") Locale locale) {
        return new ModelNode();
    }

    private static ModelNode getPathDescription(final String description, final ResourceBundle bundle) {
        final ModelNode node = new ModelNode();

        node.get(TYPE).set(ModelType.OBJECT);
        node.get(DESCRIPTION).set(bundle.getString(description));
        node.get(ATTRIBUTES, PATH, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, PATH, DESCRIPTION).set(bundle.getString("path.path"));
        node.get(ATTRIBUTES, PATH, REQUIRED).set(false);
        node.get(ATTRIBUTES, RELATIVE_TO, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, RELATIVE_TO, DESCRIPTION).set(bundle.getString("path.relative-to"));
        node.get(ATTRIBUTES, RELATIVE_TO, REQUIRED).set(false);

        return node;
    }
}
