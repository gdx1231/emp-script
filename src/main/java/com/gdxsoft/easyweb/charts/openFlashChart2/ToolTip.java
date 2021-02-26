package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

/**
 * /{ // shadow: true, // rounded: 1, // stroke: 2, // colour: '#808080', //
 * background: '#f0f0f0', // title: "color: #0000F0; font-weight: bold;
 * font-size: 12;", // body: "color: #000000; font-weight: normal; font-size:
 * 12;", // mouse: Tooltip.CLOSEST, // text: "_default" //}
 * 
 * @author Administrator
 * 
 */
public class ToolTip extends AllBase{
	private String text = "_default";
	private boolean shadow = true;
	private int rounded = 1;
	private int stroke = 2;
	private String colour;// = "#808080";
	private String background;// = "#f0f0f0";
	private String titleStyle;// = "color: #0000F0; font-weight: bold;
								// font-size: 12;";
	private String bodyStyle;// = "color: #000000; font-weight: normal;
								// font-size: 12;";
	private ToolTipStyle mouseStyle;// = ToolTipStyle.CLOSEST;

	public int mouse;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("text", text);
		map.put("shadow", shadow);
		map.put("rounded", rounded);
		map.put("stroke", stroke);
		map.put("colour", colour);
		map.put("background", background);
		map.put("title", titleStyle);
		map.put("body", bodyStyle);
		//map.put("mouseStyle", mouseStyle);
		
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the shadow
	 */
	public boolean isShadow() {
		return shadow;
	}

	/**
	 * @param shadow
	 *            the shadow to set
	 */
	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	/**
	 * @return the rounded
	 */
	public int getRounded() {
		return rounded;
	}

	/**
	 * @param rounded
	 *            the rounded to set
	 */
	public void setRounded(int rounded) {
		this.rounded = rounded;
	}

	/**
	 * @return the stroke
	 */
	public int getStroke() {
		return stroke;
	}

	/**
	 * @param stroke
	 *            the stroke to set
	 */
	public void setStroke(int stroke) {
		this.stroke = stroke;
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
	 * @return the background
	 */
	public String getBackground() {
		return background;
	}

	/**
	 * @param background
	 *            the background to set
	 */
	public void setBackground(String background) {
		this.background = background;
	}

	/**
	 * @return the titleStyle
	 */
	public String getTitleStyle() {
		return titleStyle;
	}

	/**
	 * @param titleStyle
	 *            the titleStyle to set
	 */
	public void setTitleStyle(String titleStyle) {
		this.titleStyle = titleStyle;
	}

	/**
	 * @return the bodyStyle
	 */
	public String getBodyStyle() {
		return bodyStyle;
	}

	/**
	 * @param bodyStyle
	 *            the bodyStyle to set
	 */
	public void setBodyStyle(String bodyStyle) {
		this.bodyStyle = bodyStyle;
	}

	/**
	 * @return the mouseStyle
	 */
	public ToolTipStyle getMouseStyle() {
		return mouseStyle;
	}

	/**
	 * @param mouseStyle
	 *            the mouseStyle to set
	 */
	public void setMouseStyle(ToolTipStyle mouseStyle) {
		this.mouseStyle = mouseStyle;
	}

	/**
	 * @return the mouse
	 */
	public int getMouse() {
		return mouse;
	}

	/**
	 * @param mouse
	 *            the mouse to set
	 */
	public void setMouse(int mouse) {
		this.mouse = mouse;
	}

	public void setProximity() {
		mouse = 1;
	}

	public String toString() {
		return this.text;
	}
}
