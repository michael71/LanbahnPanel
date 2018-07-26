package de.blankedv.lanbahnpanel.railroad

import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.elements.SignalElement
import de.blankedv.lanbahnpanel.elements.TurnoutElement
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.railroad.loconet.Accessory
import de.blankedv.lanbahnpanel.railroad.loconet.LNMessage
import de.blankedv.lanbahnpanel.railroad.loconet.Sensor

/**
 * communicates with the SX3-PC server program (usually on port 4104)
 *
 * @author mblank
 */

class LbServerClient() : GenericClient() {

    lateinit var rx : Handler

    init {
        if (DEBUG) Log.d(TAG, "LbServerClient constructor.")
    }

    override fun readChannel(addr: Int, peClass : Class<*>) : String {
        when (peClass) {
            TurnoutElement::class.java, SignalElement::class.java -> {
                // TODO extend for multi-aspect signals (= more than 1bit-info 0/1
                return createReqState(addr)
            }
            else -> return ""
        }
    }

    override fun readChannel(addr: Int) : String {
        // class can be ignored for selectrix
        return "ERROR"
    }

    override fun setChannel(addr: Int, data: Int, peClass : Class<*>) :String {

        when (peClass) {
            TurnoutElement::class.java, SignalElement::class.java -> {
                // TODO extend for multi-aspect signals (= more than 1bit-info 0/1
                var d = 1
                if (data != 0) d = 0   // invert data info for loconet 1 = straight, 0 = closed
                return createSwitchReq(addr, d)
            }
        }
        return ""
    }

    override fun setChannel(addr: Int, data: Int) :String {
        return ""
    }

    override fun setPowerState (onoff : Boolean) : String {
        if (onoff) {
            return ""
        } else {
            return ""
        }
    }

    /**
     * Loconet TCP Protocol, see https://sourceforge.net/projects/loconetovertcp/
     */

    override fun handleReceive(msg: String, rxHandler: Handler): Boolean {

        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running

        rx = rxHandler

        Log.d(TAG,"LN rec: $msg")

        interpretLoconetMessage(msg)   // calls "storeDCC_Data()"

        return true
    }
    private fun sendToUI(msgType: Int, a1: Int, a2: Int, msg: String ="") {
        val m = Message.obtain()
        m.what = msgType   // LOCONET_MESSAGE, ERROR_MESSAGE OR  CONNECTION_MESSAGE type
        m.arg1 = a1
        m.arg2 = a2
        if (!msg.isEmpty()) m.obj = msg
        rx?.sendMessage(m)  // send string to UI Thread via Message
    }

    private fun storeDCC_Data(ln: LNMessage): String {
        // TODO not all opcodes implemented
        // TODO convert to adr/data for PanelElements
        when (ln.getOpCode()) {
            "A2" -> return ""  // OPC_LOCO_SND
            "A1" -> return ""  // OPC_LOCO_DIRF
            "A0" -> return ln.toSlotSpeed().toString()  // OPC_LOCO_SPD
            "82" -> {
                sendToUI(TYPE_POWER_MSG, 0,0)
                return "Global Power OFF"
            }
            "83" -> {
                sendToUI(TYPE_POWER_MSG, 0, 1)
                return "Global Power ON"
            }
            "B0" -> {
                val a = ln.toAccessory()  // switch or signal
                if (isValidAccessoryAddress(a.address)) {
                    Accessory.addOrReplaceAccessoryData(a)
                    sendToUI(TYPE_LN_ACC_MSG, a.address, a.data)
                }
                return a.toString()
            }
            "BC" -> return ln.saveSwitchRequest()  // isRequestSwitchState

            "B4" -> {
                val a = ln.evalLackToAccessory() // LackResponeToSwitchStateRequest
                if (a == null) return "?"
                if (isValidAccessoryAddress(a.address)) {
                    Accessory.addOrReplaceAccessoryData(a)
                    sendToUI(TYPE_LN_ACC_MSG, a.address, a.data)
                }

                return a.toString()
            }

            "B2" -> {
                val sens = ln.toSensor()  // SensorCmd
                Sensor.addOrReplaceSensorData(sens)
                sendToUI(TYPE_LN_SENSOR_MSG, sens.address, sens.data)
                return sens.toString()
            }

            "E4" -> {
                val lis = ln.updateOrCreateLissy()
                return lis.toString()
            }

            "E5","ED" -> {
                return ln.toLNCVString()
            }

            else -> return "?"
        }


    }

    private fun interpretLoconetMessage(lnmessage: String) {

        if (lnmessage.contains("RECEIVE ")) {
            // remove "RECEIVE" from string and convert to Array
            val ln = LNMessage(lnmessage)
            // store in central dcc datastore
            var st = storeDCC_Data(ln)
            if (DEBUG) Log.d(TAG,"LN: $st")
          }
    }

    private fun isValidAccessoryAddress (a : Int) : Boolean {
        // TODO
        return true
        /* return (!filterAddresses
                or (a in minAccAddress..maxAccAddress)
                or (a in MainActivity.buttonsDCCAddresses) )  // DCC Addresses switch by the Buttons */

    }

    private fun createSwitchReq(a : Int, d : Int) :String {
        /* <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function
 <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>, 7 ls adr bits. A1,A0 select 1 of 4 input pairs in a DS54
 <SW2> =<0,0,DIR,ON- A10,A9,A8,A7> Control bits and 4 MS adr bits.
 ,DIR=1 for Closed,/GREEN, =0 for Thrown/RED
 ,ON=1 for Output ON, =0 FOR output OFF */
        val intA = IntArray(3)
        intA[0] = 0xb0  // opcode
        var adr = a -1
        intA[1] = (adr and 0x7f)
        var dir = 0x10
        if (d != 0) dir += 0x20
        intA[2] = dir + (adr.shr(7) and 0x0f)
        val lnmsg = LNMessage(intA)
        return "SEND "+lnmsg.toXString()
        /* example for turnout #107
        RECEIVE B0 6A 10 35     LN: switch addr=107 0/thrown/red ON
        RECEIVE B0 6A 00 25                         closed ON  */
    }

    private fun createReqState(a : Int) :String {
        // example BC 4F 0F 03  => LACK B4 3C 50 27
        /* 0xBC ;REQ state of SWITCH
          ;<0xBC>,<SW1>,<SW2>,<CHK>  */
        val intA = IntArray(3)
        intA[0] = 0xbc  // opcode
        var adr = a -1
        intA[1] = (adr and 0x7f)
        intA[2] = (adr.shr(7) and 0x0f)
        val lnmsg = LNMessage(intA)
        return "SEND "+lnmsg.toXString()
        /* example for turnout #107
        BC 4F 0F 03    switch state req, a=2000  */
    }
}
