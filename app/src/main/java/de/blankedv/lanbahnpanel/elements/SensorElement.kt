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


    override fun getSensitiveRect() : Rect {
        if (x2 == INVALID_INT) { // dot type sensor
            return Rect(x - RASTER / 5, y - RASTER / 7, x + RASTER / 5, y + RASTER / 7)
        } else { // line type sensor
            return Rect((x + x2) / 2 - RASTER / 5, (y + y2) / 2 - RASTER / 7, (x + x2) / 2 + RASTER / 5,
                    (y + y2) / 2 + RASTER / 7)
        }
    }

    override fun doDraw(canvas: Canvas) {

        if (x2 != INVALID_INT) {  // draw dashed line as sensor
            // read data from SX bus and set red/gray dashed line accordingly

            if (inRoute) {
                if (state == STATE_OCCUPIED) {
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintRedDash)
                } else {
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintDarkYellowDash)
                }
            } else {
                if ((state == STATE_UNKNOWN) || (state == STATE_FREE)) {
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintGrayDash)
                } else {  // occupied
                    canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintRedDash)
                }
            }
        } else {
            // draw lamp type of sensor   s_on.png etc
            // "inRoute is ignored in this case
            val h: Int
            val w: Int
            val bm: Bitmap?

            val bmName = StringBuilder("sensor")

            if ((state == STATE_FREE) or (state == STATE_UNKNOWN) ) {
                bmName.append("_off")
            } else {
                bmName.append("_on")
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
