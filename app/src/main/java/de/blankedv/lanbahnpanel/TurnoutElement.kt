package de.blankedv.lanbahnpanel

import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*
import de.blankedv.lanbahnpanel.LinePaints.*
import android.graphics.Canvas
import android.util.Log


class TurnoutElement : ActivePanelElement {

    // for turnouts which can be interactivly set from panel

    constructor(type: String, x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {}

    constructor() {
        adr = INVALID_INT
        state = ActivePanelElement.STATE_UNKNOWN
    }

    constructor(turnout: PanelElement) {
        x = turnout.x
        y = turnout.y
        x2 = turnout.x2
        y2 = turnout.y2
        xt = turnout.xt
        yt = turnout.yt
        adr = INVALID_INT
        state = ActivePanelElement.STATE_UNKNOWN
    }

    override fun doDraw(canvas: Canvas) {

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        if (enableEdit) {
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getGreenPaint())
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), INSTANCE.getRedPaint())
        } else if (adr == INVALID_INT) {
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaint2())
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), INSTANCE.getLinePaint2())
        } else {

            if (state == ActivePanelElement.STATE_CLOSED) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), INSTANCE.getBgPaint())
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaint2())
            } else if (state == ActivePanelElement.STATE_THROWN) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getBgPaint())
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), INSTANCE.getLinePaint2())
            } else if (state == ActivePanelElement.STATE_UNKNOWN) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (xt * prescale).toFloat(), (yt * prescale).toFloat(), INSTANCE.getBgPaint())
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getBgPaint())
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
        sendQ.add("SET $adr $state")  // ==> send changed data over network turnout interface
        if (DEBUG) Log.d(TAG, "toggle(adr=$adr) new state=$state")
    }

    companion object {

        fun findTurnoutByAddress(address: Int): TurnoutElement? {
            for (pe in panelElements) {
                if (pe is TurnoutElement) {
                    if (pe.adr == address) {
                        return pe
                    }
                }
            }
            return null
        }
    }

}
