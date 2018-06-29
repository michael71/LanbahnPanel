package de.blankedv.lanbahnpanel

import android.graphics.*
import de.blankedv.lanbahnpanel.LanbahnPanelApplication.*
import android.util.Log
import de.blankedv.lanbahnpanel.AndroBitmaps.bitmaps

/**
 * control area - fixed geometry, above the panel
 */

class ControlArea {

    private val commBtn: ControlButton
    private val clearRoutesBtn: ControlButton
    private var editPaint = Paint()
    private var noWifiPaint = Paint()
    private var connPaint = Paint()

    private val ySpeed = 50f
    private val xSpeed = 0f   // needed for position of texts in control area

    private var errorMsg = ""
    private var errorTime: Long = 0
    private var seconds: Long = 0
    private var lastSecond: Long = 0

    init {
        val green: Paint
        val white: Paint


        editPaint.color = Color.RED
        editPaint.textSize = 30f
        editPaint.typeface = Typeface.DEFAULT_BOLD


        noWifiPaint.color = Color.RED
        noWifiPaint.textSize = 60f
        noWifiPaint.typeface = Typeface.DEFAULT_BOLD


        connPaint.color = Color.LTGRAY
        connPaint.textSize = 14f
        //connPaint.setTypeface(Typeface.DEFAULT_BOLD);

        green = Paint()
        green.color = Color.GREEN
        white = Paint()
        white.color = Color.WHITE

        commBtn = ControlButton(0.09f, 0.5f, bitmaps["commok"], bitmaps["nocomm"])
        //powerBtn = new ControlButton(0.13f,0.5f, bitmaps.get("greendot"),bitmaps.get("reddot"));
        clearRoutesBtn = ControlButton(0.25f, 0.5f, bitmaps["clearrouteson"], bitmaps["clearroutesoff"])

        //	lonstokeWestBtn = new ControlButton(0.50f,0.6f, bitmaps.get("lonstokewest"));
    }

    fun draw(canvas: Canvas) {

        commBtn.doDraw(canvas, LanbahnPanelApplication.connectionIsAlive())
        canvas.drawText(conn_state_string, (canvas.width * 0.01f).toInt().toFloat(), ySpeed * 1.6f, connPaint)


        //powerBtn.doDraw(canvas,LanbahnPanelApplication.isPowerOn());
        if (enableRoutes) {
            clearRoutesBtn.doDrawBlink(canvas, clearRouteButtonActive)
        }

        for (l in lampGroups)
            l.doDraw(canvas, l.isOn)


        canvas.drawBitmap(bitmaps["lonstokewest"], (canvas.width * 0.72f).toInt().toFloat(), ySpeed * 0.22f, null)

        if (enableEdit) canvas.drawText("Edit", (canvas.width * 0.36f).toInt().toFloat(), ySpeed * 1f, editPaint)
        if (noWifiFlag) {
            canvas.drawText("No Wifi !", (canvas.width * 0.54f).toInt().toFloat(), ySpeed * 1.3f, noWifiPaint)
        }

        if (errorMsg.length > 0 && System.currentTimeMillis() - errorTime < 3000) {
            canvas.drawText(errorMsg, (canvas.width * 0.38f).toInt().toFloat(), ySpeed * 1f, editPaint)
        } else {
            // clear
            errorMsg = ""
        }


        if (System.currentTimeMillis() - lastSecond >= 1000) {
            // increment seconds timer
            seconds++
            lastSecond = System.currentTimeMillis()
            Route.auto()
        }

    }

    fun checkTouch(x: Float, y: Float) {
        if (!LanbahnPanelApplication.connectionIsAlive() && commBtn.isTouched(x, y)) {
            restartCommFlag = true
            Log.d(TAG, "ControlArea - restartCommFlag is set")
        }
        if (enableRoutes && clearRoutesBtn.isTouched(x, y)) {
            if (!clearRouteButtonActive) {
                clearRouteButtonActive = true
            } else {
                // actual clear route is done in "RouteButtonElement" class
                clearRouteButtonActive = false
            }
        } else {
            for (l in lampGroups) {
                if (l.isTouched(x, y)) l.toggle()
            }
        }
    }

    fun recalcGeometry() {

        commBtn.recalcXY()
        clearRoutesBtn.recalcXY()

        for (l in lampGroups) {
            l.recalcXY()
        }

    }

    fun dispErrorMsg(e: String) {
        errorMsg = e
        errorTime = System.currentTimeMillis()
    }

}