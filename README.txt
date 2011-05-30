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

    $ cd dist-dir/target/jboss-datagrid-<version>/bin
    $ sh standalone.sh

    At this point you should be able to connect to 127.0.0.1:11222.

NOTE: I couldn't find a way to create a new profile like 'datagrid', so I 
      simply overwrote the standalone profile.  The configuration files
      specific to Infinispan is located at standalone/configuration.

TO-DO
=====
* Deploy REST server
* Make the configuration for the AS7's domain mode
  (Currently only for standalone mode)
* Consider consolidating the current configuration files into standalone.xml
* Figure out how to introduce a new profile 'datagrid' instead of overwriting
  'standalone'
* Rebrand the distribution (AS -> Data Grid)
* Remove unused modules

