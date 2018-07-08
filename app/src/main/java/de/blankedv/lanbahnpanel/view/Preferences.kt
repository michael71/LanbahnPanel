package de.blankedv.lanbahnpanel.view

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Environment
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.ListPreference
import android.preference.PreferenceManager
import android.util.Log
import de.blankedv.lanbahnpanel.R

import java.io.File
import java.util.ArrayList

import de.blankedv.lanbahnpanel.model.*

class Preferences : PreferenceActivity(), OnSharedPreferenceChangeListener {

    private var drawAddressesPref: CheckBoxPreference? = null
    private var enableZoomPref: CheckBoxPreference? = null
    private var enableEditPref: CheckBoxPreference? = null
    private var saveStatesPref: CheckBoxPreference? = null
    private var flipPref: CheckBoxPreference? = null
    private var routesPref: CheckBoxPreference? = null
    private var drawAddressesPref2: CheckBoxPreference? = null
    private var selectStylePref: ListPreference? = null
    private var configFilenamePref: ListPreference? = null
    private var ipPref: EditTextPreference? = null
    private var portPref: EditTextPreference? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
        //locoAdrPref = (EditTextPreference)getPreferenceScreen().findPreference(KEY_LOCO_ADR);
        drawAddressesPref = preferenceScreen.findPreference(KEY_DRAW_ADR) as CheckBoxPreference
        drawAddressesPref2 = preferenceScreen.findPreference(KEY_DRAW_ADR2) as CheckBoxPreference
        enableZoomPref = preferenceScreen.findPreference(KEY_ENABLE_ZOOM) as CheckBoxPreference
        enableEditPref = preferenceScreen.findPreference(KEY_ENABLE_EDIT) as CheckBoxPreference
        saveStatesPref = preferenceScreen.findPreference(KEY_SAVE_STATES) as CheckBoxPreference
        routesPref = preferenceScreen.findPreference(KEY_ROUTES) as CheckBoxPreference
        ipPref = preferenceScreen.findPreference(KEY_IP) as EditTextPreference
        portPref = preferenceScreen.findPreference(KEY_PORT) as EditTextPreference
        flipPref = preferenceScreen.findPreference(KEY_FLIP) as CheckBoxPreference

        selectStylePref = preferenceScreen.findPreference(KEY_STYLE_PREF) as ListPreference
        selectStylePref!!.summary = "current selected style is " + selectedStyle!!

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        ipPref!!.summary = "= " + prefs.getString(KEY_IP, DEFAULT_SXNET_IP)!!
        portPref!!.summary = "= " + prefs.getString(KEY_PORT, DEFAULT_SXNET_PORT)!!
        selectStylePref!!.summary = "current selected style is " + prefs.getString(KEY_STYLE_PREF, "?")!!
        configFilenamePref = preferenceScreen.findPreference(KEY_CONFIG_FILE) as ListPreference
        //locosFilenamePref = (ListPreference)getPreferenceScreen().findPreference(KEY_LOCOS_FILE);
        //val extCat = findPreference("extended_cat") as PreferenceCategory


        val entries = matchingXMLFiles()
        if (entries != null) configFilenamePref!!.entries = entries
        if (entries != null) configFilenamePref!!.entryValues = entries

        configFilenamePref!!.summary = "config loaded from " + prefs.getString(KEY_CONFIG_FILE, "-")!!
        // locosFilenamePref.setSummary("locos loaded from "+prefs.getString(KEY_LOCOS_FILE,"-"));

        selectStylePref = preferenceScreen.findPreference(KEY_STYLE_PREF) as ListPreference
        selectStylePref!!.summary = "current selected style is " + selectedStyle!!

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {

        // Let's do something if a preference value changes
        when (key) {
            KEY_DRAW_ADR -> {
                drawAddresses = sharedPreferences.getBoolean(KEY_DRAW_ADR, false)
                Log.d(TAG, "drawAddresses changed.")
            }
            KEY_DRAW_ADR2 -> {
                drawAddresses2 = sharedPreferences.getBoolean(KEY_DRAW_ADR2, false)
                Log.d(TAG, "drawAddresses2 changed.")
            }
            KEY_FLIP -> {
                flipUpsideDown = sharedPreferences.getBoolean(KEY_FLIP, false)
                Log.d(TAG, "upside down changed.")
            }
            KEY_ENABLE_ZOOM -> {
                zoomEnabled = sharedPreferences.getBoolean(KEY_ENABLE_ZOOM, false)
                Log.d(TAG, "zoomEnabled changed.")
            }
            KEY_STYLE_PREF -> {
                selectedStyle = sharedPreferences.getString(KEY_STYLE_PREF, "US")
                Log.d(TAG, "selectedStyle = " + selectedStyle!!)
                selectStylePref!!.summary = "current selected style is " + selectedStyle!!
            }
            KEY_SAVE_STATES -> {
                saveStates = sharedPreferences.getBoolean(KEY_SAVE_STATES, false)
                Log.d(TAG, "saveStates changed =$saveStates")
            }
            KEY_ENABLE_EDIT -> enableEdit = sharedPreferences.getBoolean(KEY_ENABLE_EDIT, false)
            KEY_IP -> ipPref!!.summary = "= " + sharedPreferences.getString(KEY_IP, DEFAULT_SXNET_IP)!!
            KEY_PORT -> portPref!!.summary = "= " + sharedPreferences.getString(KEY_PORT, DEFAULT_SXNET_PORT)!!
            KEY_CONFIG_FILE -> configFilenamePref!!.summary = "config loaded from " + sharedPreferences.getString(KEY_CONFIG_FILE, "-")!!
            else -> Log.e(TAG, "unhandled preferences change, key=$key")
        }

        /*} no loco control
		  else  if (key.equals(KEY_LOCO_ADR)) {
			int newLocoAdr =0;
			try {
				newLocoAdr = Integer.parseInt(sharedPreferences.getString(KEY_LOCO_ADR,"22"));
				locoAdrPref.setSummary("= "+sharedPreferences.getString(KEY_LOCO_ADR,"22"));
			} catch (NumberFormatException e) {
				Log.e(TAG,"invalid loco address");
			}

			if ((newLocoAdr>0) && (newLocoAdr<=103)) {
				LanbahnPanelApplication.setLocoAdr(newLocoAdr);

			}
		} */

    }

    override fun onResume() {
        super.onResume()

        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
        ipPref!!.summary = "= " + prefs.getString(KEY_IP, "")!!
        portPref!!.summary = "= " + prefs.getString(KEY_PORT, DEFAULT_SXNET_PORT)!!
        selectStylePref!!.summary = "current selected style is " + prefs.getString(KEY_STYLE_PREF, "?")!!
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun matchingXMLFiles(): Array<String?>? {

        if ((Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
                and (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)) {
            // Something  is wrong
            Log.d(TAG, "cannot read ExternalStorage Directory ")
            return null
        }

        val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY)
        Log.d(TAG, "reading directory " + dir.absolutePath)

        val files = ArrayList<String>()
        for (filename in dir.list()) {
            if ((filename.startsWith("panel")) and (filename.endsWith(".xml"))) {
                files.add(filename)
            }
        }
        if (files.size > 0) {
            val array = arrayOfNulls<String>(files.size)
            return files.toArray(array)
        } else {
            return null
        }
    }

}