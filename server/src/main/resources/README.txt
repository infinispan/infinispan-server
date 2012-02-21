Welcome to JBoss Data Grid 6.0
http://www.jboss.com/edg6-early-access/

JBoss Data Grid is a cloud-ready highly scalable distributed data
store from Red Hat. You might be wondering how to use it, so here's an example:

1. Fire up JDG by executing:

./bin/standalone.sh

or if you are in Windows:

./bin/standalone.bat

2. Open up your favorite IDE, create a new project, create a Demo class,
and add a main method:

public class Demo {
   public static void main(String[] args) {
      ...
   }
}

3. Set up your dependencies:
The easiest approach is to use this Maven pom; save it into a new directory
and import it into your favorite IDE! Alternatively, you can add Infinispan to
an existing project. If you use Maven (or another build tool which can download
dependencies from Maven) then the easiest approach is to add a dependency on:

<dependency>
   <groupId>org.infinispan</groupId>
   <artifactId>infinispan-client-hotrod</artifactId>
   <version>5.1.0.FINAL</version>
</dependency>

4. Once you have set up your dependencies, you need to establish a remote
connection to the JBoss Data Grid:

public static void main(String[] args) {
   // By default it connects to localhost:11222
   CacheContainer container = new RemoteCacheManager();
}

5. Next, you need to get a handle on the cache:

public static void main(String[] args) {
   // By default it connects to localhost:11222
   CacheContainer container = new RemoteCacheManager();
   RemoteCache cache = container.getCache();
}

6. Finally, you can store and retrieve data from JBoss Data Grid:

public static void main(String[] args) {
   // By default it connects to localhost:11222
   CacheContainer container = new RemoteCacheManager();
   RemoteCache cache = container.getCache();

   cache.put("key", "value");
   assert cache.containsKey("key");
   cache.remove("key");
   assert cache.isEmpty();
}

