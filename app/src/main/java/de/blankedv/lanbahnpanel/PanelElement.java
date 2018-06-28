package de.blankedv.lanbahnpanel;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.linePaint;
import static de.blankedv.lanbahnpanel.LinePaints.linePaint2;

/**
 * generic panel element - this can be a passive (never changing)
 * panel element or an active (lanbahn status dependent) 
 * element.
 * 
 * @author mblank
 * 
 */
public class PanelElement {

	protected String name = "";
	protected int x; // starting point
	protected int y;
	protected int x2 = INVALID_INT; // endpoint - x2 always >x
	protected int y2 = INVALID_INT;
	protected int xt = INVALID_INT; // "thrown" position for turnout
	protected int yt = INVALID_INT;
	protected String route = "";

	public PanelElement(int x, int y) {
		this.x = x;
		this.y = y;
		name = "";
	}

	public PanelElement(Point poi) {

		this.x = poi.x;
		this.y = poi.y;
		name = "";
	}

	public PanelElement(Point poi, Point closed, Point thrown) {
		this.x = poi.x;
		this.y = poi.y;
		this.x2 = closed.x;
		this.y2 = closed.y;
		this.xt = thrown.x;
		this.yt = thrown.y;
		name = "";
	}

	public PanelElement() {
	}

    /** get the type-name which is used in the XML panel definition file
     *
     * @return
     */
	public String getType() {
        String className = this.getClass().getSimpleName();
        switch (className) {
            case "SignalElement":
                return "signal";
            case "PanelElement":
                return "track";
            case "TurnoutElement":
                return "turnout";
            case "SensorElement":
                return "sensor";
            case "ActivePanelElement":
                return "other";
            case "RouteButtonElement":
                return "routebutton";
            case "DoubleSlipElement":
                return "doubleslip";
            default:
                Log.d(TAG,"could not determine type of panel element");
                return "error";

        }

	}

	public void doDraw(Canvas canvas) {

		if (y == y2) { // horizontal line
			// draw a line and not a bitmap
			canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
					* prescale, INSTANCE.getLinePaint());
		} else { // diagonal, draw with round stroke
			canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
					* prescale, INSTANCE.getLinePaint2());
		}

	}

	public boolean isSelected(int lastTouchX, int lastTouchY) {
		return false;
	}

	public void toggle() {
		// do nothing for non changing element
	}

	public int getAdr() {
		return INVALID_INT;
	}

	public int getAdr2() {
		return INVALID_INT;
	}

	public void setAdr(int a) {

	}

	public boolean hasAdrX(int address) {
		return false;
	}

	public void updateData(int data) {
		// do nothing for non changing element
	}

	public int getState() {
		return 0;
	}

	public void setState(int a) {

	}

	public static PanelElement getPeByAddress(int address) {
		for (PanelElement pe : panelElements) {
			if (pe.getAdr() == address) {
				return pe;
			}
		}
		return null;
	}

	/** scale all panel elements for better fit on display and for
	 * possible "upside down" display (=view from other side of the 
	 * layout)
	 * currently only called from readXMLConfigFile (i.e. NOT when
	 * flipUpsideDown is changed in the prefs)
	 */
	public static void scaleAll() {
		
		// in WriteConfig the NEW values are written !!
		
		int xmin = INVALID_INT;
		int xmax = INVALID_INT;
		int ymin = INVALID_INT;
		int ymax = INVALID_INT;
		boolean first = true;
		for (PanelElement pe : panelElements) {
			if (first) {
				xmin = xmax = pe.x;
				ymin = ymax = pe.y;
				first = false;
			}

			if ((pe.x != INVALID_INT) && (pe.x < xmin))
				xmin = pe.x;
			if ((pe.x != INVALID_INT) && (pe.x > xmax))
				xmax = pe.x;
			if ((pe.x2 != INVALID_INT) && (pe.x2 < xmin))
				xmin = pe.x2;
			if ((pe.x2 != INVALID_INT) && (pe.x2 > xmax))
				xmax = pe.x2;
			if ((pe.xt != INVALID_INT) && (pe.xt < xmin))
				xmin = pe.xt;
			if ((pe.xt != INVALID_INT) && (pe.xt > xmax))
				xmax = pe.xt;

			if ((pe.y != INVALID_INT) && (pe.y < ymin))
				ymin = pe.y;
			if ((pe.y != INVALID_INT) && (pe.y > ymax))
				ymax = pe.y;
			if ((pe.y2 != INVALID_INT) && (pe.y2 < ymin))
				ymin = pe.y2;
			if ((pe.y2 != INVALID_INT) && (pe.y2 > ymax))
				ymax = pe.y2;
			if ((pe.yt != INVALID_INT) && (pe.yt < ymin))
				ymin = pe.yt;
			if ((pe.yt != INVALID_INT) && (pe.yt > ymax))
				ymax = pe.yt;

		}

		// now move origin to (20,20)
		for (PanelElement pe : panelElements) {
			if (!flipUpsideDown) {
				if (pe.x != INVALID_INT)
					pe.x = 20 + (pe.x - xmin);
				if (pe.x2 != INVALID_INT)
					pe.x2 = 20 + (pe.x2 - xmin);
				if (pe.xt != INVALID_INT)
					pe.xt = 20 + (pe.xt - xmin);
				if (pe.y != INVALID_INT)
					pe.y = 20 + (pe.y - ymin);
				if (pe.y2 != INVALID_INT)
					pe.y2 = 20 + (pe.y2 - ymin);
				if (pe.yt != INVALID_INT)
					pe.yt = 20 + (pe.yt - ymin);
			} else {
				if (pe.x != INVALID_INT)
					pe.x = 20 + (xmax - pe.x);
				if (pe.x2 != INVALID_INT)
					pe.x2 = 20 + (xmax - pe.x2);
				if (pe.xt != INVALID_INT)
					pe.xt = 20 + (xmax - pe.xt);
				if (pe.y != INVALID_INT)
					pe.y = 20 + (ymax - pe.y);
				if (pe.y2 != INVALID_INT)
					pe.y2 = 20 + (ymax - pe.y2);
				if (pe.yt != INVALID_INT)
					pe.yt = 20 + (ymax - pe.yt);
			}

		}

		if (DEBUG)
			Log.d(TAG, "xmin=" + xmin + " xmax=" + xmax + " ymin=" + ymin
					+ " ymax=" + ymax);
		
		configHasChanged = true;

	}

}
