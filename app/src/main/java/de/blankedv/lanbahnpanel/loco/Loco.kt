package de.blankedv.lanbahnpanel.loco

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Message
import android.util.Log

import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication


import de.blankedv.lanbahnpanel.model.DEBUG
import de.blankedv.lanbahnpanel.model.TAG
import de.blankedv.lanbahnpanel.model.sendQ
import de.blankedv.lanbahnpanel.railroad.Commands


class Loco {
    var adr: Int = 0
    var name: String
    var mass: Int = 0    // 1...5 (never == 0 !)
    var vmax = 100   // maximum speed in km/h

    private var sxData = 0   // SX Data byte (if SX is used)

    // speed vars used for setting loco speed
    var speed_act: Int = 0        // -31 ... +31, speed currently sent via SXnet
    var speed_to_be: Int = 0   // -31 ... +31,  speed after Mass Simulation

    // actual speed read from SX bus - used for display
    var speed_from_sx: Int = 0

    private var last_sx = 999   // used to avoid resending

    internal var lamp: Boolean = false
    var lamp_to_be: Boolean = false
    internal var function: Boolean = false
    var function_to_be: Boolean = false
    internal var lastToggleTime: Long = 0
    private var speedSetTime: Long = 0   // last time the speed was set on interface

    var lbm: Bitmap? = null

    private var massCounter = 0
    private val CHANGERATE = 10   // incr/decr speed every 300msec
    private var changeCounter = 0

    private var sendNewDataFlag = false

    var incrFlag = false
    var decrFlag = false


    val isForward: Boolean
        get() = speed_act >= 0

    private// are the loco-controls touched in the last 5 seconds
    val isActive: Boolean
        get() = if (System.currentTimeMillis() - speedSetTime < 5000 || System.currentTimeMillis() - lastToggleTime < 5000) {
            true
        } else {
            false
        }

    constructor() {  // dummy loco
        this.adr = 22
        this.name = "Lok 22"
        this.mass = 3
        lastToggleTime = 0 // no toggle so far
        // init other data from actual SX bus data
        initFromSX()

    }

    constructor(name: String) {  // dummy loco
        this.adr = 3
        this.name = name
        this.mass = 3
        lastToggleTime = 0 // no toggle so far
        // init other data from actual SX bus data
        initFromSX()
    }

    constructor(name: String, adr: Int, mass: Int) { // TODO lbm: Bitmap) {

        this.adr = adr
        this.name = name
        //this.lbm = lbm
        if (mass >= 1 && mass <= 5) {
            this.mass = mass
        } else {
            this.mass = 3
        }
        lastToggleTime = 0 // no toggle so far
        // init other data from actual SX bus data
        initFromSX()
    }

    fun getAdr(): String {
        return "" + adr
    }


    fun initFromSX() {
        Commands.readLocoData(this.adr)
        resetToBe()
    }

    private fun resetToBe() {
        speed_act = speed_from_sx
        speed_to_be = speed_act
        function_to_be = function
        lamp_to_be = lamp
    }

    fun updateLocoFromSX(d : Int) {
        if (DEBUG) Log.d(TAG,"updateLocoFromSX d="+d)
        var s = d and 0x1f
        if (d and 0x20 != 0) s = -s
        speed_from_sx = s
        lamp = d and 0x40 != 0
        function = d and 0x80 != 0
        if (System.currentTimeMillis() - lastToggleTime > 2000) {
            // safe to update "to-be" state as "as-is" state
            lamp_to_be = lamp
            function_to_be = function
        }
    }

