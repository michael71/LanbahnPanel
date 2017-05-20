package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.INVALID_INT;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.bitmaps;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.sendQ;

/**
 * a LampGroups consists of a number of individual lamps on an IO16 Bricklet
 * lamps have a unique address and can be set (1 => switched on) or 
 * cleared (0 =>switched off)
 */
public class LampGroup extends ControlButton {

	private boolean isOn = false;
    private int lbAddr = INVALID_INT;
	private int lbValue = 0 ; //value for "ON"

	public LampGroup(int pos, int a, int v) {
		super(0.35f + (0.1f * pos), 0.5f,
				bitmaps.get("lamp_on"), bitmaps.get("lamp_off"));
		lbAddr = a;
		lbValue = v;
		}


	public void switchOn() {
		if (isOn)
			return;

		isOn = true;
		// set all lamps to on
		sendQ.add("SET " + lbAddr + " " + lbValue);
	}

	public void switchOff() {
		if (!isOn)
			return;

		isOn = false;
		// set all lamps to on
		sendQ.add("SET " + lbAddr + " " + 0);

	}

	public boolean isOn() {
        return isOn;
    }

	public int getAdr() {
        return lbAddr;
    }

	public void toggle() {
		if (isOn) {
			switchOff();
		} else {
			switchOn();
		}

	}
}
