#!/bin/bash
echo "Stopping..."
pid=`ps -ef | grep web-favorites.jar | grep -v grep | awk '{print $2}'`
if [ -n "$pid" ]
then
kill -9 $pid
fi
