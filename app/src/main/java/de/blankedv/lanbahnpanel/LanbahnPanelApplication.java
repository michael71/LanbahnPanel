package de.blankedv.lanbahnpanel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import static de.blankedv.lanbahnpanel.ActivePanelElement.STATE_UNKNOWN;

/** Lanbahn Panel
 * Rev 2.0 - 03 Jan 2015 - now using LANBAHN protocol Rev. 2.0
 */
public class LanbahnPanelApplication extends Application {

	public static final boolean DEBUG = true; // enable or disable debugging
												// with file
	public static final boolean DEBUG_COMM = true; // debugging of all lanbahn
													// msgs
    public static boolean comm1;

	public static boolean demoFlag = false;
	public static boolean noWifiFlag = false;

	public static final int SXNET_PORT = 4104;
    public static final int SXMAX = 128; // maximum sx channel number

	public static int width, height;
	public static final String TAG = "LanbahnPanelActivity";
	public static String selectedStyle = "UK"; // German style or USS style

	public static ArrayList<PanelElement> panelElements = new ArrayList<>();
	public static ArrayList<Route> routes = new ArrayList<>();
	public static ArrayList<CompRoute> compRoutes = new ArrayList<>();
    public static ArrayList<LampGroup> lampButtons = new ArrayList<>();
    public static final int MAX_LAMP_BUTTONS = 4;
    private static int[] sxData = new int[SXMAX];   // contains all selectrix channel data

	public static String panelName = "";

	public static boolean drawAddresses = false;
	public static boolean drawAddresses2 = false;
	public static boolean flipUpsideDown = false;  //display all panel element from "other side"
	public static boolean saveStates;

	// preferences
	// public static final String KEY_LOCO_ADR = "locoAdrPref";
	public static final String KEY_DRAW_ADR = "drawAddressesPref";
	public static final String KEY_DRAW_ADR2 = "drawAddressesPref2";
	public static final String KEY_STYLE_PREF = "selectStylePref";
	public static final String KEY_ENABLE_ZOOM = "enableZoomPref";
	public static final String KEY_ENABLE_EDIT = "enableEditPref";
	public static final String KEY_SAVE_STATES = "saveStatesPref";
	public static final String KEY_ROUTES = "routesPref";
	public static final String KEY_FLIP = "flipPref";
	public static final String KEY_ENABLE_ALL_ADDRESSES = "enableAllAddressesPref";
	public static final String KEY_XOFF = "xoffPref";
	public static final String KEY_YOFF = "yoffPref";
	public static final String KEY_SCALE = "scalePref";
	public static final String KEY_IP = "ipPref";
    public static final String KEY_CONFIG_FILE="configFilenamePref";
	public static Handler handler; //

	// connection state
	public static SXnetClientThread client;
	private static long timeOfLastReceivedMessage = 0;
    public static boolean restartCommFlag = false;

    // put all messages which should be sent into this queue
	public static final BlockingQueue<String> sendQ = new ArrayBlockingQueue<>(
			200);

	public static final int TYPE_STATUS_MSG = 0;
	public static final int TYPE_ROUTE_MSG = 1;
    public static final int TYPE_FEEDBACK_MSG = 2;
    public static final int TYPE_ERROR_MSG = 3;

	public static String connString = "";

	public static final String LOCAL_DIRECTORY = "lanbahnpanel/"; // with trailing
															// slash
	
	public static final String CONFIG_FILENAME = "lb-panel1.xml";
	
	public static final String DEMO_FILE = "demo-panel.xml"; // demo data in raw
																// assets dir.

	public static boolean configHasChanged = false; // store info whether config
													// has changed
	// if true, then a new config file is written at the end of the Activity

	public static final int INVALID_INT = -9999;
    public static final int INVALID_LANBAHN_DATA = 999;

	public static final Hashtable<String, Bitmap> bitmaps = new Hashtable<>();

	public static boolean zoomEnabled;
	public static float scale = 1.0f; // user selectable scaling of panel area

	// fixed prefix for scaling - should be =1 for small displays and =2 for
	// large displays
	// all Paints and x/y-s are scaled before drawing
	public static final int prescale = 2;

	public static final int RASTER = (int) (20 * prescale); // raster points
															// with xx pixels
	public static final int TURNOUT_LENGTH = 10; // NOT to be prescaled
	public static final int TURNOUT_LENGTH_LONG = (int) (TURNOUT_LENGTH * 1.4f);
	public static float xoff = 10 * prescale;
	public static float yoff = 50 * prescale;
	// public static Bitmap myBitmap = Bitmap.createBitmap(4000,1600,
	// Bitmap.Config.ARGB_4444);
	public static Bitmap myBitmap = Bitmap.createBitmap(2000, 800,
			Bitmap.Config.ARGB_4444);
	public static Canvas myCanvas = new Canvas(myBitmap);

	public static boolean enableEdit = false;
	public static boolean enableRoutes = false;
	// enable edit mode for lanbahn addresses in panel.

	public static boolean enableAllAddresses = false;
	// if true, ALL lanbahn addresses from 1..MAX_LANBAHN_ADDRESS can be select
	// if false => only addresses which were announced before can be selected.

	public static boolean clearRouteButtonActive = false; // state of clear
															// routes button
    public static String conn_state_string = "?";

	public static Context appContext;
	
	public static boolean lampState[];   // TODO

	// @SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG)
			Log.d(TAG, "onCreate LanbahnPanelApplication");