    /** called every 100 milliseconds
     *
     */
    fun timer() {

        massSimulation()

        changeCounter++


        if (sendNewDataFlag) {  // if anything changed, send new value
            sendNewDataFlag = false

            // calc SX byte from speed, lamp, function
            var sx = 0
            if (lamp_to_be)
                sx = sx or 0x40
            if (function_to_be)
                sx = sx or 0x80
            if (speed_act < 0) {
                sx = sx or 0x20
                sx += -speed_act
            } else {
                sx += speed_act
            }
            if (sx != last_sx) { // avoid sending the same message again
                speedSetTime = System.currentTimeMillis()   // we are actively controlling the loco
                last_sx = sx

                /*if (demoFlag) {
                    Message m = Message.obtain();
                    m.what = SX_FEEDBACK_MESSAGE;
                    m.arg1 = adr;
                    m.arg2 = sx;
                    handler.sendMessage(m);  // send SX data to UI Thread via Message
                    return;
                } */
                Commands.setLocoData(adr, sx)


            }
        }
    }

    @Synchronized
    private fun massSimulation() {
        // depending on "mass", do more or less often
        if (massCounter < mass) {
            massCounter++
            return
        }

        if (incrFlag) {
            incrLocoSpeed()
        } else if (decrFlag) {
            decrLocoSpeed()
        }

        massCounter = 0 //reset
        // bring actual speed and speed_to_be closer together

        if (speed_to_be != speed_act) {
            if (speed_to_be > speed_act) {
                speed_act++
            }
            if (speed_to_be < speed_act) {
                speed_act--
            }
            if (DEBUG) Log.d(TAG, "massSim: to-be=$speed_to_be act=$speed_act")
            sendNewDataFlag = true
        }

        if (!isActive) {
            resetToBe()
        }
    }


    fun stopLoco() {
        speed_act = 0
        incrFlag = false
        decrFlag = false
        speed_to_be = speed_act
        sendNewDataFlag = true
        speedSetTime = System.currentTimeMillis()
    }

    fun setSpeed(s: Int) {
        speed_to_be = s
        // limit range
        if (speed_to_be < -31) speed_to_be = -31
        if (speed_to_be > 31) speed_to_be = 31
        speedSetTime = System.currentTimeMillis()
    }

    /** increase loco speed by one
     *
     */
    fun incrLocoSpeed() {
        speed_to_be += 1
        if (speed_to_be < -31) speed_to_be = -31
        if (speed_to_be > 31) speed_to_be = 31
        /*if (Math.abs(speed_act - speed_to_be) <= 1) {
            speed_act = speed_to_be
        } */
        sendNewDataFlag = true
        speedSetTime = System.currentTimeMillis()
    }

    /** increase loco speed by one
     *
     */
    fun decrLocoSpeed() {
        speed_to_be += -1
        if (speed_to_be < -31) speed_to_be = -31
        if (speed_to_be > 31) speed_to_be = 31
        sendNewDataFlag = true
        speedSetTime = System.currentTimeMillis()
    }

    fun startDecrLocoSpeed() {
        Log.d(TAG,"startDecrLocoSpeed")
        decrFlag = true
    }

    fun startIncrLocoSpeed() {
        Log.d(TAG,"startIncrLocoSpeed")
        incrFlag = true
    }

    fun isDecrOrIncr () : Boolean {
        return (decrFlag or incrFlag)
    }

    fun stopDecrIncr() {
        decrFlag = false
        incrFlag = false
    }
    fun toggleLocoLamp() {
        if (System.currentTimeMillis() - lastToggleTime > 250) {  // entprellen
            if (lamp_to_be) {
                lamp_to_be = false
            } else {
                lamp_to_be = true
            }
            lastToggleTime = System.currentTimeMillis()
            if (DEBUG) Log.d(TAG, "loco touched: toggle lamp_to_be")
            sendNewDataFlag = true
        }
    }

    fun toggleFunc() {
        if (System.currentTimeMillis() - lastToggleTime > 250) {  // entprellen
            if (function_to_be) {
                function_to_be = false
            } else {
                function_to_be = true
            }
            if (DEBUG) Log.d(TAG, "loco touched: toggle func")
            lastToggleTime = System.currentTimeMillis()
            sendNewDataFlag = true
        }
    }


    fun longString(): String {
        return "$name ($adr)(m=$mass)"
    }


}
