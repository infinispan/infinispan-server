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
package com.jboss.datagrid;

import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 */
public class DataGridConstants {

   private static final ServiceName JBOSS = ServiceName.of("jboss");

   public static final ServiceName DATAGRID = JBOSS.append("datagrid");

   // Service names (SN_*)

   public static final ServiceName SN_ENDPOINT = DATAGRID.append("endpoint");

   // XML namespaces (NS_*)

   public static final String NS_ENDPOINT_1_0 = "urn:jboss:domain:datagrid:endpoint:1.0";

   public static String VERSION = DataGridConstants.class.getPackage().getImplementationVersion();

   private DataGridConstants() {
      // Constant table
   }
}
