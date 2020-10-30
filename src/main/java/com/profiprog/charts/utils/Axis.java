package com.profiprog.charts.utils;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static com.profiprog.charts.utils.ColorHelper.parseColor;
import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;

public class Axis {

	private static final Pattern AXIS_INDEX = Pattern.compile("\\d+:");
	private static final int TICK_SIZE = 3;

	public static class AxisSpec {
		final char type;
		float startValue = 0f;
		float endValue = 100f;
		float optStep = 0f;
		List<String> labels = new LinkedList<String>();
		List<Float> positions = new LinkedList<Float>();
		public Color labelColor = Color.gray;
		public int fontSize = 11;
		public float alignment;
		public String axisOrTick = "lt";
		public Color tickColor = Color.gray;
		public Color axisColor = Color.gray;

		public AxisSpec(char axisType) {
			type = axisType;
			switch (type) {
				case 'r': alignment = -1f; break;
				case 'x': case 't': alignment = 0f; break;
				case 'y': alignment = 1f; break;
			}
		}

		public String getLabel(int elementIndex) {
			return  labels.isEmpty() ? String.valueOf(elementIndex) :
					elementIndex < labels.size() ? labels.get(elementIndex) : null;
		}

		public List<Label> getLabels(DataSeries series, int activeHeight) {
			LinkedList<Label> result = new LinkedList<Label>();
			if (type == 'x' || type == 't') {
				for (int i = 0; i < series.itemsInSeries(); i++) {
					float position = (endValue - startValue) * ((i << 1) + 1) / (series.itemsInSeries() << 1);
					result.add(new Label(getLabel(i), position));
				}
			}
			else if (!labels.isEmpty()) {
				for (int i = 0; i < labels.size(); i++) {
					float position = positions.size() >= labels.size() ? positions.get(i) :
							labels.size() == 1 ? 0f : i * (endValue - startValue) / (labels.size() - 1);
					result.add(new Label(labels.get(i), position));
				}
			}
			else {
				double gridStep = getGridStep(activeHeight);
				for (double x = 0.0; x < endValue; x += gridStep) {
					if (x < startValue) continue;
					result.add(new Label(String.format("%f", x), (float) x));
				}
				for (double x = -gridStep; x > startValue; x -= gridStep) {
					if (x > endValue) continue;
					result.addFirst(new Label(String.format("%f", x), (float) x));
				}
				trimZeroAfterDecimalPoint(result);
			}
			return result;
		}

		public int getPosition(int inc, float scale, DataSeries series, Dimension size, Insets insets) {
			if (type == 'x' || type == 't') {
				float position = (endValue - startValue) * ((inc << 1) + 1) / (series.itemsInSeries() << 1);
				int space = size.width - insets.left - insets.right;
				return insets.left + (int) (scale * position * space / (endValue - startValue));
			}
			else if (!labels.isEmpty()) {
				float position = positions.size() >= labels.size() ? positions.get(inc % positions.size()) :
						labels.size() == 1 ? 0f : inc * (endValue - startValue) / (labels.size() - 1);
				int space = size.height - insets.top - insets.bottom;
				return size.height - insets.bottom - (int) (scale * position * space / (endValue - startValue));
			}
			else {
				double gridStep = getGridStep(size.height - insets.bottom - insets.top);
				int space = size.height - insets.top - insets.bottom;
				int position = (int) (scale * (float) (inc * gridStep * space) / (endValue - startValue));
				return size.height - insets.bottom - position;
			}
		}

		private void trimZeroAfterDecimalPoint(LinkedList<Label> result) {
			int trimIndex = 0;
			for (Label label : result) {
				int comaIndex = label.text.indexOf('.');
				if (comaIndex < 0) continue;
				for (int i = label.text.length() - 1; i > comaIndex; i--) {
					if (label.text.charAt(i) != '0') {
						i++;
						if (i - comaIndex > trimIndex) trimIndex = i - comaIndex;
						break;
					}
				}
			}
			for (Label label : result) {
				int comaIndex = label.text.indexOf('.');
				if (comaIndex < 0) continue;
				label.text = label.text.substring(0, comaIndex + trimIndex);
			}
		}

		public double getGridStep(int activeSize) {
			double step = (endValue - startValue) / (activeSize / 28.);
			double pow = Math.pow(10, Math.floor(Math.log10(step)));

			step = Math.round(step / pow);
			switch ((int) step) {
				case 1:
				case 2: break;
				case 3: step = 2.5; break;
				case 4:
				case 5:
				case 6: step = 5; break;
				case 7:
				case 8:
				case 9:
				case 10: step = 10; break;
				default:
					return (endValue - startValue);
			}
			return step * pow;
		}

