
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
@file:JvmName("Variables")

package de.blankedv.lanbahnpanel.model

import android.content.Context
import android.graphics.Rect
import de.blankedv.lanbahnpanel.elements.CompRoute
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.elements.Route
import de.blankedv.lanbahnpanel.railroad.RRConnectionThread
import java.util.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

// with file
var noWifiFlag = false


//var width: Int = 0
//var height:Int = 0

var selectedStyle: String = "UK" // German style or USS style
var selectedScale: String = "auto" // automatic scaling

var panelElements = ArrayList<PanelElement>()
var routes = ArrayList<Route>()
var compRoutes = ArrayList<CompRoute>()


var panelName = ""
var panelProtocol = ""
var panelVersion = ""

var drawAddresses = false
var drawAddresses2 = false
var flipUpsideDown = false  //display all panel element from "other side"
var saveStates: Boolean = false
var enableDiscoverTurnouts = false

// connection state
var client: RRConnectionThread? = null

var restartCommFlag = false

// put all messages which should be sent into this queue
val sendQ: BlockingQueue<String> = ArrayBlockingQueue(
        200)


@Volatile
var connString = ""

val DIRECTORY = "/lanbahnpanel/"
// with leading and trailing slash !!

var configFilename = "lb-panel1.xml"

val DEMO_FILE = "demo-panel.xml" // demo data in raw
// assets dir.

var configHasChanged = false // store info whether config
// has changed
// if true, then a new config file is written at the end of the Activity

//var zoomEnabled: Boolean = false
var scale = 1.0f // user selectable scaling of panel area

var xoff = (10 * prescale).toFloat()
var yoff = (50 * prescale).toFloat()


var enableEdit = false
var enableRoutes = false
// enable edit mode for lanbahn addresses in panel.

var clearRouteButtonActive = false // state of clear
// routes button
var conn_state_string = "?"

var appContext: Context? = null

var globalPower = POWER_UNKNOWN

var panelRect : Rect = Rect(0,0,100,100)