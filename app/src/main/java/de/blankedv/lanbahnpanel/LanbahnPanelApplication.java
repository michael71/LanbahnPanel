package de.blankedv.lanbahnpanel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import static de.blankedv.lanbahnpanel.Constants.KEY_DRAW_ADR;
import static de.blankedv.lanbahnpanel.Constants.KEY_DRAW_ADR2;
import static de.blankedv.lanbahnpanel.Constants.KEY_ENABLE_ALL_ADDRESSES;
import static de.blankedv.lanbahnpanel.Constants.KEY_ENABLE_EDIT;
import static de.blankedv.lanbahnpanel.Constants.KEY_ENABLE_ZOOM;
import static de.blankedv.lanbahnpanel.Constants.KEY_FLIP;
import static de.blankedv.lanbahnpanel.Constants.KEY_ROUTES;
import static de.blankedv.lanbahnpanel.Constants.KEY_SAVE_STATES;
import static de.blankedv.lanbahnpanel.Constants.KEY_SCALE;
import static de.blankedv.lanbahnpanel.Constants.KEY_STYLE_PREF;
import static de.blankedv.lanbahnpanel.Constants.KEY_XOFF;
import static de.blankedv.lanbahnpanel.Constants.KEY_YOFF;
import static de.blankedv.lanbahnpanel.Constants.STATE_UNKNOWN;

import de.blankedv.lanbahnpanel.Constants.*;

/** Lanbahn Panel
 * Rev 3.1 - 28 Jun 2018 - now using sxnet protocol
 */

    // TODO: kotlin review and simplify
    // TODO: handle absence ot connection to SX command station
	// TODO move ControlAreaButtons to actionBar and remove all control area code

public class LanbahnPanelApplication extends Application {

	public static final boolean DEBUG = true; // enable or disable debugging
												// with file
	public static boolean noWifiFlag = false;

	public static final int SXNET_PORT = 4104;
	public static final int SXMAX = 112; // maximum sx channel number - only SX0 !
    public static final int LBMAX = 9999; // maximum lanbahn channel number

	public static int width, height;
	public static final String TAG = "LanbahnPanelActivity";
	public static String selectedStyle = "UK"; // German style or USS style

	public static ArrayList<PanelElement> panelElements = new ArrayList<>();
	public static ArrayList<Route> routes = new ArrayList<>();
	public static ArrayList<CompRoute> compRoutes = new ArrayList<>();
    public static ArrayList<LampGroup> lampGroups = new ArrayList<>();
    public static final int MAX_LAMP_BUTTONS = 4;

	public static String panelName = "";

	public static boolean drawAddresses = false;
	public static boolean drawAddresses2 = false;
	public static boolean flipUpsideDown = false;  //display all panel element from "other side"
	public static boolean saveStates;

	public static Handler handler; //

	// connection state
	public static SXnetClientThread client;
	private static long timeOfLastReceivedMessage = 0;
    public static boolean restartCommFlag = false;

    // put all messages which should be sent into this queue
	public static final BlockingQueue<String> sendQ = new ArrayBlockingQueue<>(
			200);

    public static final int TYPE_FEEDBACK_MSG = 2;
    public static final int TYPE_ERROR_MSG = 3;

	public static volatile String connString = "";

	public static final String DIRECTORY = "lanbahnpanel/";
    // with trailing slash !!

	public static String configFilename = "lb-panel1.xml";
	
	public static final String DEMO_FILE = "demo-panel.xml"; // demo data in raw
																// assets dir.

	public static boolean configHasChanged = false; // store info whether config
													// has changed
	// if true, then a new config file is written at the end of the Activity

	public static final int INVALID_INT = -1;
	public static final int LBP_NOTIFICATION_ID = 201; //arbitrary id for notification


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

	//@SuppressLint("HandlerLeak")
	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG)
			Log.d(TAG, "onCreate LanbahnPanelApplication");

		// do some initializations
		// for (int i=0; i<MAX_LANBAHN_ADDR; i++) lanbahnData[i]=0;
		AndroBitmaps.INSTANCE.init(getResources());
		LPaints.INSTANCE.init(prescale);

        String myAndroidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

		Log.d(TAG,"LanbahnPanelApplication - androidDeviceID="+myAndroidDeviceId);
		// scaling, zoom prefs are loaded from LanbahnPanelActivity

		// handler for receiving sxnet messages

		handler = new Handler() {    // TODO move message func. to ReceiveQueue
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				int chan = msg.arg1;
				//if (DEBUG) Log.d(TAG,"rec. msg for chan= "+chan);
				int data = msg.arg2;
				timeOfLastReceivedMessage = System.currentTimeMillis();
                if (what == TYPE_FEEDBACK_MSG) {
                    for (PanelElement pe : panelElements) {
                        if (pe.getAdr() == chan) {
							//               if (DEBUG) Log.d(TAG,"updating "+pe.toString());
                            pe.updateData(data);

                            // it is possible that two elements have the same channel
                            // therefor all channels are iterated
                        }
                    }

                    for (Route rt : routes) {
                        if (rt.getId() == chan) {
                            rt.updateData(data);
                        }
                    }

					for (LampGroup l : lampGroups) {
						if (l.getAdr() == chan) {
							if (data == 0) {
								l.switchOff();
							} else {
								l.switchOn();
							}
						}
					}

                }  else if (what == TYPE_ERROR_MSG) {
                    if (DEBUG) Log.d(TAG,"error msg "+chan+" "+data);
                    for (PanelElement pe : panelElements) {
                        if (pe.getAdr() == chan) {
                            pe.updateData(Constants.STATE_UNKNOWN);
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

	public static boolean connectionIsAlive() {
        if (client == null) {
			conn_state_string = "NOT CONNECTED";
            return false;
        } else {
            return client.isConnected();
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
		editor.putBoolean(Constants.KEY_ENABLE_ALL_ADDRESSES, enableAllAddresses);
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
		LPaints.INSTANCE.init(prescale);
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

    /**
     * Display OnGoing Notification that indicates Network Thread is still Running.
     * Currently called from LanbahnPanelActivity onPause, passing the current intent
     * to return to when reopening.
     */
    void addNotification(Intent notificationIntent) {
        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,channelId)
                        .setSmallIcon(R.drawable.lb_icon)
                        .setContentTitle(this.getString(R.string.notification_title))
                        .setContentText(this.getString(R.string.notification_text))
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, LBP_NOTIFICATION_ID, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Since android Oreo notification channel is needed.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId,
					"Lanbahn Channel",
					NotificationManager.IMPORTANCE_DEFAULT);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(LBP_NOTIFICATION_ID, builder.build());
    }

    // Remove notification
    void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(LBP_NOTIFICATION_ID);
    }
}
