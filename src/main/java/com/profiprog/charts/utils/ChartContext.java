package com.profiprog.charts.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ChartContext {
	public BufferedImage image;
	public JsonOutput json;

	public Graphics2D g2d;
	public Dimension size;
	public Insets chartInsets;
	public Insets insets;
	public DataSeries data;
	private Map<Integer, Font> fontCache = new HashMap<Integer, Font>();

	public Map<String, Object> params = new HashMap<String, Object>();
	private final String fontName;

	public ChartContext(Dimension size, String fontName) {
		this.size = size;
		this.fontName = fontName;
		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
		g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		json = new JsonOutput();
		insets = new Insets(0, 0, 0, 0);
		chartInsets = new Insets(0, 0, 0, 0);
	}

	public void put(Class referer, String name, Object value) {
		params.put(referer.getName() + '.' + name, value);
	}

	public <T> T get(Class referer, String name, Class<? extends T> returnType) {
		return returnType.cast(params.get(referer.getName() + '.' + name));
	}

	public Font getFont(int fontSize) {
		Font font = fontCache.get(fontSize);
		if (font == null) {
			font = new Font(fontName, Font.PLAIN, fontSize);
			fontCache.put(fontSize, font);
		}
		return font;
	}

	public FontMetrics getFontMetrics(int fontSize) {
		return g2d.getFontMetrics(getFont(fontSize));
	}

	public void setFont(int fontSize) {
		g2d.setFont(getFont(fontSize));
	}

	public Dimension getChartAreaSize() {
		return new Dimension(getChartAreaWidth(), getChartAreaHeight());
	}

	public int getChartAreaHeight() {
		return size.height - insets.bottom - insets.top;
	}

	public int getChartAreaWidth() {
		return size.width - insets.left - insets.right;
	}
}
