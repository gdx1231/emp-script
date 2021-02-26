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
import org.jfree.chart.plot.PiePlot3D;


/**
 * @author guolei
 *
 */
public class Pie3D extends Pie {

	public Pie3D(){
		
	}
	
	/**
	 * @param request
	 * @param session
	 */
	public Pie3D(HttpServletRequest request, HttpSession session) {
		super(request, session);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param labelData
	 * @param chartData
	 * @param imageWidth
	 * @param imageHeight
	 */
	public Pie3D(String labelData, String chartData, int imageWidth,
			int imageHeight) {
		super(labelData, chartData, imageWidth, imageHeight);
		// TODO Auto-generated constructor stub
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
		JFreeChart jfc = ChartFactory.createPieChart3D("",
				this._DefaultPieDataset, true, true, true);
		jfc.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		PiePlot3D pp = (PiePlot3D) jfc.getPlot();
		pp.setNoDataMessage("nodata");
		pp.setExplodePercent(_DefaultPieDataset.getKey(0).toString(), 0.2);
		Utils.saveChart(name, jfc, this._ImageWidth, this._ImageHeight);
	}
}
