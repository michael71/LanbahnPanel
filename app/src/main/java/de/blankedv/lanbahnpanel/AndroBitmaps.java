package de.blankedv.lanbahnpanel;

import de.blankedv.lanbahnpanel.R;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.bitmaps;

public class AndroBitmaps {
	
	public static void init(Resources resources)  {
		
		bitmaps.put("greendot", BitmapFactory.decodeResource(resources, R.drawable.greendot));
		bitmaps.put("reddot", BitmapFactory.decodeResource(resources, R.drawable.reddot));
		bitmaps.put("greydot", BitmapFactory.decodeResource(resources, R.drawable.greydot));
		
		bitmaps.put("commok",BitmapFactory.decodeResource(resources, R.drawable.commok));
		bitmaps.put("nocomm",BitmapFactory.decodeResource(resources, R.drawable.nocomm));

		bitmaps.put("sensor_on",BitmapFactory.decodeResource(resources, R.drawable.led_red_2));
		bitmaps.put("sensor_off",BitmapFactory.decodeResource(resources, R.drawable.led_off_2));
		
		bitmaps.put("lamp_on",BitmapFactory.decodeResource(resources, R.drawable.lamp1));
		bitmaps.put("lamp_off",BitmapFactory.decodeResource(resources, R.drawable.lamp0));
		
		
		bitmaps.put("incr",BitmapFactory.decodeResource(resources, R.drawable.incr));
		bitmaps.put("decr",BitmapFactory.decodeResource(resources, R.drawable.decr));
		
		bitmaps.put("lock",  BitmapFactory.decodeResource(resources, R.drawable.lock_s));
		bitmaps.put("unlock",  BitmapFactory.decodeResource(resources, R.drawable.unlock_s));
		
		bitmaps.put("clearrouteson",  BitmapFactory.decodeResource(resources, R.drawable.clearrouteson));
		bitmaps.put("clearroutesoff",  BitmapFactory.decodeResource(resources, R.drawable.clearroutesoff));
		
		bitmaps.put("lonstokewest",  BitmapFactory.decodeResource(resources, R.drawable.lonstokewest));


	}

	
}
