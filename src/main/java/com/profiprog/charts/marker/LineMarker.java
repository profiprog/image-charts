package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ChartContext;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.lang.Math.round;

public class LineMarker extends ShapeMarker implements ChartMarker {


	public static final int NULL_VALUE = Integer.MIN_VALUE;

	public static LineMarker create(String spec) {
		LineMarker result = new LineMarker();
		result.parse(spec);
		return result;
	}

	@Override
	public void render(ChartContext ctx) {
		int length = ctx.data.itemsInSeries();

		/* prepare range of poly */
		int start = startPoint == null ? 0 : (int) Math.floor(startPoint);
		int end = endPoint == null ? length - 1 : (int) Math.ceil(endPoint);

		/* support define range from end */
		if (start < 0) start = Math.max(0, length + start);
		if (end < 0) end = Math.min(length - 1, length + end);

		/* swap wrong order of coordinates */
		if (end < start) { int aux = start; start = end; end = aux; }

		/* allocate poly size */
		int[] xPoints = new int[end - start + 1];
		int[] yPoints = new int[xPoints.length];

		/* calculate poly coordinates */
		Dimension size = ctx.getChartAreaSize();
		for (int i = start; i <= end; i++) {
			Integer val = ctx.data.get(seriesIndex, i);

			int x = size.width * ((i << 1) + 1) / (length << 1);
			int y = val == null ? NULL_VALUE : size.height - (int) round(size.getHeight() * val / ctx.data.maxValue);

			xPoints[i - start] = x;
			yPoints[i - start] = y;
		}

		/* move start of poly */
		double moveStart = startPoint == null ? 0. : startPoint - Math.floor(startPoint);
		if (moveStart > 0.) {
			Integer a = ctx.data.get(seriesIndex, start);
			Integer b = ctx.data.get(seriesIndex, start + 1);
			if (a != null && b != null) {
				double val = a + (b - a) * moveStart;
				yPoints[0] = size.height - (int) round(size.getHeight() * val / ctx.data.maxValue);
				xPoints[0] = (int) round(size.width * (2*(start + moveStart) + 1) / (length << 1));
			}
		}
		/* move end of poly */
		double moveEnd = endPoint == null ? 0. : endPoint - Math.floor(endPoint);
		if (moveEnd > 0.) {
			Integer a = ctx.data.get(seriesIndex, end - 1);
			Integer b = ctx.data.get(seriesIndex, end);
			if (a != null && b != null) {
				double val = a + (b - a) * moveEnd;
				yPoints[yPoints.length - 1] = size.height - (int) round(size.getHeight() * val / ctx.data.maxValue);
				xPoints[xPoints.length - 1] = (int) round(size.width * (2*(end - 1 + moveEnd) + 1) / (length << 1));
			}
		}

		/* remember graphic state */
		Paint paint = ctx.g2d.getPaint();
		Stroke stroke = ctx.g2d.getStroke();
		AffineTransform transform = ctx.g2d.getTransform();

		/* set new graphic state */
		ctx.g2d.setPaint(color);
		ctx.g2d.translate(ctx.insets.left, ctx.insets.top);
		ctx.g2d.setStroke(new BasicStroke(this.size, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

		/* draw poly parts (split by NULL_VALUE) */
		int drawSize = xPoints.length;
		for (int i = 0; i < drawSize; i++) {
			if (yPoints[i] != NULL_VALUE) continue;
			if (i > 1) ctx.g2d.drawPolyline(xPoints, yPoints, i);
			drawSize -= i + 1;
			System.arraycopy(xPoints, i + 1, xPoints, 0, drawSize);
			System.arraycopy(yPoints, i + 1, yPoints, 0, drawSize);
			i = -1;
		}
		/* draw rest of poly */
		if (drawSize > 1) ctx.g2d.drawPolyline(xPoints, yPoints, drawSize);

		/* restore graphic state */
		ctx.g2d.setPaint(paint);
		ctx.g2d.setStroke(stroke);
		ctx.g2d.setTransform(transform);
	}
}
