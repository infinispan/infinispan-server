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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

public class RestWriteAttributeHandler extends ReloadRequiredWriteAttributeHandler {
   private static final AttributeDefinition[] ATTRIBUTES = {
      EndpointAttributeDefinitions.NAME,
      EndpointAttributeDefinitions.CACHE_CONTAINER,
      EndpointAttributeDefinitions.CONTEXT_PATH,
      EndpointAttributeDefinitions.VIRTUAL_SERVER,
      EndpointAttributeDefinitions.SECURITY_DOMAIN,
      EndpointAttributeDefinitions.AUTH_METHOD
   };
   static final RestWriteAttributeHandler INSTANCE = new RestWriteAttributeHandler();

   private RestWriteAttributeHandler() {
       super(ATTRIBUTES);
   }

   public void registerAttributes(final ManagementResourceRegistration registry) {
       for (AttributeDefinition attr : ATTRIBUTES) {
           registry.registerReadWriteAttribute(attr, null, this);
       }
   }

}