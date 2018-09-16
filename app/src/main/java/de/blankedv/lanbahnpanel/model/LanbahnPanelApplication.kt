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
import android.view.Gravity
import com.google.gson.Gson
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.*
import de.blankedv.lanbahnpanel.railroad.Commands
import de.blankedv.lanbahnpanel.settings.PanelSettings
import de.blankedv.lanbahnpanel.util.LanbahnBitmaps
import de.blankedv.lanbahnpanel.util.LPaints
import org.jetbrains.anko.toast

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

        prefs = PreferenceManager
                .getDefaultSharedPreferences(this)

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
                    TYPE_CONNECTION_MSG -> {
                        if (data == 0) {
                            cmdStationConnection = CMD_STATION_OFF
                        } else {
                            cmdStationConnection = CMD_STATION_ON
                        }
                    }
                    TYPE_ROUTING_MSG -> {
                        val editor = prefs.edit()
                        if (data == 0) {
                            editor.putBoolean(KEY_ROUTING,false)
                        } else {
                            editor.putBoolean(KEY_ROUTING,true)
                        }
                        editor.apply()
                    }
                    TYPE_ROUTE_INVALID_MSG -> {
                        if (DEBUG) Log.d(TAG, "route invalid, can not be set")
                        toast(getString(R.string.route_invalid)).setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0,0)
                    }
                    TYPE_GENERIC_MSG -> {
                        PanelElement.updateAcc(chan, data)  // turnout, signal, doubleslip
                        PanelElement.updateSensor(chan, data)  // special handling
                        // TODO check if this does not imply double setting of turnouts/signals ???
                        // ?? Route.update(chan, data)
                    }

                    TYPE_SX_MSG -> {   // data of 8 (or less) lanbahn channels bundled in a data byte
                        for (i in 1..8) {   // sxbit 1..8
                            val lbChan = chan * 10 + i
                            val allMatchingPEs = PanelElement.getAllPesByAddress(lbChan)
                            for (pe in allMatchingPEs) {
                                var d: Int
                                if (pe.nbit == 2) { // check if nbit is != 1
                                    d = data.shr(i - 1) and 0x03
                                } else {
                                    d = data.shr(i - 1) and 0x01
                                }
                                pe.state = d
                            }
                            // there should be no routes in the SX address range !!!
                        }
                    }

                    TYPE_LOCO_MSG -> {
                        //if (DEBUG) Log.d(TAG,"xloco message chan=$chan d=$data")
                        if ((selectedLoco == null) && (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false))) {
                            Log.e(TAG, "no loco selected")
                        }
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


                    TYPE_SHUTDOWN_MSG -> {
                        if (DEBUG) Log.d(TAG, "client thread disconnecting")
                        toast("no response, disconnecting")
                        client = null
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

    fun saveCurrentLoco() {

        val editor = prefs.edit()
        Log.d(TAG, "saveCurrentLoco")
        // generic

        if (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) {
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
        pSett.selStyle = prefs.getString(KEY_STYLE_PREF,"US")
        pSett.selScale = prefs.getString(KEY_SCALE_PREF,"auto")
        pSett.fiveViews = prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)
        pSett.selQua = prefs.getInt(KEY_QUADRANT,0)
        pSett.system = prefs.getString(KEY_CONTROL_SYSTEM,"sx")

        // save all panel specific settings
        val serializedObject = Gson().toJson(`pSett`)
        if (DEBUG) Log.d(TAG, "save panel=$panelName panel-settings=" + serializedObject)
        editor.putString(KEY_PANEL_SETTINGS + "_" + panelName, serializedObject)

        // Commit the edits!
        editor.apply()
    }


    fun loadPanelSettings() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        Log.d(TAG, "loadPanelSettings panel=$panelName")

        // init pSett from generic settings
        pSett = PanelSettings(prefs.getString(KEY_STYLE_PREF,"US"),
                prefs.getString(KEY_SCALE_PREF,"auto"),
                prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false),
                prefs.getInt(KEY_QUADRANT, 0),
                prefs.getString(KEY_CONTROL_SYSTEM,"sx"))

        if (prefs.contains(KEY_PANEL_SETTINGS + "_" + panelName)) {
            val gson = Gson()
            if (DEBUG) Log.d(TAG, "panel-settings=" +
                    prefs.getString(KEY_PANEL_SETTINGS + "_" + panelName, "??"))
            pSett = gson.fromJson(prefs.getString(KEY_PANEL_SETTINGS + "_" + panelName, ""), pSett.javaClass)
            // overwrite generic settings and store as current values

            // save panel specific settings for later use in SettingsActivity
            val editor = prefs.edit()
            editor.putString(KEY_STYLE_PREF, pSett.selStyle)
            editor.putString(KEY_SCALE_PREF, pSett.selScale)
            editor.putBoolean(KEY_FIVE_VIEWS_PREF, pSett.fiveViews)
            editor.putInt(KEY_QUADRANT, pSett.selQua)
            editor.putString(KEY_CONTROL_SYSTEM,pSett.system)
            // Commit the edits!
            editor.apply()

        }

        LPaints.init(prescale, prefs.getString(KEY_STYLE_PREF,"US"), applicationContext)
        val editor = prefs.edit()
        if (pSett.fiveViews == true) {
            editor.putInt(KEY_QUADRANT, pSett.selQua)
        } else {
            editor.putInt(KEY_QUADRANT, 0 ) // must be reset, when we have only one view left
        }
        editor.apply()
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
        lateinit var pSett: PanelSettings

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
            val systemFromSettings = prefs.getString(KEY_CONTROL_SYSTEM,"sx")
            if (DEBUG) Log.d(TAG, "requstAllPanelData() protoFromSettings=$systemFromSettings")
            // request state of all active panel elements

            // send in chunks of 40 reads
            val addrArray = ArrayList<Int>()
            for (pe in panelElements.filter { it.adr != INVALID_INT && (it.isExpired() == true) }) {
                if (pe is ActivePanelElement) {
                    addrArray.add(pe.adr)
                    if (addrArray.size > 40) {
                        Commands.readMultipleChannels(addrArray)
                        addrArray.clear()
                    }
                }
            }
            Commands.readMultipleChannels(addrArray)

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
            if (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) {
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

            scale = Math.min(sc1X, sc1Y)
            val fact = sc1X / sc1Y

            val hRect = 1.0f * (re.bottom - re.top)
            val wRect = 1.0f * (re.right - re.left)

            if (sc1X < sc1Y) {
                if (DEBUG) Log.d(TAG, "autoscale sc1X < sc1Y fact=$fact")
                val hCalc = remainingHeight / scale
                when (qua) {
                    0 -> {
                        xoff = 0f   //correct
                        yoff = scale * (hCalc - hRect) / 2 //correct
                    }
                    1 -> {
                        xoff = 0f  //correct
                        yoff = 0f   //correct
                    }
                    2 -> {
                        xoff = -(panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        yoff = 0f    //correct
                    }
                    3 -> {
                        xoff = 0f   //correct
                        yoff = -(re.top + (re.bottom - re.top) / 2f)   //correct
                    }
                    4 -> {
                        xoff = -(panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        yoff = -(re.top + (re.bottom - re.top) / 2f)    //correct
                    }
                }
            } else {
                if (DEBUG) Log.d(TAG, "autoscale sc1X > sc1Y fact=$fact")
                val wCalc = width / scale
                when (qua) {
                    0 -> {
                        xoff = scale * (wCalc - wRect) / 2    //correct
                        yoff = 0f   //correct
                    }
                    1 -> {
                        xoff = 0f    //correct
                        yoff = 0f     //correct
                    }
                    2 -> {
                        xoff = -(panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  //  CORRECT
                        yoff = 0f // correct
                    }
                    3 -> {
                        xoff = 0f   // correct
                        yoff = -(panelRect.top + (panelRect.bottom - panelRect.top) / 2f) * scale // CORRECT
                    }
                    4 -> {
                        xoff = -(panelRect.left + (panelRect.right - panelRect.left) / 2.0f) * scale  // CORRECT
                        //yoff = -(re.top + (re.bottom - re.top) / 2f)  // not correct
                        yoff = -(panelRect.top + (panelRect.bottom - panelRect.top) / 2f) * scale // CORRECT
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

