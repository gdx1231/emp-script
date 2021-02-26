package com.gdxsoft.easyweb.charts;

import org.jfree.data.general.DefaultPieDataset;

public interface IPie {

	public abstract String createChart();

	public abstract void setData(DefaultPieDataset defaultPieDataset);

	public abstract void setSize(int width, int height);

	public abstract String getLabelDataName();

	/**
	 * @param labelDataName
	 *            the _LabelDataName to set
	 */
	public abstract void setLabelDataName(String labelDataName);

	/**
	 * @return the _ChartDataName
	 */
	public abstract String getChartDataName();

	/**
	 * @param chartDataName
	 *            the _ChartDataName to set
	 */
	public abstract void setChartDataName(String chartDataName);

	/**
	 * @return the _SeriesDataName
	 */
	public abstract String getSeriesDataName();

	/**
	 * @param seriesDataName
	 *            the _SeriesDataName to set
	 */
	public abstract void setSeriesDataName(String seriesDataName);

	public abstract void setChartName(String chartName);

	/**
	 * @return 图片文件名
	 */
	public abstract String getChartName();

	/**
	 * @return 图片真实目录
	 */
	public abstract String getChartPath();

	/**
	 * @return 图片的URL
	 */
	public abstract String getUrl();

	/**
	 * @return Pie对象
	 */
	public abstract DefaultPieDataset getDefaultPieDataset();

	public int getImageWidth();

	/**
	 * @param imageWidth
	 *            the _ImageWidth to set
	 */
	public void setImageWidth(int imageWidth);

	/**
	 * @return the _ImageHeight
	 */
	public int getImageHeight();

	/**
	 * @param imageHeight
	 *            the _ImageHeight to set
	 */
	public void setImageHeight(int imageHeight);

	public String getChartType();

	/**
	 * @param chartType
	 *            the _ChartType to set
	 */
	public void setChartType(String chartType);
}