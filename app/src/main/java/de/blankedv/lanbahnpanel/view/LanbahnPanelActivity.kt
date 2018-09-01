package de.blankedv.lanbahnpanel.view

import android.Manifest
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import de.blankedv.lanbahnpanel.*

import java.util.Timer
import java.util.TimerTask

import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.clearPanelData
import de.blankedv.lanbahnpanel.config.ReadConfig
import de.blankedv.lanbahnpanel.config.Download
import de.blankedv.lanbahnpanel.config.WriteConfig
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.elements.Route
import de.blankedv.lanbahnpanel.elements.RouteButtonElement
import de.blankedv.lanbahnpanel.loco.Loco
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.railroad.Commands
import de.blankedv.lanbahnpanel.railroad.Railroad
import de.blankedv.lanbahnpanel.settings.SettingsActivity
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.util.Utils.threadSleep
import org.jetbrains.anko.*


/**
 * LanbahnPanelActivity is the MAIN activity of the lanbahn panel
 *
 * @author mblank
 */
class LanbahnPanelActivity : AppCompatActivity() {

    internal lateinit var builder: Builder


    internal var mBound = false
    internal lateinit var tv: TextView
    internal lateinit var params: LayoutParams

    internal lateinit var but: Button
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(-0xcf77d0))
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (DEBUG)
            Log.d(TAG, "onBackPressed - LanbahnPanelActivity")
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

        (application as LanbahnPanelApplication).saveCurrentLoco()
        (application as LanbahnPanelApplication).savePanelSettings()
        //if (configHasChanged) {
        if (checkStorageWritePermission()) {
            WriteConfig.toXMLFile()
        } else {
            toast("ERROR: App has NO WRITE PERMISSION to write a new config file !")
        }

        if (prefs.getBoolean(KEY_SAVE_STATES, false))
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

        setActionBarBackground()  // matching to panel style
        LPaints.init(prescale, prefs.getString(KEY_STYLE_PREF, "US"), applicationContext)

        var newPanel = reloadConfigIfPanelFileChanged()
        if (newPanel) {
            // get panel preferences (and override current preferences for scaling, quadrant
            (application as LanbahnPanelApplication).loadPanelSettings()
        }


        if (DEBUG) Log.d(TAG, "panelName=$panelName enableFiveViews=${prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)} selQuadrant=${prefs.getInt(KEY_QUADRANT,0)}")
        // set quadrants mode and display selected selQuadrant

        enableForQuadrantButtons(prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false))
        displayQuadrant(prefs.getInt(KEY_QUADRANT, 0))
        displayLockAndRoutingState()

        setActionBarBackground()  // matching to panel style
        LPaints.init(prescale, prefs.getString(KEY_STYLE_PREF, "US"), applicationContext)

        if (DEBUG) debugLogDisplayMetrics()

        if (prefs.getBoolean(KEY_ROUTES, false)) {
            Route.clearAllRoutes()
        } else {
            RouteButtonElement.autoReset()  // this will also reset the sensors to STATE_FREE
        }

        if (prefs.getBoolean(KEY_SAVE_STATES, false)) {
            loadStates()
        }

        if (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) {
            // get last loco settings and (re-)init current loco
            initLocoList()
        }

        LanbahnPanelApplication.expireAllPanelElements()
        LanbahnPanelApplication.requestAllPanelData()

        mHandler.postDelayed({ updateUI() }, 500)

        (application as LanbahnPanelApplication).removeNotification()
        title = "Lanbahn Panel \"$panelName\""

    }

    private fun setActionBarBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return // does not work for old android versions

        when (prefs.getString(KEY_STYLE_PREF,"US")) {
            "US" -> supportActionBar?.setBackgroundDrawable(ColorDrawable(-0xff990f)) //Color.BLUE))  // TODO color ??
            "UK" -> supportActionBar?.setBackgroundDrawable(ColorDrawable(-0xdfAAdd))  // kotlin: 0xffff0000.toInt()
            "DE" -> supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLUE))
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        mOptionsMenu = menu
        setConnectionIcon()
        enableForQuadrantButtons(prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false))
        displayLockAndRoutingState()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_settings // call preferences activity
            -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }

            R.id.action_connect -> {
                toast("trying reconnect")
                restartCommFlag = true
                return true
            }
            R.id.action_q1 -> {
                if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)) displayQuadrant(1)
                return true
            }
            R.id.action_q2 -> {
                if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)) displayQuadrant(2)
                return true
            }
            R.id.action_q3 -> {
                if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)) displayQuadrant(3)
                return true
            }
            R.id.action_q4 -> {
                if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)) displayQuadrant(4)
                return true
            }
            R.id.action_qall -> {
                if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false)) displayQuadrant(0)
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
            R.id.menu_flip_panel -> {
                PanelElement.flipPanel()
                return true
            }

            /* R.id.menu_check_service -> {
                //TODO int num = mService.getRandomNumber();
                toast("not implemented")
                // "number: " + num, Toast.LENGTH_SHORT).show();
                return true
            } */
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
        sendQ.clear()
        if (client != null) {
            Log.d(TAG, "LanbahnPanelActivity - shutdown client.")
            client?.shutdown()
            threadSleep(100) // give client some time to shut down.
        }


        Log.d(TAG, "Sart of Communication to Comm.Station")
        startCommunication()

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                selectedLoco?.timer()
                if (restartCommFlag) {
                    restartCommFlag = false
                    runOnUiThread {
                        Log.d(TAG, "restarting Communication to Comm.Station")
                        startCommunication()
                    }

                }
            }
        }, 100, 100)

        // request updates for all channels used in Panel is now done in "OnResume"

    }

    /**
     * remark: always running from UI Thread
     *
     * @return
     */
    fun startCommunication() {
        Log.d(TAG, "LahnbahnPanelActivity - startCommunication.")
        if (client != null) {
            client?.shutdown()
            Thread.sleep(100) // give client some time to shut down.
        }

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val ip = prefs.getString(KEY_IP, DEFAULT_SXNET_IP)
        val port = Integer.parseInt(prefs.getString(KEY_PORT, DEFAULT_SXNET_PORT))

        client = Railroad(ip!!, port)
        client?.start()

        /*
        // check connection after some seconds (async) (socket creation has 5sec timeout)
        val handler = Handler()
        handler.postDelayed({
            if (connectionIsAlive()) {
                LanbahnPanelApplication.requestAllPanelData()
            } else {
                val msg = " NO CONNECTION TO $ip:$port! Check WiFi/SSID and server settings "
               longToast(msg)
                conn_state_string = "NOT CONNECTED"
            }
        }, 6500)
*/
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
        //displayLockAndRoutingState()
        Route.auto()

        /* TODO check which frequency is needed */
        if ((counter.rem(4) == 0) and (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) and (selectedLoco != null)) {
            val adr = selectedLoco?.adr ?: INVALID_INT
            Commands.readLocoData(adr)
        }

        mHandler.postDelayed({ updateUI() }, 500)
    }

    private fun enableForQuadrantButtons(yes: Boolean) {
        if (mOptionsMenu == null) return

        if (DEBUG) Log.d(TAG, "enableForQuadrantButtons($yes)")
        if (yes) {
            mOptionsMenu?.findItem(R.id.action_q1)?.setIcon(R.drawable.q1_v2_48_gray)
            mOptionsMenu?.findItem(R.id.action_q2)?.setIcon(R.drawable.q2_v2_48_gray)
            mOptionsMenu?.findItem(R.id.action_q3)?.setIcon(R.drawable.q3_v2_48_gray)
            mOptionsMenu?.findItem(R.id.action_q4)?.setIcon(R.drawable.q4_v2_48_gray)
            mOptionsMenu?.findItem(R.id.action_qall)?.setIcon(R.drawable.qa_v2_48_gray)
            when (prefs.getInt(KEY_QUADRANT, 0)) {  // mark selected quadrant "white"
                0 -> mOptionsMenu?.findItem(R.id.action_qall)?.setIcon(R.drawable.qa_v2_48)
                1 -> mOptionsMenu?.findItem(R.id.action_q1)?.setIcon(R.drawable.q1_v2_48)
                2 -> mOptionsMenu?.findItem(R.id.action_q2)?.setIcon(R.drawable.q2_v2_48)
                3 -> mOptionsMenu?.findItem(R.id.action_q3)?.setIcon(R.drawable.q3_v2_48)
                4 -> mOptionsMenu?.findItem(R.id.action_q4)?.setIcon(R.drawable.q4_v2_48)
            }


        } else {
            mOptionsMenu?.findItem(R.id.action_q1)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_q2)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_q3)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_q4)?.setIcon(R.drawable.trans_48)
            mOptionsMenu?.findItem(R.id.action_qall)?.setIcon(R.drawable.trans_48)
        }
    }

    private fun togglePower() {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val allowed = prefs.getBoolean(KEY_ENABLE_POWER_CONTROL, false)
        if ((client == null) or (!client!!.isConnected())) {
            toast("ERROR: not connected - cannot set global power state")
        } else if (allowed) {
            when (globalPower) {
                POWER_OFF -> Commands.setPower(1)
                POWER_ON -> Commands.setPower(0)
                POWER_UNKNOWN -> Commands.setPower(1)
            }
        } else {
            toast("not allowed to set global power state, check settings")
        }
    }


    private fun displayLockAndRoutingState() {
        if (DEBUG) Log.d(TAG, "selectedScale = ${prefs.getString(KEY_SCALE_PREF,"auto")}")
        when (prefs.getString(KEY_SCALE_PREF,"auto")) {
            "auto" -> mOptionsMenu?.findItem(R.id.action_lock_state)?.setIcon(R.drawable.ic_letter_a)
            "manual" -> mOptionsMenu?.findItem(R.id.action_lock_state)?.setIcon(R.drawable.ic_lock_open_white_48dp)
            "locked" -> mOptionsMenu?.findItem(R.id.action_lock_state)?.setIcon(R.drawable.ic_lock_white_48dp)
        }
        when (prefs.getBoolean(KEY_ROUTES,false)) {
            true -> mOptionsMenu?.findItem(R.id.action_routing)?.setIcon(R.drawable.ic_letter_r)
            false -> mOptionsMenu?.findItem(R.id.action_routing)?.setIcon(R.drawable.ic_letter_non_r)
         }
    }

    private fun displayQuadrant(q: Int) {
        // 0 means "ALL"
        val editor = prefs.edit()
        if (prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false) ) {
            editor.putInt(KEY_QUADRANT, q)
        } else {
            // must use "0" !!
            editor.putInt(KEY_QUADRANT, 0)
        }
        editor.commit()
        if (prefs.getString(KEY_SCALE_PREF,"auto") == "auto") {
            LanbahnPanelApplication.calcAutoScale(mWidth, mHeight, prefs.getInt(KEY_QUADRANT,0))
        }
        // ??? TODO check if necessary here
        //  enableForQuadrantButtons(prefs.getBoolean(KEY_FIVE_VIEWS_PREF,false))

    }

    private fun initLocoList() {

        val lastLocoAddress = prefs.getInt(KEY_LOCO_ADR, 3)

        if (locolist == null) {
            Toast.makeText(this, "could not read locolist from config xml file or errors in file", Toast.LENGTH_LONG).show()
            Log.e(TAG, "could not read loco list")
        } else {
            // if last loco (from stored loco_address) is in list then use this loco
            for (loco in locolist) {
                if (loco.adr === lastLocoAddress) {
                    selectedLoco = loco // update from file
                    selectedLoco?.initFromSX()
                }
            }

        }

        if (selectedLoco == null) { // use first loco in list or use default
            if (locolist.size >= 1) {
                selectedLoco = locolist[0]  // first loco in xml file
            } else {
                // as a default use a "dummy loco"
                val locoMass = Integer
                        .parseInt(prefs.getString(KEY_LOCO_MASS, "3")!!)
                val locoName = prefs.getString(KEY_LOCO_NAME, "default loco 22")
                selectedLoco = Loco(locoName, lastLocoAddress, locoMass)
                selectedLoco?.initFromSX()
            }
            if (DEBUG) Log.d(TAG, "selectedLoco adr=" + selectedLoco?.adr)
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

    /** display powert station only when the connection to the command station is on
     *
     */
    private fun setPowerStateIcon() {

        if (cmdStationConnection == CMD_STATION_OFF) {
            mOptionsMenu?.findItem(R.id.action_power)?.setIcon(R.drawable.cmd_station_off)
        } else {
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

    private fun reloadConfigIfPanelFileChanged(): Boolean {
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        val cfFilename = prefs.getString(KEY_CONFIG_FILE, "-")
        var result = false
        // panel file changed or empty panel => (re-)load panel
        if ((cfFilename != configFilename) || (panelElements.size == 0)) {
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
            saveStates()
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(this)
            val server = prefs.getString(KEY_IP, "")
            val urlConfig = "http://$server:8000/config"
            getPanel(urlConfig)

        } else {
            longToast("ERROR: App has NO PERMISSION to write files !")
        }
    }

    private fun getPanel(url: String) {

        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {// We cannot read/write the media
            Log.e(TAG, "external storage not available or not writeable")
            toast("external storage not available or not writeable")
            return
        }

        doAsync {
            val (res, content) = Download(url).run()
            uiThread {
                if (res) {
                    //Log.d(TAG, content)
                    // content == filename
                    longToast("file read => $content")

                    Log.d(TAG, "config file read => reloading config from $content")
                    ReadConfig.readConfigFromFile(appContext!!)
                    loadStates()
                    initLocoList()

                } else {

                    longToast("ERROR: config file NOT read")
                    Log.e(TAG, "config file NOT read - ERROR:\n$content")  // content == error message

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

    }


}
