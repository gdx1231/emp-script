package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class RadarAxis extends XAxis {
	private XAxisLabels spokeLabels;
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("spoke-labels", spokeLabels);
		 
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public RadarAxis(double max) {
		super.setMax(max);
	}

	public RadarAxis() {

	}

	/**
	 * @return the spokeLabels
	 */
	public XAxisLabels getSpokeLabels() {
		return spokeLabels;
	}

	/**
	 * @param spokeLabels
	 *            the spokeLabels to set
	 */
	public void setSpokeLabels(XAxisLabels spokeLabels) {
		this.spokeLabels = spokeLabels;
	}
}
