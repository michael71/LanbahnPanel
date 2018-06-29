package de.blankedv.lanbahnpanel

import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log

open class ControlButton(private val xrel: Float, private val yrel: Float  // relative position in control area.
                         , private val bmON: Bitmap?, private val bmOFF: Bitmap?) {
    private var w = 10
    private var h = 10 // half of the bitmap width and height
    private var blink: Long = 0
    private var toggleBlink: Boolean = false
    // x and y are actual position of bitmap placing, NOT the center!
    var x = 0f
    var y = 0f

    init {
        w = bmON!!.width / 2
        h = bmON.height / 2
        if (controlAreaRect != null) {
            recalcXY()
        }

    }


    fun isTouched(xt: Float, yt: Float): Boolean {
        if (xt > x && xt < x + w.toFloat() + w.toFloat() && yt > y && yt < y + h.toFloat() + h.toFloat()) {
            if (DEBUG) Log.d(TAG, this.toString() + " was touched.")
            return true
        } else {
            if (DEBUG) Log.d(TAG, this.toString() + " was not touched.")
            return false
        }
    }

    fun recalcXY() {
        if (bmON == null) {
            w = (controlAreaRect.right - controlAreaRect.left) / 30
            h = (controlAreaRect.bottom - controlAreaRect.top) / 3
        }
        x = controlAreaRect.left + xrel * (controlAreaRect.right - controlAreaRect.left) - w  // position where bitmap is drawn
        y = controlAreaRect.top + yrel * (controlAreaRect.bottom - controlAreaRect.top) - h
        if (DEBUG) Log.d(TAG, this.toString() + "btn recalc, x=" + x + " y=" + y + " w=" + w + " h=" + h)
    }

    fun doDraw(c: Canvas, state: Boolean) {

        if (state) {
            c.drawBitmap(bmON!!, x, y, null)

        } else {
            c.drawBitmap(bmOFF, x, y, null)
        }
    }

    fun doDrawBlink(c: Canvas, state: Boolean) {

        if (System.currentTimeMillis() - blink > 300) {
            toggleBlink = !toggleBlink
            blink = System.currentTimeMillis()
        }

        if (state) {
            if (toggleBlink) {
                c.drawBitmap(bmOFF, x, y, null)
            } else {
                c.drawBitmap(bmON!!, x, y, null)
            }
        } else {
            c.drawBitmap(bmOFF, x, y, null)
        }
    }


    fun doDraw(c: Canvas) {
        c.drawBitmap(bmON!!, x, y, null)
    }

    fun doDraw(c: Canvas, value: Int, p: Paint) {
        // (x,y) drawing position for text is DIFFERENT than for bitmaps.(upper left)
        // (x,y) = lower left start of text.
        c.drawBitmap(bmON!!, x, y, null)
        //c.drawRect(x+3,y+3,x+w+w-3,y+h+h-3, bg);
        // no loco control		c.drawText(""+getLocoAdr(),x+w*0.6f,y+h*1.42f, p);

    }
}
