package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class BarStackValue extends AllBase {
	private String colour;
	private double val;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("colour", colour);
		map.put("val", val);
		return UJSon.createParameter(map, false);
	}

	public BarStackValue(double val, String color) {
		this.colour = color;
		this.val = val;
	}

	public BarStackValue(double val) {
		this.val = val;
	}

	public BarStackValue() {

	}

	public static BarStackValue newInstance(double val) {
		return new BarStackValue(val);
	}

	/**
	 * @return the colour
	 */
	public String getColour() {
		return colour;
	}

	/**
	 * @param colour
	 *            the colour to set
	 */
	public void setColour(String colour) {
		this.colour = colour;
	}

	/**
	 * @return the val
	 */
	public double getVal() {
		return val;
	}

	/**
	 * @param val
	 *            the val to set
	 */
	public void setVal(double val) {
		this.val = val;
	}
}
