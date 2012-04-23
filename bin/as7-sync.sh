#!/bin/bash

DIR=`dirname $0`
JDG_HOME=`cd $DIR/..; pwd`
AS7_HOME=`cd $JDG_HOME/../jboss-as-7.x/; pwd`

# Remove old stuff
rm -rf $JDG_HOME/server/src/main/resources/modules

# Refresh from AS7
cp -r $AS7_HOME/build/src/main/resources/modules $JDG_HOME/server/src/main/resources/modules
cp $AS7_HOME/build/build.xml $JDG_HOME/server/jboss-as-build.xml

# Add to git
git add $JDG_HOME/server/src/main/resources/modules $JDG_HOME/server/jboss-as-build.xml
git commit

