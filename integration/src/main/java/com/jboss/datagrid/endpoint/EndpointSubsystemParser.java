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
package com.jboss.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

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
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 *
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

      final ModelNode address = new ModelNode();
      address.add(SUBSYSTEM, subsystemName);
      address.protect();

      final ModelNode subsystem = new ModelNode();
      subsystem.get(OP).set(ADD);
      subsystem.get(OP_ADDR).set(address);
      list.add(subsystem);

      // Handle elements
      ModelNode op = null;
      String elemName = null;
      do {
         int tag = reader.nextTag();
         if (tag != XMLStreamConstants.START_ELEMENT) {
            continue;
         }

         elemName = reader.getLocalName();
         if (ModelKeys.HOTROD_CONNECTOR.equals(elemName)) {
            op = readServerConnector(reader, ModelKeys.HOTROD_CONNECTOR, address);
            list.add(op);
         } else if (ModelKeys.MEMCACHED_CONNECTOR.equals(elemName)) {
            op = readServerConnector(reader, ModelKeys.MEMCACHED_CONNECTOR, address);
            list.add(op);
         } else if (ModelKeys.REST_CONNECTOR.equals(elemName)) {
            op = readRestConnector(reader, ModelKeys.REST_CONNECTOR, address);
            list.add(op);
         } else if (ModelKeys.TOPOLOGY_STATE_TRANSFER.equals(elemName)) {
            readTopologyStateTransfer(reader, op);
         } else {
            throw ParseUtils.unexpectedElement(reader);
         }
      } while (reader.hasNext() && !elemName.equals(SUBSYSTEM));
   }

   /**
    * Handle parsing of the hotrod and memcached connector configuration
    *
    * @param reader
    * @param connector
    */
   private ModelNode readServerConnector(final XMLExtendedStreamReader reader, final String name, ModelNode parentAddress) {
      final ModelNode op = new ModelNode();
      op.get(OP).set(ADD);

      String providedName = name;
      // Handle required attributes first.
      String socketBinding = reader.getAttributeValue(null, ModelKeys.SOCKET_BINDING);
      if (socketBinding == null) {
         ParseUtils.missingRequired(reader, Collections.singleton(ModelKeys.SOCKET_BINDING));
      }

      op.get(ModelKeys.SOCKET_BINDING).set(socketBinding);

      int attrCnt = reader.getAttributeCount();
      for (int i = 0; i < attrCnt; i++) {
         String attrName = reader.getAttributeLocalName(i);
         String attrValue = reader.getAttributeValue(i);
         if (ModelKeys.NAME.equals(attrName)) {
            op.get(ModelKeys.NAME).set(attrValue);
            providedName = attrValue;
         } else if (ModelKeys.CACHE_CONTAINER.equals(attrName)) {
            op.get(ModelKeys.CACHE_CONTAINER).set(attrValue);
         } else if (ModelKeys.WORKER_THREADS.equals(attrName)) {
            op.get(ModelKeys.WORKER_THREADS).set(Integer.parseInt(attrValue));
         } else if (ModelKeys.IDLE_TIMEOUT.equals(attrName)) {
            op.get(ModelKeys.IDLE_TIMEOUT).set(Integer.parseInt(attrValue));
         } else if (ModelKeys.TCP_NODELAY.equals(attrName)) {
            op.get(ModelKeys.TCP_NODELAY).set(Boolean.parseBoolean(attrValue));
         } else if (ModelKeys.SEND_BUFFER_SIZE.equals(attrName)) {
            op.get(ModelKeys.SEND_BUFFER_SIZE).set(Integer.parseInt(attrValue));
         } else if (ModelKeys.RECEIVE_BUFFER_SIZE.equals(attrName)) {
            op.get(ModelKeys.RECEIVE_BUFFER_SIZE).set(Integer.parseInt(attrValue));
         } else if (ModelKeys.SOCKET_BINDING.equals(attrName)) {
            // Handled already
         } else {
            ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      final ModelNode address = parentAddress.clone();
      address.add(name, providedName);
      address.protect();
      op.get(OP_ADDR).set(address);

      return op;
   }

   /**
    * Handle parsing of the Rest connector configuration
    *
    * @param reader
    * @param connector
    * @throws XMLStreamException
    */
   private ModelNode readRestConnector(final XMLExtendedStreamReader reader, final String name, ModelNode parentAddress) throws XMLStreamException {
      final ModelNode op = new ModelNode();
      op.get(OP).set(ADD);

      final ModelNode address = parentAddress.clone();
      address.add(name, name);
      address.protect();
      op.get(OP_ADDR).set(address);

      int attrCnt = reader.getAttributeCount();
      for (int i = 0; i < attrCnt; i++) {
         String attrName = reader.getAttributeLocalName(i);
         String attrValue = reader.getAttributeValue(i);
         if (ModelKeys.NAME.equals(attrName)) {
            EndpointAttributeDefinitions.NAME.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.CACHE_CONTAINER.equals(attrName)) {
            EndpointAttributeDefinitions.CACHE_CONTAINER.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.VIRTUAL_SERVER.equals(attrName)) {
            EndpointAttributeDefinitions.VIRTUAL_SERVER.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.CONTEXT_PATH.equals(attrName)) {
            EndpointAttributeDefinitions.CONTEXT_PATH.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.SECURITY_DOMAIN.equals(attrName)) {
            EndpointAttributeDefinitions.SECURITY_DOMAIN.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.AUTH_METHOD.equals(attrName)) {
            EndpointAttributeDefinitions.AUTH_METHOD.parseAndSetParameter(attrValue, op, reader);
         } else if (ModelKeys.SECURITY_MODE.equals(attrName)) {
            EndpointAttributeDefinitions.SECURITY_MODE.parseAndSetParameter(attrValue, op, reader);
         } else {
            ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      return op;
   }

   private void readTopologyStateTransfer(final XMLExtendedStreamReader reader, final ModelNode operation) {
      ModelNode topologyStateTransfer = operation.get(ModelKeys.TOPOLOGY_STATE_TRANSFER);

      int attrCnt = reader.getAttributeCount();
      for (int i = 0; i < attrCnt; i++) {
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
      writer.writeEndElement();
   }

   private void writeConnectors(final XMLExtendedStreamWriter writer, final ModelNode node) throws XMLStreamException {
      for (String connectorType : ModelKeys.CONNECTORS) {
         if (node.hasDefined(connectorType)) {
            ModelNode connectors = node.get(connectorType);
            for (Property property : connectors.asPropertyList()) {
               ModelNode connector = property.getValue();
               writer.writeEmptyElement(connectorType);
               for (String connectorAttribute : ModelKeys.CONNECTOR_ATTRIBUTES) {
                  if (connector.hasDefined(connectorAttribute)) {
                     writer.writeAttribute(connectorAttribute, connector.get(connectorAttribute).asString());
                  }
               }
               writeTopologyStateTransfer(writer, connector);
            }
         }
      }
   }

   private void writeTopologyStateTransfer(final XMLExtendedStreamWriter writer, final ModelNode node) throws XMLStreamException {
      if (node.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER)) {
         ModelNode topologyStateTransfer = node.get(ModelKeys.TOPOLOGY_STATE_TRANSFER);
         writer.writeEmptyElement(ModelKeys.TOPOLOGY_STATE_TRANSFER);
         for (String connectorAttribute : ModelKeys.TOPOLOGY_ATTRIBUTES) {
            if (topologyStateTransfer.hasDefined(connectorAttribute)) {
               writer.writeAttribute(connectorAttribute, topologyStateTransfer.get(connectorAttribute).asString());
            }
         }
      }
   }
}
