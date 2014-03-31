#/bin/bash

cp="target/combinedLearner-1.2-SNAPSHOT.jar:.:dependencies/*"
javaOpts="-Djava.library.path=lib/"
srcdir=csvfilee
dstdir=csvfile

srcdirtest=csvfilee
dstdirtest=csvtest

export LD_LIBRARY_PATH=`pwd`
java ${javaOpts} -classpath ${cp} testsimulator.Demo
