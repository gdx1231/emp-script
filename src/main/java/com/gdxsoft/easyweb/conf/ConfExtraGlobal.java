package com.gdxsoft.easyweb.conf;

public class ConfExtraGlobal {
	private String lang;
	private String date;
	private String time;
	private String currency;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * 语言
	 * @return
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * 语言
	 * @param lang
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

}
