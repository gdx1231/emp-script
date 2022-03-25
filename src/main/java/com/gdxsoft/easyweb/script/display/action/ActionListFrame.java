package com.gdxsoft.easyweb.script.display.action;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.data.export.TxtExport;
import com.gdxsoft.easyweb.data.export.DbfExport;
import com.gdxsoft.easyweb.data.export.ExcelExport;
import com.gdxsoft.easyweb.data.export.IExport;
import com.gdxsoft.easyweb.data.export.XmlExport;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.datasource.SearchParameter;
import com.gdxsoft.easyweb.datasource.SearchParameterInit;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ActionListFrame extends ActionBase implements IAction {
	private static Logger LOG = LoggerFactory.getLogger(ActionListFrame.class);

	public void executeCallClass(String name) throws Exception {

		UserXItemValues sqlset = super.getUserConfig().getUserActionItem().getItem("ClassSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String className = sqlItem.getItem("ClassName");
		String conData = sqlItem.getItem("ConData").trim();
		String methodName = sqlItem.getItem("MethodName");
		String methodData = sqlItem.getItem("MethodData").trim();
		String xmlTag = sqlItem.getItem("XmlTag").trim();
		Object[] objConData = executeCallClassCreateObjects(conData);
		Object[] objMethodData = executeCallClassCreateObjects(methodData);

		super.getDebugFrames().addDebug(this, "ACT", "开始调用Class (" + className + ", " + methodName + ")");

		UObjectValue ov = new UObjectValue();
		boolean isok = ov.loadClass(className, objConData, methodName, objMethodData, super.getRequestValue());
		if (!isok) {
			super.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回错误");
			return;
		}
		Object instance = ov.getInstance();
		if (instance == null) {// 实例没生成？应该不会
			return;
		}
		super.getDebugFrames().addDebug(this, "ACT", "生成" + instance);
		if (instance instanceof IClassDao) {
			// ClassDaoBase的实例
			super.getDebugFrames().addDebug(this, "ACT", "调用" + instance);
			this.executeClassOfIClassDao((IClassDao<?>) instance, name);
			return;
		}
		Object oo = ov.getObject();
		if (oo == null) {
			super.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回空值");
			return;
		}
		super.createClassData(oo, xmlTag);
	}

	/**
	 * 调用接口为IClassDao的类
	 * 
	 * @param o    类
	 * @param name
	 * @throws Exception
	 */
	private void executeClassOfIClassDao(IClassDao<?> o, String name) throws Exception {
		String ajax = super.getItemValues().getSysParas().getAjaxCallType();
		RequestValue rv = super.getRequestValue();

		o.getConn().setDebugFrames(super.getDebugFrames());
		o.getConn().setRequestValue(rv);
		String sql = o.getSqlSelect();

		if (ajax != null && ajax.trim().equalsIgnoreCase("DOWN_DATA")) {
			// 下载数据
			String dataName = this.createDownloadData(sql);
			if (dataName != null) {
				String key = "DOWN_DATA_" + rv.getString("EWA.ID");
				rv.addValue(key, dataName);
			}
			return;
		}

		int iPageSize = this.getUserSettingPageSize();
		PageSplit ps = new PageSplit(0, rv, iPageSize);
		String keyField = this.getPageItemValue("PageSize", "KeyField");

		sql = this.createSqListFrame(sql, o.getConn());

		try {
			DTTable tb = o.executeQuery(sql, keyField, ps.getPageSize(), ps.getPageCurrent());
			tb.setName(name);
			if (tb.isOk()) {
				super.getDTTables().add(tb);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			o.getConn().close();
		}
	}

	/**
	 * 执行SQL，分页只在第一个query中执行，其余的SQL完全执行
	 * 
	 * @param name 执行的sqlCall name
	 */
	public void executeCallSql(String name) throws Exception {
		UserXItemValues sqlset = super.getUserConfig().getUserActionItem().getItem("SqlSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String sqlExp = sqlItem.getItem("Sql");
		String[] sqlArray = sqlExp.split(";");

		String transType = sqlItem.getItem("TransType");
		boolean isTrans = transType.equalsIgnoreCase("yes") ? true : false;
		DataConnection conn = super.getItemValues().getSysParas().getDataConn();
		if (isTrans) {
			conn.transBegin();
		}

		RequestValue rv = super.getRequestValue();
		int runInc = 0; // 执行次数
		boolean isSplitSql = false;
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) {
				// 空语句
				continue;
			}

			String sqlType = sqlItem.getItem("SqlType");
			if (runInc > 0 && DataConnection.checkIsSelect(sql) && !sqlType.equals("query")) {
				// 执行过程中有其它Select过程
				super.executeSqlQuery(sql);
				DTTable dt = (DTTable) super.getDTTables().getLast();
				if (dt.getCount() == 1) {
					super.addDTTableToRequestValue(dt);
				}

			} else if (sqlType.equals("query")) {// 查询
				if (sql.toLowerCase().indexOf("ewa_err_out") > 0 || isSplitSql) {
					super.executeSqlQuery(sql);
				} else {
					// 执行分页
					isSplitSql = true; // 分页只执行1次
					if (sql.toUpperCase().indexOf("WHERE") < 0) {
						throw new Exception("查询语句中应包含WHERE条件, (" + sql + ")");
					}
					String sql1 = this.createSqListFrame(sql, conn);
					String ajax = super.getRequestValue().getString("EWA_AJAX");
					if (ajax == null) {
						ajax = "";
					} else {
						ajax = ajax.trim();
					}
					if (ajax.equalsIgnoreCase("DOWN_DATA")) {
						// 下载数据
						String dataName = this.createDownloadData(sql1);
						if (dataName != null) {
							String key = "DOWN_DATA_" + rv.getString("EWA.ID");
							rv.addValue(key, dataName);
						}
					} else if (ajax.equalsIgnoreCase("JSON") || ajax.equalsIgnoreCase("JSON_ALL")) {
						if (rv.getString("EWA_PAGESIZE") != null) { // 分页
							int iPageSize = this.getUserSettingPageSize();
							PageSplit ps = new PageSplit(0, rv, iPageSize);
							String keyField = this.getPageItemValue("PageSize", "KeyField");
							conn.executeQueryPage(sql1, keyField, ps.getPageCurrent(), ps.getPageSize());
						} else {
							conn.executeQuery(sql1); // all
						}
						DTTable tb = new DTTable(); // 映射到自定义数据表
						tb.initData(conn.getLastResult().getResultSet());
						tb.setName(name);
						conn.getLastResult().getResultSet().close();
						if (tb.isOk()) {
							super.getDTTables().add(tb);
							// 加载Hor数据
							super.executeExtOpt(sql, tb);
						}
					} else {
						int iPageSize = this.getUserSettingPageSize();
						PageSplit ps = new PageSplit(0, rv, iPageSize);
						String keyField = this.getPageItemValue("PageSize", "KeyField");
						conn.executeQueryPage(sql1, keyField, ps.getPageCurrent(), ps.getPageSize());

						DTTable tb = new DTTable(); // 映射到自定义数据表
						tb.initData(conn.getLastResult().getResultSet());
						tb.setName(name);
						conn.getLastResult().getResultSet().close();
						if (tb.isOk()) {
							super.getDTTables().add(tb);
							// 加载Hor数据
							super.executeExtOpt(sql, tb);
							super.checkActionErrorOutInTable(tb);
						}
					}
				}
			} else if (sqlType.equals("procedure")) {// 存储过程
				super.executeSqlProcdure(sql);
			} else {// 更新
				if (DataConnection.checkIsSelect(sql)) {
					super.executeSqlQuery(sql);
					DTTable dt = (DTTable) super.getDTTables().getLast();
					if (dt.getCount() == 1) {
						super.addDTTableToRequestValue(dt);
					}
				} else {
					super.executeSqlUpdate(sql);
				}
			}
			if (conn.getErrorMsg() != null && conn.getErrorMsg().length() > 0) {
				if (isTrans) {
					conn.transRollback();
				}
				conn.close();
				throw new Exception(conn.getErrorMsg());
			}
			runInc++;
		}
		if (isTrans) {
			conn.transCommit();
		}
		this.executeSessionsCookies(sqlItem);
	}

	/**
	 * 下载数据
	 * 
	 * @param sql sql语句
	 * @return 文件相对地址
	 * @throws Exception
	 */
	private String createDownloadData(String sql) throws Exception {
		RequestValue rv = super.getRequestValue();
		String lang = rv.getLang();
		String downType = rv.s("EWA_AJAX_DOWN_TYPE");
		// 允许导出的模式
		String allowExport = super.getPageItemValue("PageSize", "AllowExport");
		if (allowExport == null || allowExport.trim().length() == 0) {
			return null;
		}
		allowExport = allowExport.toUpperCase().trim();
		downType = downType == null ? "XLS" : downType.toUpperCase();
		if (allowExport.indexOf(downType) < 0) {
			return null;
		}
		IExport exp = null;
		boolean isExcel = false;
		if (downType.equals("DBF")) {
			exp = new DbfExport();
		} else if (downType.equals("XML")) {
			exp = new XmlExport();
		} else if (downType.equals("TXT")) {
			exp = new TxtExport(lang);
		} else { // excel
			exp = new ExcelExport(lang);
			isExcel = true;
		}
		DataConnection cnn = super.getItemValues().getSysParas().getDataConn();
		cnn.executeQuery(sql);
		String msg = cnn.getErrorMsg();
		if (msg != null && msg.length() > 0) {
			throw new Exception(msg);
		}

		ResultSet rs = cnn.getLastResult().getResultSet();
		String upload = UPath.getPATH_UPLOAD();
		File fUpload = new File(upload);
		upload = fUpload.getAbsolutePath();
		File ff;
		String exportPathAndName = upload + "/download_datas/" + rv.s("EWA.ID");
		try {
			if (isExcel && !rv.s("EWA_AJAX_DOWN_TYPE").equals("XLS_OLD")) {
				// 下载 字段显示成为配置的信息，去掉未显示的内容
				DTTable tbExport = this.createDownloadTable(rs);
				ff = exp.export(tbExport, exportPathAndName);
			} else {
				ff = exp.export(rs, exportPathAndName);
			}
			String name = ff.getAbsolutePath().replace(upload, UPath.getPATH_UPLOAD_URL());
			return name.replace("\\", "/");
		} catch (Exception e) {
			throw e;
		} finally {
			rs.close();
		}

	}

	/**
	 * 创建下载用的表，字段显示成为配置的信息，去掉未显示的内容
	 * 
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	private DTTable createDownloadTable(ResultSet rs) throws Exception {
		DTTable tb = new DTTable();
		tb.initData(rs);
		UserConfig uc = super.getUserConfig();
		Map<String, UserXItem> map = new HashMap<String, UserXItem>();
		Map<String, Integer> mapOrder = new HashMap<String, Integer>();
		int order = 0;
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);

			// 检查是否为隐含字段，在逻辑控制页面生成
			if (super.getHtmlClass().getSysParas().isHiddenColumn(uxi.getName())) {
				continue;
			}
			String name = uxi.getName().toUpperCase();
			mapOrder.put(name, order);
			map.put(name, uxi);
			order++;

		}
		String lang = super.getHtmlClass().getSysParas().getLang();
		int total_export_fields = 0;
		for (int i = 0; i < tb.getColumns().getCount(); i++) {
			DTColumn col = tb.getColumns().getColumn(i);
			if (col.getName() != null && map.containsKey(col.getName().toUpperCase())) {
				String name = col.getName().toUpperCase();
				UserXItem uxi = map.get(name);
				String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
				// 设置描述
				col.setDescription(des);
				// 设置排序
				col.setOrder(mapOrder.get(name));
				total_export_fields++;
			} else {
				// 去掉未显示的内容
				col.setHidden(true);
			}
		}

		// 如果导出的字段数量小于 2 个，则恢复所有字段，避免因自定义导出语句和配置定义完全不一致
		if (total_export_fields < 2) {
			for (int i = 0; i < tb.getColumns().getCount(); i++) {
				DTColumn col = tb.getColumns().getColumn(i);
				col.setHidden(false);
			}
		}

		return tb;
	}

	/**
	 * 合成用户排序表达式
	 * 
	 * @return
	 * @throws Exception
	 */
	private String createSqlOrder() throws Exception {
		DataConnection conn = super.getItemValues().getSysParas().getDataConn();
		boolean isMySqlOrder = false;
		if (conn != null && conn.getCurrentConfig().getType().equalsIgnoreCase("MYSQL")) {
			isMySqlOrder = true;
		}
		RequestValue rv = super.getItemValues().getRequestValue();
		UserConfig uc = super.getUserConfig();

		String userOrder = rv.getString("EWA_LF_ORDER");
		if (userOrder == null || userOrder.trim().length() == 0) {
			return "";

		}
		String keyField = uc.getUserPageItem().getSingleValue("PageSize", "KeyField");

		String name = userOrder.split(" ")[0].trim();
		if (!uc.getUserXItems().testName(name)) {
			return "";
		}

		UserXItem uxi = uc.getUserXItems().getItem(name);
		if (!(uxi.testName("DataItem") && uxi.getItem("DataItem").count() > 0)) {
			return "";
		}

		// 排序字段
		String s1 = uxi.getItem("DataItem").getItem(0).getItem("DataField");
		String dt = uxi.getItem("DataItem").getItem(0).getItem("DataType");

		String orderField = s1.trim().toUpperCase();

		// 用户自定义排序表达式
		if (uxi.getItem("OrderSearch").getItem(0).testName("OrderExp")) {
			String orderExp = uxi.getItem("OrderSearch").getItem(0).getItem("OrderExp");
			if (orderExp.trim().length() > 0) {
				s1 = orderExp;
			}
		}

		if (isMySqlOrder) { // mysql utf8 的中文排序
			if (!(dt.equalsIgnoreCase("bigint") || dt.equalsIgnoreCase("int") || dt.equalsIgnoreCase("number")
					|| dt.equalsIgnoreCase("date") || dt.equalsIgnoreCase("binary"))) {
				if (s1.indexOf("(") == -1) {
					s1 = "CONVERT( " + s1 + " USING gbk )";
				}
			}
		}
		if (userOrder.indexOf(" ") > 0) {
			s1 += " DESC";
		}

		userOrder = s1;
		if (keyField != null && keyField.trim().length() > 0) {
			String[] s2 = keyField.trim().toUpperCase().split(",");
			for (int i = 0; i < s2.length; i++) {
				String f = s2[i].trim().split(" ")[0];
				if (orderField.equals(f)) { // 已经在表达式上面了
					continue;
				}
				if (isMySqlOrder && !uc.getUserXItems().testName(f)) {
					uxi = uc.getUserXItems().getItem(name);
					dt = uxi.getItem("DataItem").getItem(0).getItem("DataType");
					if (!(dt.equalsIgnoreCase("bigint") || dt.equalsIgnoreCase("int") || dt.equalsIgnoreCase("number")
							|| dt.equalsIgnoreCase("date") || dt.equalsIgnoreCase("binary"))) {
						String desc = "";
						if (f.indexOf(" ") > 0) {
							String[] fs = f.split(" ");
							f = fs[0];
							desc = " " + fs[1];
						}
						f = "CONVERT( " + f + " USING gbk )" + desc;
					}
				}
				userOrder += "," + f;
			}
		}
		// System.out.println(userOrder);
		return userOrder;
	}

	/**
	 * ewa_search=bas_tag[eq]acc,bas_tag_grp[lk]src
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private String createSqlSearchInit(DataConnection conn) throws Exception {
		RequestValue rv = super.getItemValues().getRequestValue();

		String ewa_search = rv.getString("ewa_search");
		if (ewa_search == null || ewa_search.trim().length() == 0) {
			return "";
		}

		String databaseType = conn.getDatabaseType();
		if (databaseType == null) {
			databaseType = "";
		} else {
			databaseType = databaseType.trim().toUpperCase();
		}

		UserConfig uc = super.getUserConfig();
		MStr sb = new MStr();
		sb.append("  (1=1 ");
		String[] para = ewa_search.split(",");
		for (int i = 0; i < para.length; i++) {
			SearchParameterInit lsp = new SearchParameterInit(para[i]);
			if (!lsp.isValid()) { // 是否合法
				continue;
			}
			// 字段在配置里不存在，丢弃
			if (!uc.getUserXItems().testName(lsp.getName())) {
				LOG.warn("Search field " + lsp.getName() + " not defined");
				continue;
			}
			UserXItem uxi = uc.getUserXItems().getItem(lsp.getName());
			if (!uxi.testName("DataItem")) {
				LOG.warn("Search field " + lsp.getName() + " no DataItem");
				continue;
			}
			UserXItemValues us = uxi.getItem("DataItem");
			if (us.count() == 0) {
				LOG.warn("Search field " + lsp.getName() + " DataItem no data");
				continue;
			}
			UserXItemValue u = us.getItem(0);

			String dataType = u.getItem("DataType").trim().toUpperCase();
			String dataField = u.getItem("DataField");

			// 用户自定义排序表达式
			if (uxi.getItem("OrderSearch").getItem(0).testName("SearchExp")) {
				String searchExp = uxi.getItem("OrderSearch").getItem(0).getItem("SearchExp");
				if (searchExp.trim().length() > 0) {
					dataField = searchExp;
				}
			}

			if (lsp.getTag().equals("or")) {
				// EWA_SEARCH=MEMO_STATE[or]MEMO_ING;MEMO_FINISH
				String[] pps = lsp.getPara1().split("\\;");
				sb.al(" AND (");
				for (int ia = 0; ia < pps.length; ia++) {
					if (ia > 0) {
						sb.al(" OR ");
					}
					sb.a(dataField);
					sb.a("='");
					sb.a(pps[ia].trim().replace("'", "''"));
					sb.a("' ");
				}
				sb.a(")");
			} else if (lsp.getTag().equals("eq") || lsp.getTag().equals("gt") || lsp.getTag().equals("lt")
					|| lsp.getTag().equals("gte") || lsp.getTag().equals("lte") || lsp.getTag().equals("uneq")) {
				String op = "=";// eq;
				if (lsp.getTag().equals("gt")) {
					op = ">";
				} else if (lsp.getTag().equals("lt")) {
					op = "<";
				} else if (lsp.getTag().equals("lte")) {
					op = "<=";
				} else if (lsp.getTag().equals("gte")) {
					op = ">=";
				} else if (lsp.getTag().equals("uneq")) {
					op = "!=";
				}
				String pp = lsp.getPara1();
				if (dataType.indexOf("DATE") >= 0 || dataType.indexOf("TIME") >= 0) {
					try {
						String d1 = conn.getDateTimePara(pp);
						sb.append(" AND (");
						sb.append(dataField);
						sb.append(op);

						sb.append(d1);
						sb.al(")");
					} catch (Exception err) {
						LOG.error(lsp.toString());
						LOG.error(err.getLocalizedMessage());
					}
				} else if (dataType.indexOf("NUM") == 0 || dataType.indexOf("INT") >= 0) {
					try {
						UConvert.ToDouble(pp);
						sb.append(" AND (");
						sb.append(dataField);
						sb.append(op);
						sb.append(pp);
						sb.al(")");
					} catch (Exception err) {
						sb.append(" ");
						LOG.error(lsp.toString());
						LOG.error(err.getLocalizedMessage());
					}

				} else {
					sb.append(" AND (");
					if (databaseType.equals("HSQLDB")) {
						sb.a(" UPPER(" + dataField + ")");
						pp = pp.toUpperCase();
					} else {
						sb.a(dataField);
					}
					sb.a(op);
					sb.a("'");
					sb.a(pp.replace("'", "''"));
					sb.al("')");
				}

			} else {
				sb.append(" AND (");
				String exp = lsp.getPara1().replace("'", "''");
				if (databaseType.equals("HSQLDB")) {
					sb.al(" UPPER(" + dataField + ")");
					exp = exp.toUpperCase();
				} else {
					sb.al(dataField);
				}
				if (lsp.getTag().equalsIgnoreCase("llk")) { // 左 like
					sb.al(" like '" + exp + "%')");
				} else if (lsp.getTag().equalsIgnoreCase("rlk")) { // 右 like
					sb.al(" like '%" + exp + "')");
				} else { // lk
					sb.al(" like '%" + exp.replace("'", "''") + "%')");
				}
			}
		}
		return sb.toString() + ")";

	}

	/**
	 * 合成用户检索表达式
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private String createSqlSearch(DataConnection conn) throws Exception {
		RequestValue rv = super.getItemValues().getRequestValue();

		String userSearch = rv.getString("EWA_LF_SEARCH");
		if (userSearch == null || userSearch.trim().length() == 0) {
			return "";
		}

		UserConfig uc = super.getUserConfig();
		StringBuilder sb = new StringBuilder();
		sb.append("  (1=1 ");
		String[] para = userSearch.split("@!@");
		for (int i = 1; i < para.length; i++) {
			SearchParameter lsp = new SearchParameter(para[i]);
			if (!lsp.isValid()) {
				continue;
			}

			HashMap<String, String> fieldMap = new HashMap<String, String>();
			String[] searchNames = lsp.getName().split(",");
			for (int mm = 0; mm < searchNames.length; mm++) {
				String searchName = searchNames[mm];
				UserXItem uxi = uc.getUserXItems().getItem(searchName);
				if (!uxi.testName("DataItem")) {
					continue;
				}
				UserXItemValues us = uxi.getItem("DataItem");
				if (us.count() == 0)
					continue;
				UserXItemValue u = us.getItem(0);

				String dataType = u.getItem("DataType").trim().toUpperCase();
				String dataField = u.getItem("DataField");
				if (fieldMap.containsKey(dataField)) {
					continue;
				}
				// 用户自定义排序表达式
				if (uxi.getItem("OrderSearch").getItem(0).testName("SearchExp")) {
					String searchExp = uxi.getItem("OrderSearch").getItem(0).getItem("SearchExp");
					if (searchExp.trim().length() > 0) {
						dataField = searchExp;
					}
				}

				fieldMap.put(dataField, dataType);
			}

			if (fieldMap.size() == 0) {
				continue;
			}

			int inc = 0;
			StringBuilder sbExp = new StringBuilder();
			for (String dataField : fieldMap.keySet()) {
				String dataType = fieldMap.get(dataField); // 字段类型
				String exp = this.createSearchSql(lsp, conn, dataField, dataType);

				// System.out.println(exp);

				if (exp == null) {
					continue;
				}
				if (inc == 0) {
					sbExp.append(" ");
				} else {
					sbExp.append(" OR ");
				}
				sbExp.append("(");
				sbExp.append(exp);
				sbExp.append(")");

				inc++;
			}
			if (inc > 0) {
				sb.append(" AND (");
				sb.append(sbExp);

				sb.append(" )");

			}
		}
		userSearch = sb.toString() + ")";
		return userSearch;
	}

	/**
	 * 创建复合索引的单个查询
	 * 
	 * @param lsp
	 * @param conn
	 * @param dataField
	 * @param dataType
	 * @return
	 */
	private String createSearchSql(SearchParameter lsp, DataConnection conn, String dataField, String dataType) {
		String exp = null;
		if (lsp.isDouble()) { // 双字段
			exp = createSearchDoubleSql(lsp, conn, dataField, dataType);
		} else if (lsp.getTag().equals("fix")) {
			if (lsp.getParas()[0] != null && (lsp.getParas()[0].trim().length() > 0)) {
				exp = createSearchFixSql(lsp, conn, dataField, dataType);
			}
		} else {
			if (lsp.getPara1().trim().length() != 0) {
				exp = createSearchTextSql(lsp, conn, dataField, dataType);
			}
		}

		return exp;
	}

	/**
	 * 创建检索的文字查询
	 * 
	 * @param lsp
	 * @param conn
	 * @param dataField
	 * @param dataType
	 * @return
	 */
	private String createSearchTextSql(SearchParameter lsp, DataConnection conn, String dataField, String dataType) {
		String[] txtExps = { lsp.getPara1().trim() };
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(lsp.getPara1().trim());
		if (m.find()) {
			// 用空格分割查询字符串，拼接为 OR
			txtExps = lsp.getPara1().trim().split(" ");
		}
		MStr s = new MStr();
		String op = " like ";
		String op_left = "%";
		String op_right = "%";

		if (lsp.getTag().equals("llk")) {// 左like
			op_left = "";
		} else if (lsp.getTag().equals("rlk")) {// 右like
			op_right = "";
		} else if (lsp.getTag().equals("eq")) {// =
			op_right = "";
			op_left = "";
			op = " = ";
		}

		StringBuilder sb = new StringBuilder();

		int len = txtExps.length;

		String databaseType = conn.getDatabaseType();
		if (databaseType == null) {
			databaseType = "";
		} else {
			databaseType = databaseType.trim().toUpperCase();
		}

		for (int k = 0; k < len; k++) {
			String exp = txtExps[k].trim();
			if (exp.length() == 0) {
				continue;
			}
			if (s.length() > 0) {
				s.a(" OR ");
			}

			if (databaseType.equals("HSQLDB")) {
				s.a(" UPPER(" + dataField + ") " + op + " '" + op_left + exp.replace("'", "''").toUpperCase() + op_right
						+ "' ");
			} else {
				s.a(" " + dataField + op + " '" + op_left + exp.replace("'", "''") + op_right + "' ");
			}
		}
		if (s.length() == 0) {
			return null;
		}
		if (s.length() > 0) {
			if (len > 1)
				sb.append("(");
			sb.append(s.toString());
			if (len > 1)
				sb.append(") ");
		}

		return sb.toString();
	}

	/**
	 * 创建查询的固定查询
	 * 
	 * @param lsp
	 * @param conn
	 * @param dataField
	 * @param dataType
	 * @return
	 */
	private String createSearchFixSql(SearchParameter lsp, DataConnection conn, String dataField, String dataType) {
		UserConfig uc = super.getUserConfig();

		StringBuilder sb = new StringBuilder();
		sb.append(" (");
		int inc = 0;
		boolean useLike = false;
		try {
			UserXItem uxi = uc.getUserXItems().getItem(dataField);
			if (uxi.getItem("DataRef").count() > 0) {
				UserXItemValue vs = uxi.getItem("DataRef").getItem(0);
				String refSql = vs.getItem("RefSql");
				String refKey = vs.getItem("RefKey");
				String refShow = vs.getItem("RefShow");
				if (StringUtils.isNotBlank(refSql) && StringUtils.isNotBlank(refKey)
						&& StringUtils.isNotBlank(refShow)) {
					// 是否多个ID的匹配，例如：CAMP_OPT_TSG,CAMP_OPT_LQC,CAMP_OPT_ZQC
					useLike = "yes".equals(vs.checkItemExists("RefMulti") ? vs.getItem("RefMulti") : "");
				}
			}
		} catch (Exception e) {
		}
		for (int m = 0; m < lsp.getParas().length; m++) {
			String pp = lsp.getParas()[m].trim();
			if (pp.length() == 0 || pp.length() > 100) {
				continue;
			}

			if (inc > 0) {
				sb.append(" OR ");
			}
			pp = pp.replace("'", "''");
			if (conn.getDatabaseType().equalsIgnoreCase("MYSQL")) {
				pp = pp.replace("\\", "\\\\");
			}
			if (useLike) {
				sb.append(dataField + " like '%" + pp + "%'");
			} else {
				sb.append(dataField + " = '" + pp + "'");
			}
			inc++;
		}
		if (inc == 0) { // 没有表达式
			return null;
		}
		sb.append(")\r\n");
		return sb.toString();
	}

	/**
	 * 创建查询的区间查询
	 * 
	 * @param lsp
	 * @param conn
	 * @param dataField
	 * @param dataType
	 * @return
	 */
	private String createSearchDoubleSql(SearchParameter lsp, DataConnection conn, String dataField, String dataType) {
		StringBuilder sb = new StringBuilder();

		if (dataType.indexOf("DATE") >= 0 || dataType.indexOf("TIME") >= 0) {// 日期形式
			int timeDiff = this.getHtmlClass().getSysParas().getTimeDiffMinutes();
			boolean isHaveFirst = false;
			if (!lsp.getPara1().equals("")) {
				String d1;
				if (timeDiff != 0) { // 和时差进行计算后的日期
					Timestamp t = conn.getTimestamp(lsp.getPara1());

					// 过滤时间的时差和显示操作相反
					Timestamp t1 = (Timestamp) Utils.getTimeDiffValue(t, -timeDiff);
					d1 = conn.getDateTimePara(t1);
				} else {
					d1 = conn.getDateTimePara(lsp.getPara1());
				}
				sb.append(dataField + " >= " + d1);
				isHaveFirst = true;
			}
			if (!lsp.getPara2().equals("")) {
				// String dd2 = conn.getDateTimePara(lsp.getPara2());
				String dd2;
				if (timeDiff != 0) { // 和时差进行计算后的日期
					Timestamp t = conn.getTimestamp(lsp.getPara2());
					t = new Timestamp(t.getTime() + 24 * 60 * 60 * 1000 - 1); // 加23小时

					// 过滤时间的时差和显示操作相反
					Timestamp t1 = (Timestamp) Utils.getTimeDiffValue(t, -timeDiff);
					dd2 = conn.getDateTimePara(t1);
				} else {
					dd2 = conn.getDateTimePara(lsp.getPara2());
				}
				if (isHaveFirst) {
					sb.append(" AND ");
				}
				sb.append(dataField + " <= " + dd2);
			}
		} else if (dataType.indexOf("NUM") == 0 || dataType.indexOf("INT") >= 0) {// 数字形式
			boolean isHaveFirst = false;
			if (!lsp.getPara1().equals("")) {
				try {
					Double.parseDouble(lsp.getPara1());
					isHaveFirst = true;
				} catch (Exception err) {
					return null;
				}
			}
			sb.append(dataField + " >= " + lsp.getPara1());
			if (!lsp.getPara2().equals("")) {
				try {
					Double.parseDouble(lsp.getPara2());
				} catch (Exception err) {
					return null;
				}
				if (isHaveFirst) {
					sb.append(" AND ");
				}
				sb.append(dataField + " <= " + lsp.getPara2());
			}
		}

		return sb.toString();
	}

	/**
	 * 重新组合ListFrame的 SQL查询语句，用于排序和查询
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	private String createSqListFrame(String sql, DataConnection conn) throws Exception {
		// mysql中文排序
		boolean isMySqlOrder = false;
		if (conn != null && conn.getCurrentConfig().getType().equalsIgnoreCase("MYSQL")) {
			isMySqlOrder = true;
		}
		sql = super.getSql(sql);

		String userOrder = this.createSqlOrder();
		String userSearch = this.createSqlSearch(conn);

		if (userSearch.trim().length() == 0) {
			userSearch = this.createSqlSearchInit(conn);
		}
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		sql = sp.rebuildSql(userOrder, userSearch, isMySqlOrder);
		return sql;
	}

	/**
	 * 获取用户每页显示数量
	 * 
	 * @return
	 */
	private int getUserSettingPageSize() {
		String pageSize = this.getPageItemValue("PageSize", "PageSize");
		int iPageSize = 10;
		try {
			iPageSize = Integer.parseInt(pageSize);
		} catch (Exception e) {

		}
		return iPageSize;
	}
}
