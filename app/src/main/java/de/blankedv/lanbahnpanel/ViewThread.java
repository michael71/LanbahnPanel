package de.blankedv.lanbahnpanel;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.TimerTask;

public class ViewThread extends Thread {

	    private Panel mPanel;
	    private SurfaceHolder mHolder;
	    private boolean mRun = false;

	 
	    public ViewThread(Panel panel) {
	        mPanel = panel;
	        mHolder = mPanel.getHolder();
	    }
	 
	    public void setRunning(boolean run) {
	        mRun = run;
	    }
	 
	    @Override
	    public void run() {
	        while (mRun) {
                Canvas canvas = mHolder.lockCanvas();
	            if (canvas != null) {
	                mPanel.doDraw(canvas);
	                mHolder.unlockCanvasAndPost(canvas);
				 	try {
						// do not redraw more than 10 times per second
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
	        }
	    }

}
