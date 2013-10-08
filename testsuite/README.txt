Running the testsuite
---------------------

  mvn clean verify (runs all tests)

Running a subset of the testsuite
---------------------------------
Subsets of the testsuite are specified by profiles that switch off running of certain test classes or the whole surefire plugin executions.
Currently these subsets are predefined:

  -P suite.client                    (Client tests, all local/dist/repl cachemode)
  -P suite.client.{local|dist|repl}  (Client tests, only {local|dist|repl} cachemode)
  -P suite.examples                  (Example config tests)
  -P suite.leveldb                   (LevelDB cache store tests - the whole suite.client with leveldb configs + additional custom tests)

  -P suite.others                    (Tests that do not belong to any of the suites above. Useful when running a single test that's outside
                                      of any pre-defined group)

Runnins with specific server zip distribution
---------------------------------------------

If you specify -Dzip.dist=path/to/distro.zip the test server directories target/server/node* will be based on the contents of this zip file.


Running specific test 
---------------------
When running only a specific test, it's important to realize that by default, there are multiple executions of the maven-surefire-plugin defined 
and therefore the test might be executed multiple times even if it's specified via -Dtest= option. e.g.

  mvn clean verify -Dtest=org/infinispan/server/test/client/hotrod/HotRodRemoteCacheTest#testPut
  will run the testPut method three times, each time in different clustering mode (local/dist/repl)

so besides the -Dtest= directive it's useful to specify also the most specific suite for the given test:

  mvn clean verify -P suite.client.local -Dtest=org/infinispan/server/test/client/hotrod/HotRodRemoteCacheTest#testPut
  will run the testPut method only once for the local cache mode.

Running tests for specific client 
---------------------------------
This is controlled by following profiles

  -P client.rest      (REST client)
  -P client.memcached (Memcached client)
  -P client.hotrod    (Hot Rod client)

Client side logging
-------------------

The testsuite uses Log4j for logging and the logging config is in src/test/resources/log4j.xml
The file output goes by default to file infinispan-server.log 

Server side logging
-------------------

The server logs will be stored in the standard location of the test distributions:

  target/server/node1/standalone/log/server.log
  target/server/node2/standalone/log/server.log
  target/server/node3/standalone/log/server.log

Setting of the server side loglevels comming soon (see https://issues.jboss.org/browse/JBQA-8381)

LevelDB specifics
-----------------
properties:

  leveldb.compression - sets compression type, allowed values: SNAPPY, NONE
  leveldb.impl        - sets implementation type, allowed values: AUTO, JAVA, JNI
  leveldb.patch       - used with -Dzip.dist. Patches the zip distribution with dependencies of leveldb cache store taken from upstream build.
  