package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;


public final class LocoControlArea {

	private static Paint editPaint, demoPaint;
/*	private static Paint paintLocoAdrTxt, paintLocoSpeedTxt, paintLocoSpeed;
	private static int sliderXoff, sliderYoff;

	private static final float X_LOCO_MID=0.5f;
	private static final float X_LOCO_RANGE=0.25f;

	
	private static final float ZERO_LINE_HALFLENGTH = 15f;  
	private static float canvasWidth = 100f;
	private static long lastSpeedCheckMove=0L;

	private static int sxmin=0, sxmax=0;   */

	private LocoButton stopBtn, commBtn,  clearRoutesBtn; //powerBtn, lampBtn, adrBtn, incrSpeedBtn, decrSpeedBtn, commBtn, powerBtn, functionBtn;

    
    private static float ySpeed = 50f, xSpeed=0;   // needed for position of texts in control area
	
	private static String errorMsg = "";
	private static long errorTime;
	private static long seconds = 0;
	private static long lastSecond = 0;

	//private static float lastXt=X_LOCO_MID;

	//private int selectedLoco=0;

	public LocoControlArea() {
        Paint green, white,

        editPaint  = new Paint();
		editPaint.setColor(Color.RED);
		editPaint.setTextSize(30);
		editPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		demoPaint  = new Paint();
		demoPaint.setColor(Color.CYAN);
		demoPaint.setTextSize(30);	
		demoPaint.setTypeface(Typeface.DEFAULT_BOLD);

		/* paintLocoSpeedTxt = new Paint();
		paintLocoSpeedTxt.setColor(Color.WHITE);
		paintLocoSpeedTxt.setTextSize(20); */

		green = new Paint();
		green.setColor(Color.GREEN);
		white = new Paint();
		white.setColor(Color.WHITE);

		/*stopBtn = new LocoButton(0.20f,0.5f,
				bitmaps.get("stop_s_on"),
				bitmaps.get("stop_s_off"));
		lampBtn = new LocoButton(0.848f,0.5f,
				bitmaps.get("lamp1"),
				bitmaps.get("lamp0"));
		functionBtn = new LocoButton(0.80f,0.5f,
			 	bitmaps.get("func1"),
			 	bitmaps.get("func0"));  
     	adrBtn = new LocoButton(0.91f,0.5f);  // only text.
		incrSpeedBtn = new LocoButton(0.97f,0.5f,bitmaps.get("incr"));
		decrSpeedBtn = new LocoButton(0.03f,0.5f,bitmaps.get("decr"));  */ 
	
		commBtn = new LocoButton(0.09f,0.5f,bitmaps.get("commok"),bitmaps.get("nocomm"));
		//powerBtn = new LocoButton(0.13f,0.5f, bitmaps.get("greendot"),bitmaps.get("reddot"));
		clearRoutesBtn = new LocoButton(0.25f,0.5f, bitmaps.get("clearrouteson"),bitmaps.get("clearroutesoff"));
	
		
	    //	lonstokeWestBtn = new LocoButton(0.50f,0.6f, bitmaps.get("lonstokewest"));
	}

	public static void dispErrorMsg(String e) {
		errorMsg = e;
		errorTime = System.currentTimeMillis();
	}
	
