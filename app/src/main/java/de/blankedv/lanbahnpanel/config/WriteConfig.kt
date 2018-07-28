package de.blankedv.lanbahnpanel.config

import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter
import android.os.Environment
import android.util.Log
import android.util.Xml
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils

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

    fun toXMLFile(): Boolean {

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e(TAG, "external storage not writeable!")
            return false
        }

        var fWriter: FileWriter? = null
        try {
            panelVersion = Utils.dateTime
            fWriter = FileWriter(
                    Environment.getExternalStorageDirectory().toString()
                            + DIRECTORY + configFilename + "." + panelVersion)

            fWriter.write(writeXml())
            fWriter.flush()

            if (DEBUG or DEBUG_WRITE)
                Log.d(TAG, "Config File $configFilename.$panelVersion saved! ")
            configHasChanged = false // reset flag

        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
            return false
        } finally {

            fWriter?.close()

        }

        return true
    }

    /**
     * writeXml
     *
     * make xml string from all PanelElements (including deducted elements)
     *
     */
    private fun writeXml(): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()

        try {
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.text("\n")
            serializer.startTag("", "layout-config") // namespace ="" always
            serializer.attribute("", "filename", configFilename)
            serializer.text("\n")
            serializer.startTag("", "panel")
            serializer.attribute("", "name", panelName)
            serializer.attribute("", "protocol", panelProtocol)
            serializer.attribute("", "panelStyle", panelStyle)
            serializer.attribute("", "panelVersion", panelVersion)
            serializer.text("\n")


            // now write all panel elements to the file
            for (pe in panelElements) {
                if (DEBUG_WRITE)
                    Log.d(TAG, "writing panel element " + pe.toString())
                serializer.startTag("", pe.type)
                if (DEBUG_WRITE)
                    Log.d(TAG, " type=" + pe.type)
                if (pe.name.length > 0) {
                    serializer.attribute("", "name", "" + pe.name)
                }

                serializer.attribute("", "x", "" + pe.x)
                serializer.attribute("", "y", "" + pe.y)
                if (pe.x2 != INVALID_INT) { // save only valid attributes
                    serializer.attribute("", "x2", "" + pe.x2)
                    serializer.attribute("", "y2", "" + pe.y2)
                }
                if (pe.xt != INVALID_INT) {
                    serializer.attribute("", "xt", "" + pe.xt)
                    serializer.attribute("", "yt", "" + pe.yt)
                }
                if (pe.adr != INVALID_INT) {
                    serializer.attribute("", "adr", "" + pe.adr)
                }

                serializer.endTag("", pe.type)
                serializer.text("\n")
            }

            // write the routes
            for (rt in routes) {
                if (DEBUG_WRITE)
                    Log.d(TAG, "writing routes " + rt.toString())
                serializer.startTag("", "route")

                serializer.attribute("", "id", "" + rt.id)
                serializer.attribute("", "btn1", "" + rt.btn1)
                serializer.attribute("", "btn2", "" + rt.btn2)
                serializer.attribute("", "route", "" + rt.routeString)
                serializer.attribute("", "sensors", "" + rt.sensorsString)
                serializer.attribute("", "offending", "" + rt.offendingString)

                serializer.endTag("", "route")
                serializer.text("\n")
            }

            // write the composite routes
            for (rt in compRoutes) {
                if (DEBUG_WRITE)
                    Log.d(TAG, "writing routes " + rt.toString())
                serializer.startTag("", "comproute")

                serializer.attribute("", "id", "" + rt.id)
                serializer.attribute("", "btn1", "" + rt.btn1)
                serializer.attribute("", "btn2", "" + rt.btn2)
                serializer.attribute("", "routes", "" + rt.routesString)

                serializer.endTag("", "comproute")
                serializer.text("\n")
            }

            serializer.endTag("", "panel")
            serializer.text("\n")
            serializer.endTag("", "layout-config")
            serializer.endDocument()
            return writer.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

}
