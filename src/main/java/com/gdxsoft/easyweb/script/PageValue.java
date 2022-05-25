package com.gdxsoft.easyweb.script;

import java.math.BigDecimal;

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

		if (this._Value instanceof java.lang.String) {
			this._DataType = "String";
		} else if (this._Value instanceof java.util.Date) {
			this._DataType = "Date";
		} else if (this._Value instanceof java.lang.Long) {
			this._DataType = "BigInt";
		} else if (this._Value instanceof java.lang.Integer || this._Value instanceof java.lang.Short) {
			this._DataType = "Int";
		} else if (this._Value instanceof java.lang.Double || this._Value instanceof java.lang.Float) {
			this._DataType = "Number";
		}
		String simpleName = this._Value.getClass().getSimpleName();
		if (simpleName.equals("byte[]") || simpleName.equals("Byte[]")) {
			this._DataType = "Binary";
		} else {
			this._DataType = simpleName;
		}
	}

	/**
	 * 获取二进制数组
	 * 
	 * @return
	 */
	public byte[] toBinary() {
		Object v = this.getValue();
		if (v == null) {
			return null;
		}
		// 类型为Byte[]时，需要手动转换为 byte[]
		if (v.getClass().getSimpleName().equals("Byte[]")) {
			Byte[] bb = (Byte[]) v;
			byte[] bb1 = new byte[bb.length];
			for (int i = 0; i < bb.length; i++) {
				Byte b = bb[i];
				if (b == null) {
					b = 0;
				}
				bb1[i] = b;
			}
			return bb1;
		}
		return (byte[]) v;
	}

	/**
	 * 获取参数的整数
	 * 
	 * @param pv
	 * @return
	 */
	public Integer toInteger() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof java.lang.Integer) {
			return (Integer) t1;
		}
		String v1 = t1.toString();
		if (v1.trim().length() > 0) {
			if (v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
				return null;
			}
			int intVal = Integer.parseInt(v1.split("\\.")[0]);
			return intVal;
		} else {
			return null;
		}
	}

	/**
	 * 获取参数的长整数
	 * 
	 * @param pv
	 * @return
	 */
	public Long toLong() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof java.lang.Long) {
			return (Long) t1;
		}
		String v1 = t1.toString();
		if (v1.trim().length() > 0) {
			if (v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
				return null;
			}
			long intVal = Long.parseLong(v1.split("\\.")[0]);
			return intVal;
		} else {
			return null;
		}
	}

	/**
	 * 获取参数的双精度
	 * 
	 * @param pv
	 * @return
	 */
	public Double toDouble() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof java.lang.Double) {
			return (Double) t1;
		}
		String v1 = t1.toString();
		if (v1 != null && v1.trim().length() > 0) {
			if (v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
				return null;
			}
			double dbVal = Double.parseDouble(v1);
			return dbVal;
		} else {
			return null;
		}
	}

	public BigDecimal toBigDecimal() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof java.math.BigDecimal) {
			return (BigDecimal) t1;
		}
		String v1 = t1.toString();
		if (v1.trim().length() > 0) {
			if (v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
				return null;
			}
			BigDecimal dbVal = new BigDecimal(v1);
			return dbVal;
		} else {
			return null;
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

	public String toString() {
		if (this._Value == null) {
			return null;
		} else {
			return this._Value.toString();
		}
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
