package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.preference.PreferenceManager
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.model.*

/**
 * generic panel element - this can be a passive (never changing)
 * panel element or an active (lanbahn status dependent)
 * element.
 *
 * @author mblank
 */
open class PanelElement {

    var name = ""
    var x: Int = 0 // starting point
    var y: Int = 0
    var x2 = INVALID_INT // endpoint - x2 always >x
    var y2 = INVALID_INT
    var xt = INVALID_INT // "thrown" position for turnout
    var yt = INVALID_INT
    var adr = INVALID_INT
    var adr2 = INVALID_INT   // needed for DCC sensors and signals
    var state = STATE_UNKNOWN
    var nbit = 1  // for multi-aspect signals, nbit=2 => 4 possible values 0,1,2,3
    var inRoute = false

    var route = ""
    var invert = DISP_STANDARD     // not inverted

    /** get the type-name which is used in the XML panel definition file
     *
     * @return
     */
    val type: String
        get() {
            val className = this.javaClass.simpleName
            when (className) {
                "SignalElement" -> return "signal"
                "PanelElement" -> return "track"
                "TurnoutElement" -> return "turnout"
                "SensorElement" -> return "sensor"
                "ActivePanelElement" -> return "other"
                "RouteButtonElement" -> return "routebutton"
                "DoubleSlipElement" -> return "doubleslip"
                else -> {
                    Log.d(TAG, "could not determine type of panel element")
                    return "error"
                }
            }

        }

