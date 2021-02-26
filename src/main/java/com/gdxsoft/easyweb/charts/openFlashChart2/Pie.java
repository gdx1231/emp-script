package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class Pie extends Chart<PieValue> {
	private int border;
	private String[] colours;
	private double alpha;
	private PieAnimationSeries animate;
	private double startAngle;
	private boolean gradientfill;
	private boolean noLabels;
	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("border", border);
		map.put("colours", colours);
		map.put("alpha", alpha);
		map.put("animate", animate);
		map.put("start-angle", startAngle);
		map.put("gradient-fill", gradientfill);
		map.put("no-labels", noLabels);
		
		
		return super.toJSON()+",\n"+ UJSon.createParameter(map, false);
	}
	public Pie() {
		this.setChartType("pie");
		this.border = 2;
		this.colours = new String[] { "#d01f3c", "#356aa0", "#C79810" };
		this.alpha = 0.6;
		// this.animate = true;
		// gradientfill = true;

	}

	/**
	 * @return the border
	 */
	public int getBorder() {
		return border;
	}

	/**
	 * @param border
	 *            the border to set
	 */
	public void setBorder(int border) {
		this.border = border;
	}

	/**
	 * @return the colours
	 */
	public String[] getColours() {
		return colours;
	}

	/**
	 * @param colours
	 *            the colours to set
	 */
	public void setColours(String[] colours) {
		this.colours = colours;
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha
	 *            the alpha to set
	 */
	public void setAlpha(double value) {
		if (value < 0)
			alpha = 0;
		else if ((value >= 0) && (value <= 1))
			alpha = value;
		else if ((value > 1) && (value <= 100))
			alpha = value / 100;
		else
			alpha = 1.0;
	}

	/**
	 * @return the animate
	 */
	public PieAnimationSeries getAnimate() {
		return animate;
	}

	/**
	 * @param animate
	 *            the animate to set
	 */
	public void setAnimate(PieAnimationSeries animate) {
		this.animate = animate;
	}

	/**
	 * @return the startAngle
	 */
	public double getStartAngle() {
		return startAngle;
	}

	/**
	 * @param startAngle
	 *            the startAngle to set
	 */
	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	/**
	 * @return the gradientfill
	 */
	public boolean isGradientfill() {
		return gradientfill;
	}

	/**
	 * @param gradientfill
	 *            the gradientfill to set
	 */
	public void setGradientfill(boolean gradientfill) {
		this.gradientfill = gradientfill;
	}

	/**
	 * @return the noLabels
	 */
	public boolean isNoLabels() {
		return noLabels;
	}

	/**
	 * @param noLabels
	 *            the noLabels to set
	 */
	public void setNoLabels(boolean noLabels) {
		this.noLabels = noLabels;
	}

}
