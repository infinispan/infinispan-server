JBoss JDG Maven Repository
==========================

This archive contains Maven repository artifacts for JBoss Data Grid 6.0.0.  
 
Installation (Option 1) - Local File System
--------------------------------

For initial testing in a small team, the repository can be extracted to 
a directory on the local file system.

    unzip jboss-datagrid-maven-repository-6.0.0.zip

This will create a Maven repository in a directory called "jboss-datagrid-maven-repository-6.0.0".

Make a note of the both location of this directory for later use.

 
Installation (Option 2) - Apache Web Server
--------------------------------
 
To use the repository in a multi-user environment, the repository can be installed 
in a standard webserver such as Apache httpd, or a Maven repository manager such as Nexus.
To install the repository in Apache, simply unzip the repository in a web accessible 
directory on the Apache server.

    unzip jboss-datagrid-maven-repository-6.0.0.zip

This will create a Maven repository in a directory called "jboss-datagrid-maven-repository-6.0.0".
Apache should then be configured to allow read access and directory browsing in this directory.

 
Installation (Option 3) - Maven Repository Manager
--------------------------------------------------

If you already use a repository manager, you can use it to host the JDG repository alongside 
your existing repositories.  Please refer to the documentation for your repository manager,
for example:

* [Apache Archiva](http://archiva.apache.org/)
* [JFrog Artifactory](http://www.jfrog.com/products.php)
* [Sonatype Nexus](http://nexus.sonatype.org/)
 
Maven Configuration
-------------------

In order to correctly use this repository, the Maven settings (settings.xml) will 
need to be updated.  A default settings.xml file is included with each Maven distribution 
in the "conf" directory.  The Maven user settings is normally found in the ".m2" sub-directory 
of the user's home directory.  For more information about configuring Maven, refer to the 
[Maven site](http://maven.apache.org/settings.html).

The URL of the repository will depend on where the 
repository is located (i.e. on the filesystem, web server etc).  A few example 
URLs are provided here:

* File system - file:///path/to/repo/jboss-datagrid-maven-repository-6.0.0
* Apache Web Server - http://intranet.acme.com/jboss-datagrid-maven-repository-6.0.0
* Nexus Repository Manager - https://intranet.acme.com/nexus/content/repositories/jboss-datagrid-maven-repository-6.0.0

An example Maven settings file (example-settings.xml) is included in the root directory of the Maven
repository zip file.  An excerpt containing the relevant portions of settings.xml is provided below.
More information about configuring your Maven  settings is available on the Apache Maven site.

 
    <settings>
      ...
      <profiles>
        ...
        <profile>
          <id>jboss-datagrid-repository</id>
          <repositories>
            <repository>
              <id>jboss-datagrid-repository</id>
              <name>JBoss Data Grid Maven Repository</name>
              <url>file:///path/to/repo/jboss-datagrid-maven-repository-6.0.0</url>
              <layout>default</layout>
              <releases>
                <enabled>true</enabled>
                <updatePolicy>never</updatePolicy>
              </releases>
              <snapshots>
                <enabled>false</enabled>
                <updatePolicy>never</updatePolicy>
              </snapshots>
            </repository>
          </repositories>
          <pluginRepositories>
            <pluginRepository>
              <id>jboss-datagrid-repository-group</id>
              <name>JBoss Data Grid Maven Repository</name>
              <url>file:///path/to/repo/jboss-datagrid-maven-repository-6.0.0</url>
              <layout>default</layout>
              <releases>
                <enabled>true</enabled>
                <updatePolicy>never</updatePolicy>
              </releases>
              <snapshots>
                <enabled>false</enabled>
                <updatePolicy>never</updatePolicy>
              </snapshots>
            </pluginRepository>
          </pluginRepositories>
        </profile>

      </profiles>

      <activeProfiles>
        <activeProfile>jboss-datagrid-repository</activeProfile>
      </activeProfiles>
      ...
    </settings>

 
