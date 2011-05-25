HOW TO BUILD
============

1) Check out the source code.  The latest JBoss AS will also be pulled into
   the external/as directory.  

NOTE: We could later set up CI to manage our own Maven repository that has
      the latest ZIP distribution, like TorqueBox team does:

          https://torquebox.ci.cloudbees.com/job/jboss-as7/lastSuccessfulBuild/artifact/.repository/org/jboss/as/jboss-as-build/7.0.0.Beta4-SNAPSHOT/

2) Build AS 7 and install the artifacts into the local Maven repository:

    $ cd external/as
    $ mvn install

3) Build EDG:

    $ cd ../..
    $ mvn install

NOTE: Once you've gone through the step 1 ~ 4, you can build an individual
      module to save the build time.  For example, you can avoid assembling
      AS 7 and ZIP distribution:

          $ cd dist-dir
          $ mvn package

4) Run EDG:

    $ cd dist-dir/target/jboss-datagrid-<version>/bin
    $ sh standalone.sh

    At this point you should be able to connect to 127.0.0.1:11222.

NOTE: I couldn't find a way to create a new profile like 'datagrid', so I 
      simply overwrote the standalone profile.  The configuration files
      specific to Infinispan is located at standalone/configuration.

TO-DO
=====
* Implement core subsystem
* Modify Hot Rod subsystem to use the CacheManager defined in the core
  subsystem (Currently, it just creates its own one.)
* Implement memcached subsystem
* Deploy REST server
* Consider consolidating the current configuration files into standalone.xml
* Figure out how to introduce a new profile 'datagrid' instead of overwriting
  'standalone'
* Rebrand the distribution (AS -> Data Grid)
* Remove unused modules

