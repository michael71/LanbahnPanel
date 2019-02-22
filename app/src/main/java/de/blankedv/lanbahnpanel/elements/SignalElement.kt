package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints.greenSignal
import de.blankedv.lanbahnpanel.util.LPaints.redSignal
import de.blankedv.lanbahnpanel.util.LPaints.signalLine
import de.blankedv.lanbahnpanel.util.LPaints.whitePaint
import de.blankedv.lanbahnpanel.util.LPaints.yellowSignal
import de.blankedv.lanbahnpanel.model.*

class SignalElement : ActivePanelElement {

    // for signals which can be interactivly set from panel

    private val radius = 3f * prescale
    private val radiusL = 3.5f *prescale

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {}

    constructor() {
        adr = INVALID_INT
        state = STATE_UNKNOWN
    }

    override fun getSensitiveRect() : Rect {
       return Rect(x - RASTER / 5, y - RASTER / 7, x + RASTER / 5, y + RASTER / 7)
    }

    override fun doDraw(canvas: Canvas) {

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), signalLine)
        canvas.drawLine((x2 * prescale).toFloat(), (y2 - 2.5f) * prescale, (x2 * prescale).toFloat(), (y2 + 2.5f) * prescale, signalLine)
        canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, whitePaint)
        if (adr == INVALID_INT) {
            canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, whitePaint)
        } else {
            if (state == STATE_RED) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radiusL, redSignal)
            } else if (state == STATE_GREEN) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radiusL, greenSignal)
            } else if (state == STATE_YELLOW || state == STATE_YELLOW_FEATHER) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radiusL, yellowSignal)
            } else if (state == STATE_UNKNOWN) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radiusL, whitePaint)
            }

        }

        if (prefs.getBoolean(KEY_DRAW_ADR, false)) doDrawAddresses(canvas)
    }

    override fun toggle() {

        if (adr == INVALID_INT) return  // do nothing if no sx address defined.

        if (System.currentTimeMillis() - lastToggle < 250) return   // do not toggle twice within 250msecs

        lastToggle = System.currentTimeMillis()  // reset toggle timer


        // only for a SIMPLE SIGNAL RED / GREEN
        if (state == 0) {
            state = 1
        } else {
            state = 0
        }

        // state = STATE_UNKNOWN; // until updated via lanbahn message
        val cmd = "SET $adr $state"
        if (!sendQ.contains(cmd)) {
            sendQ.add(cmd) // ==> send changed data over network turnout interface
        }
        if (DEBUG) Log.d(TAG, "toggle(adr=$adr) new state=$state")
    }

    override fun isSelected(xs: Int, ys: Int): Boolean {
        // for signal check radius = RASTER/5 around signal center
        val minx = x - radiusL
        val maxx = x + radiusL
        val miny = y - radiusL
        val maxy = y + radiusL

        // the touchpoint should be within rectangle of panel element
        if (xs >= minx && xs <= maxx && ys >= miny && ys <= maxy) {
            if (DEBUG)
                Log.d(TAG, "selected adr=$adr type=$type  ($x,$y)")
            return true
        } else {
            // if (DEBUG) Log.d(TAG, "No Signal selection");
            return false
        }
    }
}
