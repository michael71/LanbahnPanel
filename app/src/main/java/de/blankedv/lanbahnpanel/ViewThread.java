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
	 				synchronized (mPanel.getHolder()) {
						mPanel.doDraw(canvas);
					}
					mHolder.unlockCanvasAndPost(canvas);

				 	try {
						// max 5 frames per second
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
	        }
	    }

}
