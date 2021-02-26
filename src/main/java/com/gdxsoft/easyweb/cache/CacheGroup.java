package com.gdxsoft.easyweb.cache;

import java.util.HashMap;

public class CacheGroup<T> {

	private String _Code;
	private HashMap<String, T> _Items;

	public CacheGroup() {
		this._Items = new HashMap<String, T>();
	}

	public T getItem(String code) {
		if (this._Items.containsKey(code)) {
			return this._Items.get(code);
		} else {
			return null;
		}
	}

	public synchronized void addItem(String code, T o) {
		if (this._Items.containsKey(code)) {
			this._Items.remove(code);
		}
		this._Items.put(code, o);
	}

	/**
	 * @return the _Code
	 */
	public String getCode() {
		return _Code;
	}

	/**
	 * @param code
	 *            the _Code to set
	 */
	public void setCode(String code) {
		_Code = code;
	}

	
	/**
	 * @return the _Items
	 */
	public HashMap<String, T> getItems() {
		return _Items;
	}
}
