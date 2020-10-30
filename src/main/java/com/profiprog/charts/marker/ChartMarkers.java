package com.profiprog.charts.marker;

import com.profiprog.charts.utils.ChartContext;

import java.util.LinkedList;
import java.util.List;

import static com.profiprog.charts.utils.StringHelper.splitString;

public class ChartMarkers {

	public List<ChartMarker> renderers = new LinkedList<ChartMarker>();

	public static ChartMarkers parse(String chm) {
		if (chm == null) return null;
		ChartMarkers result = new ChartMarkers();
		for (String spec : splitString(chm, '|')) {
			ChartMarker marker = buildMarker(spec);
			if (marker != null) result.renderers.add(marker);
		}
		return result;
	}

	private static ChartMarker buildMarker(String spec) {
		char markerTypeIdentifier = extractMarkerTypeIdentifier(spec);
		switch (markerTypeIdentifier) {
			case 'E':
				return ErrorBarMarker.create(spec);
			case 'o':
				return CircleMarker.create(spec);
			case 'N':
				return DataValueMarker.create(spec);
			case 'D':
				return LineMarker.create(spec);
			default:
				throw new UnsupportedOperationException("Unsupported marker type: " + spec);
		}
	}

	private static char extractMarkerTypeIdentifier(String s) {
		if (s.startsWith("@")) s = s.substring(1);
		if (s.length() > 0) return s.charAt(0);
		return '\0';
	}

	public void render(ChartContext ctx) {
		for (ChartMarker marker : renderers)
			marker.render(ctx);
	}
}
