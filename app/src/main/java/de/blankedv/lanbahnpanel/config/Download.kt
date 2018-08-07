package de.blankedv.lanbahnpanel.config

import android.os.Environment
import android.util.Log
import de.blankedv.lanbahnpanel.model.*
import org.w3c.dom.Document

import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class Download(private val url: String) {

    fun run(): Pair<Boolean, String> {
        Log.d(TAG, "Download(url).run(), url=$url")
        try {
            val content = URL(url).readText()
            if (content.startsWith("ERROR", true)) {
                return Pair(false, content)
            }
            // determine filename
            val fileName = getFilename(content)
            if (fileName.startsWith("ERROR", true)) {
                // xml parsing error
                return Pair(false, fileName)
            }

            // check, if file with this name exists -> delete it.
            var oldfile = File(Environment.getExternalStorageDirectory().toString()
                    + DIRECTORY + fileName)
            if (oldfile.exists()) {
                if (oldfile.delete()) {
                    Log.d(TAG, "Download, old file Deleted :" + fileName);
                } else {
                    Log.d(TAG, "Download, old file not Deleted :" + fileName);
                }
            }

            // copy the file to external storage storage
            var fWriter: FileWriter? = null
            try {
                fWriter = FileWriter(Environment.getExternalStorageDirectory().toString()
                        + DIRECTORY + fileName)
                fWriter.write(content)
                fWriter.flush()
                Log.d(TAG, "Download, file written:" + fileName);
            } catch (e: IOException) {
                Log.e(TAG, "Download WRITE ERROR: " + e.message)
                return Pair(false, "WRITE: " + e.message)
            } finally {
                fWriter?.close()
            }
            return Pair(true, fileName)
        } catch (e: IOException) {
            Log.e(TAG, "Download ERROR: " + e.message)
            return Pair(false, "" + e.message)
        }
    }

    private fun getFilename(filecontent: String): String {
        var defaultFileName = FNAME_FROM_SERVER

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val mystream = InputSource(StringReader(filecontent))
        val doc : Document
        try {
            doc = builder.parse(mystream)
        } catch (e : SAXParseException) {
            Log.e(TAG,"could not parse download content")
            return "ERROR: SAXParseException"
        }
        val root = doc.documentElement ?: return FNAME_FROM_SERVER
        if (root.tagName.contains("loco")) {
            defaultFileName += "locolist_"
        } else {
            defaultFileName += "panel_"
        }
        val fName = parseAttribute(root, "filename")
        if (fName.endsWith(".xml")) {
            return fName
        } else {
            return defaultFileName
        }
    }

    private fun parseAttribute(item: Node, type: String): String {
        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            if (theAttribute.nodeName == type) {
                return theAttribute.nodeValue
            }
        }
        return ""
    }
}