		public void parseFormat(String substring) {
			throw new UnsupportedOperationException();
		}

		public boolean isDrawingLine() {
			return axisOrTick.contains("l");
		}

		public boolean isDrawingTicks() {
			return axisOrTick.contains("t");
		}

		public boolean isDrawingLabels() {
			return !axisOrTick.contains("-");
		}

		public class Label {
			String text;
			float position;

			public Label(String text, float position) {
				this.text = text;
				this.position = position;
			}

			public int getHorizontalPosition(Dimension size, Insets insets, FontMetrics fontMetrics) {
				int space = size.width - insets.left - insets.right;
				int position = (int) (this.position * space / (endValue - startValue));
				int result = insets.left + position;
				if (fontMetrics != null) result -= fontMetrics.stringWidth(text) * (alignment + 1) / 2;
				return result;
			}

			public int getVerticalPosition(Dimension size, Insets insets, FontMetrics fontMetrics) {
				int space = size.height - insets.top - insets.bottom;
				int position = (int) (this.position * space / (endValue - startValue));
				int result =  size.height - insets.bottom - position;
				if (fontMetrics != null) result += (fontMetrics.getAscent() - 1) >> 1;
				return result;
			}

			public String toString() {
				return text + ":" + position;
			}
		}
	}

	private List<AxisSpec> axis = new LinkedList<AxisSpec>();
	private List<AxisSpec> bottom = new LinkedList<AxisSpec>();
	private List<AxisSpec> top = new LinkedList<AxisSpec>();
	private List<AxisSpec> left = new LinkedList<AxisSpec>();
	private List<AxisSpec> right = new LinkedList<AxisSpec>();

	public static Axis parse(String chxt, //Visible axes
							 String chxr, //Axis ranges
							 String chxl, //Axis labels
							 String chxp, //Axis label positions
							 String chxs, //Axis label styles
							 String chxtc //Axis tick mark styles
	) {
		if (chxt == null) return null;
		Axis result = new Axis();

		for (String axeType : splitString(chxt, ',')) {
			if (axeType.length() != 1) continue;
			AxisSpec axis = new AxisSpec(axeType.charAt(0));
			result.axis.add(axis);
			switch (axis.type) {
				case 'x':
					result.bottom.add(axis);
					break;
				case 'y':
					result.left.add(axis);
					break;
				case 'r':
					result.right.add(axis);
					break;
				case 't':
					result.top.add(axis);
					break;
			}
		}

		if (chxr != null) {
			for (String rangeDef : splitString(chxr, '|')) {
				String[] range = splitString(rangeDef, ',');
				AxisSpec axis = result.axis.get(parseInt(range[0]));
				axis.startValue = parseFloat(range[1]);
				axis.endValue = parseFloat(range[2]);
				if (axis.endValue <= axis.startValue)
					throw new IllegalArgumentException("chxr.end_val(3) must be greather than chxr.start_val(2)");
				if (range.length > 3) axis.optStep = parseFloat(range[3]);
			}
		}

		if (chxl != null) {
			AxisSpec axis = null;
			for (String label : splitString(chxl, '|')) {
				if (AXIS_INDEX.matcher(label).matches()) {
					int axisIndex = parseInt(label.substring(0, label.length() - 1));
					axis = axisIndex < result.axis.size() ? result.axis.get(axisIndex) : null;
				}
				else if (axis != null) axis.labels.add(label);
			}
		}

		if (chxp != null) {
			for (String labelPosition : splitString(chxp, '|')) {
				String[] position = splitString(labelPosition, ',');
				int axisIndex = parseInt(position[0]);
				if (axisIndex >= result.axis.size()) continue;
				AxisSpec axis = result.axis.get(axisIndex);
				for (int i = 1; i < position.length; i++)
					axis.positions.add(parseFloat(position[i]));
			}
		}

		if (chxs != null) {
			for (String labelStyles : splitString(chxs, '|')) {
				String[] styles = splitString(labelStyles, ',');
				int formatIndex = styles[0].indexOf("N");
				int axisIndex = parseInt(formatIndex >= 0 ? styles[0].substring(0, formatIndex) : styles[0]);
				AxisSpec axis = result.axis.get(axisIndex);
				if (formatIndex >= 0) axis.parseFormat(styles[0].substring(formatIndex));

				if (styles.length > 1 && !styles[1].isEmpty()) axis.labelColor = parseColor(styles[1]);
				if (styles.length > 2 && !styles[2].isEmpty()) axis.fontSize = parseInt(styles[2]);
				if (styles.length > 3 && !styles[3].isEmpty()) axis.alignment = parseFloat(styles[3]);
				if (styles.length > 4 && !styles[4].isEmpty()) axis.axisOrTick = styles[4];
				if (styles.length > 5 && !styles[5].isEmpty()) axis.tickColor = parseColor(styles[5]);
				if (styles.length > 6 && !styles[6].isEmpty()) axis.axisColor = parseColor(styles[6]);
			}
		}

		return result;
	}

