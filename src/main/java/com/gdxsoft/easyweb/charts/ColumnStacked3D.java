/**
 * 
 */
package com.gdxsoft.easyweb.charts;

import java.awt.RenderingHints;
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;


/**
 * @author guolei
 *
 */
public class ColumnStacked3D extends Column {

	public ColumnStacked3D(){
		
	}
	
	/**
	 * @param request
	 * @param session
	 */
	public ColumnStacked3D(HttpServletRequest request, HttpSession session) {
		super(request, session);
	}

	/**
	 * @param labelData
	 * @param seriesData
	 * @param chartData
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ColumnStacked3D(String labelData, String seriesData,
			String chartData, int imageWidth, int imageHeight) {
		super(labelData, seriesData, chartData, imageWidth, imageHeight);
	}

	public String createChart() {
		String chartPath = super.createDataHashCode();
		File f1 = new File(chartPath);
		if (!f1.exists()) {
			super.createData();
			this.createChart(chartPath);
		}
		return super.getUrl();
	}
	
	private void createChart(String name) {
		JFreeChart jfc = ChartFactory.createStackedBarChart3D("", "", "",
				super._DefaultCategoryDataset, PlotOrientation.VERTICAL, true,
				true, true);
		jfc.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		Utils.saveChart(name, jfc, this._ImageWidth, this._ImageHeight);
	}
}
