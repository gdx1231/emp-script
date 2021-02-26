package com.gdxsoft.easyweb.charts.openFlashChart2;

public class BarFilled extends BarBase {

	public BarFilled() {
		super.setChartType("bar_filled");
	}
	
	public void add(BarFilledValue barFilledValue)
    {
        this.getValues().add(barFilledValue);
    }
}
