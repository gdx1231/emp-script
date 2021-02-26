package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.HashMap;
import java.util.List;

import com.gdxsoft.easyweb.utils.UJSon;

public class YAxis extends Axis {
	private Integer tickLength;
	private YAxisLabels labels;

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toJSON());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("tick-length", tickLength);
		map.put("labels", labels);

		String curJSon=UJSon.createParameter(map, false);
		if(curJSon.length()>0){
			if (sb.length() > 0) {
				sb.append(",\n");
			}
			sb.append(UJSon.createParameter(map, false));
		}
		return sb.toString();
	}

	public void setRange(double min, double max, int step) {
		super.setMax(max);
		super.setMin(min);
		super.setSteps(step);
	}

	/**
	 * @return the tickLength
	 */
	public Integer getTickLength() {
		return tickLength;
	}

	/**
	 * @param tickLength
	 *            the tickLength to set
	 */
	public void setTickLength(int tickLength) {
		this.tickLength = tickLength;
	}

	/**
	 * @return the labels
	 */
	public YAxisLabels getLabels() {
		if (this.labels == null)
			this.labels = new YAxisLabels();
		return labels;
	}

	/**
	 * @param labels
	 *            the labels to set
	 */
	public void setLabels(YAxisLabels labels) {
		this.labels = labels;
	}

	public void setLabels(List<String> labelsvalue) {
		this.labels.setLabels(labelsvalue);
	}
}
