
// to make constants usable in JAVA via  Constants.GLOBAL_NUMBER for example
@file:JvmName("Constants")

package de.blankedv.lanbahnpanel

import android.os.Handler


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