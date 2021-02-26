package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class BarBase extends Chart<Double> {
	private Animation onshow = new Animation();

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("on-show", onshow);

		return super.toJSON() + ",\n" + UJSon.createParameter(map, false);
	}

	public BarBase() {
		this.setChartType("bar");
	}

	/**
	 * @return the onshow
	 */
	public Animation getOnShow() {
		return onshow;
	}

	/**
	 * @param onshow
	 *            the onshow to set
	 */
	public void setOnShow(Animation onshow) {
		this.onshow = onshow;
	}
}
