package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;
import android.graphics.Canvas;
import android.util.Log;

public class SignalElement extends ActivePanelElement {

	// for signals which can be interactivly set from panel

	public SignalElement(String type, int x, int y, String name, int adr) {
		super(type, x, y, name,  adr);	
	} 

 	public SignalElement() {
 		adr = INVALID_INT;
		state = STATE_UNKNOWN;
 	}

	public SignalElement(PanelElement signal) {
		type = signal.type;
		x = signal.x;
		y = signal.y;
		adr = INVALID_INT;
		state = STATE_UNKNOWN;
	}
	
	public static SignalElement findSignalByAddress(int address) {
		for (PanelElement pe: panelElements) {
			if ( pe instanceof SignalElement )  {
				if (pe.getAdr() == address) {
					return (SignalElement)pe;
				}
			}
		}
		
		return null;
		
	}

	@Override
	public void doDraw(Canvas canvas) {

		// read data from SX bus and paint position of turnout accordingly
		// draw a line and not a bitmap
		canvas.drawLine(x*prescale,y*prescale, x2*prescale,y2*prescale, signalLine);
       	canvas.drawLine(x2*prescale,(y2-2.5f)*prescale, x2*prescale,(y2+2.5f)*prescale, signalLine);
       	canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, whitePaint);
		if ((enableEdit) || (adr == INVALID_INT)) {
			canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, whitePaint);
		} else {   
			if (state == STATE_RED) {
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, redSignal);
			} else if (state == STATE_GREEN){
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, greenSignal);
			} else if ((state == STATE_YELLOW) || (state == STATE_YELLOW_FEATHER)){
				canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, yellowSignal);
		    }
			else if (state == STATE_UNKNOWN){
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, whitePaint);
			}
	
		}

		if (drawAddresses) doDrawAddresses(canvas);
	}
	
	@Override
	public void toggle() {
		if (enableRoutes) return; // do not set signals by hand if routes are enabled
		
		if (adr == INVALID_INT) return; // do nothing if no sx address defined.
		
		if ((System.currentTimeMillis() - lastToggle) < 250) return;  // do not toggle twice within 250msecs
		
		lastToggle = System.currentTimeMillis();  // reset toggle timer

		
		// only for a SIMPLE SIGNAL RED / GREEN
		if (state == 0) {
			state = 1;
		} else {
			state = 0;
		}	
		
		// state = STATE_UNKNOWN; // until updated via lanbahn message
		sendQ.add("S "+adr+" "+state);  // ==> send changed data over network turnout interface
		if (DEBUG) Log.d(TAG,"toggle(adr="+adr+") new state="+state);
	}

	@Override
	public boolean isSelected(int xs, int ys) {
		// for signal check radius = RASTER/5 around signal center		
        int minx = x-RASTER/5;
        int maxx = x+RASTER/5;
        int miny = y-RASTER/5;
        int maxy = y+RASTER/5;
   
        // the touchpoint should be within rectangle of panel element
		if ( (xs >= minx) && (xs <= maxx) && (ys >= miny) && (ys <= maxy)) {
			if (DEBUG)
				Log.d(TAG, "selected adr=" + adr + " " + type + "  (" + x+","+ y+")");
			return true;
		} else {
			// if (DEBUG) Log.d(TAG, "No Signal selection");
			return false;
		}
	}
 }
