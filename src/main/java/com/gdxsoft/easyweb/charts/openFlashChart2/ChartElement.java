package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class ChartElement extends AllBase{
	private String text;
	private String style;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("text", text);
		map.put("style", style);
		return UJSon.createParameter(map, false);
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
	 * @return the style
	 */
	public String getStyle() {
		if (style == null)
			style = "{font-size: 20px; color:#0000ff; font-family: 宋体; text-align: center;}";
		return this.style;

	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}
}
