package de.blankedv.lanbahnpanel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*
import de.blankedv.lanbahnpanel.LinePaints.*


class SensorElement : ActivePanelElement {

    constructor(x: Int, y: Int, name: String, adr: Int) : super(x, y, name, adr) {

    }

    constructor() : super() {}

    override fun doDraw(canvas: Canvas) {

        if (x2 != INVALID_INT) {  // draw dashed line as sensor
            // read data from SX bus and set red/gray dashed line accordingly

            if (state == ActivePanelElement.STATE_FREE) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaintGrayDash())

            } else if (state == ActivePanelElement.STATE_OCCUPIED) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaintRedDash())


            } else if (state == ActivePanelElement.STATE_INROUTE) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaintDarkYellowDash())


            } else if (state == ActivePanelElement.STATE_UNKNOWN) {

                canvas.drawLine((x * prescale).toFloat(), (y * prescale).toFloat(), (x2 * prescale).toFloat(), (y2 * prescale).toFloat(), INSTANCE.getLinePaintGrayDash())
            }
        } else {
            // draw lamp type of sensor   s_on.png etc

            val h: Int
            val w: Int
            val bm: Bitmap?

            val bmName = StringBuilder("sensor")

            if (state == ActivePanelElement.STATE_FREE || state == ActivePanelElement.STATE_UNKNOWN) {
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
