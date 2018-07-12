package de.blankedv.lanbahnpanel.view

import android.Manifest
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.*
import android.content.pm.PackageManager

import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import de.blankedv.lanbahnpanel.*

import java.util.Timer
import java.util.TimerTask

import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.clearPanelData
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.connectionIsAlive
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.appHandler
import de.blankedv.lanbahnpanel.config.ReadConfig
import de.blankedv.lanbahnpanel.config.DownloadPanel
import de.blankedv.lanbahnpanel.config.WriteConfig
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.elements.Route
import de.blankedv.lanbahnpanel.elements.RouteButtonElement
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.calcAutoScale
import de.blankedv.lanbahnpanel.railroad.RRConnectionThread
import de.blankedv.lanbahnpanel.util.Utils.threadSleep
import org.jetbrains.anko.*

/**
 * LanbahnPanelActivity is the MAIN activity of the lanbahn panel
 *
 * @author mblank
 */
class LanbahnPanelActivity : AppCompatActivity() {

    lateinit internal var builder: Builder

    internal var mBound = false
    lateinit internal var tv: TextView
    lateinit internal var params: LayoutParams

    lateinit internal var but: Button
    private var mOptionsMenu: Menu? = null
    private var mHandler = Handler()  // used for UI Update timer
    private var counter = 0

    private val KEY_STATES = "states"
    private var shuttingDown = false

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            /*binder = service as LoconetService.LocalBinder
             mService = binder.service
             mBound = true */
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

        //ReadConfig.readConfig(this)  READ IN ONRESUME

        setContentView(Panel(this))

        builder = AlertDialog.Builder(this)
        builder.setMessage(
                applicationContext.getString(R.string.exit_confirm))
                .setCancelable(false)
                .setPositiveButton(applicationContext.getString(R.string.yes)
                ) { _, _ ->
                    shutdownClient()
                    threadSleep(100L)
                    clearPanelData() // needs to be done to start again with
                    // a state of "UNKNOWN" when no current data
                    finish()
                }
                .setNegativeButton(applicationContext.getString(R.string.no)
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
        // bindService(intent, mConnection, Context.BIND_AUTO_CREATE);     val onComplete : BroadcastReceiver {

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

        (application as LanbahnPanelApplication).saveZoomEtc()
        //if (configHasChanged) {
            if (checkStorageWritePermission()) {
                WriteConfig.toXMLFile()
            } else {
                toast("ERROR: App has NO PERMISSION to write files !")
            }
        //}
        if (saveStates)
            saveStates()
        sendQ.clear()
        if (!shuttingDown) {
            (application as LanbahnPanelApplication).addNotification(this.intent)
        }

    }

    fun shutdownClient() {
        shuttingDown = true
        Log.d(TAG, "LanbahnPanelActivity - shutting down Client.")
        (application as LanbahnPanelApplication).removeNotification()
        client?.shutdown()
        client = null

    }


