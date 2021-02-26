package com.gdxsoft.easyweb.script.display;

import java.util.regex.Pattern;

public class TreeOtherIcon {

	private String _Open;
	private String _Close;
	private String _Filter;
	private String _Test;
	private Pattern _Pattern;

	public String getOpen() {
		return _Open;
	}

	public void setOpen(String open) {
		_Open = open;
	}

	public String getClose() {
		return _Close;
	}

	public void setClose(String close) {
		_Close = close;
	}

	public String getFilter() {
		return _Filter;
	}

	public void setFilter(String filter) {
		_Filter = filter;
		_Pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
	}

	public String getTest() {
		return _Test;
	}

	public void setTest(String test) {
		_Test = test;
	}

	public Pattern getPattern() {
		return _Pattern;
	}

}
