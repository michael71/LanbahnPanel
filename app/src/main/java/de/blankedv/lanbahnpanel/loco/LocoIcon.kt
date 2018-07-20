package de.blankedv.lanbahnpanel.loco

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log


class LocoIcon(bm: Bitmap) {

    var bitmap: Bitmap? = null
        private set

    init {
        // create new loco Icon from Input Stream

        bitmap = Bitmap.createBitmap(bm)
        // should always be landscape
        if (bitmap!!.height > bitmap!!.width) {
            // Setting post rotate to 90
            val mtx = Matrix()
            mtx.postRotate(90f)
            // Rotating Bitmap
            bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, mtx, true)
        }
        scale()
        roundCorners()
    }

    private fun roundCorners() {
        val w = bitmap!!.width
        val h = bitmap!!.height
        //if (DEBUG) Log.d(TAG,"rounded corners in: "+w+" * "+h);
        assert(w == BITMAP_WIDTH)
        var y = (h - BITMAP_HEIGHT) / 2
        if (y < 0) y = 0
        val crop = Bitmap.createBitmap(bitmap!!, 0, y, BITMAP_WIDTH, BITMAP_HEIGHT)  //230x100 cropped

        // make nice round corners
        val output = Bitmap.createBitmap(crop.width, crop.height, Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, crop.width, crop.height)
        val rectF = RectF(rect)
        val roundPx = 12f

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)
        canvas.drawBitmap(crop, rect, rect, paint)
        bitmap = output
    }

    fun scale() {
        // scale bitmap
        val ratio = bitmap!!.height.toFloat() / bitmap!!.width
        var newheight = (BITMAP_WIDTH * ratio).toInt()
        if (newheight < BITMAP_HEIGHT) newheight = BITMAP_HEIGHT
        bitmap = Bitmap.createScaledBitmap(bitmap!!, BITMAP_WIDTH, newheight, true)
    }

    companion object {

        val BITMAP_HEIGHT = 100
        val BITMAP_WIDTH = 230

        fun calc_scale(w: Int, h: Int): Int {
            var scale = 1
            if (w > BITMAP_WIDTH && h > BITMAP_HEIGHT) {
                // scale should be a power of 2 !!
                scale = Math.pow(2.0, (Math.round(Math.log(BITMAP_WIDTH / w.toDouble())) / Math.log(0.5)).toInt().toDouble()).toInt()
            }
            return scale
        }
    }
}
