package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class BarSketch extends BarBase {
	private String outlinecolour;
	private int offset;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("outline-colour", outlinecolour);
		map.put("offset", offset);
		return super.toJSON() + ",\n" + UJSon.createParameter(map, false);
	}

	public BarSketch(String colour, String outlinecolor, int offset) {
		this.setChartType("bar_sketch");
		this.setColour(colour);
		this.outlinecolour = outlinecolor;
		this.offset = offset;
	}

	/**
	 * @return the outlinecolour
	 */
	public String getOutLineColour() {
		return outlinecolour;
	}

	/**
	 * @param outlinecolour
	 *            the outlinecolour to set
	 */
	public void setOutLineColour(String outlinecolour) {
		this.outlinecolour = outlinecolour;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
