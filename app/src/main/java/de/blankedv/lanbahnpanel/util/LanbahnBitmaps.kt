package de.blankedv.lanbahnpanel.util

import de.blankedv.lanbahnpanel.R
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import java.util.Hashtable

object LanbahnBitmaps {


    val bitmaps = Hashtable<String, Bitmap>()

    fun init(resources: Resources) {

        bitmaps["greendot"] = BitmapFactory.decodeResource(resources, R.drawable.greendot)
        bitmaps["reddot"] = BitmapFactory.decodeResource(resources, R.drawable.reddot)
        bitmaps["greydot"] = BitmapFactory.decodeResource(resources, R.drawable.greydot)

        bitmaps["commok"] = BitmapFactory.decodeResource(resources, R.drawable.commok)
        bitmaps["nocomm"] = BitmapFactory.decodeResource(resources, R.drawable.nocomm)

        bitmaps["sensor_on"] = BitmapFactory.decodeResource(resources, R.drawable.led_red_2)
        bitmaps["sensor_off"] = BitmapFactory.decodeResource(resources, R.drawable.led_off_2)

        bitmaps["incr"] = BitmapFactory.decodeResource(resources, R.drawable.incr)
        bitmaps["decr"] = BitmapFactory.decodeResource(resources, R.drawable.decr)

        bitmaps["lock"] = BitmapFactory.decodeResource(resources, R.drawable.ic_locked)
        bitmaps["unlock"] = BitmapFactory.decodeResource(resources, R.drawable.ic_unlocked)

        bitmaps["q1"] = BitmapFactory.decodeResource(resources, R.drawable.q1_v2_48)
        bitmaps["q2"] = BitmapFactory.decodeResource(resources, R.drawable.q2_v2_48)
        bitmaps["q3"] = BitmapFactory.decodeResource(resources, R.drawable.q3_v2_48)
        bitmaps["q4"] = BitmapFactory.decodeResource(resources, R.drawable.q4_v2_48)
        bitmaps["qa"] = BitmapFactory.decodeResource(resources, R.drawable.qa_v2_48)


        bitmaps["clearrouteson"] = BitmapFactory.decodeResource(resources, R.drawable.clearrouteson)
        bitmaps["clearroutesoff"] = BitmapFactory.decodeResource(resources, R.drawable.clearroutesoff)

        bitmaps["lonstokewest"] = BitmapFactory.decodeResource(resources, R.drawable.lonstokewest)

        bitmaps["genloco_s"] = BitmapFactory.decodeResource(resources, R.drawable.genloco_s)

        bitmaps["stop_s_on"] = BitmapFactory.decodeResource(resources, R.drawable.stop_s_on)
        bitmaps["stop_s_off"] = BitmapFactory.decodeResource(resources, R.drawable.stop_s_off)


        bitmaps["lamp1"] = BitmapFactory.decodeResource(resources, R.drawable.light_on_24dp)
        bitmaps["lamp0"] = BitmapFactory.decodeResource(resources, R.drawable.light_off_24dp)

        bitmaps["func1"] = BitmapFactory.decodeResource(resources, R.drawable.bell_on_24dp)
        bitmaps["func0"] = BitmapFactory.decodeResource(resources, R.drawable.bell_off_24dp)

        bitmaps["slider"] = BitmapFactory.decodeResource(resources, R.drawable.slider_white_24dp)
        bitmaps["slider_grey"] = BitmapFactory.decodeResource(resources, R.drawable.slider_grey_24dp)
    }



}
