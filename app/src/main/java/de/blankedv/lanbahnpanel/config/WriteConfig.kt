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

    fun toXMLFile(): Boolean {

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {999
            Log.e(TAG, "external storage not writeable!")
            return false
        }

        var fWriter: FileWriter? = null
        try {
            configFileVersion = Utils.dateTime
            fWriter = FileWriter(
                    Environment.getExternalStorageDirectory().toString()
                            + DIRECTORY + configFilename + "." + configFileVersion)

            fWriter.write(writeXml())
            fWriter.flush()

            if (DEBUG or DEBUG_WRITE)
                Log.d(TAG, "Config File $configFilename.$configFileVersion saved! ")
            configHasChanged = false // reset flag
999
            deleteOlderFiles()

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
            serializer.attribute("", "version", configFileVersion)
            serializer.text("\n")
            serializer.startTag("", "locolist")
            serializer.attribute("", "name", locoListName)
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
            serializer.endTag("", "locolist")
            serializer.text("\n")
            serializer.startTag("", "panel")
            serializer.attribute("", "name", panelName)
            serializer.attribute("", "protocol", panelProtocol)
            // wird nicht mehr benutzt !! serializer.attribute("", "panelStyle", panelStyle)
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
                // there can be a secondary address - but only if there is a "first" address
                if (pe.adr != INVALID_INT) {
                    if (pe.adr2 != INVALID_INT) {
                        serializer.attribute("", "adr", "" + pe.adr + "," + pe.adr2)
                    } else { // only a single Address
                        serializer.attribute("", "adr", "" + pe.adr)
                    }
                }
                // there can be a secondary invert setting - but only if there is a "first" invert setting
                if ((pe.invert != 0) || (pe.invert2 != 0)){
                    if (pe.invert2 != 0) {
                        serializer.attribute("", "inv", "" + pe.invert + "," +pe.invert2)
                    } else {
                        serializer.attribute("", "inv", "" + pe.invert)
                    }
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

    /*
	example for loco config:

	<locolist name="demo-loco-list">
	<loco adr="22" name="Lok22" mass="2"/><loco adr="97" name="SchönBB" mass="2"/>
	<loco adr="44" name="CSX4416" mass="4"/><loco adr="27" name="ET423-1" mass="2"/>
	</locolist>
	*/

    private fun deleteOlderFiles() {

        if ((Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
                and (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)) {
            // Something  is wrong
            Log.d(TAG, "cannot read ExternalStorage Directory ")
            return
        }

        val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY)
        //Log.d(TAG, "reading directory " + dir.absolutePath)

        var fileList = dir.list().filter { it.startsWith(configFilename) }
                .filter { !it.endsWith(".xml") }.sortedWith(naturalOrder())

        if (fileList.isNotEmpty()) {

            if (fileList.lastIndex > NUMBER_OF_FILES_TO_RETAIN) {
                for (i in 0..(fileList.lastIndex - NUMBER_OF_FILES_TO_RETAIN)) {
                    Log.d(TAG, "deleting ${fileList.get(i)}")
                    var file = File(dir.absolutePath + "/" + fileList.get(i))
                    file?.delete()
                }
            }
        } else {
            Log.d(TAG, "no old config files found")
        }
    }

}
