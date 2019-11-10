Android Screenshot Server
=========================
This small application is a proof of concept for a way to retrieve screenshots from any application
on an Android device.

Please note that this can be a serious security hole and should NOT be used in real conditions.
To retrieve properly an Android device screen content, use the [MediaProjection] API.

How does it work
----------------
The Screenshot Server application work like a standard Java application instead of an Android
application. Therefore, it must be launched from the command line using adb. This is a small
server application which listen on the specified port on localhost (57000 by default), and make use
of hidden APIs to retrieve the screen content.

To keep the server running once the adb connection is closed, the __scripts__ directory contains
some Unix shell scripts to run it in background. The server must be launched using the shell user.
The _killServer.sh_ script must be copied to the device to be launched, but not _runServer.sh_.

Client applications which connect to the port will just retrieve the screenshot as a PNG file, then
the connexion will be closed. A small sample is provided in the __screenshotClient__ module.

Requirements
------------
- Android device with version 5.0 or above (API level 21). Note that since we use hidden APIs,
further versions of Android could remove them and make the application unusable.
- Android SDK installed on a computer, or a single adb executable available in the path.

[MediaProjection]: https://developer.android.com/reference/android/media/projection/MediaProjection