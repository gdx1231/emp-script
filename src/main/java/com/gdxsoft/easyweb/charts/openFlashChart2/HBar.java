package com.gdxsoft.easyweb.charts.openFlashChart2;

public class HBar extends BarBase {
	public HBar() {
		this.setChartType("hbar");
	}

	public void add(HBarValue hBarValue) {
		this.getValues().add(hBarValue);
	}
}
