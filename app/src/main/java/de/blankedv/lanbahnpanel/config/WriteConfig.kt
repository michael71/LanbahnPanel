package de.blankedv.lanbahnpanel.config

import java.io.FileWriter
import java.io.StringWriter
import android.os.Environment
import android.util.Log
import android.util.Xml
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils
import java.io.File

/**
 * WriteConfig - Utility turnout save Panel Config
 *
 * @author Michael Blank
 *
 * @version 1.0
 */

object WriteConfig {

    /**
     * writeConfigToXML
     *
     * saves all PanelElements (including deducted elements) turnout an XML file
     *
     * @param
     * @return true, if succeeds - false, if not.
     */

    private val DEBUG_WRITE = false

    fun locosToXMLFile(): Boolean {

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e(TAG, "external storage not writeable!")
            return false
        }

        var fWriter: FileWriter? = null
        try {

            fWriter = FileWriter(
                    Environment.getExternalStorageDirectory().toString()
                            + DIRECTORY + FNAME_LOCOS_FILE)

            fWriter.write(writeLocosXml())
            fWriter.flush()

            if (DEBUG or DEBUG_WRITE)
                Log.d(TAG, "Config File " + FNAME_LOCOS_FILE + " saved! ")

        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
            return false
        } finally {

            fWriter?.close()

        }

        return true
    }


    /**
     * writeLocosXml
     *
     * make xml string from all PanelElements (including deducted elements)
     *
     */
    private fun writeLocosXml(): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()

        try {
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.text("\n")
            serializer.startTag("", "locos")
            serializer.text("\n")
            for (l in locolist) {
                if (DEBUG) Log.d(TAG, "writing loco ")
                serializer.startTag("", "loco")
                serializer.attribute("", "adr", "" + l.getAdr())
                serializer.attribute("", "name", "" + l.name)
                serializer.attribute("", "mass", "" + l.mass)
                serializer.attribute("", "vmax", "" + l.vmax)
                serializer.endTag("", "loco")
                serializer.text("\n")
                if (l.lbm != null) {
                    WriteBitmap.save(l.name, l.lbm)
                }
            }
            serializer.endTag("", "locos")
            serializer.text("\n")
            serializer.endDocument()
            return writer.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
