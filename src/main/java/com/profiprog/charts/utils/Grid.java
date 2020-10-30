package com.profiprog.charts.utils;

import java.awt.*;

import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Float.parseFloat;
import static java.lang.Math.round;

public class Grid {

	float xStepSize;
	float yStepSize;
	float dashLength = 4f;
	float spaceLength = 4f;
	float xOffset = 0f;
	float yOffset = 0f;


	public static Grid parse(String chg) {
		if (chg == null) return null;
		String[] data = splitString(chg, ',');
		Grid result = new Grid();
		result.xStepSize = parseFloat(data[0]);
		result.yStepSize = parseFloat(data[1]);
		if (data.length > 2) result.spaceLength = result.dashLength = parseFloat(data[2]);
		if (data.length > 3) result.spaceLength = parseFloat(data[3]);
		if (data.length > 4) result.xOffset = parseFloat(data[4]);
		if (data.length > 5) result.yOffset = parseFloat(data[5]);
		return result;
	}

	public void render(Graphics2D g2d, Dimension size, DataSeries data, Insets insets, Axis axis) {
		Paint paint = g2d.getPaint();
		Stroke stroke = g2d.getStroke();

		g2d.setPaint(ColorHelper.parseColor("444444"));
		if (spaceLength == 0f)
			g2d.setStroke(new BasicStroke(.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		else
			g2d.setStroke(new BasicStroke(.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
					dashLength, new float[] { dashLength, spaceLength }, 0f));

		if (xStepSize > 0f) {
			int width = size.width - insets.left - insets.right;
			float xOffset = width * this.xOffset / 100f;

			for (int i = 0, x = round(xOffset); x < width; x = (int) (xOffset + ++i * width * xStepSize / 100f)) {
				int dx = insets.left + x;
				g2d.drawLine(dx, insets.top, dx, size.height - insets.bottom);
			}
		}
		else if (xStepSize < 0f) {
			float xAxisScale = 1/Math.abs(xStepSize);
			for (int i = 0, x = axis.bottomPosition(i, xAxisScale, data, size, insets);
				 x <= size.width - insets.right;
				 x = axis.bottomPosition(++i, xAxisScale, data, size, insets)) {
				g2d.drawLine(x, insets.top, x, size.height - insets.bottom);
			}
		}
		if (yStepSize > 0f) {
			int height = size.height - insets.top - insets.bottom;
			float yOffset = height * this.yOffset / 100f;

			for (int i = 0, y = round(yOffset); y < height; y = (int) (yOffset + ++i * height * yStepSize / 100f)) {
				int dy = size.height - insets.bottom - y;
				g2d.drawLine(insets.left, dy, size.width - insets.right, dy);
			}
		}
		else if (yStepSize < 0f) {
			float yAxisScale = 1/Math.abs(yStepSize);
			for (int i = 0, y = axis.leftPosition(i, yAxisScale, data, size, insets);
				 y > insets.top;
				 y = axis.leftPosition(++i, yAxisScale, data, size, insets)) {
				g2d.drawLine(insets.left, y, size.width - insets.right, y);
			}
		}

		g2d.setPaint(paint);
		g2d.setStroke(stroke);
	}
}
