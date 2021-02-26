package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class LineDotValue extends AllBase {
	private Double val;
	private String tip;
	private String color;
	private Integer sides;
	private Integer rotation;
	private String type;
	private boolean hollow;
	private Integer dotsize;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("value", val);
		map.put("tip", tip);
		map.put("colour", color);
		map.put("sides", sides);
		map.put("rotation", rotation);
		map.put("type", type);
		map.put("hollow", hollow);
		map.put("dot-size", dotsize);
		
		
		return UJSon.createParameter(map, false);
	}

	public LineDotValue() {
	}

	public LineDotValue(double val) {
		this.val = val;
	}

	public LineDotValue(double val, String tip, String color) {
		this.val = val;
		this.tip = tip;
		this.color = color;
	}

	public LineDotValue(double val, String color) {
		this.val = val;
		this.color = color;
	}

	/**
	 * @return the val
	 */
	public Double getVal() {
		return val;
	}

	/**
	 * @param val
	 *            the val to set
	 */
	public void setVal(double val) {
		this.val = val;
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
	 * @return the sides
	 */
	public Integer getSides() {
		return sides;
	}

	/**
	 * @param sides
	 *            the sides to set
	 */
	public void setSides(int sides) {
		this.sides = sides;
	}

	/**
	 * @return the rotation
	 */
	public Integer getRotation() {
		return rotation;
	}

	/**
	 * @param rotation
	 *            the rotation to set
	 */
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the isHollow
	 */
	public boolean isHollow() {
		return this.hollow;
	}

	/**
	 * @param isHollow
	 *            the isHollow to set
	 */
	public void setHollow(boolean isHollow) {
		this.hollow = isHollow;
	}

	/**
	 * @return the dotsize
	 */
	public Integer getDotSize() {
		return dotsize;
	}

	/**
	 * @param dotsize
	 *            the dotsize to set
	 */
	public void setDotSize(int dotsize) {
		this.dotsize = dotsize;
	}
}
