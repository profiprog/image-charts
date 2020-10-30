package com.profiprog.charts.utils;

import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;

public class JsonOutput {
	private List<ChartShape> chartshape = new LinkedList<ChartShape>();
	private double translateX = 0.;
	private double translateY = 0.;
	private double scaleX = 1.;
	private double scaleY = 1.;

	public void adaptTransform(AffineTransform transform) {
		translateX = transform.getTranslateX();
		translateY = transform.getTranslateY();
		scaleX = transform.getScaleX();
		scaleY = transform.getScaleY();
	}

	private static class ChartShape {
		private String name;
		private String type;
		private int[] coords;

		public ChartShape(String name, String type, int[] coords) {
			this.name = name;
			this.type = type;
			this.coords = coords;
		}

		public int[] getCoords() {
			return coords;
		}

		public void setCoords(int[] coords) {
			this.coords = coords;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public List<ChartShape> getChartshape() {
		return chartshape;
	}

	public void setChartshape(List<ChartShape> chartshape) {
		this.chartshape = chartshape;
	}

	public void addChartShape(String name, String type, int...coords) {
		chartshape.add(new ChartShape(name, type, coords));
	}

	public void addChartRectangle(int xLeft, int yBottom, int xRight, int yTop, String nameFormat, Object...args) {
		addChartShape(String.format(nameFormat, args), "RECT",
				transformX(xLeft), transformY(yBottom),
				transformX(xRight), transformY(yTop));
	}

	private int transformY(int y) {
		return (int) Math.round(translateY + y * scaleY);
	}

	private int transformX(int x) {
		return (int) Math.round(translateX + x * scaleX);
	}
}
