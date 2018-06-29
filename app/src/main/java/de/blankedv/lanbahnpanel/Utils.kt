package de.blankedv.lanbahnpanel

import java.text.SimpleDateFormat
import java.util.Date

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import de.blankedv.lanbahnpanel.model.INVALID_INT
import de.blankedv.lanbahnpanel.model.TAG

/** Utils - utility functions
 *
 * @author Michael Blank
 */
object Utils {

    //df.setTimeZone(TimeZone.getTimeZone("PST"));
    val dateTime: String
        get() {
            val df = SimpleDateFormat("yyyyMMdd_hhmmss")
            return df.format(Date())
        }

    /** scale a bitmap both in x and y direction
     *
     * @author Michael Blank
     * @param bm bitmap turnout resize
     * @param scale scaling factor (both in x and y direction)
     * @return re-scaled Bitmap
     */

    fun getResizedBitmap(bm: Bitmap, scale: Float): Bitmap {

        val width = bm.width

        val height = bm.height


        // create a matrix for the manipulation

        val matrix = Matrix()
        // resize the bit map

        matrix.postScale(scale, scale)
        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }
    // svn test comment

    /**
     * calculate minimum of 3 integers, the first one is always a
     * valid number, the other can if INVALID_INT (=>not taken into
     * account) or valid integers, then they are evaluated
     */
    fun min(x: Int, xt: Int, x2: Int): Int {
        var m = x  // is always defined.
        if (x == INVALID_INT) Log.e(TAG, "Utils.min: x is undefined.")
        if (xt != INVALID_INT && xt < m) m = xt
        if (x2 != INVALID_INT && x2 < m) m = x2
        return m
    }

    /**
     * calculate maximum of 3 integers, the first one is always a
     * valid number, the other can if INVALID_INT (=>not taken into
     * account) or valid integers, then they are evaluated
     */
    fun max(x: Int, xt: Int, x2: Int): Int {
        var m = x
        if (x == INVALID_INT) Log.e(TAG, "Utils.min: x is undefined.")
        if (xt != INVALID_INT && xt > m) m = xt
        if (x2 != INVALID_INT && x2 > m) m = x2
        return m
    }


}
