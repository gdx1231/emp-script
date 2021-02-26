package com.gdxsoft.easyweb.charts.openFlashChart2;

public class Chart<T> extends ChartBase  {
	public Chart() {
		super.setFillAlpha(0.35);
		 
	}

	public void add(T v) {
		super.values.add(v);
	}
}
