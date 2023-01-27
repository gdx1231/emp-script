package com.gdxsoft.easyweb.define.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.BatchInsert;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.maps.MapFieldType;
import com.gdxsoft.easyweb.define.database.maps.Maps;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ImportTables {
	private static Logger LOGGER = LoggerFactory.getLogger(ImportTables.class);
	private Document _DocTable;
	private Document _DocData;

	private Table[] _Tables;
	HashMap<String, SqlTable> _SqlTables;
	HashMap<String, ArrayList<String>> _SqlDatas;

	private HashMap<String, List<Object>> _TablesInsertFix;

	private DataConnection _Conn;
	private String targetDatabase_;

	private String replaceMetaDatabaseName; // DDL replace meta database name, e.g. `main_data_db`
	private String replaceWorkDatabaseName; // DDL replace work database name, e.g. `work_db`

	private int batInsertCount = 100;

	// 模块说明信息，主要用到 metaDatabase meta库名称和 workDatabase工作库名称
	private ModuleDescription moduleDescription;

	// All SQLS and ERRORS
	private MStr sqls = new MStr();

	/**
	 * 所有的DDL、SQL和错误信息
	 * 
	 * @return
	 */
	public MStr getSqls() {
		return sqls;
	}

	/**
	 * 模块说明信息，主要用到<br>
	 * metaDatabase meta库名称<br>
	 * workDatabase工作库名称
	 * 
	 * @return
	 */
	public ModuleDescription getModuleDescription() {
		return moduleDescription;
	}

	/**
	 * 模块说明信息，主要用到<br>
	 * metaDatabase meta库名称<br>
	 * workDatabase工作库名称
	 * 
	 * @param moduleDescription
	 */
	public void setModuleDescription(ModuleDescription moduleDescription) {
		this.moduleDescription = moduleDescription;
	}

	public ImportTables(Document docTable, Document docData, DataConnection conn) {
		this._DocTable = docTable;
		this._DocData = docData;

		this._Conn = conn;
	}

	public void readTables() {
		NodeList nl = UXml.retNodeList(this._DocTable, "Tables/Table");
		this._Tables = new Table[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++) {
			Table table = this.initTable(nl.item(i));
			table.setImportTables(this);
			table.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
			table.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);
			this._Tables[i] = table;
		}
	}

	/**
	 * 替换元数据库和工作库的名称
	 * 
	 * @param sourceSql
	 * @return
	 */
	private String replaceMetaOrWorkDatabaseName(String sourceSql) {
		String s = sourceSql;
		if (this.replaceMetaDatabaseName != null) {
			s = s.replace(Table.REPLACE_META_DATABASE_NAME, this.replaceMetaDatabaseName);
		}
		if (this.replaceWorkDatabaseName != null) {
			s = s.replace(Table.REPLACE_WORK_DATABASE_NAME, this.replaceWorkDatabaseName);
		}

		return s;
	}

	private String replaceMysql8Collates(String sourceSql) {
		String s = sourceSql;
		int inc = 0;
		while (true) {
			String snew = this.replaceMysql8Collate(s);
			if (snew.equals(s)) {
				return snew;
			}
			s = snew;
			inc++;
			if (inc > 9000) {
				LOGGER.error("Too much matches, over {} {}", inc, sourceSql);
				break;
			}
		}

		return s;
	}

	/**
	 * CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER
	 * VIEW
	 * 
	 * @return
	 */
	private String replaceMysqlDefiner(String sourceSql) {
		// DEFINER=root@localhost
		String replaceStart = "DEFINER=";
		String s = this.replaceString(sourceSql, replaceStart);

		// ALGORITHM=UNDEFINED
		String replaceStart1 = "ALGORITHM=";
		s = this.replaceString(s, replaceStart1);

		String replaceStart2 = "SQL SECURITY DEFINER";
		s = this.replaceString(s, replaceStart2);
		return s;
	}

	private String replaceString(String sourceSql, String replaceStart) {
		String s = sourceSql;
		String su = s.toLowerCase();

		int loc0 = su.indexOf(replaceStart.toLowerCase());
		if (loc0 == -1) {
			return sourceSql;
		}
		int skip = replaceStart.length();

		if (!replaceStart.endsWith("=")) {// 和替换字符串一致
			int loc1 = loc0 + skip;
			String tag = s.substring(loc0, loc1 + 1);
			s = s.replace(tag, "/* " + tag + " */ ");
			return s;
		}

		// ALGORITHM=UNDEFINED
		// DEFINER=root@localhost
		int loc1 = -1;
		for (int i = loc0 + skip; i < su.length(); i++) {
			String c = su.substring(i, i + 1);
			if (c.equals(" ") || c.equals("\t") || c.equals("\n") || c.equals("\r")) {
				loc1 = i - 1;
				break;
			}
		}
		if (loc1 > loc0) {
			String tag = s.substring(loc0, loc1 + 1);
			s = s.replace(tag, "/* " + tag + " */ ");
		}

		return s;
	}

	private String replaceMysql8Collate(String sourceSql) {
//		CREATE TABLE `adm_racl_tag` (
//			  `RACL_TAG` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RACL_TAG',
//			  `RACL_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'RACL_NAME',
//			  PRIMARY KEY (`RACL_TAG`)
//		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
		String s = sourceSql;
		String su = s.toUpperCase();
		int loc0 = su.indexOf("COLLATE ");
		int skip = 8;
		boolean findMethod2 = false;
		if (loc0 == -1) {
			loc0 = su.indexOf("AUTO_INCREMENT=");
			skip = "AUTO_INCREMENT=".length();
			findMethod2 = true;
		}
		if (loc0 == -1) {
			loc0 = su.indexOf("COLLATE=");
			findMethod2 = true;
			skip = 8;
		}
		if (loc0 > 0) {
			int loc1 = -1;
			for (int i = loc0 + skip; i < su.length(); i++) {
				String c = su.substring(i, i + 1);
				if (c.equals(" ") || c.equals("\t") || c.equals("\n") || c.equals("\r")) {
					loc1 = i - 1;
					break;
				}
				if (findMethod2 && i == su.length() - 1) {
					loc1 = i; // 最后一个字符了
				}
			}
			if (loc1 > loc0) {
				String tag = s.substring(loc0, loc1 + 1);
				s = s.replace(tag, "/* DEL " + tag.substring(7) + " */ ");
			}
		}

		return s;
	}

	/**
	 * Create the tables
	 * 
	 * @param maps
	 * @return
	 */
	private String createDatabaseTables(HashMap<String, SqlTable> maps) {
		Iterator<String> it = maps.keySet().iterator();
		StringBuilder sb = new StringBuilder();
		boolean isTargetMysql = SqlUtils.isMySql(_Conn);
		while (it.hasNext()) {
			SqlTable t = maps.get(it.next());
			if ("VIEW".equalsIgnoreCase(t.getTable().getTableType())) {
				continue;
			}
			String s = t.getCreate();

			s = this.replaceMetaOrWorkDatabaseName(s);

			if (isTargetMysql) {
				// 删除mysql8带有的 COLLATE
				s = this.replaceMysql8Collates(s);
			}

			LOGGER.info("Create the table {}", t.getTable().getName());
			// 表结构
			LOGGER.debug("TABLE DDL -> {}", s);
			sqls.al("/* " + t.getTable().getName() + ", DDL:  */");
			sqls.al(s); // ddl
			this._Conn.executeUpdateNoParameter(s);
			if (this._Conn.getErrorMsg() != null) {
				sb.append(this._Conn.getErrorMsg() + "\n");
				sqls.al("/* ERROR:" + this._Conn.getErrorMsg() + " */");
				this._Conn.clearErrorMsg();
			}

			// 主键
			String pkSql = t.getPk();
			if (pkSql != null && pkSql.trim().length() > 0) {
				pkSql = this.replaceMetaOrWorkDatabaseName(pkSql);
				LOGGER.debug("TABLE PK -> {}", pkSql);
				sqls.al("/* " + t.getTable().getName() + ", PK:  */");
				sqls.al(pkSql);

				this._Conn.executeUpdateNoParameter(pkSql);
				if (this._Conn.getErrorMsg() != null) {
					sb.append(this._Conn.getErrorMsg() + "\n");
					sqls.al("/* ERROR:" + this._Conn.getErrorMsg() + " */");
					this._Conn.clearErrorMsg();
				}
			}

			// 字段备注
			ArrayList<String> s2 = t.getComments();
			if (s2.size() > 0) {
				sqls.al("/* " + t.getTable().getName() + ", COMMENT:  */");
				for (int i = 0; i < s2.size(); i++) {
					String sql = s2.get(i);
					sql = sql.replace("{SCHMEA}", this._Conn.getSchemaName());
					sql = this.replaceMetaOrWorkDatabaseName(sql);

					LOGGER.debug("TABLE COMMENT -> {}", sql);

					sqls.al(sql);
					this._Conn.executeUpdateNoParameter(sql);
					if (this._Conn.getErrorMsg() != null) {
						sb.append(this._Conn.getErrorMsg() + "\n");
						sqls.al("/* ERROR:" + this._Conn.getErrorMsg() + " */");
						this._Conn.clearErrorMsg();
					}
				}
			}
			// 索引
			ArrayList<String> s1 = t.getIndexes();
			if (s1.size() > 0) {
				sqls.al("/* " + t.getTable().getName() + ", INDEXES:  */");
				for (int i = 0; i < s1.size(); i++) {
					String sqlIndex = s1.get(i);
					sqlIndex = this.replaceMetaOrWorkDatabaseName(sqlIndex);
					LOGGER.debug("TABLE INDEX -> {}", sqlIndex);
					sqls.al(s1.get(i));

					this._Conn.executeUpdateNoParameter(s1.get(i));
					if (this._Conn.getErrorMsg() != null) {
						// sb.append(this._Conn.getErrorMsg() + "\n");
						LOGGER.warn(this._Conn.getErrorMsg());
						sqls.al("/* ERROR:" + this._Conn.getErrorMsg() + " */");
						this._Conn.clearErrorMsg();
					}
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Create the views
	 * 
	 * @param maps
	 * @return
	 */
	private String createDatabaseViews(HashMap<String, SqlTable> maps) {
		StringBuilder sb = new StringBuilder();
		// 处理视图
		String targetDatabase = this._Conn.getDatabaseType();
		boolean isMySqlTarget = SqlUtils.isSqlServer(targetDatabase);
		maps.forEach((key, t) -> {
			if (!"VIEW".equalsIgnoreCase(t.getTable().getTableType())) {
				return;
			}

			String sourceDatabase = t.getTable().getDatabaseType();
			String ddl = t.getCreate();
			ddl = ddl.replace("{SCHMEA}", this._Conn.getSchemaName());
			ddl = this.replaceMetaOrWorkDatabaseName(ddl);

			if (SqlUtils.isMySql(sourceDatabase)) {
				// CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER
				// VIEW
				ddl = this.replaceMysqlDefiner(ddl); // 删除root标记信息
				if (!targetDatabase.equals(sourceDatabase)) {
					ddl = ddl.replace("`", "");
				}
			} else if (SqlUtils.isSqlServer(sourceDatabase) && isMySqlTarget) {
				// 源数据库是sqlserver，目标数据库是mysql
				ddl = StringUtils.replaceIgnoreCase(ddl, "isnull(", "ifnull(");
				ddl = StringUtils.replaceIgnoreCase(ddl, "getdate()", "now()");
			}

			LOGGER.info("Create the view {}", t.getTable().getName());
			LOGGER.debug("VIEW DDL -> {}", ddl);
			sqls.al("/*  " + t.getTable().getName() + ", VIEW DDL:  */");
			sqls.al(ddl);

			this._Conn.executeUpdateNoParameter(ddl);
			if (this._Conn.getErrorMsg() != null) {
				sb.append(this._Conn.getErrorMsg() + "\n");
				sqls.al("/* ERROR:" + this._Conn.getErrorMsg() + " */");

				this._Conn.clearErrorMsg();
			}
		});

		return sb.toString();
	}

	/**
	 * Import table or view structures
	 * 
	 * @return
	 * @throws Exception
	 */
	public String importTables() throws Exception {
		this.readTables();

		String thisDatabase = "";
		if (targetDatabase_ != null) {
			String sql0 = "select top 1 CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA";
			DTTable tb = DTTable.getJdbcTable(sql0);

			if (tb.isOk() && tb.getCount() > 0) {
				thisDatabase = tb.getCell(0, 0).toString();
				_Conn.executeUpdateNoParameter("use " + targetDatabase_);
			}
		}
		String databaseType = this._Conn.getDatabaseType();
		HashMap<String, SqlTable> maps = this.getSqlTables(databaseType);

		StringBuilder errors = new StringBuilder();

		// Create tables
		String errTables = this.createDatabaseTables(maps);
		errors.append(errTables);

		// Create views
		String errViews = this.createDatabaseViews(maps);
		errors.append(errViews);

		if (thisDatabase.trim().length() > 0) {
			_Conn.executeUpdateNoParameter("use " + thisDatabase);
		}
		this._Conn.close();

		return errors.toString();
	}

	/**
	 * Import tables data, and return the errors
	 * 
	 * @return the errors
	 */
	public String importDatas() {
		NodeList nl = UXml.retNodeList(this._DocData, "Datas/Data");
		StringBuilder sb = new StringBuilder();

		if (_TablesInsertFix == null) {
			_TablesInsertFix = new HashMap<String, List<Object>>();
		}
		// this._Conn.getDatabaseType().equalsIgnoreCase("MSSQL");
		boolean isSqlServer = SqlUtils.isSqlServer(_Conn);
		sqls.al("/* import data */");

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getChildNodes().getLength() == 0) {
				// 没有数据节点
				continue;
			}
			Table data = new Table();
			UObjectValue.fromXml(node, data);
			// 避免表和视图重名
			String key = this.getTableKey(data);
			SqlTable st = this._SqlTables.get(key);
			Table t = null;
			if (st == null) { // 老数据，没有扩展属性
				for (int m = 0; m < this._Tables.length; i++) {
					if (this._Tables[m].getName().equals(data.getName())) {
						t = this._Tables[m];
						break;
					}
				}
			} else {
				t = st.getTable();
			}
			if (t == null) {
				LOGGER.warn("Can't fund the table {}", key);
				continue;
			}
			try {
				this.createInsertFix(t);
				if (isSqlServer) {
					String sql = "set IDENTITY_INSERT " + t.getName() + " on";
					sqls.al(sql);
					this._Conn.executeUpdateNoParameter(sql);
				}

				String s = this.importTableRows(t, node);
				sb.append(s);

				if (isSqlServer) {
					String sql = "set IDENTITY_INSERT " + t.getName() + " off";
					sqls.al(sql);
					this._Conn.executeUpdateNoParameter(sql);
				}
			} catch (Exception e) {
				LOGGER.warn(e.getLocalizedMessage());
			}
		}
		_Conn.close();
		return sb.toString();
	}

	private String getTableKey(Table table) {
		String key = table.getName() + "," + table.getTableType() + "," + table.getCatalogName() + ","
				+ table.getSchemaName();
		return key;
	}

	/**
	 * 生成插入模版
	 * 
	 * @param t
	 * @throws Exception
	 */
	private void createInsertFix(Table t) throws Exception {
		String key = getTableKey(t);
		if (_TablesInsertFix.containsKey(key)) {
			return;
		}
		List<Object> lst = new ArrayList<Object>();
		MStr sb = new MStr();

		FixTableOrField fix = FixTableOrField.getInstance(_Conn);

		sb.a("INSERT INTO ").a(t.getDatabasePrefix(this._Conn.getDatabaseType())) // 数据库前缀
				.a(fix.getFixCharBefore()) // 前缀
				.a(t.getName()) // 表名称
				.a(fix.getFixCharAfter()) // 后缀
				.a(" (");
		Field[] fs = new Field[t.getFields().size()];
		MapFieldType[] dts = new MapFieldType[t.getFields().size()];

		HashMap<String, MapFieldType> alFrom = null;
		alFrom = Maps.instance().getMapFieldTypes().getTypes(t.getDatabaseType());

		for (int i = 0; i < t.getFields().size(); i++) {
			String n = t.getFields().getFieldList().get(i);
			Field f = t.getFields().get(n);
			if (i > 0) {
				sb.append(", ");
			}
			sb.a(fix.getFixCharBefore()) // 前缀
					.a(f.getName()) // 字段名称
					.a(fix.getFixCharAfter()); // 后缀
			fs[i] = f;
			String dtType = f.getDatabaseType().toUpperCase();
			if (dtType.indexOf(" ") > 0) { // int identity
				dtType = dtType.split(" ")[0];
			}
			MapFieldType dt = alFrom.get(dtType);
			dts[i] = dt;
		}
		sb.append(") VALUES ");

		lst.add(sb.toString());
		lst.add(fs);
		lst.add(dts);

		_TablesInsertFix.put(key, lst);
	}

	/**
	 * 导入表数据，返回错误信息
	 * 
	 * @param t    表
	 * @param node
	 * @return
	 */
	private String importTableRows(Table t, Node node) {
		String key = this.getTableKey(t);
		List<Object> lst = this._TablesInsertFix.get(key);

		// insert into tb_a(f_aa, f_bb) values
		String insertTableSql = lst.get(0).toString();
		sqls.al(insertTableSql);
		// 字段
		Field[] fs = (Field[]) lst.get(1);
		// 数据类型
		MapFieldType[] dts = (MapFieldType[]) lst.get(2);

		NodeList nl = UXml.retNodeList(node, "Row");

		LOGGER.info("Start to import table {} data, {} records", t.getName(), nl.getLength());
		long t0 = System.currentTimeMillis();
		List<String> values = new ArrayList<>();
		for (int i = 0; i < nl.getLength(); i++) {

			Node row = nl.item(i);
			// values 表达式，例如：(0, 'a')
			String sqlValues = this.createInsertSql(fs, dts, row);
			values.add(sqlValues);
			sqls.al(sqlValues);
		}
		BatchInsert bi = new BatchInsert(this._Conn, false);
		// 批量插入数据的数量
		bi.setMaxInsertCount(batInsertCount);
		String errors = bi.insertBatch(insertTableSql, values);

		LOGGER.info("End import table {} data, {} records, time {}ms", t.getName(), nl.getLength(),
				System.currentTimeMillis() - t0);

		return errors;
	}

	/**
	 * 生成Insert SQL语句
	 * 
	 * @param fs  字段
	 * @param dts 类型
	 * @param row XML数据行
	 * @return
	 */
	private String createInsertSql(Field[] fs, MapFieldType[] dts, Node row) {
		MStr sb1 = new MStr();
		sb1.append("(");
		boolean isMysql = SqlUtils.isMySql(this._Conn);
		for (int m = 0; m < fs.length; m++) {
			Field f = fs[m];
			MapFieldType dt = dts[m];
			// String prefix = dt == null ? "'" : dt.getEwa().getInsertPrefix();
			String v1 = getNodeAtt(row, f.getName());
			if (m > 0) {
				sb1.append(", ");
			}
			if (v1 == null) {
				sb1.append("null");
				continue;
			}
			if (dt == null) {
				sb1.a(this._Conn.sqlParameterStringExp(v1));
				continue;
			}

			String covert = dt.getEwa().getInsertCovert();
			MapFieldType target = null;
			String insertCovert = "";
			try {
				target = dts[m].getEwa().convertTo(this._Conn.getDatabaseType());
				insertCovert = target.getInsertCovert();
			} catch (Exception err) {
				LOGGER.warn("The convert {} -> {} error ,{}", this._Conn.getDatabaseType(), dts[m].getDatabaseName(),
						err.getMessage());
			}
			// boolean isMysql = target != null &&
			// SqlUtils.isMySql(target.getDatabaseName());

			if (covert != null && covert.equals("BIN") && isMysql) {// 二进制转换
				sb1.a("x'");
				sb1.a(v1.replace("'", "''"));
				sb1.a("'");
			} else if (dt.getName().equals("BIT") && SqlUtils.isMySql(dt.getDatabaseName())
					&& SqlUtils.isSqlServer(this._Conn)) {
				if (v1.equalsIgnoreCase("true")) {
					sb1.append("1");
				} else {
					sb1.append("0");
				}
			} else if (covert.equals("DATE")) {
				if (target == null) {
					sb1.append("null");
				} else {
					String v2 = insertCovert.replace("@val", v1);
					sb1.append(v2);
				}
			} else {
				String value = this._Conn.sqlParameterStringExp(v1);
				sb1.a(value);
//				String value =v1.replace("'", "''");
//				if (isMysql) {
//					// 替换转义符
//					value = value.replace("\\", "\\\\");
//				}
// 				sb1.a(prefix + value + prefix);
			}
		}
		sb1.append(")");
		return sb1.toString();
	}

	private String getNodeAtt(Node node, String attName) {
		if (node.getAttributes().getNamedItem(attName) == null) {
			return null;
		}
		return node.getAttributes().getNamedItem(attName).getNodeValue();
	}

	public HashMap<String, SqlTable> getSqlTables(String databaseType) throws Exception {
		if (_SqlTables != null) {
			return _SqlTables;
		}
		_SqlTables = new HashMap<String, SqlTable>();

		for (int i = 0; i < this._Tables.length; i++) {
			SqlTable t = new SqlTable();

			t.createSqlTable(this._Tables[i], databaseType);

			// 避免表和视图重名
			String key = this.getTableKey(this._Tables[i]);
			_SqlTables.put(key, t);
		}
		return _SqlTables;
	}

	private Table initTable(Node node) {
		Table table = new Table();
		table.fromXml((Element) node);
		return table;
	}

	/**
	 * @return the _Conn
	 */
	public DataConnection getConn() {
		return _Conn;
	}

	/**
	 * @param conn the _Conn to set
	 */
	public void setConn(DataConnection conn) {
		_Conn = conn;
	}

	/**
	 * 需要转换的数据库, 执行前执行use targetDatabase，执行后 use prevdatabase
	 * 
	 * @return
	 */
	public String getTargetDatabase() {
		return targetDatabase_;
	}

	/**
	 * 设置需要转换的数据库, 执行前执行use targetDatabase，执行后 use prevdatabase
	 * 
	 * @param targetDatabase_
	 */
	public void setTargetDatabase(String targetDatabase_) {
		this.targetDatabase_ = targetDatabase_;
	}

	/**
	 * DDL replace meta database name, e.g. `main_data_db`
	 * 
	 * @return the replaceMetaDatabaseName
	 */
	public String getReplaceMetaDatabaseName() {
		return replaceMetaDatabaseName;
	}

	/**
	 * DDL replace meta database name, e.g. `main_data_db`
	 * 
	 * @param replaceMetaDatabaseName the replaceMetaDatabaseName to set
	 */
	public void setReplaceMetaDatabaseName(String replaceMetaDatabaseName) {
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
	}

	/**
	 * DDL replace work database name, e.g. `work_db`
	 * 
	 * @return the replaceWorkDatabaseName
	 */
	public String getReplaceWorkDatabaseName() {
		return replaceWorkDatabaseName;
	}

	/**
	 * DDL replace work database name, e.g. `work_db`
	 * 
	 * @param replaceWorkDatabaseName the replaceWorkDatabaseName to set
	 */
	public void setReplaceWorkDatabaseName(String replaceWorkDatabaseName) {
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;
	}

	public Table[] getTables() {
		return _Tables;
	}

	/**
	 * Bulk insert records
	 * 
	 * @return
	 */
	public int getBatInsertCount() {
		return batInsertCount;
	}

	/**
	 * Bulk insert records
	 * 
	 * @param batInsertCount
	 */
	public void setBatInsertCount(int batInsertCount) {
		this.batInsertCount = batInsertCount;
	}
}
