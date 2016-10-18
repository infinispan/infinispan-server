/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

import org.apache.catalina.startup.ContextConfig;
import org.jboss.as.web.WebLogger;

/**
 *
 * RestContextConfig.
 *
 * @author Tristan Tarrant
 * @since 6.0
 */
public class RestContextConfig extends ContextConfig {

   protected static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(RestContextConfig.class);

   @Override
   protected void completeConfig() {
      if (ok) {
         resolveServletSecurity();
      }

      if (ok) {
         validateSecurityRoles();
      }

      // Configure an authenticator if we need one
      if (ok) {
         authenticatorConfig();
      }

      // Make our application unavailable if problems were encountered
      if (!ok) {
         WebLogger.WEB_LOGGER.unavailable(context.getName());
         context.setConfigured(false);
      }
   }

}
