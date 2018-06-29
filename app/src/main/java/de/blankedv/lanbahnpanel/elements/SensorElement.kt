package de.blankedv.lanbahnpanel.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import de.blankedv.lanbahnpanel.graphics.AndroBitmaps.bitmaps
import de.blankedv.lanbahnpanel.graphics.LPaints.linePaintDarkYellowDash
import de.blankedv.lanbahnpanel.graphics.LPaints.linePaintGrayDash
import de.blankedv.lanbahnpanel.graphics.LPaints.linePaintRedDash
import de.blankedv.lanbahnpanel.model.*

class SensorElement : ActivePanelElement {

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {

    }

    constructor() : super() {}

    override fun doDraw(canvas: Canvas) {

        if (x2 != INVALID_INT) {  // draw dashed line as sensor
            // read data from SX bus and set red/gray dashed line accordingly

            if (state == STATE_FREE) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintGrayDash)

            } else if (state == STATE_OCCUPIED) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintRedDash)


            } else if (state == STATE_INROUTE) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintDarkYellowDash)


            } else if (state == STATE_UNKNOWN) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), linePaintGrayDash)
            }
        } else {
            // draw lamp type of sensor   s_on.png etc

            val h: Int
            val w: Int
            val bm: Bitmap?

            val bmName = StringBuilder("sensor")

            if (state == STATE_FREE || state == STATE_UNKNOWN) {
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
        if (drawAddresses2)
            doDrawAddresses(canvas)
    }


}
