package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class PieValue extends AllBase {
	private double val;
	private String text;
	private String onClick;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("val", val);
		map.put("text", text);
		map.put("on-click", onClick);
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public PieValue(double val) {
		this.val = val;
	}

	public static PieValue instanceNew(double val) {
		return new PieValue(val, "");
	}

	public PieValue(double val, String text) {
		this.val = val;
		this.text = text;
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

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the click
	 */
	public String getOnClick() {
		return onClick;
	}

	/**
	 * @param click
	 *            the click to set
	 */
	public void setOnClick(String click) {
		this.onClick = click;
	}

}
