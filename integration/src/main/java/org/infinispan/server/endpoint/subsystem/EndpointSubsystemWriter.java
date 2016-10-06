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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.clustering.infinispan.subsystem.Namespace;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * The parser for the data grid endpoint subsystem configuration.
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
