HOW TO BUILD
============

1) Check out the source code.
2) Run 

    $ git submodule init
    $ git submodule update

   to initialize your local configuration file, and to fetch the AS7 submodule

2) Build EDG:

    $ mvn clean install -DskipTests

NOTE: Once you've built EDG successfully, you can build an individual
      module to save the build time.  For example, you can avoid assembling
      AS 7 and ZIP distribution:

          $ cd dist-dir
          $ mvn package

3) Run EDG:

    $ rm -fr ~/edg
    $ cp -R dist-dir/target/jboss-datagrid-<version> ~/edg
    $ cd ~/edg/bin
    $ sh standalone.sh

NOTE: We copy the distribution directory to a place independent from the
      build so that any modifications made are not lost during the build.

    At this point you should be able to connect to:

      127.0.0.1:11222 (Hot Rod server)
      127.0.0.1:11211 (Memcached)
      http://127.0.0.1:8080/datagrid/ (REST server)

NOTE: I couldn't find a way to create a new profile like 'datagrid', so I 
      simply overwrote the standalone profile.  The configuration files
      specific to Infinispan is located at standalone/configuration.

NOTE: In case of startup failure, AS 7 might mark datagrid.war deployment
      as failed.  Even if you fixed the problem and restart EDG, the REST
      server will not be redeployed.  You will find an empty file named
      'datagrid.war.failed' at standalone/deployments.  Remove the file and
      then AS 7 will try to redeploy the REST server. (i.e. datagrid.war)

TO-DO
=====
* Consolidate the current configuration files into standalone.xml
* Figure out how to introduce a new profile 'datagrid' instead of overwriting
  'standalone'
* Rebrand the distribution (AS -> Data Grid)
* Remove unused modules

HOW TO UPGRADE AS 7
===================
JBoss AS 7 is included in the build via git submodules. Therefore to change
version/branch/etc go into the jboss-as directory and perform the appropriate
checkout. Then perform the following steps:

1) Modify the parent POM of EDG 6 to reflect the change.

    <properties>
      ...
      <version.org.jboss.as>7.0.0.CR1</version.org.jboss.as>
      ...
    </properties>

2) Make sure everything builds OK after upgrade.  Fix the compilation errors
   if found.
3) Make sure everything runs OK after upgrade.  It will probably fail because
   of the changes in the XML schema of some subsystems.  Compare the difference
   between EDG 6 configuration and AS 7 configuration, and replace the
   offending subsystem's configuration with the new one.

    $ diff -uN dist-dir/src/main/resources/standalone/configuration/standalone.xml jboss-as/build/src/main/resources/standalone/configuration/standalone.xml 

4) If it still does not work, there might have been some changes in module
    versions and metadata, which require more in-depth review.

