package com.gdxsoft.easyweb.script.display;

import java.io.IOException;

import com.gdxsoft.easyweb.cache.CachedValue;
import com.gdxsoft.easyweb.cache.CachedValueManager;
import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.PageValues;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.action.CommonSqls;
import com.gdxsoft.easyweb.script.display.items.ItemImage;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ItemValues {
	private Object _UserClass;
	private MList _DTTables;
	private DTTable _ListFrameTable; // ListFrame的表
	private MTable _ParameterValues;
	private UObjectValue _UObjectValue;
	private HtmlClass _HtmlClass;
	private Object _LastValue;
	private MList _JSONObjects;
	private MTable _RefTables;

	/**
	 * 获取上次提取的数据
	 * 
	 * @return the _LastValue
	 */
	public Object getLastValue() {
		return _LastValue;
	}

	public ItemValues() {
		_ParameterValues = new MTable();
	}

	/**
	 * 获取SQL的唯一表达式，将参数复到SQL尾部，避免参数造成不一致
	 * 
	 * @param sql
	 * @return
	 */
	public String getSqlUniqueExp(String sql) {
		String sql1 = sql.trim().toLowerCase();
		MListStr al = Utils.getParameters(sql1, "@");
		MStr s = new MStr();
		s.al(sql1);
		for (int i = 0; i < al.size(); i++) {
			String val = this.getRequestValue().getString(al.get(i));
			s.al("[" + i + "]. " + al.get(i) + "=" + val);
		}
		return s.toString();
	}

	/**
	 * 获取RefTable
	 * 
	 * @param sql
	 * @param keys
	 * @return
	 */
	public DTTable getRefTable(String sql, String[] keys) {

		return this.loadRefTable(sql, keys);
	}

	/**
	 * 获取REF所需的表，表默认保存在Cache中10分钟 EWA_R 强制刷新数据
	 * 
	 * @param sql
	 * @param keys
	 * @return
	 */
	private DTTable loadRefTable(String sql, String[] keys) {
		DTTable dt = null;
		String sql1 = sql.trim().toLowerCase();
		if (this._RefTables == null) {
			_RefTables = new MTable();
		} else {
			if (_RefTables.containsKey(sql1)) {
				return (DTTable) _RefTables.get(sql1);
			}
		}
		if (sql1.startsWith("{action") && sql1.endsWith("}")) {
			// 调用Action 执行返回数据集,
			String action = sql1.replace("{action", "").replace("}", "").replace(":", "");
			try {
				this.getHtmlClass().getHtmlCreator().executeAction(action);

				// 获取最后一个表
				dt = (DTTable) this.getDTTables().getLast();

				// 从数据列表中移除表，避免出现数据不一致错误
				this.getDTTables().removeValue(dt);
				dt.getAttsTable().add("TYPE", "ACTION");
				// 不保存Cache

				_RefTables.add(sql1, dt);
				return dt;
			} catch (Exception err) {
				System.out.println(err.getMessage());
				return null;
			}
		} else if (CommonSqls.isCommonDataExp(sql1)) { // 通用数据
			String cd = CommonSqls.getCommonDataName(sql1);
			String sqla = ((MTable) CommonSqls.get(cd)).get("SQL").toString();

			CommonSqls cs = new CommonSqls();
			String exp = "ItemValues.loadRefTable :: [" + getSqlUniqueExp(sqla) + "]";

			// 表达式保存索引模式
			for (int i = 0; i < keys.length; i++) {
				exp = exp + "::KEY=" + keys[i];
			}
			// EWA_R 强制刷新数据
			if (this.getRequestValue().getString("EWA_R") == null) {
				CachedValue c = CachedValueManager.getValue(exp);
				if (c != null) {
					return (DTTable) c.getValue();
				}
			}

			// 获取数据集
			MList lst = cs.executeSelect(cd, getDataConn());
			if (lst != null) {
				dt = (DTTable) lst.get(0);
				dt.getColumns().setKeys(keys);
				dt.rebuildIndex();
				CachedValueManager.addValue(exp, dt);
				dt.getAttsTable().add("TYPE", "COMMON_DATA");

				// common data 定义的属性
				dt.getAttsTable().add("FIELDVALUE", lst.get(1));
				dt.getAttsTable().add("FIELDDISPLAY", lst.get(2));
				return dt;
			}
		} else { // 标准SQL查询
			if (SqlUtils.checkChnOrderByDatabase(this.getDataConn().getDatabaseType())) {
				// 处理中文排序 CONVERT(username USING gbk)
				SqlPart sp = new SqlPart();
				try {
					sp.setSql(sql);
					sql = sp.rebuildSql(null, null, this.getDataConn().getDatabaseType());
				} catch (Exception err) {
					// 可能没有 where条件
				}
			}
			if (this.getRequestValue().getString("EWA_R") == null && sql.toLowerCase().indexOf("bas_tag") > 0) { // bas_tag
				try {
					dt = DTTable.getCachedTable(sql, 100, this.getDataConn());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				} catch (ClassNotFoundException e) {
					System.out.println(e.getMessage());
				}

			} else {
				dt = DTTable.getJdbcTable(sql, this.getDataConn());
			}
			if (dt.getIndexes() == null || dt.getIndexes().getIndexes().size() == 0) {
				dt.getColumns().setKeys(keys);
				dt.rebuildIndex();
			}
			_RefTables.add(sql1, dt);
			return dt;

			// String exp = "ItemValues.loadRefTable :: [" +
			// getSqlUniqueExp(sql)
			// + "]";
			// for (int i = 0; i < keys.length; i++) {
			// exp = exp + "::KEY=" + keys[i];
			// }
			// // EWA_R 强制刷新数据
			// if (this.getRequestValue().getString("EWA_R") == null) {
			// CachedValue c = CachedValueManager.getValue(exp);
			// if (c != null) {
			// return (DTTable) c.getValue();
			// }
			// }
			// boolean rst = this.getDataConn().executeQuery(sql);
			// if (rst) {
			// dt = new DTTable();
			// MList lst = this.getDataConn().getResultSetList();
			// ResultSet rs = this.getDataConn().getLastResult()
			// .getResultSet();
			// dt.initData(rs, keys);
			// try {
			// rs.close();
			// } catch (Exception err) {
			//
			// }
			// lst.removeValue(rs);
			// if (dt.isOk()) {
			// CachedValueManager.addValue(exp, dt);
			// dt.getAttsTable().add("TYPE", "SQL");
			// return dt;
			// }
			// }
		}
		return null;
	}

	public String getValue(UserXItem userXItem) throws Exception {
		this._LastValue = null;

		String name = userXItem.getName();
		String dataFieldName = name;
		String dataType = "string";
		String format = "";
		double numberScale = 1;
		if (userXItem.testName("DataItem")) {
			UserXItemValues us = userXItem.getItem("DataItem");
			if (us.count() > 0) {
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
							numberScale = Double.parseDouble(scale);
						} catch (Exception err) {

						}
					}
				}
			}
		}

		String val = this.getCacheValue(name, dataFieldName);
		if (val != null)
			return val;

		// 是否是处理为日期
		boolean isDate = format != null
				&& (format.toUpperCase().indexOf("DATE") >= 0 || format.toUpperCase().indexOf("TIME") >= 0);

		// 用户和系统的时区差值
		int timeDiffMinutes = _HtmlClass.getSysParas().getTimeDiffMinutes();

		// 来源数据库的数据
		Object o = this.getTableValue(dataFieldName, dataType);
		if (o != null) {
			if (isDate && timeDiffMinutes != 0) {
				Object ot = Utils.getTimeDiffValue(o, timeDiffMinutes);
				this._LastValue = ot;
			} else {
				if (numberScale > 1) {
					o = calcNumberScale(o, numberScale);
				}

				this._LastValue = o;
			}

			return HtmlUtils.formatValue(format, this._LastValue, this._HtmlClass.getSysParas().getLang());
		}

		// 来源类的数据
		val = this.getClassValue(name, dataFieldName);
		if (val != null) {
			if (isDate && timeDiffMinutes != 0) {
				Object ot = Utils.getTimeDiffValue(val, timeDiffMinutes);
				this._LastValue = ot;
			} else {
				if (numberScale > 1) {
					o = calcNumberScale(val, numberScale);
					this._LastValue = o;
				} else {
					this._LastValue = val;
				}
			}
			return HtmlUtils.formatValue(format, this._LastValue, this._HtmlClass.getSysParas().getLang());
		}

		// 来源RequestValue的数据
		val = this.getRvValue(name, dataFieldName);
		if (val != null) {
			if (isDate && timeDiffMinutes != 0) {
				Object ot = Utils.getTimeDiffValue(val, timeDiffMinutes);
				this._LastValue = ot;
			} else {
				if (numberScale > 1) {
					o = calcNumberScale(val, numberScale);
					this._LastValue = o;
				} else {
					this._LastValue = val;
				}
			}
			return HtmlUtils.formatValue(format, this._LastValue, this._HtmlClass.getSysParas().getLang());
		}
		return null;
	}

	/**
	 * 计算数字除以比例后的数值
	 * 
	 * @param ori
	 * @param numberScale 百/千/万/十万/百万/千万
	 * @return
	 */
	public Object calcNumberScale(Object ori, double numberScale) {
		if (ori == null || numberScale == 1) {
			return ori;
		}
		try {
			double d1 = Double.parseDouble(ori.toString()) / numberScale;
			return d1;
		} catch (Exception err) {
			return ori;
		}
	}

	public Object getTableValue(UserXItem userXItem) throws Exception {
		if (!userXItem.testName("DataItem")) {
			return null;
		}
		UserXItemValues us = userXItem.getItem("DataItem");
		if (us.count() == 0) {
			return null;
		}
		UserXItemValue u = us.getItem(0);

		String name = userXItem.getName();

		String dataFieldName = name;
		if (u.testName("DataField")) {
			if (u.getItem("DataField").trim().length() > 0) {
				dataFieldName = u.getItem("DataField");
			}
		}
		String dataType = "string";
		if (u.testName("DataType")) {
			dataType = u.getItem("DataType");
		}
		return this.getTableValue(dataFieldName, dataType);
	}

	/**
	 * 根据后缀获取值, 例如
	 * 
	 * @name:SS表示从session中取name
	 * 
	 * @param name 参数名
	 * @return
	 */
	public String getValueByTag(String name) {
		String n = name.toUpperCase().trim();
		String[] ns = n.split(":");
		MList lst = new MList();
		if (ns.length != 2) {
			return "参数表达错误";
		}
		for (int i = 1; i < ns.length; i += 1) {
			String tag = ns[i];
			if (tag.equals("SS")) { // session
				lst.add(PageValueTag.SESSION);
			} else if (tag.equals("CE")) {// COOKIE_ENCYRPT
				lst.add(PageValueTag.COOKIE_ENCYRPT);
			} else if (tag.equals("CC")) { // cookie
				lst.add(PageValueTag.COOKIE);
			} else if (tag.equals("FF")) {// session COOKIE_ENCYRPT
				lst.add(PageValueTag.FORM);
			} else if (ns[1].equals("QQ")) {// session COOKIE_ENCYRPT COOKIE
				lst.add(PageValueTag.QUERY_STRING);
			} else if (ns[1].equals("TT")) {
				lst.add(PageValueTag.DTTABLE);
			}
		}
		return this.getValueByTag(ns[0], lst);
	}

	private String getValueByTag(String name, MList tags) {
		PageValue ov = null;
		PageValues pvs = this.getRequestValue().getPageValues();
		for (int i = 0; i < tags.size(); i++) {
			PageValueTag tag = (PageValueTag) tags.get(i);
			ov = pvs.getPageValue(name, tag);
			if (ov != null) {
				break;
			}
		}
		if (ov == null) {
			return null;
		} else {
			return (ov.getValue() == null) ? null : ov.getValue().toString();
		}

	}

	public String getValue(String name, String dataFieldName) throws Exception {
		String val = this.getCacheValue(name, dataFieldName);
		if (val != null)
			return val;
		if (name.indexOf(":") > 0) { // 根据名称后缀获取不同类型的值
			return this.getValueByTag(name);
		}

		if (this._DTTables != null && this._DTTables.size() > 0) {
			Object o = this.getTableValue(dataFieldName, "String");
			if (o != null) {
				if (o.getClass().isArray()) {
					String cp = this.getSysParas().getRequestValue().getContextPath();
					return ItemImage.getImage(cp, (byte[]) o);
				}
				return o.toString();
			}
		}
		val = this.getClassValue(name, dataFieldName);
		if (val != null)
			return val;

		val = this.getRvValue(name, dataFieldName);
		if (val != null)
			return val;

		return null;

	}

	/**
	 * 替换内容的@参数，如果参数值为null，则不替换
	 * 
	 * @param s1     原始字符
	 * @param isHtml 是否保留html格式
	 * @return 替换后的字符
	 * @throws Exception
	 */
	public String replaceParameters(String s1, boolean isHtml) {
		return replaceParameters(s1, isHtml, false);
	}

	/**
	 * 替换内容的@参数，如果参数值为null，则替换成“”
	 * 
	 * @param s1         原始字符
	 * @param isHtml     是否保留html格式
	 * @param allReplace 是否全替换
	 * @return 替换后的字符
	 * @throws Exception
	 */
	public String replaceParameters(String s1, boolean isHtml, boolean allReplace) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val;
			try {
				val = this.getValue(name, name);
			} catch (Exception e) {
				val = e.getLocalizedMessage();
			}
			if (!isHtml) {
				val = Utils.textToInputValue(val).replace("\n", "<br />");
			}
			if (val == null) {
				if (allReplace) {
					val = "";
				} else {
					continue;
				}
			}
			String find = "@" + name;
			// 替换一次
			sb.replace(find, val);
		}
		return sb.toString();
	}

	/**
	 * 替换逻辑表达式中参数，如果参数不存在，则替换为""
	 * 
	 * @param s1
	 * @return
	 */
	public String replaceLogicParameters(String s1) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val = null;
			try {
				val = this.getValue(name, name);
			} catch (Exception e) {
				val = e.getLocalizedMessage();
			}
			if (val == null) {
				val = "";
			}
			String find = "@" + name;
			sb.replace(find, val);
		}
		return sb.toString();
	}

	/**
	 * 替换脚本中参数，如果参数不存在，则保留原始参数
	 * 
	 * @param s1
	 * @return
	 */
	public String replaceJsParameters(String s1) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val = null;
			try {
				val = this.getValue(name, name);
			} catch (Exception e) {
				val = e.getLocalizedMessage();
			}
			val = Utils.textToJscript(val);
			if (val == null) {
				continue;
			}
			String find = "@" + name;
			sb.replace(find, val);
		}
		return sb.toString();
	}

	/**
	 * 替换参数
	 * 
	 * @param s1
	 * @param isHtml
	 * @param allReplace
	 * @param isMoneyFormat 是否转换数字为货币表示
	 * @return
	 */
	public String replaceParameters(String s1, boolean isHtml, boolean allReplace, boolean isMoneyFormat) {
		if (s1 == null)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val;
			try {
				val = this.getValue(name, name);
			} catch (Exception e) {
				val = e.getLocalizedMessage();
			}
			if (!isHtml) {
				val = Utils.textToInputValue(val).replace("\n", "<br />");
			}
			if (val == null) {
				if (allReplace) {
					val = "";
				} else {
					continue;
				}
			}
			String find = "@" + name;

			if (isMoneyFormat) {
				val = UFormat.formatMoney(val);
			}
			sb.replace(find, val);
		}
		return sb.toString();
	}

	public String getCacheValue(String name, String dataFieldName) {
		// ListFrame和Report的Repeat为多记录RecordSet，因此不能使用Cache;
		if (this._HtmlClass.getSysParas() == null) {
			return null;
		}
		String frameType = this._HtmlClass.getSysParas().getFrameType();
		if (frameType == null) {
			return null;
		}
		if (frameType != null && (frameType.equalsIgnoreCase("ListFrame") || frameType.equalsIgnoreCase("Report")
				|| frameType.equalsIgnoreCase("Menu") || frameType.equalsIgnoreCase("Tree")))
			return null;

		String val = this.getParameterValue(dataFieldName);
		if (val != null) {
			return val;
		} else {
			return this.getParameterValue(name);
		}
	}

	/**
	 * Get the field value from the all tables
	 * 
	 * @param dataFieldName the field name
	 * @param dataType      the return data type
	 * @return the value
	 * @throws Exception
	 */
	public Object getTableValue(String dataFieldName, String dataType) throws Exception {
		Object val = null;
		if (this._ListFrameTable != null) { // ListFrame
			DTRow row = this._ListFrameTable.getCurRow();
			if (row == null) {
				return null;
			}
			if (row.getTable().getColumns().testName(dataFieldName)) {
				val = this.getCellValue(row, dataFieldName, dataType);
				return val;
			}
		}
		RequestValue rv = this._HtmlClass.getHtmlCreator().getRequestValue();
		String binType = rv.s("EWA_BIN_TYPE");
		// 从所有Table中获取数据
		for (int i = 0; i < this._DTTables.size(); i++) {
			DTTable tb = (DTTable) this._DTTables.get(i);
			if (!tb.isOk() || tb.getCount() == 0) {
				continue;
			}
			DTRow row;
			if (tb.getCurRow() == null) {
				row = tb.getRow(0);
			} else {
				row = tb.getCurRow();
			}

			if (!row.getTable().getColumns().testName(dataFieldName)) {
				// 不包含字段名
				continue;
			}

			val = this.getCellValue(row, dataFieldName, dataType);
			if (dataType.equalsIgnoreCase("binary")) { // 二进制转换成二进制文件
				String sVal = null;
				byte[] buf;
				if (val != null) {
					buf = (byte[]) val;
					if ("base64".equals(binType)) { // base64
						sVal = UConvert.ToBase64String(buf);
					} else if ("16".equals(binType)) { // 16进制
						sVal = Utils.bytes2hex(buf);
					} else {
						String path0 = UPath.getPATH_IMG_CACHE_URL() + "/binary/";
						String path = UPath.getPATH_IMG_CACHE() + "/binary/";
						String ext = null;

						// 先从数据库中检查是否有文件扩展名字段（文件+"_EXT"）,例如：F_CNT对应 F_CNT_EXT，大小写无关
						if (row.getTable().getColumns().testName(dataFieldName + "_EXT")) {
							ext = row.getCell(dataFieldName + "_EXT").toString();
						}
						if (ext == null) {
							ext = UFile.getExtFromFileBytes(buf);
						}
						String name = UFile.createMd5File(buf, ext, path, false);
						sVal = path0 + name;
					}
				}
				this.addParameterValue(dataFieldName, sVal);
				return sVal;
			} else {
				this.addParameterValue(dataFieldName, val == null ? null : val.toString());
			}
			break;

		}
		return val;
	}

	private Object getCellValue(DTRow row, String dataFieldName, String dataType) throws Exception {
		DTCell cell = row.getCell(dataFieldName);
		if (cell.getColumn().getTypeName() != null && cell.getColumn().getTypeName().toUpperCase() == "CLOB") {
			return cell.toString();
		}
		return cell == null ? null : cell.getValue();
	}

	public String getClassValue(String name, String dataFieldName) {
		if (this._UObjectValue == null) {
			return null;
		}
		String val = this._UObjectValue.getValue(dataFieldName);
		if (val == null) {
			val = this._UObjectValue.getValue(name);
		}
		if (val != null) {
			this.addParameterValue(dataFieldName, val);
			this.addParameterValue(name, val);
		}
		return val;
	}

	public String getRvValue(String name, String dataFieldName) {
		if (this._HtmlClass.getSysParas() == null || this._HtmlClass.getSysParas().getRequestValue() == null) {
			return null;
		}
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
		if (rv == null) {
			return null;
		}
		String val = rv.getString(dataFieldName);
		if (val == null) {
			val = rv.getString(name);
		}
		if (val != null) {
			this.addParameterValue(name, val);
			this.addParameterValue(dataFieldName, val);
		}
		return val;
	}

	private String getParameterValue(String name) {
		if (name == null) {
			return null;
		}
		String n1 = name.toUpperCase().trim();
		if (this._ParameterValues.containsKey(n1)) {
			Object v = this._ParameterValues.get(n1);
			return v == null ? null : v.toString();
		} else {
			return null;
		}
	}

	private void addParameterValue(String name, String val) {
		if (val == null || name == null) {
			return;
		}
		this._ParameterValues.put(name.toUpperCase().trim(), val);
	}

	/**
	 * @return the _RequestValue
	 */
	public RequestValue getRequestValue() {
		return this._HtmlClass.getSysParas().getRequestValue();
	}

	/**
	 * @return the _UserClass
	 */
	public Object getUserClass() {
		return _UserClass;
	}

	/**
	 * @param userClass the _UserClass to set
	 */
	public void setUserClass(Object userClass) {
		_UserClass = userClass;
	}

	/**
	 * @return the _DataConn
	 */
	public DataConnection getDataConn() {
		return this._HtmlClass.getSysParas().getDataConn();
	}

	/**
	 * @return the _UObjectValue
	 */
	public UObjectValue getUObjectValue() {
		return _UObjectValue;
	}

	/**
	 * @param objectValue the _UObjectValue to set
	 */
	public void setUObjectValue(UObjectValue objectValue) {
		_UObjectValue = objectValue;
	}

	/**
	 * @return the _DTTables
	 */
	public MList getDTTables() {
		return _DTTables;
	}

	/**
	 * @param tables the _DTTables to set
	 */
	public void setDTTables(MList tables) {
		_DTTables = tables;
	}

	/**
	 * @return the _ListFrameTable
	 */
	public DTTable getListFrameTable() {
		return _ListFrameTable;
	}

	/**
	 * @param listFrameTable the _ListFrameTable to set
	 */
	public void setListFrameTable(DTTable listFrameTable) {
		_ListFrameTable = listFrameTable;
	}

	/**
	 * @return the _SysParas
	 */
	public SysParameters getSysParas() {
		return this._HtmlClass.getSysParas();
	}

	/**
	 * @return the _HtmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	/**
	 * @param htmlClass the _HtmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		_HtmlClass = htmlClass;
	}

	public MList getJSONObjects() {
		if (_JSONObjects == null) {
			_JSONObjects = new MList();
		}
		return _JSONObjects;
	}
}
