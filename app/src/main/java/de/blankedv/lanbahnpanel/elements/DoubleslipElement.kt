package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints.bgPaint
import de.blankedv.lanbahnpanel.util.LPaints.greenPaint
import de.blankedv.lanbahnpanel.util.LPaints.linePaint2
import de.blankedv.lanbahnpanel.util.LPaints.redPaint
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.railroad.Commands
import de.blankedv.lanbahnpanel.util.Utils


class DoubleslipElement : ActivePanelElement {
    var dx = INVALID_INT
    var dy = 0
    var dx2 = 0
    var dy2 = 0
    // for turnouts which can be interactivly set from panel

    constructor() {
        adr = INVALID_INT
        adr2 = INVALID_INT
        state = STATE_UNKNOWN
        dx = INVALID_INT

    }

    constructor(dslip: PanelElement) {
        x = dslip.x
        y = dslip.y
        x2 = dslip.x2
        y2 = dslip.y2
        xt = dslip.xt
        yt = dslip.yt
        adr = dslip.adr
        adr2 = dslip.adr2
        state = STATE_UNKNOWN
        invert = DISP_STANDARD
        dx = INVALID_INT

    }

    public fun initDxEtc() {
        dx = xt - x
        dy = yt - y
        dx2 = x2 - x
        dy2 = y2 - y
        if ( (dx < 0) or (dy < 0) or (dx2 <0) or (dy2 <0) )
            Log.e(TAG,"ERROR, check doubleslip dx etc signs at (x,y)=($x,$y)")
    }

    override fun getSensitiveRect(): Rect {
        if (dx == INVALID_INT) initDxEtc()
        val minx = Utils.min(x -dx, x-dx2 , xt, x2) - RASTER / 13
        val maxx = Utils.max(x -dx, x-dx2 , xt, x2) + RASTER / 13
        val miny = Utils.min(y -dy, y-dy2 , yt, y2) - RASTER / 13
        val maxy = Utils.max(y -dy, y-dy2 , yt, y2) + RASTER / 13
        return Rect(minx, miny, maxx, maxy)
    }

    override fun doDraw(canvas: Canvas) {
        if (dx == INVALID_INT) initDxEtc()

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        if (prefs.getBoolean(KEY_ENABLE_EDIT, false)) {
            if (invert == DISP_STANDARD) {
                canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), greenPaint)
                canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), redPaint)
            } else {
                canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), greenPaint)
                canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), redPaint)
            }
        } else if (adr == INVALID_INT) {
            canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaint2)
            canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), linePaint2)
        } else {


            if (state == STATE_UNKNOWN) {
                canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), bgPaint)
                canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), bgPaint)
            } else {

                val bit0 = state and 0x01
                val bit1 = (state.shr(1) and 0x01)

                if (bit0 == 0) {  // == STATE_CLOSED
                    if (invert == DISP_STANDARD) {
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), bgPaint)
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaint2)
                    } else {
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), bgPaint)
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), linePaint2)
                    }
                } else {  // bit0 == 1
                    if (invert == DISP_STANDARD) {
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), bgPaint)
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), linePaint2)
                    } else {
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), bgPaint)
                        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaint2)
                    }
                }
                if (bit1 == 0) {  // == STATE_CLOSED
                    if (invert2 == DISP_STANDARD) {
                        canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), bgPaint)
                        canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), linePaint2)
                    } else {
                        canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), bgPaint)
                        canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(),linePaint2)
                    }
                } else {  // bit0 == 1
                    if (invert2 == DISP_STANDARD) {
                        canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), bgPaint)
                        canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), linePaint2)
                    } else {
                        canvas.drawLine(((x-dx) * prescale).toFloat(), ((y-dy) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), bgPaint)
                        canvas.drawLine(((x-dx2) * prescale).toFloat(), ((y-dy2) * prescale).toFloat(), (x * prescale).toFloat(), (y * prescale).toFloat(), linePaint2)
                    }
                }

            }

        }

        if (prefs.getBoolean(KEY_DRAW_ADR, false)) doDrawAddresses(canvas)
    }

    override fun toggle() {
        if (prefs.getBoolean(KEY_ROUTING, false)) return  // do not set turnouts by hand if routes are enabled

        if (adr == INVALID_INT) return  // do nothing if no sx address defined.

        if (System.currentTimeMillis() - lastToggle < 250) return   // do not toggle twice within 250msecs

        lastToggle = System.currentTimeMillis()  // reset toggle timer


        // doubleslip: 4 states
        if (state >= 3) {
            state = 0
        } else {
            state++
        }

        // state = STATE_UNKNOWN; // until updated via lanbahn message
        /** was:
        val cmd = "SET $adr $state"
        if (!sendQ.contains(cmd)) {
        sendQ.add(cmd) // ==> send changed data over network turnout interface
        } */
        Commands.setChannel(adr, state, DoubleslipElement::class.java)

        if (DEBUG) Log.d(TAG, "toggle(adr=$adr) new state=$state")
    }

}
