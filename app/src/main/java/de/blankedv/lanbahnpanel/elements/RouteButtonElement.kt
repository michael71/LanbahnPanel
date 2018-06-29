package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.util.Log
import de.blankedv.lanbahnpanel.graphics.LPaints
import de.blankedv.lanbahnpanel.model.*

/**  button for selecting routes
 *
 * this buttons are local to the device and the state is NOT sent
 * via LANBAHN messages
 *
 */
class RouteButtonElement : ActivePanelElement {

    // display a route button

    var blink = System.currentTimeMillis()
    var toggleBlink = false
    private var timeSet: Long = 0

    /**
     *
     * @return true if the button is currently pressed, else false
     */
    fun isPressed(): Boolean

    {
        return (state == STATE_PRESSED)
    }

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {}

    constructor() {
        adr = INVALID_INT
        state = STATE_UNKNOWN
    }

    override fun doDraw(canvas: Canvas) {

        // read data from SX bus and paint position of turnout accordingly
        // draw a line and not a bitmap
        //canvas.drawLine(x*prescale,y*prescale, x2*prescale,y2*prescale, signalLine);
        //canvas.drawLine(x2*prescale,(y2-2.5f)*prescale, x2*prescale,(y2+2.5f)*prescale, signalLine);
        if (!enableRoutes) return

        canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 4f * prescale, LPaints.whitePaint)

