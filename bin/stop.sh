#!/bin/bash

pid=`ps ax | grep "java -server -jar" | grep '[r]esth' | awk 'NR==1{print $1}' | cut -d' ' -f1`
if [ -n "${pid}" ]; then
  echo stopping restheart: process pid is $pid
  kill $pid
else
  echo restheart is not running
fi
