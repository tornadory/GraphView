/**
 * This file is part of GraphView.
 * 
 * GraphView is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GraphView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with GraphView. If not, see
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Copyright Jonas Gehring
 */

package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Line Graph View. This draws a line chart.
 */
public class LineGraphView extends GraphView {
	private final Paint paintBackground;
	private boolean drawBackground;
	private boolean drawDataPoints;
	private boolean drawDataPointIndex;
	private float dataPointsRadius = 10f;
	private Paint labelPaint;

	public LineGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		init();
	}

	public LineGraphView(Context context, String title) {
		super(context, title);
		paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		init();
	}

	private void init() {
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);

		labelPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight,
			float border, double minX, double minY, double diffX, double diffY, float horstart,
			GraphViewSeriesStyle style) {
		// draw background
		float lastY = 0;
		float lastX = 0;
		float lastUnmodEndX = 0;
		float lastUnmodEndY = 0;

		// draw data
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);

		labelPaint.setColor(style.textColor);
		labelPaint.setTextAlign(Align.CENTER);
		labelPaint.setTextSize(dataPointsRadius);

		if (drawDataPoints) {
			horstart += dataPointsRadius;
			graphwidth -= dataPointsRadius * 2 + style.thickness;
		}

		Path bgPath = null;
		if (drawBackground) {
			bgPath = new Path();
		}

		lastY = 0;
		lastX = 0;
		float firstX = 0;
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].getX() - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastX + (horstart + 1);
				float startY = (float) (border - lastY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight;

				// draw data point
				if (drawDataPoints) {
					drawDataPoint(canvas, labelPaint, i, startX, startY);
					// Save the unmodified points for the last data point circle
					lastUnmodEndX = endX;
					lastUnmodEndY = endY;
					// We don't want to the line draw inside our circle
					double theta = Math.atan2(endY - startY, endX - startX);
					double dX = Math.cos(theta) * dataPointsRadius;
					double dY = Math.sin(theta) * dataPointsRadius;
					startX += dX;
					startY += dY;
					endX -= dX;
					endY -= dY;
				}

				canvas.drawLine(startX, startY, endX, endY, paint);
				if (bgPath != null) {
					if (i == 1) {
						firstX = startX;
						bgPath.moveTo(startX, startY);
					}
					bgPath.lineTo(endX, endY);
				}
			}
			lastY = (float) y;
			lastX = (float) x;
		}

		if (drawDataPoints) {
			// Draw the very last point
			drawDataPoint(canvas, labelPaint, values.length, lastUnmodEndX, lastUnmodEndY);
		}

		if (bgPath != null) {
			// end / close path
			bgPath.lineTo(lastX, graphheight + border);
			bgPath.lineTo(firstX, graphheight + border);
			bgPath.close();
			canvas.drawPath(bgPath, paintBackground);
		}
	}

	private void drawDataPoint(Canvas canvas, Paint labelPaint, int index, float x, float y) {
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(x, y, dataPointsRadius, paintBackground); // Background
		canvas.drawCircle(x, y, dataPointsRadius, paint); // Stroke
		if (drawDataPointIndex) {
			String label = String.valueOf(index);
			Rect bounds = new Rect();
			labelPaint.getTextBounds(label, 0, label.length(), bounds);
			canvas.drawText(label, x, y + bounds.height() / 2, labelPaint); // Text
		}
	}

	public int getBackgroundColor() {
		return paintBackground.getColor();
	}

	public float getDataPointsRadius() {
		return dataPointsRadius;
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}

	public boolean getDrawDataPoints() {
		return drawDataPoints;
	}

	@Override
	public void setBackgroundColor(int color) {
		paintBackground.setColor(color);
	}

	public void setDataPointsRadius(float dataPointsRadius) {
		this.dataPointsRadius = dataPointsRadius;
	}

	/**
	 * @param drawBackground
	 *            true for a light blue background under the graph line
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

	public void setDrawDataPoints(boolean drawDataPoints) {
		this.drawDataPoints = drawDataPoints;
	}

	public boolean isDrawDataPointIndex() {
		return drawDataPointIndex;
	}

	public void setDrawDataPointIndex(boolean drawDataPointIndex) {
		this.drawDataPointIndex = drawDataPointIndex;
	}

}
