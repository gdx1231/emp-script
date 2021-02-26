package com.gdxsoft.easyweb.cache;

import java.util.Date;

/**
 * 缓存值
 * 
 * @author Administrator
 * 
 * @param <T>
 */
public class CachedValue {

	private int _Id;
	private Date _Start;
	private int _LifeSeconds;
	private Object _Value;

	public CachedValue(int id) {
		this._Id = id;
		this._LifeSeconds = 600; // 10分钟
		this._Start = new Date();
 
	}

	public CachedValue(int id, Object value) {
		this._Id = id;
		this._LifeSeconds = 600; //  10分钟
		this._Start = new Date();
		this._Value = value;
	}

	public boolean isValid() {
		Date t = new Date();
		long ms = (t.getTime() - _Start.getTime()) / 1000;
		if (ms > _LifeSeconds) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return the _Id
	 */
	public int getId() {
		return _Id;
	}

	/**
	 * @param id
	 *            the _Id to set
	 */
	public void setId(int id) {
		_Id = id;
	}

	/**
	 * @return the _Start
	 */
	public Date getStart() {
		return _Start;
	}

	/**
	 * @param start
	 *            the _Start to set
	 */
	public void setStart(Date start) {
		_Start = start;
	}

	/**
	 * @return the _LifeSeconds
	 */
	public int getLifeSeconds() {
		return _LifeSeconds;
	}

	/**
	 * @param lifeSeconds
	 *            the _LifeSeconds to set
	 */
	public void setLifeSeconds(int lifeSeconds) {
		_LifeSeconds = lifeSeconds;
	}

	/**
	 * @return the _Value
	 */
	public Object getValue() {
		return _Value;
	}

	/**
	 * @param value
	 *            the _Value to set
	 */
	public void setValue(Object value) {
		_Value = value;
	}

}
