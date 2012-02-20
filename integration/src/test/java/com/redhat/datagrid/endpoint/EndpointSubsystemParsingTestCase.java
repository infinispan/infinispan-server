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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.datagrid.DataGridConstants;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="http://www.dataforte.net/blog/">Tristan Tarrant</a>
 */
public class EndpointSubsystemParsingTestCase extends AbstractSubsystemTest {

   private String subsystemXml;

   public EndpointSubsystemParsingTestCase() throws IOException {
      super(DataGridConstants.SN_ENDPOINT.getSimpleName(), new EndpointExtension());
      InputStream is = this.getClass().getResourceAsStream("/subsystem-endpoint.xml");
      subsystemXml = loadStreamAsString(is);

   }

   private static String loadStreamAsString(InputStream is) throws java.io.IOException {
      StringBuffer s = new StringBuffer(1000);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      char[] buf = new char[1024];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
         s.append(buf, 0, numRead);
      }
      reader.close();
      return s.toString();
   }

   /**
    * Tests that the xml is parsed into the correct operations
    */
   @Test
   public void testParseSubsystem() throws Exception {
      // Parse the subsystem xml into operations
      List<ModelNode> operations = super.parse(subsystemXml);

      // Check that we have the expected number of operations
      Assert.assertEquals(5, operations.size());

      // Check that each operation has the correct content
      ModelNode addSubsystem = operations.get(0);
      Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
      PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
      Assert.assertEquals(1, addr.size());
      PathElement element = addr.getElement(0);
      Assert.assertEquals(SUBSYSTEM, element.getKey());
      Assert.assertEquals(DataGridConstants.SN_ENDPOINT.getSimpleName(), element.getValue());
   }

   /**
    * Test that the model created from the xml looks as expected
    */
   @Test
   public void testInstallIntoController() throws Exception {
      // Parse the subsystem xml and install into the controller
      KernelServices services = super.installInController(new EndpointAdditionalInitialization(), subsystemXml);

      // Read the whole model and make sure it looks as expected
      ModelNode model = services.readWholeModel();
      Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(DataGridConstants.SN_ENDPOINT.getSimpleName()));
   }

   /**
    * Starts a controller with a given subsystem xml and then checks that a second controller
    * started with the xml marshalled from the first one results in the same model
    */
   @Test
   public void testParseAndMarshalModel() throws Exception {
      // Parse the subsystem xml and install into the first controller

      KernelServices servicesA = super.installInController(new EndpointAdditionalInitialization(), subsystemXml);

      // Get the model and the persisted xml from the first controller
      ModelNode modelA = servicesA.readWholeModel();
      String marshalled = servicesA.getPersistedSubsystemXml();
      // Install the persisted xml from the first controller into a second controller
      KernelServices servicesB = super.installInController(new EndpointAdditionalInitialization(),marshalled);
      ModelNode modelB = servicesB.readWholeModel();

      // Make sure the models from the two controllers are identical
      super.compare(modelA, modelB);
   }

   /**
    * Starts a controller with the given subsystem xml and then checks that a second controller
    * started with the operations from its describe action results in the same model
    */
   @Test
   public void testDescribeHandler() throws Exception {
      // Parse the subsystem xml and install into the first controller
      KernelServices servicesA = super.installInController(new EndpointAdditionalInitialization(), subsystemXml);
      // Get the model and the describe operations from the first controller
      ModelNode modelA = servicesA.readWholeModel();
      ModelNode describeOp = new ModelNode();
      describeOp.get(OP).set(DESCRIBE);
      describeOp.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, DataGridConstants.SN_ENDPOINT.getSimpleName())).toModelNode());
      List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

      // Install the describe options from the first controller into a second controller
      KernelServices servicesB = super.installInController(new EndpointAdditionalInitialization(), operations);
      ModelNode modelB = servicesB.readWholeModel();

      // Make sure the models from the two controllers are identical
      super.compare(modelA, modelB);

   }

   private static final class EndpointAdditionalInitialization extends AdditionalInitialization {
      @Override
      protected void addExtraServices(ServiceTarget target) {
         target.addService(MockTransportService.NAME, new MockTransportService()).install();
      }
   }

   public static class MockTransportService implements Service<MockTransportService> {

      public static final ServiceName NAME = ServiceName.JBOSS.append("infinispan", "default", "transport");

      @Override
      public MockTransportService getValue() throws IllegalStateException, IllegalArgumentException {
         return this;
      }

      @Override
      public void start(StartContext context) throws StartException {
      }

      @Override
      public void stop(StopContext context) {
      }

   }
}