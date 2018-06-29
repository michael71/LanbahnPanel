package de.blankedv.lanbahnpanel


import android.graphics.Point
import android.util.Log
import de.blankedv.lanbahnpanel.model.DEBUG
import de.blankedv.lanbahnpanel.model.TAG
import de.blankedv.lanbahnpanel.model.TURNOUT_LENGTH
import de.blankedv.lanbahnpanel.model.TURNOUT_LENGTH_LONG

object LinearMath {

    /**
     * Computes the intersection between two lines. The calculated point is
     * approximate, since integers are used. If you need a more precise result,
     * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
     * license). http://www.ahristov.com (c) 2012 Michael Blank, for lines with
     * endpoints
     */
    fun trackIntersect(e: PanelElement, f: PanelElement): PanelElement? {

        // only look for crossing track elements
        if (e.type != "track" || f.type != "track")
            return null

        val x1: Int
        val y1: Int
        val x2: Int
        val y2: Int
        val x3: Int
        val y3: Int
        val x4: Int
        val y4: Int

        x1 = e.x
        y1 = e.y
        x2 = e.x2
        y2 = e.y2

        x3 = f.x
        y3 = f.y
        x4 = f.x2
        y4 = f.y2

        val d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (d == 0)
            return null

        val xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d
        val yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d
        val px = Point(xi, yi)

        // additional code by Michael Blank
        // check if within limit of lines
        var xt: Int
        var yt: Int
        var xc: Int
        var yc: Int // for turnout x-thrown, y-thrown, x-closed,
        // y-closed
        if (xi >= Math.min(x1, x2) && xi <= Math.max(x1, x2) // within

                // x-limits
                // of first
                // line
                && xi >= Math.min(x3, x4) && xi <= Math.max(x3, x4) // within

                // x-limits
                // of
                // second
                // line
                && yi >= Math.min(y1, y2) && yi <= Math.max(y1, y2) // within

                // y-limits
                // of
                // first
                // line
                && yi >= Math.min(y3, y4) && yi <= Math.max(y3, y4)) { // within
            // y-limits
            // of
            // second
            // line

            // check if point is not endpoint of both tracks => no turnout
            if (xi == e.x && yi == e.y || // startpoint of e ( e=thrown,
                    // f=closed)
                    xi == e.x2 && yi == e.y2) { // endpoint of e =>
                // delta-x immer negativ
                // start/endpoint of e
                if (xi == f.x && yi == f.y || xi == f.x2 && yi == f.y2) {
                    // AND start/endpoint of f
                    return null // => no turnout
                }
            }

            // check if point is
            // not endpoint of first track
            // and not endpoint of second track
            // => double crossover
            var doubleslip = true
            if (xi == e.x && yi == e.y || // startpoint of e ( e=thrown,
                    // f=closed)
                    xi == e.x2 && yi == e.y2) { // endpoint of e =>
                // delta-x immer negativ
                doubleslip = false
            }
            // start/endpoint of f
            if (xi == f.x && yi == f.y || xi == f.x2 && yi == f.y2) {
                doubleslip = false
            }


            if (DEBUG && doubleslip)
                Log.d(TAG, "LinMath: found doubleslip at (" + xi + "," + yi
                        + ")")

            if (!doubleslip) {
                // =========== this is a turnout !! ======================
                // find closed and thrown positions (x2>x) both for e and f !!
                // 1. check, turnout which lines' endpoint (xi,yi) belongs

                xt = xi
                xc = xt
                yt = yi
                yc = yt
                if (xi == e.x && yi == e.y) { // startpoint of e ( e=thrown,
                    // f=closed)
                    if (e.y2 - e.y != 0) { // y steigung immer = +/- 1
                        if (e.y2 > e.y) {
                            xt = xi + TURNOUT_LENGTH
                            yt = yi + TURNOUT_LENGTH
                        } else {
                            xt = xi + TURNOUT_LENGTH
                            yt = yi - TURNOUT_LENGTH
                        }
                        xc = xi + TURNOUT_LENGTH_LONG
                        yc = yi
                    } else { // steigung == 0
                        xt = xi + TURNOUT_LENGTH_LONG
                        yt = yi
                        if (f.y2 > f.y) {
                            xc = xi + TURNOUT_LENGTH
                            yc = yi + TURNOUT_LENGTH
                        } else {
                            xc = xi + TURNOUT_LENGTH
                            yc = yi - TURNOUT_LENGTH
                        }
                    }
                } else if (xi == e.x2 && yi == e.y2) { // endpoint of e =>
                    // delta-x immer
                    // negativ
                    if (e.y2 - e.y != 0) { // y steigung immer = +/- 1
                        if (e.y2 > e.y) {
                            xt = xi - TURNOUT_LENGTH
                            yt = yi - TURNOUT_LENGTH
                        } else {
                            xt = xi - TURNOUT_LENGTH
                            yt = yi + TURNOUT_LENGTH
                        }
                        xc = xi - TURNOUT_LENGTH_LONG
                        yc = yi
                    } else { // steigung == 0
                        xt = xi - TURNOUT_LENGTH_LONG
                        yt = yi
                        if (f.y2 > f.y) {
                            xc = xi - TURNOUT_LENGTH
                            yc = yi + TURNOUT_LENGTH
                        } else {
                            xc = xi - TURNOUT_LENGTH
                            yc = yi - TURNOUT_LENGTH
                        }
                    }
                } else if (xi == f.x && yi == f.y) { // startpoint of f
                    if (f.y2 - f.y != 0) { // y steigung immer = +/- 1
                        if (f.y2 > f.y) {
                            xt = xi + TURNOUT_LENGTH
                            yt = yi + TURNOUT_LENGTH
                        } else {
                            xt = xi + TURNOUT_LENGTH
                            yt = yi - TURNOUT_LENGTH
                        }
                        xc = xi + TURNOUT_LENGTH_LONG
                        yc = yi
                    } else { // steigung == 0
                        xt = xi + TURNOUT_LENGTH_LONG
                        yt = yi
                        if (e.y2 > e.y) {
                            xc = xi + TURNOUT_LENGTH
                            yc = yi + TURNOUT_LENGTH
                        } else {
                            xc = xi + TURNOUT_LENGTH
                            yc = yi - TURNOUT_LENGTH
                        }
                    }
                } else if (xi == f.x2 && yi == f.y2) { // endpoint of f
                    if (f.y2 - f.y != 0) { // y steigung immer = +/- 1
                        if (f.y2 > f.y) {
                            xt = xi - TURNOUT_LENGTH
                            yt = yi - TURNOUT_LENGTH
                        } else {
                            xt = xi - TURNOUT_LENGTH
                            yt = yi + TURNOUT_LENGTH
                        }
                        xc = xi - TURNOUT_LENGTH_LONG
                        yc = yi
                    } else { // steigung == 0
                        xt = xi - TURNOUT_LENGTH_LONG
                        yt = yi
                        if (e.y2 > e.y) {
                            xc = xi - TURNOUT_LENGTH
                            yc = yi - TURNOUT_LENGTH
                        } else {
                            xc = xi - TURNOUT_LENGTH
                            yc = yi + TURNOUT_LENGTH
                        }
                    }
                }
                val pt = PanelElement(px, Point(xc, yc),
                        Point(xt, yt))
                return TurnoutElement(pt)
            } else {
                //return new DoubleSlipElement(px, px, px);  TODO
                return null
            }
        } else {
            return null // for debugging: new PanelElement("turnok", px);
        }
    }

}
