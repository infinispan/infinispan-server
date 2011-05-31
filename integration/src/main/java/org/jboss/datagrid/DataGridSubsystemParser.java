/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.datagrid;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * The messaging subsystem domain parser
 *
 * @author scott.stark@jboss.org
 * @author Emanuel Muckenhuber
 */
class DataGridSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private final String subsystemName;
    private final String namespaceUri;

    DataGridSubsystemParser(String subsystemName, String namespaceUri) {
        this.subsystemName = subsystemName;
        this.namespaceUri = namespaceUri;
    }

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(ADD);
        operation.get(OP_ADDR).add(SUBSYSTEM, subsystemName);
        list.add(operation);

        // Handle elements
        String localName = null;
        do {
            int tag = reader.nextTag();
            if (tag == XMLStreamConstants.START_ELEMENT) {
                localName = reader.getLocalName();
                if (DataGridConstants.CONFIG_PATH.equals(localName)) {
                    final ModelNode path = parseConfigPath(reader);
                    operation.get(DataGridConstants.CONFIG_PATH).set(path);
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        } while (reader.hasNext() && !localName.equals("subsystem"));
    }

    static ModelNode parseConfigPath(XMLExtendedStreamReader reader) throws XMLStreamException {
        final ModelNode configPath = new ModelNode();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String localName = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            if (DataGridConstants.RELATIVE_TO.equals(localName)) {
                configPath.get(RELATIVE_TO).set(value);
            } else if (DataGridConstants.PATH.equals(localName)) {
                configPath.get(PATH).set(value);
            } else {
                throw unexpectedAttribute(reader, i);
            }
        }
        requireNoContent(reader);
        return configPath;
    }

    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(namespaceUri, false);
        final ModelNode node = context.getModelNode();
        if (has(node, DataGridConstants.CONFIG_PATH)) {
            writeConfigPath(writer, node);
        }
        writer.writeEndElement();
    }

    static void writeConfigPath(final XMLExtendedStreamWriter writer, ModelNode node) throws XMLStreamException {
        if(node.has(DataGridConstants.CONFIG_PATH)) {
            node = node.get(DataGridConstants.CONFIG_PATH);
            final String path = node.has(PATH) ? node.get(PATH).asString() : null;
            final String relativeTo = node.has(RELATIVE_TO) ? node.get(RELATIVE_TO).asString() : null;
            if(path != null || relativeTo != null) {
                writer.writeEmptyElement(DataGridConstants.CONFIG_PATH);
                if(path != null) {
                    writer.writeAttribute(PATH, path);
                }
                if(relativeTo != null) {
                    writer.writeAttribute(RELATIVE_TO, relativeTo);
                }
            }
        }
    }

    private boolean has(ModelNode node, String name) {
        return node.has(name) && node.get(name).isDefined();
    }
}
