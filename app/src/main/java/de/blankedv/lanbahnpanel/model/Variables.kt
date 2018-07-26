
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
@file:JvmName("Variables")

package de.blankedv.lanbahnpanel.model

import android.content.Context
import android.graphics.Rect
import de.blankedv.lanbahnpanel.elements.CompRoute
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.elements.Route
import de.blankedv.lanbahnpanel.loco.Loco
import de.blankedv.lanbahnpanel.railroad.Railroad
import java.util.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue


var sxData = IntArray(SXMAX+1, { _ -> 0 })

var selectedStyle: String = "UK" // German style or USS style
var selectedScale: String = "auto" // automatic scaling

var panelElements = ArrayList<PanelElement>()
var routes = ArrayList<Route>()
var compRoutes = ArrayList<CompRoute>()
var sxMappings = ArrayList<LanbahnSXPair>();  // maps lanbahn addresses to SX addresses
var locolist = ArrayList<Loco>()

var panelName = ""
var panelProtocol = ""
var panelVersion = ""

var drawAddresses = false
var drawAddresses2 = false
var flipUpsideDown = false  //display all panel element from "other side"
var saveStates: Boolean = false
var enableDiscoverTurnouts = false
var enableFiveViews = false

var mWidth: Int = 0  // TODO move away from global scope
var mHeight: Int = 0  // TODO move away from global scope

var hCalc = 0f // DEBUG only
var hRect = 0f // DEBUG only

// connection state
var client: Railroad? = null

var restartCommFlag = false

// put all messages which should be sent into this queue
val sendQ: BlockingQueue<String> = ArrayBlockingQueue(400)

@Volatile
var connString = ""

public const val DIRECTORY = "/lanbahnpanel/"
// with leading and trailing slash !!

var configFilename = "lb-panel1.xml"
var locoConfigFilename = "locos.xml"
var enableLocoControl = false

const val DEMO_FILE = "demo-panel.xml" // demo data in raw
// assets dir.

var configHasChanged = false // store info whether config
// has changed
// if true, then a new config file is written at the end of the Activity
var locoConfigHasChanged = false

@Volatile var selQuadrant = 0  // TODO move away from global scope

var enableEdit = false
var enableRoutes = false
// enable edit mode for lanbahn addresses in panel.

var clearRouteButtonActive = false // state of clear
// routes button
var conn_state_string = "?"

var appContext: Context? = null

@Volatile var globalPower = POWER_UNKNOWN

var panelRect : Rect = Rect(0,0,100,100)

var controlAreaRect: Rect? = null   // public needed for java
@Volatile var selectedLoco : Loco? = null
var locoListName = "?"