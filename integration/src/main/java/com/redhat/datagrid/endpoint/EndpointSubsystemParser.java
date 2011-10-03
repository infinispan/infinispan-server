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
package com.redhat.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.redhat.datagrid.DataGridConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * The parser for the data grid endpoint subsystem configuration.
 *
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
class EndpointSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private final String subsystemName;
    private final String namespaceUri;

    EndpointSubsystemParser(String subsystemName, String namespaceUri) {
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
        String elemName = null;
        do {
            int tag = reader.nextTag();
            if (tag != XMLStreamConstants.START_ELEMENT) {
                continue;
            }

            elemName = reader.getLocalName();
            if (ModelKeys.CONNECTOR.equals(elemName)) {
                readConnector(reader, operation);
            } else if (ModelKeys.TOPOLOGY_STATE_TRANSFER.equals(elemName)) {
                readTopologyStateTransfer(reader, operation);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        } while (reader.hasNext() && !elemName.equals(DataGridConstants.SUBSYSTEM));
    }

    private void readConnector(final XMLExtendedStreamReader reader,
            final ModelNode operation) {

        // Handle required attributes first.
        String protocol = reader.getAttributeValue(null, ModelKeys.PROTOCOL);
        if (protocol == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(ModelKeys.PROTOCOL));
        }

        String socketBinding = reader.getAttributeValue(null, ModelKeys.SOCKET_BINDING);
        if (socketBinding == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(ModelKeys.SOCKET_BINDING));
        }

        ModelNode connector = operation.get(ModelKeys.CONNECTOR).get(protocol);
        connector.get(ModelKeys.SOCKET_BINDING).set(socketBinding);

        int attrCnt = reader.getAttributeCount();
        for (int i = 0; i < attrCnt; i ++) {
            String attrName = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            if (ModelKeys.CACHE_CONTAINER.equals(attrName)) {
                connector.get(ModelKeys.CACHE_CONTAINER).set(attrValue);
            } else if (ModelKeys.WORKER_THREADS.equals(attrName)) {
                connector.get(ModelKeys.WORKER_THREADS).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.IDLE_TIMEOUT.equals(attrName)) {
                connector.get(ModelKeys.IDLE_TIMEOUT).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.TCP_NODELAY.equals(attrName)) {
                connector.get(ModelKeys.TCP_NODELAY).set(Boolean.parseBoolean(attrValue));
            } else if (ModelKeys.SEND_BUFFER_SIZE.equals(attrName)) {
                connector.get(ModelKeys.SEND_BUFFER_SIZE).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.RECEIVE_BUFFER_SIZE.equals(attrName)) {
                connector.get(ModelKeys.RECEIVE_BUFFER_SIZE).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.PROTOCOL.equals(attrName)) {
                // Handled already
            } else if (ModelKeys.SOCKET_BINDING.equals(attrName)) {
                // Handled already
            } else {
                ParseUtils.unexpectedAttribute(reader, i);
            }
        }
    }

    private void readTopologyStateTransfer(final XMLExtendedStreamReader reader, final ModelNode operation) {
        ModelNode topologyStateTransfer = operation.get(ModelKeys.TOPOLOGY_STATE_TRANSFER);

        int attrCnt = reader.getAttributeCount();
        for (int i = 0; i < attrCnt; i ++) {
            String attrName = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            if (ModelKeys.LOCK_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.LOCK_TIMEOUT).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.REPLICATION_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.REPLICATION_TIMEOUT).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.UPDATE_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.UPDATE_TIMEOUT).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.EXTERNAL_HOST.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.EXTERNAL_HOST).set(attrValue);
            } else if (ModelKeys.EXTERNAL_PORT.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.EXTERNAL_PORT).set(Integer.parseInt(attrValue));
            } else if (ModelKeys.LAZY_RETRIEVAL.equals(attrName)) {
                topologyStateTransfer.get(ModelKeys.LAZY_RETRIEVAL).set(Boolean.parseBoolean(attrValue));
            } else {
                ParseUtils.unexpectedAttribute(reader, i);
            }
        }
    }

    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(namespaceUri, false);
        final ModelNode node = context.getModelNode();
        writeConnectors(writer, node);
        writeTopologyStateTransfer(writer, node);
        writer.writeEndElement();
    }

    private void writeConnectors(final XMLExtendedStreamWriter writer,
            final ModelNode node) throws XMLStreamException {
        if (node.hasDefined(ModelKeys.CONNECTOR)) {
            ModelNode connectors = node.get(ModelKeys.CONNECTOR);
            for (Property property: connectors.asPropertyList()) {
                ModelNode connector = property.getValue();
                writer.writeEmptyElement(ModelKeys.CONNECTOR);
                writer.writeAttribute(ModelKeys.PROTOCOL, property.getName());
                writer.writeAttribute(ModelKeys.SOCKET_BINDING, connector.get(ModelKeys.SOCKET_BINDING).asString());
                if (connector.hasDefined(ModelKeys.WORKER_THREADS)) {
                    writer.writeAttribute(ModelKeys.WORKER_THREADS, connector.get(ModelKeys.WORKER_THREADS).asString());
                }
                if (connector.hasDefined(ModelKeys.IDLE_TIMEOUT)) {
                    writer.writeAttribute(ModelKeys.IDLE_TIMEOUT, connector.get(ModelKeys.IDLE_TIMEOUT).asString());
                }
                if (connector.hasDefined(ModelKeys.TCP_NODELAY)) {
                    writer.writeAttribute(ModelKeys.TCP_NODELAY, connector.get(ModelKeys.TCP_NODELAY).asString());
                }
                if (connector.hasDefined(ModelKeys.SEND_BUFFER_SIZE)) {
                    writer.writeAttribute(ModelKeys.SEND_BUFFER_SIZE, connector.get(ModelKeys.SEND_BUFFER_SIZE).asString());
                }
                if (connector.hasDefined(ModelKeys.RECEIVE_BUFFER_SIZE)) {
                    writer.writeAttribute(ModelKeys.RECEIVE_BUFFER_SIZE, connector.get(ModelKeys.RECEIVE_BUFFER_SIZE).asString());
                }
            }
        }
    }

    private void writeTopologyStateTransfer(final XMLExtendedStreamWriter writer,
            final ModelNode node) throws XMLStreamException {
        if (node.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER)) {
            ModelNode topologyStateTransfer = node.get(ModelKeys.TOPOLOGY_STATE_TRANSFER);
            writer.writeEmptyElement(ModelKeys.TOPOLOGY_STATE_TRANSFER);
            if (topologyStateTransfer.hasDefined(ModelKeys.LOCK_TIMEOUT)) {
                writer.writeAttribute(ModelKeys.LOCK_TIMEOUT, topologyStateTransfer.get(ModelKeys.LOCK_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(ModelKeys.REPLICATION_TIMEOUT)) {
                writer.writeAttribute(ModelKeys.REPLICATION_TIMEOUT, topologyStateTransfer.get(ModelKeys.REPLICATION_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(ModelKeys.UPDATE_TIMEOUT)) {
                writer.writeAttribute(ModelKeys.UPDATE_TIMEOUT, topologyStateTransfer.get(ModelKeys.UPDATE_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(ModelKeys.EXTERNAL_HOST)) {
                writer.writeAttribute(ModelKeys.EXTERNAL_HOST, topologyStateTransfer.get(ModelKeys.EXTERNAL_HOST).asString());
            }
            if (topologyStateTransfer.hasDefined(ModelKeys.EXTERNAL_PORT)) {
                writer.writeAttribute(ModelKeys.EXTERNAL_PORT, topologyStateTransfer.get(ModelKeys.EXTERNAL_PORT).asString());
            }
            if (topologyStateTransfer.hasDefined(ModelKeys.LAZY_RETRIEVAL)) {
                writer.writeAttribute(ModelKeys.LAZY_RETRIEVAL, topologyStateTransfer.get(ModelKeys.LAZY_RETRIEVAL).asString());
            }
        }
    }
}
