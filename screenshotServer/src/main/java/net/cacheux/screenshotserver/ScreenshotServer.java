package net.cacheux.screenshotserver;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint("PrivateApi")
public class ScreenshotServer {
    private static final String TAG = ScreenshotServer.class.getSimpleName();
    private static final int DEFAULT_PORT = 57000;

    private final IInterface displayService;

    private int port;

    private ServerSocket serverSocket;
    private AtomicBoolean running = new AtomicBoolean(false);

    private ScreenshotServer(int port) {
        this.port = port;

        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method getService = Class.forName("android.os.ServiceManager")
                    .getDeclaredMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, "display");
            Method asInterface = Class.forName("android.hardware.display.IDisplayManager$Stub")
                    .getMethod("asInterface", IBinder.class);
            displayService = (IInterface) asInterface.invoke(null, binder);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "Error getting Android services", e);
            throw new IllegalStateException("Error getting Android services", e);
        }
    }

    private void startServer() {
        Log.i(TAG, "Starting ScreenshotServer on port " + port);

        running.set(true);
        try {
            serverSocket = new ServerSocket(port);
            do {
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThread.start();
            } while (running.get());
        } catch (IOException e) {
            Log.e(TAG, "Error with server socket", e);
        }
    }

    private void stopServer() {
        Log.i(TAG, "Stopping ScreenshotServer");

        running.set(false);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
        }
    }

    public static void main(String[] args) {
        File pidFile = new File("/data/local/tmp/screenshotServer.pid");

        if (pidFile.exists()) {
            pidFile.delete();
        }

        try (FileWriter fileWriter = new FileWriter(pidFile)) {
            fileWriter.write(String.valueOf(android.os.Process.myPid()));
        } catch (IOException e) {
            Log.e(TAG, "Error writing pid file", e);
        }

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error reading port number", e);
            }
        }

        final ScreenshotServer server = new ScreenshotServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stopServer();
            }
        });
        server.startServer();
    }

    private class ClientThread extends Thread {
        private Socket socket;

        ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Bitmap bitmap = takeScreenshot();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, socket.getOutputStream());
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error with client socket", e);
            }
        }

        private Bitmap takeScreenshot() {
            Rect screenSize = getScreenSize();

            try {
                return (Bitmap) Class.forName("android.view.SurfaceControl")
                        .getMethod("screenshot", int.class, int.class)
                        .invoke(null, screenSize.width(), screenSize.height());
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Error taking screenshot", e);
                throw new IllegalStateException("Error taking screenshot", e);
            }
        }

        private Rect getScreenSize() {
            try {
                Object displayInfo = displayService.getClass()
                        .getMethod("getDisplayInfo", int.class)
                        .invoke(displayService, 0);
                Class<?> cls = displayInfo.getClass();
                int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
                int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
                return new Rect(0, 0, width, height);
            } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Error getting screen info", e);
                throw new IllegalStateException("Error getting screen info", e);
            }
        }
    }
}
