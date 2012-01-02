package com.redhat.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.InjectedValue;

import com.redhat.datagrid.DataGridConstants;

public class EndpointUtils {
   private static final String INFINISPAN_SERVICE_NAME = "infinispan";

   public static ServiceName getCacheContainerServiceName(String cacheContainerName) {
      ServiceName cacheContainerServiceName = ServiceName.JBOSS.append(INFINISPAN_SERVICE_NAME);
      if (cacheContainerName != null) {
         cacheContainerServiceName = cacheContainerServiceName.append(cacheContainerName);
      }
      return cacheContainerServiceName;
   }

   public static ServiceName getTransportServiceName(String cacheContainerName) {
      return getCacheContainerServiceName(cacheContainerName).append("transport");
   }

   public static ServiceName getServiceName(final ModelNode node, final String... prefix) {
      final PathAddress address = PathAddress.pathAddress(node.require(OP_ADDR));
      final String name = address.getLastElement().getValue();
      if (prefix.length > 0)
         return DataGridConstants.SN_ENDPOINT.append(prefix).append(name);
      else
         return DataGridConstants.SN_ENDPOINT.append(name);
   }

   public static void addCacheContainerDependency(OperationContext context, ServiceBuilder<?> builder, String cacheContainerName, InjectedValue<EmbeddedCacheManager> target) {
      ServiceName cacheContainerServiceName = getCacheContainerServiceName(cacheContainerName);
      builder.addDependency(ServiceBuilder.DependencyType.REQUIRED, cacheContainerServiceName, EmbeddedCacheManager.class, target);
   }

   public static void addSocketBindingDependency(ServiceBuilder<?> builder, String socketBindingName, InjectedValue<SocketBinding> target) {
      final ServiceName socketName = SocketBinding.JBOSS_BINDING_NAME.append(socketBindingName);
      builder.addDependency(socketName, SocketBinding.class, target);
   }

   public static ModelNode pathAddress(PathElement... elements) {
      return PathAddress.pathAddress(elements).toModelNode();
   }

   public static void copyIfSet(String name, ModelNode source, ModelNode target) {
      if (source.hasDefined(name)) {
         target.get(name).set(source.get(name));
      }
   }
}
