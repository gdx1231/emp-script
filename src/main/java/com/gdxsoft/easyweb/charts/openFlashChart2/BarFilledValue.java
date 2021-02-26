package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class BarFilledValue extends BarValue {
	private String outlineColor;
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("outline-colour", outlineColor);
		 
		return super.toJSON()+",\n"+ UJSon.createParameter(map, false);
	}
	
	
	public BarFilledValue() {
		super();
	}

	public BarFilledValue(double top, double bottom) {
		super(top, bottom);
	}

	/**
	 * @return the outlineColor
	 */
	public String getOutlineColor() {
		return outlineColor;
	}

	/**
	 * @param outlineColor
	 *            the outlineColor to set
	 */
	public void setOutlineColor(String outlineColor) {
		this.outlineColor = outlineColor;
	}
}
