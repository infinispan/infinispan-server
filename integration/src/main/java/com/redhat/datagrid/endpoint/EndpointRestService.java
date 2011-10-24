/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package com.redhat.datagrid.endpoint;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.ws.rs.core.Application;

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
import org.jboss.as.server.mgmt.domain.HttpManagement;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.deployment.WebCtxLoader;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
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
public class EndpointRestService implements Service<Context> {

   private static final Logger log = Logger.getLogger("com.redhat.datagrid");
   private final StandardContext context;
   private final InjectedValue<String> pathInjector = new InjectedValue<String>();
   private final InjectedValue<VirtualHost> hostInjector = new InjectedValue<VirtualHost>();
   private final InjectedValue<HttpManagement> httpManagementInjector = new InjectedValue<HttpManagement>();
   private final InjectedValue<EmbeddedCacheManager> cacheManagerInjector = new InjectedValue<EmbeddedCacheManager>();
   private Method cacheManagerSetter;
   

   public EndpointRestService() {
      this.context = new StandardContext();

      try {
         Class<?> cls = Class.forName("org.infinispan.rest.ManagerInstance", true, getClass()
                  .getClassLoader());
         cacheManagerSetter = cls.getMethod("instance_$eq", EmbeddedCacheManager.class);
      } catch (Exception e) {
         throw new ServiceRegistryException("failed to locate ManagerInstance.instance", e);
      }
   }

   /** {@inheritDoc} */
   public synchronized void start(StartContext startContext) throws StartException {
      HttpManagement httpManagement = httpManagementInjector.getOptionalValue();
      EmbeddedCacheManager cacheManager = cacheManagerInjector.getValue();
      try {
         cacheManagerSetter.invoke(null, cacheManager);
      } catch (Exception e) {
         throw new StartException("Could not set the cacheManager on the REST endpoint", e);
      }
      try {
         context.setPath("");
         context.addLifecycleListener(new ContextConfig());
         context.setDocBase(pathInjector.getValue() + File.separatorChar + "rest");

         final Loader loader = new WebCtxLoader(this.getClass().getClassLoader());
         Host host = hostInjector.getValue().getHost();
         loader.setContainer(host);
         context.setLoader(loader);
         context.setInstanceManager(new LocalInstanceManager(httpManagement));

         // Configuration for Resteasy bootstrap
         addContextApplicationParameter(context, "resteasy.resources", "org.infinispan.rest.Server");
         //addContextApplicationParameter(context, "javax.ws.rs.Application" , EndpointRestApplication.class.getName());
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
         log.info("Started REST server");
      } catch (Exception e) {
         throw new StartException("failed to create context", e);
      }
      try {
         context.start();
      } catch (LifecycleException e) {
         throw new StartException("failed to start context", e);
      }
   }

   private static void addContextApplicationParameter(Context context, String paramName,
            String paramValue) {
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

   public Injector<HttpManagement> getHttpManagementInjector() {
      return httpManagementInjector;
   }

   public InjectedValue<EmbeddedCacheManager> getCacheManagerInjector() {
      return cacheManagerInjector;
   }

   private static class LocalInstanceManager implements InstanceManager {
      private final HttpManagement httpManagement;

      LocalInstanceManager(HttpManagement httpManagement) {
         this.httpManagement = httpManagement;
      }

      @Override
      public Object newInstance(String className) throws IllegalAccessException,
               InvocationTargetException, NamingException, InstantiationException,
               ClassNotFoundException {
         return Class.forName(className).newInstance();
      }

      @Override
      public Object newInstance(String fqcn, ClassLoader classLoader)
               throws IllegalAccessException, InvocationTargetException, NamingException,
               InstantiationException, ClassNotFoundException {
         return Class.forName(fqcn, false, classLoader).newInstance();
      }

      @Override
      public Object newInstance(Class<?> c) throws IllegalAccessException,
               InvocationTargetException, NamingException, InstantiationException {
         return c.newInstance();
      }

      @Override
      public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException,
               NamingException {
         throw new IllegalStateException();
      }

      @Override
      public void destroyInstance(Object o) throws IllegalAccessException,
               InvocationTargetException {
      }
   }
}
