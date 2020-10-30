package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ChartContext;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.lang.Math.round;

public class CircleMarker extends ShapeMarker implements ChartMarker {

	public static ChartMarker create(String spec) {
		CircleMarker result = new CircleMarker();
		result.parse(spec);
		return result;
	}

	@Override
	public void render(ChartContext ctx) {
		Paint paint = ctx.g2d.getPaint();
		AffineTransform transform = ctx.g2d.getTransform();

		ctx.g2d.setPaint(color);
		ctx.g2d.translate(ctx.insets.left, ctx.insets.top);
		ctx.g2d.scale(0.5, 0.5);

		Dimension size = ctx.getChartAreaSize();

		for (int i = 0; i < ctx.data.itemsInSeries(); i++) {
			Integer value = ctx.data.get(seriesIndex, i);
			if (value == null) continue;

			int r = (int) (this.size);
			int x = (size.width * ((i << 1) + 1) / (ctx.data.itemsInSeries() << 1) << 1) - r + 1;
			int y = (size.height << 1) - (int) round(2 * size.getHeight() * value / ctx.data.maxValue) - r + 1;
			r <<= 1;

			ctx.g2d.fillOval(x, y, r, r);
		}

		ctx.g2d.setPaint(paint);
		ctx.g2d.setTransform(transform);
	}
}