        if (enableEdit || adr == INVALID_INT) {
            canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, LPaints.btn0Paint)
        } else {
            if (state == STATE_PRESSED) {
                if (System.currentTimeMillis() - blink > 500) {
                    toggleBlink = !toggleBlink
                    blink = System.currentTimeMillis()
                }
                if (toggleBlink) {
                    canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, LPaints.btn1Paint)
                } else {
                    canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, LPaints.btn0Paint)
                }
            } else if (state == STATE_NOT_PRESSED) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, LPaints.btn0Paint)
            } else if (state == STATE_UNKNOWN) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), 3f * prescale, LPaints.btn0Paint)
            }

        }

        if (drawAddresses2) doDrawAddresses(canvas)
    }

    override fun toggle() {
        if (!enableRoutes) return  // do not enable route keys if not routes are enabled

        if (adr == INVALID_INT) return  // do nothing if no address defined.

        if (System.currentTimeMillis() - lastToggle < 500) {
            Log.d(TAG, "last toggle less than 500ms ago")
            return   // do not toggle twice within 250msecs
        }

        lastToggle = System.currentTimeMillis()  // reset toggle timer

        if ((state == STATE_NOT_PRESSED) or (state == STATE_UNKNOWN)) {
            state = STATE_PRESSED
            timeSet = System.currentTimeMillis()
            checkForRoute(adr)

        } else {
            state = STATE_NOT_PRESSED
        }

        // state = STATE_UNKNOWN; // until updated via lanbahn message
        // sendQ.add("SET "+adr+" "+state);  // ==> send changed data over network
        if (DEBUG) Log.d(TAG, "toggle(adr=$adr) new state=$state time=$lastToggle")
    }

    /**
     * checks if button is being selected with a touch at point (xs, ys)
     */
    override fun isSelected(xs: Int, ys: Int): Boolean {
        // for route button check radius = RASTER/3 around center	!! slightly larger than for turnout/signal
        val minx = x - RASTER / 3
        val maxx = x + RASTER / 3
        val miny = y - RASTER / 3
        val maxy = y + RASTER / 3

        // the touchpoint should be within rectangle of panel element
        if (xs >= minx && xs <= maxx && ys >= miny && ys <= maxy) {
            if (DEBUG)
                Log.d(TAG, "selected adr=$adr type=$type  ($x,$y)")
            return true
        } else {
            // if (DEBUG) Log.d(TAG, "No Route key selection");
            return false
        }
    }

    fun reset() {
        state = STATE_NOT_PRESSED
    }

    companion object {


        fun findRouteButtonByAddress(address: Int): RouteButtonElement? {
            for (pe in panelElements) {
                if (pe is RouteButtonElement) {
                    if (pe.adr == address) {
                        return pe
                    }
                }
            }

            return null

        }

        fun checkForRoute(adrSecondBtn: Int): Boolean {


            if (DEBUG) Log.d(TAG, "checkForRoute called, adrSecondBtn=$adrSecondBtn")
            // check if a route needs to be cleared first
            if (clearRouteButtonActive) {
                if (DEBUG) Log.d(TAG, "clearRouteButtonActive:true")
                // find route with adrSecondBtn and clear it
                for (rt in routes) {
                    if (DEBUG) Log.d(TAG, "checking route id=" + rt.id)
                    if (rt.isActive && (rt.btn1 == adrSecondBtn || rt.btn2 == adrSecondBtn)) {
                        if (DEBUG) Log.d(TAG, "found route matching to btn. clearing route=" + rt.id)
                        // we found a route with this button, new clear it
                        // now set
                        rt.clear()
                    } else {
                        if (DEBUG) Log.d(TAG, "route not active route for btn=$adrSecondBtn")
                    }
                }
                clearRouteButtonActive = false
                findRouteButtonByAddress(adrSecondBtn)!!.reset()  // clear the button also
                return true
            }

            var nPressed = 0
            var adrFirstBtn = 0

            if (DEBUG) Log.d(TAG, "checking, if a route can be activated")
            // now check if a route can be activated
            for (pe in panelElements) {
                if (pe is RouteButtonElement) {
                    if (pe.isPressed()) {
                        nPressed++
                        if (pe.adr != adrSecondBtn) {
                            // if this is not the "checking" button, then it must be the first button
                            adrFirstBtn = pe.adr
                        }
                    }
                }
            }
            if (DEBUG) Log.d(TAG, "btns pressed total=$nPressed")
            if (nPressed == 2) {
                // this could be a route, 2 buttons are active
                // iterate over all possible routes
                // we must know which button was pressed first!!
                if (DEBUG) Log.d(TAG, "checking for a route from btn-$adrFirstBtn turnout btn-$adrSecondBtn")
                var routeFound = false
                for (rt in routes) {
                    if (DEBUG) Log.d(TAG, "checking route id=" + rt.id)
                    if (rt.btn1 == adrFirstBtn && rt.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons,
                        // now set
                        routeFound = true
                        if (DEBUG) Log.d(TAG, "found the route with id=" + rt.id)
                        // reset buttons
                        findRouteButtonByAddress(adrFirstBtn)!!.reset()
                        findRouteButtonByAddress(adrSecondBtn)!!.reset()

                        // set the route (i.e. sensors and turnouts)
                        rt.set()
                        break  // no need to search further
                    }
                }
                for (cr in compRoutes) {
                    if (DEBUG) Log.d(TAG, "checking composite route id=" + cr.id)
                    if (cr.btn1 == adrFirstBtn && cr.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons,
                        // now set
                        routeFound = true
                        if (DEBUG) Log.d(TAG, "found the composite route with id=" + cr.id)
                        // reset buttons
                        findRouteButtonByAddress(adrFirstBtn)!!.reset()
                        findRouteButtonByAddress(adrSecondBtn)!!.reset()

                        // set the route (i.e. sensors and turnouts)
                        cr.set()
                        break  // no need to search further
                    }
                }
                if (!routeFound) {
                    // TODO toast("keine passende Fahrstrasse.")
                    findRouteButtonByAddress(adrFirstBtn)!!.reset()  // clear the button also
                    findRouteButtonByAddress(adrSecondBtn)!!.reset()  // clear the button also
                }

            } else if (nPressed > 2) {
                if (DEBUG) Log.d(TAG, "too many routeButtons pressed, clearing all")
                // makes no sense, deselect all
                for (pe in panelElements) {
                    if (pe is RouteButtonElement) {
                        pe.reset()
                    }
                }
                // TODO toast("zu viele Buttons.")
            }


            return true
        }

        fun autoReset() {
            for (pe in panelElements) {
                if (pe is RouteButtonElement) {
                    if ((pe.state == STATE_PRESSED) && (System.currentTimeMillis() - pe.timeSet > 20 * 1000L)) {
                        pe.state = STATE_NOT_PRESSED
                    }
                }
            }

        }
    }
}
