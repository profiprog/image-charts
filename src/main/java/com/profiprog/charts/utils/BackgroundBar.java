package com.profiprog.charts.utils;

import java.awt.*;

import static com.profiprog.charts.utils.ColorHelper.parseColor;
import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Integer.parseInt;

public class BackgroundBar {

	public int seriesIndex;
	public Color barColor;

	public static BackgroundBar parse(String chbg) {
		if (chbg == null) return null;
		String[] parts = splitString(chbg, ',');
		BackgroundBar result = new BackgroundBar();
		result.seriesIndex = parseInt(parts[0]);
		result.barColor = parseColor(parts[1]);
		return result;
	}
}