	int SPACE = 1;

	public void calculateInsets(ChartContext ctx) {

		ctx.chartInsets.bottom += cumulativeFontHeight(bottom, ctx, 1);
		if (!bottom.isEmpty() && bottom.get(0).isDrawingTicks()) ctx.chartInsets.bottom += TICK_SIZE;

		ctx.chartInsets.top += cumulativeFontHeight(top, ctx, 1) + top.size();
		if (!top.isEmpty() && top.get(0).isDrawingTicks()) ctx.chartInsets.top += TICK_SIZE;

		if (!right.isEmpty() || !left.isEmpty()) {
			int maxFontSize = 0;
			for (AxisSpec axis : left)
				if (axis.isDrawingLabels() && axis.fontSize > maxFontSize)
					maxFontSize = axis.fontSize;
			for (AxisSpec axis : right)
				if (axis.isDrawingLabels() && axis.fontSize > maxFontSize)
					maxFontSize = axis.fontSize;

			if (maxFontSize > 0) {
				FontMetrics fm = ctx.getFontMetrics(maxFontSize);
				ctx.chartInsets.bottom = max(ctx.chartInsets.bottom, fm.getDescent() + (fm.getAscent() >> 1));
				ctx.chartInsets.top = max(ctx.chartInsets.top, (fm.getAscent() - 1) >> 1);
			}
		}

		int activeHeight = ctx.size.height - ctx.chartInsets.bottom - ctx.chartInsets.top;

		// left labels
		boolean incremented = false;
		for (int k = left.size() - 1; k >= 0; k--) {
			AxisSpec axis = left.get(k);
			if (!axis.isDrawingLabels()) continue;
			List<AxisSpec.Label> labels = axis.getLabels(ctx.data, activeHeight);
			FontMetrics fm = ctx.getFontMetrics(axis.fontSize);

			int maxWidth = 0;
			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int width = fm.stringWidth(label.text);
				if (width > maxWidth) maxWidth = width;
			}
			ctx.chartInsets.left += (SPACE << 1) + maxWidth;
			incremented = true;
		}
		if (!left.isEmpty()) {
			AxisSpec axis = left.get(0);
			if (!incremented) ctx.chartInsets.left += 1;
			if (axis.isDrawingTicks()) ctx.chartInsets.left += TICK_SIZE;
		}

