package de.blankedv.lanbahnpanel.railroad

import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.railroad.loconet.Accessory
import de.blankedv.lanbahnpanel.railroad.loconet.LNMessage
import de.blankedv.lanbahnpanel.railroad.loconet.Sensor
import org.jetbrains.anko.toast

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

    override fun readChannel(addr: Int, peClass : Class<ActivePanelElement>) : String {
        // class can be ignored for selectrix

      return ""
    }

    override fun readChannel(addr: Int) : String {
        // class can be ignored for selectrix
        return ""
    }

    override fun setChannel(addr: Int, data: Int, peClass : Class<ActivePanelElement>) :String {
        return ""
    }

    override fun setChannel(addr: Int, data: Int) :String {
        return ""
    }

    override fun setPowerState (switchOn : Boolean) : String {
        if (switchOn) {
            return ""
        } else {
            return ""
        }
    }

    /**
     * Loconet TCP Protocol, see https://sourceforge.net/projects/loconetovertcp/
     */

    override fun handleReceive(msg: String, recHandler: Handler): Boolean {
        var msg = msg
        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running

        rx = recHandler

        var info: Array<String>? = null
        Log.d(TAG,"LN rec: $msg")

        interpretLoconetMessage(msg)   // calls "storeDCC_Data()"

        if (!msg.isEmpty()) {
            val m = Message.obtain()
            m.what = TYPE_LN_ACC_MSG
            m.obj = msg
              recHandler.sendMessage(m)  // send route data to UI Thread via Message
        }

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
                if (isValidAccessoryAddress(a.address))
                    Accessory.addOrReplaceAccessoryData(a)
                return a.toString()
            }
            "BC" -> return ln.saveSwitchRequest()  // isRequestSwitchState

            "B4" -> {
                val a = ln.evalLackToAccessory() // LackResponeToSwitchStateRequest
                if (a == null) return "?"
                if (isValidAccessoryAddress(a.address))
                    Accessory.addOrReplaceAccessoryData(a)

                return a.toString()
            }

            "B2" -> {
                val sens = ln.toSensor()  // SensorCmd
                Sensor.addOrReplaceSensorData(sens)
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
        return true
        /* return (!filterAddresses
                or (a in minAccAddress..maxAccAddress)
                or (a in MainActivity.buttonsDCCAddresses) )  // DCC Addresses switch by the Buttons */

    }
}
