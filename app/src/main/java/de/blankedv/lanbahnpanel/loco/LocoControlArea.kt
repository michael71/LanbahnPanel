package de.blankedv.lanbahnpanel.loco

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.*
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.LanbahnBitmaps.bitmaps
import de.blankedv.lanbahnpanel.view.Dialogs

/**
 * handles the display of the top 20% of the display, the "LOCO CONTROL AREA"
 * one loco can be controlled (out of the ones defined in "locos-xyz.xml" File)
 *
 * @author mblank
 */
class LocoControlArea(internal var ctx: Context) {

    private val stopBtn: LocoButton
    private val lampBtn: LocoButton    // F0
    private val addressBtn: LocoButton
    private val functionBtn: LocoButton  //F1
    private val leftBtn: LocoButton  // speed to left
    private val rightBtn: LocoButton  // speed to right

    private var paintText = Paint()
    private var paintLargeTxt = Paint()
    private var paintTextDisabled = Paint()

    private var green = Paint()
    private var white = Paint()
    private var editPaint = Paint()
    private var sliderXoff = bitmaps.get("slider")?.getWidth()!! / 2
    private var sliderYoff = bitmaps.get("slider")?.getHeight()!! / 2

    init {

        val metrics = ctx.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        // define some paints for the loco controls and texts

        paintLargeTxt.color = WHITE
        paintLargeTxt.textSize = calcTextSize(width)

        paintText.color = WHITE
        paintText.textSize = paintLargeTxt.textSize * 0.7f

        paintTextDisabled.color = LTGRAY
        paintTextDisabled.textSize = paintLargeTxt.textSize * 0.7f


        editPaint.color = RED
        editPaint.textSize = calcTextSize(width)
        editPaint.typeface = Typeface.DEFAULT_BOLD
        green.color = GREEN
        white.color = WHITE

        addressBtn = LocoButton(0.11f, 0.5f)  // only text.
        stopBtn = LocoButton(0.20f, 0.5f)  // only text
        lampBtn = LocoButton(0.848f, 0.5f,
                bitmaps?.get("lamp1")!!,     // bitmap for "on" state
                bitmaps?.get("lamp0")!!)     // bitmap for "off" state
        functionBtn = LocoButton(0.796f, 0.5f,
                bitmaps?.get("func1")!!,
                bitmaps?.get("func0")!!)

        leftBtn = LocoButton(0.03f, 0.5f, bitmaps?.get("left")!!, bitmaps?.get("left")!!)
        rightBtn = LocoButton(0.97f, 0.5f, bitmaps?.get("right")!!)

      }

    private fun drawSlider(canvas : Canvas) {

    }
    fun draw(canvas: Canvas) {

        // Done in activity regularly
        //        selectedLoco?.updateLocoFromSX()  // to be able to display actual states of this loco

        // draw "buttons" and states
        leftBtn.doDraw(canvas,true)
        rightBtn.doDraw(canvas,true)
        addressBtn.doDraw(canvas, "A:"+selectedLoco?.adr!!.toString(), paintLargeTxt)
        lampBtn.doDraw(canvas, selectedLoco?.lamp_to_be!!)
        functionBtn.doDraw(canvas, selectedLoco?.function_to_be!!)
        if (selectedLoco?.speed_act !== 0) {
            stopBtn.doDraw(canvas, "Stop", paintText)
        } else {
            stopBtn.doDraw(canvas, "(Stop)", paintTextDisabled)
        }
        // draw slider for speed selection
        sxmin = (canvas.width * (X_LOCO_MID - X_LOCO_RANGE)).toInt()
        sxmax = (canvas.width * (X_LOCO_MID + X_LOCO_RANGE)).toInt()

        val speedLine = Rect(sxmin, (ySpeed - 1).toInt(), sxmax, ySpeed.toInt() + 1)
        canvas.drawRect(speedLine, white)

        val zeroLine = Rect((sxmin + sxmax) / 2 - 1, (ySpeed - ZERO_LINE_HALFLENGTH).toInt(),
                (sxmin + sxmax) / 2 + 1, (ySpeed + ZERO_LINE_HALFLENGTH).toInt())
        canvas.drawRect(zeroLine, white)

        canvasWidth = canvas.width.toFloat()

        xSpeedAct = canvasWidth * sxSpeed()   // grey slider on bottom
        xSpeedToBe = canvasWidth * speedToBe()  // orange slider on top

        canvas.drawBitmap(bitmaps.get("slider_grey"), xSpeedAct - sliderXoff, ySpeed - sliderYoff, null)
        canvas.drawBitmap(bitmaps.get("slider"), xSpeedToBe - sliderXoff, ySpeed - sliderYoff, null)

        var xtext = (canvasWidth * (X_LOCO_MID + X_LOCO_RANGE * 0.9f)).toInt()
        canvas.drawText(locoSpeed(), xtext.toFloat(), ySpeed + 32, paintText)
        xtext = (canvasWidth * (X_LOCO_MID - X_LOCO_RANGE * 0.9f)).toInt()
        canvas.drawText(selectedLoco?.longString(), xtext.toFloat(), ySpeed + 32, paintText)



    }

