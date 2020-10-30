package com.profiprog.charts.utils;

public interface SelectorsTarget {

	public static class Helper {
		public static void parseSelectors(String def, SelectorsTarget target) {
			int firstSelectorIndex = -1;
			int lastSelectorIndex = -1;
			int startValueIndex = -1;

			for (int i = 0; i < def.length(); i++) {
				boolean selector = Character.isLetter(def.charAt(i));

				if (startValueIndex != -1) {
					if (!selector) continue;
					String selectors = firstSelectorIndex == -1 ? null : def.substring(firstSelectorIndex, lastSelectorIndex + 1);
					target.applySelectorsValue(selectors, def.substring(startValueIndex, i));
					firstSelectorIndex = -1;
					startValueIndex = -1;
				}

				if (selector) {
					if (firstSelectorIndex == -1) firstSelectorIndex = i;
					lastSelectorIndex = i;
				}
				else {
					startValueIndex = i;
				}
			}

			if (startValueIndex != -1) {
				String selectors = firstSelectorIndex == -1 ? null : def.substring(firstSelectorIndex, lastSelectorIndex + 1);
				target.applySelectorsValue(selectors, def.substring(startValueIndex));
			}
		}

	}

	public void applySelectorsValue(String selectors, String stringValue);
}
