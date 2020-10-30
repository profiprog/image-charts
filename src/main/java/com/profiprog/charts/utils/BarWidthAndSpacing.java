package com.profiprog.charts.utils;

import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Float.parseFloat;
import static java.lang.Math.round;

public class BarWidthAndSpacing {

	public float spaceBetweenBars = 1f;
	public float spaceBetweenGroups = 1f;
	public Float maxBarWidth = null;

	public static BarWidthAndSpacing parse(String chbh) {
		BarWidthAndSpacing result = new BarWidthAndSpacing();
		if (chbh == null) chbh = "r,.3";
		String[] data = splitString(chbh, ',');
		if (!"r".equals(data[0])) throw new UnsupportedOperationException("Not implemented: chbh=" + chbh);
		if (data.length > 1 && !data[1].isEmpty()) result.spaceBetweenBars = parseFloat(data[1]);
		if (data.length > 2 && !data[2].isEmpty()) result.spaceBetweenGroups = parseFloat(data[2]);
		if (data.length > 3) result.maxBarWidth = parseFloat(data[3]);
		return result;
	}

	public int calculateBarWidth(int spaceWidth, int barCount) {
		int spaceBetweenBars = round(spaceWidth * this.spaceBetweenBars / (barCount * (1f + this.spaceBetweenBars)));
		int barWidth = spaceWidth / barCount - spaceBetweenBars;
		if (maxBarWidth != null) {
			int maxBarWidth = round(spaceWidth * this.maxBarWidth);
			if (maxBarWidth < barWidth) barWidth = maxBarWidth;
		}
		return barWidth;
	}
}
