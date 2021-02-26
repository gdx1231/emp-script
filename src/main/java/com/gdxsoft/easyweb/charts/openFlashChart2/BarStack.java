package com.gdxsoft.easyweb.charts.openFlashChart2;

import java.util.List;

public class BarStack extends BarBase {
	public BarStack()
    {
        super.setChartType("bar_stack");
    }
    public void add(BarStackValue barStackValue)
    {
        this.getValues().add(barStackValue);
    }
    public void addStack(List<BarStackValue> barStackValues)
    {
        super.getValues().add(barStackValues);
    }
}
