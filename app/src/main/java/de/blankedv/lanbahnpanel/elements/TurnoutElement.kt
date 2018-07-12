package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints.bgPaint
import de.blankedv.lanbahnpanel.util.LPaints.greenPaint
import de.blankedv.lanbahnpanel.util.LPaints.linePaint2
import de.blankedv.lanbahnpanel.util.LPaints.redPaint
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils


class TurnoutElement : ActivePanelElement {

    // for turnouts which can be interactivly set from panel

    constructor() {
        adr = INVALID_INT
        state = STATE_UNKNOWN
    }

    constructor(turnout: PanelElement) {
        x = turnout.x
        y = turnout.y
        x2 = turnout.x2
        y2 = turnout.y2
        xt = turnout.xt
        yt = turnout.yt
        adr = turnout.adr
        state = STATE_UNKNOWN
    }

    override fun getSensitiveRect() : Rect {
        val minx = Utils.min(x, xt, x2) - RASTER / 11
        val maxx = Utils.max(x, xt, x2) + RASTER / 11
        val miny = Utils.min(y, yt, y2) - RASTER / 11
        val maxy = Utils.max(y, yt, y2) + RASTER / 11
        return Rect(minx, miny, maxx, maxy)
    }

    override fun doDraw(canvas: Canvas) {

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        if (enableEdit) {
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), greenPaint)
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), redPaint)
        } else if (adr == INVALID_INT) {
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaint2)
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), linePaint2)
        } else {

            if (state == STATE_CLOSED) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), bgPaint)
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaint2)
            } else if (state == STATE_THROWN) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), bgPaint)
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), linePaint2)
            } else if (state == STATE_UNKNOWN) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), bgPaint)
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), bgPaint)
            }
        }

        if (drawAddresses) doDrawAddresses(canvas)
    }

    override fun toggle() {
        if (enableRoutes) return  // do not set turnouts by hand if routes are enabled

        if (adr == INVALID_INT) return  // do nothing if no sx address defined.

        if (System.currentTimeMillis() - lastToggle < 250) return   // do not toggle twice within 250msecs

        lastToggle = System.currentTimeMillis()  // reset toggle timer


        // only for a SIMPLE turnout
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

}
