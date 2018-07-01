
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
// @file:JvmName("Constants")

package de.blankedv.lanbahnpanel.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import de.blankedv.lanbahnpanel.*
import java.util.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue


const val DEBUG = true
const val TAG = "LanbahnPanelActivity"

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


const val SXNET_PORT = 4104
const val LBMIN = 1 // minimum sx channel number
const val LBPOWER_ADR = 1000  // lanbahn channel for sx-power address 127
const val LBMAX = 9999 // maximum lanbahn channel number

// type of communication (Accessory, Sensor, Loco etc)

const val TYPE_NONE = 0
const val TYPE_SENSOR = 1
const val TYPE_ACCESSORY = 2
const val TYPE_LISSY = 3
const val TYPE_LOCO = 4



const val MAX_LAMP_BUTTONS = 4

// fixed prefix for scaling - should be =1 for small displays and =2 for
// large displays
// all Paints and x/y-s are scaled before drawing
const val prescale = 2

const val RASTER = 20 * prescale // raster points
// with xx pixels
const val TURNOUT_LENGTH = 10 // NOT to be prescaled
const val TURNOUT_LENGTH_LONG = (TURNOUT_LENGTH * 1.4f).toInt()
