package de.blankedv.lanbahnpanel.elements

import java.util.ArrayList

import android.util.Log
import de.blankedv.lanbahnpanel.model.*

/** starting with rev. 2.5. the setting/clearing of routes is DONE ONLY in the
 * CENTRAL PC PROGRAM !
 *
 * Class Route stores a complete route, which contains sensors, signals and turnouts
 * it is tried to calculate offending routes automatically (defined as all routes which
 * set on of our turnouts. In addition offending routes can also be defined in the
 * config file (needed to crossing routes, which cannot be found automatically)
 *
 * @author mblank
 */

/**
 * constructs a route
 *
 * @param adr
 * unique identifier (int)
 * @param route
 * string for route setting like "770,1;720,2"
 * @param allSensors
 * string for sensors like "2000,2001,2002"
 * @param btn1
 * address of first route button
 * @param btn2
 * address of second route button
 * @param offending
 * string with offending routes, separated by comma
 */
class Route(var adr: Int, var btn1: Int, var btn2: Int, route: String, allSensors: String,
            offending: String) {

    private val blink = System.currentTimeMillis()
    private val toggleBlink = false

    var isActive = false
    private var timeSet: Long = 0

    var routeString = ""
    var sensorsString = ""
    var offendingString = "" // comma separated list of adr's of offending
// routes


    // sensors turnout activate for the display of this route
    private val rtSensors = ArrayList<SensorElement>()

    // signals of this route
    private val rtSignals = ArrayList<RouteSignal>()

    // turnouts of this route
    private val rtTurnouts = ArrayList<RouteTurnout>()

    // offending routes
    private val rtOffending = ArrayList<Route>()

    init {

        // these strings are written back to config file.
        this.routeString = route
        this.sensorsString = allSensors
        this.offendingString = offending

        if (DEBUG)
            Log.d(TAG, "creating route adr=$adr")

        // route = "750,1;751,2" => set 750 turnout 1 and 751 turnout value 2
        val routeElements = route.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in routeElements.indices) {
            val reInfo = routeElements[i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val addr = Integer.parseInt(reInfo[0])
            val allMatchingPEs = PanelElement.getAllPesByAddress(addr)
            for (pe in allMatchingPEs) {
                // if this is a signal, then add to my signal list "rtSignals"
                if (pe is SignalElement) {
                    if (reInfo.size == 3) {  // route signal with dependency
                        rtSignals.add(RouteSignal(pe,
                                Integer.parseInt(reInfo[1]),
                                Integer.parseInt(reInfo[2])))
                    } else {
                        rtSignals.add(RouteSignal(pe, Integer
                                .parseInt(reInfo[1])))
                    }

                } else if (pe is TurnoutElement) {
                    rtTurnouts.add(RouteTurnout(pe,
                            Integer.parseInt(reInfo[1])))
                }
            }
        }
        if (DEBUG)
            Log.d(TAG, rtSignals.size.toString() + " signals")
        if (DEBUG)
            Log.d(TAG, rtTurnouts.size.toString() + " turnouts")

        // format for sensors: just a list of addresses, separated by comma ","
        val sensorAddresses = allSensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in sensorAddresses.indices) {
            if (adr == 2201) Log.d(TAG,"i=$i")
            // add the matching elements turnout sensors list
            for (pe in panelElements) {
                if (pe is SensorElement) {
                    if (pe.adr == Integer.parseInt(sensorAddresses[i])) {
                        rtSensors.add(pe)
                    }
                }
            }
        }
        if (DEBUG)
            Log.d(TAG, rtSensors.size.toString() + " sensors")

        val offRoutes = offendingString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in offRoutes.indices) {
            for (rt in routes) {
                try {
                    val offID = Integer.parseInt(offRoutes[i])
                    if (rt.adr == offID && rt.isActive) {
                        rtOffending.add(rt)
                    }
                } catch (e: NumberFormatException) {
                }

            }
        }
        //	if (DEBUG)
        //		Log.d(TAG, rtOffending.size() + " offending routes in config");
    }//


    fun request() {
        if (DEBUG)
            Log.d(TAG, "requesting route adr=$adr")
        // request to set this route in central
        var cmd = "REQ $adr 1"    // for other tablets
        sendQ.add(cmd)
    }

    fun clearRequest() {
        if (DEBUG)
            Log.d(TAG, "requesting CLEAR route adr=$adr")
        // request to set this route in central
        var cmd = "REQ $adr 0"    // for other tablets
        sendQ.add(cmd)
    }

    class RouteSignal {
        internal var signal: SignalElement
        private var valueToSetForRoute: Int = 0
        var depFrom: Int = 0

        internal constructor(se: SignalElement, value: Int) {
            signal = se
            valueToSetForRoute = value
            depFrom = INVALID_INT
        }

        internal constructor(se: SignalElement, value: Int, dependentFrom: Int) {
            signal = se
            valueToSetForRoute = value
            depFrom = dependentFrom
        }

        internal fun dynamicValueToSetForRoute(): Int {
            // set standard value if not green
            if (depFrom == INVALID_INT || valueToSetForRoute != STATE_GREEN) {
                return valueToSetForRoute
            } else {
                // if standard-value == GREEN then check the other signal, which
                // this signal state depends on
                val depPe = PanelElement.getFirstPeByAddress(depFrom)
                return if (depPe!!.state == STATE_RED) {
                    // if other signal red, then set to yellow
                    STATE_YELLOW
                } else {
                    valueToSetForRoute
                }

            }
        }
    }

    class RouteTurnout internal constructor(internal var turnout: TurnoutElement, internal var valueToSetForRoute: Int)

    companion object {


        /**
         * check if we need to update the route state "isActive = true" when
         * data==1 and " = false" when data==0
         *
         */
        fun update ( addr : Int, data : Int) {
            for (rt in routes) {
                if (rt.adr == addr) {
                    rt.isActive = ( data != 0 )
                    if (DEBUG)
                        Log.d(TAG, "route adr=${rt.adr} isActive=${rt.isActive}")
                }
            }
        }
    }

}