    private fun sxSpeed(): Float {
        val s = selectedLoco?.speed_from_sx

        return X_LOCO_RANGE * s?.toFloat()!! / 31.0f + X_LOCO_MID
    }

    private fun speedToBe(): Float {
        val s = selectedLoco?.speed_to_be
        return X_LOCO_RANGE * s?.toFloat()!! / 31.0f + X_LOCO_MID
    }

    private fun locoSpeed(): String {
        val s = selectedLoco?.speed_from_sx
        return "" + s
    }


    fun checkSpeedMove(xt: Float, yt: Float) {
        // check slider
        //if (DEBUG) Log.d(TAG,"check slider touch xt="+xt+"  yt="+yt+" xSpeed="+xSpeed+" sliderXoff="+sliderXoff+" ySpeed="+ySpeed+" sliderYoff="+sliderYoff);

        if (xt > (X_LOCO_MID - X_LOCO_RANGE) * canvasWidth
                && xt < (X_LOCO_MID + X_LOCO_RANGE) * canvasWidth
                && yt > ySpeed - sliderYoff
                && yt < ySpeed + sliderYoff) {
            lastSpeedCheckMove = System.currentTimeMillis()
            lastXt = xt
            val s = Math.round(31.0f / X_LOCO_RANGE * (xt - X_LOCO_MID * canvasWidth) / canvasWidth)
            if (DEBUG) Log.d(TAG, "slider, speed set to be = $s")
            selectedLoco?.setSpeed(s)  // will be sent only when different to currently known speed.

        }
    }

    /**
     * check, if the control area was touched at the Button positions or at the speed slider
     *
     * @param x
     * @param y
     */
    fun checkTouch(x: Float, y: Float) {
        if (stopBtn.isTouched(x, y)) {
            selectedLoco?.stopLoco()
        } else if (lampBtn.isTouched(x, y)) {
            selectedLoco?.toggleLocoLamp()
        } else if (functionBtn.isTouched(x, y)) {
            selectedLoco?.toggleFunc()
        } else if (addressBtn.isTouched(x, y)) {
            Dialogs.selectLocoDialog()
        } else if (leftBtn.isTouched(x, y)) {
            selectedLoco?.decrLocoSpeed()
        } else if (rightBtn.isTouched(x, y)) {
            selectedLoco?.incrLocoSpeed()
        }

    }
/*
    fun checkIncrDecrSpeed(x: Float, y: Float) {
        if (leftBtn.isTouched(x, y)) {
            selectedLoco?.startDecrLocoSpeed()
        } else if (rightBtn.isTouched(x, y)) {
            selectedLoco?.startIncrLocoSpeed()
        }
    } */

    fun recalcGeometry() {
        stopBtn.recalcXY()
        lampBtn.recalcXY()
        addressBtn.recalcXY()
        functionBtn.recalcXY()
        leftBtn.recalcXY()
        rightBtn.recalcXY()
        ySpeed = (controlAreaRect?.bottom!!.toFloat() - controlAreaRect?.top!!) / 2  // mid of control area range.
    }

    private fun calcTextSize(w: Int): Float {
        // text size = 30 for width=1024
        return 30.0f * w / 1024
    }

    companion object {



        private val X_LOCO_MID = 0.5f
        private val X_LOCO_RANGE = 0.25f

        private var ySpeed = 50f
        private var xSpeedAct = 0f
        private var xSpeedToBe = 0f
        private val ZERO_LINE_HALFLENGTH = 15f  //
        private var canvasWidth = 100f

        private var sxmin = 0
        private var sxmax = 0
        private var lastSpeedCheckMove = 0L
        private var lastXt = X_LOCO_MID
    }

}