    override fun onResume() {
        super.onResume()
        if (DEBUG)
            Log.d(TAG, "onResume - LanbahnPanelActivity")
        sendQ.clear()

        (application as LanbahnPanelApplication).loadZoomEtc()

        var newPanel = reloadConfigIfPanelFileChanged()
        if (newPanel) quadrant = 0  // reset view
        Route.clearAllRoutes()

        // set quadrants mode only for large panels and auto-scale mode
        enableFourQuadrantsView = ((selectedScale == "auto") && ( panelElements.size >= N_PANEL_FOR_4Q))
        enableForQuadrantButtons(enableFourQuadrantsView)
        displayQuadrant(quadrant);


        if (DEBUG) debugLogDisplayMetrics()

        if (enableRoutes == false) {
            RouteButtonElement.autoReset()  // this will also reset the sensors to STATE_FREE
        }

        if (saveStates) {
            loadStates()
        }

        refreshAllData()
        LanbahnPanelApplication.updatePanelData()




        (application as LanbahnPanelApplication).removeNotification()
        title = "Lanbahn Panel \"$panelName\""

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

        mHandler.postDelayed({ updateUI() }, 500)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        mOptionsMenu = menu
        setConnectionIcon()
        enableForQuadrantButtons(enableFourQuadrantsView)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_settings // call preferences activity
            -> {
                startActivity(Intent(this, Preferences::class.java))
                return true
            }

            R.id.action_connect -> {
                toast("trying reconnect")
                restartCommFlag = true
                return true
            }
            R.id.action_q1 -> {
                if (enableFourQuadrantsView) displayQuadrant(1)
                return true
            }
            R.id.action_q2 -> {
                if (enableFourQuadrantsView) displayQuadrant(2)
                return true
            }
            R.id.action_q3 -> {
                if (enableFourQuadrantsView) displayQuadrant(3)
                return true
            }
            R.id.action_q4 -> {
                if (enableFourQuadrantsView) displayQuadrant(4)
                return true
            }
            R.id.action_qall -> {
                if (enableFourQuadrantsView) displayQuadrant(0)
                return true
            }
            R.id.action_power -> {
                togglePower() // toast("switching power ON/OFF not allowed")
                return true
            }
            R.id.menu_about // call preferences activity
            -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.menu_get_panel -> {
                readPanelFromServer()
                return true
            }

            R.id.menu_check_service -> {
                //TODO int num = mService.getRandomNumber();
                toast("not implemented")
                // "number: " + num, Toast.LENGTH_SHORT).show();
                return true
            }
            R.id.menu_quit -> {
                val alert = builder.create()
                alert.show()
                // done in alert: shutdownClient()
                return true
            }
            else -> return true //super.onOptionsItemSelected(item)
        }
    }


    fun openCommunication() {

        Log.d(TAG, "LanbahnPanelActivity - openCommunication.")
        if (client != null) {
            sendQ.clear()
            client?.shutdown()
            threadSleep(100) // give client some time to shut down.
        }
        sendQ.clear()

        Log.d(TAG, "first start of Communication to Comm.Station")
        startCommunication()

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (restartCommFlag) {
                    restartCommFlag = false
                    runOnUiThread {
                        Log.d(TAG, "restarting Communication")
                        startCommunication()
                    }

                }
            }
        }, 100, 100)

        // request updates for all channels used in Panel
        LanbahnPanelApplication.updatePanelData()
    }

    private fun updateUI() {
        counter++

        // logString is updated via Binding mechanism

        // the actionBar icons are NOT updated via binding, because
        // "At the moment, data binding is only for layout resources, not menu resources" (google)
        // and the implementation to "work around" this limitation looks very complicated, see
        // https://stackoverflow.com/questions/38660735/how-bind-android-databinding-to-menu
        setConnectionIcon()
        setPowerStateIcon()
        Route.auto()
        mHandler.postDelayed({ updateUI() }, 500)
    }

    private fun enableForQuadrantButtons(yes: Boolean) {
        if (DEBUG) Log.d(TAG, "enableForQuadrantButtons($yes)")
        if (yes) {
            mOptionsMenu?.findItem(R.id.action_q1)?.setIcon(R.drawable.q1_v2_48) ?: Log.e(TAG,"mOptionsMenu is not set")
            mOptionsMenu?.findItem(R.id.action_q2)?.setIcon(R.drawable.q2_v2_48)
            mOptionsMenu?.findItem(R.id.action_q3)?.setIcon(R.drawable.q3_v2_48)
            mOptionsMenu?.findItem(R.id.action_q4)?.setIcon(R.drawable.q4_v2_48)
            mOptionsMenu?.findItem(R.id.action_qall)?.setIcon(R.drawable.qa_v2_48)

        } else {
            mOptionsMenu?.findItem(R.id.action_q1)?.setIcon(R.drawable.trans_48) ?: Log.e(TAG,"mOptionsMenu is not set")
            mOptionsMenu?.findItem(R.id.action_q2)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_q3)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_q4)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_qall)?.setIcon(R.drawable.trans_48)
        }
    }

    private fun displayQuadrant(q: Int) {
        // 0 means "ALL"
          quadrant = q
          calcAutoScale(mWidth, mHeight, quadrant)
    }


    private fun togglePower() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val allowed = prefs.getBoolean(KEY_ENABLE_POWER_CONTROL, false)
        if ((client == null) or (!client!!.isConnected())) {
            toast("ERROR: not connected - cannot set global power state")
        } else if (allowed) {
            when (globalPower) {
                POWER_OFF -> client?.setPower(true)
                POWER_ON -> client?.setPower(false)
                POWER_UNKNOWN -> client?.setPower(true)
            }
        } else {
            toast("not allowed to set global power state, check settings")
        }
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

    private fun setConnectionIcon() {
        if (LanbahnPanelApplication.connectionIsAlive()) {
            mOptionsMenu?.findItem(R.id.action_connect)?.setIcon(R.drawable.commok)
        } else {
            mOptionsMenu?.findItem(R.id.action_connect)?.setIcon(R.drawable.nocomm)
            globalPower = POWER_UNKNOWN
        }
    }

    private fun setPowerStateIcon() {

        when (globalPower) {
            POWER_OFF -> {
                mOptionsMenu?.findItem(R.id.action_power)?.setIcon(R.drawable.power_stop)
            } //power_red)
            POWER_ON -> {
                mOptionsMenu?.findItem(R.id.action_power)?.setIcon(R.drawable.power_green)
            }
            POWER_UNKNOWN -> mOptionsMenu?.findItem(R.id.action_power)?.setIcon(R.drawable.power_unknown)
        }
    }

    /**
     * remark: always running from UI Thread
     *
     * @return
     */
    fun startCommunication() {
        Log.d(TAG, "LahnbahnPanelActivity - startCommunication.")
        client?.shutdown()
        Thread.sleep(100) // give client some time to shut down.

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val ip = prefs.getString(KEY_IP, DEFAULT_SXNET_IP)
        val port = Integer.parseInt(prefs.getString(KEY_PORT, DEFAULT_SXNET_PORT))

        client = RRConnectionThread(this, ip!!, port, appHandler)
        client?.start()

        Thread.sleep(300)

        if (connectionIsAlive()) {
            requestAllRailroadData()
        } else {
            val msg = " NO CONNECTION TO $ip ! Check WiFi/SSID and IP"
            longToast(msg)
            conn_state_string = "NOT CONNECTED"
        }
    }

    private fun checkStorageWritePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            // Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                // TODO
                return false
            } else { */
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE)

            // MY_PERMISSIONS_REQUEST_WRITE_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false
            /* } */
        } else {
            return true
        }
    }

    /** returned by Android after user has given permissions */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // finally, we got the permission, write updated config file NOW
                    WriteConfig.toXMLFile()
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    toast("cannot write log to File without permissions")
                }
                return
            }

