package com.gdxsoft.easyweb.script.display;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;

public class ItemFormat {
	private ItemValues itemValues;

	private String name;
	private String dataType;
	private String dataFieldName;
	private String format;
	private BigDecimal numberScale;
	private boolean isDate;// 是否是处理为日期
	// 用户和系统的时区差值
	private int timeDiffMinutes = 0;

	private String refSql;
	private String refKey;
	private String refShow;
	private boolean isRef;
	private boolean refShowMulti;
	private boolean refShowMultiValues;

	private String lang;

	private String splitChar;

	private DTRow row;

	private String refShowStyle;

	public void init(UserXItem uxi, int timeDiffMinutes) {
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

		uxi.setUsingRef(false);
		try {
			if (uxi.getItem("DataRef").count() == 0) {
				return;
			}
			UserXItemValue vs = uxi.getItem("DataRef").getItem(0);
			refSql = vs.getItem("RefSql");
			refKey = vs.getItem("RefKey");
			refShow = vs.getItem("RefShow");
			this.refShowStyle = vs.getItem("RefShowStyle");
			if (refSql == null || refKey == null || refShow == null || refSql.trim().length() == 0
					|| refKey.trim().length() == 0 || refShow.trim().length() == 0) {
				this.isRef = false;
			} else {
				this.isRef = true;
				uxi.setUsingRef(true);
			}

			// 是否多个ID的匹配，例如：CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
			this.refShowMulti = "yes".equals(vs.checkItemExists("RefMulti") ? vs.getItem("RefMulti") : "");
			// 单id时，是否显示多个值
			this.refShowMultiValues = "yes1".equals(vs.checkItemExists("RefMulti") ? vs.getItem("RefMulti") : "");
			this.splitChar = vs.checkItemExists("RefMultiSplit") ? vs.getItem("RefMultiSplit").trim() : ",";
		} catch (Exception e) {
			return;
		}

	}

	public List<String> getRefMultiValues(String val) {
		List<String> al = new ArrayList<>();
		if (!refShowMulti && !this.refShowMultiValues) { // 单个id匹配
			al.add(val);
			return al;
		}

		String[] refKeys = new String[1];
		refKeys[0] = refKey;
		DTTable dt = itemValues.getRefTable(refSql, refKeys);
		if (dt == null) {
			al.add(val);
			return al;
		}

		int cellIndex = this.getShowCellIndex(dt);

		if (cellIndex == -1) {
			// 字段不存在
			al.add(val);
			return al;
		}

		// 单个id匹配，显示这个id下的多个值
		if (this.refShowMultiValues) {
			for (int i = 0; i < dt.getCount(); i++) {
				DTCell refValCell = dt.getCell(i, cellIndex);
				if (refValCell.isNull()) {
					continue;
				}
				String refVal = refValCell.toString().trim();

				if (refVal.equals(val)) {
					al.add(refVal);
				}
			}

			return al;
		}

		// 多个ID的匹配， 例如 CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
		// 字符串分割字符，默认英文逗号
		if (splitChar.length() == 0) {
			splitChar = ","; // 默认英文逗号
		}
		String[] vals = val.split(splitChar); // 正则表达式需要用户定义
		for (int i = 0; i < vals.length; i++) {
			String v = vals[i].trim();
			DTRow row1 = dt.getRowByKey(refKey, v);
			if (row1 == null) {
				al.add(v);
				continue;
			}
			DTCell refValCell = row1.getCell(cellIndex);
			String refVal = refValCell.isNull() ? "" : refValCell.getString().trim();
			al.add(refVal);
		}
		return al;

	}

	public int getShowCellIndex(DTTable dt) {
		String show = this.getShowCellFieldName(dt);
		int cellIndex = dt.getColumns().getNameIndex(show);
		return cellIndex;
	}

	public String getShowCellFieldName(DTTable dt) {
		String show = refShow;
		if (lang.equalsIgnoreCase("enus")) {
			if (dt.getColumns().testName(show + "_en")) {
				show += "_en";
			} else if (dt.getColumns().testName(show + "en")) {
				show += "en";
			} else if (dt.getColumns().testName(show + "_enus")) {
				show += "_enus";
			} else if (dt.getColumns().testName(show + "enus")) {
				show += "enus";
			}
		}
		return show;
	}

