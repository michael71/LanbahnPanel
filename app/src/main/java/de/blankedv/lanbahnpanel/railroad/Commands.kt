package de.blankedv.lanbahnpanel.railroad

import android.util.Log
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.model.*

class Commands {

    companion object {
        fun readChannel(addr: Int, peClass: Class<*> = Object::class.java) {
            if (addr == INVALID_INT) return
            var cmd = "READ $addr"
            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "readChannel failed, queue full")
            }
        }


        fun setChannel(addr: Int, data: Int, peClass: Class<*> = Object::class.java) {
            if ((addr == INVALID_INT) or (data == INVALID_INT)) return
            var cmd = "SET $addr $data"

            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "setChannel failed, queue full")
            }
        }

        fun setPower(state: Int) {
            var cmd = "SETPOWER "
            when (state) {
                POWER_ON -> cmd += "1"
                POWER_OFF -> cmd += "0"
            }

            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "setPower failed, queue full")
            }
        }

        fun readPower() {
            var cmd = "READPOWER "
            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "readPower failed, queue full")
            }
        }


        fun setLocoData(addr : Int, data : Int) {
            var cmd = "SETLOCO $addr $data"

            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "setLocoData failed, queue full")
            }
        }

        fun readLocoData(addr : Int) {
            if (addr == INVALID_INT) return
            var cmd = "READLOCO $addr"
            val success = sendQ.offer(cmd)
            if (!success && DEBUG) {
                Log.d(TAG, "readChannel failed, queue full")
            }
        }


        /** needed for sx loco control  TODO works only for SX */
        fun requestAllLocoData() {
            for (l in locolist) {
                readLocoData(l.adr)  // TODO works only for SX
            }

        }


    }
}