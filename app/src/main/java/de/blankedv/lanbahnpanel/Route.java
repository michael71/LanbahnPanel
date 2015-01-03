package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.ActivePanelElement.*;

import java.util.ArrayList;

import android.util.Log;

/** Class Route stores a complete route, which contains sensors, signals and turnouts
 * it is tried to calculate offending routes automatically (defined as all routes which 
 * set on of our turnouts. In addition offending routes can also be defined in the 
 * config file (needed to crossing routes, which cannot be found automatically)
 * 
 * @author mblank
 * 
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class Route {

	public int id; // must be unique

	private long blink = System.currentTimeMillis();
	private boolean toggleBlink = false;

	boolean active = false;
	private long timeSet;

	String routeString = "";
	String sensorsString = "";
	String offendingString = ""; // comma separated list of id's of offending
									// routes


	// sensors turnout activate for the display of this route
	private ArrayList<SensorElement> rtSensors = new ArrayList<>();

	// signals of this route
	private ArrayList<RouteSignal> rtSignals = new ArrayList<>();

	// turnouts of this route
	private ArrayList<RouteTurnout> rtTurnouts = new ArrayList<>();

	// offending routes
	private ArrayList<Route> rtOffending = new ArrayList<>();

	int btn1, btn2;

	/**
	 * constructs a route
	 * 
	 * @param id
	 *            unique identifier (int)
	 * @param route
	 *            string for route setting like "770,1;720,2"
	 * @param allSensors
	 *            string for sensors like "2000,2001,2002"
	 * @param btn1
	 *            address of first route button
	 * @param btn2
	 *            address of second route button
	 * @param offending
	 *            string with offending routes, separated by comma
	 */
	public Route(int id, int btn1, int btn2, String route, String allSensors,
			String offending) {
		//
		this.btn1 = btn1;
		this.btn2 = btn2;
		this.id = id;

		// these strings are written back to config file.
		this.routeString = route;
		this.sensorsString = allSensors;
		this.offendingString = offending;

		if (DEBUG)
			Log.d(TAG, "creating route id=" + id);

		// route = "750,1;751,2" => set 750 turnout 1 and 751 turnout value 2
		String[] routeElements = route.split(";");
		for (int i = 0; i < routeElements.length; i++) {
			String reInfo[] = routeElements[i].split(",");

			PanelElement pe = getPeByAddress(Integer.parseInt(reInfo[0]));

			// if this is a signal, then add to my signal list "rtSignals"
			if (pe != null) {
				if (pe instanceof SignalElement) {
					if (reInfo.length == 3) {  // route signal with dependency
						rtSignals.add(new RouteSignal((SignalElement) pe, 
								Integer.parseInt(reInfo[1]),
								Integer.parseInt(reInfo[2])));
					} else {
					rtSignals.add(new RouteSignal((SignalElement) pe, Integer
							.parseInt(reInfo[1])));
					}

				} else if (pe instanceof TurnoutElement) {
					rtTurnouts.add(new RouteTurnout((TurnoutElement) pe,
							Integer.parseInt(reInfo[1])));
				}
			}
		}
		if (DEBUG)
			Log.d(TAG, rtSignals.size() + " signals");
		if (DEBUG)
			Log.d(TAG, rtTurnouts.size() + " turnouts");

		// format for sensors: just a list of addresses, seperated by comma ","
		String[] sensorAddresses = allSensors.split(",");
		for (int i = 0; i < sensorAddresses.length; i++) {
			// add the matching elements turnout sensors list
			for (PanelElement pe : panelElements) {
				if (pe instanceof SensorElement) {
					if (pe.getAdr() == Integer.parseInt(sensorAddresses[i])) {
						rtSensors.add((SensorElement) pe);
					}
				}
			}
		}
		if (DEBUG)
			Log.d(TAG, rtSensors.size() + " sensors");
		
		String[] offRoutes = offendingString.split(",");
		for (int i = 0; i < offRoutes.length; i++) {
			for (Route rt : routes) {
				try {
					int offID = Integer.parseInt(offRoutes[i]);
					if ((rt.id == offID) && (rt.active)) {
						rtOffending.add(rt);
					}
				} catch (NumberFormatException e) {
				}
			}
		}
	//	if (DEBUG)
	//		Log.d(TAG, rtOffending.size() + " offending routes in config");
	}

	public void clear() {
		timeSet = System.currentTimeMillis(); // store for resetting
												// automatically
		if (DEBUG)
			Log.d(TAG, "clearing route id=" + id);

		// deactivate sensors
		for (SensorElement se : rtSensors) {
			se.setState(STATE_FREE);
			String cmd = "SET " + se.adr + " " + STATE_FREE;
			sendQ.add(cmd);
		}

		// set signals turnout red
		for (RouteSignal rs : rtSignals) {
			rs.signal.setState(STATE_RED);
			String cmd = "SET " + rs.signal.adr + " " + STATE_RED;
			sendQ.add(cmd);
		}

		// TODO unlock turnouts
		/*
		 * for (RouteTurnout to : rtTurnouts) { 
		 *     String cmd = "U " + to.turnout.adr;
		 *     sendQ.add(cmd); 
		 * }
		 */

		active = false;
		// notify that route was cleared
		String cmd = "RT " + id + " 0";
		sendQ.add(cmd);
	}

	public void clearOffendingRoutes() {
		if (DEBUG)
			Log.d(TAG, "clearing (active) offending Routes");
		String[] offRoutes = offendingString.split(",");
		for (int i = 0; i < offRoutes.length; i++) {
			for (Route rt : routes) {
				try {
					int offID = Integer.parseInt(offRoutes[i]);
					if ((rt.id == offID) && (rt.active)) {
						rt.clear();
					}
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	public void set() {
		timeSet = System.currentTimeMillis(); // store for resetting
												// automatically

		if (DEBUG)
			Log.d(TAG, "setting route id=" + id);

		// notify that route is set
		String cmd = "RT " + id + " 1";
		sendQ.add(cmd);
		active = true;

		clearOffendingRoutes();

		// activate sensors
		for (SensorElement se : rtSensors) {
			se.setState(STATE_INROUTE);
			cmd = "SET " + se.adr + " " + STATE_INROUTE;
			sendQ.add(cmd);
		}

		// set signals
		for (RouteSignal rs : rtSignals) {
			cmd = "SET " + rs.signal.adr + " " + rs.dynamicValueToSetForRoute();
			if (DEBUG)
				Log.d(TAG, "setting route signal " + cmd);
			sendQ.add(cmd);

		}
		// set and // TODO lock turnouts
		for (RouteTurnout rtt : rtTurnouts) {
			cmd = "SET " + rtt.turnout.adr + " " + rtt.valueToSetForRoute;
			sendQ.add(cmd);
		}

	}

	public boolean isActive() {
		return active;
	}

	protected class RouteSignal {
		SignalElement signal;
		private int valueToSetForRoute;
		private int depFrom;

		RouteSignal(SignalElement se, int value) {
			signal = se;
			valueToSetForRoute = value;
			depFrom = INVALID_INT;
		}
		
		RouteSignal(SignalElement se, int value, int dependentFrom) {
			signal = se;
			valueToSetForRoute = value;
			depFrom = dependentFrom;
		}
		
		int dynamicValueToSetForRoute() {
			// set standard value if not green
			if ( (depFrom == INVALID_INT) || (valueToSetForRoute != STATE_GREEN)) {
				return valueToSetForRoute;
			} else {
				// if standard-value == GREEN then check the other signal, which
				// this signal state depends on
				PanelElement depPe = getPeByAddress(depFrom);
				if (depPe.getState() == STATE_RED) {
					// if other signal red, then set to yellow
					return STATE_YELLOW;
				} else {
					return valueToSetForRoute;
				}
				
			}
		}
	}

	protected void updateDependencies() {
		// update signals which have a dependency from another signal
		// set signals
		for (RouteSignal rs : rtSignals) {
			if (rs.depFrom != INVALID_INT) {
				if (rs.signal.getState() != rs.dynamicValueToSetForRoute()) {
					rs.signal.state = rs.dynamicValueToSetForRoute();
				    String cmd = "SET " + rs.signal.adr + " " + rs.signal.state;
					if (DEBUG)
						Log.d(TAG, "setting route signal dep.("+rs.depFrom+ ") "+ cmd);
					sendQ.add(cmd);
				}
			}
		}
		
	}
	protected class RouteTurnout {
		TurnoutElement turnout;
		int valueToSetForRoute;

		RouteTurnout(TurnoutElement te, int value) {
			turnout = te;
			valueToSetForRoute = value;
		}
	}

	public static void auto() {
		// check for auto reset of routes
		for (Route rt : routes) {
			if (((System.currentTimeMillis() - rt.timeSet) > 30 * 1000L)
					&& (rt.active)) {
				rt.clear();
			}
			// update dependencies
			if (rt.active) rt.updateDependencies();
		}
		
	}

	/**
	 * this route was activated or deactivated by a different device we need the
	 * status of this route, but we are not actively managing it.
	 * 
	 * @param data
	 */
	public void updateData(int data) {
		if (data == 0) {
			active = false;
			timeSet = System.currentTimeMillis();
		} else if (data == 1) {
			active = true;
			timeSet = System.currentTimeMillis();
		}
	}

	public void addOffending(Route rt2) {
		// check if not already contained in offending string
		if (!rtOffending.contains(rt2))
			rtOffending.add(rt2);
	}

	public String getOffendingString() {

		StringBuilder sb = new StringBuilder("");
		for (Route r : rtOffending) {
			if (sb.length() == 0) {
				sb.append(r.id);
			} else {
				sb.append(",");
				sb.append(r.id);
			}
		}
/*		if (sb.length() == 0)
			Log.d(TAG, "route id=" + id + " has no offending routes.");
		else
			Log.d(TAG, "route id=" + id + " has offending routes with ids="
					+ sb.toString()); */
		return sb.toString();

	}

	public static void calcOffendingRoutes() {
		for (Route rt : routes) {
			for (RouteTurnout t : rt.rtTurnouts) {
				// iterate over all turnouts of rt and check, if another route
				// activates the same turnout to a different position 
				for (Route rt2 : routes) {
					if (rt.id != rt2.id) {
						for (RouteTurnout t2 : rt2.rtTurnouts) {
							if ( (t.turnout.adr == t2.turnout.adr) && 
									(t.valueToSetForRoute != t2.valueToSetForRoute) ){
								rt.addOffending(rt2);
								break;
							}

						}
					}
				}
			}
			rt.offendingString = rt.getOffendingString();
			}

	}
}
