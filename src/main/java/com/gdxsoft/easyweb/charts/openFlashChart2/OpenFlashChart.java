package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdxsoft.easyweb.utils.UJSon;

public class OpenFlashChart {
	private Title title;
	private List<ChartBase> elements;
	private XAxis xAxis;
	private YAxis yAxis;
	private YAxis yAxisRight;
	private Legend xLegend;
	private Legend yLegend;
	private Legend y2Legend;
	private String bgcolor;
	private RadarAxis radarAxis;
	private ToolTip tooltip;
	private Integer numDecimals;
	private Boolean fixedNumDecimalsForced;
	private Boolean decimalSeparatorComma;
	private Boolean thousandSeparatorDisabled;

	public OpenFlashChart() {
		title = new Title("Chart Title");
		elements = new ArrayList<ChartBase>();
	}

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("title", title);
		map.put("elements", elements);
		map.put("x_axis", xAxis);
		map.put("y_axis", yAxis);
		map.put("y_axis_right", yAxisRight);
		map.put("x_legend", xLegend);
		map.put("y_legend", yLegend);
		map.put("y2_legend", y2Legend);
		map.put("bg_colour", bgcolor);
		map.put("radar_axis", radarAxis);
		map.put("tooltip", tooltip);
		map.put("num_decimals", numDecimals);
		map.put("is_fixed_num_decimals_forced", fixedNumDecimalsForced);
		map.put("is_decimal_separator_comma", decimalSeparatorComma);
		map.put("is_thousand_separator_disabled", thousandSeparatorDisabled);
		
		return  "{"+ UJSon.createParameter(map, false)+"}";
	}

	/**
	 * @return the title
	 */
	public Title getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(Title title) {
		this.title = title;
	}

	/**
	 * @return the elements
	 */
	public List<ChartBase> getElements() {
		return elements;
	}

	/**
	 * @param elements
	 *            the elements to set
	 */
	public void setElements(List<ChartBase> elements) {
		this.elements = elements;
	}

	/**
	 * @return the xAxis
	 */
	public XAxis getXAxis() {
		return xAxis;
	}

	/**
	 * @param axis
	 *            the xAxis to set
	 */
	public void setXAxis(XAxis axis) {
		xAxis = axis;
	}

	/**
	 * @return the yAxis
	 */
	public YAxis getYAxis() {
		return yAxis;
	}

	/**
	 * @param axis
	 *            the yAxis to set
	 */
	public void setYAxis(YAxis axis) {
		yAxis = axis;
	}

	/**
	 * @return the yAxisRight
	 */
	public YAxis getYAxisRight() {
		return yAxisRight;
	}

	/**
	 * @param axisRight
	 *            the yAxisRight to set
	 */
	public void setYAxisRight(YAxis axisRight) {
		yAxisRight = axisRight;
	}

	/**
	 * @return the xLegend
	 */
	public Legend getXLegend() {
		return xLegend;
	}

	/**
	 * @param legend
	 *            the xLegend to set
	 */
	public void setXLegend(Legend legend) {
		xLegend = legend;
	}

	/**
	 * @return the yLegend
	 */
	public Legend getYLegend() {
		return yLegend;
	}

	/**
	 * @param legend
	 *            the yLegend to set
	 */
	public void setYLegend(Legend legend) {
		yLegend = legend;
	}

	/**
	 * @return the y2Legend
	 */
	public Legend getY2Legend() {
		return y2Legend;
	}

	/**
	 * @param legend
	 *            the y2Legend to set
	 */
	public void setY2Legend(Legend legend) {
		y2Legend = legend;
	}

	/**
	 * @return the bgcolor
	 */
	public String getBgcolor() {
		return bgcolor;
	}

	/**
	 * @param bgcolor
	 *            the bgcolor to set
	 */
	public void setBgcolor(String bgcolor) {
		this.bgcolor = bgcolor;
	}

	/**
	 * @return the radarAxis
	 */
	public RadarAxis getRadarAxis() {
		return radarAxis;
	}

	/**
	 * @param radarAxis
	 *            the radarAxis to set
	 */
	public void setRadarAxis(RadarAxis radarAxis) {
		this.radarAxis = radarAxis;
	}

	/**
	 * @return the tooltip
	 */
	public ToolTip getTooltip() {
		return tooltip;
	}

	/**
	 * @param tooltip
	 *            the tooltip to set
	 */
	public void setTooltip(ToolTip tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @return the numDecimals
	 */
	public Integer getNumDecimals() {
		return numDecimals;
	}

	/**
	 * @param numDecimals
	 *            the numDecimals to set
	 */
	public void setNumDecimals(Integer numDecimals) {
		this.numDecimals = numDecimals;
	}

	/**
	 * @return the fixedNumDecimalsForced
	 */
	public boolean isFixedNumDecimalsForced() {
		return fixedNumDecimalsForced;
	}

	/**
	 * @param fixedNumDecimalsForced
	 *            the fixedNumDecimalsForced to set
	 */
	public void setFixedNumDecimalsForced(boolean fixedNumDecimalsForced) {
		this.fixedNumDecimalsForced = fixedNumDecimalsForced;
	}

	/**
	 * @return the decimalSeparatorComma
	 */
	public Boolean isDecimalSeparatorComma() {
		return decimalSeparatorComma;
	}

	/**
	 * @param decimalSeparatorComma
	 *            the decimalSeparatorComma to set
	 */
	public void setDecimalSeparatorComma(boolean decimalSeparatorComma) {
		this.decimalSeparatorComma = decimalSeparatorComma;
	}

	/**
	 * @return the thousandSeparatorDisabled
	 */
	public Boolean isThousandSeparatorDisabled() {
		return thousandSeparatorDisabled;
	}

	/**
	 * @param thousandSeparatorDisabled
	 *            the thousandSeparatorDisabled to set
	 */
	public void setThousandSeparatorDisabled(boolean thousandSeparatorDisabled) {
		this.thousandSeparatorDisabled = thousandSeparatorDisabled;
	}
}
