package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdxsoft.easyweb.utils.UJSon;

public class AxisLabels extends AllBase {
	private Integer steps=null;
	protected List<Object> labels;
	private String colour=null;
	private String rotate=null;
	private Integer fontSize=null;
	private Integer visibleSteps=null;
	private String formatString=null;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("steps", steps);
		map.put("labels", labels);
		map.put("colour", colour);
		map.put("font-size", fontSize);
		map.put("visible-steps", visibleSteps);
		map.put("text", formatString);
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public void add(AxisLabel label) {
		if (labels == null)
			labels = new ArrayList<Object>();
		labels.add(label);
	}

	public Integer getSteps() {

		if (this.steps == null)
			return null;
		return this.steps;
	}

	public void setSteps(Integer value) {
		this.steps = value;
	}

	/**
	 * @return the labels
	 */
	public List<Object> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labelsvalue) {
		if (labels == null)
			labels = new ArrayList<Object>();
		for (int i = 0; i < labelsvalue.size(); i++) {
			labels.add(labelsvalue.get(i));
		}
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
	 * @return the rotate
	 */
	public String getRotate() {
		return rotate;
	}

	/**
	 * @param rotate
	 *            the rotate to set
	 */
	public void setRotate(String rotate) {
		this.rotate = rotate;
	}

	/**
	 * @return the fontSize
	 */
	public Integer getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return the visibleSteps
	 */
	public Integer getVisibleSteps() {
		return visibleSteps;
	}

	/**
	 * @param visibleSteps
	 *            the visibleSteps to set
	 */
	public void setVisibleSteps(int visibleSteps) {
		this.visibleSteps = visibleSteps;
	}

	/**
	 * @return the formatString
	 */
	public String getFormatString() {
		return formatString;
	}

	/**
	 * @param formatString
	 *            the formatString to set
	 */
	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}
}
