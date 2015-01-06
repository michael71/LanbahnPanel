package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;
import android.graphics.Canvas;
import android.util.Log;

/**  button for selecting routes
 * 
 * this buttons are local to the device and the state is NOT sent
 * via LANBAHN messages
 * 
 */
public class RouteButtonElement extends ActivePanelElement {

	// display a route button 
	
	long blink = System.currentTimeMillis();
	boolean toggleBlink = false;
	private long timeSet;
	
	public RouteButtonElement(String type, int x, int y, String name, int adr) {
		super(type, x, y, name,  adr);	
	} 

 	public RouteButtonElement() {
 		adr = INVALID_INT;
		state = STATE_UNKNOWN;
 	}

	
	public static RouteButtonElement findRouteButtonByAddress(int address) {
		for (PanelElement pe: panelElements) {
			if ( pe instanceof RouteButtonElement )  {
				if (pe.getAdr() == address) {
					return (RouteButtonElement)pe;
				}
			}
		}
		
		return null;
		
	}
	
	public static boolean checkForRoute(int adrSecondBtn) {

		
		if (DEBUG) Log.d(TAG,"checkForRoute called, adrSecondBtn="+adrSecondBtn);
		// check if a route needs to be cleared first
		if (clearRouteButtonActive) {
			if (DEBUG) Log.d(TAG,"clearRouteButtonActive:true");
			// find route with adrSecondBtn and clear it
			for (Route rt: routes) {
				if (DEBUG) Log.d(TAG,"checking route id="+rt.id);
				if ( rt.isActive() 
					 && 
					 ( (rt.btn1 == adrSecondBtn) || (rt.btn2 == adrSecondBtn) ) 
				   ) {
					if (DEBUG) Log.d(TAG,"found route matching to btn. clearing route="+rt.id);
					// we found a route with this button, new clear it
					// now set 
					rt.clear();
				} else {
					if (DEBUG) Log.d(TAG,"route not active route for btn="+adrSecondBtn);
				}
			}
			clearRouteButtonActive = false;
			findRouteButtonByAddress(adrSecondBtn).reset();  // clear the button also
			return true;
		}
		
		int nPressed = 0;
		int adrFirstBtn = 0;
		
		if (DEBUG) Log.d(TAG,"checking, if a route can be activated");
		// now check if a route can be activated
		for (PanelElement pe: panelElements) {
			if ( pe instanceof RouteButtonElement )  {
				if (((RouteButtonElement)pe).isPressed()) {
					nPressed++;
					if (pe.getAdr() != adrSecondBtn) {
						// if this is not the "checking" button, then it must be the first button
						adrFirstBtn=pe.getAdr();
					}
				}
			}
		}
		if (DEBUG) Log.d(TAG,"btns pressed total="+nPressed);
		if (nPressed == 2) {
			// this could be a route, 2 buttons are active
			// iterate over all possible routes 
			// we must know which button was pressed first!!
			if (DEBUG) Log.d(TAG,"checking for a route from btn-"+adrFirstBtn+" turnout btn-"+adrSecondBtn);
			boolean routeFound = false;
			for (Route rt: routes) {
				if (DEBUG) Log.d(TAG,"checking route id="+rt.id);
				if ( (rt.btn1 == adrFirstBtn) && (rt.btn2 == adrSecondBtn) ) {
					// we found a route connecting these buttons,
					// now set 
					routeFound = true;
					if (DEBUG) Log.d(TAG,"found the route with id="+rt.id);
					// reset buttons
					findRouteButtonByAddress(adrFirstBtn).reset();
					findRouteButtonByAddress(adrSecondBtn).reset();
					
					// set the route (i.e. sensors and turnouts)
					rt.set(); 
					break;  // no need to search further
				} 
			}
			for (CompRoute cr: compRoutes) {
				if (DEBUG) Log.d(TAG,"checking composite route id="+cr.id);
				if ( (cr.btn1 == adrFirstBtn) && (cr.btn2 == adrSecondBtn) ) {
					// we found a route connecting these buttons,
					// now set 
					routeFound = true;
					if (DEBUG) Log.d(TAG,"found the composite route with id="+cr.id);
					// reset buttons
					findRouteButtonByAddress(adrFirstBtn).reset();
					findRouteButtonByAddress(adrSecondBtn).reset();
					
					// set the route (i.e. sensors and turnouts)
					cr.set(); 
					break;  // no need to search further
				} 
			}
			if ( !routeFound ) {
				ControlArea.dispErrorMsg("keine passende Fahrstrasse.");
				findRouteButtonByAddress(adrFirstBtn).reset();  // clear the button also
				findRouteButtonByAddress(adrSecondBtn).reset();  // clear the button also
			}
			
		} else if (nPressed > 2) {
			if (DEBUG) Log.d(TAG,"too many routeButtons pressed, clearing all");
			// makes no sense, deselect all
			for (PanelElement pe: panelElements) {
				if ( pe instanceof RouteButtonElement )  {
					((RouteButtonElement)pe).reset();
				}
			}
			ControlArea.dispErrorMsg("zu viele Buttons.");
		}
		

		return true;
	}

