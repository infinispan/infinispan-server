HOW TO BUILD
============

1) Check out the source code.
2) Build EDG:

    $ mvn clean install

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
To upgrade the current JBoss AS build to a newer version, you have to copy
the 'jboss-as-build module' to the 'thirdparty' directory.

1) Visit https://github.com/jbossas/jboss-as
2) Click the 'Downloads' button
3) Download the desired download package (e.g. 7.0.0.CR1) by clicking the
   version in the 'download packages' section.
5) Unzip the downloaded package.  You will find a directoy
   'jbossas-jboss-as-xxxxxx'.
6) Copy the 'build' directory to 'thirdparty' with proper name:

    $ cd jbossas-jboss-as-xxxxxx
    $ cp -R build <EDG6_trunk>/thirdparty/as-build-7.0.0.CR1

7) Modify the parent POM of EDG 6 to reflect the change.

    <properties>
      ...
      <version.org.jboss.as>7.0.0.CR1</version.org.jboss.as>
      ...
    </properties>

8) Make sure everything builds OK after upgrade.  Fix the compilation errors
   if found.
9) Make sure everything runs OK after upgrade.  It will probably fail because
   of the changes in the XML schema of some subsystems.  Compare the difference
   between EDG 6 configuration and AS 7 configuration, and replace the
   offending subsystem's configuration with the new one.

    $ diff -uN dist-dir/src/main/resources/standalone/configuration/standalone.xml thirdparty/as-build-7.0.0.Beta3/src/main/resources/standalone/configuration/standalone.xml 

10) If it still does not work, there might have been some changes in module
    versions and metadata, which require more in-depth review.

    If it works, commit the changes:

     $ svn add thirdparty/as-build-7.0.0.CR1
     $ svn commit

