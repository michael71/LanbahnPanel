package de.blankedv.lanbahnpanel.elements

import java.util.ArrayList

import android.util.Log
import de.blankedv.lanbahnpanel.model.*

/** Class Route stores a complete route, which contains sensors, signals and turnouts
 * it is tried to calculate offending routes automatically (defined as all routes which
 * set on of our turnouts. In addition offending routes can also be defined in the
 * config file (needed to crossing routes, which cannot be found automatically)
 *
 * @author mblank
 */

/**
 * constructs a route
 *
 * @param id
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
class Route(var id: Int, var btn1: Int, var btn2: Int, route: String, allSensors: String,
            offending: String) {

    private val blink = System.currentTimeMillis()
    private val toggleBlink = false

    var isActive = false
    private var timeSet: Long = 0

    var routeString = ""
    var sensorsString = ""
    var offendingString = "" // comma separated list of id's of offending
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
            Log.d(TAG, "creating route id=$id")

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

        // format for sensors: just a list of addresses, seperated by comma ","
        val sensorAddresses = allSensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in sensorAddresses.indices) {
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
                    if (rt.id == offID && rt.isActive) {
                        rtOffending.add(rt)
                    }
                } catch (e: NumberFormatException) {
                }

            }
        }
        //	if (DEBUG)
        //		Log.d(TAG, rtOffending.size() + " offending routes in config");
    }//

    fun deactivateSensorsAndTurnouts() {
        // deactivate sensors => set "inRoute = false"
        for (se in rtSensors) {
            se.inRoute = false
            //  if a route is de-activated, all connected tablets need to set the sensors to inroute = false
            // this is handled via activating and de-activating routes globally
        }

        for (rrt in rtTurnouts) {
            rrt.turnout.inRoute = false
            //  if a route is de-activated, all connected tablets need to set the turnouts to inroute = false
            // this is handled via activating and de-activating routes globally
        }

    }

    /** clear a route, set sensors to free and signals to RED
     *
     */
    fun clear() {
        timeSet = System.currentTimeMillis() // store for resetting
        // automatically
        if (DEBUG)
            Log.d(TAG, "clearing route id=$id")

        deactivateSensorsAndTurnouts()

        // set signals turnout red
        for (rs in rtSignals) {
            if (rs.signal.state != STATE_RED) {
                rs.signal.state = STATE_RED

                val cmd = "SET " + rs.signal.adr + " " + STATE_RED
                if (!sendQ.contains(cmd)) {
                    sendQ.add(cmd)
                }
            }
        }


        // TODO unlock turnouts
        /*
     * for (RouteTurnout to : rtTurnouts) {
     *     String cmd = "U " + to.turnout.adr;
     *     sendQ.add(cmd);
     * }
     */

        isActive = false
        // notify that route was cleared
        // route id's are unique, the standard number space is used
        val cmd = "SET $id 0"    // this is used by other tablets to set sensors to "not in Route"
        sendQ.add(cmd)
    }

    fun clearOffendingRoutes() {

        if (DEBUG)
            Log.d(TAG, "clearing (active) offending Routes")
        val offRoutes = offendingString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in offRoutes.indices) {
            for (rt in routes) {
                try {
                    val offID = Integer.parseInt(offRoutes[i])
                    if (rt.id == offID && rt.isActive) {
                        rt.clear()
                    }
                } catch (e: NumberFormatException) {
                }

            }
        }
    }

    fun activateSensorsAndTurnouts() {
        // activate sensors => set "inRoute = true"
        for (se in rtSensors) {
            se.inRoute = true
        }
        // set and // TODO lock turnouts
        for (rtt in rtTurnouts) {
            rtt.turnout.inRoute = true
        }
    }

    fun set() {
        timeSet = System.currentTimeMillis() // store for resetting
        // automatically

        if (DEBUG)
            Log.d(TAG, "setting route id=$id")

        // notify that route is set
        // route id's are unique, the standard number space is used
        var cmd = "SET $id 1"    // for other tablets
        sendQ.add(cmd)
        isActive = true

        clearOffendingRoutes()

        activateSensorsAndTurnouts()

        // set signals
        for (rs in rtSignals) {
            rs.signal.state = rs.dynamicValueToSetForRoute()
            cmd = "SET " + rs.signal.adr + " " + rs.dynamicValueToSetForRoute()
            if (DEBUG)
                Log.d(TAG, "setting route signal $cmd")
            sendQ.add(cmd)

        }
        // set and // TODO lock turnouts
        for (rtt in rtTurnouts) {
            rtt.turnout.state = rtt.valueToSetForRoute
            cmd = "SET " + rtt.turnout.adr + " " + rtt.valueToSetForRoute
            sendQ.add(cmd)
        }

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

    fun updateDependencies() {
        // update signals which have a dependency from another signal
        // set signals
        for (rs in rtSignals) {
            if (rs.depFrom != INVALID_INT) {
                if (rs.signal.state != rs.dynamicValueToSetForRoute()) {
                    rs.signal.state = rs.dynamicValueToSetForRoute()
                    val cmd = "SET " + rs.signal.adr + " " + rs.signal.state
                    if (DEBUG)
                        Log.d(TAG, "setting route signal dep.(" + rs.depFrom + ") " + cmd)
                    sendQ.add(cmd)
                }
            }
        }

    }

    class RouteTurnout internal constructor(internal var turnout: TurnoutElement, internal var valueToSetForRoute: Int)

    /**
     * this route was activated or deactivated by a different device we need the
     * status of this route, but we are not actively managing it.
     *
     * @param data
     */
    fun updateData(data: Int) {
        if (data == 0) {
            isActive = false
            deactivateSensorsAndTurnouts()
            timeSet = System.currentTimeMillis()
        } else if (data == 1) {
            isActive = true
            activateSensorsAndTurnouts()
            timeSet = System.currentTimeMillis()
        }
    }

    fun addOffending(rt2: Route) {
        // check if not already contained in offending string
        if (!rtOffending.contains(rt2))
            rtOffending.add(rt2)
    }


    fun getOffendString(): String {


        val sb = StringBuilder("")
        for (r in rtOffending) {
            if (sb.length == 0) {
                sb.append(r.id)
            } else {
                sb.append(",")
                sb.append(r.id)
            }
        }
        /*		if (sb.length() == 0)
            Log.d(TAG, "route id=" + id + " has no offending routes.");
        else
            Log.d(TAG, "route id=" + id + " has offending routes with ids="
                    + sb.toString()); */
        return sb.toString()

    }

    companion object {

        fun auto() {
            // check for auto reset of routes
            for (rt in routes) {
                if (System.currentTimeMillis() - rt.timeSet > 30 * 1000L && rt.isActive) {
                    rt.clear()
                }
                // update dependencies
                if (rt.isActive) rt.updateDependencies()
            }
        }

        fun calcOffendingRoutes() {

            for (rt in routes) {
                for (t in rt.rtTurnouts) {
                    // iterate over all turnouts of rt and check, if another route
                    // activates the same turnout to a different position
                    for (rt2 in routes) {
                        if (rt.id != rt2.id) {
                            for (t2 in rt2.rtTurnouts) {
                                if (t.turnout.adr == t2.turnout.adr && t.valueToSetForRoute != t2.valueToSetForRoute) {
                                    rt.addOffending(rt2)
                                    break
                                }

                            }
                        }
                    }
                }
                rt.offendingString = rt.getOffendString()
            }

        }

        fun clearAllRoutes() {
            routes.forEach { it.clear() }
        }

        fun update(chan: Int, data: Int) {
            for (rt in routes) {
                if (rt.id == chan) {
                    rt.updateData(data)
                }
            }
        }
    }
}

