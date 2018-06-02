package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import java.util.ArrayList;
import android.util.Log;

/**
 * composite route, i.e. a list of routes which build a new route, is only a
 * helper for ease of use, no more functionality than the "simple" Route which
 * it is comprised of
 * 
 * @author mblank
 * 
 */
public class CompRoute {

	int id; // must be unique
	int btn1, btn2;
	String routesString = ""; // identical to config string

	// route is comprised of a list of routes
	private ArrayList<Route> myroutes = new ArrayList<>();

	/**
	 * constructs a composite route
	 * 
	 * 
	 */
	public CompRoute(int id, int btn1, int btn2, String sRoutes) {
		//
		this.btn1 = btn1;
		this.btn2 = btn2;
		this.id = id;

		// this string written back to config file.
		this.routesString = sRoutes;

		if (DEBUG)
			Log.d(TAG, "creating comproute id=" + id);

		// routes = "12,13": these routes need to be activated.
		String[] iID = routesString.split(",");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < iID.length; i++) {
			int routeID = Integer.parseInt(iID[i]);
			for (Route rt : routes) {
				try {
					if (rt.id == routeID) {
						myroutes.add(rt);
					}
				} catch (NumberFormatException e) {
				}
			}
		}
		if (DEBUG)
			Log.d(TAG, myroutes.size() + " routes in this route.");

	}

/*	no clear for compound routes because single routes are cleared automatically after X seconds
    public void clear() {

		if (DEBUG)
			Log.d(TAG, "clearing comproute id=" + id);

		for (Route rt : myroutes) {
			if (rt.active == true) {
				rt.clear();
			}

		}

	} */

	public void clearOffendingRoutes() {
		if (DEBUG)
			Log.d(TAG, "clearing (active) offending Routes");
		for (Route rt : myroutes) {
			rt.clearOffendingRoutes();

		}
	}

	/** set all the single routes which depend on this compound route
	 *
	 */
	public void set() {

		if (DEBUG)
			Log.d(TAG, "setting comproute id=" + id);

		for (Route rt : myroutes) {
			rt.set();
		}
	}

}
