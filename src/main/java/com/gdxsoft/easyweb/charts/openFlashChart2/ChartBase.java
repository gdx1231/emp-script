package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdxsoft.easyweb.utils.UJSon;

public class ChartBase extends AllBase{
	private String type;
	protected List<Object> values;
	private double fillalpha;
	private double alpha=0.75;
	private double fontSize = 12.0;
	private String colour;
	private String text;
	private String tooltip;
	private DotStyle dotstyle;
	private boolean attachtorightaxis;

	public String toJSON() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("fill-alpha", fillalpha);
		map.put("alpha", alpha);
		map.put("colour", colour);
		map.put("font-size", fontSize);
		map.put("text", text);
		map.put("tip", tooltip);
		map.put("dot-style", dotstyle);
		map.put("axis", this.getAttachedAxis());
		map.put("values", this.values);
		return UJSon.createParameter(map, false);
	}

	protected ChartBase() {
		this.values = new ArrayList<Object>();
		fillalpha = 0.35;
		colour = "#CC3399";
		attachtorightaxis = false;
	}

	public void attachToRightAxis(boolean attach) {
		attachtorightaxis = attach;
	}

	public void setKey(String key, double fontSize) {
		this.text = key;
		this.fontSize = fontSize;
	}

 

	/**
	 * @return the type
	 */
	public String getChartType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setChartType(String type) {
		this.type = type;
	}

	/**
	 * @return the values
	 */
	public List<Object> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<Object> values) {
		this.values = values;
	}

	/**
	 * @return the fillalpha
	 */
	public double getFillAlpha() {
		return fillalpha;
	}

	/**
	 * @param fillalpha
	 *            the fillalpha to set
	 */
	public void setFillAlpha(double fillalpha) {
		this.fillalpha = fillalpha;
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
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the fontSize
	 */
	public double getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
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
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @param tooltip
	 *            the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @return the dotstyle
	 */
	public DotStyle getDotStyle() {
		if (dotstyle == null)
			dotstyle = new DotStyle();
		return dotstyle;
	}

	/**
	 * @param dotstyle
	 *            the dotstyle to set
	 */
	public void setDotStyle(DotStyle dotstyle) {
		this.dotstyle = dotstyle;
	}

	/**
	 * @return the attachtorightaxis
	 */
	public boolean isAttachToRightAxis() {
		return attachtorightaxis;
	}

	/**
	 * @param attachtorightaxis
	 *            the attachtorightaxis to set
	 */
	public void setAttachToRightAxis(boolean attachtorightaxis) {
		this.attachtorightaxis = attachtorightaxis;
	}

	public String getAttachedAxis() {

		if (attachtorightaxis)
			return "right";
		return null;
	}

	public void setAttachedAxis(String attachedAxis) {
		if (attachedAxis == null) {
			attachtorightaxis = false;
		} else {
			attachtorightaxis = attachedAxis.trim().toLowerCase().equals(
					"right");
		}
	}
}
