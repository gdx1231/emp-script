package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class DotStyle extends AllBase {
	private String type;
	private Integer sides;
	private Double alpha;
	private Boolean isHollow;
	private String background_colour;
	private Double background_alpha;
	private Integer width;
	private String tip;
	private String colour;
	private Integer dotsize;
	private String onclick;
	
	private Animation onshow = new Animation();

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("sides", sides);
		map.put("alpha", alpha);
		map.put("hollow", isHollow);
		map.put("background_colour", background_colour);
		map.put("background_alpha", background_alpha);
		map.put("width", width);
		map.put("tip", tip);
		map.put("colour", colour);
		map.put("dot-size", dotsize);
		map.put("on-click", onclick);
		map.put("on-show", onshow);

		String v=UJSon.createParameter(map, false);
		
		return v;
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
	 * @return the sides
	 */
	public int getSides() {
		return sides;
	}

	/**
	 * @param sides
	 *            the sides to set
	 */
	public void setSides(int sides) {
		this.sides = sides;
	}

	/**
	 * @return the alpha
	 */
	public Double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha
	 *            the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the isHollow
	 */
	public Boolean isHollow() {
		return isHollow;
	}

	/**
	 * @param isHollow
	 *            the isHollow to set
	 */
	public void setHollow(boolean isHollow) {
		this.isHollow = isHollow;
	}

	/**
	 * @return the background_colour
	 */
	public String getBackgroundColour() {
		return background_colour;
	}

	/**
	 * @param background_colour
	 *            the background_colour to set
	 */
	public void setBackgroundColour(String background_colour) {
		this.background_colour = background_colour;
	}

	/**
	 * @return the background_alpha
	 */
	public Double getBackgroundAlpha() {
		return background_alpha;
	}

	/**
	 * @param background_alpha
	 *            the background_alpha to set
	 */
	public void setBackgroundAlpha(double background_alpha) {
		this.background_alpha = background_alpha;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
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

	/**
	 * @return the colour
	 */
	public String getColour() {
		return colour;
	}

	/**
	 * @param colour
	 *            the colour to set
	 */
	public void setColour(String colour) {
		this.colour = colour;
	}

	/**
	 * @return the dotsize
	 */
	public Integer getDotSize() {
		return dotsize;
	}

	/**
	 * @param dotsize
	 *            the dotsize to set
	 */
	public void setDotSize(int dotsize) {
		this.dotsize = dotsize;
	}

	/**
	 * @return the onclick
	 */
	public String getOnClick() {
		return onclick;
	}

	/**
	 * @param onclick
	 *            the onclick to set
	 */
	public void setOnClick(String onclick) {
		this.onclick = onclick;
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
