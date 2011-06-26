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

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

/**
 * No description at the moment.
 */
class EndpointSubsystemProviders {

    private final DescriptionProvider subsystem;
    private final DescriptionProvider subsystemAdd;
    private final DescriptionProvider subsystemDescribe;

    EndpointSubsystemProviders() {
        subsystem = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(final Locale locale) {
                return new ModelNode();
            }
        };
        subsystemAdd = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(final Locale locale) {
                return new ModelNode();
            }
        };
        subsystemDescribe = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(Locale locale) {
                return new ModelNode();
            }
        };
    }

    public DescriptionProvider subsystem() {
        return subsystem;
    }

    public DescriptionProvider subsystemAdd() {
        return subsystemAdd;
    }

    public DescriptionProvider subsystemDescribe() {
        return subsystemDescribe;
    }
}
