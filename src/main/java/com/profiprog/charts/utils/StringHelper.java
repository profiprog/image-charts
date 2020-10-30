package com.profiprog.charts.utils;

public class StringHelper {

	public static String[] splitString(String str, char separator) {
		String[] result = new String[countCharOccurrences(str, separator) + 1];
		int resultCount = 0;
		int position = 0;
		int i;

		while ((i = str.indexOf(separator, position)) >= 0) {
			result[resultCount++] = str.substring(position, i);
			position = i + 1;
		}
		result[resultCount++] = str.substring(position);
		return result;
	}

	public static int countCharOccurrences(String str, char c) {
		int index = -1;
		int count = 0;
		while ((index = str.indexOf(c, index + 1)) >= 0) count++;
		return count;
	}
}
