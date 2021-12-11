package com.gdxsoft.easyweb.script;

public class PageValue {

	private Object _Value;
	private String _Name;
	private String _Tag;
	private String _DataType;
	private int _Length = 4000;
	private PageValueTag _PVTag; // 参数来源

	public PageValue() {
		_PVTag = PageValueTag.OTHER;
	}

	public void autoDetectDataType() {
		if (this._Value == null)
			return;

		String tName = this._Value.getClass().getName().toLowerCase();
		if (tName.equals("java.lang.String")) {
			this._DataType = "String";
			return;
		}
		if (tName.equals("java.util.date")) {
			this._DataType = "Date";
			return;
		}

		String s1 = this._Value.toString().trim();
		if (s1.length() == 0) {
			this._DataType = "String";
			return;
		}
		try {
			Double dv = Double.parseDouble(s1);
			if (s1.indexOf(".") > 0) {
				this._DataType = "Number";
				this._Value = dv.doubleValue();
			} else {
				if ((dv.intValue() + "").equalsIgnoreCase(s1)) {
					this._DataType = "Int";
					this._Value = dv.intValue();
				}
			}
		} catch (Exception e) {
			this._DataType = "String";
			this._Length = this._Value.toString().length();
		}

	}

	public PageValue(String name, String value) {
		this._Name = name;
		this._Value = value;
		if (value != null) {
			this._Length = value.length();
		}
		this._DataType = "string";
	}

	public PageValue(String name, String dataType, Object value, int length) {
		this._Name = name;
		this._Value = value;
		this._DataType = dataType;
		this._Length = length;
	}

	/**
	 * @return the _Value
	 */
	public Object getValue() {
		return _Value;
	}

	/**
	 * @param value the _Value to set
	 */
	public void setValue(Object value) {
		_Value = value;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * 获取字符串值
	 * 
	 * @return
	 */
	public String getStringValue() {
		if (this._Value == null) {
			return null;
		} else {
			return this._Value.toString();
		}
	}

	/**
	 * @return the _Tag
	 */
	public String getTag() {
		return _Tag;
	}

	/**
	 * @param tag the _Tag to set
	 */
	public void setTag(String tag) {
		_Tag = tag;
	}

	/**
	 * @return the _DateType
	 */
	public String getDataType() {
		return _DataType;
	}

	/**
	 * @param dateType the _DateType to set
	 */
	public void setDataType(String dataType) {
		_DataType = dataType;
	}

	/**
	 * @return the _Length
	 */
	public int getLength() {
		if (this._Length <= 0) {
			this._Length = 4000;
		}
		return _Length;
	}

	/**
	 * @param length the _Length to set
	 */
	public void setLength(int length) {
		_Length = length;
	}

	/**
	 * 参数来源
	 * 
	 * @return the _PVTag
	 */
	public PageValueTag getPVTag() {
		return _PVTag;
	}

	/**
	 * 参数来源
	 * 
	 * @param tag the _PVTag to set
	 */
	public void setPVTag(PageValueTag tag) {
		_PVTag = tag;
	}

	public PageValue clone() {
		PageValue pv = new PageValue();
		pv.setTag(this.getTag());

		pv.setDataType(this._DataType);
		pv.setLength(this._Length);
		pv.setPVTag(this._PVTag);
		pv.setValue(this._Value);
		pv.setName(this._Name);
		return pv;
	}
}
