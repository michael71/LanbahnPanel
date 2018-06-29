package de.blankedv.lanbahnpanel.view

import android.view.SurfaceHolder

class ViewThread(private val mPanel: Panel) : Thread() {
    private val mHolder: SurfaceHolder = mPanel.holder
    private var mRun = false


    init {
        // nothing to do
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
