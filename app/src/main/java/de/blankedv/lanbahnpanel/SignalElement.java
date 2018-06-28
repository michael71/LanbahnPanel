package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;
import android.graphics.Canvas;
import android.util.Log;

public class SignalElement extends ActivePanelElement {

	// for signals which can be interactivly set from panel

	public SignalElement(int x, int y, String name, int adr) {
		super(x, y, name,  adr);
	} 

 	public SignalElement() {
 		adr = INVALID_INT;
		state = STATE_UNKNOWN;
 	}

	@Override
	public void doDraw(Canvas canvas) {

		// read data from SX bus and paint position of turnout accordingly
		// draw a line and not a bitmap
		canvas.drawLine(x*prescale,y*prescale, x2*prescale,y2*prescale, INSTANCE.getSignalLine());
       	canvas.drawLine(x2*prescale,(y2-2.5f)*prescale, x2*prescale,(y2+2.5f)*prescale, INSTANCE.getSignalLine());
       	canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, INSTANCE.getWhitePaint());
		if ((enableEdit) || (adr == INVALID_INT)) {
			canvas.drawCircle(x*prescale,y*prescale, 3f*prescale, INSTANCE.getWhitePaint());
		} else {   
			if (state == STATE_RED) {
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, INSTANCE.getRedSignal());
			} else if (state == STATE_GREEN){
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, INSTANCE.getGreenSignal());
			} else if ((state == STATE_YELLOW) || (state == STATE_YELLOW_FEATHER)){
				canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, INSTANCE.getYellowSignal());
		    }
			else if (state == STATE_UNKNOWN){
					canvas.drawCircle(x*prescale,y*prescale, 3.5f*prescale, INSTANCE.getWhitePaint());
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
		sendQ.add("SET "+adr+" "+state);  // ==> send changed data over network turnout interface
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
				Log.d(TAG, "selected adr=" + adr + " type=" + getType() + "  (" + x+","+ y+")");
			return true;
		} else {
			// if (DEBUG) Log.d(TAG, "No Signal selection");
			return false;
		}
	}
 }
