package de.blankedv.lanbahnpanel.config

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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
import de.blankedv.lanbahnpanel.elements.*
import de.blankedv.lanbahnpanel.loco.Loco
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.LinearMath


/**
 * Parse Configuration from XML file
 * TODO review kotlin code
 *
 * @author mblank
 */
object ReadConfig {

    internal val DEBUG_PARSING = false


    /**
     *
     * read all PanelElements from a configuration (XML) file and add deducted
     * turnouts if needed. The results will be put into global ArrayList
     * "panelElements" turnouts and signals must be defined before sensors and
     * turnouts,signal and sensors must be defined before routes (because the
     * routes are dependent on these panel elements.)
     * read also a list of loco (addresses) which are used on this layout (from
     * the same config file)
     *
     * all dimension will be scaled with the global value of "prescale"
     *
     * @return true, if succeeds - false, if not.
     */
    fun readConfigFromFile(context: Context): String {

        val result: String

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            // We cannot read/write the media
            Log.e(TAG, "external storage not available or not writeable")
            Toast.makeText(context, "ERROR:External storage not readable",
                    Toast.LENGTH_LONG).show()
            return "ERROR:External storage not readable"
        }


        try {
            val f = File(Environment.getExternalStorageDirectory().toString()
                    + DIRECTORY + configFilename)
            // auf dem Nexus 7 unter /mnt/shell/emulated/0/lanbahnpanel
            val fis: FileInputStream
            if (!f.exists()) {
                Log.e(TAG, "config file=$configFilename not found, using demo data.")
                val demoIs = context.assets.open(DEMO_FILE)
                configHasChanged = true
                result = readXMLConfigFile(demoIs)
                demoIs.close()

                // create the folder for later use (if it does not exist already)
                val f2 = File(Environment.getExternalStorageDirectory().toString()
                        + "/" + DIRECTORY + "/")
                if (!f2.exists()) {
                    f2.mkdirs()
                    Log.e(TAG, "creating 'lanbahnpanel' folder")
                }
            } else {
                fis = FileInputStream(f)
                result = readXMLConfigFile(fis)

            }
            return result
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFound " + e.message)
            return "FileNotFound " + e.message
        } catch (e: IOException) {
            Log.e(TAG, "IOException " + e.message)
            return "IOException " + e.message
        }

    }

    private fun readXMLConfigFile(fis: InputStream): String {
        val factory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder

        try {
            builder = factory.newDocumentBuilder()
        } catch (e1: ParserConfigurationException) {
            Log.e(TAG, "ParserConfigException Exception - " + e1.message)
            return "ParserConfigException"
        }

        val doc: Document
        try {
            doc = builder.parse(fis)
            panelElements = parsePanelElements(doc)

            PanelElement.relocatePanelOrigin()
            routes = parseRoutes(doc) // can be done only after all panel
            // elements have been read
            Route.calcOffendingRoutes() // calculate offending routes
            compRoutes = parseCompRoutes(doc) // can be done only after routes
            // have been read

            Log.d(TAG, "a total of " + panelElements.size + " panel Elements have been read")

            locolist = parseDocForLocos(doc)
            Log.d(TAG, "locolist: " + locolist.size + " locos, name=$locoListName")

        } catch (e: SAXException) {
            Log.e(TAG, "SAX Exception - " + e.message)
            return "SAX Exception - " + e.message
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception - " + e.message)
            return "IO Exception - " + e.message
        } catch (e: Exception) {
            Log.e(TAG, "other Exception - " + e.message)
            return "other Exception - " + e.message
        }

        return ""
    }

    private fun parsePanelElements(doc: Document): ArrayList<PanelElement> {
        // assemble new ArrayList of tickets.
        val pes = ArrayList<PanelElement>()
        var items: NodeList
        val root = doc.documentElement ?: return pes

        items = root.getElementsByTagName("panel")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " panel")
        panelName = parsePanelDescription(items.item(0), "name")
        panelProtocol = parsePanelDescription(items.item(0), "protocol")
        configFileVersion = parsePanelDescription(items.item(0), "version")
        // wird nicht mehr benutzt !!! panelStyle = parsePanelDescription(items.item(0), "style")

        if (panelProtocol.isEmpty()) panelProtocol = "sx"  // make default

        // NamedNodeMap attributes = item.getAttributes();
        // Node theAttribute = attributes.items.item(i);

        // look for TrackElements - this is the lowest layer
        items = root.getElementsByTagName("track")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " track")
        for (i in 0 until items.length) {
            pes.add(parseTrack(items.item(i)))
        }

        // look for existing and known turnouts - on top of track
        items = root.getElementsByTagName("turnout")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " turnouts")
        for (i in 0 until items.length) {
            pes.add(parseTurnout(items.item(i)))
        }

        // look for signals - on top of track
        items = root.getElementsByTagName("signal")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " signals")
        for (i in 0 until items.length) {
            pes.add(parseSignal(items.item(i)))
        }


        if (prefs.getBoolean(KEY_DISCOVER_TURNOUTS_PREF,false)) {
            val newTurnouts = discoverTurnouts(pes)
            for (pe in newTurnouts) pes.add(pe)
        }

        // look for sensors
        // after tracks and turnouts !! important => on top of track

        items = root.getElementsByTagName("sensor")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " sensors")
        for (i in 0 until items.length) {
            pes.add(parseSensor(items.item(i)))
        }


        // after all other elements - on top of everything
        items = root.getElementsByTagName("routebutton")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " routebuttons")
        for (i in 0 until items.length) {
            pes.add(parseRouteButton(items.item(i)))
        }



        return pes
    }

    private fun parseTurnout(item: Node): TurnoutElement {

        val pe = TurnoutElement()
        val attributes = item.attributes
        var sxadr = INVALID_INT
        var sxbit = INVALID_INT
        var nbit = INVALID_INT

        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "name") {
                pe.name = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "x") {
                pe.x = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y") {
                pe.y = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "x2") {
                pe.x2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y2") {
                pe.y2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "xt") {
                pe.xt = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "yt") {
                pe.yt = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "adr") {
                pe.adr = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "inv") {
                pe.invert = getIntegerNodeValue(theAttribute)

                // turnout have either an lanbahn/dcc address ("adr") or a combination of
                // sxadr and sxbit (for ex. sxadr="98" sxbit="7") which will be compiled to
                // lanbahn address "987"

            } else if (theAttribute.nodeName == "sxadr") {
                sxadr = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "sxbit") {
                sxbit = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "nbit") {
                nbit = getIntegerNodeValue(theAttribute)  // TODO implement for multi-aspect signals
                pe.nbit = nbit
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file")
            }
        }
        if ((pe.adr == INVALID_INT) and (sxadr != INVALID_INT) and (sxbit != INVALID_INT)) {
            // calc from sx add a LanbahnSXPair for later storage
            var lbSxPair = LanbahnSXPair(INVALID_INT, sxadr, sxbit)
            if (nbit != INVALID_INT) {
                lbSxPair = LanbahnSXPair(INVALID_INT, sxadr, sxbit, nbit)
            }
            pe.adr = lbSxPair.lbAddr
        }

        if (DEBUG_PARSING) Log.d(TAG, "turnout: x=" + pe.x + " y=" + pe.y + " adr=" + pe.adr)
        return pe

    }

    private fun getIntegerNodeValue(a: Node): Int {
        // remove "." first to convert SX addresses like 72.1 to LB addr (=721)
        // remove whitespace also
        val s = a.nodeValue.replace(".", "").replace("\\s+".toRegex(), "")
        try {
            return Integer.parseInt(s)
        } catch (e: NumberFormatException) {
            return INVALID_INT
        }
    }

    private fun getIntegerNodeValueArray(a: Node): IntArray {
        // remove "." first to convert SX addresses like 72.1 to LB addr (=721)
        // remove whitespace also
        val s = a.nodeValue.replace(".", "").replace("\\s+".toRegex(), "")
        val sArr = s.split(",")
        var iArr = intArrayOf()
        for (s in sArr) {
            var n = INVALID_INT
            try {
                n = Integer.parseInt(s)
            } catch (e: NumberFormatException) {
            }
            iArr = iArr.plus(n)
        }
        return iArr

    }

    private fun parseSignal(item: Node): SignalElement {

        val pe = SignalElement()
        val attributes = item.attributes

        var sxadr = INVALID_INT
        var sxbit = INVALID_INT
        var nbit = INVALID_INT

        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "name") {
                pe.name = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "x") {
                pe.x = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y") {
                pe.y = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "x2") {
                pe.x2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y2") {
                pe.y2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "adr") {
                pe.adr = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "sxadr") {
                sxadr = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "sxbit") {
                sxbit = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "nbit") {
                pe.nbit = getIntegerNodeValue(theAttribute)  // TODO implement for multi-aspect signals
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file <signal>")
            }
        }
        if ((pe.adr == INVALID_INT) and (sxadr != INVALID_INT) and (sxbit != INVALID_INT)) {
            pe.adr = sxadr * 10 + sxbit   // lbaddr gets calculated from sx
        }
        if (DEBUG_PARSING) Log.d(TAG, "signal x=" + pe.x + " y=" + pe.y + " adr=" + pe.adr)
        return pe

    }

    private fun parseRouteButton(item: Node): RouteButtonElement {

        val pe = RouteButtonElement()
        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "name") {
                pe.name = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "x") {
                pe.x = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y") {
                pe.y = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "route") {
                pe.route = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "adr") {
                pe.adr = getIntegerNodeValue(theAttribute)
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file <routebutton>")
            }
        }

        return pe

    }

    private fun parsePanelDescription(item: Node, type: String): String {
        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            if (theAttribute.nodeName == type) {
                if (DEBUG_PARSING)
                    Log.d(TAG, theAttribute.nodeName + " : " + type + " value="
                            + theAttribute.nodeValue)
                return theAttribute.nodeValue
            }
        }
        return ""
    }

    private fun parseSensor(item: Node): SensorElement {
        // ticket node can be Incident oder UserRequest
        val pe = SensorElement()
        pe.x2 = INVALID_INT // to be able to distinguish between
        // different types of sensors (LAMP or dashed track)
        val attributes = item.attributes

        var sxadr = INVALID_INT
        var sxbit = INVALID_INT
        var nbit = INVALID_INT

        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "name") {
                pe.name = theAttribute.nodeValue
            } else if (theAttribute.nodeName == "x") {
                pe.x = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y") {
                pe.y = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "x2") {
                pe.x2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y2") {
                pe.y2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "adr") {
                val adrArray = getIntegerNodeValueArray(theAttribute)
                if (adrArray.size == 1) {
                    pe.adr = adrArray[0]
                } else if (adrArray.size >= 2) {
                    pe.adr = adrArray[0]
                    pe.adr2 = adrArray[1]
                }
            } else if (theAttribute.nodeName == "sxadr") {
                sxadr = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "sxbit") {
                sxbit = getIntegerNodeValue(theAttribute)
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file, <sensor>")
            }
        }

        if ((pe.adr == INVALID_INT) and (sxadr != INVALID_INT) and (sxbit != INVALID_INT)) {
            // calc from sx add a LanbahnSXPair for later storage
            val lbSxPair = LanbahnSXPair(INVALID_INT, sxadr, sxbit)
            pe.adr = lbSxPair.lbAddr
        }

        return pe

    }

    private fun getValue(s: String): Int {
        val b = java.lang.Float.parseFloat(s)
        return b.toInt()
    }

    private fun parseTrack(item: Node): PanelElement {
        // track is element of type "PanelElement" (=not active)
        val pe = PanelElement()
        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "x") {
                pe.x = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y") {
                pe.y = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "x2") {
                pe.x2 = getIntegerNodeValue(theAttribute)
            } else if (theAttribute.nodeName == "y2") {
                pe.y2 = getIntegerNodeValue(theAttribute)
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file")
            }
        }

        if (pe.x2 < pe.x) { // swap 1/2, x2 must always be >x
            var tmp = pe.x
            pe.x = pe.x2
            pe.x2 = tmp
            tmp = pe.y
            pe.y = pe.y2
            pe.y2 = tmp
        }
        return pe

    }

    private fun parseRoutes(doc: Document): ArrayList<Route> {
        // assemble new ArrayList of tickets.
        val myroutes = ArrayList<Route>()
        val items: NodeList
        val root = doc.documentElement

        // items = root.getElementsByTagName("panel");

        // look for routes - this is the lowest layer
        items = root.getElementsByTagName("route")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " routes")
        for (i in 0 until items.length) {
            val rt = parseRoute(items.item(i))
            if (rt != null)
                myroutes.add(rt)
        }

        return myroutes
    }

    private fun parseRoute(item: Node): Route? {
        // ticket node can be Incident oder UserRequest
        var id = INVALID_INT
        var btn1 = INVALID_INT
        var btn2 = INVALID_INT
        var route: String? = null
        var sensors: String? = null
        var offending = "" // not mandatory

        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "id") {
                id = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "btn1") {
                btn1 = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "btn2") {
                btn2 = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "route") {
                route = (theAttribute.nodeValue).replace(".", "")
                // in kotlin String.replace = string occurrances by string, NOT REGEX
            } else if (theAttribute.nodeName == "sensors") {
                sensors = (theAttribute.nodeValue).replace(".", "")
            } else if (theAttribute.nodeName == "offending") {
                offending = theAttribute.nodeValue
            } else {
                if (DEBUG_PARSING)
                    Log.d(TAG,
                            "unknown attribute " + theAttribute.nodeName
                                    + " in config file")
            }
        }

        // check for mandatory and valid input data
        if (id == INVALID_INT) {
            // missing info, log error
            Log.e(TAG, "missing id= info in route definition")
            return null
        } else if (btn1 == INVALID_INT) {
            Log.e(TAG, "missing btn1= info in route definition")
            return null
        } else if (btn2 == INVALID_INT) {
            Log.e(TAG, "missing btn2= info in route definition")
            return null
        } else if (route == null) {
            Log.e(TAG, "missing route= info in route definition")
            return null
        } else if (sensors == null) {
            Log.e(TAG, "missing sensors= info in route definition")
            return null
        } else {
            // everything is o.k.

            return Route(id, btn1, btn2, route, sensors, offending)
        }

    }

    private fun parseCompRoutes(doc: Document): ArrayList<CompRoute> {
        // assemble new ArrayList of tickets.
        val myroutes = ArrayList<CompRoute>()
        val items: NodeList
        val root = doc.documentElement

        // look for comp routes - this is the lowest layer
        items = root.getElementsByTagName("comproute")
        if (DEBUG) Log.d(TAG, "config: " + items.length + " comproutes")
        for (i in 0 until items.length) {
            val rt = parseCompRoute(items.item(i))
            if (rt != null)
                myroutes.add(rt)
        }

        return myroutes
    }

    private fun parseCompRoute(item: Node): CompRoute? {
        //
        var id = INVALID_INT
        var btn1 = INVALID_INT
        var btn2 = INVALID_INT
        var routes: String? = null

        val attributes = item.attributes
        for (i in 0 until attributes.length) {
            val theAttribute = attributes.item(i)
            // if (DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.nodeName == "id") {
                id = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "btn1") {
                btn1 = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "btn2") {
                btn2 = getValue(theAttribute.nodeValue)
            } else if (theAttribute.nodeName == "routes") {
                routes = (theAttribute.nodeValue).replace(".", "")
            } else {
                if (DEBUG_PARSING) {
                    Log.d(TAG, "unknown attribute " + theAttribute.nodeName + " in config file")
                }
            }
        }

        // check for mandatory and valid input data
        if (id == INVALID_INT) {
            // missing info, log error
            Log.e(TAG, "missing id= info in route definition")
            return null
        } else if (btn1 == INVALID_INT) {
            Log.e(TAG, "missing btn1= info in route definition")
            return null
        } else if (btn2 == INVALID_INT) {
            Log.e(TAG, "missing btn2= info in route definition")
            return null
        } else if (routes == null) {
            Log.e(TAG, "missing routes= info in route definition")
            return null
        } else {
            // everything is o.k.

            return CompRoute(id, btn1, btn2, routes)
        }

    }

    private fun discoverTurnouts(pes: ArrayList<PanelElement>): ArrayList<PanelElement> {
        // check for intersection of track, if new, add a turnout with unknown
        // lanbahn address

        val newPe = ArrayList<PanelElement>()

        for (i in pes.indices) {
            val p = pes[i]

            for (j in i + 1 until pes.size) {
                val q = pes[j]

                val panelelement = LinearMath.trackIntersect(p, q)

                if (panelelement != null) {
                    if (panelelement.type == "doubleslip") {
                        // do nothing in the meantime
                        // TODO implement for doubleslip a similar method as
                        // with turnout
                        // TODO currently doubleslip are two turnouts, separated
                        // by a single pixel
                        if (DEBUG_PARSING)
                            Log.d(TAG, "(i,j)=(" + i + "," + j
                                    + ") new? doubleslip found at x="
                                    + panelelement.x + " y=" + panelelement.y)

                    } else {
                        // there is an intersection with a turnout => make new
                        // turnout
                        if (DEBUG_PARSING)
                            Log.d(TAG, "(i,j)=(" + i + "," + j
                                    + ") new? turnout found at x="
                                    + panelelement.x + " y=" + panelelement.y
                                    + " xc=" + panelelement.x2 + " yc="
                                    + panelelement.y2 + " xt="
                                    + panelelement.xt + " yt="
                                    + panelelement.yt)

                        // check whether this turnout is already known
                        var known = false
                        for (e in pes) {
                            if (e.type == "turnout"
                                    && e.x == panelelement.x
                                    && e.y == panelelement.y) {
                                // at same position => match
                                known = true
                                break
                            }
                        }
                        if (!known) {
                            configHasChanged = true
                            newPe.add(TurnoutElement(panelelement))
                        }
                    }
                }

            }
        }
        return newPe
    }

    private fun parseDocForLocos(doc: Document): ArrayList<Loco> {
        //ArrayList<Loco> ls = new ArrayList<Loco>();
        var ll = ArrayList<Loco>()
        var items: NodeList
        val root = doc.documentElement

        items = root.getElementsByTagName("locolist")
        if ((items == null) or (items.length == 0)) {
            locoListName = "noName"
        } else {
            locoListName = parsePanelDescription(items.item(0), "name")
        }
        // look for Locos
        items = root.getElementsByTagName("loco")
        if (DEBUG)
            Log.d(TAG, "config: " + items.length + " locos")
        for (i in 0 until items.length) {
            val l = parseLoco(items.item(i))
            if (l != null) {
                ll.add(l)
            }
        }

        return ll
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
                if (vmax >= 30 && vmax <= 300) {
                    l.vmax = vmax
                } else {
                    if (vmax > 300) l.vmax = 300
                    if (vmax < 30) l.vmax = 30
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
}
