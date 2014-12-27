package de.blankedv.lanbahnpanel;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

public class LinePaints {
	// define Paints
	public static Paint linePaint, linePaint2, rasterPaint, circlePaint, btn0Paint, btn1Paint,
			greenPaint, redPaint, linePaintRedDash, linePaintGrayDash, linePaintLightYellowDash, linePaintDarkYellowDash;
	public static int BG_COLOR;
	public static Paint bgPaint, addressBGPaint, signalLine, greyPaint,
			whitePaint, greenSignal, redSignal, yellowPaint, yellowSignal;
	public static TextPaint addressPaint, panelNamePaint; // used for displaying
															// SX address on
															// panel and for
															// panel Name

	public static void init(int prescale) {

		BG_COLOR = Color.DKGRAY; // panel background color

		linePaint = new Paint();
		linePaint.setColor(Color.WHITE);
		linePaint.setStrokeWidth(4.5f * prescale);
		linePaint.setAntiAlias(true);
		linePaint.setDither(true);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		// linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

		signalLine = new Paint();
		signalLine.setColor(Color.WHITE);
		signalLine.setStrokeWidth(2.0f * prescale);
		signalLine.setAntiAlias(true);
		signalLine.setDither(true);
		signalLine.setStyle(Paint.Style.STROKE);
		signalLine.setStrokeCap(Paint.Cap.SQUARE);
		// linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

		linePaintRedDash = new Paint();
		linePaintRedDash.setColor(Color.RED);
		linePaintRedDash.setStrokeWidth(3.5f * prescale);
		linePaintRedDash.setAntiAlias(true);
		linePaintRedDash.setDither(true);
		linePaintRedDash.setStyle(Paint.Style.STROKE);
		linePaintRedDash.setStrokeCap(Paint.Cap.SQUARE);
		linePaintRedDash.setPathEffect(new DashPathEffect(
				new float[] { 10, 20 }, 0));

		linePaintGrayDash = new Paint(linePaintRedDash);
		linePaintGrayDash.setColor(0xffaaaaaa);
		
		linePaintDarkYellowDash = new Paint(linePaintRedDash);
		linePaintDarkYellowDash.setColor(Color.YELLOW);
		
		linePaintLightYellowDash = new Paint(linePaintRedDash);
		linePaintLightYellowDash.setColor(Color.YELLOW); // TODO 0xffaaaa00);

		linePaint2 = new Paint();
		linePaint2.setColor(Color.WHITE);
		linePaint2.setStrokeWidth(4.5f * prescale);
		linePaint2.setAntiAlias(true);
		linePaint2.setDither(true);
		linePaint2.setStyle(Paint.Style.STROKE);
		linePaint2.setStrokeCap(Paint.Cap.ROUND);

		rasterPaint = new Paint();
		rasterPaint.setColor(Color.LTGRAY);
		rasterPaint.setAntiAlias(true);
		rasterPaint.setDither(true);

		circlePaint = new Paint();
		circlePaint.setColor(0x88ff2222);
		circlePaint.setAntiAlias(true);
		circlePaint.setDither(true);

		greenPaint = new Paint();
		greenPaint.setColor(0xcc00ff00);
		greenPaint.setAntiAlias(true);
		greenPaint.setStrokeWidth(4.5f * prescale);
		greenPaint.setDither(true);
		greenPaint.setStyle(Paint.Style.STROKE);
		greenPaint.setStrokeCap(Paint.Cap.ROUND);

		greenSignal = new Paint(greenPaint);
		greenSignal.setStyle(Paint.Style.FILL);
		
		yellowPaint = new Paint();
		yellowPaint.setColor(0xccffff00);
		yellowPaint.setAntiAlias(true);
		yellowPaint.setStrokeWidth(4.5f * prescale);
		yellowPaint.setDither(true);
		yellowPaint.setStyle(Paint.Style.STROKE);
		yellowPaint.setStrokeCap(Paint.Cap.ROUND);

		yellowSignal = new Paint(yellowPaint);
		yellowSignal.setStyle(Paint.Style.FILL);
		

		redPaint = new Paint();
		redPaint.setColor(0xccff0000);
		redPaint.setStrokeWidth(4.5f * prescale);
		redPaint.setAntiAlias(true);
		redPaint.setDither(true);
		redPaint.setStyle(Paint.Style.STROKE);
		redPaint.setStrokeCap(Paint.Cap.ROUND);

		redSignal = new Paint(redPaint);
		redSignal.setStyle(Paint.Style.FILL);

		greyPaint = new Paint();
		greyPaint.setColor(Color.GRAY);
		greyPaint.setStrokeWidth(4.5f * prescale);
		greyPaint.setAntiAlias(true);
		greyPaint.setDither(true);
		greyPaint.setStyle(Paint.Style.STROKE);
		greyPaint.setStrokeCap(Paint.Cap.ROUND);

		whitePaint = new Paint();
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStrokeWidth(4.5f * prescale);
		whitePaint.setAntiAlias(true);
		whitePaint.setDither(true);
		whitePaint.setStyle(Paint.Style.STROKE);
		whitePaint.setStrokeCap(Paint.Cap.ROUND);
		
		btn0Paint = new Paint();
		btn0Paint.setColor(Color.GRAY);
		btn0Paint.setStrokeWidth(6f * prescale);
		btn0Paint.setAntiAlias(true);
		btn0Paint.setDither(true);
		btn0Paint.setStyle(Paint.Style.STROKE);
		btn0Paint.setStrokeCap(Paint.Cap.ROUND);
		
		btn1Paint = new Paint();
		btn1Paint.setColor(Color.WHITE);
		btn1Paint.setStrokeWidth(6f * prescale);
		btn1Paint.setAntiAlias(true);
		btn1Paint.setDither(true);
		btn1Paint.setStyle(Paint.Style.STROKE);
		btn1Paint.setStrokeCap(Paint.Cap.ROUND);

		bgPaint = new Paint();
		bgPaint.setColor(BG_COLOR);
		bgPaint.setAntiAlias(true);
		bgPaint.setStrokeWidth(3.8f * prescale);
		bgPaint.setDither(true);
		bgPaint.setStyle(Paint.Style.STROKE);
		bgPaint.setStrokeCap(Paint.Cap.BUTT);

		addressPaint = new TextPaint();
		addressPaint.setColor(Color.YELLOW);
		addressPaint.setTextSize(7 * prescale);
		addressPaint.setStyle(Style.FILL);

		addressBGPaint = new Paint();
		addressBGPaint.setColor(Color.DKGRAY);
		addressBGPaint.setAlpha(175);

		panelNamePaint = new TextPaint();
		panelNamePaint.setColor(Color.LTGRAY);
		panelNamePaint.setTextSize(12 * prescale);
		panelNamePaint.setStyle(Style.FILL);

		if (selectedStyle.equals("DE")) {

			linePaint.setColor(Color.BLACK);

			signalLine.setColor(Color.BLACK);

			linePaint2.setColor(Color.BLACK);

			rasterPaint.setColor(Color.LTGRAY);
			rasterPaint.setAntiAlias(true);
			rasterPaint.setDither(true);

			greyPaint.setColor(Color.GRAY);

			whitePaint.setColor(Color.BLACK);
			BG_COLOR = Color.LTGRAY;

			bgPaint.setColor(BG_COLOR);

			addressPaint.setColor(Color.YELLOW);

			addressBGPaint.setColor(Color.DKGRAY);

			panelNamePaint.setColor(Color.BLACK);

		} else 	if (selectedStyle.equals("UK")) {

			linePaint.setColor(Color.BLACK);

			signalLine.setColor(Color.BLACK);

			linePaint2.setColor(Color.BLACK);

			rasterPaint.setColor(Color.LTGRAY);
			rasterPaint.setAntiAlias(true);
			rasterPaint.setDither(true);

			greyPaint.setColor(Color.GRAY);

			whitePaint.setColor(Color.BLACK);
			BG_COLOR = 0xff306630;
			

			bgPaint.setColor(BG_COLOR);

			addressPaint.setColor(Color.YELLOW);

			addressBGPaint.setColor(Color.DKGRAY);

			panelNamePaint.setColor(Color.BLACK);

		}
	}

}
