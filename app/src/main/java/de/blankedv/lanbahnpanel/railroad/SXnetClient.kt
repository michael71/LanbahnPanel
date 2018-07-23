package de.blankedv.lanbahnpanel.railroad

import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.model.*

/**
 * communicates with the SX3-PC server program (usually on port 4104)
 *
 * @author mblank
 */

class SXnetClient() : GenericClient() {

    init {
        if (DEBUG) Log.d(TAG, "SXnetClient constructor.")
    }

    override fun readChannel(addr: Int, peClass : Class<*>) : String {
        // class can be ignored for selectrix (sxnet)
        // lanbahn address 701 => sxAddress 70.1  (bit 1)
        // bit is ignored here
        if (isValidSXAddress(addr/10)) {
            return "R ${addr / 10}"
        } else {
            return ""
        }
    }

    override fun readChannel(addr: Int) : String {
        // lanbahn address 701 => sxAddress 70.1  (bit 1)
        if (isValidSXAddress(addr/10)) {
            return "R ${addr / 10}"
        } else {
            return ""
        }
    }

    // NOT USED HERE
    fun readSXChannel(addr: Int) : String {
        return "R $addr"   // specific sx message
    }

    override fun setChannel(addr: Int, data: Int, peClass : Class<*>) :String {
        // class can be ignored for selectrix (sxnet)
        return setChannel(addr, data)
    }

    override fun setChannel(addr: Int, data: Int) :String {
        // for sx, we need to calculate the bit from the address=chan.bit
        val chan = addr / 10
        val bit = addr.rem(10)
        return "S $chan.$bit $data"
    }

    override fun setPowerState (switchOn : Boolean) : String {
        if (switchOn) {
            return "S $SX_POWER_CHANNEL 127"
        } else {
            return "S $SX_POWER_CHANNEL 0"
        }
    }



    /**
     * SX Net Protocol rev3 (all msg terminated with CR)
     * SET 803 2  / READ 803  / XL 803 2 (feedback message)
     *
     * for a list of the channels which the client has set or read in the past all changes are
     * transmitted back to the client
     */

    override fun handleReceive(msg: String, recHandler: Handler): Boolean {
        var msg = msg
        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running


        var info: Array<String>? = null
        msg = msg.toUpperCase()

        var sxAddr : Int
        var sxData: Int

        // new code: multiple commands in a single message, separated by ';'
        // example: String msg = "S 780 2 ;; S 800 3  ;  S 720 1";
        val allcmds = msg.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cmd in allcmds) {
            //Log.d(TAG,"single cmd="+cmd);
            if (!cmd.contains("ERROR")) {
                info = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // all SX feedback message have the Format "X <adr> <data>"
                // adr range 1 ... 111 or 127, data range 0 ..255
                // adr=127 is the sxnet "Power" channel
                if (info.size >= 3 && info[0] == "X") {
                    sxAddr = extractSxChannelFromString(info[1])
                    sxData = extractDataByteFromString(info[2])
                    if (sxAddr != INVALID_INT && sxData != INVALID_INT) {
                        val m = Message.obtain()
                        if (sxAddr == SX_POWER_CHANNEL) {
                            m.what = TYPE_POWER_MSG
                        } else {
                            m.what = TYPE_SX_MSG
                        }
                        m.arg1 = sxAddr
                        m.arg2 = sxData
                        recHandler.sendMessage(m)  // send SX data to UI Thread via Message
                    } else {
                        Log.e(TAG, "range error in rec. msg, cmd=$cmd adr=$sxAddr data=$sxData")
                    }
                } else if (info.size >= 3 && info[0] == "RT") {
                    sxAddr = extractSxChannelFromString(info[1])
                    sxData = extractDataByteFromString(info[2])
                    if (sxAddr != INVALID_INT && sxData != INVALID_INT) {
                        val m = Message.obtain()
                        m.what = TYPE_ROUTE_MSG
                        m.arg1 = sxAddr
                        m.arg2 = sxData
                        recHandler.sendMessage(m)  // send route data to UI Thread via Message
                    }
                }
            }
        }
        return true
    }

    /** convert data string to integer and
     *  check if data is in valid range for selectrix (8 bit, 0..255)
     */
    private fun extractDataByteFromString(s: String): Int {
        // converts String to integer between 0 and 255 (maximum data range)
        var data: Int? = INVALID_INT
        try {
            data = Integer.parseInt(s)
            if (data < 0 || data > 255) {
                data = INVALID_INT
            }
        } catch (e: Exception) {
            data = INVALID_INT
        }

        return data!!
    }

    /** convert address string to integer and
     *  check if address (=channel) is in valid range for selectrix
     */
    private fun extractSxChannelFromString(s: String): Int {

        try {
            var sxAddr = Integer.parseInt(s)
            if (isValidSXAddress(sxAddr) or (sxAddr == SX_POWER_CHANNEL)) {
                return sxAddr
            }
        } catch (e: NumberFormatException) {
        }
        return INVALID_INT

    }

    companion object {

        // check if address is in Selectrix Range 0..111
        fun isValidSXAddress(addr : Int) : Boolean {
            return ((addr >= SXMIN) and (addr <= SXMAX))

        }

        fun getSXBitValueFromByte(value : Int, bit : Int) : Int {
            if (bit in 1..8) {
                val mask = 1.shl(bit-1)
                if ((value and mask) != 0) {
                    return 1
                } else {
                    return 0
                }
            }
            return INVALID_INT
        }
    }
}
