package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.bitmaps;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.sendQ;

/**
 * a LampGroups consists of a number of individual lamps on an IO16 Bricklet
 * lamps have a unique address and can be set (1 => switched on) or 
 * cleared (0 =>switched off)
 */
public class LampGroup {

	public ControlButton btn;
	public boolean isOn = false;
	public int btnPos;   // position in the row of lamp buttons

	int[] lampAddrs;

	public LampGroup(int btnPos, int[] lamps) {
		btn = new ControlButton(0.35f + (0.1f * btnPos), 0.5f,
				bitmaps.get("lamp_on"), bitmaps.get("lamp_off"));
		lampAddrs = lamps;
		this.btnPos = btnPos;
	}

	public void switchOn() {
		if (isOn)
			return;

		isOn = true;
		// set all lamps to on
		for (int i : lampAddrs) {
			sendQ.add("SET " + i + " 1");
		}

	}

	public void switchOff() {
		if (!isOn)
			return;

		isOn = false;
		// set all lamps to on
		for (int i : lampAddrs) {
			sendQ.add("SET " + i + " 0");
		}

	}

	public void toggle() {
		if (isOn) {
			switchOff();
		} else {
			switchOn();
		}

	}
}
