package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class Animation extends AnimationBase {
	private String type;
	private Double cascade;
	private Double delay;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("cascade", cascade);
		map.put("delay", delay);
		return UJSon.createParameter(map, false);
	}

	public Animation() {
	}

	public Animation(String type, double cascade, double delay) {
		this.type = type;
		this.cascade = cascade;
		this.delay = delay;
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
	 * @return the cascade
	 */
	public Double getCascade() {
		return cascade;
	}

	/**
	 * @param cascade
	 *            the cascade to set
	 */
	public void setCascade(double cascade) {
		this.cascade = cascade;
	}

	/**
	 * @return the delay
	 */
	public Double getDelay() {
		return delay;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(double delay) {
		this.delay = delay;
	}
}
