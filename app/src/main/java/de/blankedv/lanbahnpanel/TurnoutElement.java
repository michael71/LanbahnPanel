package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;
import android.graphics.Canvas;
import android.util.Log;


public class TurnoutElement extends ActivePanelElement {

	// for turnouts which can be interactivly set from panel

	public TurnoutElement(String type, int x, int y, String name, int adr) {
		super(type, x, y, name,  adr);	
	} 

 	public TurnoutElement() {
 		adr = INVALID_INT;
		state = STATE_UNKNOWN;
 	}

	public TurnoutElement(PanelElement turnout) {
		type = turnout.type;
		x = turnout.x;
		y = turnout.y;
		x2= turnout.x2;
		y2= turnout.y2;
		xt= turnout.xt;
		yt= turnout.yt;
		adr = INVALID_INT;
		state = STATE_UNKNOWN;
	}

	@Override
	public void doDraw(Canvas canvas) {

		// read data from SX bus and paint position of turnout accordingly
		// draw a line and not a bitmap
		if (enableEdit) {
			canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,greenPaint);	
			canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,redPaint);	
		} else if (adr == INVALID_INT)  {
				canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,linePaint2);	
				canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,linePaint2);
		} else {

				if (state == STATE_CLOSED) {
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,linePaint2);
				} else if (state == STATE_THROWN){
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,linePaint2);
				} else if (state == STATE_UNKNOWN){
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,bgPaint);
				}
		}

		if (drawAddresses) doDrawAddresses(canvas);
	}
	
	@Override
	public void toggle() {
		if (enableRoutes) return; // do not set turnouts by hand if routes are enabled
		
		if (adr == INVALID_INT) return; // do nothing if no sx address defined.
		
		if ((System.currentTimeMillis() - lastToggle) < 250) return;  // do not toggle twice within 250msecs
		
		lastToggle = System.currentTimeMillis();  // reset toggle timer

		
		// only for a SIMPLE turnout
		if (state == 0) {
			state = 1;
		} else {
			state = 0;
		}	
		
		// state = STATE_UNKNOWN; // until updated via lanbahn message
		sendQ.add("SET "+adr+" "+state);  // ==> send changed data over network turnout interface
		if (DEBUG) Log.d(TAG,"toggle(adr="+adr+") new state="+state);
	}

	public static TurnoutElement findTurnoutByAddress(int address) {
		for (PanelElement pe: panelElements) {
			if ( pe instanceof SignalElement )  {
				if (pe.getAdr() == address) {
					return (TurnoutElement)pe;
				}
			}
		}
		return null;
	}

 }
