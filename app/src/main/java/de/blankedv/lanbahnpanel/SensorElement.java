package de.blankedv.lanbahnpanel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;
import static de.blankedv.lanbahnpanel.LinePaints.*;


public class SensorElement extends ActivePanelElement {
	
	public SensorElement(int x, int y, String name, int adr) {
		super(x, y, name,  adr);
        
	}
	
	public SensorElement() {
		super();
	}

	@Override
	public void doDraw(Canvas canvas) {

		if (x2 != INVALID_INT) {  // draw dashed line as sensor
			// read data from SX bus and set red/gray dashed line accordingly

			if (state == STATE_FREE) {
				
					canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
							* prescale, linePaintGrayDash);

			} else if (state == STATE_OCCUPIED) {
				
					canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
							* prescale,  linePaintRedDash);
	

			}  else if (state == STATE_INROUTE) {
				
				    canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
						* prescale, linePaintDarkYellowDash);
				

			}
			else if (state == STATE_UNKNOWN) {
				
					canvas.drawLine(x * prescale, y * prescale, x2 * prescale, y2
							* prescale, linePaintGrayDash);
			}
		} else {
			// draw lamp type of sensor   s_on.png etc

			int h, w;
			Bitmap bm;

			StringBuilder bmName = new StringBuilder("sensor");

			if ( (state == STATE_FREE)  || (state == STATE_UNKNOWN)){
				bmName.append("_off");
			} else {
				bmName.append("_on");
			}

			bm = bitmaps.get(bmName.toString());
			if (bm == null) {
                Log.e(TAG,
                        "error, bitmap not found with name="
                                + bmName.toString());
            } else {
                h = bm.getHeight() / 2;
                w = bm.getWidth() / 2;
                canvas.drawBitmap(bm, x * prescale - w, y * prescale - h, null); // center
                // bitmap
            }
		}
		if (drawAddresses2)
			doDrawAddresses(canvas);
	}
	

}
