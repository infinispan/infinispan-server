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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.InstanceManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.deployment.WebCtxLoader;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

/**
 * A service which starts the REST web application
 * 
 * @author Tristan Tarrant <ttarrant@redhat.com>
 */
public class RestService implements Service<Context> {
   private static final String DEFAULT_VIRTUAL_SERVER = "default-host";
   private static final Logger log = Logger.getLogger("com.redhat.datagrid");
   private static final String DEFAULT_CONTEXT_PATH = "";
   private final StandardContext context;
   private final InjectedValue<String> pathInjector = new InjectedValue<String>();
   private final InjectedValue<VirtualHost> hostInjector = new InjectedValue<VirtualHost>();
   private final InjectedValue<EmbeddedCacheManager> cacheManagerInjector = new InjectedValue<EmbeddedCacheManager>();
   private Method cacheManagerSetter;
   private ModelNode config;
   private String virtualServer;
   private String path;
   private String serverName;

   public RestService(ModelNode config) {
      this.config = config.clone();
      this.context = new StandardContext();
      virtualServer = this.config.hasDefined(ModelKeys.VIRTUAL_SERVER) ? this.config.get(ModelKeys.VIRTUAL_SERVER).asString() : DEFAULT_VIRTUAL_SERVER;
      path = this.config.hasDefined(ModelKeys.CONTEXT_PATH) ? cleanContextPath(this.config.get(ModelKeys.CONTEXT_PATH).asString()) : DEFAULT_CONTEXT_PATH;
      this.serverName = config.hasDefined(ModelKeys.NAME)?config.get(ModelKeys.NAME).asString():"";

      // Obtain the setter for injecting the EmbbededCacheManager into the Rest server
      try {
         Class<?> cls = Class.forName("org.infinispan.rest.ManagerInstance", true, getClass().getClassLoader());
         cacheManagerSetter = cls.getMethod("instance_$eq", EmbeddedCacheManager.class);
      } catch (Exception e) {
         throw new ServiceRegistryException("failed to locate ManagerInstance.instance", e);
      }
   }

   private static String cleanContextPath(String s) {
      if(s.endsWith("/")) 
         return s.substring(0, s.length()-1);
      else
         return s;
   }

   /** {@inheritDoc} */
   public synchronized void start(StartContext startContext) throws StartException {
      long startTime = System.currentTimeMillis();
      log.infof("REST Server starting");
      EmbeddedCacheManager cacheManager = cacheManagerInjector.getValue();
      try {
         cacheManagerSetter.invoke(null, cacheManager);
      } catch (Exception e) {
         throw new StartException("Could not set the cacheManager on the REST Server", e);
      }
      try {
         context.setPath(path);
         context.addLifecycleListener(new ContextConfig());
         context.setDocBase(pathInjector.getValue() + File.separatorChar + "rest");

         final Loader loader = new WebCtxLoader(this.getClass().getClassLoader());
         Host host = hostInjector.getValue().getHost();
         loader.setContainer(host);
         context.setLoader(loader);
         context.setInstanceManager(new LocalInstanceManager());

         // Configuration for Resteasy bootstrap
         addContextApplicationParameter(context, "resteasy.resources", "org.infinispan.rest.Server");
         addContextApplicationParameter(context, "resteasy.use.builtin.providers", "true");

         // Setup the Resteasy bootstrap listener
         context.addApplicationListener(ResteasyBootstrap.class.getName());

         // Set the welcome file
         context.setReplaceWelcomeFiles(true);
         context.addWelcomeFile("index.html");

         // Add the default servlet for managing static content
         Wrapper wrapper = context.createWrapper();
         wrapper.setName("default");
         wrapper.setServletClass("org.apache.catalina.servlets.DefaultServlet");
         context.addChild(wrapper);
         context.addServletMapping("/", "default");
         context.addMimeMapping("html", "text/html");
         context.addMimeMapping("jpg", "image/jpeg");

         // Add the Resteasy servlet dispatcher for handling REST requests
         HttpServletDispatcher hsd = new HttpServletDispatcher();
         Wrapper hsdWrapper = context.createWrapper();
         hsdWrapper.setName("Resteasy");
         hsdWrapper.setServlet(hsd);
         hsdWrapper.setServletClass(hsd.getClass().getName());
         context.addChild(hsdWrapper);

         context.addServletMapping("/rest/*", "Resteasy");

         host.addChild(context);
         context.create();         
      } catch (Exception e) {
         throw new StartException("Failed to create context for REST Server "+serverName, e);
      }
      try {
         context.start();
         long elapsedTime = Math.max(System.currentTimeMillis() - startTime, 0L);
         log.infof("REST Server started in %dms", Long.valueOf(elapsedTime));
      } catch (LifecycleException e) {
         throw new StartException("Failed to start context for REST Server "+serverName, e);
      }
   }

   public String getVirtualServer() {
      return virtualServer;
   }

   private static void addContextApplicationParameter(Context context, String paramName, String paramValue) {
      ApplicationParameter parameter = new ApplicationParameter();
      parameter.setName(paramName);
      parameter.setValue(paramValue);
      context.addApplicationParameter(parameter);
   }

   /** {@inheritDoc} */
   public synchronized void stop(StopContext stopContext) {
      try {
         hostInjector.getValue().getHost().removeChild(context);
         context.stop();
      } catch (LifecycleException e) {
         log.error("exception while stopping context", e);
      }
      try {
         context.destroy();
      } catch (Exception e) {
         log.error("exception while destroying context", e);
      }
   }

   String getCacheContainerName() {
      if (!config.hasDefined(ModelKeys.CACHE_CONTAINER)) {
         return null;
      }
      return config.get(ModelKeys.CACHE_CONTAINER).asString();
   }

   /** {@inheritDoc} */
   public synchronized Context getValue() throws IllegalStateException {
      final Context context = this.context;
      if (context == null) {
         throw new IllegalStateException();
      }
      return context;
   }

   public InjectedValue<String> getPathInjector() {
      return pathInjector;
   }

   public InjectedValue<VirtualHost> getHostInjector() {
      return hostInjector;
   }

   public InjectedValue<EmbeddedCacheManager> getCacheManager() {
      return cacheManagerInjector;
   }

   private static class LocalInstanceManager implements InstanceManager {

      @Override
      public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
         return Class.forName(className).newInstance();
      }

      @Override
      public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
         return Class.forName(fqcn, false, classLoader).newInstance();
      }

      @Override
      public Object newInstance(Class<?> c) throws IllegalAccessException, InvocationTargetException, InstantiationException {
         return c.newInstance();
      }

      @Override
      public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException {
         throw new IllegalStateException();
      }

      @Override
      public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
      }
   }

}
