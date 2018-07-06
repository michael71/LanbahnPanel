package de.blankedv.lanbahnpanel.railroad

import android.os.Handler
import de.blankedv.lanbahnpanel.elements.ActivePanelElement

abstract class GenericClient () {

    abstract fun setChannel(addr : Int, data : Int, peClass : Class<ActivePanelElement>) : String
    abstract fun readChannel(addr : Int, peClass : Class<ActivePanelElement>) : String
    abstract fun readChannel(addr : Int) : String
    abstract fun handleReceive(msg : String, rxHandler : Handler) : Boolean
}