	public List<Pair<String, DTRow>> getRefRows(String val) {
		String[] refKeys = new String[1];
		refKeys[0] = refKey;
		DTTable dt = itemValues.getRefTable(refSql, refKeys);

		if (dt == null) {
			return null;
		}
		List<Pair<String, DTRow>> al = new ArrayList<>();

		if (!refShowMulti && !refShowMultiValues) {
			DTRow row = dt.getRowByKey(refKey, val);
			Pair<String, DTRow> m = Pair.of(val, row);
			al.add(m);
			return al;
		}
		
		// 单个id下的多个值
		if (this.refShowMultiValues) {
			int cellIndex = dt.getColumns().getNameIndex( this.refKey );
			for (int i = 0; i < dt.getCount(); i++) {
				DTCell refValCell = dt.getCell(i, cellIndex);
				if (refValCell.isNull()) {
					continue;
				}
				String refVal = refValCell.toString().trim();

				if (refVal.equals(val)) {
					Pair<String, DTRow> m = Pair.of(val, refValCell.getRow());
					al.add(m);
				}
			}

			return al;
		}

		// 多个ID的匹配， 例如 CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
		// 字符串分割字符，默认英文逗号
		if (splitChar.length() == 0) {
			splitChar = ","; // 默认英文逗号
		}
		String[] vals = val.split(splitChar); // 正则表达式需要用户定义
		for (int i = 0; i < vals.length; i++) {
			String v = vals[i].trim();
			DTRow row1 = dt.getRowByKey(refKey, v);
			Pair<String, DTRow> m = Pair.of(val, row1);
			al.add(m);
		}
		return al;
	}

	public String getRefValue(String val) {
		String[] refKeys = new String[1];
		refKeys[0] = refKey;
		DTTable dt = itemValues.getRefTable(refSql, refKeys);
		if (dt == null) {
			return val;
		}

		int cellIndex = this.getShowCellIndex(dt);

		if (cellIndex == -1) {
			// 字段不存在
			return val;
		}

		if (!refShowMulti) { // 单个id匹配
			DTRow row = dt.getRowByKey(refKey, val);
			if (row == null) {
				return val;
			}
			DTCell refValCell = row.getCell(cellIndex);
			String refVal = refValCell.isNull() ? "" : refValCell.getString().trim();
			this.row = row;
			return refVal;
		}

		// 多个ID的匹配， 例如 CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
		// 字符串分割字符，默认英文逗号
		if (splitChar.length() == 0) {
			splitChar = ","; // 默认英文逗号
		}
		String[] vals = val.split(splitChar); // 正则表达式需要用户定义
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vals.length; i++) {
			String v = vals[i].trim();
			DTRow row1 = dt.getRowByKey(refKey, v);
			if (i > 0) {
				sb.append(", ");
			}
			if (row1 == null) {
				sb.append(v);
				continue;
			}
			DTCell refValCell = row1.getCell(cellIndex);
			String refVal = refValCell.isNull() ? "" : refValCell.getString().trim();
			sb.append(refVal);
		}
		return sb.toString();

	}

	public String formatValue(Object val) throws Exception {
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

	/**
	 * @return the refSql
	 */
	public String getRefSql() {
		return refSql;
	}

	/**
	 * @param refSql the refSql to set
	 */
	public void setRefSql(String refSql) {
		this.refSql = refSql;
	}

	/**
	 * @return the refKey
	 */
	public String getRefKey() {
		return refKey;
	}

	/**
	 * @param refKey the refKey to set
	 */
	public void setRefKey(String refKey) {
		this.refKey = refKey;
	}

	/**
	 * @return the refShow
	 */
	public String getRefShow() {
		return refShow;
	}

	/**
	 * @param refShow the refShow to set
	 */
	public void setRefShow(String refShow) {
		this.refShow = refShow;
	}

	/**
	 * @return the isRef
	 */
	public boolean isRef() {
		return isRef;
	}

	/**
	 * @param isRef the isRef to set
	 */
	public void setRef(boolean isRef) {
		this.isRef = isRef;
	}

	/**
	 * 是否多个ID的匹配，例如：CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
	 * 
	 * @return the refShowMulti
	 */
	public boolean isRefShowMulti() {
		return refShowMulti;
	}

	/**
	 * 是否多个ID的匹配，例如：CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
	 * 
	 * @param refShowMulti the refShowMulti to set
	 */
	public void setRefShowMulti(boolean refShowMulti) {
		this.refShowMulti = refShowMulti;
	}

	/**
	 * @return the itemValues
	 */
	public ItemValues getItemValues() {
		return itemValues;
	}

	/**
	 * @param itemValues the itemValues to set
	 */
	public void setItemValues(ItemValues itemValues) {
		this.itemValues = itemValues;
	}

	public void setLang(String lang) {
		this.lang = lang;

	}

	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @return the row
	 */
	public DTRow getRow() {
		return row;
	}

	/**
	 * @return the refShowStyle
	 */
	public String getRefShowStyle() {
		return refShowStyle;
	}

	/**
	 * @param refShowStyle the refShowStyle to set
	 */
	public void setRefShowStyle(String refShowStyle) {
		this.refShowStyle = refShowStyle;
	}
}
