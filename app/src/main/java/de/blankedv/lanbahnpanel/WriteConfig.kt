package de.blankedv.lanbahnpanel

import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter
import android.os.Environment
import android.util.Log
import android.util.Xml
import de.blankedv.lanbahnpanel.model.*

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

    fun writeToXML(): Boolean {

        val mExternalStorageWriteable: Boolean
        val state = Environment.getExternalStorageState()

        when (state) {
            Environment.MEDIA_MOUNTED ->
                // We can read and write the media
                mExternalStorageWriteable = true
            Environment.MEDIA_MOUNTED_READ_ONLY ->
                // We can only read the media
                mExternalStorageWriteable = false
            else ->
                // Something else is wrong.
                mExternalStorageWriteable = false
        }

        if (mExternalStorageWriteable) {

            /* no longer used. we don't change the original config file!!!
	  		try {

				File dir = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY);
				dir.mkdir(); // make DIRECTORY - if needed
				File from = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY + configFilename);
				String suffix = Utils.getDateTime();
				File to = new File(Environment.getExternalStorageDirectory(),
						LOCAL_DIRECTORY + configFilename + "." + suffix);
				from.renameTo(to);
			} catch (Exception e) {
				Log.e(TAG,
						"Error in renaming old config file: " + e.getMessage());
			}  */


            var fWriter: FileWriter? = null
            try {
                val suffix = Utils.dateTime
                fWriter = FileWriter(
                        Environment.getExternalStorageDirectory().toString() + "/"
                                + DIRECTORY + configFilename + "." + suffix)
                fWriter.write(writeXml())
                fWriter.flush()
                fWriter.close()

                if (DEBUG)
                    Log.d(TAG, "Config File $configFilename.$suffix saved! ")
                configHasChanged = false // reset flag

            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
                return false
            } finally {
                if (fWriter != null) {
                    try {
                        fWriter.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "could not close output file!")
                    }

                }
            }
        } else {
            Log.e(TAG, "external storage not writeable!")
            return false
        }
        return true
    }

    /**
     * writeConfigToXML
     *
     * saves all PanelElements (including deducted elements) turnout an XML file
     *
     * @param
     * @return true, if succeeds - false, if not.
     */
    private fun writeXml(): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()

        try {
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.text("\n")
            serializer.startTag("", "layout-config") // namespace ="" always
            serializer.text("\n")
            serializer.startTag("", "panel")
            serializer.attribute("", "name", panelName)
            serializer.text("\n")

            // now write all panel elements to the file
            for (pe in panelElements) {
                if (DEBUG)
                    Log.d(TAG, "writing panel element " + pe.toString())
                serializer.startTag("", pe.type)
                if (DEBUG)
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
                if (DEBUG)
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
                if (DEBUG)
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
