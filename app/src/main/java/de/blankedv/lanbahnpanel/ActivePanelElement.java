package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

/**
 * all active panel elements, like turnouts, signals, trackindicators (=sensors)
 * are derviced from this class. These elements have a "state" which is exactly
 * the same number as the "data" of the lanbahn messages "S 810 2" => set state
 * of panel element with address=810 to state=2
 * 
 * a panel element has only 1 address (=> double slips are 2 panel elements)
 * 
 * @author mblank
 * 
 */
public abstract class ActivePanelElement extends PanelElement {

	// these constants are defined just for easier understanding of the
	// methods of the classes derived from this class
	
	// turnouts
	protected static final int STATE_CLOSED = 0;
	protected static final int STATE_THROWN = 1;

	// signals
	protected static final int STATE_RED = 0;
	protected static final int STATE_GREEN = 1;
	protected static final int STATE_YELLOW = 2;
	protected static final int STATE_YELLOW_FEATHER = 3;
	
	// buttons
	protected static final int STATE_NOT_PRESSED = 0;
	protected static final int STATE_PRESSED = 1;

	// sensors
	protected static final int STATE_FREE = 0;
	protected static final int STATE_OCCUPIED = 1;
	protected static final int STATE_INROUTE = 2;

	protected static final int STATE_UNKNOWN = INVALID_INT;

	protected int state;
	protected int adr = INVALID_INT;
	protected long lastToggle = 0L;
	protected long lastUpdateTime = 0L;

	public ActivePanelElement() {
		super(null, 0, 0);
	}

	/**
	 * constructor for an ACTIVE panel element with 1 address default state is
	 * "CLOSED" (="RED")
	 * 
	 * @param type
	 * @param x
	 * @param y
	 * @param name
	 * @param adr
	 */
	public ActivePanelElement(String type, int x, int y, String name, int adr) {
		super(null, x, y);
		this.type = type;
		this.state = STATE_UNKNOWN;
		this.adr = adr;
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public int getAdr() {
		return adr;
	}
	
	@Override
	public int getState() {
		return state;
	}

	@Override
	public boolean hasAdrX(int address) {
		if (adr == address) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setAdr(int adr) {
		this.adr = adr;
		this.state = STATE_UNKNOWN;
		this.lastUpdateTime = System.currentTimeMillis();
		if (adr != INVALID_INT) {
			sendQ.add("READ " + adr); // request update for this element
		}
	}

	@Override
	public void setState(int state) {
		this.state = state;
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public void updateData(int data) {
        if (data == INVALID_LANBAHN_DATA) {
            state = STATE_UNKNOWN;
        } else {
            state = data;
        }
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public boolean isSelected(int xs, int ys) {
		// check only for active elements
		// search x for range in x..(x+/-w)
		// search y for range in y..(y+/-h)

		Rect rect = getRect();

		// the touchpoint should be within rectangle of panel element
		// similar  rect.contains() methods, BUT the lines of the rect are
		// both included in the allowed area
		if ((xs >= rect.left) && (xs <= rect.right) && (ys >= rect.top)
				&& (ys <= rect.bottom)) {
			if (DEBUG)
				Log.d(TAG, "selected adr=" + adr + " " + type + "  (" + x + ","
						+ y + ") in rect=" + rect.toString());
			return true;
		} else {
			// if (DEBUG) Log.d(TAG, "NO sel.  adr=" + adr + " " + type +
			// " not in rect="+ rect.toString());
			return false;
		}
	}

	protected Rect getRect() {
		if (this instanceof SignalElement)  {
			return new Rect(x - RASTER / 5, y - RASTER / 7, x + RASTER / 5, y
					+ RASTER / 7);
		} else if (this instanceof SensorElement) {
			if (x2 == INVALID_INT) {  // dot type sensor
				return new Rect(x - RASTER / 5, y - RASTER / 7, x + RASTER / 5, y
						+ RASTER / 7);
			} else {   // line type sensor
			return new Rect((x+x2)/2 - RASTER / 5, (y+y2)/2 - RASTER / 7, (x+x2)/2 + RASTER / 5, 
					(y+y2)/2 + RASTER / 7);

			}
		} else {
			// Rect rect = new Rect(left, top, right, bottom)
			int minx = Utils.min(x, xt, x2);
			int maxx = Utils.max(x, xt, x2);

            //noinspection SuspiciousNameCombination
            int miny = Utils.min(y, yt, y2);
			int maxy = Utils.max(y, yt, y2);

			// Rect rect = new Rect(left, top, right, bottom)
			return new Rect(minx, miny, maxx, maxy);
		}
	}

	public Rect prescaleRect(Rect r) {
		r.top = r.top * prescale;
		r.bottom = r.bottom * prescale;
		r.left = r.left * prescale;
		r.right = r.right * prescale;
		return r;
	}

	protected void doDrawAddresses(Canvas canvas) {

		Rect bounds = new Rect();
		String txt;
		if (adr == INVALID_INT) {
			txt = "???";
		} else {
			txt = "" + adr;
		}
		addressPaint.getTextBounds(txt, 0, txt.length(), bounds);
		int text_height = bounds.height();
		int text_width = bounds.width();

		Rect pre = prescaleRect(getRect());
		canvas.drawRect(pre, addressBGPaint); // dark rectangle
		canvas.drawText(txt, pre.left + text_width / 8, pre.top + 3
				* text_height / 2, addressPaint); // the
		// numbers

	}

	/**
	 * 
	 * @return true, if the state of this element has not been communicated
	 *               during in the last 20 seconds
	 */
	public boolean isExpired() {
		return ((System.currentTimeMillis() - lastUpdateTime) > 20 * 1000);
	}

}
