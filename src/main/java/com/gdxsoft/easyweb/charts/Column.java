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
import org.jfree.data.category.DefaultCategoryDataset;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UPath;

public class Column implements ICategory {

	protected DefaultCategoryDataset _DefaultCategoryDataset;
	protected RequestValue _RequestValue;
	protected String _LabelData;
	protected String _ChartData;
	protected String _SeriesData;
	protected int _ImageWidth;
	protected int _ImageHeight;
	private String _ChartName;
	private String _ChartPath;
	private String _Url;

	private String _LabelDataName;
	private String _ChartDataName;
	private String _SeriesDataName;

	private String _ChartType;

	public Column() {

	}

	public Column(HttpServletRequest request, HttpSession session) {
		this._RequestValue = new RequestValue(request, session);
		this.createData();
		Object lbl = this._RequestValue.getObject(Utils.PIE_LABLE_NAME
				.toUpperCase());
		Object data = this._RequestValue.getObject(Utils.PIE_DATA_NAME
				.toUpperCase());
		Object series = this._RequestValue.getObject(Utils.PIE_SERIES_NAME
				.toUpperCase());
		if (lbl == null || data == null || series == null) {
			return;
		}

		Object size = this._RequestValue.getObject(Utils.PIE_IMAGE_SIZE
				.toUpperCase());
		int[] iSize = Utils.getImageSize(size == null ? null : size.toString());
		_ImageWidth = iSize[0];
		_ImageHeight = iSize[1];
		this._LabelData = lbl.toString();
		this._ChartData = data.toString();
		_SeriesData = series.toString();
	}

	/**
	 * @param labelData
	 *            标签数据
	 * @param chartData
	 *            图表数据
	 * @param imageWidth
	 *            图片宽度
	 * @param imageHeight
	 *            图片高度
	 */
	public Column(String labelData, String seriesData, String chartData,
			int imageWidth, int imageHeight) {
		this._LabelData = labelData;
		this._ChartData = chartData;
		this._SeriesData = seriesData;
		this._ImageHeight = imageHeight;
		this._ImageWidth = imageWidth;
	}

	protected void createData() {
		if (this._DefaultCategoryDataset != null)
			return;

		this._DefaultCategoryDataset = new DefaultCategoryDataset();
		String[] labels = _LabelData.split(",");
		String[] series = this._SeriesData.split(",");
		String[] datas = _ChartData.split(",");
		for (int i = 0; i < series.length; i++) {
			String[] data = datas[i].split("\\^");
			for (int m = 0; m < data.length; m++) {
				double b1 = Double.parseDouble(data[m]);
				this._DefaultCategoryDataset.addValue(b1, series[i], labels[m]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdxsoft.easyweb.charts.ICategory#setData(org.jfree.data.category.
	 * DefaultCategoryDataset)
	 */
	public void setData(DefaultCategoryDataset catagoryDataset) {
		this._DefaultCategoryDataset = catagoryDataset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.charts.ICategory#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		this._ImageWidth = width;
		this._ImageHeight = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.charts.ICategory#createChart()
	 */
	public String createChart() {
		String chartPath = createDataHashCode();

		File f1 = new File(chartPath);
		if (!f1.exists()) {
			// this.createData();
			this.saveChart(chartPath);
		}
		return this._Url;
	}

	private void saveChart(String name) {
		JFreeChart jfc = ChartFactory.createBarChart("", "", "",
				this._DefaultCategoryDataset, PlotOrientation.VERTICAL, true,
				true, true);
		/*----------设置消除字体的锯齿渲染（解决中文问题）--------------*/
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

	protected String createDataHashCode() {
		String s1 = this._ChartName;
		s1 = s1.replace("-", "X") + "  ";
		String path = "";
		for (int i = 0; i < s1.length() - 1; i += 2) {
			path += s1.substring(i, i + 2).trim() + "/";
		}
		this._ChartPath = UPath.getPATH_UPLOAD() + "/temp_charts/" + path;
		this._ChartName = "EWA_" + s1.trim() + ".png";
		this._Url = UPath.getPATH_UPLOAD_URL() +  "/temp_charts/" + path + this._ChartName;

		String chartPath = this._ChartPath + this._ChartName;
		this.createDirs(chartPath);
		return chartPath;
	}

	/**
	 * 生成目录
	 * 
	 * @param chartPath
	 */
	private void createDirs(String chartPath) {
		File f1 = new File(chartPath);
		File root = f1.getParentFile();
		if (!root.exists()) {
			root.mkdirs();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.charts.ICategory#getChartName()
	 */
	public String getChartName() {
		return _ChartName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.charts.ICategory#getChartPath()
	 */
	public String getChartPath() {
		return _ChartPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.charts.ICategory#getUrl()
	 */
	public String getUrl() {
		return _Url;
	}

	/**
	 * @return the _LabelDataName
	 */
	public String getLabelDataName() {
		return _LabelDataName;
	}

	/**
	 * @param labelDataName
	 *            the _LabelDataName to set
	 */
	public void setLabelDataName(String labelDataName) {
		_LabelDataName = labelDataName;
	}

	/**
	 * @return the _ChartDataName
	 */
	public String getChartDataName() {
		return _ChartDataName;
	}

	/**
	 * @param chartDataName
	 *            the _ChartDataName to set
	 */
	public void setChartDataName(String chartDataName) {
		_ChartDataName = chartDataName;
	}

	/**
	 * @return the _SeriesDataName
	 */
	public String getSeriesDataName() {
		return _SeriesDataName;
	}

	/**
	 * @param seriesDataName
	 *            the _SeriesDataName to set
	 */
	public void setSeriesDataName(String seriesDataName) {
		_SeriesDataName = seriesDataName;
	}

	/**
	 * @return the _DefaultCategoryDataset
	 */
	public DefaultCategoryDataset getDefaultCategoryDataset() {
		return _DefaultCategoryDataset;
	}

	/**
	 * @param chartName
	 *            the _ChartName to set
	 */
	public void setChartName(String chartName) {
		_ChartName = chartName;
	}

	/**
	 * @return the _ImageWidth
	 */
	public int getImageWidth() {
		return _ImageWidth;
	}

	/**
	 * @param imageWidth
	 *            the _ImageWidth to set
	 */
	public void setImageWidth(int imageWidth) {
		_ImageWidth = imageWidth;
	}

	/**
	 * @return the _ImageHeight
	 */
	public int getImageHeight() {
		return _ImageHeight;
	}

	/**
	 * @param imageHeight
	 *            the _ImageHeight to set
	 */
	public void setImageHeight(int imageHeight) {
		_ImageHeight = imageHeight;
	}

	/**
	 * @return the _ChartType
	 */
	public String getChartType() {
		return _ChartType;
	}

	/**
	 * @param chartType
	 *            the _ChartType to set
	 */
	public void setChartType(String chartType) {
		_ChartType = chartType;
	}
}
