package de.blankedv.lanbahnpanel.railroad.loconet

import android.widget.Button

open class DCC_Data (val address : Int, val data : Int )




data class LocoSlot(val locoAddr: Int, val slotId : Int, val state : Int)

data class SlotSpeed(val slotId : Int, val speed : Int) {
    override fun toString(): String {
        return "Loco Slot#"+slotId+" v="+speed
    }
}

data class UIKey(val btn : Button, val command: String, val text: String)


