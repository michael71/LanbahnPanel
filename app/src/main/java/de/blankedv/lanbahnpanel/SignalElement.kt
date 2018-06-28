package de.blankedv.lanbahnpanel

import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*
import de.blankedv.lanbahnpanel.LinePaints.*
import android.graphics.Canvas
import android.util.Log

class SignalElement : ActivePanelElement {

    // for signals which can be interactivly set from panel

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {}

    constructor() {
        adr = INVALID_INT
        state = ActivePanelElement.STATE_UNKNOWN
    }

    override fun doDraw(canvas: Canvas) {

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getSignalLine())
        canvas.drawLine((x2 * prescale).toFloat(), (y2 - 2.5f) * prescale, (x2 * prescale).toFloat(), (y2 + 2.5f) * prescale, INSTANCE.getSignalLine())
        canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, INSTANCE.getWhitePaint())
        if (enableEdit || adr == INVALID_INT) {
            canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, INSTANCE.getWhitePaint())
        } else {
            if (state == ActivePanelElement.STATE_RED) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3.5f * prescale, INSTANCE.getRedSignal())
            } else if (state == ActivePanelElement.STATE_GREEN) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3.5f * prescale, INSTANCE.getGreenSignal())
            } else if (state == ActivePanelElement.STATE_YELLOW || state == ActivePanelElement.STATE_YELLOW_FEATHER) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3.5f * prescale, INSTANCE.getYellowSignal())
            } else if (state == ActivePanelElement.STATE_UNKNOWN) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3.5f * prescale, INSTANCE.getWhitePaint())
            }

        }

        if (drawAddresses) doDrawAddresses(canvas)
    }

    override fun toggle() {
        if (enableRoutes) return  // do not set signals by hand if routes are enabled

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
        sendQ.add("SET $adr $state")  // ==> send changed data over network turnout interface
        if (DEBUG) Log.d(TAG, "toggle(adr=$adr) new state=$state")
    }

    override fun isSelected(xs: Int, ys: Int): Boolean {
        // for signal check radius = RASTER/5 around signal center
        val minx = x - RASTER / 5
        val maxx = x + RASTER / 5
        val miny = y - RASTER / 5
        val maxy = y + RASTER / 5

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
