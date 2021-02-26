package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class ScatterValue extends AllBase {
	private double x;
	private double y;
	private Integer dotSize;
	private String dotType;
	private String onClick;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("x", x);
		map.put("y", y);
		map.put("dot-size", dotSize);
		map.put("dot-type", dotType);
		map.put("on-click", onClick);
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public ScatterValue(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public ScatterValue(double x, double y, int dotsize) {
		this.x = x;
		this.y = y;
		if (dotsize > 0)
			this.dotSize = dotsize;
		// this.dottype = DotType.HOLLOW_DOT;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the dotSize
	 */
	public Integer getDotSize() {
		return dotSize;
	}

	/**
	 * @param dotSize
	 *            the dotSize to set
	 */
	public void setDotSize(int dotSize) {
		this.dotSize = dotSize;
	}

	/**
	 * @return the dotType
	 */
	public String getDotType() {
		return dotType;
	}

	/**
	 * @param dotType
	 *            the dotType to set
	 */
	public void setDotType(String dotType) {
		this.dotType = dotType;
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
