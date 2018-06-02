package de.blankedv.lanbahnpanel;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.Panel.controlAreaRect;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class ControlButton {
    private float xrel, yrel;  // relative position in control area.
    private Bitmap bmON, bmOFF;
    private int w=10,h=10; // half of the bitmap width and height
	private long blink;
	private boolean toggleBlink;
    // x and y are actual position of bitmap placing, NOT the center!
    public float x=0,y=0;
	public ControlButton(float x2, float y2, Bitmap on, Bitmap off) {
		this.xrel = x2;
		this.yrel = y2;
		bmON = on;
		bmOFF = off;
		w=bmON.getWidth()/2;
		h=bmON.getHeight()/2;
		if (controlAreaRect != null) {
		    recalcXY();
		}

	}


	public boolean isTouched(float xt, float yt) {
    	if ( (xt > x) && (xt < (x+w+w)) && (yt > y) && (yt < (y+h+h))) {
    		if (DEBUG) Log.d(TAG,this.toString()+" was touched.");
    		return true;
    	} else {
    		if (DEBUG) Log.d(TAG,this.toString() +" was not touched.");
    		return false;
    	}
    }
	
	public void recalcXY() {
		if (bmON == null) {
			w=(controlAreaRect.right-controlAreaRect.left)/30;
			h=(controlAreaRect.bottom-controlAreaRect.top)/3;
		}
		x=controlAreaRect.left + xrel*(controlAreaRect.right-controlAreaRect.left) - w;  // position where bitmap is drawn
		y=controlAreaRect.top + yrel*(controlAreaRect.bottom-controlAreaRect.top)- h;
		if (DEBUG) Log.d(TAG,this.toString()+"btn recalc, x="+x+" y="+y+" w="+w+" h="+h);
	}
	
	public void doDraw(Canvas c, boolean state) {
		
		if (state) {
			c.drawBitmap(bmON,x, y,  null);
			
		} else {
			c.drawBitmap(bmOFF,x, y,  null);
		}
	}
	
    public void doDrawBlink(Canvas c, boolean state) {
		
		if ((System.currentTimeMillis() - blink) > 300 ) {
			toggleBlink = !toggleBlink;
			blink = System.currentTimeMillis();
		} 

		if (state) {
			if (toggleBlink) {
				c.drawBitmap(bmOFF,x, y,  null);
			} else {
			   c.drawBitmap(bmON,x, y,  null);
			}
		} else {
			c.drawBitmap(bmOFF,x, y,  null);
		}
	}
	
	
	public void doDraw(Canvas c) {
	    c.drawBitmap(bmON,x, y,  null);
	}
	
	public void doDraw(Canvas c, int value, Paint p) {
        // (x,y) drawing position for text is DIFFERENT than for bitmaps.(upper left)
		// (x,y) = lower left start of text.
		c.drawBitmap(bmON,x, y,  null);
		//c.drawRect(x+3,y+3,x+w+w-3,y+h+h-3, bg);
// no loco control		c.drawText(""+getLocoAdr(),x+w*0.6f,y+h*1.42f, p);

		}
}
