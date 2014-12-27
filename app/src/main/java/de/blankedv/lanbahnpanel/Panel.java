package de.blankedv.lanbahnpanel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;

/**
 * the main panel of the application is comprised of two parts: a (small height) LOCO CONTROL area
 *  at the top and the larger part with the main SWITCH PANEL at the bottom, handles all touch events
 */
public class Panel extends SurfaceView implements SurfaceHolder.Callback {
	public static Rect controlAreaRect;
	
	private ViewThread mThread;



	private int mX, mY;
	
	private float mPosX, mPosY;

	private float mLastTouchX, mLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private static Paint paintControlAreaBG;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	private long scalingTime =0L;

	private int mWidth, mHeight;
	
	private static final long SCALING_WAIT = 1000L;
	
	LocoControlArea locoControlArea;

	//@TargetApi(8)
	public Panel(Context context) {
		super(context);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());         
        
		paintControlAreaBG  = new Paint();
		paintControlAreaBG.setColor(0xff224422);
        
        locoControlArea = new LocoControlArea();
         
		LanbahnPanelApplication.updatePanelData();
		getHolder().addCallback(this);

		mThread = new ViewThread(this);

	}


	//  start and stop the dedicated rendering thread 
	//  implementing SurfaceHolder Callback for your SurfaceView. 
	//  This should be enough turnout
	//	limit drawing only when the SurfaceView is visible.

	public void surfaceCreated(SurfaceHolder holder) {
		if (!mThread.isAlive()) {
			mThread = new ViewThread(this);
			mThread.setRunning(true);
			mThread.start();
		}
	}


	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mThread.isAlive()) {
			mThread.setRunning(false);
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(event);
        
		final int action = event.getAction();


		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = event.getX();
			final float y = event.getY();

			mLastTouchX = x;
			mLastTouchY = y;

			mX = (int) (x); // /scale);
			mY = (int) (y); ///scale);
			
			mPosX=0;
			mPosY=0;
			//if (DEBUG)  Log.d(TAG,"ACTION_DOWN - (scaled) mX="+mX+"  mY"+mY);
			//if (DEBUG)  Log.d(TAG,"ACTION_DOWN - (abs) x="+x+"  y"+y);
			mActivePointerId = event.getPointerId(0);
			break;
		}      
		case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = event.findPointerIndex(mActivePointerId);
            final float x = event.getX(pointerIndex);
            final float y = event.getY(pointerIndex);

            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!mScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;
                if ((zoomEnabled) && (mX>300) && (mY>200)) {
                   xoff += dx;
       		       yoff += dy;
       		       scalingTime = System.currentTimeMillis();  // avoid control of SX elements during pan-move
                }
               // invalidate();
              // if (DEBUG)  Log.d(TAG,"mPosX="+mPosX+" mPosY="+mPosY);
                // no loco control
                //locoControlArea.checkSpeedMove(x, y);
            }

            mLastTouchX = x;
            mLastTouchY = y;
            
            if (enableEdit) {
            	//selSxAddress.dismiss();
            }
  
            break;
        }

		case MotionEvent.ACTION_UP: {
			if (DEBUG)  Log.d(TAG,"ACTION_UP");
			mActivePointerId = INVALID_POINTER_ID;

			// do SX control only when NOT scaling (and wait 1 sec after scaling)
			long deltaT = System.currentTimeMillis()-scalingTime;
			if (!mScaleDetector.isInProgress() )  { //&& (deltaT > SCALING_WAIT)) {
				// assuming control area is always at the top !!
				if (mLastTouchY < controlAreaRect.bottom)  {
					Log.d(TAG,"ACTION_UP _Checking Loco Control  at: mlastTouchX="+mLastTouchX+"  mLastTouchY"+mLastTouchY);   	
					locoControlArea.checkTouch(mLastTouchX,mLastTouchY);
				} else {
					//Log.d(TAG,"ACTION_UP _Checking panel elements at: mlastTouchX="+mLastTouchX+"  mLastTouchY"+mLastTouchY);   
					int xs = Math.round(((mLastTouchX-xoff)/scale)/prescale); // reduced by overall dimension scaling factors
					int ys = Math.round(((mLastTouchY-yoff)/scale)/prescale);

					Log.d(TAG,"ACTION_UP _Checking panel elements at: xs="+xs+"  ys"+ys);   
					for (PanelElement e:panelElements) {
						boolean sel=false;
						if (enableRoutes) {
							// check only!! route buttons when routing is enabled
							if (e instanceof RouteButtonElement) {
								sel = e.isSelected(xs,ys);
							} 
						} else {
						  sel = e.isSelected(xs,ys);
						}
						if (sel) { //mLastTouchX, mLastTouchY)) {
							if (enableEdit) {
								Dialogs.selectAddressDialog(e); //    					
							} else {
								e.toggle();
							}
							break; // only 1 can be selected with one touch
						}
					}
				}

			} else {
				if (DEBUG) Log.d(TAG,"scaling wait - delta-t="+deltaT);
			}
			break;
		}

        case MotionEvent.ACTION_CANCEL: {
        	if (DEBUG)  Log.d(TAG,"ACTION_CANCEL - mPosX="+mPosX+" mPosY="+mPosY);
            mActivePointerId = INVALID_POINTER_ID;
          
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = event.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = event.getX(newPointerIndex);
                mLastTouchY = event.getY(newPointerIndex);
                mActivePointerId = event.getPointerId(newPointerIndex);
       
            }
            break;
        }
        default:
        	if (DEBUG) Log.d(TAG,"unknown motion event = "+event.toString());
        } // end switch

		return true; //super.onTouchEvent(event);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG,"surface changed - format="+format+" w="+width+" h="+height);
        mWidth = width;
        mHeight = height;
        controlAreaRect = new Rect(0,0,mWidth,mHeight/8);
        locoControlArea.recalcGeometry();
	}


	public void doDraw(Canvas canvas) {

		canvas.drawColor(BG_COLOR);
		
	    // draw Panel and scale with zoom
		//if (USS == true)
	    myBitmap.eraseColor(Color.TRANSPARENT); // Color.DKGRAY);
	    
	    // label with panel name and display green "unlock", if zoom enabled
	    int topLeft = mHeight/8;
	    if (zoomEnabled) {
	    	canvas.drawBitmap(bitmaps.get("unlock"),5, topLeft,null);
	    } else {
	    	canvas.drawBitmap(bitmaps.get("lock"),5, topLeft,null);
	    }
        canvas.drawText(panelName, 50, topLeft+24, panelNamePaint);
        
	    Matrix matrix = new Matrix();
		matrix.postScale(scale,scale);	   
		matrix.postTranslate(xoff, yoff);
		for (PanelElement e: panelElements) {
			e.doDraw(myCanvas);       
		}
	    drawRaster(myCanvas, RASTER);
	    
		canvas.drawBitmap(myBitmap,matrix, null);	
		canvas.drawRect(controlAreaRect, paintControlAreaBG);
       
	    locoControlArea.draw(canvas); // NOT scaled with zoom

	}



	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (zoomEnabled) {
			mScaleFactor *= detector.getScaleFactor();
            Log.d(TAG,"mScaleFactor="+mScaleFactor);
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
			 Log.d(TAG,"mScaleFactor (lim)="+mScaleFactor);
            scale = mScaleFactor;
			invalidate();
			scalingTime = System.currentTimeMillis();
			}
			return true;
		}
	}
	

	
	private void drawRaster(Canvas canvas, int step) {
		for (int x = 0; x<canvas.getWidth(); x+=step) {
			for (int y = 0; y<canvas.getHeight(); y+=step) {
				canvas.drawPoint(x, y, rasterPaint);
			}
		}
	}
	
	
}
