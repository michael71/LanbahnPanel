package de.blankedv.lanbahnpanel

import android.app.Activity
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast

import java.util.Timer
import java.util.TimerTask

import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*

/**
 * LanbahnPanelActivity is the MAIN activity of the lanbahn panel
 *
 * @author mblank
 */
class LanbahnPanelActivity : Activity() {
    internal var builder: Builder

    internal var mService: LoconetService
    internal var mBound = false

    internal var tv: TextView
    internal var params: LayoutParams
    internal var mainLayout: LinearLayout? = null
    internal var but: Button
    internal var click = true
    private val KEY_STATES = "states"
    private var shuttingDown = false

    val wifiName: String?
        get() {
            val manager = applicationContext.getSystemService(applicationContext.WIFI_SERVICE) as WifiManager
            if (manager.isWifiEnabled) {
                val wifiInfo = manager.connectionInfo
                if (wifiInfo != null) {
                    val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                    if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        return wifiInfo.ssid
                    }
                }
            }
            return null
        }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LoconetService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DEBUG)
            Log.d(TAG, "onCreate LanbahnPanelActivity")
        popUp = PopupWindow(this)
        layout = LinearLayout(this)
        appContext = this
        tv = TextView(this)

        but = Button(this)
        but.text = "Click Me"

        params = LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)
        //layout.setOrientation(LinearLayout.VERTICAL);
        tv.text = "popup window with address..."
        layout.addView(tv, params)
        popUp.contentView = layout

        ParseConfig.readConfig(this)

        setContentView(Panel(this))

        builder = AlertDialog.Builder(this)
        builder.setMessage(
                applicationContext.getString(R.string.exit_confirm))
                .setCancelable(false)
                .setPositiveButton(
                        applicationContext.getString(R.string.yes)
                ) { dialog, id ->
                    shuttingDown = true
                    shutdownLanbahnClient()
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, e.message)
                    }

                    clearPanelData() // needs to be done
                    // to start
                    // again with a state of "UNKNOWN" when no
                    // current data
                    finish()
                }
                .setNegativeButton(
                        applicationContext.getString(R.string.no)
                ) { dialog, id -> dialog.cancel() }
        openCommunication()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (DEBUG)
            Log.d(TAG, "onBackPressed - LanbahnPanelActivity")
    }

    override fun onStart() {
        super.onStart()
        // TODO Bind to LoconetService
        //Intent intent = new Intent(this, LoconetService.class);
        // bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    override fun onStop() {
        super.onStop()
        if (DEBUG)
            Log.d(TAG, "onStop - LanbahnPanelActivity")
        // TODO  unbindService(mConnection);
        // mBound = false;
    }

    override fun onPause() {
        super.onPause()
        if (DEBUG)
            Log.d(TAG, "onPause - LanbahnPanelActivity")
        // firstStart=false; // flag to avoid re-connection call during
        // first
        // start
        // sendQ.add(DISCONNECT);
        (application as LanbahnPanelApplication).saveZoomEtc()
        if (configHasChanged)
            WriteConfig.writeToXML()
        if (saveStates)
            saveStates()
        sendQ.clear()
        if (!shuttingDown) {
            (application as LanbahnPanelApplication).addNotification(this.intent)
        }

    }

    fun shutdownLanbahnClient() {
        Log.d(TAG, "LanbahnPanelActivity - shutting down Client.")
        (application as LanbahnPanelApplication).removeNotification()
        if (client != null) {
            client.shutdown()

        }
    }

    override fun onResume() {
        super.onResume()
        if (DEBUG)
            Log.d(TAG, "onResume - LanbahnPanelActivity")
        sendQ.clear()

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val cfFilename = prefs.getString(KEY_CONFIG_FILE, "-")
        if (cfFilename != configFilename) {
            // reload, if a new panel config file selected
            configFilename = cfFilename
            if (DEBUG) {
                Log.d(TAG, "onResume - reloading panel config.")
            }
            ParseConfig.readConfigFromFile(this) // reload config File with scaling
            (application as LanbahnPanelApplication).loadZoomEtc()
            // TODO recalcScale();
        } else {
            (application as LanbahnPanelApplication).loadZoomEtc() // reload
            // settings without scaling
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels
        if (DEBUG)
            Log.i(TAG, "metrics - w=$width  h=$height")

        (application as LanbahnPanelApplication).loadZoomEtc()

        if (enableRoutes == false) {
            RouteButtonElement.autoReset()  // this will also reset the sensors to STATE_FREE
        }

        if (saveStates)
            loadStates()

        refreshAllData()
        LanbahnPanelApplication.updatePanelData()
        (application as LanbahnPanelApplication).removeNotification()

    }

    /**
     * set all active panel elements to "expired" to have them updated soon
     */
    private fun refreshAllData() {
        for (e in panelElements) {
            if (e is ActivePanelElement) {
                // add its address to list of interesting addresses
                // only needed for active elements, not for tracks
                val a = e.adr

                if (a != INVALID_INT) {
                    e.setExpired()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_settings // call preferences activity
            -> {
                startActivity(Intent(this, Preferences::class.java))
                return true
            }

            R.id.menu_about // call preferences activity
            -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.menu_check_service -> {
                //TODO int num = mService.getRandomNumber();
                Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT).show()
                // "number: " + num, Toast.LENGTH_SHORT).show();
                return true
            }
            R.id.menu_quit -> {
                val alert = builder.create()
                alert.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun openCommunication() {

        Log.d(TAG, "LanbahnPanelActivity - openCommunication.")
        if (client != null) {
            sendQ.clear()
            client.shutdown()
            try {
                Thread.sleep(100) // give client some time to shut down.
            } catch (e: InterruptedException) {
                if (DEBUG)
                    Log.e(TAG, "could not sleep...")
            }

        }
        sendQ.clear()

        Log.d(TAG, "first start of  SXNetComm")
        startSXNetCommunication()

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (restartCommFlag) {
                    restartCommFlag = false
                    runOnUiThread {
                        Log.d(TAG, "restarting SXNetComm")
                        startSXNetCommunication()
                    }

                }
            }
        }, 100, 100)

        // request updates for all channels used in Panel
        LanbahnPanelApplication.updatePanelData()
    }

    fun saveStates() {
        val sb = StringBuilder()

        for (pe in panelElements) {
            if (pe.adr != INVALID_INT) {
                if (pe.state != INVALID_INT)
                    sb.append(pe.adr).append(",").append(pe.state).append(";")
            }

        }

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        Log.d(TAG, "saving States=" + sb.toString())
        editor.putString(KEY_STATES, sb.toString())
        editor.commit()
    }

    fun loadStates() {
        if (DEBUG)
            Log.d(TAG, "loading States")
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val states = prefs.getString(KEY_STATES, "")
        if (states!!.length == 0) {
            Log.d(TAG, "previous state of devices could not be read")
            return
        }

        try {
            val keyvalues = states.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (kv in keyvalues) {
                val s2 = kv.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (pe in panelElements) {
                    if (pe is ActivePanelElement) {
                        if (pe.adr == Integer.parseInt(s2[0])) {
                            pe.state = Integer.parseInt(s2[1])
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during loadStates: " + e.message)
        }

        Log.d(TAG, "states=$states")

    }

    /**
     * remark: always running from UI Thread
     *
     * @return
     */
    fun startSXNetCommunication() {
        Log.d(TAG, "LahnbahnPanelActivity - startSXNetCommunication.")
        if (client != null) {
            client.shutdown()
            try {
                Thread.sleep(100) // give client some time to shut down.
            } catch (e: InterruptedException) {
                if (DEBUG)
                    Log.e(TAG, "could not sleep...")
            }

        }

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val ip = prefs.getString(KEY_IP, "192.168.2.50")
        /*	Log.d(TAG, "AndroPanelActivity - autoIPEnabled="+autoIPEnabled);


		if ((autoIPEnabled == true) && (autoIP.length()>0) && (!ip.equals(autoIP))) {
			ip = autoIP;
			Log.d(TAG, "AndroPanelActivity - auto ip changed="+ip);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(KEY_IP, ip);
			// Commit the edits!
			editor.commit();
		} */


        client = SXnetClientThread(this, ip!!, SXNET_PORT)
        client.start()

        try {
            Thread.sleep(300)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


        if (connectionIsAlive()) {
            requestAllSXdata()
            Log.d(TAG, "SSID=" + wifiName!!)
            conn_state_string = "SSID=$wifiName     $ip"
        } else {
            val msg = " NO CONNECTION TO $ip ! Check WiFi/SSID and IP"
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            conn_state_string = "NOT CONNECTED"

        }
    }

    fun shutdownSXClient() {
        Log.d(TAG, "LanbahnPanelActivity - shutting down SXnet Client.")
        if (client != null)
            client.shutdown()
        if (client != null)
            client.disconnectContext()
        client = null

    }

    companion object {

        var popUp: PopupWindow
        var layout: LinearLayout

        // request state of all active panel elements
        private fun requestAllSXdata() {
            for (pe in panelElements) {
                if (pe is ActivePanelElement) {
                    client.readChannel(pe.adr)
                }
            }
            for (l in lampGroups) {
                client.readChannel(l.adr)
            }
        }
    }


}