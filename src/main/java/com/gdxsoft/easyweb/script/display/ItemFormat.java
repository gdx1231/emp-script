package com.gdxsoft.easyweb.script.display;

import java.math.BigDecimal;

import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;

public class ItemFormat {
	private String name;
	private String dataType;
	private String dataFieldName;
	private String format;
	private BigDecimal numberScale;
	private boolean isDate;// 是否是处理为日期
	// 用户和系统的时区差值
	private int timeDiffMinutes = 0;

	public void init(UserXItem uxi,  int timeDiffMinutes) {
		this.timeDiffMinutes = timeDiffMinutes;
		this.name = uxi.getName();
		this.dataFieldName = name;
		
		this.dataType = "string";
		this.format = "";
		this.numberScale = new BigDecimal("1");
		
		UserXItemValues us;
		try {
			us = uxi.getItem("DataItem");
			if (us.count() == 0) {
				return;
			}
			UserXItemValue u = us.getItem(0);
			if (u.testName("DataField") && u.getItem("DataField").trim().length() > 0) {
				dataFieldName = u.getItem("DataField");
			}
			if (u.testName("DataType")) {
				dataType = u.getItem("DataType");
			}
			if (u.testName("Format")) {
				format = u.getItem("Format");
			}
			if (u.testName("NumberScale")) {
				String scale = u.getItem("NumberScale");
				if (scale.trim().length() > 0) {
					try {
						numberScale = new BigDecimal(scale);
					} catch (Exception err) {
					}
				}
			}
		} catch (Exception e) {
			return;
		}

		// 是否是处理为日期
		this.isDate = format != null
				&& (format.toUpperCase().indexOf("DATE") >= 0 || format.toUpperCase().indexOf("TIME") >= 0);

	}

	public String formatValue(Object val, String lang) throws Exception {
		if (val == null) {
			return null;
		}
		Object o = val;
		if (isDate && timeDiffMinutes != 0) {
			o = Utils.getTimeDiffValue(val, timeDiffMinutes);
		} else {
			if (numberScale.longValue() > 1) {
				o = UFormat.calcNumberScale(val, numberScale);
			} else {
			}
		}
		return HtmlUtils.formatValue(format, o, lang);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the numberScale
	 */
	public BigDecimal getNumberScale() {
		return numberScale;
	}

	/**
	 * @param numberScale the numberScale to set
	 */
	public void setNumberScale(BigDecimal numberScale) {
		this.numberScale = numberScale;
	}

	/**
	 * @return the isDate
	 */
	public boolean isDate() {
		return isDate;
	}

	/**
	 * @param isDate the isDate to set
	 */
	public void setDate(boolean isDate) {
		this.isDate = isDate;
	}

	/**
	 * @return the timeDiffMinutes
	 */
	public int getTimeDiffMinutes() {
		return timeDiffMinutes;
	}

	/**
	 * @param timeDiffMinutes the timeDiffMinutes to set
	 */
	public void setTimeDiffMinutes(int timeDiffMinutes) {
		this.timeDiffMinutes = timeDiffMinutes;
	}

	/**
	 * @return the dataFieldName
	 */
	public String getDataFieldName() {
		return dataFieldName;
	}

	/**
	 * @param dataFieldName the dataFieldName to set
	 */
	public void setDataFieldName(String dataFieldName) {
		this.dataFieldName = dataFieldName;
	}
}
