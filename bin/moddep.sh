#!/bin/sh
DIRNAME=`dirname "$0"`
BASEDIR=$DIRNAME/..
MODULES=`find $BASEDIR -name 'module.xml'`
$DIRNAME/moduleTools.py $@ $DIRNAME/../dist-dir/src/main/resources/standalone/configuration/standalone.xml $MODULES

# moddep.sh | tr . / | sed 's/\(.*\)/modules\/\1\/main/' | xargs rm -rf
# find . -type d -empty -delete
