package com.gdxsoft.easyweb.define.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.define.database.maps.MapFieldType;
import com.gdxsoft.easyweb.define.database.maps.MapFieldTypes;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplate;
import com.gdxsoft.easyweb.define.database.maps.MapSqlTemplates;
import com.gdxsoft.easyweb.define.database.maps.Maps;
import com.gdxsoft.easyweb.define.group.ImportTables;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class Table {
	/**
	 * 被替换的DDL的元数据库前缀
	 */
	public static final String REPLACE_META_DATABASE_NAME = "{REPLACE_META_DATABASE_NAME}";
	/**
	 * 被替换的DDL的工作数据库前缀
	 */
	public static final String REPLACE_WORK_DATABASE_NAME = "{REPLACE_WORK_DATABASE_NAME}";

	private static Logger LOGGER = LoggerFactory.getLogger(Table.class);
	private String _Name;
	private String _SchemaName;
	private String _CatalogName;
	private Fields _Fields;
	private String _ConnectionConfigName;
	private String _TableType;
	private TablePk _Pk;
	private ArrayList<TableFk> _Fks;
	private ArrayList<TableIndex> _Indexes;
	private String _DatabaseType;
	private String replaceMetaDatabaseName;
	private String replaceWorkDatabaseName;
	private String _SqlTable; // create table's sql
	private ArrayList<String> _SqlFks; // create fk sql
	private ArrayList<String> _SqlIndexes; // create index sql

	private Document _Doc;

	private DataConnection _Conn;

	private String refId; // 来源参考

	private String sourceXml;

	private ImportTables importTables;

	public ImportTables getImportTables() {
		return importTables;
	}

	public void setImportTables(ImportTables importTables) {
		this.importTables = importTables;
	}

	public Table() {

	}

	/**
	 * 替换数据前缀，例如：my_work.dbo. (sqlserver), my_work.
	 * 
	 * @param table
	 * @param targetDatabaseType
	 * @return
	 */
	public String getDatabasePrefix(String targetDatabaseType) {
		if (this.getReplaceMetaDatabaseName() == null && this.getReplaceMetaDatabaseName().trim().length() == 0) {
			return "";
		}
		boolean isSqlServer = SqlUtils.isSqlServer(targetDatabaseType);
		StringBuilder sb = new StringBuilder();
		if (this.fromMetaDatabase()) {
			sb.append(this.getReplaceMetaDatabaseName());
			if (isSqlServer) {
				sb.append(".dbo.");
			} else {
				sb.append(".");
			}

		} else if (this.fromWorkDatabase()) {
			sb.append(this.getReplaceWorkDatabaseName());
			if (isSqlServer) {
				sb.append(".dbo.");
			} else {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	private String fromSourceDatabase() {
		// 来源表是否是SQLSERVER
		boolean isSourceSqlServer = SqlUtils.isSqlServer(this.getDatabaseType());

		String sourceDatabase = null;
		if (isSourceSqlServer) {
			sourceDatabase = this.getCatalogName();
		} else {
			sourceDatabase = this.getSchemaName();
		}
		return sourceDatabase;
	}

	/**
	 * 来源表/视图是否为meta库
	 * 
	 * @param table
	 * @return
	 */
	public boolean fromMetaDatabase() {
		String sourceDatabase = fromSourceDatabase();
		if (sourceDatabase == null) {
			return false;
		}
		if (getImportTables() == null || getImportTables().getModuleDescription() == null) {
			return false;
		}

		String sourceMeta = getImportTables().getModuleDescription().getMetaDatabase();
		return sourceDatabase.equalsIgnoreCase(sourceMeta);
	}

	/**
	 * 来源表/视图是否为work库
	 * 
	 * @param table
	 * @return
	 */
	public boolean fromWorkDatabase() {
		String sourceDatabase = fromSourceDatabase();
		if (sourceDatabase == null) {
			return false;
		}
		if (getImportTables() == null || getImportTables().getModuleDescription() == null) {
			return false;
		}

		String sourceWork = getImportTables().getModuleDescription().getWorkDatabase();
		return sourceDatabase.equalsIgnoreCase(sourceWork);
	}

	public void writeDataToXml(String where) {
		if (this._Doc == null) {
			this._Doc = UXml.createBlankDocument();
			Element e = this._Doc.createElement("root");
			this._Doc.appendChild(e);
		}

	}

	private void writeTableAttritubes(Element e) {
		e.setAttribute("Name", this._Name);
		e.setAttribute("DatabaseType", this._DatabaseType);
		e.setAttribute("SchamaName", this._SchemaName);
		e.setAttribute("CatalogName", this._CatalogName);
		e.setAttribute("TableType", this._TableType);
	}

	/**
	 * 填写数据到xml
	 * 
	 * @param where  执行条件
	 * @param parent 数据父节点
	 * @throws Exception
	 */
	public void writeDataToXml(String where, Element parent) throws Exception {
		LOGGER.info("Start export {} data to xml", this._Name);
		long t0 = System.currentTimeMillis();
		this._Doc = parent.getOwnerDocument();
		String sql = "SELECT * FROM " + this._Name + " WHERE " + where;
		
		MapFieldTypes maps;
		maps = Maps.instance().getMapFieldTypes();
		
		Element e = this._Doc.createElement("Data");
		this.writeTableAttritubes(e);
		
		String xmlData = UXml.asXml(e);
		LOGGER.info("{}", xmlData);
		
		parent.appendChild(e);
		
		DataConnection conn = new DataConnection();
		conn.setConfigName(_ConnectionConfigName);
		HashMap<String, MapFieldType> map = maps.getTypes(conn.getDatabaseType());
		try {
			conn.executeQuery(sql);
			ResultSet rs = conn.getLastResult().getResultSet();
			while (rs.next()) {
				Element row = this._Doc.createElement("Row");
				e.appendChild(row);
				for (int i = 0; i < this._Fields.size(); i++) {
					Field f = this._Fields.get(this._Fields.getFieldList().get(i));
					MapFieldType mapType = map.get(f.getDatabaseType().toUpperCase().replace(" IDENTITY", ""));
					Object v = rs.getObject(f.getName());
					if (v == null) {
						continue;
					} else {
						String v1;
						String cvt = mapType.getEwa().getInsertCovert();
						if (cvt.equals("BIN")) {
							v1 = Utils.bytes2hex(rs.getBytes(f.getName()));
							row.setAttribute(f.getName(), v1);
						} else if (cvt.equals("DATE")) {
							v1 = Utils.getDateXmlString(rs.getTimestamp(f.getName()));

						} else {
							v1 = v.toString();
						}
						v1 = UXml.filterInvalidXMLcharacter(v1);
						row.setAttribute(f.getName(), v1);
					}
				}
			}
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
		} finally {
			conn.close();
		}
		long t1 = System.currentTimeMillis() - t0;
		LOGGER.info("End export {} data to xml, {}ms", this._Name, t1);
	}

	public void initBlankFrame() {
		_Fields = new Fields();
		_Pk = new TablePk();
		_Fks = new ArrayList<TableFk>();
		this._Indexes = new ArrayList<TableIndex>();
	}

	public String toXml() {
		this._Doc = UXml.createBlankDocument();
		Element e = this._Doc.createElement("root");
		this._Doc.appendChild(e);
		return this.toXml(e);
	}

	/**
	 * 填写字段信息到xml
	 * 
	 * @param parent 父节点
	 * @return
	 */
	public String toXml(Element parent) {
		this._Doc = parent.getOwnerDocument();
		Element e = this._Doc.createElement("Table");
		parent.appendChild(e);
		this.writeTableAttritubes(e);

		Element eFields = this._Doc.createElement("Fields");
		e.appendChild(eFields);

		for (int i = 0; i < this._Fields.size(); i++) {
			Field f = this._Fields.get(this._Fields.getFieldList().get(i));
			Element eleField = this._Doc.createElement("Field");
			eFields.appendChild(eleField);

			UObjectValue.writeXmlNodeAtts(eleField, f);

		}

		Element ePk = this._Doc.createElement("Pk");
		e.appendChild(ePk);
		TablePk pk = this._Pk;
		UObjectValue.writeXmlNodeAtts(ePk, pk);
		for (int i = 0; i < pk.getPkFields().size(); i++) {
			Field pk0 = pk.getPkFields().get(i);
			Element elePk = this._Doc.createElement("PkField");
			ePk.appendChild(elePk);
			elePk.setAttribute("Name", pk0.getName());
		}

		Element eIndexes = this._Doc.createElement("Indexes");
		e.appendChild(eIndexes);
		ArrayList<TableIndex> idx = this._Indexes;
		for (int i = 0; i < idx.size(); i++) {
			TableIndex idx0 = idx.get(i);
			Element eleIndex = this._Doc.createElement("Index");
			eIndexes.appendChild(eleIndex);
			UObjectValue.writeXmlNodeAtts(eleIndex, idx0);

			for (int m = 0; m < idx0.getIndexFields().size(); m++) {
				Element eleIndexField = this._Doc.createElement("IndexField");
				eleIndex.appendChild(eleIndexField);
				IndexField f = idx0.getIndexFields().get(m);
				UObjectValue.writeXmlNodeAtts(eleIndexField, f);
			}
		}

		// 创建表或视图的ddl
		if (this._SqlTable != null) {
			Element ddl = this._Doc.createElement("DDL");
			ddl.setTextContent(_SqlTable);
			e.appendChild(ddl);
		}

		return UXml.asXml(e);

	}

	/**
	 * 从XML对象返回 Table
	 * 
	 * @param eleTable
	 */
	public void fromXml(Element eleTable) {
		this.sourceXml = UXml.asXmlPretty(eleTable);

		NodeList nlFields = UXml.retNodeList(eleTable, "Fields/Field");
		this.initBlankFrame();
		UObjectValue.fromXml(eleTable, this);
		for (int i = 0; i < nlFields.getLength(); i++) {
			Node n = nlFields.item(i);
			Field f = new Field();
			UObjectValue.fromXml((Element) n, f);
			this._Fields.put(f.getName(), f);
			this._Fields.getFieldList().add(f.getName());
		}

		Node nPk = UXml.retNode(eleTable, "Pk");
		UObjectValue.fromXml(nPk, this._Pk);
		NodeList nlPk = UXml.retNodeList(nPk, "PkField");
		for (int i = 0; i < nlPk.getLength(); i++) {
			Field f = new Field();
			this._Pk.getPkFields().add(f);
			UObjectValue.fromXml(nlPk.item(i), f);
		}

		NodeList nlIndexes = UXml.retNodeList(eleTable, "Indexes/Index");
		for (int i = 0; i < nlIndexes.getLength(); i++) {
			TableIndex idx = new TableIndex();
			this._Indexes.add(idx);
			UObjectValue.fromXml(nlIndexes.item(i), idx);
			nlFields = UXml.retNodeList(nlIndexes.item(i), "IndexField");
			for (int m = 0; m < nlFields.getLength(); m++) {
				IndexField f = new IndexField();
				idx.getIndexFields().add(f);

				UObjectValue.fromXml(nlFields.item(m), f);
			}
		}
		NodeList ddlNode = UXml.retNodeList(eleTable, "DDL");
		if (ddlNode.getLength() > 0) {
			String ddl = ddlNode.item(0).getTextContent();
			this._SqlTable = ddl;
		}

	}

	public Table(String tableName, String schemaName, String tableType, String connectionConfigName) {
		this._Name = tableName;
		this._SchemaName = schemaName;
		_ConnectionConfigName = connectionConfigName;
		this._TableType = tableType;
	}

	public Table(String tableName, String schemaName, String connectionConfigName) {
		this._Name = tableName;
		this._SchemaName = schemaName;
		_ConnectionConfigName = connectionConfigName;
		DataConnection conn = new DataConnection();
		conn.setConfigName(_ConnectionConfigName);
		_DatabaseType = conn.getCurrentConfig().getType();

	}

	public Table(String tableName, String connectionConfigName) {
		this._Name = tableName;
		_ConnectionConfigName = connectionConfigName;
		DataConnection conn = new DataConnection();
		conn.setConfigName(_ConnectionConfigName);
		_DatabaseType = conn.getCurrentConfig().getType();
		this._SchemaName = conn.getCurrentConfig().getSchemaName();

	}

	/**
	 * 初始化对象
	 * 
	 * @param tableName 表名称
	 * @param conn      连接
	 */
	public Table(String tableName, DataConnection conn) {
		this._Name = tableName;
		_ConnectionConfigName = conn.getCurrentConfig().getConnectionString();
		_DatabaseType = conn.getCurrentConfig().getType();
		this._SchemaName = conn.getCurrentConfig().getSchemaName();

		this._Conn = conn;
	}

	public void init() {
		DataConnection conn;
		if (this._Conn == null) {
			conn = new DataConnection();
			conn.setConfigName(_ConnectionConfigName);
		} else {
			conn = this._Conn;
		}

		_DatabaseType = conn.getCurrentConfig().getType();
		_Fields = new Fields();
		try {
			conn.connect();
			DatabaseMetaData dataMeta = conn.getConnection().getMetaData();

			this.initTableInfoFromInformationSchema(conn);

			initFields(dataMeta);
			initPk(dataMeta);
			initFk(dataMeta);
			initIndexes(dataMeta);
			initRemarks(conn);
			initRemarks();
			initFieldIdentity(conn);

			// 获取创建表或视图的DDL
			this.initTableOrViewDDL(conn);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (Exception ee) {
				LOGGER.error(ee.getMessage());
			}
		}

	}

	/**
	 * 获取创建表或视图的DDL语句
	 * 
	 * @param conn
	 */
	private void initTableOrViewDDL(DataConnection conn) {
		boolean isView = "VIEW".equalsIgnoreCase(this._TableType);
		String tempName = isView ? "ViewDDL" : "TableDDL";
		String sql = this.getSqlTemplate(conn, tempName);
		if (sql == null || sql.trim().length() < 4) {
			return;
		}
		sql = sql.replace("{SCHMEA}", this._SchemaName);
		sql = sql.replace("{TABLE_NAME}", this._Name);
		if (!conn.executeQuery(sql)) {
			LOGGER.error(conn.getErrorMsg());
			return;
		}

		boolean isSqlServer = "MSSQL".equalsIgnoreCase(conn.getCurrentConfig().getType())
				|| "SqlServer".equalsIgnoreCase(conn.getCurrentConfig().getType());

		DTTable tb = new DTTable();
		tb.initData(conn.getLastResult().getResultSet());
		if (tb.getCount() == 0) {
			return;
		}
		String colName = null;
		if (tb.getColumns().testName("Create View")) { // mysql
			colName = "Create View";
		} else if (tb.getColumns().testName("Create Table")) {// mysql
			colName = "Create Table";
		} else if (tb.getColumns().testName("text")) {// sqlserver
			colName = "text";
		}

		if (colName == null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tb.getColumns().getCount(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(tb.getColumns().getColumn(i).getName());
			}
			LOGGER.error("NOT defined the DDL column name from {}", sb.toString());
			return;
		}

		String ddl = null;
		StringBuilder sbDdl = new StringBuilder();
		try {

			for (int i = 0; i < tb.getCount(); i++) {
				String s = tb.getCell(i, colName).toString();
				sbDdl.append(s);
			}
			ddl = sbDdl.toString();
		} catch (Exception e) {
			LOGGER.error("{}", e.getMessage());
			return;
		}
		if (!isView) { // 表
			this.setSqlTable(ddl);
			return;
		}

		// 视图替换数据库名称
		if (this.replaceMetaDatabaseName != null) {
			// 替换元数据库的前缀
			// ddl = ddl.replace(replaceMetaDatabaseName, REPLACE_META_DATABASE_NAME);
			if (isSqlServer) {
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceMetaDatabaseName + "..",
						REPLACE_META_DATABASE_NAME + ".");
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceMetaDatabaseName + ".dbo.",
						REPLACE_META_DATABASE_NAME + ".");
			} else {
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceMetaDatabaseName, REPLACE_META_DATABASE_NAME);
			}
		}
		if (this.replaceWorkDatabaseName != null) {
			// 替换工作数据库前缀
			// ddl = ddl.replace(replaceWorkDatabaseName, REPLACE_WORK_DATABASE_NAME);
			if (isSqlServer) {
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceWorkDatabaseName + "..",
						REPLACE_WORK_DATABASE_NAME + ".");
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceWorkDatabaseName + ".dbo.",
						REPLACE_WORK_DATABASE_NAME + ".");
			} else {
				ddl = StringUtils.replaceIgnoreCase(ddl, replaceWorkDatabaseName, REPLACE_WORK_DATABASE_NAME);
			}
		}
		if (isSqlServer) {
			ddl = ddl.replace("[", "").replace("]", "").replace("dbo.", "");
			ddl = StringUtils.replaceIgnoreCase(ddl, "CREATE VIEW", "CREATE VIEW " + REPLACE_WORK_DATABASE_NAME + ".",
					1);
		}
		this.setSqlTable(ddl);

	}

	/**
	 * Initialize the table catalog name
	 * 
	 * @param conn Database connection pool
	 */
	private void initTableInfoFromInformationSchema(DataConnection conn) {
		String sql = "select * from information_schema.TABLES t where TABLE_SCHEMA ='"
				+ this._SchemaName.replace("'", "''") + "' and table_name='" + this._Name.replace("'", "''") + "'";
		DTTable tb = DTTable.getJdbcTable(sql, conn);
		if (tb.getCount() == 0) {
			return;
		}

		try {
			this._CatalogName = tb.getCell(0, "TABLE_CATALOG").toString();
		} catch (Exception e) {

		}
	}

	public void init(String targetDatabase) {
		DataConnection conn;
		if (this._Conn == null) {
			conn = new DataConnection();
			conn.setConfigName(_ConnectionConfigName);
		} else {
			conn = this._Conn;
		}
		String thisDatabase = "";
		String sql0 = "select top 1 CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA";
		DTTable tb = DTTable.getJdbcTable(sql0);

		if (tb.isOk() && tb.getCount() > 0) {
			thisDatabase = tb.getCell(0, 0).toString();
			conn.executeUpdateNoParameter("use " + targetDatabase);
		}

		_DatabaseType = conn.getCurrentConfig().getType();
		_Fields = new Fields();
		try {
			conn.connect();
			DatabaseMetaData dataMeta = conn.getConnection().getMetaData();

			initTableInfoFromInformationSchema(conn);

			initFields(dataMeta);
			initPk(dataMeta);
			initFk(dataMeta);
			initIndexes(dataMeta);
			initRemarks(conn);
			initRemarks();
			initFieldIdentity(conn);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			try {
				if (thisDatabase.trim().length() > 0) {
					conn.executeUpdateNoParameter("use " + thisDatabase);
				}
				conn.close();
			} catch (Exception ee) {
				LOGGER.error(ee.getMessage());
			}
		}

	}

	/**
	 * 初始化字段信息
	 * 
	 * @param dataMeta
	 */
	private void initFields(DatabaseMetaData dataMeta) {
		ResultSet rs = null;
		boolean isMySql = false;
		HashMap<String, Integer> fieldsMap = new HashMap<String, Integer>();
		try {
			rs = dataMeta.getColumns(null, _SchemaName, _Name, null);
			ResultSetMetaData md = rs.getMetaData();
			String name1 = md.getClass().getName();
			if (name1.indexOf("com.mysql") >= 0) {
				isMySql = true;
			}
			for (int i = 1; i <= md.getColumnCount(); i++) {
				String name;
				if (isMySql) {
					// mysql bug??
					name = md.getColumnLabel(i);
				} else {
					name = md.getColumnName(i);
				}
				String nameLabel = md.getColumnLabel(i);
				if (nameLabel != null) {
					name = nameLabel; // 出现在mysql中
				}
				fieldsMap.put(name, i);
			}
			while (rs.next()) {
				Field f1 = new Field();
				f1.setTableName(_Name);
				String colName = rs.getString("COLUMN_NAME");

				f1.setName(colName);
				f1.setDatabaseType(rs.getString("TYPE_NAME"));
				String[] names = rs.getString("TYPE_NAME").split(" ");
				if (names.length == 2 && names[1].toLowerCase().indexOf("identity") == 0) {
					// sqlserver
					f1.setIdentity(true);
				}
				if (fieldsMap.containsKey("IS_AUTOINCREMENT")) {
					String IS_AUTOINCREMENT = rs.getString("IS_AUTOINCREMENT");

					if ("YES".equals(IS_AUTOINCREMENT)) {
						f1.setIdentity(true);
					}
				}

				f1.setDescription(rs.getString("REMARKS"));

				f1.setCharOctetLength(rs.getInt("CHAR_OCTET_LENGTH"));
				f1.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
				f1.setDataType(rs.getInt("DATA_TYPE"));
				f1.setColumnSize(rs.getInt("COLUMN_SIZE"));
				f1.setMaxlength(rs.getInt("COLUMN_SIZE"));

				if (rs.getString("IS_NULLABLE").equals("YES")) {
					f1.setNull(true);
				} else {
					f1.setNull(false);
				}

				f1.setFKColumnName("");
				f1.setFKTableName("");

				_Fields.put(f1.getName(), f1);
				_Fields.getFieldList().add(f1.getName());
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * 更新字段备注信息
	 * 
	 * @param conn
	 */
	private void initRemarks(DataConnection conn) {
		String sql = this.getSqlTemplate(conn, "FieldCommentsGet");

		if (sql == null || sql.trim().length() < 4) {
			return;
		}
		sql = sql.replace("{SCHMEA}", this._SchemaName);
		sql = sql.replace("{TABLE_NAME}", this._Name);
		if (!conn.executeQuery(sql)) {
			LOGGER.error(conn.getErrorMsg());
			return;
		}
		DTTable tb = new DTTable();
		tb.initData(conn.getLastResult().getResultSet());

		try {
			for (int i = 0; i < tb.getCount(); i++) {
				DTRow r = tb.getRow(i);
				String colName = r.getCell("COLUMN_NAME").toString();
				String remarks = r.getCell("REMARKS").toString();
				if (remarks != null) {
					Field f1 = _Fields.get(colName);
					if (f1 != null) {
						f1.setDescription(remarks);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void initIndexes(DatabaseMetaData dataMeta) {
		ResultSet rs = null;
		_Indexes = new ArrayList<TableIndex>();
		try {
			rs = dataMeta.getIndexInfo(null, _SchemaName, _Name, false, true);
			while (rs.next()) {
				String idxName = "";
				TableIndex index = null;
				while (rs.next()) {
					String name = rs.getString("INDEX_NAME");
					if (!name.equals(idxName)) {
						index = new TableIndex();
						index.setIndexName(name);
						boolean b = rs.getBoolean("NON_UNIQUE");
						index.setUnique(!b);
						_Indexes.add(index);
						idxName = name;
					}
					IndexField f2 = new IndexField();
					f2.setName(rs.getString("COLUMN_NAME"));
					try {
						String asc = rs.getString("ASC_OR_DESC");
						if (asc == null || asc.equals("A")) {
							f2.setAsc(true);
						} else {
							f2.setAsc(false);
						}
					} catch (Exception err) {
						System.out.println(err.getMessage());
					}
					index.getIndexFields().add(f2);
				}
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {

				}
			}
		}
	}

	private void initRemarks() {
		for (int i = 0; i < this._Fields.getFieldList().size(); i++) {
			Field f1 = this._Fields.get(this._Fields.getFieldList().get(i));
			if (f1.getDescription() == null || f1.getDescription().equals("")) {
				f1.setDescription(f1.getName());
			}
		}
	}

	/**
	 * 获取SQL模板，在TypesMap.xml中定义，返回指定名称的SQL模板
	 * 
	 * @param conn    数据库连接
	 * @param tmpName 模板名称{PrimaryKey,FieldCommentsGet,FieldCommentSet,IdentityField
	 *                ... }
	 * @return
	 */
	private String getSqlTemplate(DataConnection conn, String tmpName) {
		String sql = null;
		try {
			MapSqlTemplates sqls = Maps.instance().getMapSqlTemplates();
			MapSqlTemplate c = sqls.getSqlTemplate(conn.getDatabaseType());
			if (c == null) {
				return null;
			}
			sql = c.getSqlTemplate(tmpName);
			return sql;

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 获取字段是否为自增型
	 * 
	 * @param conn
	 */
	private void initFieldIdentity(DataConnection conn) {
		String sql = this.getSqlTemplate(conn, "IdentityField");
		if (sql == null || sql.trim().length() < 4) {
			return;
		}
		sql = sql.replace("{SCHMEA}", this._SchemaName);
		sql = sql.replace("{TABLE_NAME}", this._Name);
		if (!conn.executeQuery(sql)) {
			LOGGER.error(conn.getErrorMsg());
			return;
		}
		DTTable tb = new DTTable();
		tb.initData(conn.getLastResult().getResultSet());
		if (tb.getCount() == 0) {
			return;
		}
		DTRow r = tb.getRow(0);
		String colName = null;
		try {
			colName = r.getCell("COLUMN_NAME").toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		if (colName == null) {
			return;
		}
		if (this._Fields.containsKey(colName)) {
			Field f = this._Fields.get(colName);
			f.setIdentity(true);
		}
	}

	/**
	 * 初始化主键
	 * 
	 * @param dataMeta
	 */
	private void initPk(DatabaseMetaData dataMeta) {
		this._Pk = new TablePk();
		this._Pk.setTableName(this._Name);
		ResultSet rs = null;
		try {
			rs = dataMeta.getPrimaryKeys(null, _SchemaName, _Name);
			while (rs.next()) {
				if (_Pk.getPkName() == null) {
					String name = rs.getString("PK_NAME");
					_Pk.setPkName(name);
				}
				Field f1 = this._Fields.get(rs.getString("COLUMN_NAME"));
				f1.setPk(true);
				_Pk.getPkFields().add(f1);
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * 初始化外键
	 * 
	 * @param dataMeta
	 */
	private void initFk(DatabaseMetaData dataMeta) {
		this._Fks = new ArrayList<TableFk>();
		ResultSet rs = null;
		try {
			rs = dataMeta.getImportedKeys(null, _SchemaName, _Name);
			String fkName = "";
			TableFk fk = null;
			while (rs.next()) {
				String name = rs.getString("FK_NAME");
				if (!name.equals(fkName)) {
					fk = new TableFk();
					fk.setFkName(name);
					fk.setTableName(this._Name);
					TablePk pk = new TablePk();
					pk.setPkName(rs.getString("PK_NAME"));
					pk.setTableName(rs.getString("PKTABLE_NAME"));
					fk.setPk(pk);
					_Fks.add(fk);
					fkName = name;
				}
				Field f2 = this._Fields.get(rs.getString("FKCOLUMN_NAME"));
				f2.setFk(true);
				f2.setFKTableName(rs.getString("PKTABLE_NAME"));
				f2.setFKColumnName(rs.getString("PKCOLUMN_NAME"));
				fk.getFkFields().add(f2);
				Field f1 = new Field();
				f1.setName(rs.getString("PKCOLUMN_NAME"));
				f1.setTableName(rs.getString("PKTABLE_NAME"));
				fk.getPk().getPkFields().add(f1);
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	public String toJs() {
		StringBuilder sb = new StringBuilder();
		sb.append(toJs(""));
		HashMap<String, String> tables = new HashMap<String, String>();
		for (int i = 0; i < this.getFields().getFieldList().size(); i++) {
			Field f1 = this._Fields.get(this._Fields.getFieldList().get(i));
			if (!f1.isFk()) {
				continue;
			}
			if (tables.get(f1.getFKTableName()) == null) {
				Table tb = new Table(f1.getFKTableName(), this._SchemaName, this._ConnectionConfigName);
				sb.append(tb.toJs(tb.getName()));
				tables.put(f1.getFKTableName(), "hehe 啥也没有");
			}
		}
		return sb.toString();
	}

	public String toJs(String name) {
		StringBuilder js = new StringBuilder();
		js.append("FIELDS" + name + "=new Array();\r\n");
		for (int i = 0; i < this.getFields().getFieldList().size(); i++) {
			Field f1 = this._Fields.get(this._Fields.getFieldList().get(i));
			js.append("FIELDS" + name + "[" + i + "]=new Object();\r\n");
			js.append("FIELDS" + name + "[" + i + "].NAME='" + f1.getName() + "';\r\n");
			js.append("FIELDS" + name + "[" + i + "].DESCRIPTION='" + f1.getDescription() + "';\r\n");
			js.append("FIELDS" + name + "[" + i + "].MAXLENGTH='" + f1.getMaxlength() + "';\r\n");
			js.append("FIELDS" + name + "[" + i + "].DATABASETYPE='" + f1.getDatabaseType() + "';\r\n");
			js.append("FIELDS" + name + "[" + i + "].ISPK=" + f1.isPk() + ";\r\n");
			js.append("FIELDS" + name + "[" + i + "].ISNULL=" + f1.isNull() + ";\r\n");
			js.append("FIELDS" + name + "[" + i + "].ISFK=" + f1.isFk() + ";\r\n");
			js.append("FIELDS" + name + "[" + i + "].FKTABLENAME='" + f1.getFKTableName() + "';\r\n");
			js.append("FIELDS" + name + "[" + i + "].FKCOLUMNNAME='" + f1.getFKColumnName() + "';\r\n");
		}
		return js.toString();
	}

	public Fields getFields() {
		if (this._Fields == null) {
			this.init();
		}
		return _Fields;
	}

	public String getName() {
		return _Name;
	}

	public String getSchemaName() {
		return _SchemaName;
	}

	/**
	 * @return the _TableType
	 */
	public String getTableType() {
		return _TableType;
	}

	/**
	 * @param tableType the _TableType to set
	 */
	public void setTableType(String tableType) {
		_TableType = tableType;
	}

	/**
	 * @return the _Pk
	 */
	public TablePk getPk() {
		if (this._Fields == null) {
			this.init();
		}
		return _Pk;
	}

	/**
	 * @param pk the _Pk to set
	 */
	public void setPk(TablePk pk) {
		_Pk = pk;
	}

	/**
	 * @return the _Fks
	 */
	public ArrayList<TableFk> getFks() {
		if (this._Fields == null) {
			this.init();
		}
		return _Fks;
	}

	/**
	 * @param fks the _Fks to set
	 */
	public void setFks(ArrayList<TableFk> fks) {
		_Fks = fks;
	}

	/**
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @param schemaName the _SchemaName to set
	 */
	public void setSchemaName(String schemaName) {
		_SchemaName = schemaName;
	}

	/**
	 * @param connectionConfigName the _ConnectionConfigName to set
	 */
	public void setConnectionConfigName(String connectionConfigName) {
		_ConnectionConfigName = connectionConfigName;
	}

	/**
	 * @param fields the _Fields to set
	 */
	public void setFields(Fields fields) {
		_Fields = fields;
	}

	/**
	 * @return the _DatabaseType
	 */
	public String getDatabaseType() {
		return _DatabaseType;
	}

	/**
	 * @param databaseType the _DatabaseType to set
	 */
	public void setDatabaseType(String databaseType) {
		_DatabaseType = databaseType;
	}

	/**
	 * DDL
	 * 
	 * @return the _SqlTable
	 */
	public String getSqlTable() {
		return _SqlTable;
	}

	/**
	 * DDL
	 * 
	 * @param sqlTable the _SqlTable to set
	 */
	public void setSqlTable(String sqlTable) {
		_SqlTable = sqlTable;
	}

	/**
	 * @return the _SqlFks
	 */
	public ArrayList<String> getSqlFks() {
		return _SqlFks;
	}

	/**
	 * @param sqlFks the _SqlFks to set
	 */
	public void setSqlFks(ArrayList<String> sqlFks) {
		_SqlFks = sqlFks;
	}

	/**
	 * @return the _SqlIndexs
	 */
	public ArrayList<String> getSqlIndexes() {
		return _SqlIndexes;
	}

	/**
	 * @param sqlIndexs the _SqlIndexs to set
	 */
	public void setSqlIndexes(ArrayList<String> sqlIndexes) {
		_SqlIndexes = sqlIndexes;
	}

	/**
	 * @return the _Indexes
	 */
	public ArrayList<TableIndex> getIndexes() {
		return _Indexes;
	}

	/**
	 * @param indexes the _Indexes to set
	 */
	public void setIndexes(ArrayList<TableIndex> indexes) {
		_Indexes = indexes;
	}

	/**
	 * @return the _CatalogName
	 */
	public String getCatalogName() {
		return _CatalogName;
	}

	/**
	 * @param _CatalogName the _CatalogName to set
	 */
	public void setCatalogName(String _CatalogName) {
		this._CatalogName = _CatalogName;
	}

	/**
	 * 被替换的DDL的元数据库前缀
	 * 
	 * @return the replaceMetaDatabaseName
	 */
	public String getReplaceMetaDatabaseName() {
		return replaceMetaDatabaseName;
	}

	/**
	 * 被替换的DDL的元数据库前缀
	 * 
	 * @param replaceMetaDatabaseName the replaceMetaDatabaseName to set
	 */
	public void setReplaceMetaDatabaseName(String replaceMetaDatabaseName) {
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
	}

	/**
	 * 被替换的DDL的工作数据库前缀
	 * 
	 * @return the replaceWorkDatabaseName
	 */
	public String getReplaceWorkDatabaseName() {
		return replaceWorkDatabaseName;
	}

	/**
	 * 被替换的DDL的工作数据库前缀
	 * 
	 * @param replaceWorkDatabaseName the replaceWorkDatabaseName to set
	 */
	public void setReplaceWorkDatabaseName(String replaceWorkDatabaseName) {
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;
	}

	/**
	 * 来源参考
	 * 
	 * @return the refId
	 */
	public String getRefId() {
		return refId;
	}

	/**
	 * 来源参考
	 * 
	 * @param refId the refId to set
	 */
	public void setRefId(String refId) {
		this.refId = refId;
	}

	/**
	 * 来源的xml内容
	 * 
	 * @return
	 */
	public String getSourceXml() {
		return sourceXml;
	}

}
