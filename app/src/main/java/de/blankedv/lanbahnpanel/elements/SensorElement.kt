package de.blankedv.lanbahnpanel.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import de.blankedv.lanbahnpanel.util.LanbahnBitmaps.bitmaps
import de.blankedv.lanbahnpanel.util.LPaints.linePaintDarkYellowDash
import de.blankedv.lanbahnpanel.util.LPaints.linePaintGrayDash
import de.blankedv.lanbahnpanel.util.LPaints.linePaintRedDash
import de.blankedv.lanbahnpanel.model.*

class SensorElement : ActivePanelElement {

    constructor() : super() {}

    // check bit1 for "INROUTE"
    fun isInRoute() : Boolean {
        if ((state and 0x02) != 0) {
            return true
        } else {
            return false
        }
    }

    // check bit0 for "OCCUPIED"
    fun isOccupied() : Boolean {
        if ((state and 0x01) != 0) {
            return true
        } else {
            return false
        }
    }

    override fun getSensitiveRect(): Rect {
        if (x2 == INVALID_INT) { // dot type sensor
            return Rect(x - RASTER / 5, y - RASTER / 7, x + RASTER / 5, y + RASTER / 7)
        } else { // line type sensor
            return Rect((x + x2) / 2 - RASTER / 5, (y + y2) / 2 - RASTER / 7, (x + x2) / 2 + RASTER / 5,
                    (y + y2) / 2 + RASTER / 7)
        }
    }

    override fun doDraw(canvas: Canvas) {

        if (x2 != INVALID_INT) {  // draw dashed line as sensor
            // read data from central station and set red/gray dashed line accordingly
            if (isOccupied()) {
                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintRedDash)
            } else {
                if (isInRoute()) {
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintDarkYellowDash)
                } else {
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintGrayDash)
                }
            }
        } else {
            // draw lamp type of sensor   s_on.png etc
            // "inRoute is ignored in this case
            val h: Int
            val w: Int
            val bm: Bitmap?

            val bmName = StringBuilder("sensor")

            if (isOccupied()) {
                bmName.append("_on")
            } else {
                bmName.append("_off")
            }

            bm = bitmaps[bmName.toString()]
            if (bm == null) {
                Log.e(TAG,
                        "error, bitmap not found with name=" + bmName.toString())
            } else {
                h = bm.height / 2
                w = bm.width / 2
                canvas.drawBitmap(bm, (x * prescale - w).toFloat(), (y * prescale - h).toFloat(), null) // center
                // bitmap
            }
        }
        if (prefs.getBoolean(KEY_DRAW_ADR2, false))
            doDrawAddresses(canvas)
    }


}
