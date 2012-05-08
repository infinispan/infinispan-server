package com.jboss.datagrid.endpoint;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.InjectedValue;

import com.jboss.datagrid.DataGridConstants;

public class EndpointUtils {
   private static final String INFINISPAN_SERVICE_NAME = "infinispan";

   public static ServiceName getCacheServiceName(String cacheContainerName, String cacheName) {
      ServiceName cacheServiceName = getCacheContainerServiceName(cacheContainerName);
      if (cacheName != null) {
         cacheServiceName = cacheServiceName.append(cacheName);
      } else {
         cacheServiceName = cacheServiceName.append(CacheContainer.DEFAULT_CACHE_NAME);
      }
      return cacheServiceName;
   }

   public static ServiceName getCacheContainerServiceName(String cacheContainerName) {
      ServiceName cacheContainerServiceName = ServiceName.JBOSS.append(INFINISPAN_SERVICE_NAME);
      if (cacheContainerName != null) {
         cacheContainerServiceName = cacheContainerServiceName.append(cacheContainerName);
      }
      return cacheContainerServiceName;
   }

   public static ServiceName getServiceName(final ModelNode node, final String... prefix) {
      final PathAddress address = PathAddress.pathAddress(node.require(OP_ADDR));
      final String name = address.getLastElement().getValue();
      if (prefix.length > 0)
         return DataGridConstants.DATAGRID.append(prefix).append(name);
      else
         return DataGridConstants.DATAGRID.append(name);
   }

   public static void addCacheDependency(OperationContext context, ServiceBuilder<?> builder, String cacheContainerName, String cacheName) {
      ServiceName cacheServiceName = getCacheServiceName(cacheContainerName, cacheName);
      builder.addDependency(ServiceBuilder.DependencyType.REQUIRED, cacheServiceName);
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
