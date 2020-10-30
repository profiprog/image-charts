package com.profiprog.charts.utils;

import java.awt.*;

import static com.profiprog.charts.utils.StringHelper.splitString;

public class ColorHelper {

	public static Color[] parseColors(String chco) {
		String[] parts = splitString(chco, ',');
		Color[] result = new Color[parts.length];
		for (int i = 0; i < parts.length; i++)
			result[i] = parseColor(parts[i]);
		return result;
	}

	public static Color parseColor(String rgba) {
		if (rgba.length() != 8 && rgba.length() != 6)
			throw new IllegalArgumentException("Color '" + rgba + "'");
		int r = Integer.parseInt(rgba.substring(0, 2), 16);
		int g = Integer.parseInt(rgba.substring(2, 4), 16);
		int b = Integer.parseInt(rgba.substring(4, 6), 16);
		if (rgba.length() == 6) return new Color(r, g, b);
		int a = Integer.parseInt(rgba.substring(6, 8), 16);
		return new Color(r, g, b, a);
	}

	public static Color[] parseColors(String chco, String defaultValue) {
		return parseColors(chco == null || chco.isEmpty() ? defaultValue : chco);
	}
}
