package de.blankedv.lanbahnpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.ListPreference;
import android.util.Log;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@SuppressWarnings("unused")
	private CheckBoxPreference drawAddressesPref, enableZoomPref, enableEditPref, saveStatesPref, 
	                                        flipPref,routesPref,enableAllAddressesPref, drawAddressesPref2;
	private ListPreference selectStylePref;

	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 

		addPreferencesFromResource(R.xml.preferences); 
		//locoAdrPref = (EditTextPreference)getPreferenceScreen().findPreference(KEY_LOCO_ADR);
		drawAddressesPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_DRAW_ADR);
		drawAddressesPref2 = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_DRAW_ADR2);
		enableZoomPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_ZOOM);
		enableEditPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_EDIT);   
		saveStatesPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_SAVE_STATES);
		routesPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ROUTES);
		flipPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_FLIP);
		enableAllAddressesPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_ALL_ADDRESSES); 
		selectStylePref = (ListPreference)getPreferenceScreen().findPreference(KEY_STYLE_PREF);
		selectStylePref.setSummary("current selected style is "+selectedStyle);
		/*PreferenceCategory extCat = (PreferenceCategory) findPreference("extended_cat");
		*/

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		// Let's do something if a preference value changes
		if (key.equals(KEY_DRAW_ADR)) {
			drawAddresses = sharedPreferences.getBoolean(KEY_DRAW_ADR,false);
			Log.d(TAG,"drawAddresses changed.");
		} else if (key.equals(KEY_DRAW_ADR2)) {
			drawAddresses2 = sharedPreferences.getBoolean(KEY_DRAW_ADR2,false);
				Log.d(TAG,"drawAddresses2 changed.");
		} else if (key.equals(KEY_FLIP)) {
			flipUpsideDown = sharedPreferences.getBoolean(KEY_FLIP,false);
			Log.d(TAG,"upside down changed.");
			
	   }  else if (key.equals(KEY_ENABLE_ZOOM)) {
			zoomEnabled = sharedPreferences.getBoolean(KEY_ENABLE_ZOOM, false);
			Log.d(TAG,"zoomEnabled changed.");
		}  else if (key.equals(KEY_STYLE_PREF)) {
			selectedStyle = sharedPreferences.getString(KEY_STYLE_PREF,"US");
			Log.d(TAG,"selectedStyle = "+selectedStyle);
			selectStylePref.setSummary("current selected style is "+selectedStyle);
		} else if (key.equals(KEY_SAVE_STATES)) {
			saveStates = sharedPreferences.getBoolean(KEY_SAVE_STATES, false);
			Log.d(TAG,"saveStates changed ="+saveStates);
		} else if (key.equals(KEY_ENABLE_EDIT)) {
			enableEdit = sharedPreferences.getBoolean(KEY_ENABLE_EDIT, false);
		} else if (key.equals(KEY_ENABLE_ALL_ADDRESSES)) {
			enableAllAddresses = sharedPreferences.getBoolean(KEY_ENABLE_ALL_ADDRESSES, false);
		} else {
			Log.e(TAG,"unhandled preferences change, key="+key);
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

		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
		// Setup the initial values
		// ipPref.setSummary("= "+prefs.getString(KEY_IP,""));
		// portPref.setSummary("= "+prefs.getString(KEY_PORT,""));
		// locoAdrPref.setSummary("= "+prefs.getString(KEY_LOCO_ADR,""));

		// Set up a listener whenever a key changes            
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes            
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}



}