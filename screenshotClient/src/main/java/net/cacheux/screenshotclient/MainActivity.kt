package net.cacheux.screenshotclient

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ScreenshotClient"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        screenshotButton.setOnClickListener(::onScreenshotButton)
    }

    private fun onScreenshotButton(view: View) {
        thread {
            val bmp = retrieveScreenshot()
            if (bmp != null) {
                runOnUiThread {
                    screenshotImage.setImageBitmap(bmp)
                }
            }
        }
    }

    private fun retrieveScreenshot() : Bitmap? {
        try {
            val socket = Socket(InetAddress.getLocalHost(), 57000)
            return BitmapFactory.decodeStream(socket.getInputStream())
        } catch (e: IOException) {
            Log.e(TAG, "Connexion error", e)
            toast(R.string.connexion_error)
        }
        return null
    }

    private fun toast(message: Int) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
