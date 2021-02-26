package com.gdxsoft.easyweb.charts.openFlashChart2;

public class LineHollow extends LineBase {
	public LineHollow() {
		// this.ChartType = "line_hollow";
		this.getDotStyle().setHollow(true);
		this.getDotStyle().setType(DotType.HOLLOW_DOT);
	}
}

