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
package org.jboss.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.datagrid.DataGridConstants;
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
            if (DataGridConstants.CONNECTOR.equals(elemName)) {
                readConnector(reader, operation);
            } else if (DataGridConstants.TOPOLOGY_STATE_TRANSFER.equals(elemName)) {
                readTopologyStateTransfer(reader, operation);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        } while (reader.hasNext() && !elemName.equals(DataGridConstants.SUBSYSTEM));
    }

    private void readConnector(final XMLExtendedStreamReader reader,
            final ModelNode operation) {
        
        // Handle required attributes first.
        String protocol = reader.getAttributeValue(null, DataGridConstants.PROTOCOL);
        if (protocol == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(DataGridConstants.PROTOCOL));
        }
        
        String socketBinding = reader.getAttributeValue(null, DataGridConstants.SOCKET_BINDING);
        if (socketBinding == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(DataGridConstants.SOCKET_BINDING));
        }
        
        ModelNode connector = operation.get(DataGridConstants.CONNECTOR).get(protocol);
        connector.set(DataGridConstants.SOCKET_BINDING, socketBinding);
        
        int attrCnt = reader.getAttributeCount();
        for (int i = 0; i < attrCnt; i ++) {
            String attrName = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            if (DataGridConstants.CACHE_CONTAINER.equals(attrName)) {
                connector.set(DataGridConstants.CACHE_CONTAINER, attrValue);
            } else if (DataGridConstants.WORKER_THREADS.equals(attrName)) {
                connector.set(DataGridConstants.WORKER_THREADS, Integer.parseInt(attrValue));
            } else if (DataGridConstants.IDLE_TIMEOUT.equals(attrName)) {
                connector.set(DataGridConstants.IDLE_TIMEOUT, Integer.parseInt(attrValue));
            } else if (DataGridConstants.TCP_NODELAY.equals(attrName)) {
                connector.set(DataGridConstants.TCP_NODELAY, Boolean.parseBoolean(attrValue));
            } else if (DataGridConstants.SEND_BUFFER_SIZE.equals(attrName)) {
                connector.set(DataGridConstants.SEND_BUFFER_SIZE, Integer.parseInt(attrValue));
            } else if (DataGridConstants.RECEIVE_BUFFER_SIZE.equals(attrName)) {
                connector.set(DataGridConstants.RECEIVE_BUFFER_SIZE, Integer.parseInt(attrValue));
            } else if (DataGridConstants.PROTOCOL.equals(attrName)) {
                // Handled already
            } else if (DataGridConstants.SOCKET_BINDING.equals(attrName)) {
                // Handled already
            } else {
                ParseUtils.unexpectedAttribute(reader, i);
            }
        }
    }

    private void readTopologyStateTransfer(final XMLExtendedStreamReader reader, final ModelNode operation) {
        ModelNode topologyStateTransfer = operation.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER);
        
        int attrCnt = reader.getAttributeCount();
        for (int i = 0; i < attrCnt; i ++) {
            String attrName = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            if (DataGridConstants.LOCK_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.LOCK_TIMEOUT, Integer.parseInt(attrValue));
            } else if (DataGridConstants.REPLICATION_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.REPLICATION_TIMEOUT, Integer.parseInt(attrValue));
            } else if (DataGridConstants.UPDATE_TIMEOUT.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.UPDATE_TIMEOUT, Integer.parseInt(attrValue));
            } else if (DataGridConstants.EXTERNAL_HOST.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.EXTERNAL_HOST, attrValue);
            } else if (DataGridConstants.EXTERNAL_PORT.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.EXTERNAL_PORT, Integer.parseInt(attrValue));
            } else if (DataGridConstants.LAZY_RETRIEVAL.equals(attrName)) {
                topologyStateTransfer.set(DataGridConstants.LAZY_RETRIEVAL, Boolean.parseBoolean(attrValue));
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
        if (node.hasDefined(DataGridConstants.CONNECTOR)) {
            ModelNode connectors = node.get(DataGridConstants.CONNECTOR);
            for (Property property: connectors.asPropertyList()) {
                ModelNode connector = property.getValue();
                writer.writeEmptyElement(DataGridConstants.CONNECTOR);
                writer.writeAttribute(DataGridConstants.PROTOCOL, property.getName());
                writer.writeAttribute(DataGridConstants.SOCKET_BINDING, connector.get(DataGridConstants.SOCKET_BINDING).asString());
                if (connector.hasDefined(DataGridConstants.WORKER_THREADS)) {
                    writer.writeAttribute(DataGridConstants.WORKER_THREADS, connector.get(DataGridConstants.WORKER_THREADS).asString());
                }
                if (connector.hasDefined(DataGridConstants.IDLE_TIMEOUT)) {
                    writer.writeAttribute(DataGridConstants.IDLE_TIMEOUT, connector.get(DataGridConstants.IDLE_TIMEOUT).asString());
                }
                if (connector.hasDefined(DataGridConstants.TCP_NODELAY)) {
                    writer.writeAttribute(DataGridConstants.TCP_NODELAY, connector.get(DataGridConstants.TCP_NODELAY).asString());
                }
                if (connector.hasDefined(DataGridConstants.SEND_BUFFER_SIZE)) {
                    writer.writeAttribute(DataGridConstants.SEND_BUFFER_SIZE, connector.get(DataGridConstants.SEND_BUFFER_SIZE).asString());
                }
                if (connector.hasDefined(DataGridConstants.RECEIVE_BUFFER_SIZE)) {
                    writer.writeAttribute(DataGridConstants.RECEIVE_BUFFER_SIZE, connector.get(DataGridConstants.RECEIVE_BUFFER_SIZE).asString());
                }
            }
        }
    }

    private void writeTopologyStateTransfer(final XMLExtendedStreamWriter writer,
            final ModelNode node) throws XMLStreamException {
        if (node.hasDefined(DataGridConstants.TOPOLOGY_STATE_TRANSFER)) {
            ModelNode topologyStateTransfer = node.get(DataGridConstants.TOPOLOGY_STATE_TRANSFER);
            writer.writeEmptyElement(DataGridConstants.TOPOLOGY_STATE_TRANSFER);
            if (topologyStateTransfer.hasDefined(DataGridConstants.LOCK_TIMEOUT)) {
                writer.writeAttribute(DataGridConstants.LOCK_TIMEOUT, topologyStateTransfer.get(DataGridConstants.LOCK_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(DataGridConstants.REPLICATION_TIMEOUT)) {
                writer.writeAttribute(DataGridConstants.REPLICATION_TIMEOUT, topologyStateTransfer.get(DataGridConstants.REPLICATION_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(DataGridConstants.UPDATE_TIMEOUT)) {
                writer.writeAttribute(DataGridConstants.UPDATE_TIMEOUT, topologyStateTransfer.get(DataGridConstants.UPDATE_TIMEOUT).asString());
            }
            if (topologyStateTransfer.hasDefined(DataGridConstants.EXTERNAL_HOST)) {
                writer.writeAttribute(DataGridConstants.EXTERNAL_HOST, topologyStateTransfer.get(DataGridConstants.EXTERNAL_HOST).asString());
            }
            if (topologyStateTransfer.hasDefined(DataGridConstants.EXTERNAL_PORT)) {
                writer.writeAttribute(DataGridConstants.EXTERNAL_PORT, topologyStateTransfer.get(DataGridConstants.EXTERNAL_PORT).asString());
            }
            if (topologyStateTransfer.hasDefined(DataGridConstants.LAZY_RETRIEVAL)) {
                writer.writeAttribute(DataGridConstants.LAZY_RETRIEVAL, topologyStateTransfer.get(DataGridConstants.LAZY_RETRIEVAL).asString());
            }
        }
    }
}
