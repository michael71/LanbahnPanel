package de.blankedv.lanbahnpanel.util

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.text.TextPaint
import de.blankedv.lanbahnpanel.model.selectedStyle

/**
 * initialize all paints for later use in onDraw() methods (depending on diplay style/theme)
 *
 * TODO review kotlin code
 */
object LPaints {
    // define Paints
    lateinit var linePaint: Paint
    lateinit var linePaint2: Paint
    lateinit var rasterPaint: Paint
    lateinit var circlePaint: Paint
    lateinit var btn0Paint: Paint
    lateinit var btn1Paint: Paint
    lateinit var greenPaint: Paint
    lateinit  var redPaint: Paint
    lateinit var linePaintRedDash: Paint
    lateinit var linePaintGrayDash: Paint
    lateinit var linePaintLightYellowDash: Paint
    lateinit var linePaintDarkYellowDash: Paint
    var BG_COLOR: Int = 0
    lateinit var bgPaint: Paint
    lateinit var addressBGPaint: Paint
    lateinit var signalLine: Paint
    lateinit var greyPaint: Paint
    lateinit var whitePaint: Paint
    lateinit var greenSignal: Paint
    lateinit var redSignal: Paint
    lateinit var yellowPaint: Paint
    lateinit var yellowSignal: Paint

    // used for displaying addresses on panel and for panel Name
    lateinit var addressPaint: TextPaint
    lateinit var panelNamePaint: TextPaint

    fun init(prescale: Int) {

        BG_COLOR = Color.DKGRAY // panel background color

        linePaint = Paint()
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = 4.5f * prescale
        linePaint.isAntiAlias = true
        linePaint.isDither = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        // linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        signalLine = Paint()
        signalLine.color = Color.WHITE
        signalLine.strokeWidth = 2.0f * prescale
        signalLine.isAntiAlias = true
        signalLine.isDither = true
        signalLine.style = Paint.Style.STROKE
        signalLine.strokeCap = Paint.Cap.SQUARE
        // linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        linePaintRedDash = Paint()
        linePaintRedDash.color = Color.RED
        linePaintRedDash.strokeWidth = 3.5f * prescale
        linePaintRedDash.isAntiAlias = true
        linePaintRedDash.isDither = true
        linePaintRedDash.style = Paint.Style.STROKE
        linePaintRedDash.strokeCap = Paint.Cap.SQUARE
        linePaintRedDash.pathEffect = DashPathEffect(
                floatArrayOf(10f, 20f), 0f)

        linePaintGrayDash = Paint(linePaintRedDash)
        linePaintGrayDash.color = -0x555556

        linePaintDarkYellowDash = Paint(linePaintRedDash)
        linePaintDarkYellowDash.color = Color.YELLOW

        linePaintLightYellowDash = Paint(linePaintRedDash)
        linePaintLightYellowDash.color = Color.YELLOW // TODO 0xffaaaa00);

        linePaint2 = Paint()
        linePaint2.color = Color.WHITE
        linePaint2.strokeWidth = 4.5f * prescale
        linePaint2.isAntiAlias = true
        linePaint2.isDither = true
        linePaint2.style = Paint.Style.STROKE
        linePaint2.strokeCap = Paint.Cap.ROUND

        rasterPaint = Paint()
        rasterPaint.color = Color.LTGRAY
        rasterPaint.isAntiAlias = true
        rasterPaint.isDither = true

        circlePaint = Paint()
        circlePaint.color = -0x7700ddde
        circlePaint.isAntiAlias = true
        circlePaint.isDither = true

        greenPaint = Paint()
        greenPaint.color = -0x33ff0100
        greenPaint.isAntiAlias = true
        greenPaint.strokeWidth = 4.5f * prescale
        greenPaint.isDither = true
        greenPaint.style = Paint.Style.STROKE
        greenPaint.strokeCap = Paint.Cap.ROUND

        greenSignal = Paint(greenPaint)
        greenSignal.style = Paint.Style.FILL

        yellowPaint = Paint()
        yellowPaint.color = -0x33000100
        yellowPaint.isAntiAlias = true
        yellowPaint.strokeWidth = 4.5f * prescale
        yellowPaint.isDither = true
        yellowPaint.style = Paint.Style.STROKE
        yellowPaint.strokeCap = Paint.Cap.ROUND

        yellowSignal = Paint(yellowPaint)
        yellowSignal.style = Paint.Style.FILL


        redPaint = Paint()
        redPaint.color = -0x33010000
        redPaint.strokeWidth = 4.5f * prescale
        redPaint.isAntiAlias = true
        redPaint.isDither = true
        redPaint.style = Paint.Style.STROKE
        redPaint.strokeCap = Paint.Cap.ROUND

        redSignal = Paint(redPaint)
        redSignal.style = Paint.Style.FILL

        greyPaint = Paint()
        greyPaint.color = Color.GRAY
        greyPaint.strokeWidth = 4.5f * prescale
        greyPaint.isAntiAlias = true
        greyPaint.isDither = true
        greyPaint.style = Paint.Style.STROKE
        greyPaint.strokeCap = Paint.Cap.ROUND

        whitePaint = Paint()
        whitePaint.color = Color.WHITE
        whitePaint.strokeWidth = 4.5f * prescale
        whitePaint.isAntiAlias = true
        whitePaint.isDither = true
        whitePaint.style = Paint.Style.STROKE
        whitePaint.strokeCap = Paint.Cap.ROUND

        btn0Paint = Paint()
        btn0Paint.color = Color.GRAY
        btn0Paint.strokeWidth = 6f * prescale
        btn0Paint.isAntiAlias = true
        btn0Paint.isDither = true
        btn0Paint.style = Paint.Style.STROKE
        btn0Paint.strokeCap = Paint.Cap.ROUND

        btn1Paint = Paint()
        btn1Paint.color = Color.WHITE
        btn1Paint.strokeWidth = 6f * prescale
        btn1Paint.isAntiAlias = true
        btn1Paint.isDither = true
        btn1Paint.style = Paint.Style.STROKE
        btn1Paint.strokeCap = Paint.Cap.ROUND

        bgPaint = Paint()
        bgPaint.color = BG_COLOR
        bgPaint.isAntiAlias = true
        bgPaint.strokeWidth = 3.8f * prescale
        bgPaint.isDither = true
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeCap = Paint.Cap.BUTT

        addressPaint = TextPaint()
        addressPaint.color = Color.YELLOW
        addressPaint.textSize = (7 * prescale).toFloat()
        addressPaint.style = Style.FILL

        addressBGPaint = Paint()
        addressBGPaint.color = Color.DKGRAY
        addressBGPaint.alpha = 175

        panelNamePaint = TextPaint()
        panelNamePaint.color = Color.LTGRAY
        panelNamePaint.textSize = (12 * prescale).toFloat()
        panelNamePaint.style = Style.FILL

        if (selectedStyle == "DE") {

            linePaint.color = Color.BLACK

            signalLine.color = Color.BLACK

            linePaint2.color = Color.BLACK

            rasterPaint.color = Color.LTGRAY
            rasterPaint.isAntiAlias = true
            rasterPaint.isDither = true

            greyPaint.color = Color.GRAY

            whitePaint.color = Color.BLACK
            BG_COLOR = Color.LTGRAY

            bgPaint.color = BG_COLOR

            addressPaint.color = Color.YELLOW

            addressBGPaint.color = Color.DKGRAY

            panelNamePaint.color = Color.BLACK

        } else if (selectedStyle == "UK") {

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
