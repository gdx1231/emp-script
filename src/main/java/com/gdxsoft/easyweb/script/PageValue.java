package com.gdxsoft.easyweb.script;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.gdxsoft.easyweb.utils.Utils;

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

		if (this._Value instanceof byte[] || this._Value instanceof Byte[]) {
			this._DataType = "Binary";
		} else if (this._Value instanceof java.lang.String) {
			this._DataType = "String";
		} else if (this._Value instanceof java.util.Date) {
			this._DataType = "Date";
		} else if (this._Value instanceof java.time.LocalDate) {
			this._DataType = "Date";
		} else if (this._Value instanceof java.time.LocalTime) {
			this._DataType = "Time";
		} else if (this._Value instanceof java.time.LocalDateTime ||
				this._Value instanceof java.time.ZonedDateTime ||
				this._Value instanceof java.time.OffsetDateTime ||
				this._Value instanceof java.time.Instant) {
			this._DataType = "DateTime";
		} else if (this._Value instanceof java.lang.Long) {
			this._DataType = "BigInt";
		} else if (this._Value instanceof java.lang.Integer || this._Value instanceof java.lang.Short) {
			this._DataType = "Int";
		} else if (this._Value instanceof java.lang.Double || this._Value instanceof java.lang.Float) {
			this._DataType = "Number";
		} else if (this._Value instanceof java.math.BigDecimal) {
			this._DataType = "Number";
		} else {
			this._DataType = this._Value.getClass().getSimpleName();
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
		if (v instanceof byte[]) {
			return (byte[]) v;
		}
		if (v instanceof Byte[]) {
			Byte[] bb = (Byte[]) v;
			byte[] result = new byte[bb.length];
			for (int i = 0; i < bb.length; i++) {
				result[i] = bb[i] == null ? 0 : bb[i];
			}
			return result;
		}
		return null;
	}

	/**
	 * 获取参数的整数
	 *
	 * @return
	 */
	public Integer toInteger() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof Number) {
			return ((Number) t1).intValue();
		}
		String v1 = t1.toString().trim().replace(",", "");
		if (v1.isEmpty() || v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
			return null;
		}
		int dotIndex = v1.indexOf('.');
		if (dotIndex >= 0) {
			v1 = v1.substring(0, dotIndex);
		}
		return Integer.parseInt(v1);
	}

	/**
	 * 获取参数的长整数
	 *
	 * @return
	 */
	public Long toLong() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof Number) {
			return ((Number) t1).longValue();
		}
		String v1 = t1.toString().trim().replace(",", "");
		if (v1.isEmpty() || v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
			return null;
		}
		int dotIndex = v1.indexOf('.');
		if (dotIndex >= 0) {
			v1 = v1.substring(0, dotIndex);
		}
		return Long.parseLong(v1);
	}

	/**
	 * 获取参数的双精度
	 *
	 * @return
	 */
	public Double toDouble() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof Number) {
			return ((Number) t1).doubleValue();
		}
		String v1 = t1.toString().trim().replace(",", "");
		if (v1.isEmpty() || v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
			return null;
		}
		return Double.parseDouble(v1);
	}

	public BigDecimal toBigDecimal() {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof BigDecimal) {
			return (BigDecimal) t1;
		}
		if (t1 instanceof Number) {
			return new BigDecimal(t1.toString());
		}
		String v1 = t1.toString().trim().replace(",", "");
		if (v1.isEmpty() || v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
			return null;
		}
		return new BigDecimal(v1);
	}

	/**
	 * 获取参数的日期
	 *
	 * @return
	 */
	public java.util.Date toDate(String lang) {
		Object t1 = this.getValue();
		if (t1 == null) {
			return null;
		}
		if (t1 instanceof java.util.Date) {
			return (java.util.Date) t1;
		}
		if (t1 instanceof java.time.Instant) {
			return java.util.Date.from((java.time.Instant) t1);
		}
		if (t1 instanceof java.time.LocalDateTime) {
			return java.util.Date.from(((java.time.LocalDateTime) t1).atZone(java.time.ZoneId.systemDefault()).toInstant());
		}
		if (t1 instanceof java.time.LocalDate) {
			return java.util.Date.from(((java.time.LocalDate) t1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
		}
		if (t1 instanceof java.time.ZonedDateTime) {
			return java.util.Date.from(((java.time.ZonedDateTime) t1).toInstant());
		}
		if (t1 instanceof java.time.OffsetDateTime) {
			return java.util.Date.from(((java.time.OffsetDateTime) t1).toInstant());
		}
		if (t1 instanceof Number) {
			return new java.util.Date(((Number) t1).longValue());
		}
		String v1 = t1.toString().trim();
		if (v1.isEmpty() || v1.equalsIgnoreCase("undefined") || v1.equalsIgnoreCase("null")) {
			return null;
		}
		Timestamp t = Utils.getTimestamp(v1, lang, false);

		Date date = new Date(t.getTime());
		return date; 
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
