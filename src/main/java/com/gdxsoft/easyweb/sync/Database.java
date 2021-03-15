package com.gdxsoft.easyweb.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.group.ImportTables;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class Database {
	private static String FIELDS_STR = "TABLE_NAME,COLUMN_NAME"
			+ ",IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION"
			+ ",NUMERIC_PRECISION_RADIX,NUMERIC_SCALE";
	private static String[] FIELDS = FIELDS_STR.split(",");

	private static String ROUTINE_STR = "ROUTINE_NAME,ROUTINE_TYPE,ROUTINE_BODY,ROUTINE_DEFINITION,CREATED,LAST_ALTERED";
	private static String[] ROUTINES = ROUTINE_STR.split(",");
	private String databaseName_;
	private JSONObject tables_;
	private HashMap<String, Boolean> identitiesMap_;

	public static String getCode() {
		String xml = UPath.getRealPath() + "/ewa_conf.xml";
		try {
			Document doc = UXml.retDocument(xml);
			NodeList nl = doc.getElementsByTagName("remote_database_syncs");

			if (nl.getLength() > 0) {
				Element ele = (Element) nl.item(0);
				String code = ele.getAttribute("code");
				return code;
			}
			return "remote_database_syncs not defined";
		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	public static String getRemoteUrl() {
		String xml = UPath.getRealPath() + "/ewa_conf.xml";
		try {
			Document doc = UXml.retDocument(xml);
			NodeList nl = doc.getElementsByTagName("remote_database_syncs");

			if (nl.getLength() > 0) {
				Element ele = (Element) nl.item(0);
				String u = ele.getAttribute("url");
				String code = ele.getAttribute("code");
				return u + "?code=" + URLEncoder.encode(code, "utf-8");
			}
			return "remote_database_syncs not defined";
		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	/**
	 * 获取所有表数据（包含字段）
	 * @return the tables
	 */
	public JSONObject getTables() {
		return tables_;
	}

	public Database(String databaseName) {
		databaseName_ = databaseName;
	}

	public static String getDatabases() {
		String sql = "SELECT name FROM master..SYSDATABASES WHERE NOT name IN"
				+ " ('master','tempdb','model','msdb','ReportServer','ReportServerTempDB') order by name";
		DTTable tb = DTTable.getJdbcTable(sql);

		return tb.toJson(null);
	}

	public static String getRemoteDatabases() {
		String remote_url = getRemoteUrl() + "&method=databases";
		UNet net = new UNet();
		net.setIsShowLog(false);

		net.setEncode("utf-8");
		HashMap<String, String> map = new HashMap<String, String>();
		String rst = net.doPost(remote_url, map);
		return rst;
	}

	public static String sendSqlToRemote(String remotedatabase, String json) {
		String remote_url = getRemoteUrl() + "&method=execute&dbname="
				+ remotedatabase;

		UNet net = new UNet();
		net.setIsShowLog(false);

		net.setEncode("utf-8");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("json", json);
		String rst = net.doPost(remote_url, map);
		System.out.println(rst);
		return rst;
	}

	public static String executeJson(String database, String json) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");
		try {
			JSONObject jsonObj = new JSONObject(json);
			// String diff = jsonObj.getString("diff");
			String run = jsonObj.getString("run");
			JSONObject jsonRst = new JSONObject();

			if (run.equals("xml")) {
				String xml = "<Tables>" + jsonObj.getString("sql")
						+ "</Tables>";
				Document doc = UXml.asDocument(xml);
				ImportTables ex = new ImportTables(doc, null, cnn);
				ex.setTargetDatabase(database);

				String rst = ex.importTables();
				jsonRst.put("RST", true);
				jsonRst.put("MSG", rst);
			} else if (run.equals("sql")) {
				String sql = jsonObj.getString("sql");
				if (sql.indexOf("{DB}") > 0) {
					sql = sql.replace("{DB}", database);
					// 分段执行
					String[] sqls = sql.split("\\-\\-FG\\-\\-");
					for (int i = 0; i < sqls.length; i++) {
						String sqla = sqls[i];
						if (sqla.trim().length() > 0) {
							cnn.executeUpdateNoParameter(sqla);
						}
					}
				} else {
					// 需要更换数据库，更换完成后要更换回去，避免数据库连接错误
					String sql0 = "select top 1 CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA";
					DTTable tb = DTTable.getJdbcTable(sql0);
					if (tb.isOk() && tb.getCount() > 0) {
						String thisDatabase = tb.getCell(0, 0).toString();
						cnn.executeUpdateNoParameter("use " + database);
						// 分段执行
						String[] sqls = sql.split("\\-\\-FG\\-\\-");

						for (int i = 0; i < sqls.length; i++) {
							String sqla = sqls[i];
							if (sqla.trim().length() > 0) {
								cnn.executeUpdateNoParameter(sqla);
							}
						}

						cnn.executeUpdateNoParameter("use " + thisDatabase);
					}
				}

				if (cnn.getErrorMsg() == null) {
					jsonRst.put("RST", true);
					jsonRst.put("MSG", "OK");
				} else {
					jsonRst.put("RST", false);
					jsonRst.put("ERR", cnn.getErrorMsg());
				}
			}

			return jsonRst.toString();
		} catch (JSONException e) {
			return "{\"RST\":false,ERR:\""
					+ Utils.textToJscript(e.getMessage()) + "\"}";
		} catch (Exception e) {
			return "{\"RST\":false,ERR:\""
					+ Utils.textToJscript(e.getMessage()) + "\"}";
		} finally {
			cnn.close();
		}
	}

	public boolean init() {
		try {
			this.queryTables();
			this.queryFields();
			this.setHashFields();
			this.queryProcedure();
			this.queryFunction();
			this.queryDefault();
			this.queryTrigger();

			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public JSONObject compareToRemote(String remotedbname) throws JSONException {

		String sqlIdentitys = "select object_name(id) 表名,name Identity列名 from "
				+ this.databaseName_
				+ "..syscolumns "
				+ "	where COLUMNPROPERTY(ID,NAME,'IsIdentity')=1  "
				+ "AND OBJECTPROPERTY(id, N'IsUserTable')= 1 ORDER BY object_name(id)";

		DTTable tbIdentitys = DTTable.getJdbcTable(sqlIdentitys);

		this.identitiesMap_ = new HashMap<String, Boolean>();
		for (int i = 0; i < tbIdentitys.getCount(); i++) {
			String key = tbIdentitys.getCell(i, 0).toString().toUpperCase()
					+ "&" + tbIdentitys.getCell(i, 1).toString().toUpperCase();
			identitiesMap_.put(key, true);
		}

		String remote_url = getRemoteUrl() + "&method=init&dbname="
				+ remotedbname;
		UNet net = new UNet();
		net.setIsShowLog(false);

		net.setEncode("utf-8");
		HashMap<String, String> map = new HashMap<String, String>();
		String rst = net.doPost(remote_url, map);
		JSONObject remote_json = new JSONObject(rst);

		Iterator<?> keys = this.tables_.keys();
		JSONObject rstJson = new JSONObject();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			JSONObject o1 = new JSONObject();
			JSONObject remote_table = null;
			JSONObject this_table = this.tables_.getJSONObject(key);
			if (remote_json.has(key)) {
				remote_table = remote_json.getJSONObject(key);
			}
			o1 = this.compareToRemoteSingle(key, this_table, remote_table);
			if (o1 != null) {
				rstJson.put(key, o1);
			}
		}

		return rstJson;
	}

	private JSONObject compareToRemoteSingle(String key, JSONObject this_table,
			JSONObject remote_table) throws JSONException {
		String type = this_table.getString("TABLE_TYPE");
		JSONObject o1 = new JSONObject();
		o1.put("name", key);
		o1.put("type", type);

		if (key.equals("ORD_BOOK_CUSTOMER")) {
			int aa = 1;
			aa++;
			System.out.println(aa);
		}
		if (remote_table != null
				&& this_table.has("_fields_hash")
				&& this_table.getInt("_hash") == remote_table.getInt("_hash")
				&& this_table.getInt("_fields_hash") == remote_table
						.getInt("_fields_hash")) {
			return null;
		}
		if (remote_table != null) {
			if (type.equals("BASE TABLE")) {
				if (this_table.getInt("_hash") != remote_table.getInt("_hash")) {
					o1.put("diff", "type");
					// 表类型不同，例如视图变成表
				} else {
					o1.put("diff", "field");
					try {
						String sql = this.createAddFields(this_table,
								remote_table);
						o1.put("sql", sql);
						o1.put("run", "sql");

						String des = createFieldsDiffDes(this_table,
								remote_table);
						o1.put("des", des);

					} catch (Exception e) {
						System.out.println(e.getMessage());
						System.out.println(this_table);

					}
				}
			} else if (type.equals("VIEW")) {
				String sql = this.createOrUpdateView(this_table);
				o1.put("diff", "CHANGED");
				o1.put("sql", sql);
				o1.put("run", "sql");
				String des = createViewFieldsDiffDes(this_table, remote_table);
				o1.put("des", des);
			} else if (type.equals("PROCEDURE") || type.equals("FUNCTION")) {
				o1.put("diff", "CHANGED");
				String ROUTINE_DEFINITION = this_table
						.getString("ROUTINE_DEFINITION");
				String ROUTINE_DEFINITION1 = remote_table
						.getString("ROUTINE_DEFINITION");

				if (ROUTINE_DEFINITION.equals(ROUTINE_DEFINITION1)) {// 内容定义一致
					return null;
				}

				String LAST_ALTERED = this_table.getString("LAST_ALTERED");
				String LAST_ALTERED1 = remote_table.getString("LAST_ALTERED");
				Date dt = Utils.getDate(LAST_ALTERED);
				Date dt1 = Utils.getDate(LAST_ALTERED1);

				if (dt1.getTime() == dt.getTime()) {
					return null; // 时间一致
				}

				if (dt1.getTime() > dt.getTime()) {
					return null;// 目标存储过程时间》本地修改时间
				}

				String sql = this.createOrUpdateProcedure(this_table);

				o1.put("sql", sql);
				o1.put("run", "sql");
			} else if (type.equals("DEFAULT")) {
				if (this_table.getInt("_hash") != remote_table.getInt("_hash")) {
					o1.put("diff", "CHANGED");
					String sql = this.createUpdateDefaultSql(this_table,
							remote_table);
					o1.put("sql", sql);
					o1.put("run", "sql");
				} else {
					return null;
				}
			} else if (type.equals("TRIGGER")) {
				o1.put("diff", "CHANGED");
				String ROUTINE_DEFINITION = this_table
						.getString("ROUTINE_DEFINITION");
				String ROUTINE_DEFINITION1 = remote_table
						.getString("ROUTINE_DEFINITION");
				String disabled = this_table.getString("DISABLE");
				String disabled1 = remote_table.getString("DISABLE");
				if(!this_table.has("PARENT_TABLE")){
					return null;
				}
				String parent_table = this_table.getString("PARENT_TABLE");
				if (key.equals("ACC_RECORD_INSERT")) {
					System.out.println(this_table);
					System.out.println(remote_table);
				}
				if (ROUTINE_DEFINITION.equals(ROUTINE_DEFINITION1)) {// 内容定义一致
					if (disabled.equals("1") && disabled1.equals("0")) {
						// 是否禁用
						String sql = "alter table [" + parent_table
								+ "] disable trigger [" + key + "]";
						o1.put("sql", sql);
						o1.put("run", "sql");
						return o1;
					}
					return null;
				}

				String LAST_ALTERED = this_table.getString("LAST_ALTERED");
				String LAST_ALTERED1 = remote_table.getString("LAST_ALTERED");
				Date dt = Utils.getDate(LAST_ALTERED);
				Date dt1 = Utils.getDate(LAST_ALTERED1);

				if (dt1.getTime() == dt.getTime()) {
					if (disabled.equals("1") && disabled1.equals("0")) {
						// 是否禁用
						String sql = "alter table [" + parent_table
								+ "] disable trigger [" + key + "]";
						o1.put("sql", sql);
						o1.put("run", "sql");
						return o1;
					}
					return null; // 时间一致
				}

				if (dt1.getTime() > dt.getTime()) {
					if (disabled.equals("1") && disabled1.equals("0")) {
						// 是否禁用
						String sql = "alter table [" + parent_table
								+ "] disable trigger [" + key + "]";
						o1.put("sql", sql);
						o1.put("run", "sql");
						return o1;
					}
					return null;// 目标存储过程时间》本地修改时间
				}

				String sql = this.createOrUpdateProcedure(this_table);
				if (disabled.equals("1") && disabled1.equals("0")) {
					// 是否禁用
					String sql1 = "alter table [" + parent_table
							+ "] disable trigger [" + key + "]";
					sql = sql + "\n--FG--\n" + sql1;
					return o1;
				}
				o1.put("sql", sql);
				o1.put("run", "sql");
			}
		} else {
			o1.put("diff", "new");
			if (type.equals("BASE TABLE")) {
				try {
					String xml = this.createNewTable(this_table);
					o1.put("sql", xml);
					o1.put("run", "xml");

				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println(this_table);

				}
			} else if (type.equals("VIEW")) {
				try {
					String sql = this.createOrUpdateView(this_table);
					o1.put("sql", sql);
					o1.put("run", "sql");

				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println(this_table);

				}
			} else if (type.equals("PROCEDURE") || type.equals("FUNCTION")
					|| type.equals("TRIGGER")) {
				String sql = this.createOrUpdateProcedure(this_table);

				o1.put("sql", sql);
				o1.put("run", "sql");
			} else if (type.equals("DEFAULT")) {
				String sql = this.createUpdateDefaultSql(this_table, null);
				o1.put("sql", sql);
				o1.put("run", "sql");
			} else if (type.equals("TRIGGER")) {
				String sql = this.createOrUpdateProcedure(this_table);
				String disabled = this_table.getString("DISABLE");
				String parent_table = this_table.getString("PARENT_TABLE");
				if (disabled.equals("1")) {
					// 是否禁用
					String sql1 = "alter table [" + parent_table
							+ "] disable trigger [" + key + "]";
					sql = sql + "\n--FG--\n" + sql1;
					return o1;
				}
				o1.put("sql", sql);
				o1.put("run", "sql");
			}
		}

		return o1;
	}

	/**
	 * 创建更新默认值
	 * 
	 * @param r
	 * @return
	 * @throws JSONException
	 */
	private String createUpdateDefaultSql(JSONObject from, JSONObject to)
			throws JSONException {

		String tbName = from.getString("TB_NAME").toString();

		String sql = "";
		if (to != null) {
			String drop_constraint = to.getString("A_DN").toString();
			sql += "ALTER TABLE [" + tbName + "] drop constraint  ["
					+ drop_constraint + "]; ";
		}
		String def = from.getString("A_DD").toString();
		String field = from.getString("A_FN").toString();
		def = def.substring(1, def.length() - 1);
		String constraint = from.getString("A_DN").toString();
		sql += "ALTER TABLE [" + tbName + "] ADD CONSTRAINT [" + constraint
				+ "] default " + def + " for [" + field + "];";
		return sql;

	}

	private String createOrUpdateProcedure(JSONObject fromTable)
			throws JSONException {
		String tbName = fromTable.getString("TABLE_NAME");
		MStr sqlViews = new MStr();
		sqlViews.a("SELECT A.name, B.text FROM [" + this.databaseName_
				+ "].SYS.syscomments B " + "INNER JOIN [" + this.databaseName_
				+ "].SYS.sysobjects A ON A.id=B.id " + "WHERE A.name IN ( ");
		sqlViews.a("'" + tbName.replace("'", "''") + "'");
		sqlViews.a(") order by b.id,b.colid ");

		DTTable tb = DTTable.getJdbcTable(sqlViews.toString());

		MStr s = new MStr();
		String sqlDrop = this.createDropSql(tbName,
				fromTable.getString("TABLE_TYPE"));
		s.al(sqlDrop);
		s.al("\n--FG--\n");
		// 因为返回是多行，因此需要拼接起来
		for (int i = 0; i < tb.getCount(); i++) {
			String sql = tb.getCell(i, 1).toString();
			s.a(sql);
		}
		return s.toString();
	}

	private String createOrUpdateView(JSONObject fromTable)
			throws JSONException {
		String tbName = fromTable.getString("TABLE_NAME");
		MStr sqlViews = new MStr();
		sqlViews.a("SELECT A.name, B.text FROM [" + this.databaseName_
				+ "].SYS.syscomments B " + "INNER JOIN [" + this.databaseName_
				+ "].SYS.sysobjects A ON A.id=B.id " + "WHERE A.name IN ( ");
		sqlViews.a("'" + tbName.replace("'", "''") + "'");
		sqlViews.a(") order by b.id,b.colid ");

		DTTable tb = DTTable.getJdbcTable(sqlViews.toString());

		MStr s = new MStr();
		String sqlDrop = this.createDropSql(tbName, "VIEW");
		s.al(sqlDrop);
		s.al("\n--FG--\n");
		// 因为返回是多行，因此需要拼接起来
		for (int i = 0; i < tb.getCount(); i++) {
			String sql = tb.getCell(i, 1).toString();
			s.a(sql);
		}
		return s.toString();
	}

	private String createDropSql(String name, String type) {
		String sql = "IF EXISTS(SELECT * FROM SYSOBJECTS WHERE NAME='"
				+ name.replace("'", "''") + "')\r\n DROP " + type + " [" + name
				+ "]";
		return sql;
	}

	private String createNewTable(JSONObject fromTable)
			throws ParserConfigurationException, SAXException, IOException,
			JSONException {
		DataConnection conn;
		conn = new DataConnection();
		conn.setConfigName("");
		String tbName = fromTable.getString("TABLE_NAME");
		Table tb = new Table(tbName, conn);
		tb.init(this.databaseName_);
		return tb.toXml();
	}

	/**
	 * 生成新增或修改字段的 sql
	 * 
	 * @param fromTable
	 * @param toTable
	 * @return
	 * @throws JSONException
	 */
	private String createAddFields(JSONObject fromTable, JSONObject toTable)
			throws JSONException {

		JSONObject fromFields = fromTable.getJSONObject("_FIELDS");
		JSONObject toFields = toTable.getJSONObject("_FIELDS");
		String sql0 = "alter table {DB}..[" + toTable.getString("TABLE_NAME")
				+ "] ";
		Iterator<?> keys = fromFields.keys();
		MStr s = new MStr();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			JSONObject fromField = fromFields.getJSONObject(key);

			String identity_key = toTable.getString("TABLE_NAME").toUpperCase()
					+ "&" + key.toUpperCase();
			boolean isIdentity = this.identitiesMap_.containsKey(identity_key);

			String tp;
			try {
				tp = this.createFieldType(fromField);

			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(fromField);
				return null;
			}

			if (toFields.has(key)) { // 修改字段
				JSONObject toField = toFields.getJSONObject(key);

				if (toField.getInt("_hash") != fromField.getInt("_hash")) {
					String s1 = sql0 + " alter column ["
							+ fromField.getString("COLUMN_NAME") + "]";
					s1 += tp;
					if (isIdentity) {
						s1 += " identity(1,1) ";
					}
					s.al(s1 + ";\n--FG--\n");
				}
			} else { // 添加字段
				String s1 = sql0 + " add ["
						+ fromField.getString("COLUMN_NAME") + "]";
				s1 += tp;
				if (isIdentity) {
					s1 += " identity(1,1) ";
				}
				s.al(s1 + ";\n--FG--\n");
			}
		}

		return s.toString();
	}

	private String createFieldsDiffDes(JSONObject fromTable, JSONObject toTable)
			throws JSONException {

		JSONObject fromFields = fromTable.getJSONObject("_FIELDS");
		JSONObject toFields = toTable.getJSONObject("_FIELDS");
		Iterator<?> keys = fromFields.keys();
		MStr s = new MStr();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			JSONObject fromField = fromFields.getJSONObject(key);

			if (toFields.has(key)) { // 修改字段
				JSONObject toField = toFields.getJSONObject(key);

				if (toField.getInt("_hash") != fromField.getInt("_hash")) {
					String s1 = key + createFieldType(fromField) + "|"
							+ createFieldType(toField);
					s.al(s1);
				}
			} else { // 添加字段
				String s1 = key + createFieldType(fromField) + "| 无";
				s.al(s1);
			}
		}

		return s.toString();
	}

	private String createViewFieldsDiffDes(JSONObject fromTable,
			JSONObject toTable) throws JSONException {

		JSONObject fromFields = fromTable.getJSONObject("_FIELDS");
		JSONObject toFields = toTable.getJSONObject("_FIELDS");
		Iterator<?> keys = fromFields.keys();
		MStr s = new MStr();
		while (keys.hasNext()) {
			String key = keys.next().toString();

			if (!toFields.has(key)) { // 修改字段

				String s1 = key + "| 无";
				s.al(s1);
			}
		}
		  keys = toFields.keys();
		while (keys.hasNext()) {
			String key = keys.next().toString();

			if (!fromFields.has(key)) { // 修改字段

				String s1 = " 无 |"+ key ;
				s.al(s1);
			}
		}
		return s.toString();
	}

	private String createFieldType(JSONObject fromField) throws JSONException {
		String DATA_TYPE = fromField.getString("DATA_TYPE");
		String s1 = " " + DATA_TYPE;
		if (DATA_TYPE.indexOf("INT") >= 0 || DATA_TYPE.equals("MONEY")
				|| DATA_TYPE.equals("IMAGE") || DATA_TYPE.indexOf("TIME") >= 0
				|| DATA_TYPE.indexOf("BIT") >= 0
				|| DATA_TYPE.indexOf("DATE") >= 0
				|| DATA_TYPE.indexOf("TEXT") >= 0
				|| DATA_TYPE.indexOf("LOB") > 0
				|| DATA_TYPE.indexOf("FLOAT") >= 0) {

		} else if (DATA_TYPE.indexOf("NUM") >= 0 || DATA_TYPE.indexOf("DECIMAL") >= 0) {
			String NUMERIC_PRECISION = fromField.getString("NUMERIC_PRECISION");
			String NUMERIC_SCALE = fromField.getString("NUMERIC_SCALE");

			s1 += "(" + NUMERIC_PRECISION + "," + NUMERIC_SCALE + ")";
		} else {
			String CHARACTER_MAXIMUM_LENGTH = fromField
					.getString("CHARACTER_MAXIMUM_LENGTH");
			if (CHARACTER_MAXIMUM_LENGTH.equals("-1")) {
				CHARACTER_MAXIMUM_LENGTH = "max";
			}
			s1 += "(" + CHARACTER_MAXIMUM_LENGTH + ")";
		}
		String IS_NULLABLE = fromField.getString("IS_NULLABLE");
		if (IS_NULLABLE.equals("NO")) {
			s1 += " not null";
		} else {
			s1 += "   null";
		}

		return s1;
	}

	private void queryTables() throws Exception {
		tables_ = new JSONObject();
		String sql = "select * from "
				+ databaseName_
				+ ".INFORMATION_SCHEMA.TABLES where not TABLE_NAME in ('SYSDIAGRAMS')";
		DTTable tb = DTTable.getJdbcTable(sql);

		for (int i = 0; i < tb.getCount(); i++) {
			JSONObject obj = this.getTableJson(tb.getRow(i));
			tables_.put(obj.getString("TABLE_NAME"), obj);
		}
	}

	private JSONObject getTableJson(DTRow r) throws Exception {
		JSONObject obj = new JSONObject();
		String name = r.getCell("TABLE_NAME").toString().toUpperCase();
		String type = r.getCell("TABLE_TYPE").toString().toUpperCase();

		obj.put("TABLE_NAME", name);
		obj.put("TABLE_TYPE", type);
		obj.put("_FIELDS", new JSONObject());
		this.setHash(obj);
		return obj;

	}

	private String queryProcedure() throws Exception {
		String sql = "SELECT * FROM "
				+ databaseName_
				+ ".INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE='PROCEDURE' and not ROUTINE_NAME like 'sp%'";
		DTTable tb = DTTable.getJdbcTable(sql);
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			JSONObject obj = this.getProcedureJson(r);
			String tbName = obj.getString("ROUTINE_NAME");
			obj.put("TABLE_NAME", tbName);
			obj.put("TABLE_TYPE", "PROCEDURE");

			this.tables_.put(tbName, obj);

		}

		return null;
	}

	/**
	 * 字段的Json表达式
	 * 
	 * @param r
	 * @return
	 * @throws Exception
	 */
	private JSONObject getProcedureJson(DTRow r) throws Exception {
		JSONObject obj = new JSONObject();

		for (int i = 0; i < 2; i++) {
			String f = ROUTINES[i].trim();
			String v = r.getCell(f).toString();
			if (v != null) {
				obj.put(f, v.toUpperCase());
			}
		}

		this.setHash(obj);

		for (int i = 2; i < ROUTINES.length; i++) {
			String f = ROUTINES[i].trim();
			String v = r.getCell(f).toString();
			if (v != null && f.equals("ROUTINE_DEFINITION")) {
				v = v.hashCode() + "";
			}
			if (v != null) {
				obj.put(f, v.toUpperCase());
			}
		}
		return obj;

	}

	private String queryDefault() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("	select UPPER(b.name)+'/'+UPPER(c.name) TABLE_NAME, UPPER(b.name) TB_NAME "
				+ ",'DEFAULT' TABLE_TYPE, UPPER(c.name) A_FN, a.name A_DN,a.definition A_DD \n");
		sb.append("	from [OneWorld_BATE].sys.default_constraints a\n");
		sb.append("		inner join [OneWorld_BATE]..sysobjects b on a.parent_object_id=b.id\n");
		sb.append("		inner join [OneWorld_BATE].sys.columns c on b.id=c.object_id and a.parent_column_id=c.column_id\n");

		String sql = sb.toString().replace("OneWorld_BATE", this.databaseName_);
		DTTable tb = DTTable.getJdbcTable(sql);
		JSONArray arr = tb.toJSONArray();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			String tbName = obj.getString("TABLE_NAME");
			String adn = obj.getString("A_DN");
			obj.remove("A_DN");
			this.setHash(obj);
			obj.put("A_DN", adn);

			this.tables_.put(tbName, obj);
		}
		return null;
	}

	private String queryTrigger() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("	SELECT a.name TABLE_NAME,'TRIGGER' TABLE_TYPE,b.modify_date,convert(int,b.is_disabled) is_disabled ,c.text,"
				+ " OBJECT_NAME(b.parent_id) PARENT_TABLE  FROM  [{DB}].sys.SYSOBJECTS A \n");
		sb.append("	 inner join [{DB}].sys.triggers b on a.id=b.object_id\n");
		sb.append("	 inner join [{DB}].SYS.syscomments c on a.id=c.id\n");
		sb.append("			 WHERE A.XTYPE='TR'  \n");
		sb.append("	order by a.name,c.colid\n");
		String sql = sb.toString().replace("{DB}", this.databaseName_);

		DTTable tb = DTTable.getJdbcTable(sql);
		String lastName = "";
		String lastText = "";
		JSONObject obj = null;
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			String tbName = r.getCell("TABLE_NAME").toString();
			if (!tbName.equals(lastName)) {
				lastName = tbName;
				obj = new JSONObject();
				obj.put("TABLE_NAME", tbName);
				obj.put("TABLE_TYPE", "TRIGGER");
				// 是否禁用
				obj.put("DISABLE", r.getCell("is_disabled").toString());
				obj.put("LAST_ALTERED", r.getCell("modify_date").toString());
				// 触发器所属表
				obj.put("PARENT_TABLE", r.getCell("PARENT_TABLE").toString());
				this.tables_.put(tbName, obj);
				lastText = "";
			}

			lastText = lastText + r.getCell("text").toString();
			obj.put("ROUTINE_DEFINITION", lastText.hashCode() + "");
			this.setHash(obj);
		}
		return null;
	}

	private String queryFunction() throws Exception {
		String sql = "SELECT * FROM "
				+ databaseName_
				+ ".INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_TYPE='FUNCTION' and not ROUTINE_NAME like 'sp%'";
		DTTable tb = DTTable.getJdbcTable(sql);
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			JSONObject obj = this.getProcedureJson(r);
			String tbName = obj.getString("ROUTINE_NAME");
			obj.put("TABLE_NAME", tbName);
			obj.put("TABLE_TYPE", "FUNCTION");

			this.tables_.put(tbName, obj);

		}

		return null;
	}

	/**
	 * 检索所有字段
	 * 
	 * @throws Exception
	 */
	private void queryFields() throws Exception {
		String sql = "select * from " + databaseName_
				+ ".INFORMATION_SCHEMA.COLUMNS";
		DTTable tb = DTTable.getJdbcTable(sql);
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			String tbName = r.getCell("TABLE_NAME").toString().toUpperCase();
			String colName = r.getCell("COLUMN_NAME").toString().toUpperCase();
			if (!this.tables_.has(tbName)) {
				continue;
			}
			JSONObject table = this.tables_.getJSONObject(tbName);
			JSONObject obj = null;
			if (table.getString("TABLE_TYPE").equals("VIEW")) {
				// 视图只比较字段名称，不比较类型，因为字段类型依赖于表
				obj = new JSONObject();
				obj.put("TABLE_NAME", tbName);
				obj.put("COLUMN_NAME", colName);
				this.setHash(obj);

			} else {
				obj = this.getFieldJson(r);
			}

			table.getJSONObject("_FIELDS").put(colName, obj);
		}
	}

	/**
	 * 字段的Json表达式
	 * 
	 * @param r
	 * @return
	 * @throws Exception
	 */
	private JSONObject getFieldJson(DTRow r) throws Exception {
		JSONObject obj = new JSONObject();

		for (int i = 0; i < FIELDS.length; i++) {
			String f = FIELDS[i].trim();
			String v = r.getCell(f).toString();
			if (v != null) {
				obj.put(f, v.toUpperCase());
			}
		}

		this.setHash(obj);
		return obj;

	}

	private void setHashFields() throws JSONException {
		Iterator<?> keys = this.tables_.keys();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			JSONObject table = this.tables_.getJSONObject(key);
			JSONObject fields = table.getJSONObject("_FIELDS");

			Iterator<?> keys1 = fields.keys();
			ArrayList<String> al = new ArrayList<String>();
			while (keys1.hasNext()) {
				Object key1 = keys1.next();
				al.add(key1.toString());
			}
			String[] s1 = new String[al.size()];
			al.toArray(s1);
			Arrays.sort(s1);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s1.length; i++) {
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(s1[i]);
				sb.append("=");
				sb.append(fields.get(s1[i]).toString());
			}
			int hash = sb.toString().hashCode();
			table.put("_fields_hash", hash);
		}
	}

	private void setHash(JSONObject obj) throws Exception {
		Iterator<?> keys = obj.keys();
		ArrayList<String> al = new ArrayList<String>();
		while (keys.hasNext()) {
			Object key = keys.next();
			al.add(key.toString());
		}
		String[] s1 = new String[al.size()];
		al.toArray(s1);
		Arrays.sort(s1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s1.length; i++) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(s1[i]);
			sb.append("=");
			sb.append(obj.get(s1[i]));
		}
		int hash = sb.toString().hashCode();
		obj.put("_hash", hash);
	}
}
