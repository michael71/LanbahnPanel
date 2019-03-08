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

    // display a route button

    var blink = System.currentTimeMillis()
    var toggleBlink = false
    //private var timeSet: Long = 0

    private val radius = 8f * prescale
    // private val radiusL = 5.5f * prescale


    /**
     *
     * @return true if the button is currently pressed, else false
     */
    fun isPressed(): Boolean {
        return (state == BTN_PRESSED)
    }

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr)

    constructor() {
        adr = INVALID_INT
        state = BTN_NOT_PRESSED
    }


    /** draw route buttons  (vector draw
     *
     * @param canvas
     *
     * */
    override fun doDraw(canvas: Canvas) {

        if (!prefs.getBoolean(KEY_ROUTING, false)) return

        //canvas.drawCircle((x * prescale).toFloat(), (y * prescale).toFloat(), radiusL, LPaints.whitePaint)

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

        if (state == BTN_NOT_PRESSED) {
            //timeSet = System.currentTimeMillis()
            // check first if this is the first button of a currently active route
            // if this is the case then clear this route
            var clearing = false
            if (DEBUG) Log.d(TAG, "checking for route and compRoute clear, btn#$adr")
            for (rt in routes) {
                if (rt.isActive && rt.btn1 == adr) {
                    if (DEBUG) Log.d(TAG, "found route matching to btn. requesting to clear route=" + rt.adr)
                    // we found a route with this button, now clear it
                    // now set
                    rt.clearRequest()
                    clearing = true
                    state = BTN_NOT_PRESSED   // reset btn
                }
            }
            for (crt in compRoutes) {
                if (crt.isActive && crt.btn1 == adr) {
                    if (DEBUG) Log.d(TAG, "found COMP route matching to btn. requesting to clear COMP route=" + crt.adr)
                    // we found a route with this button, now clear it
                    // now set
                    crt.clearRequest()
                    clearing = true
                    state = BTN_NOT_PRESSED   // reset btn
                }
            }
            if (!clearing) {
                state = BTN_PRESSED
                markPossibleRouteEndButtons(adr)


            } else {
                appContext!!.longToast("Fahrstraße gelöscht!")
            }

        } else if (state == BTN_MARKED) {
            state = BTN_PRESSED
            checkForRoute(adr)
        }  else {
            state = BTN_NOT_PRESSED
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
        Log.d(TAG,"btn#$adr not-pressed")
    }

    fun mark() {
        state = BTN_MARKED
        Log.d(TAG,"btn#$adr marked")
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

        fun checkForRoute(adrSecondBtn: Int): Boolean {

            if (DEBUG) Log.d(TAG, "checkForRoute called, adrSecondBtn=$adrSecondBtn")
            // check if a route needs to be cleared first

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
                    if (DEBUG) Log.d(TAG, "checking route adr=" + rt.adr)
                    if (rt.btn1 == adrFirstBtn && rt.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons,
                        // now set
                        routeFound = true
                        if (DEBUG) Log.d(TAG, "found the route with adr=" + rt.adr)
                        // reset buttons
                        findRouteButtonByAddress(adrFirstBtn)!!.reset()
                        findRouteButtonByAddress(adrSecondBtn)!!.reset()

                        // set the route (i.e. sensors and turnouts)
                        if (prefs.getBoolean(KEY_ROUTING, true)) {
                            rt.request();
                        }
                        break  // no need to search further
                    }
                }
                for (cr in compRoutes) {
                    if (DEBUG) Log.d(TAG, "checking composite route adr=" + cr.adr)
                    if (cr.btn1 == adrFirstBtn && cr.btn2 == adrSecondBtn) {
                        // we found a route connecting these buttons,
                        // now set
                        routeFound = true
                        if (DEBUG) Log.d(TAG, "found the composite route with adr=" + cr.adr)
                        // reset buttons
                        findRouteButtonByAddress(adrFirstBtn)!!.reset()
                        findRouteButtonByAddress(adrSecondBtn)!!.reset()

                        // set the route (i.e. sensors and turnouts)
                        if (prefs.getBoolean(KEY_ROUTING, true)) {
                            cr.request();
                        }
                        break  // no need to search further
                    }
                }
                if (!routeFound) {
                    appContext?.toast("keine passende Fahrstrasse.")
                    findRouteButtonByAddress(adrFirstBtn)!!.reset()  // clear the button also
                    findRouteButtonByAddress(adrSecondBtn)!!.reset()  // clear the button also
                } else {
                    // route was found, clear all route Buttons
                    Log.d(TAG,"reset all route buttons")
                    for (pe in panelElements) {
                        if (pe is RouteButtonElement) {
                            pe.reset()
                        }
                    }
                }

            } else if (nPressed > 2) {
                if (DEBUG) Log.d(TAG, "too many routeButtons pressed, clearing all")
                // makes no sense, deselect all
                for (pe in panelElements) {
                    if (pe is RouteButtonElement) {
                        pe.reset()
                    }
                }
                appContext?.toast("zu viele Route-Buttons gedrückt.")
            }


            return true
        }


    }
}
