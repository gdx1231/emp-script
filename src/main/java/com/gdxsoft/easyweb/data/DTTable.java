package com.gdxsoft.easyweb.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.cache.SqlCachedValue;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class DTTable implements Serializable {
	private static Logger LOGGER = LoggerFactory.getLogger(DTTable.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 123456L;
	private DTColumns _Columns;
	private DTRows _Rows;
	private boolean _IsOk;
	private String _ErrorInfo;
	private Document _Document;
	private NodeList _DataList;
	// true，从xml节点下的子节点获取数据，false 从当前节点属性获取数据
	private boolean _IsXmlChildNode = false;
	private String _XmlDataTag;
	private DTIndexes _Indexes;
	private boolean _IsBuildIndex = false; // 是否建立index
	private Node _XmlNodeDataParent; // xml数据节点父节点
	private String _XmlDataNodeName; // xml数据节点名称
	private String _Name;
	private MTable _AttsTable;

	private IBinaryHandle _JsonBinaryHandle = new DefaultBinaryHandle(); // 处理二进制
	private boolean _IshaveImage;

	private int _TimeDiffMinutes; // 用户和系统的时差

	/**
	 * 用户和系统的时差(分钟)
	 * 
	 * @return
	 */
	public int getTimeDiffMinutes() {
		return _TimeDiffMinutes;
	}

	/**
	 * 用户和系统的时差(分钟)
	 * 
	 * @param timeDiffMinutes 分钟
	 */
	public void setTimeDiffMinutes(int timeDiffMinutes) {
		this._TimeDiffMinutes = timeDiffMinutes;
	}

	private static Logger LOOGER = LoggerFactory.getLogger(SqlCached.class);

	// 郭磊
	// 2016-08-20

	/**
	 * 设置JSON处理二进制的方法
	 * 
	 * @return
	 */
	public IBinaryHandle getJsonBinaryHandle() {
		return _JsonBinaryHandle;
	}

	/**
	 * 设置JSON处理二进制的方法
	 * 
	 * @param binaryHandle
	 */
	public void setJsonBinaryHandle(IBinaryHandle binaryHandle) {
		this._JsonBinaryHandle = binaryHandle;
	}

	/**
	 * 获取 cached 表数据
	 * 
	 * @param sql
	 * @param intLifeSeconds 最长生命时间
	 * @param dataSourceName
	 * @param rv
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static DTTable getCachedTable(String sql, int intLifeSeconds, String dataSourceName, RequestValue rv)
			throws IOException, ClassNotFoundException {
		String cachedKey = rv.replaceParameters(sql).toUpperCase();
		SqlCached ins = SqlCached.getInstance();
		SqlCachedValue sc = ins.getBinary(cachedKey);

		if (sc == null || sc.checkOvertime(intLifeSeconds)) {
			DTTable tb = getJdbcTable(sql, dataSourceName, rv);
			if (tb.isOk()) {
				try {
					ins.add(cachedKey, tb.toSerialize());
				} catch (Exception err) {
					System.err.println(err.getMessage());
				}
			}
			return tb;
		} else {
			// System.out.println("序列化表");
			DTTable tb = DTTable.fromSerialize(sc.getBinary());

			return tb;
		}
	}

	/**
	 * 获取 cached 表数据
	 * 
	 * @param sql
	 * @param intLifeSeconds 最长生命时间
	 * @param cnn            数据库连接
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static DTTable getCachedTable(String sql, int intLifeSeconds, DataConnection cnn)
			throws IOException, ClassNotFoundException {
		String cachedKey = replaceParameters(sql, cnn.getRequestValue()).toUpperCase();
		SqlCached ins = SqlCached.getInstance();
		SqlCachedValue sc = ins.getBinary(cachedKey);
		DTTable tb;
		if (sc != null && !sc.checkOvertime(intLifeSeconds)) {
			tb = DTTable.fromSerialize(sc.getBinary());
			return tb;
		}

		tb = getJdbcTable(sql, cnn);
		if (tb.isOk()) {
			try {
				ins.add(cachedKey, tb.toSerialize());
			} catch (Exception err) {
				LOGGER.error(err.getMessage());
			}
		}

		return tb;
	}

	/**
	 * 替换原始字符串中的@参数
	 * 
	 * @param source
	 * @param rv
	 * @return
	 */
	public static String replaceParameters(String source, RequestValue rv) {
		if (source == null || rv == null)
			return source;
		MListStr a = Utils.getParameters(source, "@");
		MStr sb = new MStr(source);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val = rv.s(name);
			val = val == null ? "[NULL]" : val;

			String find = "@" + name;
			sb.replace(find, val);
		}
		return sb.toString();
	}

	/**
	 * 获取 JDBC 表，自动关闭连接,使用默认的数据库连接
	 * 
	 * @param sql
	 * @return
	 */
	public static DTTable getJdbcTable(String sql) {
		return getJdbcTable(sql, "", null);
	}

	/**
	 * 获取 JDBC 表，自动关闭连接
	 * 
	 * @param sql
	 * @param dataSourceName
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, String dataSourceName) {
		return getJdbcTable(sql, dataSourceName, null);
	}

	/**
	 * 获取 JDBC 表，自动关闭连接
	 * 
	 * @param sql
	 * @param dataSourceName 指定连接源
	 * @param rv
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, String dataSourceName, RequestValue rv) {
		DataConnection conn = new DataConnection();
		conn.setConfigName(dataSourceName);
		if (rv != null) {
			conn.setRequestValue(rv);
		}
		DTTable tb;
		try {
			tb = getJdbcTable(sql, conn);
		} catch (Exception err) {
			tb = new DTTable();
			tb.setOk(false);
			tb.setErrorInfo(err.getMessage());

			LOGGER.error(err.getMessage());
		} finally {
			conn.close();
		}
		return tb;
	}

	/**
	 * 获取 JDBC 表，自动关闭连接
	 * 
	 * @param sql
	 * @param rv
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, RequestValue rv) {
		return getJdbcTable(sql, "", rv);
	}

	/**
	 * 获取 JDBC 表，注意没有关闭数据库连接，请手动关闭<br>
	 * 获取数据后，移除该查询的数据集
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, DataConnection conn) {
		boolean rst = conn.executeQuery(sql);
		DTTable tb = new DTTable();
		if (rst) {
			tb.initData(conn.getLastResult().getResultSet());
			try {
				conn.getLastResult().getResultSet().close();
			} catch (Exception eee) {
				LOGGER.warn("Close result. {}", eee.getMessage());
			}
			conn.getResultSetList().removeAt(conn.getResultSetList().size() - 1);
		} else {
			tb.setOk(false);
			// 不返回sql
			tb.setErrorInfo(conn.getErrorMsgOnly());

			LOGGER.error(conn.getErrorMsg());
		}

		return tb;
	}

	/**
	 * 获取 JDBC 分页表，自动关闭连接
	 * 
	 * @param sql            执行查询的语句
	 * @param pkFiled        主键
	 * @param pageSize       每页的记录数
	 * @param curPage        当前页
	 * @param dataSourceName 数据源
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, String pkFiled, int pageSize, int curPage, String dataSourceName) {
		return getJdbcTable(sql, pkFiled, pageSize, curPage, dataSourceName, null);
	}

	/**
	 * 获取 JDBC 分页表，自动关闭连接
	 * 
	 * @param sql            执行查询的语句
	 * @param pkFiled        主键
	 * @param pageSize       每页的记录数
	 * @param curPage        当前页
	 * @param dataSourceName 数据源
	 * @param rv             RequestValue
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, String pkFiled, int pageSize, int curPage, String dataSourceName,
			RequestValue rv) {
		DataConnection conn = new DataConnection();
		conn.setConfigName(dataSourceName);
		if (rv != null) {
			conn.setRequestValue(rv);
		}
		DTTable tb = getJdbcTable(sql, pkFiled, pageSize, curPage, conn);
		conn.close();
		return tb;
	}

	/**
	 * 获取 JDBC 表，注意没有关闭数据库连接，请手动关闭
	 * 
	 * @param sql      执行查询的语句
	 * @param pkFiled  主键
	 * @param pageSize 每页的记录数
	 * @param curPage  当前页
	 * @param conn     数据库连接，请手动关闭
	 * @return
	 */
	public static DTTable getJdbcTable(String sql, String pkFiled, int pageSize, int curPage, DataConnection conn) {
		boolean rst = conn.executeQueryPage(sql, pkFiled, curPage, pageSize);
		if (rst) {
			DTTable tb = new DTTable();
			tb.initData(conn.getLastResult().getResultSet());
			return tb;
		} else {
			return null;
		}
	}

	/**
	 * 从序列化二进制中获取表
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static DTTable fromSerialize(byte[] buf) throws IOException, ClassNotFoundException {
		// Serialize

		ByteArrayInputStream fis = new ByteArrayInputStream(buf);
		ObjectInputStream ois = new ObjectInputStream(fis);
		DTTable tb = (DTTable) ois.readObject();
		ois.close();
		fis.close();
		return tb;
	}

	public DTTable() {
		this._Columns = new DTColumns();
		this._Rows = new DTRows();
		this._Rows.setTable(this);
		this._IsOk = true;

	}

	/**
	 * 映射表数据到 根据 objectClass 的类<br>
	 * Map the table rows to the target class list
	 * 
	 * @param objectClass the target class
	 * @return the target class list
	 * @throws Exception the exception
	 */
	public List<?> toClasses(Class<?> objectClass) throws Exception {
		List<Object> al = new ArrayList<Object>();
		for (int i = 0; i < this.getCount(); i++) {
			Object obj = objectClass.newInstance();
			this.getRow(i).parseToClass(obj);
			al.add(obj);
		}
		return al;
	}

	/**
	 * 序列化表<br>
	 * Serialize the table
	 * 
	 * @return the serialized binary
	 * @throws IOException the exception
	 */
	public byte[] toSerialize() throws IOException {
		// Serialize
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this);
		oos.close();

		byte[] buf = fos.toByteArray();
		fos.close();

		return buf;
	}

	/**
	 * 根据指定的字段初始化字段的XML属性，是否从XML的属性中获取<br>
	 * Initialize the table columns XML "isAttribute" according to the specified, Whether to get the value from the XML
	 * element attribute fields
	 * 
	 * @param fields      the fields name
	 * @param isAttribute 是否从XML的属性中获取 <br>
	 *                    Whether to get the value from the XML element attribute
	 */
	public void initXmlColumnsByFields(String[] fields, boolean isAttribute) {
		for (int i = 0; i < fields.length; i++) {
			DTColumn col = new DTColumn();
			col.setName(fields[i]);
			col.setTypeName("String");
			col.setIsXmlAttribute(isAttribute);

			this._Columns.addColumn(col);
		}
		this.initIndexes();
	}

	/**
	 * 根据XML Document 初始化数据<br>
	 * Initialize the table data from the XML document
	 * 
	 * @param doc        the XML document
	 * @param xmlTagName the tag name
	 */
	public void initData(Document doc, String xmlTagName) {
		this._Document = doc;
		this._DataList = UXml.retNodeListByPath(this._Document, xmlTagName);
		this._XmlDataTag = xmlTagName;

		if (this._Columns.getCount() == 0) {
			initColumnsByXml();
		}
		Node row0 = null;
		if (this._DataList != null && this._DataList.getLength() > 0) {
			for (int m = 0; m < this._DataList.getLength(); m++) {
				Node row = this._DataList.item(m);
				if (m == 0) {
					row0 = row;
					this._XmlDataNodeName = row.getNodeName();
					this._XmlNodeDataParent = row.getParentNode();
				}
				if (row.getNodeType() == Node.ELEMENT_NODE && row.getParentNode().equals(row0.getParentNode())) {
					initRowDataByXml(row);
				}

			}
			// int mmm = 0;
			// mmm++;
		} else {
			String[] tags = xmlTagName.split("/"); // 多级tagName
			String s = "";
			Node n1 = this._Document;

			for (int i = 0; i < tags.length - 1; i++) {
				if (s.length() > 0) {
					s += "/";
				}
				s += tags[i];
				Node n;
				if (i == 0) {
					n = this._Document.getFirstChild();
				} else {
					NodeList nl = UXml.retNodeListByPath(this._Document, s);
					if (nl == null) {
						n = null;
					} else {
						n = nl.item(0);
					}
				}
				if (n == null) {
					Element e = n1.getOwnerDocument().createElement(tags[i]);
					n1.appendChild(e);
					n1 = e;
				} else {
					n1 = n;
				}
			}
			this._XmlNodeDataParent = n1;
			this._XmlDataNodeName = tags[tags.length - 1];
		}
	}

	/**
	 * 根据Xml字符串 初始化数据<br>
	 * Initialize the table data from the XML string
	 * 
	 * @param xml        the XML string
	 * @param xmlTagName the XML tag name
	 */
	public void initData(String xml, String xmlTagName) {
		xml = UXml.filterInvalidXMLcharacter(xml);
		this._Document = UXml.asDocument(xml);
		this.initData(this._Document, xmlTagName);
	}

	/**
	 * 通过节点的子节点的textcontent获取数据<br>
	 * Initialize the table data from specified text node of the XML document
	 * 
	 * @param doc     the XML document
	 * @param dataTag the text node tag name
	 */
	public void initDataByXmlChildNodes(Document doc, String dataTag) {
		this._IsXmlChildNode = true;
		this.initData(doc, dataTag);
	}

	/**
	 * 通过节点的子节点的textcontent获取数据<br>
	 * Initialize the table data from specified text node of the XML document
	 * 
	 * @param xml     the XML string
	 * @param dataTag the XML tag name
	 */
	public void initDataByXmlChildNodes(String xml, String dataTag) {
		xml = UXml.filterInvalidXMLcharacter(xml);
		this._IsXmlChildNode = true;
		this._Document = UXml.asDocument(xml);
		this.initData(this._Document, dataTag);
	}

	/**
	 * 根据Xml Node 初始化行数据
	 * 
	 * @param nodeRow
	 */
	private void initRowDataByXml(Node nodeRow) {
		DTRow row = this.addRow();
		if (this._IsXmlChildNode) {
			for (int i = 0; i < nodeRow.getChildNodes().getLength(); i++) {
				Node node = nodeRow.getChildNodes().item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				try {

					DTCell cell = row.getCell(node.getNodeName());
					cell.setValue(node.getTextContent());
					// System.out.println(cell.getValue());
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		} else {
			for (int i = 0; i < this._Columns.getCount(); i++) {
				String val = null;
				String name = _Columns.getColumn(i).getName();
				if (name.equalsIgnoreCase("INNERTEXT")) {
					val = nodeRow.getTextContent();
					if (val != null) {
						val = val.trim();
					}
				} else {
					val = UXml.retNodeValue(nodeRow, name);
				}
				DTCell cell = row.getCell(i);
				cell.setValue(val);
			}
		}
		row.setNodeRow(nodeRow);
	}

	/**
	 * 连接两个表，将需要连接的表数据放到本表中类似数据库的左联<br>
	 * {"TYPE": "JOIN", "FROM_KEYS": "BAS_TAG", "TO_KEYS": "VIS_STATUS"}
	 * 
	 * @param fromTable 需要连接的表
	 * @param fromKeys  需要连接的表的主键
	 * @param toKeys    当前表的主键
	 * @throws Throwable the exception
	 */
	public void join(DTTable fromTable, String[] fromKeys, String[] toKeys) throws Throwable {
		fromTable.getColumns().setKeys(fromKeys);
		fromTable.rebuildIndex();

		HashMap<DTColumn, DTColumn> map = new HashMap<DTColumn, DTColumn>();

		int formIdx = this._Columns.getCount();

		// 当前表添加来源表的字段
		DTColumn[] searchCols = new DTColumn[fromKeys.length];
		int idx = 0;
		for (int i = 0; i < fromTable.getColumns().getCount(); i++) {
			DTColumn col = fromTable.getColumns().getColumn(i);
			if (col.isKey()) {
				searchCols[idx] = col;
				idx++;
			}
			DTColumn newCol = new DTColumn();

			// 获取可以使用的字段名称
			String fieldName = this._Columns.getCanUsedName(col.getName());

			newCol.setName(fieldName);
			newCol.setDescription(col.getDescription());
			newCol.setTypeName(col.getTypeName());

			this._Columns.addColumn(newCol);
			map.put(col, newCol);
		}

		// 获取当前表查询用的字段的索引号
		int[] cols = new int[toKeys.length];
		for (int i = 0; i < cols.length; i++) {
			String colName = toKeys[i].toUpperCase().trim();
			DTColumn col = this.getColumns().getColumn(colName);
			cols[i] = col.getIndex();
		}

		for (int k = 0; k < this.getCount(); k++) {
			DTRow r1 = this.getRow(k); // 当前表行
			Object[] vals = new Object[cols.length];

			// 获取当前表的行的查询用值
			for (int ia = 0; ia < cols.length; ia++) {
				vals[ia] = r1.getCell(cols[ia]).getValue();
			}

			// 获取来源表的行
			DTRow formR1 = fromTable.getRowByKeys(searchCols, vals);
			for (int ia = formIdx; ia < this._Columns.getCount(); ia++) {
				DTCell cell = new DTCell();
				DTColumn col = this._Columns.getColumn(ia);
				cell.setColumn(col);
				if (formR1 != null) {
					Object val = formR1.getCell(col.getIndex() - formIdx).getValue();
					cell.setValue(val);
				}
				r1.addData(cell);
			}
		}
	}

	/**
	 * 合并垂直表
	 * 
	 * @param fromTable  来源的纵向表
	 * @param fromKeys   来源和当前表关联的字段
	 * @param toKeys     当前表和来源表关联的字段
	 * @param fields     需要附加的字段
	 * @param namedField 来源表标记为字段名称的字段 *
	 * @param valueField 取值用的字段
	 * @throws Throwable the exception
	 */
	public void joinHor(DTTable fromTable, String[] fromKeys, String[] toKeys, String[] fields, String namedField,
			String valueField) throws Throwable {

		String[] indexKeys = new String[fromKeys.length + 1];

		for (int i = 0; i < fromKeys.length; i++) {
			indexKeys[i] = fromKeys[i];
		}

		indexKeys[indexKeys.length - 1] = namedField;

		// 来源表生成索引
		fromTable.getColumns().setKeys(indexKeys);
		fromTable.rebuildIndex();

		// 标记为名称的字段
		DTColumn namedCol = fromTable.getColumns().getColumn(namedField);
		// 来源主键的字段，用于查询
		DTColumn[] searchCols = new DTColumn[fromKeys.length + 1];
		// 查询的最后一条是指标记名称字段
		searchCols[searchCols.length - 1] = namedCol;
		for (int i = 0; i < fromKeys.length; i++) {
			DTColumn col = fromTable.getColumns().getColumn(fromKeys[i]);
			searchCols[i] = col;
		}

		// 保存新增字段和附加名称的 MAP数据
		HashMap<String, DTColumn> map = new HashMap<String, DTColumn>();
		// 添加新字段
		for (int i = 0; i < fields.length; i++) {
			String name = fields[i].trim();
			DTColumn newCol = new DTColumn();

			// 获取可以使用的字段名称
			String fieldName = this._Columns.getCanUsedName(name);

			newCol.setName(fieldName);
			newCol.setDescription(name);
			this._Columns.addColumn(newCol);
			map.put(name, newCol);
		}

		// 获取当前表查询用的字段的索引号
		int[] cols = new int[toKeys.length];
		for (int i = 0; i < cols.length; i++) {
			String colName = toKeys[i].toUpperCase().trim();
			DTColumn col = this.getColumns().getColumn(colName);
			cols[i] = col.getIndex();
		}

		// 根据当前数据，扫描每一行数据，在来源表中查询所需数据
		for (int k = 0; k < this.getCount(); k++) {
			DTRow r1 = this.getRow(k); // 当前表行
			Object[] vals = new Object[cols.length + 1];

			// 获取当前表的行的查询用值
			for (int ia = 0; ia < cols.length; ia++) {
				vals[ia] = r1.getCell(cols[ia]).getValue();
			}

			for (int zz = 0; zz < fields.length; zz++) {
				String name = fields[zz].trim();
				// 对应的字段名称
				vals[vals.length - 1] = name;

				// 获取来源表的行
				DTRow formR1 = fromTable.getRowByKeys(searchCols, vals);

				DTCell cell = new DTCell();
				DTColumn col = map.get(name);
				cell.setColumn(col);
				if (formR1 != null) {
					// 从行中取值
					Object val = formR1.getCell(valueField).getValue();
					cell.setValue(val);
				}
				r1.addData(cell);
			}
		}
	}

	/**
	 * 新增一行数据
	 */
	public DTRow addRow() {
		DTRow row = new DTRow();
		row.setTable(this);
		for (int i = 0; i < this._Columns.getCount(); i++) {
			DTCell cell = new DTCell();
			cell.setColumn(this._Columns.getColumn(i));
			row.addData(cell);
		}
		this._Rows.addRow(row);

		return row;
	}

	/**
	 * 返回JSON对象 含有图片的最多返回50条记录，其它最多1000条数据
	 * 
	 * @return the JSON array
	 * @throws JSONException the exception
	 */
	public JSONArray toJSONArray() throws JSONException {
		JSONArray json = new JSONArray();
		String contentpath = "";
		for (int m = 0; m < getCount(); m++) {
			DTRow r = getRow(m);
			JSONObject obj = new JSONObject();
			for (int i = 0; i < getColumns().getCount(); i++) {
				String name = getColumns().getColumn(i).getName();

				DTCell cell = r.getCell(i);
				Object v = getCellValueByJson(cell, contentpath);

				obj.put(name, v);

			}
			json.put(obj);

			if (this._IshaveImage && m > 50) { // 含有图片的最多返回50条记录
				break;
			}
			if (m > 50000) { // 最多1000条数据
				break;
			}
		}
		return json;
	}

	/**
	 * 创建JSON时，获取Cell值的方法
	 * 
	 * @param cell        the table cell
	 * @param contentpath the binary value save to
	 * @return the value
	 */
	public Object getCellValueByJson(DTCell cell, String contentpath) {
		Object val = cell.getValue();
		if (this.getJsonBinaryHandle() == null) {
			return val;
		}
		if (cell.getValue() != null) {
			String objClassName = cell.getValue().getClass().toString();
			if (objClassName.indexOf("[B") >= 0) { // 图片处理
				byte[] buf = (byte[]) cell.getValue();
				String v = this.getJsonBinaryHandle().handle(buf, contentpath);
				// 含有图片
				this._IshaveImage = true;
				return v;
			} else if (this.getTimeDiffMinutes() != 0 && (objClassName.toUpperCase().indexOf("TIME") >= 0
					|| objClassName.toUpperCase().indexOf("DATE") >= 0)) {
				// 返回计算时差后的时间
				return Utils.getTimeDiffValue(val, this.getTimeDiffMinutes());
			} else if (objClassName.toUpperCase().indexOf("CLOB") >= 0) {
				return cell.toString();
			} else if (objClassName.equals("class java.lang.Long")) {
				// 避免js的 number无法表达long值，会失去精度
				return cell.toString();
			}
		}
		return val;
	}

	/**
	 * 返回JSON对象，二进制数据转换成文件<br>
	 * 特征码：##BINARY_FILE["+objBinFile.toString()+"]BINARY_FILE##
	 * 
	 * @return the JSON array
	 * @throws JSONException the exception
	 */
	public JSONArray toJSONArrayBinaryToFile(String http) throws JSONException {
		JSONArray json = new JSONArray();
		String path = UPath.getPATH_IMG_CACHE() + "/sync_binary/";// 本地物理路径
		String url = UPath.getPATH_IMG_CACHE_URL() + "/sync_binary/";
		if (http != null && http.trim().length() > 0) {
			if (http.toLowerCase().indexOf("http://") != 0 && http.toLowerCase().indexOf("https://") != 0) {
				throw new JSONException("参数:http前缀应该以http://或https://开始，" + http);
			}
			if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
				throw new JSONException("参数:UPath.getPATH_IMG_CACHE_URL()已经以http://或https://开始，" + url);
			}
			url = http.trim() + "/" + url;
		}
		for (int m = 0; m < getCount(); m++) {
			DTRow r = getRow(m);
			JSONObject obj = new JSONObject();
			json.put(obj);
			for (int i = 0; i < getColumns().getCount(); i++) {
				String name = getColumns().getColumn(i).getName();
				DTCell cell = r.getCell(i);
				Object val = cell.getValue();
				if (val != null) {
					String objClassName = cell.getValue().getClass().toString();
					if (objClassName.indexOf("[B") >= 0) { // 二进制数据转换成文件
						byte[] buf = (byte[]) val;
						String v1 = this.getJsonBinaryHandle().handle(buf, path, url);
						obj.put(name, v1);
						continue;
					}
				}
				obj.put(name, cell.getValue());
			}
		}
		return json;
	}

	/**
	 * 返回JSON数据
	 * 
	 * 含有图片的最多返回50条记录，其它最多1000条数据
	 * 
	 * @param rv 现在没啥用了
	 * @return JSONObject
	 */
	public String toJson(RequestValue rv) {
		MStr sb = new MStr();
		sb.append("[\r\n");
		boolean ishaveImage = false;
		String contentpath = rv != null ? rv.getContextPath() : "";

		int field_name_case = 0; // 原始
		if (rv != null) {
			if (rv.s("EWA_JSON_FIELD_CASE") != null) {
				if (rv.s("EWA_JSON_FIELD_CASE").equalsIgnoreCase("upper")) {
					field_name_case = 1; // 大写字段
				} else if (rv.s("EWA_JSON_FIELD_CASE").equalsIgnoreCase("lower")) {
					field_name_case = 2; // 小写字段
				}
			}
		}
		for (int m = 0; m < getCount(); m++) {
			DTRow r = getRow(m);
			if (m > 0) {
				sb.append(",\r\n");
			}
			sb.append("{");
			for (int i = 0; i < getColumns().getCount(); i++) {
				String name = getColumns().getColumn(i).getName();
				DTCell cell = r.getCell(i);
				Object v = this.getCellValueByJson(cell, contentpath);
				if (i > 0) {
					sb.append(", ");
				}
				if (field_name_case == 1) {
					name = name.toUpperCase();
				} else if (field_name_case == 2) {
					name = name.toLowerCase();
				}
				sb.append("\"" + Utils.textToJscript(name) + "\"");
				sb.append(": ");
				sb.append(v == null ? "null" : "\"" + Utils.textToJscript(v.toString()) + "\"");
			}
			sb.append("}");
			if (ishaveImage && m > 50) { // 含有图片的最多返回50条记录
				break;
			}
			if (m > 50000) { // 最多1000条数据
				break;
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private String createFieldData(Object o, DTColumn col) {
		String v1;
		if (o == null) {
			return "";
		}
		String name = col.getName();
		String type = col.getTypeName().toUpperCase();
		if (type.indexOf("BIN") >= 0 || type.indexOf("IMAGE") >= 0) {
			v1 = Utils.bytes2hex((byte[]) o);
		} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
			v1 = Utils.getDateXmlString(o);
			String s2 = this.createNodeAtt(name, v1);
			java.util.Date t = (java.util.Date) o;
			s2 += " " + this.createNodeAtt(name + "_TS", t.getTime() + "");
			return s2;
		} else {
			v1 = o.toString();
		}
		return this.createNodeAtt(name, v1);
	}

	private String createNodeAtt(String attName, String attValue) {
		if (attName == null || attName.trim().length() == 0 || attValue == null) {
			return "";
		}
		String s = attName.trim() + "=\"" + UXml.createXmlValue(attValue) + "\" ";
		return s;
	}

	/**
	 * Convert to the XML String
	 * 
	 * @param rv the RequestValue
	 * @return the XML String
	 */
	public String toXml(RequestValue rv) {
		MStr s = new MStr();
		s.al("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		s.al("<data name=\"" + UXml.createXmlValue(this._Name) + "\">");
		for (int i = 0; i < this.getCount(); i++) {
			s.a("<r ");
			DTRow r = this.getRow(i);
			for (int m = 0; m < this.getColumns().getCount(); m++) {
				Object o = r.getCell(m).getValue();
				String val = this.createFieldData(o, this.getColumns().getColumn(m));
				s.a(val);
			}
			s.al(" />");
		}
		s.al("</data>");

		return s.toString();
	}

	public DTRow addXmlNewRow() {
		DTRow row = this.addRow();
		// 如果是XML数据
		if (this._XmlNodeDataParent != null && this._XmlDataNodeName != null) {
			Element e = this._Document.createElement(this._XmlDataNodeName);
			this._XmlNodeDataParent.appendChild(e);
			row.setNodeRow(e);
		}
		return row;
	}

	/**
	 * 根据Xml初始化字段信息<br>
	 * Initialize the table columns by XML node
	 */
	private void initColumnsByXml() {
		if (this._IsXmlChildNode) {
			HashMap<String, DTColumn> cols = new HashMap<String, DTColumn>();
			for (int i = 0; i < this._DataList.getLength(); i++) {
				Node node = this._DataList.item(i);
				for (int m = 0; m < node.getChildNodes().getLength(); m++) {
					Node cNode = node.getChildNodes().item(m);
					if (cNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					String tagName = cNode.getNodeName();
					DTColumn col = null;
					if (cols.containsKey(tagName)) {
						col = cols.get(tagName);
						if (col.isXmlCData()) {
							continue;
						}
					} else {
						col = new DTColumn();
						col.setIsXmlAttribute(false);
						col.setTypeName("String");
						col.setName(tagName);

						cols.put(tagName, col);
						this._Columns.addColumn(col);
					}
					for (int k = 0; k < cNode.getChildNodes().getLength(); k++) {
						if (cNode.getChildNodes().item(k).getNodeType() == Node.CDATA_SECTION_NODE) {
							col.setIsXmlCData(true);
							break;
						}
					}
				}
			}
		} else {
			if (this._DataList.getLength() == 0) {
				return;
			}

			Node node = this._DataList.item(0);
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				DTColumn col = new DTColumn();
				col.setIsXmlAttribute(true);
				String attName = node.getAttributes().item(i).getNodeName();
				col.setName(attName);
				col.setTypeName("String");
				this._Columns.addColumn(col);
			}
		}

		this.initIndexes();
	}

	/**
	 * 根据ResultSet初始化数据<br>
	 * Initialize the table data from the JDBC ResultSet
	 * 
	 * @param rs the JDBC ResultSet
	 */
	public void initData(ResultSet rs) {
		this.initData(rs, null);
	}

	/**
	 * 根据ResultSet初始化数据 Initialize the table data from the JDBC ResultSet
	 * 
	 * @param rs   the JDBC ResultSet
	 * @param keys 主键表达式 the data keys
	 */
	public void initData(ResultSet rs, String[] keys) {
		this.initColumns(rs);
		if (keys != null && keys.length > 0) {
			this._Columns.setKeys(keys);
		}
		if (!_IsOk) {
			return;
		}
		try {
			while (rs.next()) {
				this.initDataRowByRs(rs);
			}

		} catch (SQLException e) {
			this._IsOk = false;
			this._ErrorInfo = e.getMessage();

			return;
		}
		List<DTColumn> jsonCols = new ArrayList<DTColumn>();
		for (int i = 0; i < this._Columns.getCount(); i++) {
			if (this._Columns.getColumn(i).isJson()) {
				jsonCols.add(this._Columns.getColumn(i));
			}
		}
		if (jsonCols.size() > 0) {
			this.handleJsonData(jsonCols);
		}
	}

	/**
	 * 处理json对象，将json变成字段
	 * 
	 * @param jsonCols
	 */
	private void handleJsonData(List<DTColumn> jsonCols) {
		for (int i = 0; i < this._Rows.getCount(); i++) {
			DTRow r = this._Rows.getRow(i);
			for (int m = 0; m < jsonCols.size(); m++) {
				DTColumn col_json = jsonCols.get(m);
				String json_str = r.getCell(col_json.getIndex()).toString();
				if (json_str == null || json_str.trim().length() == 0) {
					continue;
				}

				this.handleJsonData1(json_str, r);
			}
		}
	}

	private void handleJsonData1(String json_str, DTRow r) {
		JSONObject json_o = null;
		try {
			json_o = new JSONObject(json_str);
		} catch (Exception err) {
			LOOGER.error(err.getMessage());
			return;
		}
		Iterator<?> it = json_o.keys();
		try {
			while (it.hasNext()) {
				String name = it.next().toString();
				Object val = json_o.get(name);

				DTColumn col;
				if (!this._Columns.testName(name)) {
					col = new DTColumn();
					col.setName(name);
					col.setTypeName("varchar");
					col.setDescription("json-extract");
					this._Columns.addColumn(col);
				} else {
					col = this._Columns.getColumn(name);
					if (col.getDescription() == null || !col.getDescription().equals("json-extract")) {
						// 该字段在表中已经存在，不能进行抽取字段
						continue;
					}
				}
				DTCell cell = new DTCell();
				cell.setColumn(col);

				if (val != org.json.JSONObject.NULL) {
					cell.setValue(val);
				}
				r.addData(cell);
			}
		} catch (Exception err) {

		}
	}

	/**
	 * 根据keys值获取数据行<br>
	 * Returns the row form the keys
	 * 
	 * @param cols keys的字段
	 * @param vals keys值
	 * @return the row
	 */
	public DTRow getRowByKeys(DTColumn[] cols, Object vals[]) {
		MStr keysExp = new MStr();
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) {
				keysExp.append(";");
			}
			DTColumn col = cols[i];
			keysExp.append(col.getName().toUpperCase().trim());
			keysExp.append("=");
			keysExp.append(vals[i]);
		}
		int code = keysExp.toString().hashCode();
		return this._Rows.getRowByKeys(code);
	}

	/**
	 * 根据keys值获取数据行<br>
	 * Returns the row form the keys
	 * 
	 * @param colsNames keys的字段名称
	 * @param vals      keys值
	 * @return the row
	 * @throws Exception the Exception
	 */
	public DTRow getRowByKeys(String[] colsNames, Object vals[]) throws Exception {
		DTColumn[] cols = new DTColumn[colsNames.length];
		for (int i = 0; i < colsNames.length; i++) {
			String name = colsNames[i].trim();
			DTColumn col = this._Columns.getColumn(name);
			cols[i] = col;
		}
		return this.getRowByKeys(cols, vals);
	}

	/**
	 * 重建索引<br>
	 * Rebuild the table index
	 */
	public void rebuildIndex() {
		ArrayList<DTColumn> colsIdx = new ArrayList<DTColumn>();
		for (int i = 0; i < this._Columns.getCount(); i++) {
			DTColumn col = this._Columns.getColumn(i);
			if (col.isKey()) {
				colsIdx.add(col);
			}
		}
		for (int i = 0; i < this.getCount(); i++) {
			DTRow row = this.getRow(i);
			MStr keysExp = new MStr();
			for (int k = 0; k < colsIdx.size(); k++) {
				if (keysExp.length() > 0) {
					keysExp.append(";");
				}
				DTColumn col = colsIdx.get(k);
				DTCell cell = row.getCell(col.getIndex());

				keysExp.append(col.getName().toUpperCase().trim());
				keysExp.append("=");
				keysExp.append(cell.getValue());
			}

			if (keysExp.length() > 0) {
				row.setKeysExp(keysExp.toString().hashCode());
				Integer exp = row.getKeysExp();
				if (!this._Rows.getKeysIndexes().containsKey(exp)) {
					this._Rows.getKeysIndexes().add(exp, row.getIndex());
				}
			}
		}

	}

	/**
	 * 根据key值获取数据行<br>
	 * Returns the row form the key
	 * 
	 * @param col the key column
	 * @param val the key value
	 * @return the row
	 */
	public DTRow getRowByKey(DTColumn col, Object val) {
		DTColumn[] cols = new DTColumn[1];
		cols[0] = col;
		Object[] vals = new Object[1];
		vals[0] = val;
		return this.getRowByKeys(cols, vals);
	}

	/**
	 * 根据key值获取数据行<br>
	 * Returns the row form the key
	 * 
	 * @param colName 字段名称<br>
	 *                the key column name
	 * @param val     字段值<br>
	 *                the key value
	 * @return the row
	 */
	public DTRow getRowByKey(String colName, Object val) {
		DTColumn col = null;
		try {
			col = this._Columns.getColumn(colName);
			return this.getRowByKey(col, val);
		} catch (Exception e) {
			return null;
		}
	}

	private void initDataRowByRs(ResultSet rs) {
		DTRow row = new DTRow();
		row.setTable(this);
		MStr keysExp = new MStr();
		for (int i = 0; i < this._Columns.getCount(); i++) {
			DTCell cell = new DTCell();
			DTColumn col = this._Columns.getColumn(i);
			cell.setColumn(col);
			String colTypeName = col.getTypeName().toUpperCase();

			try {
				if (colTypeName.indexOf("TIMESTAMP") >= 0 || colTypeName.indexOf("DATE") >= 0) {
					java.sql.Timestamp t = rs.getTimestamp(i + 1);
					cell.setValue(t);
				} else {
					cell.setValue(rs.getObject(i + 1));
				}
			} catch (SQLException e) {
				cell.setValue(e.getMessage());
			}
			if (col.isKey()) {
				if (keysExp.length() > 0) {
					keysExp.append(";");
				}
				keysExp.append(col.getName().toUpperCase().trim());
				keysExp.append("=");
				keysExp.append(cell.getValue());
			}
			row.addData(cell);

		}
		// keys表达式，要放置到this._Rows.addRow之前
		if (keysExp.length() > 0) {
			row.setKeysExp(keysExp.toString().hashCode());
		}
		// 添加数据行，同时根据keys表达式建立索引
		this._Rows.addRow(row);
	}

	/**
	 * 根据HashMap类初始化数据<br>
	 * Initialize the data from the Map data
	 * 
	 * @param mapData the Map data
	 */
	public void initData(Map<?, ?> mapData) {
		Iterator<?> it = mapData.keySet().iterator();
		int m = 0;
		UObjectValue uo = new UObjectValue();
		try {
			while (it.hasNext()) {
				Object key = it.next();
				Object data = mapData.get(key);
				uo.setObject(data);
				if (m == 0) {
					this.initColumnsByClass(uo);
					DTColumn col = new DTColumn();
					col.setName("KEY");
					col.setTypeName("String");
					this._Columns.addColumn(col);
				}

				this.initDataRowByClass(uo);
				DTRow row = this._Rows.getRow(m);
				DTCell cell = new DTCell();
				cell.setColumn(this.getColumns().getColumn(this.getColumns().getCount() - 1));
				row.addData(cell);
				cell.setValue(key);

				m++;
			}
		} catch (Exception e) {
			this._IsOk = false;
			this._ErrorInfo = e.getMessage();
		}
	}

	/**
	 * 根据List类初始化数据 Initialize the data from the List data
	 * 
	 * @param listData the List data
	 */
	public void initData(List<?> listData) {
		UObjectValue uo = new UObjectValue();
		try {
			for (int i = 0; i < listData.size(); i++) {
				Object data = listData.get(i);
				uo.setObject(data);
				if (i == 0) {
					this.initColumnsByClass(uo);
				}
				this.initDataRowByClass(uo);
			}
		} catch (Exception e) {
			this._IsOk = false;
			this._ErrorInfo = e.getMessage();
		}
	}

	/**
	 * 通过JSONArray创建表<br>
	 * Initialize the table data from the JSON array
	 * 
	 * @param obj the JSON array
	 * @throws Exception the exception
	 */
	public void initData(JSONArray obj) throws Exception {
		for (int i = 0; i < obj.length(); i++) {
			DTRow row = new DTRow();
			row.setTable(this);
			this._Rows.addRow(row);

			JSONObject o = obj.getJSONObject(i);
			Iterator<?> it = o.keys();
			while (it.hasNext()) {
				String name = it.next().toString();
				Object val = o.get(name);
				if (!this._Columns.testName(name)) {
					DTColumn col = new DTColumn();
					col.setName(name);
					col.setTypeName("varchar");
					this._Columns.addColumn(col);
				}
				DTCell cell = new DTCell();
				cell.setColumn(this._Columns.getColumn(name));
				if (val != null) {
					cell.setValue(val);
				}
				row.addData(cell);
			}
		}

	}

	/**
	 * 根据类结构初始化行数据
	 * 
	 * @param uo
	 */
	private void initDataRowByClass(UObjectValue uo) {
		DTRow row = new DTRow();
		row.setTable(this);
		for (int i = 0; i < uo.getGetterMethods().size(); i++) {
			String val = uo.getValue(uo.getGetterMethods().get(i));
			DTCell cell = new DTCell();
			cell.setColumn(this._Columns.getColumn(i));
			row.addData(cell);
			cell.setValue(val);
		}
		this._Rows.addRow(row);
	}

	private void initColumnsByClass(UObjectValue uo) {

		try {
			ArrayList<Method> m = uo.getGetterMethods();
			for (int i = 0; i < m.size(); i++) {
				DTColumn col = new DTColumn();
				Method m0 = m.get(i);
				col.setName(m0.getName());
				col.setTypeName(m0.getReturnType().getName());
				this._Columns.addColumn(col);
			}
		} catch (Exception e) {
			this._IsOk = false;
			this._ErrorInfo = e.getMessage();
		}
		this.initIndexes();

	}

	/**
	 * 根据ResultSet初始化字段信息<br>
	 * Initialize the table columns from the JDBC result
	 * 
	 * @param rs the JDBC result
	 */
	public void initColumns(ResultSet rs) {
		boolean isMySql = false;
		try {
			ResultSetMetaData md = rs.getMetaData();
			String name1 = md.getClass().getName();
			if (name1.endsWith("com.mysql.jdbc.ResultSetMetaData")) {
				isMySql = true;
			}
			for (int i = 1; i <= md.getColumnCount(); i++) {
				DTColumn col = new DTColumn();
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

				col.setName(name);
				col.setLength(md.getColumnDisplaySize(i));
				col.setPrecision(md.getPrecision(i));
				col.setScale(md.getScale(i));
				col.setTypeName(md.getColumnTypeName(i));
				
				// 2021-07-01 
				col.setCatalogName(md.getCatalogName(i));
				col.setTableName( md.getTableName(i));
				col.setSchemaName(md.getSchemaName(i));
				
				col.setClassName(md.getColumnClassName(i));
				
				this._Columns.addColumn(col);

				// JSON字段
				if (col.getTypeName().toUpperCase().indexOf("JSON") >= 0) {
					col.setIsJson(true);
				}
			}
		} catch (SQLException e) {
			this._IsOk = false;
			this._ErrorInfo = e.getMessage();
		}
		this.initIndexes();
	}

	public DTRow getRow(int index) {
		DTRow row = this._Rows.getRow(index);
		return row;
	}

	public int getRowIndex() {
		return this._Rows.getIndex();
	}

	private void initIndexes() {
		if (this._IsBuildIndex) {
			this._Indexes.setColumns(this._Columns);
		}
	}

	/**
	 * @return the _Columns
	 */
	public DTColumns getColumns() {
		return _Columns;
	}

	/**
	 * @param columns the _Columns to set
	 */
	public void setColumns(DTColumns columns) {
		_Columns = columns;
	}

	/**
	 * @return the _Rows
	 */
	public DTRows getRows() {
		return _Rows;
	}

	/**
	 * @param rows the _Rows to set
	 */
	public void setRows(DTRows rows) {
		_Rows = rows;
		_Rows.setTable(this);
	}

	/**
	 * @return the _IsOk
	 */
	public boolean isOk() {
		return _IsOk;
	}

	/**
	 * @return the _ErrorInfo
	 */
	public String getErrorInfo() {
		return _ErrorInfo;
	}

	/**
	 * @return the _CurRow
	 */
	public DTRow getCurRow() {
		return this._Rows.getCurRow();
	}

	/**
	 * 获取数据行数
	 * 
	 * @return the table rows count
	 */
	public int getCount() {
		if (this._IsOk) {
			return this._Rows.getCount();
		} else {
			return -1;
		}
	}

	public DTCell getCell(int rowIndex, int colIndex) {
		return this._Rows.getRow(rowIndex).getCell(colIndex);
	}

	public DTCell getCell(int rowIndex, String colName) throws Exception {
		return this._Rows.getRow(rowIndex).getCell(colName);
	}

	/**
	 * @return the _XmlDataTag
	 */
	public String getXmlDataTag() {
		return _XmlDataTag;
	}

	/**
	 * @param xmlDataTag the _XmlDataTag to set
	 */
	public void setXmlDataTag(String xmlDataTag) {
		_XmlDataTag = xmlDataTag;
	}

	/**
	 * @return the _IsBuildIndex
	 */
	public boolean isBuildIndex() {
		return _IsBuildIndex;
	}

	/**
	 * @param isBuildIndex the _IsBuildIndex to set
	 */
	public void setIsBuildIndex(boolean isBuildIndex) {
		if (isBuildIndex && this._Indexes == null) {
			this._Indexes = new DTIndexes();
		}
		_IsBuildIndex = isBuildIndex;
	}

	/**
	 * @return the Indexes
	 */
	public DTIndexes getIndexes() {
		return _Indexes;
	}

	/**
	 * 获取XML数据的数据节点父节点
	 * 
	 * @return the XmlNodeDataParent
	 */
	public Node getXmlNodeDataParent() {
		return _XmlNodeDataParent;
	}

	/**
	 * 获取XML数据的节点名称
	 * 
	 * @return the XmlDataNodeName
	 */
	public String getXmlDataNodeName() {
		return _XmlDataNodeName;
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

	/**
	 * 获取行合计<br>
	 * Sum the field
	 * 
	 * @param rowIndex the field index
	 * @return the sum result
	 */
	public Double getRowSum(int rowIndex) {
		Pattern pat = Pattern.compile("\\d+\\.*\\d*", Pattern.CASE_INSENSITIVE);
		Double v = null;
		DTRow r = this._Rows.getRow(rowIndex);
		for (int i = 0; i < this._Columns.getCount(); i++) {
			if (r.getCount() <= i) {
				continue;
			}
			String sv = r.getCell(i).toString();
			if (sv == null)
				continue;
			try {
				v = (v == null ? 0.0 : v) + Double.parseDouble(sv);
			} catch (Exception e) {
				Matcher mat = pat.matcher(sv);
				double vv = 0;
				int inc = 0;
				while (mat.find()) {
					MatchResult mr = mat.toMatchResult();
					String s1 = mr.group();
					vv += Double.parseDouble(s1);
					inc++;
				}
				if (inc == 1) {
					v = (v == null ? 0.0 : v) + vv;
				}
			}
		}
		return v;
	}

	/**
	 * 获取行平均值<br>
	 * 
	 * 
	 * @param rowIndex
	 * @return
	 */
	public Double getRowAvg(int rowIndex) {
		Double v = this.getRowSum(rowIndex);
		if (v == null)
			return null;
		v = v / this._Columns.getCount();
		return v;
	}

	/**
	 * 获取行最大值
	 * 
	 * @param rowIndex
	 * @return
	 */
	public Double getRowMax(int rowIndex) {
		Double v = null;
		DTRow r = this._Rows.getRow(rowIndex);
		for (int i = 0; i < this._Columns.getCount(); i++) {
			String sv = r.getCell(i).toString();
			if (sv == null)
				continue;
			try {
				double v1 = Double.parseDouble(sv);
				if (v == null || v1 > v) {
					v = v1;
				}
			} catch (Exception e) {
			}
		}
		return v;
	}

	/**
	 * 获取行最小值
	 * 
	 * @param rowIndex
	 * @return
	 */
	public Double getRowMin(int rowIndex) {
		Double v = null;
		DTRow r = this._Rows.getRow(rowIndex);
		for (int i = 0; i < this._Columns.getCount(); i++) {
			String sv = r.getCell(i).toString();
			if (sv == null)
				continue;
			try {
				double v1 = Double.parseDouble(sv);
				if (v == null || v1 < v) {
					v = v1;
				}
			} catch (Exception e) {
			}
		}
		return v;
	}

	/**
	 * 获取列合计值
	 * 
	 * @param colIndex
	 * @return
	 */
	public Double getColSum(int colIndex) {
		Double v = null;
		Pattern pat = Pattern.compile("\\d+\\.*\\d*", Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < this.getCount(); i++) {
			if (this.getRow(i).getCount() <= colIndex) {
				continue;
			}
			String sv = this.getCell(i, colIndex).toString();
			if (sv == null)
				continue;
			try {
				v = (v == null ? 0.0 : v) + Double.parseDouble(sv);
			} catch (Exception e) {
				Matcher mat = pat.matcher(sv);
				double vv = 0;
				int inc = 0;
				while (mat.find()) {
					MatchResult mr = mat.toMatchResult();
					String s1 = mr.group();
					vv += Double.parseDouble(s1);
					inc++;
				}
				if (inc == 1) {
					v = (v == null ? 0.0 : v) + vv;
				}
			}
		}
		return v;
	}

	public Double getColCalc(int colIndex) {
		Double v = null;
		return v;
	}

	/**
	 * 获取列平均值
	 * 
	 * @param colIndex
	 * @return
	 */
	public Double getColAvg(int colIndex) {
		Double v = this.getColSum(colIndex);
		if (v == null)
			return null;
		return v / this.getCount();
	}

	/**
	 * 获取列最小值
	 * 
	 * @param colIndex
	 * @return
	 */
	public Double getColMin(int colIndex) {
		Double v = null;
		for (int i = 0; i < this.getCount(); i++) {
			String sv = this.getCell(i, colIndex).toString();
			if (sv == null)
				continue;
			try {
				double v1 = Double.parseDouble(sv);
				if (v == null || v1 < v) {
					v = v1;
				}
			} catch (Exception e) {
			}
		}
		return v;
	}

	/**
	 * 获取列最大值
	 * 
	 * @param colIndex
	 * @return
	 */
	public Double getColMax(int colIndex) {
		Double v = null;
		for (int i = 0; i < this.getCount(); i++) {
			String sv = this.getCell(i, colIndex).toString();
			if (sv == null)
				continue;
			try {
				double v1 = Double.parseDouble(sv);
				if (v == null || v1 > v) {
					v = v1;
				}
			} catch (Exception e) {
			}
		}
		return v;
	}

	/**
	 * 附加对象，可以添加任意类型对象
	 * 
	 * @return the _AttsTable
	 */
	public MTable getAttsTable() {
		if (this._AttsTable == null) {
			this._AttsTable = new MTable();
		}
		return _AttsTable;
	}

	/**
	 * 将表字段拼接为用“，”分割的字符串表达式，例如：'abc', 'cdf', 'aaa'，用于数据库查询的ID
	 * 
	 * @param fieldName
	 * @param addQuotationMarks
	 * @return
	 */
	public String joinIds(String fieldName, boolean addQuotationMarks) {
		int colIndx = this.getColumns().getNameIndex(fieldName);
		if (colIndx == -1) {
			return "FIELD NOT FOUNDED";
		}
		StringBuilder sbIds = new StringBuilder();
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		int inc = 0;
		for (int i = 0; i < this.getCount(); i++) {

			String id = this.getCell(i, colIndx).toString();
			if (id == null) {
				continue;
			}
			if (map.containsKey(id)) {
				continue;
			}
			if (inc > 0) {
				sbIds.append(", ");
			}
			inc++;

			if (addQuotationMarks) {
				sbIds.append("'");
			}
			sbIds.append(id.replace("'", "''"));
			if (addQuotationMarks) {
				sbIds.append("'");
			}
		}

		return sbIds.toString();
	}

	public void setOk(boolean isOk) {
		this._IsOk = isOk;
	}

	public void setErrorInfo(String errorInfo) {
		this._ErrorInfo = errorInfo;
	}
}
