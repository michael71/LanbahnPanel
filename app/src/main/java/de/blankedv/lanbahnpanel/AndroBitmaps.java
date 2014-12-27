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

/* no longer used, because layout is drawn as vector graphics
   		bitmaps.put("r_e_closed",BitmapFactory.decodeResource(resources, R.drawable.turn_r_e_closed));

		bitmaps.put("r_e_thrown",BitmapFactory.decodeResource(resources, R.drawable.turn_r_e_thrown));
		bitmaps.put("r_e_unknown2",BitmapFactory.decodeResource(resources, R.drawable.turn_r_e_unknown2));
		
		bitmaps.put("l_e_closed",BitmapFactory.decodeResource(resources, R.drawable.turn_l_e_closed));
		bitmaps.put("l_e_thrown",BitmapFactory.decodeResource(resources, R.drawable.turn_l_e_thrown));
		bitmaps.put("l_e_unknown2",BitmapFactory.decodeResource(resources, R.drawable.turn_l_e_unknown2));
		
		bitmaps.put("l_w_closed",BitmapFactory.decodeResource(resources, R.drawable.turn_l_w_closed));
		bitmaps.put("l_w_thrown",BitmapFactory.decodeResource(resources, R.drawable.turn_l_w_thrown));
		bitmaps.put("l_w_unknown2",BitmapFactory.decodeResource(resources, R.drawable.turn_l_w_unknown2));
		
		bitmaps.put("r_w_closed",BitmapFactory.decodeResource(resources, R.drawable.turn_r_w_closed));
		bitmaps.put("r_w_thrown",BitmapFactory.decodeResource(resources, R.drawable.turn_r_w_thrown));
		bitmaps.put("r_w_unknown2",BitmapFactory.decodeResource(resources, R.drawable.turn_r_w_unknown2));
		 
    	bitmaps.put("track_top",BitmapFactory.decodeResource(resources, R.drawable.track_top));
		bitmaps.put("track_bottom",BitmapFactory.decodeResource(resources, R.drawable.track_bottom));
		bitmaps.put("track_l_r",BitmapFactory.decodeResource(resources, R.drawable.track_l_r));
		bitmaps.put("track_l_r_s",BitmapFactory.decodeResource(resources, R.drawable.track_l_r_s));
		bitmaps.put("track_both",BitmapFactory.decodeResource(resources, R.drawable.track_both));
	
 		bitmaps.put("s_on",BitmapFactory.decodeResource(resources, R.drawable.s_on));
 		bitmaps.put("s_off",BitmapFactory.decodeResource(resources, R.drawable.s_off));

		bitmaps.put("loco",  BitmapFactory.decodeResource(resources, R.drawable.loco_s));
		
		bitmaps.put("loco1",  BitmapFactory.decodeResource(resources, R.drawable.f7_2_s));
		bitmaps.put("loco0",  BitmapFactory.decodeResource(resources, R.drawable.f7_2_l));
		
		bitmaps.put("button120",  BitmapFactory.decodeResource(resources, R.drawable.button120));
		bitmaps.put("button100",  BitmapFactory.decodeResource(resources, R.drawable.button100));
		
		bitmaps.put("stop_s_on", BitmapFactory.decodeResource(resources, R.drawable.stop_s_on));
		bitmaps.put("stop_s_off", BitmapFactory.decodeResource(resources, R.drawable.stop_s_off));

		bitmaps.put("lamp1",BitmapFactory.decodeResource(resources, R.drawable.lamp1btn));
		bitmaps.put("lamp0",BitmapFactory.decodeResource(resources, R.drawable.lamp0btn));
	
		bitmaps.put("func1",  BitmapFactory.decodeResource(resources, R.drawable.func1));
		bitmaps.put("func0",  BitmapFactory.decodeResource(resources, R.drawable.func0));
		
		bitmaps.put("bump",  BitmapFactory.decodeResource(resources, R.drawable.bump));
		
		bitmaps.put("slider",  BitmapFactory.decodeResource(resources, R.drawable.slider));
		bitmaps.put("slider_grey",  BitmapFactory.decodeResource(resources, R.drawable.slider_grey));
		
		*/
		

	}

	
}
