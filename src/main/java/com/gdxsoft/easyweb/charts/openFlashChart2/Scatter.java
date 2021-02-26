package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class Scatter extends Chart<ScatterValue> {
	private Integer dotSize;
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("dot-size", dotSize);
		 
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}
	public Scatter() {
		this.setChartType("scatter");
	}

	public Scatter(String color, int dotsize) {
		this.setChartType("scatter");
		this.setColour(color);
		this.dotSize = dotsize;
		this.getDotStyle().setType(DotType.SOLID_DOT);
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
}