	@Override
	public void doDraw(Canvas canvas) {

		// read data from SX bus and paint position of turnout accordingly
		// draw a line and not a bitmap
		//canvas.drawLine(x*prescale,y*prescale, x2*prescale,y2*prescale, signalLine);
       	//canvas.drawLine(x2*prescale,(y2-2.5f)*prescale, x2*prescale,(y2+2.5f)*prescale, signalLine);
		if (!enableRoutes) return;
		
       	canvas.drawCircle(x*prescale,y*prescale, 4f*prescale, whitePaint);
       	
		if ((enableEdit) || (adr == INVALID_INT)) {
			canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, btn0Paint);
		} else {   
			if (state == STATE_PRESSED) {
				if ((System.currentTimeMillis() - blink) > 500 ) {
					toggleBlink = !toggleBlink;
					blink =System.currentTimeMillis();
				} 
				if (toggleBlink) {
					canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, btn1Paint);
				} else {
					canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, btn0Paint);
				}
			} else if (state == STATE_NOT_PRESSED){
					canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, btn0Paint);
			} else if (state == STATE_UNKNOWN){
					canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, btn0Paint);
			}
	
		}

		if (drawAddresses2) doDrawAddresses(canvas);
	}
	
	@Override 
	public void toggle() {
		if (!enableRoutes) return; // do not enable route keys if not routes are enabled
		
		if (adr == INVALID_INT) return; // do nothing if no address defined.
		
		if ((System.currentTimeMillis() - lastToggle) < 500) {
			Log.d(TAG,"last toggle less than 500ms ago");
			return;  // do not toggle twice within 250msecs
		}
		
		lastToggle = System.currentTimeMillis();  // reset toggle timer

		if ( (state == STATE_NOT_PRESSED) |  (state == STATE_UNKNOWN) ) {
			state = STATE_PRESSED;
			timeSet = System.currentTimeMillis();
			checkForRoute(adr);
			
		} else {
			state = STATE_NOT_PRESSED;
		}	
		
		// state = STATE_UNKNOWN; // until updated via lanbahn message
		// sendQ.add("SET "+adr+" "+state);  // ==> send changed data over network
		if (DEBUG) Log.d(TAG,"toggle(adr="+adr+") new state="+state+" time="+lastToggle);
	}

	/**
	 * checks if button is being selected with a touch at point (xs, ys)
	 */
	@Override
	public boolean isSelected(int xs, int ys) {
		// for route button check radius = RASTER/3 around center	!! slightly larger than for turnout/signal	
        int minx = x-RASTER/3;
        int maxx = x+RASTER/3;
        int miny = y-RASTER/3;
        int maxy = y+RASTER/3;
   
        // the touchpoint should be within rectangle of panel element
		if ( (xs >= minx) && (xs <= maxx) && (ys >= miny) && (ys <= maxy)) {
			if (DEBUG)
				Log.d(TAG, "selected adr=" + adr + " " + type + "  (" + x+","+ y+")");
			return true;
		} else {
			// if (DEBUG) Log.d(TAG, "No Route key selection");
			return false;
		}
	}
	
	/** 
	 * 
	 * @return true if the button is currently pressed, else false
	 */ 
	public boolean isPressed() {
		if (state == STATE_PRESSED) {
			return true;
		} else {
			return false;
		}
	}
	
	public void reset() {
		state = STATE_NOT_PRESSED;
	}
	
	public static void autoReset() {
		for (PanelElement pe: panelElements) {
			if ( pe instanceof RouteButtonElement )  {
				if ( ( ((RouteButtonElement)pe).state == STATE_PRESSED ) && 
				     ( ( System.currentTimeMillis() -  ((RouteButtonElement)pe).timeSet) > 20 *1000L ) 
				   ) {		
		  			((RouteButtonElement)pe).state = STATE_NOT_PRESSED;
				}
			}
		}
		
	}
 }
