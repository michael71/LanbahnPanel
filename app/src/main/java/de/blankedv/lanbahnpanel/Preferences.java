package de.blankedv.lanbahnpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @SuppressWarnings("unused")
    private CheckBoxPreference drawAddressesPref, enableZoomPref, enableEditPref, saveStatesPref,
            flipPref, routesPref, enableAllAddressesPref, drawAddressesPref2;
    private ListPreference selectStylePref, configFilenamePref;
    private EditTextPreference ipPref;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        //locoAdrPref = (EditTextPreference)getPreferenceScreen().findPreference(KEY_LOCO_ADR);
        drawAddressesPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_DRAW_ADR);
        drawAddressesPref2 = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_DRAW_ADR2);
        enableZoomPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ENABLE_ZOOM);
        enableEditPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ENABLE_EDIT);
        saveStatesPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SAVE_STATES);
        routesPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ROUTES);
        ipPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_IP);
        flipPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_FLIP);
        enableAllAddressesPref = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ENABLE_ALL_ADDRESSES);
        selectStylePref = (ListPreference) getPreferenceScreen().findPreference(KEY_STYLE_PREF);
        selectStylePref.setSummary("current selected style is " + selectedStyle);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        ipPref.setSummary("= " + prefs.getString(KEY_IP, ""));
        selectStylePref.setSummary("current selected style is " + prefs.getString(KEY_STYLE_PREF, "?"));
        configFilenamePref = (ListPreference) getPreferenceScreen().findPreference(KEY_CONFIG_FILE);
        //locosFilenamePref = (ListPreference)getPreferenceScreen().findPreference(KEY_LOCOS_FILE);
        PreferenceCategory extCat = (PreferenceCategory) findPreference("extended_cat");

        String[] allfiles = allFiles();
        CharSequence[] entries = matchingFiles("panel", allfiles);
        CharSequence[] entryValues = entries;
        if (entries != null) configFilenamePref.setEntries(entries);
        if (entryValues != null) configFilenamePref.setEntryValues(entryValues);

        configFilenamePref.setSummary("config loaded from " + prefs.getString(KEY_CONFIG_FILE, "-"));
        // locosFilenamePref.setSummary("locos loaded from "+prefs.getString(KEY_LOCOS_FILE,"-"));

        selectStylePref = (ListPreference) getPreferenceScreen().findPreference(KEY_STYLE_PREF);
        selectStylePref.setSummary("current selected style is " + selectedStyle);

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        // Let's do something if a preference value changes
        switch (key) {
            case KEY_DRAW_ADR:
                drawAddresses = sharedPreferences.getBoolean(KEY_DRAW_ADR, false);
                Log.d(TAG, "drawAddresses changed.");
                break;
            case KEY_DRAW_ADR2:
                drawAddresses2 = sharedPreferences.getBoolean(KEY_DRAW_ADR2, false);
                Log.d(TAG, "drawAddresses2 changed.");
                break;
            case KEY_FLIP:
                flipUpsideDown = sharedPreferences.getBoolean(KEY_FLIP, false);
                Log.d(TAG, "upside down changed.");
                break;
            case KEY_ENABLE_ZOOM:
                zoomEnabled = sharedPreferences.getBoolean(KEY_ENABLE_ZOOM, false);
                Log.d(TAG, "zoomEnabled changed.");
                break;
            case KEY_STYLE_PREF:
                selectedStyle = sharedPreferences.getString(KEY_STYLE_PREF, "US");
                Log.d(TAG, "selectedStyle = " + selectedStyle);
                selectStylePref.setSummary("current selected style is " + selectedStyle);
                break;
            case KEY_SAVE_STATES:
                saveStates = sharedPreferences.getBoolean(KEY_SAVE_STATES, false);
                Log.d(TAG, "saveStates changed =" + saveStates);
                break;
            case KEY_ENABLE_EDIT:
                enableEdit = sharedPreferences.getBoolean(KEY_ENABLE_EDIT, false);
                break;
            case KEY_ENABLE_ALL_ADDRESSES:
                enableAllAddresses = sharedPreferences.getBoolean(KEY_ENABLE_ALL_ADDRESSES, false);
                break;
            case KEY_IP:
                ipPref.setSummary("= " + sharedPreferences.getString(KEY_IP, ""));
                break;
            case KEY_CONFIG_FILE:
                configFilenamePref.setSummary("config loaded from " + sharedPreferences.getString(KEY_CONFIG_FILE, "-"));
                break;
            default:
                Log.e(TAG, "unhandled preferences change, key=" + key);
                break;
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

    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        ipPref.setSummary("= " + prefs.getString(KEY_IP, ""));
        selectStylePref.setSummary("current selected style is " + prefs.getString(KEY_STYLE_PREF, "?"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private String[] allFiles() {
        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }

        if (mExternalStorageAvailable) {

            File dir = new File(Environment.getExternalStorageDirectory() + "/" + DIRECTORY);
            Log.d(TAG, "reading directory " + dir.getAbsolutePath());
            String[] allfiles = dir.list();

            return allfiles;


        } else {
            Log.d(TAG, "cannot read externalStorageDirectory ");
            return null;
        }

    }

    private String[] matchingFiles(String match, String[] all) {
        if (all == null) return null;
        ArrayList<String> files = new ArrayList<String>();
        for (String s : all) {
            int i = s.indexOf(match);
            if (i == 0) {
                // found a filename beginning with content of "match"
                files.add(s);
            }
        }
        //	return (String[]) files.toArray(); funktioniert nicht ...
        int size = files.size();
        if (size > 0) {
            String[] fl = new String[size];
            for (int i = 0; i < size; i++) {
                fl[i] = files.get(i);
            }
            return fl;
        } else {
            return null;
        }
    }


}