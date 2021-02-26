package com.gdxsoft.easyweb.charts.openFlashChart2;

public class Candle extends BarBase {
	public Candle()
    {
        this.setChartType( "candle");
    }
    public void add(CandleValue candleValue)
    {
        this.getValues().add(candleValue);
    }
}
