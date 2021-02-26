package com.gdxsoft.easyweb.charts;

import java.awt.Color;
import java.awt.RenderingHints;
import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;


public class Column3D extends Column {

	public Column3D(){
		
	}
	
	public Column3D(HttpServletRequest request, HttpSession session) {
		super(request, session);
	}

	public Column3D(String labelData, String seriesData, String chartData,
			int imageWidth, int imageHeight) {
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
		JFreeChart jfc = ChartFactory.createBarChart3D("", "", "",
				super._DefaultCategoryDataset, PlotOrientation.VERTICAL, true,
				true, true);
		jfc.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		
		CategoryPlot plot = jfc.getCategoryPlot();
	       // 2,设置详细图表的显示细节部分的背景颜色
        // plot.setBackgroundPaint(Color.PINK);
        // 3,设置垂直网格线颜色
        plot.setDomainGridlinePaint(Color.black);
        // 4,设置是否显示垂直网格线
        plot.setDomainGridlinesVisible(true);
        // 5,设置水平网格线颜色
        plot.setRangeGridlinePaint(Color.black);
        // 6,设置是否显示水平网格线
        plot.setRangeGridlinesVisible(true);


		
		Utils.saveChart(name, jfc, this._ImageWidth, this._ImageHeight);
	}
}
