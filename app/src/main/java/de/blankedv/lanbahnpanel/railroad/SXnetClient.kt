package de.blankedv.lanbahnpanel.railroad

import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
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
      return "READ $addr"
    }

    override fun readChannel(addr: Int) : String {
        return "READ $addr"   // general lanbahn address range
    }

    // NOT USED HERE
    fun readSXChannel(addr: Int) : String {
        return "R $addr"   // specific sx message
    }

    override fun setChannel(addr: Int, data: Int, peClass : Class<*>) :String {
        // class can be ignored for selectrix (sxnet)
        return "SET $addr $data"
    }

    override fun setChannel(addr: Int, data: Int) :String {
        return "SET $addr $data"
    }

    override fun setPowerState (switchOn : Boolean) : String {
        if (switchOn) {
            return "SET $POWER_CHANNEL 1"
        } else {
            return "SET $POWER_CHANNEL 0"
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

        var adr = INVALID_INT
        var data: Int

        // new code: multiple commands in a single message, separated by ';'
        // example: String msg = "S 780 2 ;; S 800 3  ;  S 720 1";
        val allcmds = msg.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cmd in allcmds) {
            //Log.d(TAG,"single cmd="+cmd);
            if (!cmd.contains("ERROR")) {
                info = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // all lanbahn feedback message have the Format "XL <adr> <data>"
                //                          pure SX (=andropanel) has "X" message
                // adr range 1 ... 9999, data range 0 ..255
                // adr=1000 is the sxnet/lanbahn "Power" channel
                if (info.size >= 3 && info[0] == "XL") {
                    adr = extractChannelFromString(info[1])
                    data = extractDataFromString(info[2])
                    if (adr != INVALID_INT && data != INVALID_INT) {
                        val m = Message.obtain()
                        if (adr == POWER_CHANNEL) {
                            m.what = TYPE_POWER_MSG
                        } else {
                            m.what = TYPE_GENERIC_MSG
                        }
                        m.arg1 = adr
                        m.arg2 = data
                        recHandler.sendMessage(m)  // send SX data to UI Thread via Message
                    } else {
                        Log.e(TAG, "range error in rec. msg, cmd=$cmd adr=$adr data=$data")
                    }
                } else if (info.size >= 3 && info[0] == "X") {
                    adr = extractChannelFromString(info[1])
                    data = extractDataFromString(info[2])
                    if (adr != INVALID_INT && data != INVALID_INT) {
                        val m = Message.obtain()
                        m.what = TYPE_SX_MSG
                        m.arg1 = adr
                        m.arg2 = data
                        recHandler.sendMessage(m)  // send SX data to UI Thread via Message
                    } else {
                        Log.e(TAG, "range error in rec. msg, cmd=$cmd adr=$adr data=$data")
                    }
                } else if (info.size >= 3 && info[0] == "RT") {
                    adr = extractChannelFromString(info[1])
                    data = extractDataFromString(info[2])
                    if (adr != INVALID_INT && data != INVALID_INT) {
                        val m = Message.obtain()
                        m.what = TYPE_ROUTE_MSG
                        m.arg1 = adr
                        m.arg2 = data
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
    private fun extractDataFromString(s: String): Int {
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
     * check if address (=channel) is in valid range for selectrix
     */
    private fun extractChannelFromString(s: String): Int {

        try {
            var channel = Integer.parseInt(s)
            if ((channel in LBMIN..LBMAX) or (channel == POWER_CHANNEL)) {
                return channel
            } else {
                return INVALID_INT
            }
        } catch (e: Exception) {
            return INVALID_INT
        }


    }


}
