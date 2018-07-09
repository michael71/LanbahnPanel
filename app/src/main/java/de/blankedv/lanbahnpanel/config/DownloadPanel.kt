package de.blankedv.lanbahnpanel.config

import android.os.Environment
import android.util.Log
import de.blankedv.lanbahnpanel.model.*

import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.FileWriter
import java.io.IOException
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class DownloadPanel(private val url: String) {

    fun run(): Pair<Boolean, String> {
        Log.d(TAG, "DownloadPanel(url).run()")
        try {
            val content = URL(url).readText()

            // determine filename
            val fileName = getFilename(content)

            // copy the file to external storage storage
            var fWriter: FileWriter? = null
            try {
                fWriter = FileWriter(Environment.getExternalStorageDirectory().toString()
                        + DIRECTORY + fileName)
                fWriter.write(content)
                fWriter.flush()
            } catch (e: IOException) {
                Log.e(TAG, "DownloadPanel WRITE ERROR: " + e.message)
                return Pair(false, "WRITE: " + e.message)
            } finally {
                fWriter?.close()
            }
            return Pair(true, fileName)
        } catch (e: IOException) {
            Log.e(TAG, "DownloadPanel ERROR: " + e.message)
            return Pair(false, "" + e.message)
        }
    }

    private fun getFilename(filecontent: String): String {
        var fName = FNAME_PANEL_FROM_SERVER   // default name, if fName not found in panel

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val mystream = InputSource(StringReader(filecontent))
        val doc = builder.parse(mystream)
        val root = doc.documentElement ?: return FNAME_PANEL_FROM_SERVER

        fName = parseAttribute(root, "filename")
        if (fName.endsWith(".xml")) {
            return fName
        } else {
            return FNAME_PANEL_FROM_SERVER
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