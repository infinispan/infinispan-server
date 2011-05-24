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

TO-DO
=====
* Add Infinispan (modules, configuration, ..)
* Port test suites (perhaps using Arquillian?)
* Remove unused modules
* Rebrand the distribution (AS -> EDG)

