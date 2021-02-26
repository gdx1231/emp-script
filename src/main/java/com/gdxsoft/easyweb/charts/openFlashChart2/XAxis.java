package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;
import java.util.List;

import com.gdxsoft.easyweb.utils.UJSon;

public class XAxis extends Axis {
	private String tickHeight;
	private XAxisLabels labels;
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		if (sb.length() > 0) {
			sb.append(",\n");
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("tick-height", tickHeight);
		map.put("labels", labels);
		 
		 
		
		sb.append(UJSon.createParameter(map, false));
		return sb.toString();
	}
	/**
	 * @return the tickHeight
	 */
	public String getTickHeight() {
		return tickHeight;
	}

	/**
	 * @param tickHeight
	 *            the tickHeight to set
	 */
	public void setTickHeight(String tickHeight) {
		this.tickHeight = tickHeight;
	}

	/**
	 * @return the labels
	 */
	public XAxisLabels getLabels() {
		if(labels==null){
			labels=new XAxisLabels();
		}
		return labels;
	}

	/**
	 * @param labels
	 *            the labels to set
	 */
	public void setLabels(XAxisLabels labels) {
		this.labels = labels;
	}

	public void setLabels(List<String> labelsvalue) {
		if(labels==null){
			labels=new XAxisLabels();
		}
		labels.setLabels(labelsvalue);
	}
}
