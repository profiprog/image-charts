package com.profiprog.charts.utils;


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;

import static com.profiprog.charts.utils.ColorHelper.parseColor;
import static com.profiprog.charts.utils.SelectorsTarget.Helper.parseSelectors;
import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Character.isLetter;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class ChartTitles {

	private class Title implements SelectorsTarget {
		final String text;
		Color color = Color.gray;
		int fontSize = 10;
		int alignment = 0;
		float position = 0f;
		boolean rotateText = false;

		int marginTop = 0;
		int marginLeft = 0;
		int marginBottom = 0;
		int marginRight = 0;
		int spaceBetweenLines = 0;

		private Title(String text) {
			this.text = text;
		}

		public void applyStyle(String style) {
			String[] data = splitString(style, ',');
			if (!data[0].isEmpty()) color = parseColor(data[0]);
			if (data.length > 1 && !data[1].isEmpty()) fontSize = parseInt(data[1]);
			if (data.length > 2 && !data[2].isEmpty()) {
				int i = 0;
				char c;
				while (i < data[2].length() && isLetter(c = data[2].charAt(i))) {
					switch (c) {
						case 'C': rotateText = true;
						case 'c': position = alignment = 0; break;
						case 'L': rotateText = true;
						case 'l': position = alignment = -1; break;
						case 'R': rotateText = true;
						case 'r': position = alignment = 1; break;
					}
					i++;
				}
				if (i < data[2].length()) position = parseFloat(data[2].substring(i));
			}
			if (data.length > 3 && !data[3].isEmpty()) parseSelectors(data[3], this);
		}

		public void applySelectorsValue(String selectors, String strValue) {
			int value = parseInt(strValue);
			if (selectors == null) {
				marginTop = marginRight = marginBottom = marginLeft = value;
			}
			else for (char c : selectors.toCharArray()) {
				switch (c) {
					case 'l': marginLeft = value; break;
					case 'r': marginRight = value; break;
					case 't': marginTop = value; break;
					case 'b': marginBottom = value; break;
					case 's': spaceBetweenLines = value; break;
				}
			}
		}
	}

	private Title[] top;
	private Title[] left;
	private Title[] right;
	private Title[] bottom;

	public static ChartTitles parse(String chtt, String chlt, String chrt, String chbt, String chts) {
		ChartTitles result = new ChartTitles();
		result.fillTop(chtt);
		result.fillLeft(chlt);
		result.fillRight(chrt);
		result.fillBottom(chbt);
		if (chts != null) {
			String[] styles = splitString(chts, '|');
			if (styles.length == 1) { // global styles
				result.applyStyle("tlrb", styles[0]);
			}
			else for (String style : styles) { // style specified for type orientation and line
				if (style.length() == 0) continue;
				String[] selectorAndStyle = style.split(",", 2);
				result.applyStyle(selectorAndStyle[0], selectorAndStyle[1]);
			}
		}
		return result;
	}

	private void applyStyle(String selector, String style) {
		int i = 0;
		List<Title[]> selectedPositions = new LinkedList<Title[]>();
		while (i < selector.length()) {
			char c = selector.charAt(i);
			if (Character.isDigit(c)) break;
			switch (c) {
				case 't': selectedPositions.add(top); break;
				case 'l': selectedPositions.add(left); break;
				case 'r': selectedPositions.add(right); break;
				case 'b': selectedPositions.add(bottom); break;
			}
			i++;
		}
		selector = selector.substring(i);
		int selectedLine = selector.isEmpty() ? -1 : parseInt(selector);
		for (Title[] position : selectedPositions) {
			if (position == null) continue;
			if (selectedLine >= 0) position[selectedLine].applyStyle(style);
			else for (Title title : position) title.applyStyle(style);
		}
	}

	private void fillBottom(String chbt) {
		if (chbt != null) bottom = split(chbt);
	}

	private void fillRight(String chrt) {
		if (chrt != null) right = split(chrt);
	}

	private void fillLeft(String chlt) {
		if (chlt != null) left = split(chlt);
	}

	private void fillTop(String chtt) {
		if (chtt != null) top = split(chtt);
	}

	private Title[] split(String def) {
		String[] lines = splitString(def, '|');
		Title[] result = new Title[lines.length];
		for (int i = 0; i < lines.length; i++) result[i] = new Title(lines[i]);
		return result;
	}

	public void calculateInsests(ChartContext ctx) {
		if (top != null) {
			for (int i = 0; i < top.length; i++) {
				Title title = top[i];
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				if (i == 0) ctx.chartInsets.top += title.marginTop;
				ctx.chartInsets.top += fm.getHeight();
				if (i == top.length - 1) ctx.chartInsets.top += title.marginBottom;
				else ctx.chartInsets.top += title.spaceBetweenLines;
			}
		}

		if (bottom != null) {
			for (int i = bottom.length - 1; i >= 0; i--) {
				Title title = bottom[i];
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				if (i == bottom.length - 1) ctx.chartInsets.bottom += title.marginBottom;
				ctx.chartInsets.bottom += fm.getHeight();
				if (i == 0) ctx.chartInsets.bottom += title.marginTop;
				else ctx.chartInsets.bottom += title.spaceBetweenLines;
			}
		}

		if (left != null) {
			for (int i = 0; i < left.length; i++) {
				Title title = left[i];
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				if (i == 0) ctx.chartInsets.left += title.marginTop;
				ctx.chartInsets.left += fm.getHeight();
				if (i == left.length - 1) ctx.chartInsets.left += title.marginBottom;
				else ctx.chartInsets.left += title.spaceBetweenLines;
			}
		}

		if (right != null) {
			for (int i = right.length - 1; i >= 0; i--) {
				Title title = right[i];
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				if (i == right.length - 1) ctx.chartInsets.right += title.marginBottom;
				ctx.chartInsets.right += fm.getHeight();
				if (i == 0) ctx.chartInsets.right += title.marginTop;
				else ctx.chartInsets.right += title.spaceBetweenLines;
			}
		}
	}

	public void render(ChartContext ctx) {
		Paint paint = ctx.g2d.getPaint();
		Font font = ctx.g2d.getFont();
		AffineTransform transform = ctx.g2d.getTransform();

		if (top != null) {
			int maxWidth = 0;
			for (Title title : top) {
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				maxWidth = Math.max(maxWidth, fm.stringWidth(title.text));
			}
			for (int i = 0; i < top.length; i++) {
				Title title = top[i];
				ctx.g2d.setPaint(title.color);
				ctx.setFont(title.fontSize);
				FontMetrics fm = ctx.g2d.getFontMetrics();
				if (i == 0) ctx.insets.top += title.marginTop;
				ctx.insets.top += fm.getHeight();
				int width = fm.stringWidth(title.text);
				int signum = (int) Math.signum(title.position);
				int x = ctx.chartInsets.left + title.marginLeft
						+ (int) (((ctx.size.width - ctx.chartInsets.left - ctx.chartInsets.right - title.marginLeft - title.marginRight) * (title.position + 1)) / 2)
						- ((width * (title.alignment + 1)) >> 1)
						- signum * ((maxWidth * (1 - signum * title.alignment)) >> 1);
				ctx.g2d.drawString(title.text, x, ctx.insets.top - fm.getDescent());
				if (i == top.length - 1) ctx.insets.top += title.marginBottom;
				else ctx.insets.top += title.spaceBetweenLines;
			}
		}

		if (bottom != null) {
			int maxWidth = 0;
			for (Title title : bottom) {
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				maxWidth = Math.max(maxWidth, fm.stringWidth(title.text));
			}
			for (int i = bottom.length - 1; i >= 0; i--) {
				Title title = bottom[i];
				ctx.g2d.setPaint(title.color);
				ctx.setFont(title.fontSize);
				FontMetrics fm = ctx.g2d.getFontMetrics();
				if (i == bottom.length - 1) ctx.insets.bottom += title.marginBottom;
				ctx.insets.bottom += fm.getHeight();
				int width = fm.stringWidth(title.text);
				int signum = (int) Math.signum(title.position);
				int x = ctx.chartInsets.left + title.marginLeft
						+ (int) (((ctx.size.width - ctx.chartInsets.left - ctx.chartInsets.right - title.marginLeft - title.marginRight) * (title.position + 1)) / 2)
						- ((width * (title.alignment + 1)) >> 1)
						- signum * ((maxWidth * (1 - signum * title.alignment)) >> 1);
				ctx.g2d.drawString(title.text, x, ctx.size.height - ctx.insets.bottom + fm.getAscent());
				if (i == 0) ctx.insets.bottom += title.marginTop;
				else ctx.insets.bottom += title.spaceBetweenLines;
			}
		}

		ctx.g2d.rotate(-Math.PI/2);

		if (left != null) {
			int maxWidth = 0;
			for (Title title : left) {
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				maxWidth = Math.max(maxWidth, fm.stringWidth(title.text));
			}
			for (int i = 0; i < left.length; i++) {
				Title title = left[i];
				ctx.g2d.setPaint(title.color);
				ctx.setFont(title.fontSize);
				FontMetrics fm = ctx.g2d.getFontMetrics();
				if (i == 0) ctx.insets.left += title.marginTop;
				ctx.insets.left += fm.getHeight();
				int width = fm.stringWidth(title.text);
				int signum = (int) Math.signum(title.position);
				int x = - ctx.size.height + ctx.chartInsets.bottom + title.marginLeft
						+ (int) (((ctx.size.height - ctx.chartInsets.top - ctx.chartInsets.bottom - title.marginLeft - title.marginRight) * (title.position + 1)) / 2)
						- ((width * (title.alignment + 1)) >> 1)
						- signum * ((maxWidth * (1 - signum * title.alignment)) >> 1);
				ctx.g2d.drawString(title.text, x, ctx.insets.left - fm.getDescent());
				if (i == left.length - 1) ctx.insets.left += title.marginBottom;
				else ctx.insets.left += title.spaceBetweenLines;
			}
		}

		if (right != null) {
			boolean rotateText = false;
			int maxWidth = 0;
			for (Title title : right) {
				FontMetrics fm = ctx.getFontMetrics(title.fontSize);
				maxWidth = Math.max(maxWidth, fm.stringWidth(title.text));
				rotateText |= title.rotateText;
			}

			if (rotateText) {
				ctx.g2d.setTransform(transform);
				ctx.g2d.rotate(Math.PI/2);
				for (int i = 0; i < right.length; i++) {
					Title title = right[i];
					ctx.g2d.setPaint(title.color);
					ctx.setFont(title.fontSize);
					FontMetrics fm = ctx.g2d.getFontMetrics();
					if (i == 0) ctx.insets.right += title.marginTop;
					ctx.insets.right += fm.getHeight();
					int width = fm.stringWidth(title.text);
					int signum = (int) Math.signum(title.position);
					int x = title.marginLeft + ctx.chartInsets.top
							+ (int) (((ctx.size.height - ctx.chartInsets.top - ctx.chartInsets.bottom - title.marginLeft - title.marginRight) * (title.position + 1)) / 2)
							- ((width * (title.alignment + 1)) >> 1)
							- signum * ((maxWidth * (1 - signum * title.alignment)) >> 1);
					ctx.g2d.drawString(title.text, x, - ctx.size.width + ctx.insets.right - fm.getDescent());
					if (i == right.length - 1) ctx.insets.right += title.marginBottom;
					else ctx.insets.right += title.spaceBetweenLines;
				}
			}
			else {
				for (int i = right.length - 1; i >= 0; i--) {
					Title title = right[i];
					ctx.g2d.setPaint(title.color);
					ctx.setFont(title.fontSize);
					FontMetrics fm = ctx.g2d.getFontMetrics();
					if (i == right.length - 1) ctx.insets.right += title.marginBottom;
					ctx.insets.right += fm.getHeight();
					int width = fm.stringWidth(title.text);
					int signum = (int) Math.signum(title.position);
					int x = - ctx.size.height + ctx.chartInsets.bottom + title.marginLeft
							+ (int) (((ctx.size.height - ctx.chartInsets.top - ctx.chartInsets.bottom - title.marginLeft - title.marginRight) * (title.position + 1)) / 2)
							- ((width * (title.alignment + 1)) >> 1)
							- signum * ((maxWidth * (1 - signum * title.alignment)) >> 1);
					ctx.g2d.drawString(title.text, x, ctx.size.width - ctx.insets.right + fm.getAscent());
					if (i == 0) ctx.insets.right += title.marginTop;
					else ctx.insets.right += title.spaceBetweenLines;
				}
			}
		}

		ctx.g2d.setPaint(paint);
		ctx.g2d.setFont(font);
		ctx.g2d.setTransform(transform);
	}
}
