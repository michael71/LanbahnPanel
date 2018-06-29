
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
@file:JvmName("Constants")

package de.blankedv.lanbahnpanel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import java.util.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue


const val DEBUG = true

const val INVALID_INT = -1

// preferences
// public static final String KEY_LOCO_ADR = "locoAdrPref";
const val KEY_DRAW_ADR = "drawAddressesPref"
const val KEY_DRAW_ADR2 = "drawAddressesPref2"
const val KEY_STYLE_PREF = "selectStylePref"
const val KEY_ENABLE_ZOOM = "enableZoomPref"
const val KEY_ENABLE_EDIT = "enableEditPref"
const val KEY_SAVE_STATES = "saveStatesPref"
const val KEY_ROUTES = "routesPref"
const val KEY_FLIP = "flipPref"
const val KEY_ENABLE_ALL_ADDRESSES = "enableAllAddressesPref"
const val KEY_XOFF = "xoffPref"
const val KEY_YOFF = "yoffPref"
const val KEY_SCALE = "scalePref"
const val KEY_IP = "ipPref"
const val KEY_CONFIG_FILE = "configFilenamePref"

const val LBP_NOTIFICATION_ID = 201 //arbitrary id for notification

// turnouts
const val STATE_CLOSED = 0
const val STATE_THROWN = 1

// signals
const val STATE_RED = 0
const val STATE_GREEN = 1
const val STATE_YELLOW = 2
const val STATE_YELLOW_FEATHER = 3

// buttons
const val STATE_NOT_PRESSED = 0
const val STATE_PRESSED = 1

// sensors
const val STATE_FREE = 0
const val STATE_OCCUPIED = 1
const val STATE_INROUTE = 2
const val STATE_UNKNOWN = -1

// with file
var noWifiFlag = false

const val SXNET_PORT = 4104
const val SXMAX = 112 // maximum sx channel number - only SX0 !
const val LBMAX = 9999 // maximum lanbahn channel number

var width: Int = 0
var height:Int = 0
const val TAG = "LanbahnPanelActivity"
var selectedStyle: String? = "UK" // German style or USS style

var panelElements = ArrayList<PanelElement>()
var routes = ArrayList<Route>()
var compRoutes = ArrayList<CompRoute>()
var lampGroups = ArrayList<LampGroup>()
const val MAX_LAMP_BUTTONS = 4

var panelName = ""

var drawAddresses = false
var drawAddresses2 = false
var flipUpsideDown = false  //display all panel element from "other side"
var saveStates: Boolean = false

lateinit var handler: Handler //

// connection state
var client: SXnetClientThread? = null
var restartCommFlag = false

// put all messages which should be sent into this queue
val sendQ: BlockingQueue<String> = ArrayBlockingQueue(
        200)

val TYPE_FEEDBACK_MSG = 2
val TYPE_ERROR_MSG = 3

@Volatile
var connString = ""

val DIRECTORY = "lanbahnpanel/"
// with trailing slash !!

var configFilename = "lb-panel1.xml"

val DEMO_FILE = "demo-panel.xml" // demo data in raw
// assets dir.

var configHasChanged = false // store info whether config
// has changed
// if true, then a new config file is written at the end of the Activity

var zoomEnabled: Boolean = false
var scale = 1.0f // user selectable scaling of panel area

// fixed prefix for scaling - should be =1 for small displays and =2 for
// large displays
// all Paints and x/y-s are scaled before drawing
const val prescale = 2

const val RASTER = 20 * prescale // raster points
// with xx pixels
const val TURNOUT_LENGTH = 10 // NOT to be prescaled
const val TURNOUT_LENGTH_LONG = (TURNOUT_LENGTH * 1.4f).toInt()
var xoff = (10 * prescale).toFloat()
var yoff = (50 * prescale).toFloat()
// public static Bitmap myBitmap = Bitmap.createBitmap(4000,1600,
// Bitmap.Config.ARGB_4444);
var myBitmap = Bitmap.createBitmap(2000, 800,
        Bitmap.Config.ARGB_4444)
var myCanvas = Canvas(myBitmap)

var enableEdit = false
var enableRoutes = false
// enable edit mode for lanbahn addresses in panel.

var clearRouteButtonActive = false // state of clear
// routes button
var conn_state_string = "?"

var appContext: Context? = null

// var lampState: BooleanArray? = null   // TODO

var controlArea: ControlArea = ControlArea()

var controlAreaRect = Rect(0,100,0,100)
