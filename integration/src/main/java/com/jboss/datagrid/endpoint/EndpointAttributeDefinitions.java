/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.jboss.datagrid.endpoint;

import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelType;

public class EndpointAttributeDefinitions {

   protected static final SimpleAttributeDefinition NAME =
         new SimpleAttributeDefinitionBuilder(ModelKeys.NAME, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.NAME)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition CACHE_CONTAINER =
         new SimpleAttributeDefinitionBuilder(ModelKeys.CACHE_CONTAINER, ModelType.STRING, false)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.CACHE_CONTAINER)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition SOCKET_BINDING =
         new SimpleAttributeDefinitionBuilder(ModelKeys.SOCKET_BINDING, ModelType.STRING, false)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.SOCKET_BINDING)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition WORKER_THREADS =
         new SimpleAttributeDefinitionBuilder(ModelKeys.WORKER_THREADS, ModelType.INT, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.WORKER_THREADS)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition IDLE_TIMEOUT =
         new SimpleAttributeDefinitionBuilder(ModelKeys.IDLE_TIMEOUT, ModelType.LONG, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.IDLE_TIMEOUT)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition TCP_NODELAY =
         new SimpleAttributeDefinitionBuilder(ModelKeys.TCP_NODELAY, ModelType.BOOLEAN, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.TCP_NODELAY)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition RECEIVE_BUFFER_SIZE =
         new SimpleAttributeDefinitionBuilder(ModelKeys.RECEIVE_BUFFER_SIZE, ModelType.LONG, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.RECEIVE_BUFFER_SIZE)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition SEND_BUFFER_SIZE =
         new SimpleAttributeDefinitionBuilder(ModelKeys.SEND_BUFFER_SIZE, ModelType.LONG, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.SEND_BUFFER_SIZE)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition EXTERNAL_HOST =
         new SimpleAttributeDefinitionBuilder(ModelKeys.EXTERNAL_HOST, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.EXTERNAL_HOST)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition EXTERNAL_PORT =
         new SimpleAttributeDefinitionBuilder(ModelKeys.EXTERNAL_PORT, ModelType.INT, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.EXTERNAL_PORT)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition LAZY_RETRIEVAL =
         new SimpleAttributeDefinitionBuilder(ModelKeys.LAZY_RETRIEVAL, ModelType.BOOLEAN, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.LAZY_RETRIEVAL)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition LOCK_TIMEOUT =
         new SimpleAttributeDefinitionBuilder(ModelKeys.LOCK_TIMEOUT, ModelType.LONG, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.LOCK_TIMEOUT)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition REPLICATION_TIMEOUT =
         new SimpleAttributeDefinitionBuilder(ModelKeys.REPLICATION_TIMEOUT, ModelType.LONG, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.REPLICATION_TIMEOUT)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition CONTEXT_PATH =
         new SimpleAttributeDefinitionBuilder(ModelKeys.CONTEXT_PATH, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.CONTEXT_PATH)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition VIRTUAL_SERVER =
         new SimpleAttributeDefinitionBuilder(ModelKeys.VIRTUAL_SERVER, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.VIRTUAL_SERVER)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition SECURITY_DOMAIN =
         new SimpleAttributeDefinitionBuilder(ModelKeys.SECURITY_DOMAIN, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.SECURITY_DOMAIN)
                 .setRestartAllServices()
                 .build();

   protected static final SimpleAttributeDefinition AUTH_METHOD =
         new SimpleAttributeDefinitionBuilder(ModelKeys.AUTH_METHOD, ModelType.STRING, true)
                 .setAllowExpression(true)
                 .setXmlName(ModelKeys.AUTH_METHOD)
                 .setRestartAllServices()
                 .build();

   protected static final ObjectTypeAttributeDefinition TOPOLOGY_STATE_TRANSFER =
         ObjectTypeAttributeDefinition.Builder.of(ModelKeys.TOPOLOGY_STATE_TRANSFER,
               EXTERNAL_HOST, EXTERNAL_PORT, LAZY_RETRIEVAL, LOCK_TIMEOUT, REPLICATION_TIMEOUT).build();
}
