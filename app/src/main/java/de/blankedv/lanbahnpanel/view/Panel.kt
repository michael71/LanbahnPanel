package de.blankedv.lanbahnpanel.view

import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView

import de.blankedv.lanbahnpanel.elements.DoubleslipElement
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication
import de.blankedv.lanbahnpanel.elements.RouteButtonElement
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.pSett
import de.blankedv.lanbahnpanel.loco.LocoControlArea
import de.blankedv.lanbahnpanel.util.LPaints.paintControlAreaBG


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


    var locoControlArea: LocoControlArea? = null

    private lateinit var toneG: ToneGenerator
    private var toneEnabled = false

    private var time0 = System.currentTimeMillis() - 10001

    // Bitmap.Config.ARGB_4444 is deprecated and not used for SDK-version >= kitkat
    private var mBitmap = Bitmap.createBitmap(2200, 1300,
            Bitmap.Config.ARGB_8888)
    private var mCanvas = Canvas(mBitmap)

    init {
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        holder.addCallback(this)

        locoControlArea = LocoControlArea(context)
        mThread = ViewThread(this)

        try {
            toneG = ToneGenerator(AudioManager.STREAM_ALARM, 70)
            toneEnabled = true;
        } catch (e: Exception) {
            Log.e(TAG, "could not init ToneGenerator => disabled Tones.")
        }

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
                    Log.d(TAG, "ACTION_DOWN - (abs) x=$x  y=$y");
                    mActivePointerId = event.getPointerId(0)
                    //if (!mScaleDetector.isInProgress) {
                    //  locoControlArea?.checkIncrDecrSpeed(xint topLeft = mHeight/8;, y)
                    //}
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
                        if ((prefs.getString(KEY_SCALE_PREF, "auto") == "manual") && mX > 300 && mY > 200) {
                            pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].xoff += dx
                            pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].yoff += dy
                            scalingTime = System.currentTimeMillis()  // avoid control of SX elements during pan-move
                            if (DEBUG) Log.d(TAG, "new xoff/yoff (qua=${prefs.getInt(KEY_QUADRANT, 0)}) - xoff=${pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].xoff} + yoff=${pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].yoff}")
                        }
                        // invalidate();
                        // if (DEBUG)  Log.d(TAG,"mPosX="+mPosX+" mPosY="+mPosY);

                        locoControlArea?.checkSpeedMove(x, y)
                    }

                    mLastTouchX = x
                    mLastTouchY = y
                }

                MotionEvent.ACTION_UP -> {
                    if (DEBUG) Log.d(TAG, "ACTION_UP")
                    mActivePointerId = INVALID_INT

                    if (!mScaleDetector.isInProgress) {
                        // assuming control area is always at the top !!
                        var controlAreaBottom = 0
                        if (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) {
                            controlAreaBottom = controlAreaRect?.bottom ?: 0
                        }

                        if (mLastTouchY < controlAreaBottom) {
                            Log.d(TAG,
                                    "ACTION_UP _Checking Loco Control  at: mlastTouchX="
                                            + mLastTouchX + "  mLastTouchY"
                                            + mLastTouchY)
                            locoControlArea?.checkTouch(mLastTouchX, mLastTouchY)
                        } else {

                            //Log.d(TAG,"ACTION_UP _Checking panel elements at: mlastTouchX="+mLastTouchX+"  mLastTouchY"+mLastTouchY);
                            val xs = Math.round((mLastTouchX - pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].xoff)
                                    / pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].scale / prescale) // reduced by overall dimension scaling factors
                            val ys = Math.round((mLastTouchY - pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].yoff)
                                    / pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].scale / prescale)

                            Log.d(TAG, "ACTION_UP _Checking panel elements at: xs=$xs  ys$ys")
                            if (prefs.getBoolean(KEY_ROUTING, false)) {
                                // check route buttons first when routing is enabled
                                for (e in panelElements.filter { es -> (es is RouteButtonElement) }) {
                                    if (e.isSelected(xs, ys)) {
                                        e.toggle()
                                        if (toneEnabled) {
                                            toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 100)
                                        }
                                        return true
                                    }
                                }
                            }
                            for (e in panelElements.filter { es -> !(es is RouteButtonElement) }) {
                                // check other panel elements
                                if (e.isSelected(xs, ys)) { //mLastTouchX, mLastTouchY)) {
                                    e.toggle()
                                    if (toneEnabled) { // vibrate(500L)
                                        toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 100)
                                    }
                                    break // only 1 can be selected with one touch
                                }
                            }
                        }

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
        Log.i(TAG, "surface changed - mWidth=$width mHeight=$height")
        mWidth = width
        mHeight = height

        controlAreaRect = Rect(0, 0, mWidth, mHeight / 8)
        locoControlArea?.recalcGeometry()

        if (prefs.getString(KEY_SCALE_PREF, "auto") == "auto") {
            LanbahnPanelApplication.calcAutoScale(mWidth, mHeight, prefs.getInt(KEY_QUADRANT, 0))
        }
    }


    fun doDraw(canvas: Canvas) {

        canvas.drawColor(LPaints.BG_COLOR)   // empty canvas
        // draw Panel and scale with zoom
        //if (USS == true)
        mBitmap.eraseColor(Color.TRANSPARENT) // Color.DKGRAY);

        var sc = pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].scale
        var xo = pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].xoff
        var yo = pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].yoff

        if ((DEBUG) and ((System.currentTimeMillis() - time0) > 10000)) {
            //Log.d(TAG, "panelRect x=(" + panelRect.left + "," + panelRect.right + ") y=(" + panelRect.top + "," + panelRect.bottom + ")")
            // Log.d(TAG, "qua=$selQuadrant mWidth=$mWidth mHeight=$mHeight - actual scale=$sc xoff=$xo yoff=$yo} hCalc=$hCalc hRect=$hRect")
            // Log.d(TAG, "controlAreaRect =(${controlAreaRect?.left},${controlAreaRect?.right}),(${controlAreaRect?.top},${controlAreaRect?.bottom})")

            // Samsung SM-T580  panelRect 2040x960 *prescale (=2)
            // metric 1920x1200 pixel , ratio 1.6
            /* for the 4 quadrants       (full layout  autoscale: 0.94 / 0 0)       (experimental)
                                         autoscale centered  0.94 / 0 70
            q1: scale = 2*scale, xoff = 0, yoff = 0                               (1.8 /  0     20)
            q2:    xoff = -((xmax+10) * prescale  yoff = 0                        (1.8 / -1770  155)
            q3:    xoff = 0   yoff = -((Ymax+10) * prescale                       (1.8 / 45   -680)
            q4:    xoff = -((xmax+10) * prescale   yoff = -((Ymax+10) * prescale  (1.8 / -1770 -620)
             */
            time0 = System.currentTimeMillis()
        }

        val matrix = Matrix()
        matrix.postScale(sc, sc)
        matrix.postTranslate(xo, yo)
        for (e in panelElements) {
            //if (e !is DoubleslipElement) {
            e.doDraw(mCanvas)
            //}
        }
        //debugDraw(mCanvas)

        drawRaster(mCanvas, RASTER)

        canvas.drawBitmap(mBitmap, matrix, null)

        if (prefs.getBoolean(KEY_ENABLE_LOCO_CONTROL, false)) {
            canvas.drawRect(controlAreaRect, paintControlAreaBG);
            locoControlArea?.draw(canvas); // NOT scaled !

        }

    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (prefs.getString(KEY_SCALE_PREF, "auto") == "manual") {
                mScaleFactor *= detector.scaleFactor

                // Don't let the object get too small or too large.
                mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 4.0f))
                Log.d(TAG, "new scale (qua=${prefs.getInt(KEY_QUADRANT, 0)}): scale=$mScaleFactor (limited)")
                pSett.qClip[prefs.getInt(KEY_QUADRANT, 0)].scale = mScaleFactor
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

    /* does not work for most tablets
    private fun vibrate(ms : Long) {

        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (v.hasVibrator()) {
            Log.d(TAG, "Vibrating !!!");
        } else {
            Log.v(TAG, "Device cannot vibrate");
        }

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(ms);
        }
    } */

    private fun debugDraw(mCanvas: Canvas) {
        // surface  w=1280 h=618
        //mCanvas.drawPoint(panelRect.right.toFloat(), panelRect.bottom.toFloat(), LPaints.greenPaint)
        mCanvas.drawPoint(0f, 0f, LPaints.redPaint)
        mCanvas.drawPoint(300f, 80f, LPaints.redPaint)
    }

}
