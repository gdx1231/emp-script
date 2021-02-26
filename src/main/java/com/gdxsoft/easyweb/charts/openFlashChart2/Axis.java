package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class Axis extends AllBase {
	private Integer stroke = null;
	private String colour = null;
	private String gridColour = null;

	private Integer steps = null;
	private Integer _3D = null;

	private Double min = null;
	private Double max = null;
	private Boolean offset = null;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stroke", stroke);
		map.put("colour", colour);
		map.put("grid-colour", gridColour);
		map.put("steps", steps);
		map.put("3d", _3D);
		map.put("max", max);
		map.put("min", min);
		map.put("offset", offset);
		// map.put("mouseStyle", mouseStyle);

		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public Axis() {
		offset = true;
	}

	public void SetColors(String color, String gridcolor) {
		this.colour = color;
		this.gridColour = gridcolor;
	}

	/**
	 * @return the stroke
	 */
	public Integer getStroke() {
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
	 * @return the gridColour
	 */
	public String getGridColour() {
		return gridColour;
	}

	/**
	 * @param gridColour
	 *            the gridColour to set
	 */
	public void setGridColour(String gridColour) {
		this.gridColour = gridColour;
	}

	/**
	 * @return the steps
	 */
	public Integer getSteps() {
		return steps;
	}

	/**
	 * @param steps
	 *            the steps to set
	 */
	public void setSteps(int steps) {
		this.steps = steps;
	}

	/**
	 * @return the _3D
	 */
	public Integer getAxis3D() {
		return _3D;
	}

	/**
	 * @param _3d
	 *            the _3D to set
	 */
	public void setAxis3D(int _3d) {
		_3D = _3d;
	}

	/**
	 * @return the min
	 */
	public Double getMin() {
		return min;
	}

	/**
	 * @param min
	 *            the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public Double getMax() {
		return max;
	}

	/**
	 * @param max
	 *            the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * @return the offset
	 */
	public Boolean isOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(boolean offset) {
		this.offset = offset;
	}
}
