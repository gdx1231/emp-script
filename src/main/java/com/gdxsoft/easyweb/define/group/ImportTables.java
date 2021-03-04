package com.gdxsoft.easyweb.define.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.maps.MapFieldType;
import com.gdxsoft.easyweb.define.database.maps.Maps;
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

	public ImportTables(Document docTable, Document docData, DataConnection conn) {
		this._DocTable = docTable;
		this._DocData = docData;

		this._Conn = conn;
	}

	private void readTables() {
		NodeList nl = UXml.retNodeList(this._DocTable, "Tables/Table");
		this._Tables = new Table[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++) {
			Table table = this.initTable(nl.item(i));
			this._Tables[i] = table;
		}
	}

	/**
	 * 导入表结构
	 * 
	 * @return
	 * @throws Exception
	 */
	public String importTables() throws Exception {
		readTables();
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
		Iterator<String> it = maps.keySet().iterator();
		StringBuilder sb = new StringBuilder();

		while (it.hasNext()) {
			SqlTable t = maps.get(it.next());
			String s = t.getCreate();
			// 表结构
			this._Conn.executeUpdateNoParameter(s);
			if (this._Conn.getErrorMsg() != null) {
				sb.append(this._Conn.getErrorMsg() + "\r\n");
				this._Conn.clearErrorMsg();
			}

			// 主键
			if (t.getPk() != null && t.getPk().trim().length() > 0) {
				this._Conn.executeUpdateNoParameter(t.getPk());
				if (this._Conn.getErrorMsg() != null) {
					sb.append(this._Conn.getErrorMsg() + "\r\n");
					this._Conn.clearErrorMsg();
				}
			}

			// 字段备注
			ArrayList<String> s2 = t.getComments();
			for (int i = 0; i < s2.size(); i++) {
				String sql = s2.get(i);
				sql = sql.replace("{SCHMEA}", this._Conn.getSchemaName());
				this._Conn.executeUpdateNoParameter(sql);
				if (this._Conn.getErrorMsg() != null) {
					sb.append(this._Conn.getErrorMsg() + "\r\n");
					this._Conn.clearErrorMsg();
				}
			}

			// 索引
			ArrayList<String> s1 = t.getIndexes();
			for (int i = 0; i < s1.size(); i++) {
				this._Conn.executeUpdateNoParameter(s1.get(i));
				if (this._Conn.getErrorMsg() != null) {
					// sb.append(this._Conn.getErrorMsg() + "\r\n");
					System.out.println("警告：" + this._Conn.getErrorMsg());
					this._Conn.clearErrorMsg();
				}
			}
		}
		if (thisDatabase.trim().length() > 0) {
			_Conn.executeUpdateNoParameter("use " + thisDatabase);
		}
		this._Conn.close();

		return sb.toString();
	}

	/**
	 * 导入数据，返回错误信息
	 * 
	 * @return
	 */
	public String importDatas() {
		NodeList nl = UXml.retNodeList(this._DocData, "Datas/Data");
		StringBuilder sb = new StringBuilder();

		if (_TablesInsertFix == null) {
			_TablesInsertFix = new HashMap<String, List<Object>>();
		}

		try {
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				String name = UXml.retNodeValue(node, "Name");
				Table t = this._SqlTables.get(name).getTable();
				this.createInsertFix(t);

				if (this._Conn.getDatabaseType().equalsIgnoreCase("MSSQL")) {
					String sql = "set IDENTITY_INSERT " + t.getName() + " on";
					this._Conn.executeUpdateNoParameter(sql);
				}

				String s = this.importTableRows(t, node);
				sb.append(s);

				if (this._Conn.getDatabaseType().equalsIgnoreCase("MSSQL")) {
					String sql = "set IDENTITY_INSERT " + t.getName() + " off";
					this._Conn.executeUpdateNoParameter(sql);
				}
			}
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage());
		} finally {
			_Conn.close();
		}
		return sb.toString();
	}

	/**
	 * 生成插入模版
	 * 
	 * @param t
	 * @throws Exception
	 */
	private void createInsertFix(Table t) throws Exception {
		if (_TablesInsertFix.containsKey(t.getName())) {
			return;
		}
		List<Object> lst = new ArrayList<Object>();
		MStr sb = new MStr();
		sb.a("INSERT INTO " + t.getName() + " (");
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
			sb.append(f.getName());
			fs[i] = f;
			String dtType = f.getDatabaseType().toUpperCase();
			if (dtType.indexOf(" ") > 0) { // int identity
				dtType = dtType.split(" ")[0];
			}
			MapFieldType dt = alFrom.get(dtType);
			dts[i] = dt;
		}
		sb.append(") VALUES(");

		lst.add(sb.toString());
		lst.add(fs);
		lst.add(dts);

		_TablesInsertFix.put(t.getName(), lst);
	}

	/**
	 * 导入表数据，返回错误信息
	 * 
	 * @param t
	 *            表
	 * @param node
	 * @return
	 */
	private String importTableRows(Table t, Node node) {
		List<Object> lst = this._TablesInsertFix.get(t.getName());
		// sql
		Field[] fs = (Field[]) lst.get(1);
		MapFieldType[] dts = (MapFieldType[]) lst.get(2);

		StringBuilder sbError = new StringBuilder();
		NodeList nl = UXml.retNodeList(node, "Row");
		for (int i = 0; i < nl.getLength(); i++) {
			Node row = nl.item(i);
			String sql = lst.get(0).toString() + this.createInsertSql(fs, dts, row);
			String err = this.execInsert(sql);
			if (err != null) {
				sbError.append(err);
			}
		}
		return sbError.toString();
	}

	/**
	 * 执行插入
	 * 
	 * @param sql
	 * @return
	 */
	private String execInsert(String sql) {
		this._Conn.executeUpdateNoParameter(sql);
		String err = null;
		if (this._Conn.getErrorMsg() != null && this._Conn.getErrorMsg().trim().length() > 0) {
			err = this._Conn.getErrorMsg();
			this._Conn.clearErrorMsg();
		}
		return err;
	}

	/**
	 * 生成Insert SQL语句
	 * 
	 * @param fs
	 * @param dts
	 * @param row
	 * @return
	 */
	private String createInsertSql(Field[] fs, MapFieldType[] dts, Node row) {
		MStr sb1 = new MStr();
		for (int m = 0; m < fs.length; m++) {
			Field f = fs[m];
			MapFieldType dt = dts[m];
			String prefix = dt == null ? "'" : dt.getEwa().getInsertPrefix();
			String v1 = getNodeAtt(row, f.getName());
			if (m > 0) {
				sb1.append(", ");
			}
			if (v1 == null) {
				sb1.append("null");
				continue;
			}
			if (dt == null) {
				sb1.a("'" + v1.replace("'", "''") + "'");
				continue;
			}

			// if (v1.equals("true") || v1.equals("false")) {
			// int zzzzzzzzzzzz = 0;
			// }
			String covert = dt.getEwa().getInsertCovert();
			MapFieldType target = null;
			String insertCovert = "";
			try {
				target = dts[m].getEwa().convertTo(this._Conn.getDatabaseType());
				insertCovert = target.getInsertCovert();
			} catch (Exception err) {

			}

			if (covert != null && covert.equals("BIN") && target.getDatabaseName().equalsIgnoreCase("mysql")) {
				//二进制转换
				sb1.a("x'");
				sb1.a(v1.replace("'", "''"));
				sb1.a("'");
			} else if (dt.getName().equals("BIT") && dt.getDatabaseName().equalsIgnoreCase("mysql")
					&& this._Conn.getDatabaseType().equalsIgnoreCase("MSSQL")) {
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
				sb1.a(prefix + v1.replace("'", "''") + prefix);
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
			String key = this._Tables[i].getName();
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
	 * @param conn
	 *            the _Conn to set
	 */
	public void setConn(DataConnection conn) {
		_Conn = conn;
	}

}
