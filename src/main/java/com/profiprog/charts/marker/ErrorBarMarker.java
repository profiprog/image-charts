package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ChartContext;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.lang.Math.round;

public class ErrorBarMarker extends ShapeMarker implements ChartMarker {

	public static ErrorBarMarker create(String spec) {
		ErrorBarMarker result = new ErrorBarMarker();
		result.parse(spec);
		return result;
	}

	@Override
	public void render(ChartContext ctx) {
		Paint paint = ctx.g2d.getPaint();
		Stroke stroke = ctx.g2d.getStroke();
		AffineTransform transform = ctx.g2d.getTransform();

		ctx.g2d.setPaint(color);
		ctx.g2d.translate(ctx.insets.left, ctx.insets.top);
		ctx.g2d.setStroke(new BasicStroke(this.size, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		Dimension size = ctx.getChartAreaSize();

		for (int i = 0; i < ctx.data.itemsInSeries(); i++) {
			Integer low = ctx.data.get(seriesIndex, i);
			Integer high = ctx.data.get(seriesIndex + 1, i);
			if (low == null || high == null) continue;

			int x = size.width * ((i<<1) + 1) / (ctx.data.itemsInSeries() << 1);
			int lowY = size.height - (int) round(size.getHeight() * low / ctx.data.maxValue);
			int highY = size.height - (int) round(size.getHeight() * high / ctx.data.maxValue);

			ctx.g2d.drawLine(x, lowY, x, highY);
			ctx.g2d.drawLine(x - width / 2, lowY, x + width / 2, lowY);
			ctx.g2d.drawLine(x - width / 2, highY, x + width / 2, highY);
		}

		ctx.g2d.setPaint(paint);
		ctx.g2d.setStroke(stroke);
		ctx.g2d.setTransform(transform);
	}
}
