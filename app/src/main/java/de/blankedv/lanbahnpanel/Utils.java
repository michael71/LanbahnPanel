package de.blankedv.lanbahnpanel;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.INVALID_INT;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.TAG;

/** Utils - utility functions
 * 
 * @author Michael Blank
 *
 */
public class Utils {
	/** scale a bitmap both in x and y direction
	 * 
	 * @author Michael Blank
	 * @param bm bitmap turnout resize
	 * @param scale scaling factor (both in x and y direction)
	 * @return re-scaled Bitmap
	 *
	 */
	
	public static Bitmap getResizedBitmap(Bitmap bm, float scale) {

		int width = bm.getWidth();

		int height = bm.getHeight();


		// create a matrix for the manipulation

		Matrix matrix = new Matrix();
		// resize the bit map

		matrix.postScale(scale, scale);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	public static String getDateTime()
	{
	    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
	    //df.setTimeZone(TimeZone.getTimeZone("PST"));
	    return df.format(new Date());
	}
	// svn test comment

	/**
	 * calculate minimum of 3 integers, the first one is always a
	 * valid number, the other can if INVALID_INT (=>not taken into
	 * account) or valid integers, then they are evaluated
	 */
	public static int min(int x, int xt, int x2) {
		int m = x;  // is always defined.
		if (x == INVALID_INT) Log.e(TAG,"Utils.min: x is undefined.");
		if ((xt != INVALID_INT) && (xt<m)) m=xt;
		if ((x2 != INVALID_INT) && (x2<m)) m=x2;
		return m;
	}
	
	/**
	 * calculate maximum of 3 integers, the first one is always a
	 * valid number, the other can if INVALID_INT (=>not taken into
	 * account) or valid integers, then they are evaluated
	 */
	public static int max(int x, int xt, int x2) {
		int m = x;
		if (x == INVALID_INT) Log.e(TAG,"Utils.min: x is undefined.");
		if ((xt != INVALID_INT) && (xt>m)) m=xt;
		if ((x2 != INVALID_INT) && (x2>m)) m=x2;
		return m;
	}
	
	
}
