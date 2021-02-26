package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.ArrayList;
import java.util.List;

public class YAxisLabels extends AxisLabels {
	public void setLabels(List<String> labelsvalue) {
		int pos = 0;
		if (labels == null)
			labels = new ArrayList<Object>();
		for (int i = 0; i < labelsvalue.size(); i++) {
			labels.add(labelsvalue.get(i));
			pos++;
		}
	}
}
