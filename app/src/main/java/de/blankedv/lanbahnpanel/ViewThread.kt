package de.blankedv.lanbahnpanel

import android.graphics.Canvas
import android.view.SurfaceHolder

import java.util.TimerTask

class ViewThread(private val mPanel: Panel) : Thread() {
    private val mHolder: SurfaceHolder
    private var mRun = false


    init {
        mHolder = mPanel.holder
    }

    fun setRunning(run: Boolean) {
        mRun = run
    }

    override fun run() {
        while (mRun) {
            val canvas = mHolder.lockCanvas()
            if (canvas != null) {
                synchronized(mPanel.holder) {
                    mPanel.doDraw(canvas)
                }
                mHolder.unlockCanvasAndPost(canvas)

                try {
                    // max 5 frames per second
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
    }

}
