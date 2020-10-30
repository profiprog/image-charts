package com.profiprog.charts.utils;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartSize {
	private static final Pattern CHART_SIZE = Pattern.compile("(\\d+)x(\\d+)");

	public static Dimension parse(String chs) {
		if (chs == null) throw new IllegalArgumentException("Parameter 'chs' is required!");
		Matcher m = CHART_SIZE.matcher(chs);
		if (!m.matches()) throw new IllegalArgumentException("Parameter 'chs' has invalid format!");
		return new Dimension(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
	}

}
