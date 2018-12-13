#!/bin/sh
set -x
jar=`ls /root/*.jar`
export LOGGING_PATH=/root/logs
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar ${jar} $1
