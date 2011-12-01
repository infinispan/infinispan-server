HOW TO BUILD
============

1) Check out the source code.
2) Run 

    $ git submodule init
    $ git submodule update

   to initialize your local configuration file, and to fetch the EDG Quickstart submodule
   
2) Check versions/pom.xml for the versions of the external components you want to include (AS7, Infinispan).
   Make sure you have the artifacts for those components available in your Maven repository (local or remote).

3) Build EDG:

    $ mvn clean package

3) Run EDG:

    $ rm -fr ~/edg
    $ cp -R dist-dir/target/jboss-datagrid-<version> ~/edg
    $ cd ~/edg/bin
    $ ./standalone.sh

NOTE: We copy the distribution directory to a place independent from the
      build so that any modifications made are not lost during the build.

    At this point you should be able to connect to:

      127.0.0.1:11222 (Hot Rod server)
      127.0.0.1:11211 (Memcached)
      http://127.0.0.1:8080/ (REST server)

HOW TO UPGRADE AS 7.1
=====================
JBoss AS 7.1 is included in the build via a BOM, referenced in versions/pom.xml and the modules. 

DEBUGGING
=========

To enable remote debugging of the standalone server or the domain controller, uncomment the appropriate line in bin/standalone.conf or bin/domain.conf respectively.
To enable remote debugging of the domain hosts, add the following stanza to the <jvm> element declaration in domain/configuration/host.xml

    <jvm-options>
        <option value="-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"/>
    </jvm-options> 

Make sure that different hosts use different ports (the address attribute above).
