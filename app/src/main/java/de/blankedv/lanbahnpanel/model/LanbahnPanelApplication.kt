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
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
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
        LPaints.init(prescale)

        val myAndroidDeviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        Log.d(TAG, "LanbahnPanelApplication - androidDeviceID=$myAndroidDeviceId")
        // scaling, zoom prefs are loaded from LanbahnPanelActivity

        // handler for receiving sxnet messages

        appHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val what = msg.what
                val chan = msg.arg1
                //if (DEBUG) Log.d(TAG,"rec. msg for chan= "+chan);
                val data = msg.arg2
                timeOfLastReceivedMessage = System.currentTimeMillis()
                if (what == TYPE_FEEDBACK_MSG) {
                    if (chan == LBPOWER_ADR) {
                        if (data == 0) {
                            globalPower = POWER_OFF
                        } else {
                            globalPower = POWER_ON
                        }
                    }
                    for (pe in panelElements) {
                        if (pe.adr == chan) {
                            //               if (DEBUG) Log.d(TAG,"updating "+pe.toString());
                            pe.updateData(data)

                            // it is possible that two elements have the same channel
                            // therefor all channels are iterated
                        }
                    }

                    for (rt in routes) {
                        if (rt.id == chan) {
                            rt.updateData(data)
                        }
                    }


                } else if (what == TYPE_ERROR_MSG) {
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


    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "AndroPanelApp - terminating.")

    }
    /* public static boolean isPowerOn() {
		return true; // TODO must evaluate stored lanbahn messages

	}  */

    fun saveZoomEtc() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        Log.d(TAG, "saving Zoom etc")
        editor.putBoolean(KEY_DRAW_ADR, drawAddresses)
        editor.putBoolean(KEY_DRAW_ADR2, drawAddresses2)
        editor.putString(KEY_STYLE_PREF, selectedStyle)
        editor.putBoolean(KEY_ENABLE_ZOOM, zoomEnabled)
        editor.putBoolean(KEY_ENABLE_EDIT, enableEdit)
        editor.putBoolean(KEY_SAVE_STATES, saveStates)
        editor.putBoolean(KEY_ROUTES, enableRoutes)
        editor.putBoolean(KEY_FLIP, flipUpsideDown)
        editor.putString(KEY_XOFF, "" + xoff)
        editor.putString(KEY_YOFF, "" + yoff)
        editor.putString(KEY_SCALE, "" + scale)

        // Commit the edits!
        editor.apply()
    }

    fun loadZoomEtc() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        zoomEnabled = prefs.getBoolean(KEY_ENABLE_ZOOM, false)
        Log.d(TAG, "zoomEnabled=$zoomEnabled")
        selectedStyle = prefs.getString(KEY_STYLE_PREF, "US")
        LPaints.init(prescale)
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
            Log.d(TAG, "AndroPanelApp - updatePanelData()")
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
         * needs to be executed always at shutdown to have a state of "UNKNOWN" when
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
            return false
        }
    }
}
