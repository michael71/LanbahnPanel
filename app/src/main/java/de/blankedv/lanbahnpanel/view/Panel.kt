package de.blankedv.lanbahnpanel.view

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import de.blankedv.lanbahnpanel.util.AndroBitmaps.bitmaps
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication
import de.blankedv.lanbahnpanel.elements.RouteButtonElement
import de.blankedv.lanbahnpanel.model.*


/**
 * the main panel of the application is comprised of two parts: a (small height) CONTROL area
 * at the top and the larger part with the main SWITCH PANEL at the bottom, handles all touch events
 */
class Panel(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var mThread: ViewThread? = null

    private var mX: Int = 0
    private var mY: Int = 0

    private var mPosX: Float = 0.toFloat()
    private var mPosY: Float = 0.toFloat()

    private var mLastTouchX: Float = 0.toFloat()
    private var mLastTouchY: Float = 0.toFloat()
    private var mActivePointerId = INVALID_INT

    private val mScaleDetector: ScaleGestureDetector
    private var mScaleFactor = 1f
    private var scalingTime = 0L

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    // public static Bitmap myBitmap = Bitmap.createBitmap(4000,1600,
// Bitmap.Config.ARGB_4444);
    private var mBitmap = Bitmap.createBitmap(2000, 800,
            Bitmap.Config.ARGB_4444)
    private var mCanvas = Canvas(mBitmap)

    init {
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())

        LanbahnPanelApplication.updatePanelData()
        holder.addCallback(this)
        mThread = ViewThread(this)
    }


    //  start and stop the dedicated rendering thread
    //  implementing SurfaceHolder Callback for your SurfaceView.
    //  This should be enough to
    //	limit drawing only when the SurfaceView is visible.

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!mThread!!.isAlive) {
            mThread = ViewThread(this)
            mThread!!.setRunning(true)
            mThread!!.start()
        }
    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mThread!!.isAlive) {
            mThread!!.setRunning(false)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        synchronized(holder) {
            // Let the ScaleGestureDetector inspect all events.
            mScaleDetector.onTouchEvent(event)

            val action = event.action


            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    val y = event.y

                    mLastTouchX = x
                    mLastTouchY = y

                    mX = x.toInt() // /scale);
                    mY = y.toInt() ///scale);

                    mPosX = 0f
                    mPosY = 0f
                    //if (DEBUG)  Log.d(TAG,"ACTION_DOWN - (scaled) mX="+mX+"  mY"+mY);
                    //if (DEBUG)  Log.d(TAG,"ACTION_DOWN - (abs) x="+x+"  y"+y);
                    mActivePointerId = event.getPointerId(0)
                }
                MotionEvent.ACTION_MOVE -> {
                    val pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    // Only move if the ScaleGestureDetector isn't processing a gesture.
                    if (!mScaleDetector.isInProgress) {
                        val dx = x - mLastTouchX
                        val dy = y - mLastTouchY

                        mPosX += dx
                        mPosY += dy
                        if (zoomEnabled && mX > 300 && mY > 200) {
                            xoff += dx
                            yoff += dy
                            scalingTime = System.currentTimeMillis()  // avoid control of SX elements during pan-move
                        }
                        // invalidate();
                        // if (DEBUG)  Log.d(TAG,"mPosX="+mPosX+" mPosY="+mPosY);
                        // no loco control
                        //controlArea.checkSpeedMove(x, y);
                    }

                    mLastTouchX = x
                    mLastTouchY = y
                }/*if (enableEdit) {
            	selSxAddress.dismiss();
            } */

                MotionEvent.ACTION_UP -> {
                    if (DEBUG) Log.d(TAG, "ACTION_UP")
                    mActivePointerId = INVALID_INT

                    // do SX control only when NOT scaling (and wait 1 sec after scaling
                    val deltaT = System.currentTimeMillis() - scalingTime
                    if (!mScaleDetector.isInProgress) { //&& (deltaT > SCALING_WAIT)) {
                        // assuming control area is always at the top !!

                        //Log.d(TAG,"ACTION_UP _Checking panel elements at: mlastTouchX="+mLastTouchX+"  mLastTouchY"+mLastTouchY);
                        val xs = Math.round((mLastTouchX - xoff) / scale / prescale) // reduced by overall dimension scaling factors
                        val ys = Math.round((mLastTouchY - yoff) / scale / prescale)

                        Log.d(TAG, "ACTION_UP _Checking panel elements at: xs=$xs  ys$ys")
                        for (e in panelElements) {
                            var sel = false
                            if (enableRoutes) {
                                // check only!! route buttons when routing is enabled
                                if (e is RouteButtonElement) {
                                    sel = e.isSelected(xs, ys)
                                }
                            } else {
                                sel = e.isSelected(xs, ys)
                            }
                            if (sel) { //mLastTouchX, mLastTouchY)) {
                                if (enableEdit) {
                                    Dialogs.selectAddressDialog(e) //
                                } else {
                                    e.toggle()
                                }
                                break // only 1 can be selected with one touch
                            }
                        }


                    } else {
                        if (DEBUG) Log.d(TAG, "scaling wait - delta-t=$deltaT")
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (DEBUG) Log.d(TAG, "ACTION_CANCEL - mPosX=$mPosX mPosY=$mPosY")
                    mActivePointerId = INVALID_INT
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex = event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mLastTouchX = event.getX(newPointerIndex)
                        mLastTouchY = event.getY(newPointerIndex)
                        mActivePointerId = event.getPointerId(newPointerIndex)
                    }
                }
                else -> if (DEBUG) Log.d(TAG, "unknown motion event = " + event.toString())
            } // end switch

        } // end synchronized
        return true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                height: Int) {
        Log.i(TAG, "surface changed - format=$format w=$width h=$height")
        mWidth = width
        mHeight = height

    }


    fun doDraw(canvas: Canvas) {

        canvas.drawColor(LPaints.BG_COLOR)

        // draw Panel and scale with zoom
        //if (USS == true)
        mBitmap.eraseColor(Color.TRANSPARENT) // Color.DKGRAY);

        // label with panel name and display green "unlock", if zoom enabled
        val topLeft = ( mHeight * 0.05).toFloat()
        if (zoomEnabled) {
            canvas.drawBitmap(bitmaps["unlock"], 5f, topLeft, null)
        } else {
            canvas.drawBitmap(bitmaps["lock"], 5f, topLeft, null)
        }


        val matrix = Matrix()
        matrix.postScale(scale, scale)
        matrix.postTranslate(xoff, yoff)
        for (e in panelElements) {
            e.doDraw(mCanvas)
        }
        drawRaster(mCanvas, RASTER)

        canvas.drawBitmap(mBitmap, matrix, null)

    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (zoomEnabled) {
                mScaleFactor *= detector.scaleFactor
                Log.d(TAG, "mScaleFactor=$mScaleFactor")
                // Don't let the object get too small or too large.
                mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f))
                Log.d(TAG, "mScaleFactor (lim)=$mScaleFactor")
                scale = mScaleFactor
                invalidate()
                scalingTime = System.currentTimeMillis()
            }
            return true
        }
    }


    private fun drawRaster(canvas: Canvas, step: Int) {
        var x = 0
        while (x < canvas.width) {
            var y = 0
            while (y < canvas.height) {
                canvas.drawPoint(x.toFloat(), y.toFloat(), LPaints.rasterPaint)
                y += step
            }
            x += step
        }
    }

}