// Add other 'when' lines to check for other
// permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun reloadConfigIfPanelFileChanged() : Boolean {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val cfFilename = prefs.getString(KEY_CONFIG_FILE, "-")
        var result = false
        // panel file changed or empty panel => (re-)load panel
        if ( (cfFilename != configFilename) || (panelElements.size == 0)) {
            // reload, if a new panel config file selected
            configFilename = cfFilename
            if (DEBUG) {
                Log.d(TAG, "onResume - reloading panel config.")
            }
            ReadConfig.readConfigFromFile(this) // reload config File with relocate of origin
            result = true
        }
        return result
    }


    private fun readPanelFromServer() {
        if (checkStorageWritePermission()) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(this)
            val server = prefs.getString(KEY_IP, "")
            getPanel(server)
        } else {
            longToast("ERROR: App has NO PERMISSION to write files !")
        }
    }

    private fun getPanel(server: String) {

        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {// We cannot read/write the media
            Log.e(TAG, "external storage not available or not writeable")
            toast("external storage not available or not writeable")
            return
        }

        doAsync {
            val url = "http://" + server + ":8000/config"
            val (res, content) = DownloadPanel(url).run()
            uiThread {
                if (res) {
                    //Log.d(TAG, content)
                    // content == filename
                    longToast("panel file read => select $content in settings to use it")
                } else {
                    Log.e(TAG, content)  // content == error message
                    longToast("panel file NOT read - ERROR:\n$content")
                }
            }
        }
    }

    private fun debugLogDisplayMetrics() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        if (DEBUG)
            Log.i(TAG, "metrics - w=$width  h=$height")

    }


    companion object {

        lateinit var popUp: PopupWindow
        lateinit var layout: LinearLayout

        // request state of all active panel elements
        private fun requestAllRailroadData() {
            for (pe in panelElements) {
                if (pe is ActivePanelElement) {
                    // TODO val type = 0  // TODO - no types for SX, but for loconet (sensor/acc/lissy)
                    client?.readChannel(pe.adr, pe.javaClass)
                }
            }
        }
    }


}
