package com.gdxsoft.easyweb.charts.openFlashChart2;

public class Bar extends BarBase {
	public Bar() {

	}

	public void add(BarValue barValue) {
		this.getValues().add(barValue);
	}
}
