package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class CandleValue extends BarValue {
	protected Double high;
	protected Double low;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("high", high);
		map.put("low", low);
		return super.toJSON() + ",\n" + UJSon.createParameter(map, false);
	}

	public CandleValue() {
	}

	public CandleValue(double high, double top, double bottom, double low) {
		this.high = high;
		this.top = top;
		this.bottom = bottom;
		this.low = low;
	}

	/**
	 * @return the high
	 */
	public Double getHigh() {
		return high;
	}

	/**
	 * @param high
	 *            the high to set
	 */
	public void setHigh(double high) {
		this.high = high;
	}

	/**
	 * @return the low
	 */
	public Double getLow() {
		return low;
	}

	/**
	 * @param low
	 *            the low to set
	 */
	public void setLow(double low) {
		this.low = low;
	}
}
