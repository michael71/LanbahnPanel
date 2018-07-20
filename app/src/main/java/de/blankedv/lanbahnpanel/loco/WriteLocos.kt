package de.blankedv.lanbahnpanel.loco

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.util.Xml

import org.xmlpull.v1.XmlSerializer

import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter

import de.blankedv.lanbahnpanel.model.*


/**
 * WriteConfig - Utility to save Panel Config
 *
 * @author Michael Blank
 *
 * @version 1.0
 */


object WriteLocos {

    /**
     * writeConfigToXML
     *
     * saves all Locos to an XML file
     * file: Environment.getExternalStorageDirectory()+"/"+DIRECTORY+ locoConfigFilename
     * locos taken from "locos" variable
     * name of "loco-list" = locolistName
     *
     * old file copied to locoConfigFilename.xml.bak
     *
     * @param
     * @return true, if succeeds - false, if not.
     */

    fun writeToXML(): Boolean {


        val mExternalStorageWriteable: Boolean
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            // We can read and write the media
            mExternalStorageWriteable = true
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            // We can only read the media
            mExternalStorageWriteable = false
        } else {
            // Something else is wrong.
            mExternalStorageWriteable = false
        }

        if (mExternalStorageWriteable) {
            try {
                val dir = File(Environment.getExternalStorageDirectory(), DIRECTORY)

                dir.mkdir()  // make DIRECTORY - if needed
                val from = File(Environment.getExternalStorageDirectory(), DIRECTORY + locoConfigFilename)
                val suffix = "bak" //Utils.getDateTime();
                val to = File(Environment.getExternalStorageDirectory(), "$DIRECTORY$locoConfigFilename.$suffix")
                from.renameTo(to)
            } catch (e: Exception) {
                Log.e(TAG, "Error in renaming old loco config file: " + e.message)
            }

            var fWriter: FileWriter? = null
            try {
                val fname = Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY + locoConfigFilename
                if (DEBUG) Log.d(TAG, "writing locos to $fname")
                fWriter = FileWriter(fname)
                val locosAsXML = LocosToXMLString(locoListName)
                fWriter.write(locosAsXML)
                fWriter.flush()
                fWriter.close()

                if (DEBUG) Log.d(TAG, "Loco Config File saved!")
                locoConfigHasChanged = false // reset flag

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

    /*
	example file

	<loco-config>
	<locolist name="demo-loco-list">
	<loco adr="22" name="Lok22" mass="2"/><loco adr="97" name="SchönBB" mass="2"/>
	<loco adr="44" name="CSX4416" mass="4"/><loco adr="27" name="ET423-1" mass="2"/>
	</locolist></loco-config>
	 */
    private fun LocosToXMLString(name: String): String {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        try {
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.text("\n")
            serializer.startTag("", "loco-config")   // namespace ="" always
            serializer.text("\n")
            serializer.startTag("", "locolist")
            serializer.attribute("", "name", name)
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
                    saveLocoBitmap(l.name, l.lbm)
                }
            }
            serializer.endTag("", "locolist")
            serializer.text("\n")
            serializer.endTag("", "loco-config")
            serializer.endDocument()
            return writer.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    private fun saveLocoBitmap(name: String, bm: Bitmap?) {
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