	public void draw(Canvas canvas) {
			 
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
		// draw "buttons" and states
		/* no loco
		adrBtn.doDraw(canvas,getLocoAdr(), paintLocoAdrTxt);
		lampBtn.doDraw(canvas,((LanbahnPanelApplication.getLocoSXData() & 0x40) != 0));
	 	functionBtn.doDraw(canvas,((LanbahnPanelApplication.getLocoSXData() & 0x80) != 0));
		stopBtn.doDraw(canvas,((LanbahnPanelApplication.getLocoSXData() & 0x1f) != 0));
		incrSpeedBtn.doDraw(canvas);
		decrSpeedBtn.doDraw(canvas);  
		*/
		
		
        /* no loco control	
		 draw slider for speed selection
		sxmin = (int)((canvas.getWidth()*(X_LOCO_MID-X_LOCO_RANGE)));
		sxmax = (int)((canvas.getWidth()*(X_LOCO_MID+X_LOCO_RANGE)));

		Rect speedLine = new Rect(sxmin,(int)(ySpeed-1),sxmax,(int)ySpeed+1);       
		canvas.drawRect(speedLine, white);

		Rect zeroLine = new Rect((sxmin+sxmax)/2-1,(int)(ySpeed-ZERO_LINE_HALFLENGTH),
				(sxmin+sxmax)/2+1,(int)(ySpeed+ZERO_LINE_HALFLENGTH));   
		canvas.drawRect(zeroLine, white);

		canvasWidth = canvas.getWidth();	

		int xtext =  (int)(canvasWidth*(X_LOCO_MID+X_LOCO_RANGE*0.9f));
		canvas.drawText(locoSpeed(),xtext, ySpeed+20, paintLocoSpeedTxt);


		xSpeed = canvasWidth*sxpeed();
		canvas.drawBitmap(bitmaps.get("slider"), xSpeed-sliderXoff, ySpeed-sliderYoff,  null);
		if ( (System.currentTimeMillis() - lastSpeedCheckMove) < 1000) {
			// draw additional "soll" slider
			canvas.drawBitmap(bitmaps.get("slider_grey"), xSpeed-sliderXoff, ySpeed-sliderYoff,  null);
			canvas.drawBitmap(bitmaps.get("slider"), lastXt-sliderXoff, ySpeed-sliderYoff,  null);
		} else {
			canvas.drawBitmap(bitmaps.get("slider"), xSpeed-sliderXoff, ySpeed-sliderYoff,  null);
		}
		
	

	}

	/* disabled loco control
	private float sxpeed() {
		int s = (LanbahnPanelApplication.getLocoSXData() & 0x1f);
		if ((LanbahnPanelApplication.getLocoSXData() & 0x20) != 0) s = -s;
		float speed = (X_LOCO_RANGE*(float)s / 31.0f)+X_LOCO_MID;  // sx=31 ==> ~0.25f (+0.6f offset)
		return speed;
	}

	private  String locoSpeed() {		
		int s = (LanbahnPanelApplication.getLocoSXData() & 0x1f);
		if ((LanbahnPanelApplication.getLocoSXData() & 0x20) != 0) s = -s;
		return ""+s;
	}

	private Boolean forward() {		
		return ( (LanbahnPanelApplication.getLocoSXData() & 0x20) != 0);

	}

	private int speedToSX(int s) {
		int data=0;
		if (s < 0) { data |= 0x20; s= -s;}
		if (s > 31) s=31;
		data |= s;
		return data;
	}

	public void checkSpeedMove(float xt, float yt) {
		// check slider
		//if (DEBUG) Log.d(TAG,"check slider touch xt="+xt+"  yt="+yt+" xSpeed="+xSpeed+" sliderXoff="+sliderXoff+" ySpeed="+ySpeed+" sliderYoff="+sliderYoff);

		if (   (xt > (X_LOCO_MID-X_LOCO_RANGE)*canvasWidth) 
				&& (xt < (X_LOCO_MID+X_LOCO_RANGE)*canvasWidth) 
				&& (yt > (ySpeed-sliderYoff)) 
				&& (yt < (ySpeed+sliderYoff))) {
			lastSpeedCheckMove = System.currentTimeMillis();
			lastXt = xt;
			int s = (int)(((31.4f/X_LOCO_RANGE)*(xt - X_LOCO_MID*canvasWidth))/canvasWidth);
			int sxData = speedToSX(s);
			LanbahnPanelApplication.setLocoSpeed(sxData);  // will be sent only when different turnout currently known speed.
			//LanbahnPanelApplication.sendSpeed(0,true);
		}
	}
	*/
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
			
		/*else if (decrSpeedBtn.isTouched(x,y)) {
			LanbahnPanelApplication.decrLocoSpeed();
		} else if (functionBtn.isTouched(x,y)) {
			LanbahnPanelApplication.toggleFunc();
		} else if (adrBtn.isTouched(x,y)) {
			Dialogs.selectLocoAddressDialog();
			
		} */

	} 

	public void recalcGeometry() {
		//stopBtn.recalcXY();
		//lampBtn.recalcXY();
		//adrBtn.recalcXY();	
		//decrSpeedBtn.recalcXY();
		//incrSpeedBtn.recalcXY();
		//functionBtn.recalcXY();
		//powerBtn.recalcXY();
		commBtn.recalcXY();
		clearRoutesBtn.recalcXY();
		
		for (LampGroup l : lampButtons) l.btn.recalcXY();

		//ySpeed=(controlAreaRect.bottom - controlAreaRect.top)/2;  // mid of control area range.
	}

	
}
