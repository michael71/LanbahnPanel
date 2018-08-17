package de.blankedv.lanbahnpanel.loco


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import de.blankedv.lanbahnpanel.model.DEBUG
import de.blankedv.lanbahnpanel.model.TAG

import de.blankedv.lanbahnpanel.model.controlAreaRect
import de.blankedv.lanbahnpanel.util.LanbahnBitmaps.bitmaps

/**
 * a button in the loco control area
 */
class LocoButton {
    private var xrel: Float = 0.toFloat()
    private var yrel: Float = 0.toFloat()  // relative position in control area.
    private var bmON: Bitmap? = null
    private var bmOFF: Bitmap? = null
    private var w = 10
    private var h = 10 // half of the bitmap width and height

    // x and y are actual position of bitmap placing, NOT the center!
    var x = 0f
    var y = 0f

    /** button with two bitmaps (dependent on state)
     *
     */
    constructor(x2: Float, y2: Float, on: Bitmap, off: Bitmap) {
        this.xrel = x2
        this.yrel = y2
        bmON = on
        bmOFF = off
        w = bmON!!.width / 2
        h = bmON!!.height / 2
        if (controlAreaRect != null) {
            recalcXY()
        }
    }

    constructor(x2: Float, y2: Float, on: Bitmap) {
        this.xrel = x2
        this.yrel = y2
        bmON = on
        bmOFF = on
        w = bmON!!.width / 2
        h = bmON!!.height / 2
        if (controlAreaRect != null) {
            recalcXY()
        }
    }


    /** pure text button
     *
     */
    constructor(x2: Float, y2: Float) {
        this.xrel = x2
        this.yrel = y2
        bmON = null
        bmOFF = null
        if (controlAreaRect != null) {
            w = (controlAreaRect!!.right - controlAreaRect!!.left) / 30
            h = (controlAreaRect!!.bottom - controlAreaRect!!.top) / 3
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
            w = (controlAreaRect!!.right - controlAreaRect!!.left) / 30
            h = (controlAreaRect!!.bottom - controlAreaRect!!.top) / 3
        }
        x = controlAreaRect!!.left + xrel * (controlAreaRect!!.right - controlAreaRect!!.left) - w  // position where bitmap is drawn
        y = controlAreaRect!!.top + yrel * (controlAreaRect!!.bottom - controlAreaRect!!.top) - h
        if (DEBUG)
            Log.d(TAG, this.toString() + " LocoBtn recalc, x=" + x + " y=" + y + " w=" + w + " h=" + h)
    }

    // for 2 states
    fun doDraw(c: Canvas, state: Boolean) {
        if (state) {
            c.drawBitmap(bmON!!, x, y, null)
        } else {
            c.drawBitmap(bmOFF!!, x, y, null)
        }
    }


    fun doDraw(c: Canvas, txt : String, p: Paint) {
        // (x,y) drawing position for text is DIFFERENT than for bitmaps.(upper left)
        // (x,y) = lower left start of text.
        c.drawText(txt, x + w * 0.6f, y + h * 1.42f, p)
    }

    companion object {
        private val bg = Paint()
    }

}
