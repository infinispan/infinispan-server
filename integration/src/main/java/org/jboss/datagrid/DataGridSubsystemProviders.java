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
package org.jboss.datagrid;

import java.util.Locale;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

/**
 * @author Emanuel Muckenhuber
 */
public class DataGridSubsystemProviders {

    private final DataGridSubsystemDescriptions descriptions;
    private final DescriptionProvider subsystem;
    private final DescriptionProvider subsystemAdd;
    private final DescriptionProvider subsystemDescribe;
    private final DescriptionProvider subsystemRemove;

    public DataGridSubsystemProviders(String subsystemName, Class<? extends Extension> extensionType) {
        descriptions = new DataGridSubsystemDescriptions(subsystemName, extensionType.getPackage());
        subsystem = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(final Locale locale) {
                return descriptions.getRootResource(locale);
            }
        };
        subsystemAdd = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(final Locale locale) {
                return descriptions.getSubsystemAdd(locale);
            }
        };
        subsystemDescribe = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(Locale locale) {
                return descriptions.getSubsystemDescribe(locale);
            }
        };
        subsystemRemove = new DescriptionProvider() {
            @Override
            public ModelNode getModelDescription(final Locale locale) {
                return descriptions.getSubsystemRemove(locale);
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

    public DescriptionProvider subsystemRemove() {
        return subsystemRemove;
    }
}
