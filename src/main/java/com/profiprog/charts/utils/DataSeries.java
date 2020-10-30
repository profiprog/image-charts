package com.profiprog.charts.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.profiprog.charts.utils.StringHelper.splitString;

public class DataSeries {

	public static class Value {
		public final int value;
		public final Double rawValue;

		private Value(Integer value, Double rawValue) {
			this.value = value;
			this.rawValue = rawValue;
		}

		public static Value create(Integer value, Double rawValue) {
			return value == null ? null : new Value(value, rawValue);
		}
	}

	int seriesCount = -1;
	public List<List<Value>> series = new LinkedList<List<Value>>();
	public Integer maxValue = null;

	public static final String SIMPLE_ENCODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	public static final String EXTENDED_ENCODING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";

	private static final int SIMPLE_RANGE = SIMPLE_ENCODING.length(); //62
	private static final int EXTENDED_RANGE = EXTENDED_ENCODING.length() * EXTENDED_ENCODING.length(); //2^12
	private static final int RAW_RANGE = 1048576; //2^20

	public void addSeries(Integer maxValue, List<Value> values) {
		if (this.maxValue == null) this.maxValue = maxValue;
		else if (!this.maxValue.equals(maxValue))
			throw new IllegalStateException("Mixing maxValue! (adding:"+maxValue+", currently:"+this.maxValue+")");
		this.series.add(values);
	}

	public int visibleSeriesCount() {
		return seriesCount < 0 ? series.size() : seriesCount;
	}

	public static DataSeries parse(String chd, String chds) {
		int i = chd.indexOf(":");
		if (i <= 0) throw new IllegalArgumentException("Parameter 'chd' has invalid format!");

		String format = chd.substring(0, i);
		String data = chd.substring(i + 1);

		DataSeries result;
		switch (format.charAt(0)) {
			case 't': result = parseTextFormat(data, chds);
				break;
			case 's': result = parseSimpleEncodingFormat(data);
				break;
			case 'e': result = parseExtendedEncodingFormat(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown format '" + format.charAt(0) + "' of parameter 'chd'");
		}

		if (format.length() > 1)
			result.seriesCount = Integer.parseInt(format.substring(1));

		return result;
	}

	private static DataSeries parseExtendedEncodingFormat(String data) {
		List<ArrayList<Value>> scaledData = new LinkedList<ArrayList<Value>>();
		int counter = 0;
		for (String series : splitString(data, ',')) {
			char[] chars = series.toCharArray();
			ArrayList<Value> scaledSeries = new ArrayList<Value>(chars.length);
			for (int i = 0; i + 1 < chars.length; i += 2) {
				int quotient = EXTENDED_ENCODING.indexOf(chars[i]);
				int remainder = EXTENDED_ENCODING.indexOf(chars[i + 1]);
				if (quotient < 0 || remainder < 0) scaledSeries.add(null);
				else scaledSeries.add(Value.create(quotient * EXTENDED_ENCODING.length() + remainder, null));
			}
			counter = Math.max(counter, scaledSeries.size());
			scaledData.add(scaledSeries);
		}
		DataSeries result = new DataSeries();
		for (ArrayList<Value> scaledSeries : scaledData) {
			if (scaledSeries.size() < counter) {
				scaledSeries.ensureCapacity(counter);
				while (scaledSeries.size() < counter) scaledSeries.add(null);
			}
			result.addSeries(EXTENDED_RANGE, scaledSeries);
		}
		return result;
	}

	private static DataSeries parseSimpleEncodingFormat(String data) {
		List<ArrayList<Value>> scaledData = new LinkedList<ArrayList<Value>>();
		int counter = 0;
		for (String series : splitString(data, ',')) {
			char[] chars = series.toCharArray();
			ArrayList<Value> scaledSeries = new ArrayList<Value>(chars.length);
			for (int i = 0; i < chars.length; i++) {
				int val = SIMPLE_ENCODING.indexOf(chars[i]);
				if (val < 0) scaledSeries.add(null);
				else scaledSeries.add(Value.create(val, null));
			}
			counter = Math.max(counter, scaledSeries.size());
			scaledData.add(scaledSeries);
		}
		DataSeries result = new DataSeries();
		for (ArrayList<Value> scaledSeries : scaledData) {
			if (scaledSeries.size() < counter) {
				scaledSeries.ensureCapacity(counter);
				while (scaledSeries.size() < counter) scaledSeries.add(null);
			}
			result.addSeries(SIMPLE_RANGE, scaledSeries);
		}
		return result;
	}

	private static DataSeries parseTextFormat(String data, String scaling) {
		List<List<Double>> rawData = parseRawValues(data);
		LinkedList<DoubleRange> ranges = determineRanges(rawData, scaling);

		DataSeries result = new DataSeries();
		for (int i = 0; i < rawData.size(); i++) {
			List<Double> series = rawData.get(i);
			List<Value> values = new ArrayList<Value>(series.size());
			for (Double numericalValue : series)
				values.add(Value.create(ranges.get(i).scale(numericalValue, RAW_RANGE), numericalValue));
			result.addSeries(RAW_RANGE, values);
		}
		return result;
	}

	private static LinkedList<DoubleRange> determineRanges(List<List<Double>> rawData, String scaling) {
		LinkedList<DoubleRange> ranges = new LinkedList<DoubleRange>();
		if (scaling == null) ranges.add(new DoubleRange(0, 100));
		else if (scaling.equals("a")) ranges.add(DoubleRange.findAbsoluteMinMax(rawData));
		else {
			String[] values = splitString(scaling, ',');
			if (values.length % 2 == 1)
				throw new IllegalArgumentException("Parameter 'chds' must have even count of values!");
			for (int i = 0, j = 0; (j | 1) < values.length; i++, j = i << 1) {
				double min = parseDoubleValue(values[j], "chds");
				double max = parseDoubleValue(values[j | 1], "chds");
				ranges.add(new DoubleRange(min, max));
			}
		}
		while (ranges.size() < rawData.size())
			ranges.add(ranges.getLast());
		return ranges;
	}

	private static List<List<Double>> parseRawValues(String data) {
		List<List<Double>> rawData = new LinkedList<List<Double>>();
		int counter = 0;
		for (String stringSeries : splitString(data, '|')) {
			List<Double> rawSeries = new LinkedList<Double>();
			for (String stringValue : splitString(stringSeries, ',')) {
				if (stringValue.equals("_") || stringValue.isEmpty()) rawSeries.add(null);
				else rawSeries.add(parseDoubleValue(stringValue, "chd"));
			}
			counter = Math.max(counter, rawSeries.size());
			rawData.add(rawSeries);
		}
		for (List<Double> rawSeries : rawData)
			while (rawSeries.size() < counter)
				rawSeries.add(null);
		return rawData;
	}

	private static double parseDoubleValue(String str, String param) {
		try {
			return Double.parseDouble(str);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot parse value '" + str + "' of parameter '" + param + "'", e);
		}
	}

	public int itemsInSeries() {
		return series.get(0).size();
	}

	public Integer get(int seriesIndex, int elementIndex) {
		if (seriesIndex >= series.size()) return null;
		List<Value> elements = series.get(seriesIndex);
		if (elementIndex >= elements.size()) return null;
		Value value = elements.get(elementIndex);
		return value == null ? null : value.value;
	}

	public Double getRaw(int seriesIndex, int elementIndex) {
		if (seriesIndex >= series.size()) return null;
		List<Value> elements = series.get(seriesIndex);
		if (elementIndex >= elements.size()) return null;
		Value value = elements.get(elementIndex);
		return value == null ? null : value.rawValue;
	}
}
