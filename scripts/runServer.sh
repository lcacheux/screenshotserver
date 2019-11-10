#!/bin/sh
adb shell << EOF
CLASSPATH=/data/local/tmp/screenshotServer.jar app_process /system/bin net.cacheux.screenshotserver.ScreenshotServer $@ &
exit
EOF