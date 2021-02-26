package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class PieAnimation extends AnimationBase {
	private String type;
	private Integer distance;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("distance", distance);
		
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public PieAnimation(String type, Integer distance) {
		this.type = type;
		this.distance = distance;
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
	 * @return the distance
	 */
	public Integer getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}
}
