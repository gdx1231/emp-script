package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class HBarValue extends AllBase {
	private double left;
	private double right;
	private String tip;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("left", left);
		map.put("right", right);
		map.put("tip", tip);
		return UJSon.createParameter(map, false);
	}

	public HBarValue(double left, double right) {
		this.left = left;
		this.right = right;
	}

	public HBarValue(double left, double right, String tip) {
		this.left = left;
		this.right = right;
		this.tip = tip;
	}

	/**
	 * @return the left
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft(double left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public double getRight() {
		return right;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight(double right) {
		this.right = right;
	}

	/**
	 * @return the tip
	 */
	public String getTip() {
		return tip;
	}

	/**
	 * @param tip
	 *            the tip to set
	 */
	public void setTip(String tip) {
		this.tip = tip;
	}
}