		// do some initializations
		// for (int i=0; i<MAX_LANBAHN_ADDR; i++) lanbahnData[i]=0;
		AndroBitmaps.init(getResources());
		LinePaints.init(prescale);

        // do some initializations
        for (int i = 0; i < sxData.length; i++) {
            sxData[i] = 0;
        }

		// scaling, zoom prefs are loaded from LanbahnPanelActivity

		// handler for receiving sxnet messages

		handler = new Handler() {    // TODO move message func. to ReceiveQueue
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what; 
				int chan = msg.arg1;
				int data = msg.arg2;
				timeOfLastReceivedMessage = System.currentTimeMillis();
				if (what == TYPE_STATUS_MSG) {
					for (PanelElement pe : panelElements) {
						if (pe.getAdr() == chan) {
							pe.updateData(data);
						}
					}
				} else if (what == TYPE_ROUTE_MSG) {
					for (Route rt : routes) {
						if (rt.id == chan) {
							rt.updateData(data);
						}
					}
				} else if (what == TYPE_FEEDBACK_MSG) {
                    if (DEBUG) Log.d(TAG,"feedback msg "+chan+" "+data);
                    if (chan < SXMAX) {
                        // sx data
                        sxData[chan] = data;
                    } else {
                        // lanbahn data >128
                    }

                    for (PanelElement pe : panelElements) {
                        if (pe.getAdr() == chan) {
                            pe.updateData(data);
                        }
                    }
                }  else if (what == TYPE_ERROR_MSG) {
                    if (DEBUG) Log.d(TAG,"error msg "+chan+" "+data);
                    for (PanelElement pe : panelElements) {
                        if (pe.getAdr() == chan) {
                            pe.updateData(STATE_UNKNOWN);
                        }
                    }
                }

			}
		};

	}


	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG, "AndroPanelApp - terminating.");

	}

	public static void updatePanelData() {
		Log.d(TAG, "AndroPanelApp - updatePanelData()");
		for (PanelElement e : panelElements) {
			if (e instanceof ActivePanelElement) {
				// add its address to list of interesting addresses
				// only needed for active elements, not for tracks
				int a = e.getAdr();

				if ((a != INVALID_INT) && ((ActivePanelElement) e).isExpired()) {
					boolean success = sendQ.offer("READ " + a); // request data for
																// all active
																// addresses
					if (!success)
						Log.e(TAG, "sendQ full");
				}
			}
		}
	}

	/**
	 * needs to be executed always at shutdown to have a state of "UNKNOWN" when
	 * no current data at application restart
	 */
	public static void clearPanelData() {
		for (PanelElement e : panelElements) {
			if (e instanceof ActivePanelElement) {
				// add its address to list of interesting addresses
				// only needed for active elements, not for tracks
				e.setState(STATE_UNKNOWN);

			}
		}
	}

   	/* public static boolean isPowerOn() {
		return true; // TODO must evaluate stored lanbahn messages

	}  */

	public void saveZoomEtc() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		Log.d(TAG, "saving Zoom etc");
		editor.putBoolean(KEY_DRAW_ADR, drawAddresses);
		editor.putBoolean(KEY_DRAW_ADR2, drawAddresses2);
		editor.putString(KEY_STYLE_PREF, selectedStyle);
		editor.putBoolean(KEY_ENABLE_ZOOM, zoomEnabled);
		editor.putBoolean(KEY_ENABLE_EDIT, enableEdit);
		editor.putBoolean(KEY_SAVE_STATES, saveStates);
		editor.putBoolean(KEY_ROUTES, enableRoutes);
		editor.putBoolean(KEY_FLIP,flipUpsideDown);
		editor.putBoolean(KEY_ENABLE_ALL_ADDRESSES, enableAllAddresses);
		editor.putString(KEY_XOFF, "" + xoff);
		editor.putString(KEY_YOFF, "" + yoff);
		editor.putString(KEY_SCALE, "" + scale);

		// Commit the edits!
		editor.apply();
	}

	public void loadZoomEtc() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		zoomEnabled = prefs.getBoolean(KEY_ENABLE_ZOOM, false);
		Log.d(TAG, "zoomEnabled=" + zoomEnabled);
		selectedStyle = prefs.getString(KEY_STYLE_PREF, "US");
		LinePaints.init(prescale);
		enableEdit = prefs.getBoolean(KEY_ENABLE_EDIT, false);
		saveStates = prefs.getBoolean(KEY_SAVE_STATES, false);
		if (DEBUG)
			Log.d(TAG, "saveStates=" + saveStates);
		enableAllAddresses = prefs.getBoolean(KEY_ENABLE_ALL_ADDRESSES, false);

		drawAddresses = prefs.getBoolean(KEY_DRAW_ADR, false);
		drawAddresses2 = prefs.getBoolean(KEY_DRAW_ADR2, false);
		enableRoutes = prefs.getBoolean(KEY_ROUTES, false);
		flipUpsideDown = prefs.getBoolean(KEY_FLIP,false);
		if (DEBUG)
			Log.d(TAG, "drawAddresses=" + drawAddresses);
		if (DEBUG)
			Log.d(TAG, "drawAddresses2=" + drawAddresses2);
		xoff = Float.parseFloat(prefs.getString(KEY_XOFF, "20"));
		yoff = Float.parseFloat(prefs.getString(KEY_YOFF, "50"));
		scale = Float.parseFloat(prefs.getString(KEY_SCALE, "1.0"));

	}

	public static boolean connectionIsAlive() {
		return ((System.currentTimeMillis() - timeOfLastReceivedMessage) < 30*1000);
	}

}
