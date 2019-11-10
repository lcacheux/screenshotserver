#!/bin/sh
if [ -f /data/local/tmp/screenshotServer.pid ]; then kill -2 `cat /data/local/tmp/screenshotServer.pid` && rm /data/local/tmp/screenshotServer.pid; fi
