#!/bin/sh
DIRNAME=`dirname "$0"`
BASEDIR=$DIRNAME/..
MODULES=`find $BASEDIR/dist-dir/target -name 'module.xml'`
$DIRNAME/moduleTools.py $@ $BASEDIR/dist-dir/src/main/resources/standalone/configuration/standalone.xml $MODULES

# moddep.sh | tr . / |  | xargs rm -rf
# find . -type d -empty -delete
