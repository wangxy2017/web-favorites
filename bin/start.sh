#!/bin/bash
nohup java -Xms512m -Xmx512m -Xmn256m -jar web-favorites.jar > /dev/null 2>&1 &
echo "started!"