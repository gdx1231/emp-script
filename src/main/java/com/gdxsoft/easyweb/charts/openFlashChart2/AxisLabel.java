package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;

import com.gdxsoft.easyweb.utils.UJSon;

public class AxisLabel extends AllBase {
	private String colour;
	private String text;
	private int size;
	private String rotate;
	private boolean visible = true;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("text", text);
		map.put("size", size);
		map.put("rotate", rotate);
		map.put("visible", visible);
		map.put("colour", colour);

		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}

	public AxisLabel() {
		this.visible = true;
		size = 12;
	}

	public AxisLabel(String text) {
		this.text = text;
		this.visible = true;
		size = 12;
	}

	public static AxisLabel instanceNew(String text) {
		return new AxisLabel(text);
	}

	public AxisLabel(String text, String colour, int size, String rotate) {
		this.text = text;
		this.colour = colour;
		this.size = size;
		this.rotate = rotate;

		this.visible = true;
	}

	/**
	 * 设置为垂直模式
	 * @param value
	 */
	public void setVertical(boolean value){
		 if(value)
             this.rotate = "vertical"; 
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
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
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
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 *            the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
