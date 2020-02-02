#!/bin/bash

ps ux | grep "web-favorites.jar" | grep -v grep | grep -v stop.sh | cut -c 9-15 | xargs kill

echo "stopped!"
