
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
@file:JvmName("Constants")

package de.blankedv.lanbahnpanel.model

const val DEBUG = true   // false for release-apk
const val TAG = "LanbahnPanel"

const val INVALID_INT = -1

// preferences
const val KEY_DRAW_ADR = "drawAddressesPref"
const val KEY_DRAW_ADR2 = "drawAddressesPref2"
const val KEY_STYLE_PREF = "selectStylePref"
//const val KEY_ENABLE_ZOOM = "enableZoomPref"
//const val KEY_ENABLE_AUTOSCALE = "enableAutoscalePref"
const val KEY_SCALE_PREF = "selectScalePref"
const val KEY_ENABLE_EDIT = "enableEditPref"
const val KEY_ENABLE_POWER_CONTROL = "powerControlPref"
const val KEY_SAVE_STATES = "saveStatesPref"
const val KEY_ROUTES = "routesPref"
const val KEY_FLIP = "flipPref"
const val KEY_IP = "ipPref"
const val KEY_PORT = "portPref"
const val KEY_CONFIG_FILE = "configFilenamePref"
const val KEY_QUADRANT = "lastQuadrant"
const val KEY_FIVE_VIEWS_PREF = "enableFiveViewsPref"
const val KEY_PANEL_SETTINGS = "panelSettingsPref"
const val KEY_LOCO_ADR = "locoAdrPref"
const val KEY_LOCO_MASS = "locoMassPref"
const val KEY_LOCO_NAME = "locoNamePref"
const val KEY_LOCOS_CONFIG_FILE = "locosConfigFilenamePref"
const val KEY_ENABLE_LOCO_CONTROL = "enableLocoControlPref"
const val KEY_LOCO_SYSTEM = "locoSystemPref"

/** {@value #N_PANEL_FOR_4Q} = minimum number of panel elements to display the 4 quadrants */
const val N_PANEL_FOR_4Q = 75

const val FNAME_PANEL_FROM_SERVER = "panel_from_server.xml"

const val DEMO_LOCOS_FILE = "locos-demo.xml"

const val LBP_NOTIFICATION_ID = 201 //arbitrary id for notification

// turnouts
const val STATE_CLOSED = 0
const val STATE_THROWN = 1
const val DISP_STANDARD = 0
const val DISP_INVERTED = 1

// signals
const val STATE_RED = 0
const val STATE_GREEN = 1
const val STATE_YELLOW = 2
const val STATE_YELLOW_FEATHER = 3
const val STATE_SWITCHING = 4

// buttons
const val STATE_NOT_PRESSED = 0
const val STATE_PRESSED = 1

// sensors
const val STATE_FREE = 0
const val STATE_OCCUPIED = 1
const val STATE_INROUTE = 2
const val STATE_UNKNOWN = -1

//power
const val POWER_UNKNOWN = INVALID_INT
const val POWER_ON = 1
const val POWER_OFF = 0
const val SX_POWER_CHANNEL = 127      // SX global power state is requested using  this address
const val SXMIN = 0
const val SXMAX = 111   // highest channel number for selectrix (lowest is 0)
const val LIFECHECK_SECONDS = 10  // every 10 seconds check if server connection is alive

const val DEFAULT_SXNET_PORT = "4104"  // string
const val DEFAULT_SXNET_IP = "192.168.178.29"  // string
const val LBMIN = 1 // minimum sx channel number
const val LBMAX = 9999 // maximum lanbahn channel number

// type of communication (Accessory, Sensor, Loco etc)

const val TYPE_NONE = 0
const val TYPE_SENSOR = 1
const val TYPE_ACCESSORY = 2
const val TYPE_LISSY = 3
const val TYPE_LOCO = 4

// Message Types for UI Thread
const val TYPE_ERROR_MSG = 3
const val TYPE_GENERIC_MSG = 4
const val TYPE_LN_ACC_MSG = 5
const val TYPE_LN_SENSOR_MSG = 6
const val TYPE_LN_LISSY_MSG = 7
const val TYPE_POWER_MSG = 8
const val TYPE_ROUTE_MSG = 9
const val TYPE_SX_MSG = 10

const val PROTOCOL_SX = "sx"
const val PROTOCOL_LN = "ln"

const val MAX_LAMP_BUTTONS = 4

// fixed prefix for scaling - should be =1 for small displays and =2 for
// large displays
// all Paints and x/y-s are scaled before drawing
const val prescale = 2

const val RASTER = 20 * prescale // raster points
// with xx pixels
const val TURNOUT_LENGTH = 10 // NOT to be prescaled
const val TURNOUT_LENGTH_LONG = (TURNOUT_LENGTH * 1.4f).toInt()

const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 12