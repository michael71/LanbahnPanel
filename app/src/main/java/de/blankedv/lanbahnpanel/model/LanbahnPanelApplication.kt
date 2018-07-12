package de.blankedv.lanbahnpanel.model

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.preference.ListPreference
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.elements.SensorElement
import de.blankedv.lanbahnpanel.util.AndroBitmaps
import de.blankedv.lanbahnpanel.util.LPaints

/** Lanbahn Panel
 * Rev 3.1 - 28 Jun 2018 - now using sxnet protocol
 */

// TODO: kotlin review and simplify
// TODO: handle absence ot connection to SX command station

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
        AndroBitmaps.init(resources)

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
                //if (DEBUG) Log.d(TAG,"rec. msg for chan= "+chan);
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
                    TYPE_GENERIC_MSG -> PanelElement.update(chan, data)

                    TYPE_LN_ACC_MSG -> PanelElement.updateAcc(chan, data)

                    TYPE_LN_SENSOR_MSG -> PanelElement.updateSensor(chan, data)

                    TYPE_LN_LISSY_MSG -> {
                        val lissymsg = msg.obj as String
                        // TODO update lissy element
                    }

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

    fun saveZoomEtc() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        Log.d(TAG, "saving Zoom etc")
        editor.putBoolean(KEY_DRAW_ADR, drawAddresses)
        editor.putBoolean(KEY_DRAW_ADR2, drawAddresses2)
        editor.putString(KEY_STYLE_PREF, selectedStyle)
        editor.putString(KEY_SCALE_PREF, selectedScale)
        //editor.putBoolean(KEY_ENABLE_ZOOM, zoomEnabled)
        editor.putBoolean(KEY_ENABLE_EDIT, enableEdit)
        editor.putBoolean(KEY_SAVE_STATES, saveStates)
        editor.putBoolean(KEY_ROUTES, enableRoutes)
        editor.putBoolean(KEY_FLIP, flipUpsideDown)
        editor.putString(KEY_XOFF, "" + xoff)
        editor.putString(KEY_YOFF,"" + yoff)
        editor.putString(KEY_SCALE, "" + scale)
        editor.putInt(KEY_QUADRANT, quadrant)

        // Commit the edits!
        editor.apply()
    }

    fun loadZoomEtc() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        //zoomEnabled = prefs.getBoolean(KEY_ENABLE_ZOOM, false)
        //Log.d(TAG, "zoomEnabled=$zoomEnabled")
        selectedStyle = prefs.getString(KEY_STYLE_PREF, "US")
        LPaints.init(prescale,selectedStyle)
        selectedScale = prefs.getString(KEY_SCALE_PREF, "auto")
        enableEdit = prefs.getBoolean(KEY_ENABLE_EDIT, false)
        saveStates = prefs.getBoolean(KEY_SAVE_STATES, false)
        if (DEBUG)
            Log.d(TAG, "saveStates=$saveStates")
        drawAddresses = prefs.getBoolean(KEY_DRAW_ADR, false)
        drawAddresses2 = prefs.getBoolean(KEY_DRAW_ADR2, false)
        enableRoutes = prefs.getBoolean(KEY_ROUTES, false)
        flipUpsideDown = prefs.getBoolean(KEY_FLIP, false)
        if (DEBUG)
            Log.d(TAG, "drawAddresses=$drawAddresses")
        if (DEBUG)
            Log.d(TAG, "drawAddresses2=$drawAddresses2")
        xoff = java.lang.Float.parseFloat(prefs.getString(KEY_XOFF, "20"))
        yoff = java.lang.Float.parseFloat(prefs.getString(KEY_YOFF, "50"))
        scale = java.lang.Float.parseFloat(prefs.getString(KEY_SCALE, "1.0"))
        quadrant = prefs.getInt(KEY_QUADRANT, 0)

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
            manager?.createNotificationChannel(channel)
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

        fun updatePanelData() {
            Log.d(TAG, "LanbahnPanelApp - updatePanelData()")
            for (e in panelElements) {
                if (e is ActivePanelElement) {
                    // add its address to list of interesting addresses
                    // only needed for active elements, not for tracks
                    val a = e.adr
                    if (a != INVALID_INT && e.isExpired) {
                        val success = sendQ.offer("READ $a") // request data for
                        // all active
                        // addresses
                        if (!success)
                            Log.e(TAG, "sendQ full")
                    }
                }
            }
        }

        /**
         * needs to be executed aPreferenceManagerlways at shutdown to have a state of "UNKNOWN" when
         * no current data at application restart
         */
        fun clearPanelData() {
            for (e in panelElements) {
                if (e is ActivePanelElement) {
                    // add its address to list of interesting addresses
                    // only needed for active elements, not for tracks
                    e.state = STATE_UNKNOWN
                }
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

        /**
         * calculate optimum scale from surfaceHolder width and height
         *
         * @param width of surfaceHolder (in pixels)
         * @param height of surfaceHolder (in pixels)
         * @param qua qudrant to display (0= all, 1=q1, 2=q2 ...)
         *
         * set global scale values scale, xoff, yoff
         */
        fun calcAutoScale(width: Int, height: Int, qua: Int) {
            // nexus7 (surface changed) - format=4 w=1280 h=618
            // samsung SM-T580 (surface changed) - format=4 w=1920 h=1068
            val sc1X = width / ((panelRect.right - panelRect.left) * 1.0f)
            val sc1Y = height / ((panelRect.bottom - panelRect.top) * 1.0f)
            var mult = 1f
            if (qua != 0) mult = 2f

            if (sc1X < sc1Y) { // x-dimensions of panel elements larger than y-dim
                // (this is normally the case for layout panels)

                scale = sc1X * mult
                val hCalc = height / scale
                val hRect = 1.0f * (panelRect.bottom - panelRect.top) / mult
                val wCalc = height / scale
                val wRect = 1.0f * (panelRect.right - panelRect.left) / mult

                when (qua) {
                    0,1,3 -> xoff = 0f
                    2,4   -> xoff = - wRect * scale
                }
                when (qua) {
                    0 -> yoff = (hCalc - hRect) / 2
                    1,2 -> yoff = 0f + (hCalc - hRect) / 2
                    3,4 -> yoff = (- hRect * scale) + (hCalc - hRect) / 2
                }

            } else {
                // TODO implement
                scale = sc1Y
                xoff = 0f // width * 2f * (sc1Y / sc1X) / prescale
                yoff = 0f
            }

            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
            val editor = prefs.edit()
            Log.d(TAG, "saving modified scale")
            editor.putString(KEY_XOFF, "" + xoff)
            editor.putString(KEY_YOFF, "" + yoff)
            editor.putString(KEY_SCALE, "" + scale)
            // Commit the edits!
            editor.apply()

            Log.d(TAG, "calc" +
                    "" +
                    " scale=$scale xoff=$xoff yoff=$yoff")
        }

    }
}
