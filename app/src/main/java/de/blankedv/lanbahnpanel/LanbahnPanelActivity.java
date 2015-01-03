package de.blankedv.lanbahnpanel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

/**
 * LanbahnPanelActivity is the MAIN activity of the lanbahn panel
 * 
 * @author mblank
 * 
 */
public class LanbahnPanelActivity extends Activity {
	Builder builder;

	public static PopupWindow popUp;
	public static LinearLayout layout;
	// public static View selAddressView;

	TextView tv;
	LayoutParams params;
	LinearLayout mainLayout;
	Button but;
	boolean click = true;
	private final String KEY_STATES = "states";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DEBUG)
			Log.d(TAG, "onCreate LanbahnPanelActivity");
		popUp = new PopupWindow(this);
		layout = new LinearLayout(this);
		appContext = this;
		tv = new TextView(this);

		but = new Button(this);
		but.setText("Click Me");

		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		//layout.setOrientation(LinearLayout.VERTICAL);  
		tv.setText("popup window with address...");
		layout.addView(tv, params);
		popUp.setContentView(layout);

		ParseConfig.readConfig(this);

		setContentView(new Panel(this));

		builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getApplicationContext().getString(R.string.exit_confirm))
				.setCancelable(false)
				.setPositiveButton(
						getApplicationContext().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								shutdownLanbahnClient();
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									Log.e(TAG,e.getMessage());
								}
								clearPanelData(); // needs to be done
													// to start
								// again with a state of "UNKNOWN" when no
								// current data
								finish();
							}
						})
				.setNegativeButton(
						getApplicationContext().getString(R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		openCommunication();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (DEBUG)
			Log.d(TAG, "onBackPressed - LanbahnPanelActivity");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG)
			Log.d(TAG, "onStop - LanbahnPanelActivity");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG)
			Log.d(TAG, "onPause - LanbahnPanelActivity");
		// firstStart=false; // flag to avoid re-connection call during
		// first
		// start
		// sendQ.add(DISCONNECT);
		((LanbahnPanelApplication) getApplication()).saveZoomEtc();
		if (configHasChanged)
			WriteConfig.writeToXML();
		if (saveStates)
			saveStates();
		sendQ.clear();
	}

	public void shutdownLanbahnClient() {
		Log.d(TAG, "LanbahnPanelActivity - shutting down Lanbahn Client.");
		if (client != null)
			client.shutdown();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG)
			Log.d(TAG, "onResume - LanbahnPanelActivity");
		sendQ.clear();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		if (DEBUG)
			Log.i(TAG, "metrics - w=" + width + "  h=" + height);

		((LanbahnPanelApplication) getApplication()).loadZoomEtc();

		if (saveStates)
			loadStates();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings: // call preferences activity
			startActivity(new Intent(this, Preferences.class));
			return (true);

		case R.id.menu_about: // call preferences activity
			startActivity(new Intent(this, AboutActivity.class));
			return (true);
		case R.id.menu_quit:
			AlertDialog alert = builder.create();
			alert.show();
			return (true);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openCommunication() {

		Log.d(TAG, "LanbahnPanelActivity - openCommunication.");
		if (client != null) {
			sendQ.clear();
			client.shutdown();
			try {
				Thread.sleep(100); // give client some time to shut down.
			} catch (InterruptedException e) {
				if (DEBUG)
					Log.e(TAG, "could not sleep...");
			}
		}
		sendQ.clear();
		client = new LanbahnClientThread(this);
		client.start();
		// request updates for all channels used in Panel
		LanbahnPanelApplication.updatePanelData();
	}

	public void saveStates() {
		StringBuilder sb = new StringBuilder();

		for (PanelElement pe : panelElements) {
			if (pe.getAdr() != INVALID_INT) {
				if (pe.getState() != INVALID_INT)
					sb.append(pe.getAdr()).append(",").append(pe.getState()).append(";");
			}

		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		Log.d(TAG, "saving States=" + sb.toString());
		editor.putString(KEY_STATES, sb.toString());
		editor.commit();
	}

	public void loadStates() {
		if (DEBUG)
			Log.d(TAG, "loading States");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String states = prefs.getString(KEY_STATES, "");
		if (states.length() == 0) {
			Log.d(TAG, "previous state of devices could not be read");
			return;
		}

		try {
			String[] keyvalues = states.split(";");
			for (String kv : keyvalues) {
				String[] s2 = kv.split(",");
				for (PanelElement pe : panelElements) {
					if (pe instanceof ActivePanelElement) {
						if (pe.getAdr() == Integer.parseInt(s2[0])) {
							pe.setState(Integer.parseInt(s2[1]));
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error during loadStates: " + e.getMessage());
		}

		Log.d(TAG, "states=" + states);

	}
}