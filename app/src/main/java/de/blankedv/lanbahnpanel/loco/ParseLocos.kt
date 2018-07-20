package de.blankedv.lanbahnpanel.loco

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import de.blankedv.lanbahnpanel.model.*

object ParseLocos {

    /**
     *
     * read all Locos from a configuration (XML) file results will be put into
     * global variable "locos"
     *
     * @param context
     * @return true, if succeeds - false, if not.
     */
    fun readLocosFromFile(context: Context, filename: String) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder

        //		SharedPreferences prefs = PreferenceManager
        //				.getDefaultSharedPreferences(context);
        //		locosFilename = prefs.getString(KEY_LOCOS_FILE, DEMO_LOCOS_FILE);

        try {
            builder = factory.newDocumentBuilder()
        } catch (e1: ParserConfigurationException) {
            Log.e(TAG, "ParserLocosException Exception - " + e1.message)
            Toast.makeText(context, "Parser Locos Exception - check file:$filename", Toast.LENGTH_LONG)
                    .show()
            return
        }

        val doc: Document

        val mExternalStorageAvailable: Boolean
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            // We can read and write the media
            mExternalStorageAvailable = true
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            // We can only read the media
            mExternalStorageAvailable = true
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = false
        }

        if (mExternalStorageAvailable) {
            try {
                val f = File(Environment.getExternalStorageDirectory().toString()
                        + "/" + DIRECTORY + filename)
                Log.d(TAG, "Loading Loco config file: " + Environment.getExternalStorageDirectory()
                        + "/" + DIRECTORY + filename)
                val fis: FileInputStream
                var demoIs: InputStream? = null
                if (!f.exists()) {
                    Toast.makeText(
                            context,
                            "No Loco Config file found (" + DIRECTORY
                                    + filename + ") - using " + DEMO_LOCOS_FILE,
                            Toast.LENGTH_SHORT).show()
                    demoIs = context.assets.open(DEMO_LOCOS_FILE)
                    Log.e(TAG, "No Loco Config file found (" + DIRECTORY
                            + filename + ") - using" + DEMO_LOCOS_FILE)
                    try {
                        doc = builder.parse(demoIs)
                        parseDoc(doc)
                    } catch (e: SAXException) {
                        Log.e(TAG, "SAX Exception - " + e.message)
                    }

                } else {
                    fis = FileInputStream(f)
                    try {
                        doc = builder.parse(fis)
                        parseDoc(doc)
                    } catch (e: SAXException) {
                        Log.e(TAG, "SAX Exception - " + e.message)
                    }

                }
                if (demoIs != null) {
                    demoIs.close()
                    copyDemoFile(context)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.d(TAG, "loco config loaded from $filename")
        } else {

            Toast.makeText(context, "ERROR:External storage not readable",
                    Toast.LENGTH_LONG).show()

        }

        return
    }

    private fun parseDoc(doc: Document) {
        // assemble new ArrayList of tickets.
        //ArrayList<Loco> ls = new ArrayList<Loco>();
        locolist = ArrayList<Loco>()
        var items: NodeList
        val root = doc.documentElement

        items = root.getElementsByTagName("locolist")

        locoListName = parseName(items.item(0))
        if (DEBUG)
            Log.d(TAG, "config: " + items.length + " locolists, name="
                    + locoListName)

        // look for Locos
        items = root.getElementsByTagName("loco")
        if (DEBUG)
            Log.d(TAG, "config: " + items.length + " locos")
        for (i in 0 until items.length) {
            val l = parseLoco(items.item(i))
            if (l != null) {
                locolist.add(l)
            }
        }

        return
    }

    private fun parseLoco(item: Node): Loco? {

        val l = Loco()
        l.adr = INVALID_INT
        l.mass = 3 //default
        l.name = ""
        l.vmax = 160

        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "name") {
                l.name = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "adr") {
                val adr = Integer.parseInt(theAttribute.nodeValue)
                if (adr >= 1 && adr <= 111) {
                    l.adr = adr
                } else {
                    Log.e(TAG, "ParseLoco: Error in loco adr. $adr is invalid. Setting adr=1.")
                }
            } else if (theAttribute.nodeName == "mass") {
                val mass = Integer.parseInt(theAttribute.nodeValue)
                if (mass >= 1 && mass <= 12) {
                    l.mass = mass
                } else {
                    if (mass > 12) l.mass = 12
                    if (mass < 1) l.mass = 1
                    Log.e(TAG, "ParseLoco: Error in loco mass. $mass is invalid.")
                }
            } else if (theAttribute.nodeName == "vmax") {
                val vmax = Integer.parseInt(theAttribute.nodeValue)
                if (vmax >= 40 && vmax <= 300) {
                    l.vmax = vmax
                } else {
                    if (vmax > 300) l.vmax = 300
                    if (vmax < 40) l.vmax = 40
                    Log.e(TAG, "ParseLoco: Error in loco maximum speed. $vmax is invalid.")
                }
            } else {
                if (DEBUG)
                    Log.e(TAG,
                            "ParseLoco: unknown attribute " + theAttribute.nodeName
                                    + " in config file")
            }
        }
        return if (l.adr !== INVALID_INT) {
            l
        } else {
            null
        }

    }

    private fun parseName(item: Node): String {
        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            if (DEBUG)
                Log.d(TAG, theAttribute.nodeName + "="
                        + theAttribute.nodeValue)

            if (theAttribute.nodeName == "name") {
                return theAttribute.nodeValue
            }
        }
        return ""
    }

    private fun copyDemoFile(context: Context) {
        val `in`: InputStream
        val out: OutputStream
        if (DEBUG) Log.d(TAG, "copying $DEMO_LOCOS_FILE to dir:$DIRECTORY")
        try {
            `in` = context.assets.open(DEMO_LOCOS_FILE)
            out = FileOutputStream(
                    Environment.getExternalStorageDirectory().toString() + "/"
                            + DIRECTORY   // DIR.. contains trailing slash

                            + DEMO_LOCOS_FILE)
            copyFile(`in`, out)
            `in`.close()
            out.close()


        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy asset file: " + DEMO_LOCOS_FILE + " "
                    + e.message)
        }

    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read = `in`.read(buffer)

        while (read > 0) {
            out.write(buffer, 0, read)
            read = `in`.read(buffer)
        }

    }

}
