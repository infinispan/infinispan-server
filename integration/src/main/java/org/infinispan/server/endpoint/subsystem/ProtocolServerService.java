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
package org.infinispan.server.endpoint.subsystem;

import static org.infinispan.server.endpoint.EndpointLogger.ROOT_LOGGER;

import java.net.InetSocketAddress;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.core.ProtocolServer;
import org.infinispan.server.core.configuration.ProtocolServerConfiguration;
import org.infinispan.server.core.configuration.ProtocolServerConfigurationBuilder;
import org.infinispan.server.core.transport.Transport;
import org.infinispan.util.ReflectionUtil;
import org.jboss.as.network.NetworkUtils;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * The service that configures and starts the endpoints supported by data grid.
 *
 * @author Tristan Tarrant
 */
class ProtocolServerService implements Service<ProtocolServer> {
   // The cacheManager that will be injected by the container (specified by the cacheContainer
   // attribute)
   private final InjectedValue<EmbeddedCacheManager> cacheManager = new InjectedValue<EmbeddedCacheManager>();
   // The socketBinding that will be injected by the container
   private final InjectedValue<SocketBinding> socketBinding = new InjectedValue<SocketBinding>();
   // The configuration for this service
   private final ProtocolServerConfigurationBuilder<?, ?> configurationBuilder;
   // The class which determines the type of server
   private final Class<? extends ProtocolServer> serverClass;
   // The server which handles the protocol
   private ProtocolServer protocolServer;
   // The transport used by the protocol server
   private Transport transport;
   // The name of the server
   private final String serverName;

   ProtocolServerService(String serverName, Class<? extends ProtocolServer> serverClass, ProtocolServerConfigurationBuilder<?, ?> configurationBuilder) {
      this.configurationBuilder = configurationBuilder;
      this.serverClass = serverClass;
      String serverTypeName = serverClass.getSimpleName();
      this.serverName = serverName != null ? serverTypeName + " " + serverName : serverTypeName;
   }

   @Override
   public synchronized void start(final StartContext context) throws StartException {
      ROOT_LOGGER.endpointStarting(serverName);

      boolean done = false;
      try {
         SocketBinding socketBinding = getSocketBinding().getValue();
         InetSocketAddress socketAddress = socketBinding.getSocketAddress();
         configurationBuilder.host(socketAddress.getAddress().getHostAddress());
         configurationBuilder.port(socketAddress.getPort());
         ROOT_LOGGER.endpointStarted(serverName, NetworkUtils.formatAddress(socketAddress), socketAddress.getPort());
         // Start the connector
         startProtocolServer(configurationBuilder.build());

         done = true;
      } catch (StartException e) {
         throw e;
      } catch (Exception e) {
         throw ROOT_LOGGER.failedStart(e, serverName);
      } finally {
         if (!done) {
            doStop();
         }
      }
   }

   private void startProtocolServer(ProtocolServerConfiguration configuration) throws StartException {
      // Start the server and record it
      ProtocolServer server;
      try {
         server = serverClass.newInstance();
      } catch (Exception e) {
         throw ROOT_LOGGER.failedConnectorInstantiation(e, serverName);
      }
      ROOT_LOGGER.connectorStarting(serverName);
      server.start(configuration, getCacheManager().getValue());
      protocolServer = server;

      try {
         transport = (Transport) ReflectionUtil.getValue(protocolServer, "transport");
      } catch (Exception e) {
         throw ROOT_LOGGER.failedTransportInstantiation(e.getCause(), serverName);
      }
   }

   @Override
   public synchronized void stop(final StopContext context) {
      doStop();
   }

   private void doStop() {
      try {
         if (protocolServer != null) {
            ROOT_LOGGER.connectorStopping(serverName);
            try {
               protocolServer.stop();
            } catch (Exception e) {
               ROOT_LOGGER.connectorStopFailed(e, serverName);
            }
         }
      } finally {
         ROOT_LOGGER.connectorStopped(serverName);
      }
   }

   @Override
   public synchronized ProtocolServer getValue() throws IllegalStateException {
      if (protocolServer == null) {
         throw new IllegalStateException();
      }
      return protocolServer;
   }

   InjectedValue<EmbeddedCacheManager> getCacheManager() {
      return cacheManager;
   }

   InjectedValue<SocketBinding> getSocketBinding() {
      return socketBinding;
   }

   public Transport getTransport() {
      return transport;
   }
}