    constructor()

    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
        name = ""
    }


    constructor(poi: Point, closed: Point, thrown: Point) {
        this.x = poi.x
        this.y = poi.y
        this.x2 = closed.x
        this.y2 = closed.y
        this.xt = thrown.x
        this.yt = thrown.y
        name = ""
    }

    open fun doDraw(canvas: Canvas) {

        if (y == y2) { // horizontal line
            // draw a line and not a bitmap
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), LPaints.linePaint)
        } else { // diagonal, draw with round stroke
            canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), LPaints.linePaint2)
        }

    }

    open fun isSelected(lastTouchX: Int, lastTouchY: Int): Boolean {
        return false
    }

    open fun setExpired() {
        // do nothing for non changing element
    }

    open fun isExpired() : Boolean {
        return false // non changing element
    }

    open fun toggle() {
        // do nothing for non changing element
    }

    open fun hasAdrX(address: Int): Boolean {
        return false
    }

    open fun updateData(data: Int) {
        // do nothing for non changing element
    }

    companion object {

        var flipState : Boolean = true

        /* NOT WORKING if there are 2 Panel Elements with identical address !!!
        i.e. works for signals, but certainly not for turnouts */
        fun getFirstPeByAddress(address: Int): PanelElement? {
            for (pe in panelElements) {
                if (pe.adr == address) {
                    return pe
                }
            }
            return null
        }

        fun getAllPesByAddress(address: Int) : List<PanelElement> {
            return panelElements.filter { it.adr == address}
        }

        fun update(addr: Int, data: Int) {
            for (pe in panelElements.filter { it.adr == addr }) {
                pe.state = data
            }
        }

        fun updateAcc(addr: Int, data: Int) {
            for (pe in panelElements.filter { it.adr == addr }.filter { (it is SignalElement) or (it is TurnoutElement) }) {
                pe.state = data
            }
        }

        fun updateSensor(addr: Int, data: Int) {
            for (pe in panelElements.filter { it.adr == addr }.filter { it is SensorElement }) {
                pe.state = data
            }
        }

        /** relocate panel origin for better fit on display and for possible "upside down"
         * display (=view from other side of the layout -
         *
         */
        fun relocatePanelOrigin() {
            // in WriteConfig the NEW values are written !!
            if (DEBUG) Log.d(TAG,"relocatePanelOrigin")

            val rec = getMinMaxXY()
            if (DEBUG) {
                Log.d(TAG, "relocatePanelOrigin: before adding 10: xmin=" + (rec.left) + " xmax="
                        + (rec.right) + " ymin=" + (rec.top) + " ymax=" + (rec.bottom) + "  ----------------")
            }
            // now move origin to (10,10)
            val deltaX = 10 - rec.left
            val deltaY = 10 - rec.top
            if (DEBUG) {
                Log.d(TAG, "relocatePanelOrigin: move by dx=" + deltaX + " dy=" + deltaY + "  ----------------")
            }
            for (pe in panelElements) {
                    if (pe.x != INVALID_INT)
                        pe.x += deltaX
                    if (pe.x2 != INVALID_INT)
                        pe.x2 += deltaX
                    if (pe.xt != INVALID_INT)
                        pe.xt += deltaX
                    if (pe.y != INVALID_INT)
                        pe.y += deltaY
                    if (pe.y2 != INVALID_INT)
                        pe.y2 += deltaY
                    if (pe.yt != INVALID_INT)
                        pe.yt += deltaY
            }
            panelRect = Rect(0, 0, (rec.right + deltaX + 10) * prescale, (rec.bottom + deltaY + 10) * prescale)

            if (DEBUG) {
                Log.d(TAG, "relocatePanelOrigin: after origin move mult by prescale (incl border) xmin=" + panelRect.left + " xmax="
                        + panelRect.right + " ymin=" + panelRect.top
                        + " ymax=" + panelRect.bottom + "  ----------------")
            }


            configHasChanged = true

        }

        /** flip current panel elements upside down, current state (flipped or
         * original) is stored in flipState variable
         *
         */
        fun flipPanel() {
            // in WriteConfig the NEW values are written !!
            if (DEBUG) Log.d(TAG,"flipPanel (toggle)")
            flipState = !flipState

            val rec = getMinMaxXY()

            for (pe in panelElements) {
                if (pe.x != INVALID_INT)
                    pe.x = 10 + (rec.right - pe.x)
                if (pe.x2 != INVALID_INT)
                    pe.x2 = 10 + (rec.right - pe.x2)
                if (pe.xt != INVALID_INT)
                    pe.xt = 10 + (rec.right - pe.xt)
                if (pe.y != INVALID_INT)
                    pe.y = 10 + (rec.bottom - pe.y)
                if (pe.y2 != INVALID_INT)
                    pe.y2 = 10 + (rec.bottom - pe.y2)
                if (pe.yt != INVALID_INT)
                    pe.yt = 10 + (rec.bottom - pe.yt)
            }
            panelRect = Rect(0, 0, (rec.right) * prescale, (rec.bottom) * prescale)

            if (DEBUG) {
                Log.d(TAG, "flipPanel: new Rect (should be equal old Rect!) " + panelRect.left + " xmax="
                        + panelRect.right + " ymin=" + panelRect.top
                        + " ymax=" + panelRect.bottom + "  ----------------")
            }

            configHasChanged = true

        }
        fun printPES() {
            var i = 0
            Log.d(TAG, "pe# " + "(x,x2)/(y,y2)")
            for (pe in panelElements) {
                Log.d(TAG, "pe#" + i + "(" + pe.x + "," + pe.x2 + ")/(" + pe.y + "," + pe.y2 + ")")
                i++
            }
        }

        fun printTracks() {
            var i = 0
            Log.d(TAG, "pe# " + "(x,x2)/(y,y2)")
            for (pe in panelElements.filter { !(it is ActivePanelElement) }) {
                Log.d(TAG, "pe#" + i + "(" + pe.x + "," + pe.x2 + ")/(" + pe.y + "," + pe.y2 + ")")
                i++
            }
        }

        private fun getMinMaxXY() : Rect {
            var xmin = INVALID_INT     // = left
            var xmax = INVALID_INT     // = right
            var ymin = INVALID_INT     // = top
            var ymax = INVALID_INT     // = bottom
            var first = true
            for (pe in panelElements) {
                if (first) {
                    xmax = pe.x
                    xmin = xmax
                    ymax = pe.y
                    ymin = ymax
                    first = false
                }

                if (pe.x != INVALID_INT && pe.x < xmin)
                    xmin = pe.x
                if (pe.x != INVALID_INT && pe.x > xmax)
                    xmax = pe.x
                if (pe.x2 != INVALID_INT && pe.x2 < xmin)
                    xmin = pe.x2
                if (pe.x2 != INVALID_INT && pe.x2 > xmax)
                    xmax = pe.x2
                if (pe.xt != INVALID_INT && pe.xt < xmin)
                    xmin = pe.xt
                if (pe.xt != INVALID_INT && pe.xt > xmax)
                    xmax = pe.xt

                if (pe.y != INVALID_INT && pe.y < ymin)
                    ymin = pe.y
                if (pe.y != INVALID_INT && pe.y > ymax)
                    ymax = pe.y
                if (pe.y2 != INVALID_INT && pe.y2 < ymin)
                    ymin = pe.y2
                if (pe.y2 != INVALID_INT && pe.y2 > ymax)
                    ymax = pe.y2
                if (pe.yt != INVALID_INT && pe.yt < ymin)
                    ymin = pe.yt
                if (pe.yt != INVALID_INT && pe.yt > ymax)
                    ymax = pe.yt

            }

            if (DEBUG) {
                Log.d(TAG, "getMinMaxXY: xmin=" + xmin + " xmax="
                        + xmax + " ymin=" + ymin
                        + " ymax=" + ymax )
            }
            return Rect(xmin,ymin,xmax,ymax)

        }
    }

}
