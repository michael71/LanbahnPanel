package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;


/**
 * control area - fixed geometry, above the panel
 */

public final class ControlArea {

	private static Paint editPaint, demoPaint, noWifiPaint, connPaint;

	private ControlButton stopBtn, commBtn,  clearRoutesBtn; //powerBtn, lampBtn, adrBtn, incrSpeedBtn, decrSpeedBtn, commBtn, powerBtn, functionBtn;

    
    private static float ySpeed = 50f, xSpeed=0;   // needed for position of texts in control area
	
	private static String errorMsg = "";
	private static long errorTime;
	private static long seconds = 0;
	private static long lastSecond = 0;


	public ControlArea() {
        Paint green, white;

        editPaint = new Paint();
		editPaint.setColor(Color.RED);
		editPaint.setTextSize(30);
		editPaint.setTypeface(Typeface.DEFAULT_BOLD);

        noWifiPaint = new Paint();
        noWifiPaint.setColor(Color.RED);
        noWifiPaint.setTextSize(60);
        noWifiPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		demoPaint  = new Paint();
		demoPaint.setColor(Color.CYAN);
		demoPaint.setTextSize(30);	
		demoPaint.setTypeface(Typeface.DEFAULT_BOLD);

		connPaint  = new Paint();
		connPaint.setColor(Color.LTGRAY);
		connPaint.setTextSize(14);
		//connPaint.setTypeface(Typeface.DEFAULT_BOLD);

		green = new Paint();
		green.setColor(Color.GREEN);
		white = new Paint();
		white.setColor(Color.WHITE);


		commBtn = new ControlButton(0.09f,0.5f,bitmaps.get("commok"),bitmaps.get("nocomm"));
		//powerBtn = new ControlButton(0.13f,0.5f, bitmaps.get("greendot"),bitmaps.get("reddot"));
		clearRoutesBtn = new ControlButton(0.25f,0.5f, bitmaps.get("clearrouteson"),bitmaps.get("clearroutesoff"));
	
	    //	lonstokeWestBtn = new ControlButton(0.50f,0.6f, bitmaps.get("lonstokewest"));
	}

	public static void dispErrorMsg(String e) {
		errorMsg = e;
		errorTime = System.currentTimeMillis();
	}
	
	public void draw(Canvas canvas) {
		comm1 = LanbahnPanelApplication.connectionIsAlive();
		commBtn.doDraw(canvas,LanbahnPanelApplication.connectionIsAlive());
		//powerBtn.doDraw(canvas,LanbahnPanelApplication.isPowerOn());
		if (enableRoutes) {
		   clearRoutesBtn.doDrawBlink(canvas,clearRouteButtonActive); 
		}
		
		for (LampGroup l : lampButtons)
		   l.btn.doDraw(canvas,l.isOn);

		
		canvas.drawBitmap(bitmaps.get("lonstokewest"),(int)(canvas.getWidth()*0.72f), ySpeed*0.22f,  null);
		
		if (enableEdit) canvas.drawText("Edit", (int)(canvas.getWidth()*0.36f), ySpeed*1f, editPaint);
		if (demoFlag) canvas.drawText("Demo", (int)(canvas.getWidth()*0.28f), ySpeed*1f, demoPaint);
        if (noWifiFlag) {
			canvas.drawText("No Wifi !", (int)(canvas.getWidth()*0.54f), ySpeed*1.3f, noWifiPaint);
		} else {
			canvas.drawText(conn_state_string, (int)(canvas.getWidth()*0.16f), ySpeed*1.3f, connPaint);
		}

        if ( (errorMsg.length() > 0) && (System.currentTimeMillis() - errorTime) < 3000) {
			canvas.drawText(errorMsg, (int)(canvas.getWidth()*0.38f), ySpeed*1f, editPaint);
		} else {
			// clear
			errorMsg = "";
		}
		

		if (System.currentTimeMillis() - lastSecond  >= 1000 )  {
			// increment seconds timer
			seconds++;
			lastSecond = System.currentTimeMillis();
			Route.auto();
		}
		
	}

	public void checkTouch(float x, float y) {
		if ( (enableRoutes) && (clearRoutesBtn.isTouched(x, y))) {
			if (!clearRouteButtonActive) {
				clearRouteButtonActive = true;
			} else {
				// actual clear route is done in "RouteButtonElement" class
				clearRouteButtonActive = false;
			}
		} 
		else {
			for (LampGroup l : lampButtons) {
				if (l.btn.isTouched(x,y))	l.toggle();
			}
		}
			

	} 

	public void recalcGeometry() {

		commBtn.recalcXY();
		clearRoutesBtn.recalcXY();
		
		for (LampGroup l : lampButtons) l.btn.recalcXY();

	}

	
}
