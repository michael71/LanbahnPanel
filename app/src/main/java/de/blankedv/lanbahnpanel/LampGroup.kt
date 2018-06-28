package de.blankedv.lanbahnpanel

import de.blankedv.lanbahnpanel.LanbahnPanelApplication.INVALID_INT
import de.blankedv.lanbahnpanel.LanbahnPanelApplication.bitmaps
import de.blankedv.lanbahnpanel.LanbahnPanelApplication.sendQ

/**
 * a LampGroups consists of a number of individual lamps on an IO16 Bricklet
 * lamps have a unique address and can be set (1 => switched on) or
 * cleared (0 =>switched off)
 */
class LampGroup(pos: Int, a: Int, v: Int) : ControlButton(0.35f + 0.1f * pos, 0.5f, bitmaps["lamp_on"], bitmaps["lamp_off"]) {

    var isOn = false
        private set
    var adr = INVALID_INT
    private var lbValue = 0 //value for "ON"

    init {
        adr = a
        lbValue = v
    }


    fun switchOn() {
        if (isOn)
            return

        isOn = true
        // set all lamps to on
        sendQ.add("SET $adr $lbValue")
    }

    fun switchOff() {
        if (!isOn)
            return

        isOn = false
        // set all lamps to on
        sendQ.add("SET $adr 0")

    }

    fun toggle() {
        if (isOn) {
            switchOff()
        } else {
            switchOn()
        }

    }
}
