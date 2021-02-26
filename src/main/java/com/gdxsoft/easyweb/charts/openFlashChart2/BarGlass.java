package com.gdxsoft.easyweb.charts.openFlashChart2;

public class BarGlass extends BarBase {

	public BarGlass() {
		super.setChartType("bar_glass");
	}

	public void Add(BarGlassValue barGlassValue) {
		this.getValues().add(barGlassValue);
	}
}
