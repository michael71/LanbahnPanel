package de.blankedv.lanbahnpanel.util

import de.blankedv.lanbahnpanel.R
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import java.util.Hashtable

object AndroBitmaps {


    val bitmaps = Hashtable<String, Bitmap>()

    fun init(resources: Resources) {

        bitmaps["greendot"] = BitmapFactory.decodeResource(resources, R.drawable.greendot)
        bitmaps["reddot"] = BitmapFactory.decodeResource(resources, R.drawable.reddot)
        bitmaps["greydot"] = BitmapFactory.decodeResource(resources, R.drawable.greydot)

        bitmaps["commok"] = BitmapFactory.decodeResource(resources, R.drawable.commok)
        bitmaps["nocomm"] = BitmapFactory.decodeResource(resources, R.drawable.nocomm)

        bitmaps["sensor_on"] = BitmapFactory.decodeResource(resources, R.drawable.led_red_2)
        bitmaps["sensor_off"] = BitmapFactory.decodeResource(resources, R.drawable.led_off_2)

        bitmaps["lamp_on"] = BitmapFactory.decodeResource(resources, R.drawable.lamp1)
        bitmaps["lamp_off"] = BitmapFactory.decodeResource(resources, R.drawable.lamp0)


        bitmaps["incr"] = BitmapFactory.decodeResource(resources, R.drawable.incr)
        bitmaps["decr"] = BitmapFactory.decodeResource(resources, R.drawable.decr)

        bitmaps["lock"] = BitmapFactory.decodeResource(resources, R.drawable.lock_s)
        bitmaps["unlock"] = BitmapFactory.decodeResource(resources, R.drawable.unlock_s)

        bitmaps["clearrouteson"] = BitmapFactory.decodeResource(resources, R.drawable.clearrouteson)
        bitmaps["clearroutesoff"] = BitmapFactory.decodeResource(resources, R.drawable.clearroutesoff)

        bitmaps["lonstokewest"] = BitmapFactory.decodeResource(resources, R.drawable.lonstokewest)

    }



}
