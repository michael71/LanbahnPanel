package de.blankedv.lanbahnpanel.util

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat.getDrawable
import android.text.TextPaint
import de.blankedv.lanbahnpanel.R

/**
 * initialize all paints for later use in onDraw() methods (depending on diplay style/theme)
 *
 * TODO review kotlin code
 */
object LPaints {
    // define Paints
    var linePaint = Paint()
    var linePaint2 = Paint()
    var rasterPaint = Paint()
    var circlePaint = Paint()
    var btn0Paint = Paint()
    var btn1Paint = Paint()
    var greenPaint = Paint()
    var redPaint = Paint()
    var linePaintRedDash = Paint()
    var linePaintGrayDash = Paint()
    var linePaintLightYellowDash = Paint()
    var linePaintDarkYellowDash = Paint()
    var BG_COLOR: Int = 0
    var bgPaint = Paint()
    var addressBGPaint = Paint()
    var signalLine = Paint()
    var greyPaint = Paint()
    var whitePaint = Paint()
    var greenSignal = Paint()
    var redSignal = Paint()
    var yellowPaint = Paint()
    var yellowSignal = Paint()

    // used for displaying addresses on panel and for panel Name
    var addressPaint = TextPaint()
    var panelNamePaint = TextPaint()

    fun init(prescale: Int, selectedStyle : String, ctx : Context) {

        BG_COLOR = Color.DKGRAY // panel background color

        linePaint.color = Color.WHITE
        linePaint.strokeWidth = 4.5f * prescale
        linePaint.isAntiAlias = true
        linePaint.isDither = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        // linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        signalLine.color = Color.WHITE
        signalLine.strokeWidth = 2.0f * prescale
        signalLine.isAntiAlias = true
        signalLine.isDither = true
        signalLine.style = Paint.Style.STROKE
        signalLine.strokeCap = Paint.Cap.SQUARE
        // linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        linePaintRedDash.color = Color.RED
        linePaintRedDash.strokeWidth = 3.0f * prescale
        linePaintRedDash.isAntiAlias = true
        linePaintRedDash.isDither = true
        linePaintRedDash.style = Paint.Style.STROKE
        linePaintRedDash.strokeCap = Paint.Cap.SQUARE
        linePaintRedDash.pathEffect = DashPathEffect(floatArrayOf(5f*prescale, 10f*prescale), 0f)

        linePaintGrayDash = Paint(linePaintRedDash)
        linePaintGrayDash.color = -0x555556

        linePaintDarkYellowDash = Paint(linePaintRedDash)
        linePaintDarkYellowDash.color = Color.YELLOW

        linePaintLightYellowDash = Paint(linePaintRedDash)
        linePaintLightYellowDash.color = Color.YELLOW // TODO 0xffaaaa00);


        linePaint2.color = Color.WHITE
        linePaint2.strokeWidth = 4.5f * prescale
        linePaint2.isAntiAlias = true
        linePaint2.isDither = true
        linePaint2.style = Paint.Style.STROKE
        linePaint2.strokeCap = Paint.Cap.ROUND

        rasterPaint.color = Color.LTGRAY
        rasterPaint.isAntiAlias = true
        rasterPaint.isDither = true
        rasterPaint.strokeWidth = 0.8f * prescale

        circlePaint.color = -0x7700ddde
        circlePaint.isAntiAlias = true
        circlePaint.isDither = true

        greenPaint.color = -0x33ff0100
        greenPaint.isAntiAlias = true
        greenPaint.strokeWidth = 4.5f * prescale
        greenPaint.isDither = true
        greenPaint.style = Paint.Style.STROKE
        greenPaint.strokeCap = Paint.Cap.ROUND

        greenSignal = Paint(greenPaint)
        greenSignal.style = Paint.Style.FILL

        yellowPaint.color = -0x33000100
        yellowPaint.isAntiAlias = true
        yellowPaint.strokeWidth = 4.5f * prescale
        yellowPaint.isDither = true
        yellowPaint.style = Paint.Style.STROKE
        yellowPaint.strokeCap = Paint.Cap.ROUND

        yellowSignal = Paint(yellowPaint)
        yellowSignal.style = Paint.Style.FILL

        redPaint.color = -0x33010000
        redPaint.strokeWidth = 4.5f * prescale
        redPaint.isAntiAlias = true
        redPaint.isDither = true
        redPaint.style = Paint.Style.STROKE
        redPaint.strokeCap = Paint.Cap.ROUND

        redSignal = Paint(redPaint)
        redSignal.style = Paint.Style.FILL

        greyPaint.color = Color.GRAY
        greyPaint.strokeWidth = 4.5f * prescale
        greyPaint.isAntiAlias = true
        greyPaint.isDither = true
        greyPaint.style = Paint.Style.STROKE
        greyPaint.strokeCap = Paint.Cap.ROUND

        whitePaint.color = Color.WHITE
        whitePaint.strokeWidth = 4.5f * prescale
        whitePaint.isAntiAlias = true
        whitePaint.isDither = true
        whitePaint.style = Paint.Style.STROKE
        whitePaint.strokeCap = Paint.Cap.ROUND

        btn0Paint.color = Color.GRAY
        btn0Paint.strokeWidth = 6f * prescale
        btn0Paint.isAntiAlias = true
        btn0Paint.isDither = true
        btn0Paint.style = Paint.Style.STROKE
        btn0Paint.strokeCap = Paint.Cap.ROUND

        btn1Paint.color = Color.WHITE
        btn1Paint.strokeWidth = 6f * prescale
        btn1Paint.isAntiAlias = true
        btn1Paint.isDither = true
        btn1Paint.style = Paint.Style.STROKE
        btn1Paint.strokeCap = Paint.Cap.ROUND

        bgPaint.color = BG_COLOR
        bgPaint.isAntiAlias = true
        bgPaint.strokeWidth = 3.8f * prescale
        bgPaint.isDither = true
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeCap = Paint.Cap.BUTT

        addressPaint.color = Color.YELLOW
        addressPaint.textSize = (7 * prescale).toFloat()
        addressPaint.style = Style.FILL

        addressBGPaint = Paint()
        addressBGPaint.color = Color.DKGRAY
        addressBGPaint.alpha = 175

        panelNamePaint.color = Color.LTGRAY
        panelNamePaint.textSize = (12 * prescale).toFloat()
        panelNamePaint.style = Style.FILL

        when (selectedStyle) {
            "DE" -> {

                linePaint.color = Color.BLACK
                signalLine.color = Color.BLACK
                linePaint2.color = Color.BLACK
                rasterPaint.color = Color.GRAY
                rasterPaint.isAntiAlias = true
                rasterPaint.isDither = true
                greyPaint.color = Color.GRAY
                whitePaint.color = Color.BLACK
                BG_COLOR = Color.LTGRAY
                bgPaint.color = BG_COLOR

                addressPaint.color = Color.YELLOW
                addressBGPaint.color = Color.DKGRAY
                panelNamePaint.color = Color.BLACK

            }
            "UK" -> {

                linePaint.color = Color.BLACK
                signalLine.color = Color.BLACK
                linePaint2.color = Color.BLACK
                rasterPaint.color = Color.LTGRAY
                rasterPaint.isAntiAlias = true
                rasterPaint.isDither = true
                greyPaint.color = Color.GRAY
                whitePaint.color = Color.BLACK
                BG_COLOR = -0xcf99d0
                bgPaint.color = BG_COLOR

                addressPaint.color = Color.YELLOW
                addressBGPaint.color = Color.DKGRAY
                panelNamePaint.color = Color.BLACK

                }
        }

    }
}
