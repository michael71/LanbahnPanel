package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
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
    var state = STATE_UNKNOWN
    var route = ""

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

        fun getPeByAddress(address: Int): PanelElement? {
            for (pe in panelElements) {
                if (pe.adr == address) {
                    return pe
                }
            }
            return null
        }

        fun update(addr : Int, data : Int) {
            for (pe in panelElements.filter{it.adr == addr}) {
                pe.state = data
            }
        }

        fun updateAcc(addr : Int, data : Int) {
            for (pe in panelElements.filter{it.adr == addr}.filter{(it is SignalElement) or (it is TurnoutElement)}) {
                pe.state = data
            }
        }

        fun updateSensor(addr : Int, data : Int) {
            for (pe in panelElements.filter{it.adr == addr}.filter{it is SensorElement}) {
                pe.state = data
            }
        }
        /** scale all panel elements for better fit on display and for
         * possible "upside down" display (=view from other side of the
         * layout)
         * currently only called from readXMLConfigFile (i.e. NOT when
         * flipUpsideDown is changed in the prefs)
         */
        fun scaleAll() {
if (DEBUG) {
    Log.d(TAG,"before scaleALL ---------------")
    //printPES()
    printTracks()
}
            // in WriteConfig the NEW values are written !!

            var xmin = INVALID_INT
            var xmax = INVALID_INT
            var ymin = INVALID_INT
            var ymax = INVALID_INT
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
            if (DEBUG)
                Log.d(TAG, "before adding 20: xmin=" + (xmin) + " xmax=" + (xmax) + " ymin=" + (ymin)
                        + " ymax=" + (ymax) +"  ----------------" )
            // now move origin to (20,20)
            val deltaX = 20 - xmin
            val deltaY = 20 - ymin
            if (DEBUG) Log.d(TAG, "move by dx="+deltaX+ " dy="+deltaY +"  ----------------" )
            for (pe in panelElements) {
                if (!flipUpsideDown) {
                    if (pe.x  != INVALID_INT)
                        pe.x  += deltaX
                    if (pe.x2 != INVALID_INT)
                        pe.x2 += deltaX
                    if (pe.xt != INVALID_INT)
                        pe.xt += deltaX
                    if (pe.y  != INVALID_INT)
                        pe.y  += deltaY
                    if (pe.y2 != INVALID_INT)
                        pe.y2 += deltaY
                    if (pe.yt != INVALID_INT)
                        pe.yt += deltaY
                } else {
                    if (pe.x != INVALID_INT)
                        pe.x = 20 + (xmax - pe.x)
                    if (pe.x2 != INVALID_INT)
                        pe.x2 = 20 + (xmax - pe.x2)
                    if (pe.xt != INVALID_INT)
                        pe.xt = 20 + (xmax - pe.xt)
                    if (pe.y != INVALID_INT)
                        pe.y = 20 + (ymax - pe.y)
                    if (pe.y2 != INVALID_INT)
                        pe.y2 = 20 + (ymax - pe.y2)
                    if (pe.yt != INVALID_INT)
                        pe.yt = 20 + (ymax - pe.yt)
                }

            }

            if (DEBUG) {
                Log.d(TAG,"after origin move ---------------")
                //printPES()
                printTracks()
            }

            if (DEBUG)
                Log.d(TAG, "after origin move (incl Rand) xmin=" + (0) + " xmax=" + (xmax + deltaX + 20) + " ymin=" + 0
                        + " ymax=" + (ymax +deltaY +20)  +"  ----------------" )

            panelRect = Rect(0, 0, xmax + deltaX + 20, ymax +deltaY +20)

            configHasChanged = true

        }

        fun printPES() {
            var i = 0
            Log.d(TAG, "pe# " + "(x,x2)/(y,y2)")
            for (pe in panelElements) {
                Log.d(TAG, "pe#"+i+ "("+pe.x+","+pe.x2+")/("+pe.y+","+pe.y2+")")
                i++
            }
        }

        fun printTracks() {
            var i = 0
            Log.d(TAG, "pe# " + "(x,x2)/(y,y2)")
            for (pe in panelElements.filter{ !( it is ActivePanelElement)}) {
                Log.d(TAG, "pe#"+i+ "("+pe.x+","+pe.x2+")/("+pe.y+","+pe.y2+")")
                i++
            }
        }
    }

}
