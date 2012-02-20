#!/bin/sh
DIRNAME=`dirname "$0"`
BASEDIR=$DIRNAME/..
MODULES=`find $BASEDIR/server/target -name 'module.xml'`
$DIRNAME/moduleTools.py $@ $BASEDIR/server/src/main/resources/standalone/configuration/standalone.xml $MODULES

# moddep.sh | tr . / | sed 's/$/\/main\/\*\*/' > trim-modules.txt
