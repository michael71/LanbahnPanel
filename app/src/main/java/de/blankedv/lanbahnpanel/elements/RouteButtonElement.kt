package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.model.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

/**  button for selecting routes
 *
 * this buttons are local to the device and the state is NOT sent
 * via LANBAHN messages
 *
 */
class RouteButtonElement : ActivePanelElement {

    var blink = System.currentTimeMillis()
    var toggleBlink = false

    private val radius = 8f * prescale

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr)

    constructor() {
        adr = INVALID_INT
        state = BTN_NOT_PRESSED
    }

    /** draw a route button on canvas (vector draw)
     *
     * @param canvas
     *
     */
    override fun doDraw(canvas: Canvas) {

        if (!prefs.getBoolean(KEY_ROUTING, false)) return  // do not display route keys if not routes are enabled

        if (adr == INVALID_INT) {
            canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, LPaints.btn0Paint)
        } else {
            if (state == BTN_PRESSED) {
                if (System.currentTimeMillis() - blink > 500) {
                    toggleBlink = !toggleBlink
                    blink = System.currentTimeMillis()
                }
                if (toggleBlink) {
                    canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, LPaints.btn1Paint)
                } else {
                    canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, LPaints.btn0Paint)
                }
            } else if (state == BTN_NOT_PRESSED) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, LPaints.btn0Paint)
            } else if (state == BTN_MARKED) {
                canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radius, LPaints.btnMarkPaint)
            }

            if (prefs.getBoolean(KEY_DRAW_ADR2, false)) doDrawAddresses(canvas)
        }
    }

    override fun toggle() {

        if (!prefs.getBoolean(KEY_ROUTING, false)) return  // do not enable route keys if not routes are enabled

        if (adr == INVALID_INT) return  // do nothing if no address defined.

        if (System.currentTimeMillis() - lastToggle < 500) {
            Log.d(TAG, "last toggle less than 500ms ago")
            return   // do not toggle twice within 250msecs
        }

        lastToggle = System.currentTimeMillis()  // reset toggle timer

        when (state) {
            BTN_NOT_PRESSED -> {  // start with a possible new route
                if (countMarked() == 0) {
                    if (countPressed() == 0) {
                        var clearing = checkForClear()
                        if (clearing) {
                            resetAll()
                            appContext!!.longToast("Fahrstraße gelöscht!")
                        } else {
                            state = BTN_PRESSED
                            markPossibleRouteEndButtons(adr)
                        }
                    } else {
                        appContext?.toast("keine passende Fahrstrasse.")
                        Log.d(TAG, "ignoring rt-button=$adr, because other button already in state PRESSED")
                    }
                } else {
                    // do nothing when 1 button is PRESSED and others are MARKED
                }
            }
            BTN_PRESSED -> {
                state = BTN_NOT_PRESSED
                // clear current route or comproute
                Log.d(TAG, "rt-button=$adr pressed, but no route yet -> clearing this button")

            }
            BTN_MARKED -> {
                state = BTN_PRESSED
                val result = findRouteAndRequest(adr)  // check for route and set
                if (result) {
                    Log.d(TAG, "route found")
                } else {
                    Log.e(TAG, "marked rt-btn=$adr, but no route found !")
                }
                RouteButtonElement.resetAll()
            }
            else -> {
                Log.d(TAG, "rt-btn=$adr with state=$state : SHOULD NOT HAPPEN")
            }
        }

        if (DEBUG) Log.d(TAG, "toggle rt.btn=$adr new state=$state time=$lastToggle")
    }

    /**
     * checks if button is being selected with a touch at point (xs, ys)
     */
    override fun isSelected(xs: Int, ys: Int): Boolean {
        // for route button check radius = RASTER/3 around center	!! slightly larger than for turnout/signal
        val rad = radius * 1.3  // was: RASTER / 3 = 20 * prescale / 3
        val minx = x - rad
        val maxx = x + rad
        val miny = y - rad
        val maxy = y + rad

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
        state = BTN_NOT_PRESSED
        //Log.d(TAG, "btn#$adr not-pressed")
    }

    fun mark() {
        state = BTN_MARKED
        //Log.d(TAG, "btn#$adr marked")
    }


    /** check if this route button is the first route button of an active route => then clear this route */
    private fun checkForClear(): Boolean {

        var clearing = false
        for (rt in routes) {
            if (rt.isActive && rt.btn1 == adr) {
                if (DEBUG) Log.d(TAG, "found route matching to rt-btn, requesting to clear route=" + rt.adr)
                // we found a route with this button, now clear it
                rt.clearRequest()
                clearing = true
            }
        }

        for (crt in compRoutes) {
            if (crt.isActive && crt.btn1 == adr) {
                if (DEBUG) Log.d(TAG, "found COMP route matching to btn. requesting to clear COMP route=" + crt.adr)
                // we found a route with this button, now clear it
                // now set
                crt.clearRequest()
                clearing = true
            }
        }
        return clearing

    }


    companion object {

        /** calc how many buttons are currently in the "BTN_PRESSED" state */
        fun countPressed(): Int {
            var nPressed = 0
            for (pe in panelElements) {
                if ((pe is RouteButtonElement) and (pe.state == BTN_PRESSED)) {
                    nPressed++
                }
            }
            return nPressed
        }

        /** calc how many buttons are currently in the "BTN_MARKED" state */
        fun countMarked(): Int {
            var nMarked = 0
            for (pe in panelElements) {
                if ((pe is RouteButtonElement) and (pe.state == BTN_MARKED)) {
                    nMarked++
                }
            }
            return nMarked
        }

        fun resetAll() {
            for (pe in panelElements) {
                if (pe is RouteButtonElement) {
                    pe.reset()
                }
            }
        }

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

        fun markPossibleRouteEndButtons(adr1: Int) {
            for (rt in routes) {
                if (rt.btn1 == adr1) {
                    findRouteButtonByAddress(rt.btn2)!!.mark()
                }
            }
            for (crt in compRoutes) {
                if (crt.btn1 == adr1) {
                    findRouteButtonByAddress(crt.btn2)!!.mark()
                }
            }
        }

        fun findRouteAndRequest(adrSecondBtn: Int): Boolean {
            if (DEBUG) Log.d(TAG, "findRouteAndRequest called, adrSecondBtn=$adrSecondBtn")
            // check if a route needs to be cleared first

            var adrFirstBtn = 0
            if (DEBUG) Log.d(TAG, "checking, if a route can be activated")
            // now check if a route can be activated
            for (pe in panelElements) {
                if ((pe is RouteButtonElement) && (pe.state == BTN_PRESSED) && (pe.adr != adrSecondBtn)) {
                    // if this is not the "checking" button, then it must be the first button
                    adrFirstBtn = pe.adr  // we must know which other button was pressed (first)
                }
            }
            if (DEBUG) Log.d(TAG, "btns pressed total=${countPressed()}")
            if (countPressed() == 2) {
                // this could be a route, 2 buttons are pressed
                // iterate over all possible routes
                if (DEBUG) Log.d(TAG, "checking for a route from btn-$adrFirstBtn turnout btn-$adrSecondBtn")
                for (rt in routes) {
                    if (DEBUG) Log.d(TAG, "checking route adr=" + rt.adr)
                    if (rt.btn1 == adrFirstBtn && rt.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons, request this route
                        if (DEBUG) Log.d(TAG, "found the route with adr=" + rt.adr)
                        // set the route (i.e. sensors and turnouts)
                        if (prefs.getBoolean(KEY_ROUTING, true)) {
                            rt.request();
                        }
                        return true  // no need to search further
                    }
                }
                for (crt in compRoutes) {
                    if (DEBUG) Log.d(TAG, "checking composite route adr=" + crt.adr)
                    if (crt.btn1 == adrFirstBtn && crt.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons, request this CompRoute
                        if (DEBUG) Log.d(TAG, "found the composite route with adr=" + crt.adr)
                        // set the route (i.e. sensors and turnouts)
                        if (prefs.getBoolean(KEY_ROUTING, true)) {
                            crt.request();
                        }
                        return true // no need to search further
                    }
                }
            }
            if (DEBUG) Log.d(TAG, "not exactly 2 routeButtons pressed, clearing all")
            RouteButtonElement.resetAll()
            appContext?.toast("nicht genau 2 Route-Buttons gedrückt.")
            return false
        }
    }
}