		// draw right labels
		incremented = false;
		for (int k = right.size() - 1; k >= 0; k--) {
			AxisSpec axis = right.get(k);
			if (!axis.isDrawingLabels()) continue;
			List<AxisSpec.Label> labels = axis.getLabels(ctx.data, activeHeight);
			FontMetrics fm = ctx.getFontMetrics(axis.fontSize);

			int maxWidth = 0;
			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int width = fm.stringWidth(label.text);
				if (width > maxWidth) maxWidth = width;
			}
			ctx.chartInsets.right += (SPACE << 1) + maxWidth;
			incremented = true;
		}
		if (!right.isEmpty()) {
			AxisSpec axis = right.get(0);
			if (!incremented) ctx.chartInsets.right += 1;
			if (axis.isDrawingTicks()) ctx.chartInsets.right += TICK_SIZE;
		}
	}

	public void render(ChartContext ctx) {
		Paint paint = ctx.g2d.getPaint();
		Stroke stroke = ctx.g2d.getStroke();
		Font font = ctx.g2d.getFont();
		boolean incremented;

		ctx.g2d.setStroke(new BasicStroke(.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		int activeHeight = ctx.size.height - ctx.chartInsets.bottom - ctx.chartInsets.top;

		// draw left labels
		incremented = false;
		for (int k = left.size() - 1; k >= 0; k--) {
			AxisSpec axis = left.get(k);
			if (!axis.isDrawingLabels()) continue;
			ctx.g2d.setPaint(axis.labelColor);
			ctx.setFont(axis.fontSize);
			List<AxisSpec.Label> labels = axis.getLabels(ctx.data, activeHeight);
			FontMetrics fontMetrics = ctx.g2d.getFontMetrics();

			int maxWidth = 0;
			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int width = fontMetrics.stringWidth(label.text);
				if (width > maxWidth) maxWidth = width;
			}
			ctx.insets.left += SPACE;

			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int x = ctx.insets.left + ((int) ((maxWidth - fontMetrics.stringWidth(label.text)) * (1 + axis.alignment)) >> 1);
				int y = label.getVerticalPosition(ctx.size, ctx.chartInsets, fontMetrics);
				ctx.g2d.drawString(label.text, x, y);
			}
			ctx.insets.left += SPACE + maxWidth;
			incremented = true;
		}
		if (!incremented && !left.isEmpty()) ctx.insets.left += 1;
		if (!left.isEmpty()) {
			AxisSpec axis = left.get(0);
			// draw left ticks
			if (axis.isDrawingTicks()) ctx.insets.left += TICK_SIZE;
			if (axis.isDrawingLine()) {
				ctx.g2d.setPaint(axis.axisColor);
				ctx.g2d.drawLine(ctx.insets.left - 1, ctx.size.height - ctx.chartInsets.bottom, ctx.insets.left - 1, ctx.chartInsets.top);
			}
			if (axis.isDrawingTicks()) {
				ctx.g2d.setPaint(axis.tickColor);
				int x = ctx.insets.left;
				for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
					int y = label.getVerticalPosition(ctx.size, ctx.chartInsets, null);
					ctx.g2d.drawLine(x - 1, y, x - TICK_SIZE - 1, y);
				}
			}
		}

		// draw right labels
		incremented = false;
		for (int k = right.size() - 1; k >= 0; k--) {
			AxisSpec axis = right.get(k);
			if (!axis.isDrawingLabels()) continue;
			ctx.g2d.setPaint(axis.labelColor);
			ctx.setFont(axis.fontSize);
			List<AxisSpec.Label> labels = axis.getLabels(ctx.data, activeHeight);
			FontMetrics fontMetrics = ctx.g2d.getFontMetrics();

			int maxWidth = 0;
			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int width = fontMetrics.stringWidth(label.text);
				if (width > maxWidth) maxWidth = width;
			}
			ctx.insets.right += SPACE + maxWidth;

			for (AxisSpec.Label label : labels) {
				if (label.text == null) continue;
				int x = ctx.size.width - ctx.insets.right + ((int) ((maxWidth - fontMetrics.stringWidth(label.text)) * (1 + axis.alignment)) >> 1);
				int y = label.getVerticalPosition(ctx.size, ctx.chartInsets, fontMetrics);
				ctx.g2d.drawString(label.text, x, y);
			}
			ctx.insets.right += SPACE;
			incremented = true;
		}
		if (!incremented && !right.isEmpty()) ctx.insets.right += 1;
		if (!right.isEmpty()) {
			AxisSpec axis = right.get(0);
			// draw right ticks
			if (axis.isDrawingTicks()) ctx.insets.right += TICK_SIZE;
			if (axis.isDrawingLine()) {
				ctx.g2d.setPaint(axis.axisColor);
				ctx.g2d.drawLine(ctx.size.width - ctx.insets.right, ctx.size.height - ctx.chartInsets.bottom, ctx.size.width - ctx.insets.right, ctx.chartInsets.top);
			}
			if (axis.isDrawingTicks()) {
				ctx.g2d.setPaint(axis.tickColor);
				int x = ctx.size.width - ctx.insets.right;
				for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
					int y = label.getVerticalPosition(ctx.size, ctx.chartInsets, null);
					ctx.g2d.drawLine(x, y, x + TICK_SIZE, y);
				}
			}
		}


		// draw bottom labels
		incremented = false;
		for (int i = bottom.size() - 1; i >= 0; i--) {
			AxisSpec axis = bottom.get(i);
			if (!axis.isDrawingLabels()) continue;
			ctx.setFont(axis.fontSize);
			FontMetrics fontMetrics = ctx.g2d.getFontMetrics();
			int y = ctx.size.height - ctx.insets.bottom - fontMetrics.getDescent();
			ctx.g2d.setPaint(axis.labelColor);
			for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
				if (label.text == null) continue;
				ctx.g2d.drawString(label.text, label.getHorizontalPosition(ctx.size, ctx.chartInsets, fontMetrics), y);
			}
			ctx.insets.bottom += fontMetrics.getHeight();
			incremented = true;
		}
		if (!incremented && !bottom.isEmpty()) ctx.insets.bottom += 1;
		// draw bottom ticks
		if (!bottom.isEmpty()) {
			AxisSpec axis = bottom.get(0);
			if (axis.isDrawingTicks()) ctx.insets.bottom += TICK_SIZE;
			if (axis.isDrawingLine()) {
				ctx.g2d.setPaint(axis.axisColor);
				ctx.g2d.drawLine(ctx.chartInsets.left, ctx.size.height - ctx.insets.bottom, ctx.size.width - ctx.chartInsets.right, ctx.size.height - ctx.insets.bottom);
			}
			if (axis.isDrawingTicks()) {
				ctx.g2d.setPaint(axis.tickColor);
				int y = ctx.size.height - ctx.insets.bottom;
				for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
					int x = label.getHorizontalPosition(ctx.size, ctx.chartInsets, null);
					ctx.g2d.drawLine(x, y, x, y + TICK_SIZE);
				}
			}
		}

		// draw top labels
		incremented = false;
		for (int i = top.size() - 1; i >= 0; i--) {
			AxisSpec axis = top.get(i);
			if (!axis.isDrawingLabels()) continue;
			ctx.setFont(axis.fontSize);
			FontMetrics fontMetrics = ctx.g2d.getFontMetrics();
			int y = ctx.insets.top + fontMetrics.getAscent();
			ctx.g2d.setPaint(axis.labelColor);
			for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
				if (label.text == null) continue;
				ctx.g2d.drawString(label.text, label.getHorizontalPosition(ctx.size, ctx.chartInsets, fontMetrics), y);
			}
			ctx.insets.top += fontMetrics.getHeight();
			incremented = true;
		}
		if (!incremented && !top.isEmpty()) ctx.insets.top += 1;
		// draw top ticks
		if (!top.isEmpty()) {
			AxisSpec axis = top.get(0);
			if (axis.isDrawingTicks()) ctx.insets.top += TICK_SIZE;
			if (axis.isDrawingLine()) {
				ctx.g2d.setPaint(axis.axisColor);
				ctx.g2d.drawLine(ctx.chartInsets.left, ctx.insets.top - 1, ctx.size.width - ctx.chartInsets.right, ctx.insets.top - 1);
			}
			if (axis.isDrawingTicks()) {
				ctx.g2d.setPaint(axis.tickColor);
				int y = ctx.insets.top;
				for (AxisSpec.Label label : axis.getLabels(ctx.data, activeHeight)) {
					int x = label.getHorizontalPosition(ctx.size, ctx.chartInsets, null);
					ctx.g2d.drawLine(x, y - 1, x, y - TICK_SIZE - 1);
				}
			}
		}

		if (!right.isEmpty() || !left.isEmpty()) {
			int maxFontSize = 0;
			for (AxisSpec axis : left)
				if (axis.isDrawingLabels() && axis.fontSize > maxFontSize)
					maxFontSize = axis.fontSize;
			for (AxisSpec axis : right)
				if (axis.isDrawingLabels() && axis.fontSize > maxFontSize)
					maxFontSize = axis.fontSize;

			if (maxFontSize > 0) {
				FontMetrics fm = ctx.getFontMetrics(maxFontSize);
				ctx.insets.bottom = max(ctx.insets.bottom, fm.getDescent() + (fm.getAscent() >> 1));
				ctx.insets.top = max(ctx.insets.top, (fm.getAscent() - 1) >> 1);
			}
		}

		ctx.g2d.setPaint(paint);
		ctx.g2d.setStroke(stroke);
		ctx.g2d.setFont(font);
	}

	public int leftPosition(int inc, float scale, DataSeries series, Dimension size, Insets insets) {
		if (left.isEmpty()) return Integer.MIN_VALUE;
		return left.get(0).getPosition(inc, scale, series, size, insets);
	}

	public int bottomPosition(int inc, float scale, DataSeries series, Dimension size, Insets insets) {
		if (bottom.isEmpty()) return Integer.MAX_VALUE;
		return bottom.get(0).getPosition(inc, scale, series, size, insets);
	}

	private int cumulativeFontHeight(List<AxisSpec> list, ChartContext gtx, int minValue) {
		int result = 0;
		for (AxisSpec axis : list) if (axis.isDrawingLabels())
			result += gtx.getFontMetrics(axis.fontSize).getHeight();
		return result == 0 && !list.isEmpty() ? minValue : result;
	}
}
