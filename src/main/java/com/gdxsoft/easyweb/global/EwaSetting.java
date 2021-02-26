package com.gdxsoft.easyweb.global;

public class EwaSetting {

	private String[] _Weeks;
	private String[] _Months;
	private String _Date;
	private String _Time;
	private String _Currency;
	private String _Lang;
	private String _Today;
	private String _Hour;
	private String _Minute;
	private String _Second;
	
	/**
	 * @return the _Currency
	 */
	public String getCurrency() {
		return _Currency;
	}
	/**
	 * @param currency the _Currency to set
	 */
	public void setCurrency(String currency) {
		_Currency = currency;
	}
	
	/**
	 * @return the _Lang
	 */
	public String getLang() {
		return _Lang;
	}
	/**
	 * @param lang the _Lang to set
	 */
	public void setLang(String lang) {
		_Lang = lang;
	}
	
	/**
	 * @return the _Weeks
	 */
	public String[] getWeeks() {
		return _Weeks;
	}
	/**
	 * @param weeks the _Weeks to set
	 */
	public void setWeeks(String[] weeks) {
		_Weeks = weeks;
	}
	/**
	 * @return the _Months
	 */
	public String[] getMonths() {
		return _Months;
	}
	/**
	 * @param months the _Months to set
	 */
	public void setMonths(String[] months) {
		_Months = months;
	}
	/**
	 * @return the _Date
	 */
	public String getDate() {
		return _Date;
	}
	/**
	 * @param date the _Date to set
	 */
	public void setDate(String date) {
		_Date = date;
	}
	/**
	 * @return the _Time
	 */
	public String getTime() {
		return _Time;
	}
	/**
	 * @param time the _Time to set
	 */
	public void setTime(String time) {
		_Time = time;
	}
	
	public String createJs(){
		StringBuilder sb=new StringBuilder();
		
		return sb.toString();
	}
	/**
	 * @return the _Today
	 */
	public String getToday() {
		return _Today;
	}
	/**
	 * @param today the _Today to set
	 */
	public void setToday(String today) {
		_Today = today;
	}
	/**
	 * @return the _Hour
	 */
	public String getHour() {
		return _Hour;
	}
	/**
	 * @param hour the _Hour to set
	 */
	public void setHour(String hour) {
		_Hour = hour;
	}
	/**
	 * @return the _Minute
	 */
	public String getMinute() {
		return _Minute;
	}
	/**
	 * @param minute the _Minute to set
	 */
	public void setMinute(String minute) {
		_Minute = minute;
	}
	/**
	 * @return the _Second
	 */
	public String getSecond() {
		return _Second;
	}
	/**
	 * @param second the _Second to set
	 */
	public void setSecond(String second) {
		_Second = second;
	}
}
