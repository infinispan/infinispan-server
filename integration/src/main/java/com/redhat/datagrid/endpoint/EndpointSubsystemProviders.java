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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MAX_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.redhat.datagrid.DataGridConstants;

/**
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
public class EndpointSubsystemProviders {

   static final String RESOURCE_NAME = EndpointSubsystemProviders.class.getPackage().getName()
            + ".LocalDescriptions";

   static final DescriptionProvider SUBSYSTEM = new DescriptionProvider() {

      public ModelNode getModelDescription(final Locale locale) {
         final ResourceBundle bundle = getResourceBundle(locale);

         final ModelNode node = new ModelNode();
         node.get(DESCRIPTION).set(bundle.getString("endpoint.description"));
         node.get(HEAD_COMMENT_ALLOWED).set(true);
         node.get(TAIL_COMMENT_ALLOWED).set(true);
         node.get(NAMESPACE).set(DataGridConstants.NS_ENDPOINT_1_0);
         
         node.get(OPERATIONS);

         node.get(CHILDREN, ModelKeys.CONNECTOR, DESCRIPTION).set(
                  bundle.getString("connector.description"));
         node.get(CHILDREN, ModelKeys.CONNECTOR, MIN_OCCURS).set(1);
         node.get(CHILDREN, ModelKeys.CONNECTOR, MAX_OCCURS).set(Integer.MAX_VALUE);
         node.get(CHILDREN, ModelKeys.CONNECTOR, MODEL_DESCRIPTION).setEmptyObject();

         node.get(CHILDREN, ModelKeys.TOPOLOGY_STATE_TRANSFER, DESCRIPTION).set(
                  bundle.getString("topology-state-transfer.description"));
         node.get(CHILDREN, ModelKeys.TOPOLOGY_STATE_TRANSFER, MIN_OCCURS).set(0);
         node.get(CHILDREN, ModelKeys.TOPOLOGY_STATE_TRANSFER, MAX_OCCURS).set(1);
         node.get(CHILDREN, ModelKeys.TOPOLOGY_STATE_TRANSFER, MODEL_DESCRIPTION).setEmptyObject();

         return node;
      }
   };

   static final DescriptionProvider SUBSYSTEM_DESCRIBE = new DescriptionProvider() {

      @Override
      public ModelNode getModelDescription(final Locale locale) {
         final ResourceBundle bundle = getResourceBundle(locale);
         final ModelNode node = new ModelNode();
         node.get(OPERATION_NAME).set(DESCRIBE);
         node.get(DESCRIPTION).set(bundle.getString("endpoint.description"));
         node.get(REQUEST_PROPERTIES).setEmptyObject();
         node.get(REPLY_PROPERTIES, TYPE).set(ModelType.LIST);
         node.get(REPLY_PROPERTIES, VALUE_TYPE).set(ModelType.OBJECT);

         return node;
      }
   };

   static final DescriptionProvider SUBSYTEM_ADD = new DescriptionProvider() {

      public ModelNode getModelDescription(Locale locale) {
         final ResourceBundle bundle = getResourceBundle(locale);

         final ModelNode node = new ModelNode();

         node.get(OPERATION_NAME).set(ADD);
         node.get(DESCRIPTION).set(bundle.getString("endpoint.add"));

         final ModelNode connectorNode = addNode(node.get(REQUEST_PROPERTIES), ModelKeys.CONNECTOR,
                  bundle.getString("connector.description"), ModelType.OBJECT, false);
         final ModelNode connectorNodeValue = connectorNode.get(VALUE_TYPE);
            
         addNode(connectorNodeValue, ModelKeys.PROTOCOL, bundle.getString("connector.protocol"),
                  ModelType.STRING, true);
         addNode(connectorNodeValue, ModelKeys.SOCKET_BINDING,
                  bundle.getString("connector.socket-binding"), ModelType.STRING, false);
         addNode(connectorNodeValue, ModelKeys.WORKER_THREADS,
                  bundle.getString("connector.worker-threads"), ModelType.INT, false);
         addNode(connectorNodeValue, ModelKeys.IDLE_TIMEOUT, bundle.getString("connector.idle-timeout"),
                  ModelType.LONG, false);
         addNode(connectorNodeValue, ModelKeys.TCP_NODELAY, bundle.getString("connector.tcp-nodelay"),
                  ModelType.BOOLEAN, false);
         addNode(connectorNodeValue, ModelKeys.RECEIVE_BUFFER_SIZE,
                  bundle.getString("connector.receive-buffer-size"), ModelType.LONG, false);
         addNode(connectorNodeValue, ModelKeys.SEND_BUFFER_SIZE,
                  bundle.getString("connector.send-buffer-size"), ModelType.LONG, false);

         final ModelNode topologyNode = addNode(node.get(REQUEST_PROPERTIES),
                  ModelKeys.TOPOLOGY_STATE_TRANSFER,
                  bundle.getString("topology-state-transfer.description"), ModelType.OBJECT, false);
         final ModelNode topologyNodeValue = topologyNode.get(VALUE_TYPE);

         addNode(topologyNodeValue, ModelKeys.EXTERNAL_HOST,
                  bundle.getString("topology-state-transfer.external-host"), ModelType.STRING,
                  false);
         addNode(topologyNodeValue, ModelKeys.EXTERNAL_PORT,
                  bundle.getString("topology-state-transfer.external-port"), ModelType.INT, false);
         addNode(topologyNodeValue, ModelKeys.LAZY_RETRIEVAL,
                  bundle.getString("topology-state-transfer.lazy-retrieval"), ModelType.BOOLEAN,
                  false);
         addNode(topologyNodeValue, ModelKeys.LOCK_TIMEOUT,
                  bundle.getString("topology-state-transfer.lock-timeout"), ModelType.LONG, false);
         addNode(topologyNodeValue, ModelKeys.REPLICATION_TIMEOUT,
                  bundle.getString("topology-state-transfer.replication-timeout"), ModelType.LONG,
                  false);

         node.get(REPLY_PROPERTIES).setEmptyObject();
         return node;
      }

   };

   private static ResourceBundle getResourceBundle(Locale locale) {
      if (locale == null) {
         locale = Locale.getDefault();
      }
      return ResourceBundle.getBundle(RESOURCE_NAME, locale);
   }

   private static ModelNode addNode(ModelNode parent, String attribute, String description,
            ModelType type, boolean required) {
      ModelNode node = parent.get(attribute);
      node.get(DESCRIPTION).set(description);
      node.get(TYPE).set(type);
      node.get(REQUIRED).set(required);

      return node;
   }

   private static void addModelAttribute(ModelNode node, String attribute, String description,
            ModelType type, boolean required) {
      addNode(node.get(ATTRIBUTES), attribute, description, type, required);
   }

   private static void addModelRequestProperty(ModelNode node, String attribute,
            String description, ModelType type, boolean required) {
      addNode(node.get(REQUEST_PROPERTIES), attribute, description, type, required);
   }
}
