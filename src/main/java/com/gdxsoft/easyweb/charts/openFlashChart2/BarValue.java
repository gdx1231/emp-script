package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class BarValue extends AllBase{
	protected double bottom;
	protected double top;
	protected String color;
	protected String tip;
	private String onClick;
	
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("bottom", bottom);
		map.put("top", top);
		map.put("colour", color);
		map.put("tip", tip);
		map.put("on-click", onClick);
		return UJSon.createParameter(map, false);
	}
	public BarValue() {

	}

	public BarValue(double top) {
		this.top = top;
	}

	public BarValue(double top, double bottom) {
		this.bottom = bottom;
		this.top = top;
	}

	/**
	 * @return the bottom
	 */
	public double getBottom() {
		return bottom;
	}

	/**
	 * @param bottom
	 *            the bottom to set
	 */
	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	/**
	 * @return the top
	 */
	public double getTop() {
		return top;
	}

	/**
	 * @param top
	 *            the top to set
	 */
	public void setTop(double top) {
		this.top = top;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the tip
	 */
	public String getTip() {
		return tip;
	}

	/**
	 * @param tip
	 *            the tip to set
	 */
	public void setTip(String tip) {
		this.tip = tip;
	}

	/**
	 * @return the onClick
	 */
	public String getOnClick() {
		return onClick;
	}

	/**
	 * @param onClick
	 *            the onClick to set
	 */
	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

}
