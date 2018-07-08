package de.blankedv.lanbahnpanel.elements

import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import de.blankedv.lanbahnpanel.util.LPaints
import de.blankedv.lanbahnpanel.util.Utils
import de.blankedv.lanbahnpanel.model.*

/**
 * all active panel elements, like turnouts, signals, trackindicators (=sensors)
 * are derviced from this class. These elements have a "state" which is exactly
 * the same number as the "data" of the lanbahn messages "S 810 2" => set state
 * of panel element with address=810 to state=2
 *
 * a panel element has only 1 address (=> double slips are 2 panel elements)
 *
 * TODO kotlin review
 *
 * @author mblank
 */
abstract class ActivePanelElement : PanelElement {

    var lastToggle = 0L
    var lastUpdateTime = 0L

    /**
     *
     * @return true, if the state of this element has not been communicated
     * during in the last 20 seconds
     */
    val isExpired: Boolean
        get() = System.currentTimeMillis() - lastUpdateTime > 20 * 1000

    constructor() : super(0, 0) {}

    /**
     * constructor for an ACTIVE panel element with 1 address default state is
     * "CLOSED" (="RED")
     *
     * @param x
     * @param y
     * @param name
     * @param adr
     */
    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y) {
        this.state = STATE_UNKNOWN
        this.adr = adr
        lastUpdateTime = System.currentTimeMillis()
    }

    open fun getSensitiveRect() : Rect {
        val minx = Utils.min(x, xt, x2)
        val maxx = Utils.max(x, xt, x2)
        val miny = Utils.min(y, yt, y2)
        val maxy = Utils.max(y, yt, y2)
        return Rect(minx, miny, maxx, maxy)
    }

    override fun hasAdrX(address: Int): Boolean {
        return (adr == address)
    }

    override fun updateData(data: Int) {
        if (data == INVALID_INT) {
            state = STATE_UNKNOWN
        } else {
            state = data
        }
        lastUpdateTime = System.currentTimeMillis()
    }

    override fun isSelected(xs: Int, ys: Int): Boolean {
        // check only for active elements
        // search x for range in x..(x+/-w)
        // search y for range in y..(y+/-h)

        val rect = getSensitiveRect()

        // the touchpoint should be within rectangle of panel element
        // similar  rect.contains() methods, BUT the lines of the rect are
        // both included in the allowed area
        if (xs >= rect.left && xs <= rect.right && ys >= rect.top
                && ys <= rect.bottom) {
            if (DEBUG)
                Log.d(TAG, "selected adr=" + adr + " " + this.type + "  (" + x + ","
                        + y + ") in rect=" + rect.toString())
            return true
        } else {
            // if (DEBUG) Log.d(TAG, "NO sel.  adr=" + adr + " " + this.getType() +
            // " not in rect="+ rect.toString());
            return false
        }
    }

    fun prescaleRect(r: Rect): Rect {
        r.top = r.top * prescale
        r.bottom = r.bottom * prescale
        r.left = r.left * prescale
        r.right = r.right * prescale
        return r
    }

    protected fun doDrawAddresses(canvas: Canvas) {

        val bounds = Rect()
        val txt: String
        if (adr == INVALID_INT) {
            txt = "???"
        } else {
            txt = "" + adr
        }
        LPaints.addressPaint.getTextBounds(txt, 0, txt.length, bounds)
        val text_height = bounds.height()
        val text_width = bounds.width()

        val pre = prescaleRect(getSensitiveRect())
        canvas.drawRect(pre, LPaints.addressBGPaint) // dark rectangle
        canvas.drawText(txt, (pre.left + text_width / 8).toFloat(), (pre.top + 3 * text_height / 2).toFloat(), LPaints.addressPaint) // the
        // numbers

    }

    fun setExpired() {
        lastUpdateTime = System.currentTimeMillis() - 21 * 1000
    }

    companion object {

        // these constants are defined just for easier understanding of the
        // methods of the classes derived from this class


    }

}
