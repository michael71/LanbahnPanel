package de.blankedv.lanbahnpanel.config

import android.os.Environment
import android.util.Log
import de.blankedv.lanbahnpanel.model.DIRECTORY
import de.blankedv.lanbahnpanel.model.FNAME_PANEL_FROM_SERVER
import de.blankedv.lanbahnpanel.model.TAG
import java.io.FileWriter
import java.io.IOException
import java.net.URL

class RequestPanel(val url : String) {

    fun run() : Pair<Boolean,String> {
        Log.d(TAG,"RequestPanel(url).run()")
        try {
            val content = URL(url).readText()
            // copy the file to external storage storage
            var fWriter: FileWriter? = null
            try {
                fWriter = FileWriter(Environment.getExternalStorageDirectory().toString()
                         + DIRECTORY + FNAME_PANEL_FROM_SERVER)
                fWriter.write(content)
                fWriter.flush()
            } catch (e : IOException) {
                Log.e(TAG, "RequestPanel WRITE ERROR: "+e.message)
                return Pair(false, "WRITE: "+e.message)
            } finally {
                fWriter?.close()
            }
            return Pair(true,content)
        } catch (e : IOException) {
            Log.e(TAG, "RequestPanel ERROR: "+e.message)
            return Pair(false, ""+e.message)
        }
    }
}