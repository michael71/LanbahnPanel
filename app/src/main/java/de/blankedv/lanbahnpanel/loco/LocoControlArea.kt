package de.blankedv.lanbahnpanel.loco

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.Log
import de.blankedv.lanbahnpanel.loco.LocoControlArea.Companion.X_LOCO_MID
import de.blankedv.lanbahnpanel.loco.LocoControlArea.Companion.X_LOCO_RANGE
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.AndroBitmaps.bitmaps
import de.blankedv.lanbahnpanel.view.Dialogs

/**
 * handles the display of the top 20% of the display, the "LOCO CONTROL AREA"
 * one loco can be controlled (out of the ones defined in "locos-xyz.xml" Files
 *
 * @author mblank
 */
class LocoControlArea(internal var ctx: Context) {

    private val stopBtn: LocoButton
    private val lampBtn: LocoButton
    private val adrBtn: LocoButton

    private val functionBtn: LocoButton
    private val nameBtn: LocoButton? = null

    private var paintLocoAdrTxt = Paint()
    private var paintLocoSpeedTxt = Paint()
    private var paintLocoSpeed = Paint()
    private var paintStopText = Paint()
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
         paintLocoSpeed.color = -0xdd00de
        paintLocoAdrTxt.color = Color.BLACK
        paintLocoAdrTxt.textSize = calcTextSize(width)
        paintStopText.color = Color.WHITE
        paintStopText.textSize = paintLocoAdrTxt.textSize * 0.7f
        editPaint.color = Color.RED
        editPaint.textSize = calcTextSize(width)
        editPaint.typeface = Typeface.DEFAULT_BOLD
        paintLocoSpeedTxt.color = Color.WHITE
        paintLocoSpeedTxt.textSize = calcTextSize(width) * 0.66f
        green.color = Color.GREEN
        white.color = Color.WHITE

        stopBtn = LocoButton(0.20f, 0.5f)  // only text
        lampBtn = LocoButton(0.848f, 0.5f,
                bitmaps?.get("lamp1")!!,
                bitmaps?.get("lamp0")!!)
        functionBtn = LocoButton(0.796f, 0.5f,
                bitmaps?.get("func1")!!,
                bitmaps?.get("func0")!!)
        adrBtn = LocoButton(0.91f, 0.5f)  // only text.
        //adrBtn = new LocoButton(0.91f,0.5f,bitmaps.get("genloco_s"));

        //commBtn = LocoButton(0.09f, 0.5f, bitmaps.get("commok"), bitmaps.get("nocomm"))
        //powerBtn = LocoButton(0.13f, 0.5f, bitmaps.get("greendot"), bitmaps.get("reddot"), bitmaps.get("greydot"))
    }

    fun draw(canvas: Canvas) {

        selectedLoco?.updateLocoFromSX()  // to be able to display actual states of this loco

        // draw "buttons" and states

        adrBtn.doDraw(canvas, selectedLoco?.adr!!, paintLocoAdrTxt)
        lampBtn.doDraw(canvas, selectedLoco?.lamp_to_be!!)
        functionBtn.doDraw(canvas, selectedLoco?.function_to_be!!)
        if (selectedLoco?.speed_act !== 0) {
            stopBtn.doDraw(canvas, "Stop", paintStopText)
        } else {
            stopBtn.doDraw(canvas, " * ", paintStopText)
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
        canvas.drawText(locoSpeed(), xtext.toFloat(), ySpeed + 32, paintLocoSpeedTxt)
        xtext = (canvasWidth * (X_LOCO_MID - X_LOCO_RANGE * 0.9f)).toInt()
        canvas.drawText(selectedLoco?.longString(), xtext.toFloat(), ySpeed + 32, paintLocoSpeedTxt)

        //if (enableEdit) canvas.drawText("Edit", (canvas.width * 0.36f).toInt().toFloat(), ySpeed * 0.8f, editPaint)
        //if (demoFlag) canvas.drawText("Demo", (canvas.width * 0.28f).toInt().toFloat(), ySpeed * 0.8f, demoPaint)
        //canvas.drawText(""+counter, xtext, ySpeed-20, paintLocoSpeedTxt);

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
        } else if (adrBtn.isTouched(x, y)) {
            Dialogs.selectLocoDialog()

        }

    }

    fun recalcGeometry() {
        stopBtn.recalcXY()
        lampBtn.recalcXY()
        adrBtn.recalcXY()
        functionBtn.recalcXY()
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
