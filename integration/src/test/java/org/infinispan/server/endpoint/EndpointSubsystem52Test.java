/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012-2013 Red Hat Inc. and/or its affiliates and other contributors
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
package org.infinispan.server.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.infinispan.server.endpoint.subsystem.EndpointExtension;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.ControllerInitializer;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

/**
 * @author <a href="tomaz.cerar@gmail.com">Tomaz Cerar</a>
 * @author Tristan Tarrant
 */
public class EndpointSubsystem52Test extends AbstractSubsystemBaseTest {

   public EndpointSubsystem52Test() {
      super(Constants.SUBSYSTEM_NAME, new EndpointExtension());
   }

   protected int expectedOperationCount() {
      return 7;
   }

   /**
    * Tests that the xml is parsed into the correct operations
    */
   @Test
   public void testParseSubsystem() throws Exception {
      //Parse the subsystem xml into operations
      List<ModelNode> operations = super.parse(getSubsystemXml());

      ///Check that we have the expected number of operations
      Assert.assertEquals(expectedOperationCount(), operations.size());

      //Check that each operation has the correct content
      ModelNode addSubsystem = operations.get(0);
      Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
      PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
      Assert.assertEquals(1, addr.size());
      PathElement element = addr.getElement(0);
      Assert.assertEquals(SUBSYSTEM, element.getKey());
      Assert.assertEquals(Constants.SUBSYSTEM_NAME, element.getValue());
   }

   @Override
   protected KernelServices standardSubsystemTest(String configId, boolean compareXml) throws Exception {
      return super.standardSubsystemTest(configId, false);
   }

   @Override
   protected String getSubsystemXml() throws IOException {
      return readResource("/endpoint-5.2.xml");
   }

   @Override
   protected AdditionalInitialization createAdditionalInitialization() {
      return new Initializer();
   }

   public static class Initializer extends AdditionalInitialization {
      @Override
      protected ControllerInitializer createControllerInitializer() {
         ControllerInitializer ci = new ControllerInitializer() {

            @Override
            protected void initializeSocketBindingsOperations(List<ModelNode> ops) {

               super.initializeSocketBindingsOperations(ops);

               final String[] names = { "hotrod", "memcached" };
               final int[] ports = { 11222, 11211 };
               for (int i = 0; i < names.length; i++) {
                  final ModelNode op = new ModelNode();
                  op.get(OP).set(ADD);
                  op.get(OP_ADDR).set(
                        PathAddress.pathAddress(PathElement.pathElement(SOCKET_BINDING_GROUP, SOCKET_BINDING_GROUP_NAME),
                              PathElement.pathElement(REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING, names[i])).toModelNode());
                  op.get(HOST).set("localhost");
                  op.get(PORT).set(ports[i]);
                  ops.add(op);
               }
            }
         };

         // Adding a socket-binding is what triggers ControllerInitializer to set up the interface
         // and socket-binding-group stuff we depend on TODO something less hacky
         ci.addSocketBinding("make-framework-happy", 59999);
         return ci;
      }
   }
}
