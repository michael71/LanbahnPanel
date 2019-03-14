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
(internal var adr: Int // must be unique
 , internal var btn1: Int, internal var btn2: Int, sRoutes: String) {
    internal var routesString = "" // identical to config string

    // route is comprised of a list of routes
    private val myroutes = ArrayList<Route>()
    var isActive = false


    init {

        // this string written back to config file.
        this.routesString = sRoutes

        if (DEBUG)
            Log.d(TAG, "creating comproute adr=$adr")

        // routes = "12,13": these routes need to be activated.
        val iID = routesString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in iID.indices) {
            val routeID = Integer.parseInt(iID[i])
            for (rt in routes) {
                try {
                    if (rt.adr == routeID) {
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
            Log.d(TAG, "requesting comproute adr=$adr")
        // request to set this route in central
        var cmd = "REQ $adr 1"    // for other tablets
        sendQ.add(cmd)
    }

    fun clearRequest() {
        if (DEBUG)
            Log.d(TAG, "requesting CLEAR comproute adr=$adr")
        // request to set this route in central
        var cmd = "REQ $adr 0"    // for other tablets
        sendQ.add(cmd)
    }


    companion object {

        /**
         * check if we need to update the comp route state "isActive = true" when
         * data = 1, and the depending "simple routes"
         *
         */
        fun update ( addr : Int, data : Int) {
            for (crt in compRoutes) {
                if (crt.adr == addr) {
                    crt.isActive = (data != 0)
                    for (rt in crt.myroutes) {
                        rt.isActive = (data != 0)
                    }
                }
            }
        }
    }

}
