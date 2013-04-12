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
package org.infinispan.server.endpoint.subsystem;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * The XML writer for the endpoint subsystem configuration.
 *
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 *
 */
class EndpointSubsystemWriter implements XMLStreamConstants, XMLElementWriter<SubsystemMarshallingContext> {


   EndpointSubsystemWriter() {
   }

   @Override
   public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
      context.startSubsystemElement(Namespace.CURRENT.getUri(), false);
      final ModelNode node = context.getModelNode();
      writeConnectors(writer, node);
      writer.writeEndElement();
   }

   private void writeConnectors(final XMLExtendedStreamWriter writer, final ModelNode node) throws XMLStreamException {
      for(Property property : getConnectorsByType(node, ModelKeys.HOTROD_CONNECTOR)) {
         writeHotRodConnector(writer, property.getValue());
      }
      for(Property property : getConnectorsByType(node, ModelKeys.MEMCACHED_CONNECTOR)) {
         writeMemcachedConnector(writer, property.getValue());
      }
      for(Property property : getConnectorsByType(node, ModelKeys.REST_CONNECTOR)) {
         writeRestConnector(writer, property.getValue());
      }
   }

   private List<Property> getConnectorsByType(final ModelNode node, String connectorType) {
      if (node.hasDefined(connectorType)) {
         ModelNode connectors = node.get(connectorType);
         return connectors.asPropertyList();
      } else {
         return Collections.emptyList();
      }
   }

   private void writeHotRodConnector(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      writer.writeStartElement(Element.HOTROD_CONNECTOR.getLocalName());
      writeCommonConnector(writer, connector);
      writeProtocolServerConnector(writer, connector);
      writeTopologyStateTransfer(writer, connector);
      writeSecurity(writer, connector);
      writer.writeEndElement();
   }

   private void writeMemcachedConnector(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      writer.writeStartElement(Element.MEMCACHED_CONNECTOR.getLocalName());
      writeCommonConnector(writer, connector);
      writeProtocolServerConnector(writer, connector);
      writer.writeEndElement();
   }

   private void writeRestConnector(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      writer.writeStartElement(Element.REST_CONNECTOR.getLocalName());
      writeCommonConnector(writer, connector);
      for(SimpleAttributeDefinition attribute : RestConnectorResource.REST_ATTRIBUTES) {
         attribute.marshallAsAttribute(connector, false, writer);
      }
      writer.writeEndElement();
   }

   private void writeCommonConnector(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      for(SimpleAttributeDefinition attribute : CommonConnectorResource.COMMON_CONNECTOR_ATTRIBUTES) {
         attribute.marshallAsAttribute(connector, false, writer);
      }
   }

   private void writeProtocolServerConnector(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      for(SimpleAttributeDefinition attribute : ProtocolServerConnectorResource.PROTOCOL_SERVICE_ATTRIBUTES) {
         attribute.marshallAsAttribute(connector, false, writer);
      }
   }

   private void writeTopologyStateTransfer(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      if (connector.hasDefined(ModelKeys.TOPOLOGY_STATE_TRANSFER)) {
         ModelNode topologyStateTransfer = connector.get(ModelKeys.TOPOLOGY_STATE_TRANSFER, ModelKeys.TOPOLOGY_STATE_TRANSFER_NAME);
         writer.writeStartElement(Element.TOPOLOGY_STATE_TRANSFER.getLocalName());
         for(SimpleAttributeDefinition attribute : TopologyStateTransferResource.TOPOLOGY_ATTRIBUTES) {
            attribute.marshallAsAttribute(topologyStateTransfer, false, writer);
         }
         writer.writeEndElement();
      }
   }

   private void writeSecurity(final XMLExtendedStreamWriter writer, final ModelNode connector) throws XMLStreamException {
      if (connector.hasDefined(ModelKeys.SECURITY)) {
         ModelNode security = connector.get(ModelKeys.SECURITY, ModelKeys.SECURITY_NAME);
         writer.writeStartElement(Element.SECURITY.getLocalName());
         for(SimpleAttributeDefinition attribute : SecurityResource.SECURITY_ATTRIBUTES) {
            attribute.marshallAsAttribute(security, false, writer);
         }
         writer.writeEndElement();
      }
   }
}
