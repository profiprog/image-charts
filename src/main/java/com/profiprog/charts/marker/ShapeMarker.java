package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ColorHelper;

import java.awt.*;

import static com.profiprog.charts.utils.StringHelper.splitString;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class ShapeMarker {
	protected Color color;
	protected int seriesIndex;

	protected Double startPoint;
	protected Double endPoint;

	protected float size;
	protected int width;
	protected float zOrder = 0.f;

	public void parse(String spec) {
		String[] parts = splitString(spec, ',');

		setColor(ColorHelper.parseColor(parts[1]));
		setSeriesIndex(parseInt(parts[2]));

		String[] whichPoints = splitString(parts[3], ':');
		if (whichPoints.length >= 2) {
			Double startPoint = whichPoints[0].isEmpty() ? null : parseDouble(whichPoints[0]);
			Double endPoint = whichPoints[1].isEmpty() ? null : parseDouble(whichPoints[1]);
			setWhichPoints(startPoint, endPoint);
		}

		int index = parts[4].indexOf(':');
		setSize(parseFloat(index < 0 ? parts[4] : parts[4].substring(0, index)));
		if (index >= 0) setWidth(parseInt(parts[4].substring(index + 1)));

		if (parts.length >= 6) setZOrder(parseFloat(parts[5]));
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setSeriesIndex(int seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setWhichPoints(Double startPoint, Double endPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	public void setZOrder(float zOrder) {
		this.zOrder = zOrder;
	}
}
