package net.cacheux.screenshotserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.WindowManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.Socket;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ScreenshotServerTest {
    @Test
    public void testScreenshotServer() throws Exception {
        Socket socket = new Socket(InetAddress.getLocalHost(), 51000);
        Bitmap bitmap = BitmapFactory.decodeStream(socket.getInputStream());

        assertNotNull(bitmap);

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getRealSize(size);

        assertEquals(size.x, bitmap.getWidth());
        assertEquals(size.y, bitmap.getHeight());
    }
}
