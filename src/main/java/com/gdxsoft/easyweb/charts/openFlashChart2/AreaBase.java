package com.gdxsoft.easyweb.charts.openFlashChart2;

public class AreaBase extends Chart<Double> {

	private int width;
	private double dotsize;
	private double halosize;
	private boolean loop;
	private String fillcolour;
	private Animation onshow = new Animation();

	protected AreaBase() {
		this.setChartType("area");
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
	 * @return the dotsize
	 */
	public double getDotSize() {
		return dotsize;
	}

	/**
	 * @param dotsize
	 *            the dotsize to set
	 */
	public void setDotSize(double dotsize) {
		this.dotsize = dotsize;
	}

	/**
	 * @return the halosize
	 */
	public double getHaloSize() {
		return halosize;
	}

	/**
	 * @param halosize
	 *            the halosize to set
	 */
	public void setHaloSize(double halosize) {
		this.halosize = halosize;
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
	 * @return the fillcolour
	 */
	public String getFillColour() {
		return fillcolour;
	}

	/**
	 * @param fillcolour
	 *            the fillcolour to set
	 */
	public void setFillColour(String fillcolour) {
		this.fillcolour = fillcolour;
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
