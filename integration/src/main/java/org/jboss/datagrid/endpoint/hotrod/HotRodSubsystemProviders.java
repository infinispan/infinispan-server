/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.datagrid.endpoint.hotrod;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.datagrid.endpoint.EndpointDescriptions;
import org.jboss.dmr.ModelNode;

/**
 * @author Emanuel Muckenhuber
 */
class HotRodSubsystemProviders {

    static final EndpointDescriptions DESCRIPTIONS =
        new EndpointDescriptions(HotRodExtension.SUBSYSTEM_NAME, HotRodExtension.class.getPackage());

    static final DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(final Locale locale) {
            return DESCRIPTIONS.getRootResource(locale);
        }
    };


    static final DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(final Locale locale) {
            return DESCRIPTIONS.getSubsystemAdd(locale);
        }
    };

    static final DescriptionProvider SUBSYSTEM_DESCRIBE = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            return DESCRIPTIONS.getSubsystemDescribe(locale);
        }
    };

    static final DescriptionProvider SUBSYSTEM_REMOVE = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(final Locale locale) {
            return DESCRIPTIONS.getSubsystemRemove(locale);
        }
    };
}
