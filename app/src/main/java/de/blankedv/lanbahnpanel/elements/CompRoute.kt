package de.blankedv.lanbahnpanel.elements

import java.util.ArrayList
import android.util.Log
import de.blankedv.lanbahnpanel.model.*

/**
 * composite route, i.e. a list of routes which build a new route, is only a
 * helper for ease of use, no more functionality than the "simple" Route which
 * it is comprised of
 *
 * @author mblank
 */
class CompRoute
/**
 * constructs a composite route
 *
 *
 */
(internal var id: Int // must be unique
 , internal var btn1: Int, internal var btn2: Int, sRoutes: String) {
    internal var routesString = "" // identical to config string

    // route is comprised of a list of routes
    private val myroutes = ArrayList<Route>()

    init {

        // this string written back to config file.
        this.routesString = sRoutes

        if (DEBUG)
            Log.d(TAG, "creating comproute id=$id")

        // routes = "12,13": these routes need to be activated.
        val iID = routesString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in iID.indices) {
            val routeID = Integer.parseInt(iID[i])
            for (rt in routes) {
                try {
                    if (rt.id == routeID) {
                        myroutes.add(rt)
                    }
                } catch (e: NumberFormatException) {
                }

            }
        }
        if (DEBUG)
            Log.d(TAG, myroutes.size.toString() + " routes in this route.")

    }//

    /*	no clear for compound routes because the single routes are cleared automatically after X seconds
    public void clear() {

	} */

    fun request() {
        if (DEBUG)
            Log.d(TAG, "requesting comproute id=$id")
        // request to set this route in central
        var cmd = "REQ $id 1"    // for other tablets
        sendQ.add(cmd)
    }
    /** set all the single routes which depend on this compound route
     *
     */
    fun set() {
        if (prefs.getBoolean(KEY_ROUTING,true)) return; // done in CENTRAL
        if (DEBUG)
            Log.d(TAG, "setting comproute id=$id")

        for (rt in myroutes) {
            rt.set()
        }
    }

}
