package de.blankedv.lanbahnpanel.model

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.gson.Gson
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.*
import de.blankedv.lanbahnpanel.railroad.Commands
import de.blankedv.lanbahnpanel.settings.PanelSettings
import de.blankedv.lanbahnpanel.util.LanbahnBitmaps
import de.blankedv.lanbahnpanel.util.LPaints

// TODO: kotlin review and simplify
// TODO: handle absence ot connection to command station

class LanbahnPanelApplication : Application() {

    var timeOfLastReceivedMessage = 0L

    //@SuppressLint("HandlerLeak")
    @SuppressLint("HandlerLeak")
    override fun onCreate() {
        super.onCreate()
        if (DEBUG)
            Log.d(TAG, "onCreate LanbahnPanelApplication")

        // do some initializations
        // for (int i=0; i<MAX_LANBAHN_ADDR; i++) lanbahnData[i]=0;
        LanbahnBitmaps.init(resources)

        val myAndroidDeviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        Log.d(TAG, "LanbahnPanelApplication - androidDeviceID=$myAndroidDeviceId")
        // scaling, zoom prefs are loaded from LanbahnPanelActivity

        // handler for receiving sxnet/loconet messages
        // this must be done in the "Application" (not activity) to keep track of changes
        // during other activities like "settings" or "about"
        appHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val what = msg.what
                val chan = msg.arg1
                //if (DEBUG) Log.d(TAG,"received msg for chan= "+chan);
                val data = msg.arg2
                timeOfLastReceivedMessage = System.currentTimeMillis()
                when (what) {
                    TYPE_POWER_MSG -> {
                        if (data == 0) {
                            globalPower = POWER_OFF
                        } else {
                            globalPower = POWER_ON
                        }
                    }
                    TYPE_GENERIC_MSG -> {
                        PanelElement.update(chan, data)
                    }

                    TYPE_LOCO_MSG -> {
                        //if (DEBUG) Log.d(TAG,"xloco message chan=$chan d=$data")
                        if (selectedLoco == null) Log.e(TAG,"no loco selected")
                        if (selectedLoco?.adr == chan) {
                            selectedLoco?.updateLocoFromSX(data)
                        }
                    }

                    // TYPE_LN_ACC_MSG -> PanelElement.updateAcc(chan, (1-data))   //inverted values for LN
                    // TYPE_LN_SENSOR_MSG -> PanelElement.updateSensor(chan, data)
                    /*
                    TYPE_LN_LISSY_MSG -> {
                        val lissymsg = msg.obj as String
                        // TODO update lissy element
                    }*/

                    TYPE_ROUTE_MSG -> {
                        for (rt in routes) {
                            if (rt.id == chan) {
                                rt.updateData(data)
                            }
                        }
                    }

                    TYPE_ERROR_MSG -> {
                        if (DEBUG) Log.d(TAG, "error msg $chan $data")
                        for (pe in panelElements) {
                            if (pe.adr == chan) {
                                pe.updateData(STATE_UNKNOWN)
                            }
                        }
                    }

                }

            }

        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "LanbahnPanelApp - terminating.")

    }

    fun saveGenericSettings() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        Log.d(TAG, "saveGenericSettings")
        // generic
        editor.putBoolean(KEY_DRAW_ADR, drawAddresses)
        editor.putBoolean(KEY_DRAW_ADR2, drawAddresses2)
        editor.putBoolean(KEY_ENABLE_EDIT, enableEdit)
        editor.putBoolean(KEY_SAVE_STATES, saveStates)

        // currently used - but these will be stored also for the panel
        editor.putString(KEY_STYLE_PREF, selectedStyle)
        editor.putString(KEY_SCALE_PREF, selectedScale)
        editor.putBoolean(KEY_ROUTES, enableRoutes)
        editor.putBoolean(KEY_FLIP, flipUpsideDown)
        editor.putBoolean(KEY_FIVE_VIEWS_PREF, enableFiveViews)
        editor.putInt(KEY_QUADRANT, selQuadrant)

        editor.putBoolean(KEY_ENABLE_LOCO_CONTROL, enableLocoControl)
        if (enableLocoControl) {
            val adr = selectedLoco?.adr ?: 3
            editor.putInt(KEY_LOCO_ADR, adr)  // last used loco address
        }

        // Commit the edits!
        editor.apply()
    }

    fun savePanelSettings() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        Log.d(TAG, "savePanelSettings")
        // currently used - but these will be stored also for the panel
        pSett.selStyle = selectedStyle
        pSett.selScale = selectedScale
        pSett.enRoutes = enableRoutes
        pSett.flip = flipUpsideDown
        pSett.fiveViews = enableFiveViews
        pSett.selQua = selQuadrant

        // save all panel specific settings
        val serializedObject = Gson().toJson(`pSett`)
        if (DEBUG) Log.d(TAG, "save panel=$panelName panel-settings=" + serializedObject)
        editor.putString(KEY_PANEL_SETTINGS + "_" + panelName, serializedObject)

        // Commit the edits!
        editor.apply()
    }

    fun loadGenericSettings() {
        Log.d(TAG, "loadGenericSettings")
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        // generic
        enableEdit = prefs.getBoolean(KEY_ENABLE_EDIT, false)
        saveStates = prefs.getBoolean(KEY_SAVE_STATES, false)
        drawAddresses = prefs.getBoolean(KEY_DRAW_ADR, false)
        drawAddresses2 = prefs.getBoolean(KEY_DRAW_ADR2, false)

        // read generic settings (which might be overwritten by panel settings)
        selectedStyle = prefs.getString(KEY_STYLE_PREF, "US")
        selectedScale = prefs.getString(KEY_SCALE_PREF, "auto")
        enableRoutes = prefs.getBoolean(KEY_ROUTES, false)
        flipUpsideDown = prefs.getBoolean(KEY_FLIP, false)
        enableFiveViews = prefs.getBoolean(KEY_FIVE_VIEWS_PREF, false)
        if (enableFiveViews == true) {
            selQuadrant = prefs.getInt(KEY_QUADRANT, 0)   // currently display selQuadrant
        } else {
            selQuadrant = 0  // must be reset, because we only have one view left
        }
        enableLocoControl = prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)
        // selectedLoc.adr gets loaded when a locos-config.xml file is read
        LPaints.init(prescale, selectedStyle, applicationContext)
    }

    fun loadPanelSettings() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        Log.d(TAG, "loadPanelSettings panel=$panelName")
        // init from generic settings
        pSett = PanelSettings(selectedScale, selectedScale, enableRoutes, flipUpsideDown, enableFiveViews, selQuadrant)

        if (prefs.contains(KEY_PANEL_SETTINGS + "_" + panelName)) {
            val gson = Gson()
            if (DEBUG) Log.d(TAG, "panel-settings=" +
                    prefs.getString(KEY_PANEL_SETTINGS+ "_" + panelName, "??"))
            pSett = gson.fromJson(prefs.getString(KEY_PANEL_SETTINGS + "_" + panelName, ""), pSett.javaClass)
            // overwrite generic settings and store as current values
            selectedStyle = pSett.selStyle
            selectedScale = pSett.selScale
            enableRoutes = pSett.enRoutes
            flipUpsideDown = pSett.flip
            enableFiveViews = pSett.fiveViews
            if (enableFiveViews == true) {
                selQuadrant = pSett.selQua
            } else {
                selQuadrant = 0  // must be reset, because we only have one view left
            }
            // save panel specific settings for later use in SettingsActivity
            val editor = prefs.edit()
            editor.putString(KEY_STYLE_PREF, selectedStyle)
            editor.putString(KEY_SCALE_PREF, selectedScale)
            editor.putBoolean(KEY_ROUTES, enableRoutes)
            editor.putBoolean(KEY_FLIP, flipUpsideDown)
            editor.putBoolean(KEY_FIVE_VIEWS_PREF, enableFiveViews)
            editor.putInt(KEY_QUADRANT, selQuadrant)
            // Commit the edits!
            editor.apply()

        }

        LPaints.init(prescale, selectedStyle, applicationContext)
        if (!enableFiveViews) {
            selQuadrant = 0  // must be reset, because we only have one view left
        }
    }


    /**
     * Display OnGoing Notification that indicates Network Thread is still Running.
     * Currently called from LanbahnPanelActivity onPause, passing the current intent
     * to return to when reopening.
     */
    internal fun addNotification(notificationIntent: Intent) {
        val channelId = getString(R.string.default_notification_channel_id)
        val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.lb_icon)
                .setContentTitle(this.getString(R.string.notification_title))
                .setContentText(this.getString(R.string.notification_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val contentIntent = PendingIntent.getActivity(this, LBP_NOTIFICATION_ID, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT)
        builder.setContentIntent(contentIntent)

        // Add as notification
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Lanbahn Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        manager.notify(LBP_NOTIFICATION_ID, builder.build())
    }

    // Remove notification
    internal fun removeNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(LBP_NOTIFICATION_ID)
    }

    companion object {

        lateinit var appHandler: Handler // used for communication from RRConnection Thread to UI (application)
        lateinit var pSett : PanelSettings

        /**
         * set all active panel elements to "expired" to have them updated soon
         */
        fun expireAllPanelElements() {
            if (DEBUG) Log.d(TAG, "expireAllPanelData()")
            for (pe in panelElements.filter { it is ActivePanelElement }) {
                pe.setExpired()
            }
        }

        fun requestAllPanelData() {
            if (DEBUG) Log.d(TAG, "requstAllPanelData() proto=$panelProtocol")
            // request state of all active panel elements

            for (pe in panelElements.filter { it.adr != INVALID_INT && (it.isExpired() == true) }) {
                if (pe is ActivePanelElement) {
                    Commands.readChannel(pe.adr, pe.javaClass)
                }
            }

        }



        /**
         * needs to be executed aPreferenceManagerlways at shutdown to have a state of "UNKNOWN" when
         * no current data at application restart
         */
        fun clearPanelData() {
            if (DEBUG) Log.d(TAG, "clearAllPanelData()")
            for (e in panelElements) {
                e.state = STATE_UNKNOWN
            }
        }

        fun connectionIsAlive(): Boolean {
            if (client == null) {
                conn_state_string = "NOT CONNECTED"
                return false
            } else {
                return client!!.isConnected()
            }
        }

        fun calcAllAutoscales(w: Int, h: Int) {
            calcAutoScale(w, h, 0)
            calcAutoScale(w, h, 1)
            calcAutoScale(w, h, 2)
            calcAutoScale(w, h, 3)
            calcAutoScale(w, h, 4)
        }

        /**
         * calculate optimum scale from surfaceHolder width and height
         *
         * @param width of surfaceHolder (in pixels)
         * @param height of surfaceHolder (in pixels)
         * @param qua qudrant to display (0= all, 1=q1, 2=q2 ...)
         *
         * set global scale values for this quadrant (variable qClip[qua].scale, .xoff, .yoff )
         */
        fun calcAutoScale(width: Int, heightIn: Int, qua: Int) {
            if (DEBUG) Log.d(TAG, "calcAutoScale(w=$width, h=$heightIn, q=$qua)")
            if ((width == 0) or (heightIn == 0)) return //makes no sense

            var remainingHeight = heightIn
            if (enableLocoControl) {
                remainingHeight = 7 * heightIn / 8  // can only use 7/8 of height because of locoControlArea
                if (DEBUG) Log.d(TAG, "calcAutoScale(remH=$remainingHeight)")
            }
            // Rect(int left, int top, int right, int bottom)
            val re = Rect(panelRect)
            var scale = 1f
            var xoff = 0f
            var yoff = 0f

            when (qua) {
                1 -> {
                    re.right = re.left + (re.right - re.left) / 2
                    re.bottom = re.top + (re.bottom - re.top) / 2
                }
                2 -> {
                    re.left = re.left + (re.right - re.left) / 2
                    re.bottom = re.top + (re.bottom - re.top) / 2
                }
                3 -> {
                    re.right = re.left + (re.right - re.left) / 2
                    re.top = re.top + (re.bottom - re.top) / 2
                }
                4 -> {
                    re.left = re.left + (re.right - re.left) / 2
                    re.top = re.top + (re.bottom - re.top) / 2
                }
            }
            // nexus7 (surface changed) - format=4 w=1280 h=618
            // samsung SM-T580 (surface changed) - format=4 w=1920 h=1068
            val sc1X = width / ((re.right - re.left) * 1.0f)
            val sc1Y = remainingHeight / ((re.bottom - re.top) * 1.0f)

            scale = Math.min(sc1X,sc1Y)
            val fact = sc1X / sc1Y

            val hRect = 1.0f * (re.bottom - re.top)
            val wRect = 1.0f * (re.right - re.left)

            if (sc1X < sc1Y) {
                if (DEBUG) Log.d(TAG,"autoscale sc1X < sc1Y fact=$fact")
                val hCalc = remainingHeight / scale
                when (qua) {
                    0 -> { xoff = 0f   //correct
                        yoff = scale * (hCalc - hRect) / 2 //correct
                    }
                    1 -> {
                        xoff = 0f  //correct
                        yoff = 0f   //correct
                    }
                    2 -> {
                        xoff = - ( panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        yoff = 0f    //correct
                    }
                    3 -> {
                        xoff = 0f   //correct
                        yoff = - ( re.top + (re.bottom - re.top) / 2f )   //correct
                    }
                    4 -> {
                        xoff = - ( panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        yoff = - (re.top + (re.bottom - re.top) / 2f )    //correct
                    }
                }
            } else {
                if (DEBUG) Log.d(TAG,"autoscale sc1X > sc1Y fact=$fact")
                val wCalc = width / scale
                when (qua) {
                    0 -> {
                        xoff  = scale * (wCalc - wRect) / 2    //correct
                        yoff = 0f   //correct
                    }
                    1 -> {
                        xoff = 0f    //correct
                        yoff = 0f     //correct
                    }
                    2 -> {
                        xoff = - ( panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  //  CORRECT
                        yoff = 0f // correct
                    }
                    3 -> {
                        xoff = 0f   // correct
                        yoff = - (panelRect.top + (panelRect.bottom - panelRect.top) / 2f )  * scale // CORRECT
                    }
                    4 -> {
                        xoff = - ( panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        //yoff = -(re.top + (re.bottom - re.top) / 2f)  // not correct
                        yoff = - (panelRect.top + (panelRect.bottom - panelRect.top) / 2f )  * scale // CORRECT
                    }
                }
            }
            pSett.qClip[qua].scale = scale
            pSett.qClip[qua].xoff = xoff
            pSett.qClip[qua].yoff = yoff
            /* if ((qua == 0) and enableLocoControl) {
                pSett.qClip[qua].yoff += heightIn / 8f   // additional displacement because of loco control area.
            } else if (enableLocoControl){
                pSett.qClip[qua].yoff += yoff + heightIn / 8f / 2  // additional displacement because of loco control area.
            } */


            if (DEBUG) Log.d(TAG, "autoscale result: scale=$scale xoff=$xoff yoff=$yoff (qua=$qua)")
        }

    }

}

