#!/bin/sh
set -x
gradle clean
gradle bootJar
cd build/libs
jar=`ls *.jar`
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar ${jar}
