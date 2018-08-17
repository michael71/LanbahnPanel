package de.blankedv.lanbahnpanel.config

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

import de.blankedv.lanbahnpanel.model.*


/**
 * WriteBitmap to External Storage directory
 *
 * @author Michael Blank
 *
 * @version 1.0
 */


object WriteBitmap {


    fun save(name: String, bm: Bitmap?) {
        val filename = Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY + name + ".png"
        var out: FileOutputStream? = null
        if (DEBUG) Log.d(TAG, "writing loco bitmap: $filename")
        try {
            out = FileOutputStream(filename)
            bm!!.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (e: Exception) {
            Log.e(TAG, "ERROR writing loco bitmap: " + e.message)
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                Log.e(TAG, "ERROR writing loco bitmap: " + e.message)
            }

        }
    }


}

