package de.blankedv.lanbahnpanel;

import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.TURNOUT_LENGTH;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.TURNOUT_LENGTH_LONG;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.DEBUG;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.TAG;
import android.graphics.Point;
import android.util.Log;

public class LinearMath {

	/**
	 * Computes the intersection between two lines. The calculated point is
	 * approximate, since integers are used. If you need a more precise result,
	 * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
	 * license). http://www.ahristov.com (c) 2012 Michael Blank, for lines with
	 * endpoints
	 * 
	 * @param x1
	 *            Point 1 of Line 1
	 * @param y1
	 *            Point 1 of Line 1
	 * @param x2
	 *            Point 2 of Line 1
	 * @param y2
	 *            Point 2 of Line 1
	 * @param x3
	 *            Point 1 of Line 2
	 * @param y3
	 *            Point 1 of Line 2
	 * @param x4
	 *            Point 2 of Line 2
	 * @param y4
	 *            Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	public static PanelElement trackIntersect(PanelElement e, PanelElement f) {

		// only look for crossing track elements
		if (!e.getType().equals("track") || (!f.getType().equals("track")))
			return null;

		int x1, y1, x2, y2, x3, y3, x4, y4;

		x1 = e.x;
		y1 = e.y;
		x2 = e.x2;
		y2 = e.y2;

		x3 = f.x;
		y3 = f.y;
		x4 = f.x2;
		y4 = f.y2;

		int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0)
			return null;

		int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2)
				* (x3 * y4 - y3 * x4))
				/ d;
		int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2)
				* (x3 * y4 - y3 * x4))
				/ d;
		Point px = new Point(xi, yi);

		// additional code by Michael Blank
		// check if within limit of lines
		int xt, yt, xc, yc; // for turnout x-thrown, y-thrown, x-closed,
							// y-closed
		if ((xi >= Math.min(x1, x2)) && (xi <= Math.max(x1, x2)) // within
																	// x-limits
																	// of first
																	// line
				&& (xi >= Math.min(x3, x4)) && (xi <= Math.max(x3, x4)) // within
																		// x-limits
																		// of
																		// second
																		// line
				&& (yi >= Math.min(y1, y2)) && (yi <= Math.max(y1, y2)) // within
																		// y-limits
																		// of
																		// first
																		// line
				&& (yi >= Math.min(y3, y4)) && (yi <= Math.max(y3, y4))) { // within
																			// y-limits
																			// of
																			// second
																			// line

			// check if point is not endpoint of both tracks => no turnout
			if (((xi == e.x) && (yi == e.y)) || // startpoint of e ( e=thrown,
												// f=closed)
					((xi == e.x2) && (yi == e.y2))) { // endpoint of e =>
														// delta-x immer negativ
				// start/endpoint of e
				if (((xi == f.x) && (yi == f.y))
						|| ((xi == f.x2) && (yi == f.y2))) {
					// AND start/endpoint of f
					return null; // => no turnout
				}
			}

			// check if point is
			// not endpoint of first track
			// and not endpoint of second track
			// => double crossover
			boolean doubleslip = true;
			if (((xi == e.x) && (yi == e.y)) || // startpoint of e ( e=thrown,
												// f=closed)
					((xi == e.x2) && (yi == e.y2))) { // endpoint of e =>
														// delta-x immer negativ
				doubleslip = false;
			}
			// start/endpoint of f
			if (((xi == f.x) && (yi == f.y)) || ((xi == f.x2) && (yi == f.y2))) {
				doubleslip = false;
			}

			if ((DEBUG) && (doubleslip))
				Log.d(TAG, "LinMath: found doubleslip at (" + xi + "," + yi
						+ ")");

			if (doubleslip == false) {
				// =========== this is a turnout !! ======================
				// find closed and thrown positions (x2>x) both for e and f !!
				// 1. check, turnout which lines' endpoint (xi,yi) belongs

				xc = xt = xi;
				yc = yt = yi;
				if ((xi == e.x) && (yi == e.y)) { // startpoint of e ( e=thrown,
													// f=closed)
					if ((e.y2 - e.y) != 0) { // y steigung immer = +/- 1
						if (e.y2 > e.y) {
							xt = xi + TURNOUT_LENGTH;
							yt = yi + TURNOUT_LENGTH;
						} else {
							xt = xi + TURNOUT_LENGTH;
							yt = yi - TURNOUT_LENGTH;
						}
						xc = xi + (int) (TURNOUT_LENGTH_LONG);
						yc = yi;
					} else { // steigung == 0
						xt = xi + (int) (TURNOUT_LENGTH_LONG);
						yt = yi;
						if (f.y2 > f.y) {
							xc = xi + TURNOUT_LENGTH;
							yc = yi + TURNOUT_LENGTH;
						} else {
							xc = xi + TURNOUT_LENGTH;
							yc = yi - TURNOUT_LENGTH;
						}
					}
				} else if ((xi == e.x2) && (yi == e.y2)) { // endpoint of e =>
															// delta-x immer
															// negativ
					if ((e.y2 - e.y) != 0) { // y steigung immer = +/- 1
						if (e.y2 > e.y) {
							xt = xi - TURNOUT_LENGTH;
							yt = yi - TURNOUT_LENGTH;
						} else {
							xt = xi - TURNOUT_LENGTH;
							yt = yi + TURNOUT_LENGTH;
						}
						xc = xi - (int) (TURNOUT_LENGTH_LONG);
						yc = yi;
					} else { // steigung == 0
						xt = xi - (int) (TURNOUT_LENGTH_LONG);
						yt = yi;
						if (f.y2 > f.y) {
							xc = xi - TURNOUT_LENGTH;
							yc = yi + TURNOUT_LENGTH;
						} else {
							xc = xi - TURNOUT_LENGTH;
							yc = yi - TURNOUT_LENGTH;
						}
					}
				} else if ((xi == f.x) && (yi == f.y)) { // startpoint of f
					if ((f.y2 - f.y) != 0) { // y steigung immer = +/- 1
						if (f.y2 > f.y) {
							xt = xi + TURNOUT_LENGTH;
							yt = yi + TURNOUT_LENGTH;
						} else {
							xt = xi + TURNOUT_LENGTH;
							yt = yi - TURNOUT_LENGTH;
						}
						xc = xi + (int) (TURNOUT_LENGTH_LONG);
						yc = yi;
					} else { // steigung == 0
						xt = xi + TURNOUT_LENGTH_LONG;
						yt = yi;
						if (e.y2 > e.y) {
							xc = xi + TURNOUT_LENGTH;
							yc = yi + TURNOUT_LENGTH;
						} else {
							xc = xi + TURNOUT_LENGTH;
							yc = yi - TURNOUT_LENGTH;
						}
					}
				} else if ((xi == f.x2) && (yi == f.y2)) { // endpoint of f
					if ((f.y2 - f.y) != 0) { // y steigung immer = +/- 1
						if (f.y2 > f.y) {
							xt = xi - TURNOUT_LENGTH;
							yt = yi - TURNOUT_LENGTH;
						} else {
							xt = xi - TURNOUT_LENGTH;
							yt = yi + TURNOUT_LENGTH;
						}
						xc = xi - (int) (TURNOUT_LENGTH_LONG);
						yc = yi;
					} else { // steigung == 0
						xt = xi - (int) (TURNOUT_LENGTH_LONG);
						yt = yi;
						if (e.y2 > e.y) {
							xc = xi - TURNOUT_LENGTH;
							yc = yi - TURNOUT_LENGTH;
						} else {
							xc = xi - TURNOUT_LENGTH;
							yc = yi + TURNOUT_LENGTH;
						}
					}
				}
				return new PanelElement("turnout", px, new Point(xc, yc),
						new Point(xt, yt));
			} else {
				return new PanelElement("doubleslip", px, px, px);
			}
		} else {
			return null; // for debugging: new PanelElement("turnok", px);
		}
	}

}
