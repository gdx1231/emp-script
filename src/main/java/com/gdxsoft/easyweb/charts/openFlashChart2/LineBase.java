package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class LineBase extends Chart<LineDotValue> {
	private int width;
	private int dotSize;
	private int haloSize;
	private String onClick;
	private boolean loop;
	private Animation onShow = new Animation();

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("width", width);
		map.put("dot-size", dotSize);
		map.put("halo-size", haloSize);
		map.put("halo-size", onClick);
		map.put("loop", loop);
		map.put("on-show", onShow);
		return UJSon.createParameter(map, false) + ",\n" + super.toJSON();
	}

	public LineBase() {
		this.setChartType("line");
		this.getDotStyle().setType(DotType.SOLID_DOT);

	}

	public void SetOnClickFunction(String func) {
		this.getDotStyle().setOnClick(func);
		this.onClick = func;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the dotSize
	 */
	public int getDotSize() {
		return dotSize;
	}

	/**
	 * @param dotSize
	 *            the dotSize to set
	 */
	public void setDotSize(int dotSize) {
		this.dotSize = dotSize;
	}

	/**
	 * @return the haloSize
	 */
	public int getHaloSize() {
		return haloSize;
	}

	/**
	 * @param haloSize
	 *            the haloSize to set
	 */
	public void setHaloSize(int haloSize) {
		this.haloSize = haloSize;
	}

	/**
	 * @return the onClick
	 */
	public String getOnClick() {
		return onClick;
	}

	/**
	 * @param onClick
	 *            the onClick to set
	 */
	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

	/**
	 * @return the loop
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * @param loop
	 *            the loop to set
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	/**
	 * @return the onShow
	 */
	public Animation getOnShow() {
		return onShow;
	}

	/**
	 * @param onShow
	 *            the onShow to set
	 */
	public void setOnShow(Animation onShow) {
		this.onShow = onShow;
	}
}
