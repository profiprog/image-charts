package com.profiprog.charts.utils;

import java.util.List;

public final class DoubleRange {
	public double min;
	public double max;

	public DoubleRange(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public DoubleRange() {
	}

	public void include(Double value) {
		if (value != null) {
			max = Math.max(max, value);
			min = Math.min(min, value);
		}
	}

	public static DoubleRange findAbsoluteMinMax(List<List<Double>> rawData) {
		DoubleRange result = new DoubleRange(0,0);
		for (List<Double> doubles : rawData)
			for (Double value : doubles)
				result.include(value);
		return result;
	}

	public Integer scale(Double numericalValue, int discriminantRange) {
		if (numericalValue == null) return null;
		if (numericalValue < min) return null;

		int scaledVal = (int) Math.floor(discriminantRange * (numericalValue - min) / (max - min));
		scaledVal = Math.min(scaledVal, (discriminantRange) - 1);

		return scaledVal;
	}
}
