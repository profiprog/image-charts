package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ChartContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.round;

public class DataValueMarker extends ShapeMarker implements ChartMarker {

	public static ChartMarker create(String spec) {
		DataValueMarker result = new DataValueMarker();
		result.parse(spec);
		return result;
	}

	@Override
	public void render(ChartContext ctx) {
		Paint paint = ctx.g2d.getPaint();
		AffineTransform transform = ctx.g2d.getTransform();
		Font font = ctx.g2d.getFont();

		ctx.setFont((int) size);
		FontMetrics fm = ctx.g2d.getFontMetrics();
		ctx.g2d.setPaint(color);
		ctx.g2d.translate(ctx.insets.left, ctx.insets.top);
		Dimension size = ctx.getChartAreaSize();

		List<String> strings = new ArrayList<String>(ctx.data.itemsInSeries());
		for (int i = 0; i < ctx.data.itemsInSeries(); i++) {
			Double value = ctx.data.getRaw(seriesIndex, i);
			strings.add(value == null ? null : value.toString());
		}
		trimZeroAfterDecimalPoint(strings);

		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			if (string == null) continue;
			Integer value = ctx.data.get(seriesIndex, i);

			int x = size.width * ((i<<1) + 1) / (ctx.data.itemsInSeries() << 1) - (fm.stringWidth(string) >> 1);
			int y = (size.height) - (int) round(size.getHeight() * value / ctx.data.maxValue);
			y = max(y, fm.getAscent());

			ctx.g2d.drawString(string, x, y - ((int) this.size >> 2));
		}

		ctx.g2d.setPaint(paint);
		ctx.g2d.setFont(font);
		ctx.g2d.setTransform(transform);
	}

	private void trimZeroAfterDecimalPoint(List<String> strings) {
		int trimIndex = 0;
		for (String label : strings) {
			int comaIndex = label.indexOf('.');
			if (comaIndex < 0) continue;
			for (int i = label.length() - 1; i > comaIndex; i--) {
				if (label.charAt(i) != '0') {
					i++;
					if (i - comaIndex > trimIndex) trimIndex = i - comaIndex;
					break;
				}
			}
		}
		for (int i = 0; i < strings.size(); i++) {
			String label = strings.get(i);
			int comaIndex = label.indexOf('.');
			if (comaIndex < 0) continue;
			strings.set(i, label.substring(0, comaIndex + trimIndex));
		}
	}
}
