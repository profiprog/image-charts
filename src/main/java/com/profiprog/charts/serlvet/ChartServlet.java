package com.profiprog.charts.serlvet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profiprog.charts.marker.ChartMarkers;
import com.profiprog.charts.utils.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class ChartServlet extends HttpServlet {

	public static final String RESPONSE_HEADER_PREFIX = "header:";
	private String fontName = Font.SANS_SERIF;
	private Map<String, String> responseHeaders;

	@Override
	public void init() throws ServletException {
		loadFontResource(getInitParameter("custom-font"));

		this.responseHeaders = prepareResponseHeaders();
	}

	protected Map<String, String> prepareResponseHeaders() {
		Map<String,String> responseHeaders = new LinkedHashMap<String, String>();
		Enumeration names = getInitParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement().toString();
			if (!name.startsWith(RESPONSE_HEADER_PREFIX)) continue;
			responseHeaders.put(name.substring(RESPONSE_HEADER_PREFIX.length()), getInitParameter(name));
		}
		return responseHeaders;
	}

	private void log(Throwable t, String msg, Object...args) {
		if (args.length > 0) msg = String.format(msg, args);
		System.out.printf("[%5$s %1$tF_%1$tT.%1$tL%2$s] %3$s: %4$s\n",
				System.currentTimeMillis(),
				getServletContext().getContextPath(),
				getServletName(),
				msg,
				t == null ? "INFO " : "ERROR");
		if (t != null) t.printStackTrace(System.out);
	}

	private void loadFontResource(String fontResource) {
		InputStream is = null;
		try {
			is = getServletContext().getResourceAsStream(fontResource);
			Font ttf = Font.createFont(Font.TRUETYPE_FONT, is);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ttf);
			fontName = ttf.getFontName();
		} catch (FontFormatException e) {
			log(e, "FontFormatException for font resource: %s", fontResource);
			new ServletException(e);
		} catch (IOException e) {
			log(e, "IOException for font resource: %s", fontResource);
			new ServletException(e);
		} finally {
			if (is != null) try { is.close(); } catch (Exception ignored) {}
			log(null, "Using font: %s", fontName);
		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		try {
			super.service(req, res);
		} catch (ServletException e) {
			log(e, "Internal error!");
			throw e;
		} catch (IOException e) {
			log(e, "Internal error!");
			throw e;
		} catch (RuntimeException e) {
			log(e, "Internal error!");
			throw e;
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ChartContext ctx = new ChartContext(ChartSize.parse(req.getParameter("chs")), fontName);
		ctx.data = DataSeries.parse(req.getParameter("chd"), req.getParameter("chds"));
		Color[] colors = ColorHelper.parseColors(req.getParameter("chco"), "FFBB00");
		ChartMarkers markers = ChartMarkers.parse(req.getParameter("chm"));
		Axis axis = Axis.parse(
				req.getParameter("chxt"),
				req.getParameter("chxr"),
				req.getParameter("chxl"),
				req.getParameter("chxp"),
				req.getParameter("chxs"),
				req.getParameter("chxtc"));
		Grid grid = Grid.parse(req.getParameter("chg"));
		BarWidthAndSpacing bars = BarWidthAndSpacing.parse(req.getParameter("chbh"));
		String outputFormat = req.getParameter("chof");
		BackgroundBar bgBar = BackgroundBar.parse(req.getParameter("chbg"));
		ChartTitles titles = ChartTitles.parse(
				req.getParameter("chtt"),
				req.getParameter("chlt"),
				req.getParameter("chrt"),
				req.getParameter("chbt"),
				req.getParameter("chts"));

		AffineTransform transform = ctx.g2d.getTransform();
		titles.calculateInsests(ctx);
		if (axis != null) axis.calculateInsets(ctx);
		titles.render(ctx);
		if (axis != null) axis.render(ctx);

		if (grid != null) grid.render(ctx.g2d, ctx.size, ctx.data, ctx.insets, axis);

		Dimension space = ctx.getChartAreaSize();
		ctx.g2d.translate(ctx.insets.left, ctx.insets.top);
		space.width <<= 2;
		ctx.g2d.scale(0.25, space.getHeight() / ctx.data.maxValue);
		ctx.json.adaptTransform(ctx.g2d.getTransform());

		int barWidth = ctx.data.itemsInSeries() == 0 ? space.width : bars.calculateBarWidth(space.width, ctx.data.itemsInSeries());
		if (bgBar != null) {
			List<DataSeries.Value> series = ctx.data.series.get(bgBar.seriesIndex);
			ctx.g2d.setColor(bgBar.barColor);
			for (int i = 0; i < ctx.data.itemsInSeries(); i++) {
				DataSeries.Value val = series.get(i);
				if (val == null) continue;

				int height = val.value;
				int width = barWidth;
				int y = ctx.data.maxValue - height;
				int x = space.width * ((i<<1) + 1) / (ctx.data.itemsInSeries() << 1);

				ctx.json.addChartRectangle(x, y, x + width, y + height, "bg_bar%d_%d", bgBar.seriesIndex, i);
				ctx.g2d.fillRect(x, y, width, height);
			}
		}
		for (int i = 0; i < ctx.data.itemsInSeries(); i++) {
			int heightOffset = 0;
			int width = barWidth;
			int x = space.width * ((i<<1) + 1) / (ctx.data.itemsInSeries() << 1) - (width >> 1);
			ctx.json.addChartRectangle(x, 0, x + width, ctx.data.maxValue, "bar_%d", i);

			for (int j = 0; j < ctx.data.visibleSeriesCount(); j++) {
				DataSeries.Value val = ctx.data.series.get(j).get(i);
				Integer max = ctx.data.maxValue;
				if (val == null) continue;
				ctx.g2d.setColor(j < colors.length ? colors[j] : colors[colors.length - 1]);

				int height = val.value;
				int y = ctx.data.maxValue - height - heightOffset;

				heightOffset += height;

				ctx.json.addChartRectangle(x, y, x + width, y + height, "bar%d_%d", j, i);
				ctx.g2d.fillRect(x, y, width, height);
			}
		}
		space.width >>= 2;
		ctx.g2d.setTransform(transform);

		if (markers != null) markers.render(ctx);

		if (responseHeaders != null && !responseHeaders.isEmpty()) {
			for (Map.Entry<String,String> entry : responseHeaders.entrySet()) {
				resp.addHeader(entry.getKey(), entry.getValue());
			}
		}

		if (outputFormat != null && outputFormat.equals("json")) {
			resp.setContentType("application/json");
			new ObjectMapper().writeValue(resp.getOutputStream(), ctx.json);
		}
		else {
			resp.setContentType("image/png");
			ImageIO.write(ctx.image, "png", resp.getOutputStream());
		}
	}
}
