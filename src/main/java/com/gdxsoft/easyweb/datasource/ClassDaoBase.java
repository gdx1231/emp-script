package com.gdxsoft.easyweb.datasource;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.types.UInt16;
import com.gdxsoft.easyweb.utils.types.UInt32;
import com.gdxsoft.easyweb.utils.types.UInt64;

public class ClassDaoBase<T> {
	private static Logger LOGGER = LoggerFactory.getLogger(ClassDaoBase.class);
	private String _ErrorMsg;
	private DataConnection _Conn;
	private String _ConfigName = "default";
	private RequestValue _rv;
	private Class<?> instanceClass; // 实例化的T 的 class

	private String tableName;
	private String[] fields;
	private String[] keyFields;
	private String sqlInsert;

	private String database;

	private String autoKey; // 自增主键

	/**
	 * 创建所用更新字段
	 * 
	 * @param fieldLst
	 * @return
	 */
	public Map<String, Boolean> createAllUpdateFields(String[] fieldLst) {
		HashMap<String, Boolean> updateFields = new HashMap<>();
		for (short i = 0; i < fieldLst.length; i++) {
			updateFields.put(fieldLst[i], true);
		}

		return updateFields;
	}

	/**
	 * 创建所用更新字段
	 * 
	 * @param fieldLst
	 * @return
	 */
	public Map<String, Boolean> createAllUpdateFields(String fieldLsts) {
		return this.createAllUpdateFields(fieldLsts, false);
	}

	/**
	 * 创建所用更新字段
	 * 
	 * @param fieldLsts
	 * @param lowerCase 是否改为小写
	 * @return
	 */
	public Map<String, Boolean> createAllUpdateFields(String fieldLsts, boolean lowerCase) {
		HashMap<String, Boolean> updateFields = new HashMap<>();
		if (fieldLsts == null || fieldLsts.length() == 0) {
			return updateFields;
		}
		String[] fields = fieldLsts.split(",");

		for (short i = 0; i < fields.length; i++) {
			String f = fields[i].trim();
			if (f.length() == 0) {
				continue;
			}
			if (lowerCase) {
				updateFields.put(f.toLowerCase(), true);
			} else {
				updateFields.put(f, true);
			}
		}
		return updateFields;
	}

	/**
	 * 删除多条记录
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @param rv
	 * @return
	 */
	public boolean deleteRecords(String whereString, RequestValue rv) {
		StringBuilder sql = new StringBuilder(this.createDeleteSql(false));
		sql.append("\n");
		sql.append(whereString);
		return this.executeUpdate(this.getSqlUpdate(), rv);
	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @return 记录集合
	 */
	public ArrayList<T> getRecords(String whereString) {
		return this.getRecords(whereString, null);
	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @param fields      指定返回的字段
	 * @return 记录集合
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> getRecords(String whereString, List<String> fields) {
		String sql = this.createSelectSql(tableName, whereString, fields);

		String[] arrFields;
		if (fields != null && fields.size() > 0) {
			arrFields = new String[fields.size()];
			for (int i = 0; i < fields.size(); i++) {
				String field = fields.get(i).trim();
				arrFields[i] = field;
			}
		} else {
			arrFields = this.fields;
		}
		T obj;
		try {
			obj = (T) this.instanceClass.getDeclaredConstructor().newInstance();
			return this.executeQuery(sql, obj, arrFields);
		} catch (InstantiationException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (NoSuchMethodException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (SecurityException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		}

	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @param pkFieldName 主键
	 * @param pageSize    每页记录数
	 * @param currentPage 当前页
	 * @return 记录集合
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> getRecords(String whereString, String pkFieldName, int pageSize, int currentPage) {
		String sql = this.createSelectSql(tableName, whereString, null);
		T obj;
		try {
			obj = (T) this.instanceClass.getDeclaredConstructor().newInstance();
			return this.executeQuery(sql, obj, this.fields, pkFieldName, pageSize, currentPage);
		} catch (InstantiationException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (NoSuchMethodException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		} catch (SecurityException e) {
			LOGGER.error(e.getLocalizedMessage());
			return null;
		}

	}

	/**
	 * 更新一条记录
	 * 
	 * @param para 表GRP_MAIN的映射类
	 * @return 是否成功
	 */
	public boolean updateRecord(T para) {
		RequestValue rv = this.createRequestValue(para);
		return this.executeUpdate(this.getSqlUpdate(), rv);
	}

	/**
	 * 更新一条记录
	 * 
	 * @param para         表GRP_MAIN的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean updateRecord(T para, Map<String, Boolean> updateFields) {
		// 没定义主键的话不能更新
		if (this.keyFields.length == 0) {
			return false;
		}
		String sql = this.sqlUpdateChanged(this.tableName, this.keyFields, updateFields);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		return this.executeUpdate(sql, rv);
	}

	/**
	 * 根据字段创建Rv
	 * 
	 * @param para
	 * @return
	 */
	public RequestValue createRequestValue(T para) {
		RequestValue rv = new RequestValue();
		ClassBase cb = (ClassBase) para;
		for (int i = 0; i < this.fields.length; i++) {
			String field = this.fields[i].trim();
			PageValue pv = cb.getFieldPageValue(field);
			if (pv == null) { // 404
				LOGGER.warn("The class {} field {} not found", this, field);
				continue;
			}
			rv.addValue(pv);
		}

		return rv;
	}

	/**
	 * 从 DTTable返回指定的类对象
	 * 
	 * @param tb The source table
	 * @return The target class list
	 * @throws Exception the exception
	 */
	public List<T> parseFromDTTable(DTTable tb) throws Exception {
		if (tb == null) {
			return null;
		}
		List<?> al1 = tb.toClasses(instanceClass);
		List<T> al = new ArrayList<T>();
		for (int i = 0; i < al1.size(); i++) {
			Object o = al1.get(i);
			@SuppressWarnings("unchecked")
			T o1 = (T) o;
			al.add(o1);
		}
		return al;
	}

	/**
	 * 创建 查询的 sql语句
	 * 
	 * @param tableName 表名
	 * @param where     查询条件
	 * @param fields    返回的 字段列表
	 * @return
	 */
	public String createSelectSql(String tableName, String where, List<String> fields) {
		StringBuilder sb = new StringBuilder("select ");
		if (fields != null && fields.size() > 0) {
			for (int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(", ");
				}
				String field = fields.get(i).trim();
				sb.append(field);
			}
		} else {
			sb.append(" * ");
		}
		sb.append(" from ");

		// 增加database前缀
		if (StringUtils.isNotBlank(this.database)) {
			sb.append(this.database);
			sb.append(".");
		}

		sb.append(tableName);
		if (where != null && where.trim().length() > 0) {
			sb.append(" where ");
			sb.append(where);
		}
		return sb.toString();
	}

	public String createDeleteSql() {
		return this.createDeleteSql(true);
	}

	public String createDeleteSql(boolean includePk) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		// 增加database前缀
		if (StringUtils.isNotBlank(this.database)) {
			sb.append(this.database);
			sb.append(".");
		}
		sb.append(this.tableName);
		sb.append(" WHERE ");
		if (includePk) {
			String pk = this.createPkSql();
			sb.append(pk);
		}
		return sb.toString();
	}

	public String createPkSql() {
		if (this.keyFields.length == 0) {
			return " 1>2";
		}
		StringBuilder sb = new StringBuilder(" ");
		for (int i = 0; i < this.keyFields.length; i++) {
			if (i > 0) {
				sb.append(" AND ");
			}
			String field = this.keyFields[i].trim();
			sb.append(field);
			sb.append("=@");
			sb.append(field);
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#getErrorMsg()
	 */
	public String getErrorMsg() {
		return this._ErrorMsg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeSequence(java.lang.
	 * String)
	 */
	public int executeSequence(String seqName) throws SQLException {
		ConnectToDatabase();
		String sql = "SELECT " + seqName + ".nextval from dual";
		_Conn.executeQuery(sql);
		ResultSet rs = _Conn.getLastResult().getResultSet();
		rs.next();
		int a = rs.getInt(1);
		rs.close();
		_Conn.close();
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeInt(java.lang.String)
	 */
	public int executeInt(String sql) throws SQLException {
		ConnectToDatabase();
		_Conn.executeQuery(sql);
		int a;
		ResultSet rs = _Conn.getLastResult().getResultSet();
		rs.next();
		a = rs.getInt(1);
		rs.close();
		_Conn.close();
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeDouble(java.lang.String)
	 */
	public double executeDouble(String sql) throws SQLException {
		ConnectToDatabase();
		_Conn.executeQuery(sql);
		double a;
		ResultSet rs = _Conn.getLastResult().getResultSet();
		rs.next();
		a = rs.getDouble(1);
		rs.close();
		_Conn.close();
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdxsoft.easyweb.datasource.IClassDao#getRecordCount(java.lang.String)
	 */
	public int getRecordCount(String sql) {
		ConnectToDatabase();
		int m = _Conn.getRecordCount(sql);
		_Conn.close();
		return m;
	}

	/**
	 * 创建 更新的SQL，排除主键的所有字段
	 * 
	 * @return
	 */
	public String getSqlUpdate() {
		Map<String, Boolean> pks = new HashMap<String, Boolean>();
		for (int i = 0; i < this.keyFields.length; i++) {
			String pk = this.keyFields[i].trim().toUpperCase();
			pks.put(pk, true);
		}
		Map<String, Boolean> updateFields = new HashMap<String, Boolean>();
		for (int i = 0; i < this.fields.length; i++) {
			String key = this.fields[i].trim();
			if (pks.containsKey(key.toUpperCase())) {
				continue;
			}
			updateFields.put(key, true);
		}
		String sql = this.sqlUpdateChanged(this.tableName, this.keyFields, updateFields);
		return sql;
	}

	/**
	 * 创建 更新变化 SQL
	 * 
	 * @param tableName    表名
	 * @param keys         主键
	 * @param updateFields 更新字段
	 * @return
	 */
	public String sqlUpdateChanged(String tableName, String[] keys, Map<String, Boolean> updateFields) {
		if (updateFields.size() == 0) {
			return null;
		}
		this.ConnectToDatabase();
		boolean isMysql = "mysql".equalsIgnoreCase(this._Conn.getDatabaseType());
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		// 增加database前缀
		if (StringUtils.isNotBlank(this.database)) {
			sb.append(this.database);
			sb.append(".");
		}
		sb.append(tableName);
		sb.append(" SET ");
		int inc = 0;

		StringBuilder sbWhere = new StringBuilder();
		// 主键map
		HashMap<String, Boolean> keysMap = new HashMap<String, Boolean>();
		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				sbWhere.append(" AND ");
			}
			String key = keys[i];
			if (isMysql && SqlUtils.MYSQL_RESERVED.containsKey(key.toUpperCase())) {
				sbWhere.append("`" + key + "`");
			} else {
				sbWhere.append(key);
			}
			sbWhere.append("=@");
			sbWhere.append(key);
			keysMap.put(key.toLowerCase(), true);
		}
		for (String key : updateFields.keySet()) {
			if (keysMap.containsKey(key.toLowerCase())) {
				// 主键不更新
				continue;
			}
			if (inc > 0) {
				sb.append("\n ,");
			}
			if (isMysql && SqlUtils.MYSQL_RESERVED.containsKey(key.toUpperCase())) {
				sb.append("`" + key + "`");
			} else {
				sb.append(key);
			}

			sb.append("=@");
			sb.append(key);
			inc++;
		}
		sb.append(" WHERE   ");
		sb.append(sbWhere);
		return sb.toString();
	}

	/**
	 * 创建复制数据的SQL<br>
	 * 例如：insert into xxx (f1,f2,f3) select f1,f2,f3 from xxx where vvv
	 * 
	 * @param fromWhere
	 * @return
	 */
	public String sqlCopy(String fromWhere) {
		return this.sqlCopy(fromWhere, null);
	}

	/**
	 * 创建复制数据的SQL<br>
	 * 例如：insert into xxx (f1,f2,f3) select f1,f2,f3 from xxx where vvv
	 * 
	 * @param fromWhere     数据来源的查询条件
	 * @param excludeFields 用,分割的需要排除的字段，
	 * @return
	 */
	public String sqlCopy(String fromWhere, String excludeFields) {
		Map<String, Boolean> exs = this.createAllUpdateFields(excludeFields, true);

		this.ConnectToDatabase();
		boolean isMysql = SqlUtils.isMySql(this._Conn);

		String tb;
		// 增加database前缀
		if (StringUtils.isNotBlank(this.database)) {
			tb = this.database + "." + this.tableName;
		} else {
			tb = this.tableName;
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_values = new StringBuilder();
		sb.append("INSERT INTO ").append(tb).append(" ( ");
		sb_values.append("SELECT ");
		int inc = 0;
		for (int i = 0; i < this.fields.length; i++) {
			String key = this.fields[i];
			if (this.autoKey != null && key.equalsIgnoreCase(this.autoKey)) {
				continue;// 自增字段
			}
			if (exs.containsKey(key.toLowerCase())) {
				continue;
			}
			if (inc > 0) {
				sb.append(", ");
				sb_values.append(", ");
			}
			if (isMysql && SqlUtils.MYSQL_RESERVED.containsKey(key.toUpperCase())) {
				sb.append("`" + key + "`");
				sb_values.append("`" + key + "`");
			} else {
				sb.append(key);
				sb_values.append(key);
			}
			inc++;
		}
		sb.append(")\n");
		sb_values.append("\nFROM ").append(tb).append("\nWHERE ").append(fromWhere);

		return sb.append(sb_values).toString();
	}

	/**
	 * 创建 新建变化的 SQL
	 * 
	 * @param tableName    表名
	 * @param updateFields 更新字段
	 * @return
	 */
	public String sqlInsertChanged(String tableName, Map<String, Boolean> updateFields, ClassBase ref) {
		if (updateFields.size() == 0 || ref == null) {
			return null;
		}
		this.ConnectToDatabase();
		boolean isMysql = SqlUtils.isMySql(this._Conn);

		StringBuilder sb = new StringBuilder();
		StringBuilder sb_values = new StringBuilder();
		sb.append("INSERT INTO ");
		// 增加database前缀
		if (StringUtils.isNotBlank(this.database)) {
			sb.append(this.database);
			sb.append(".");
		}
		sb.append(tableName);
		sb.append(" ( ");
		int inc = 0;
		for (String key : updateFields.keySet()) {
			// 排除为NULL的字段
			if (ref.getField(key) == null) {
				continue;
			}
			if (this.autoKey != null && key.equalsIgnoreCase(this.autoKey)) {
				// 自增字段
				continue;
			}
			if (inc > 0) {
				sb.append("\n ,");
				sb_values.append("\n ,");
			}
			if (isMysql && SqlUtils.MYSQL_RESERVED.containsKey(key.toUpperCase())) {
				sb.append("`" + key + "`");
			} else {
				sb.append(key);
			}

			sb_values.append("@");
			sb_values.append(key);
			inc++;
		}
		if (inc == 0) {
			return null;
		}

		sb.append(") VALUES (");
		sb.append(sb_values);
		sb.append(")");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeUpdate(java.lang.String,
	 * com.gdxsoft.easyweb.script.RequestValue)
	 */
	public boolean executeUpdate(String sql, RequestValue requestValue) {
		ConnectToDatabase();

		_Conn.setRequestValue(requestValue);
		_Conn.executeUpdate(sql);
		_Conn.close();
		if (this._Conn.getErrorMsg() == null) {
			return true;
		} else {
			this._ErrorMsg = this._Conn.getErrorMsg();
			return false;
		}
	}

	/**
	 * 执行自增插入
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public int executeUpdateAutoIncrement(String sql, RequestValue requestValue) {
		BigInteger autoKey = executeUpdateAutoIncrementReturnBigInteger(sql, requestValue);
		return autoKey.intValueExact();
	}

	/**
	 * 执行自增插入
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public BigInteger executeUpdateAutoIncrementReturnBigInteger(String sql, RequestValue requestValue) {
		ConnectToDatabase();
		_Conn.setRequestValue(requestValue);
		Object auto_key = _Conn.executeUpdateReturnAutoIncrementObject(sql);
		_Conn.close();

		if (this._Conn.getErrorMsg() == null) {
			return new BigInteger(auto_key.toString());
		} else {
			this._ErrorMsg = this._Conn.getErrorMsg();
			return BigInteger.valueOf(-1l);
		}
	}

	/**
	 * 执行自增插入
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public long executeUpdateAutoIncrementLong(String sql, RequestValue requestValue) {
		BigInteger autoKey = executeUpdateAutoIncrementReturnBigInteger(sql, requestValue);
		return autoKey.longValue();
	}

	/**
	 * 执行自增插入，返回UInt32
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public UInt32 executeUpdateAutoIncrementUInt32(String sql, RequestValue requestValue) {
		BigInteger autoKey = executeUpdateAutoIncrementReturnBigInteger(sql, requestValue);
		return UInt32.valueOf(autoKey);
	}

	/**
	 * 执行自增插入，返回UInt16
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public UInt16 executeUpdateAutoIncrementUInt16(String sql, RequestValue requestValue) {
		BigInteger autoKey = executeUpdateAutoIncrementReturnBigInteger(sql, requestValue);
		return UInt16.valueOf(autoKey);
	}

	/**
	 * 执行自增插入，返回UInt64
	 * 
	 * @param sql
	 * @param requestValue
	 * @return
	 */
	public UInt64 executeUpdateAutoIncrementUInt64(String sql, RequestValue requestValue) {
		BigInteger autoKey = executeUpdateAutoIncrementReturnBigInteger(sql, requestValue);
		return UInt64.valueOf(autoKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeProcdure(java.lang.
	 * String, com.gdxsoft.easyweb.script.RequestValue)
	 */
	public HashMap<String, Object> executeProcdure(String procName, RequestValue requestValue) {
		ConnectToDatabase();
		_Conn.setRequestValue(requestValue);
		HashMap<String, Object> hm = _Conn.executeProcdure(procName);
		_Conn.close();
		return hm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeQuery(java.lang.String,
	 * T, java.lang.String[])
	 */
	public ArrayList<T> executeQuery(String sql, T obj, String[] fieldList) {
		ConnectToDatabase();
		_Conn.executeQuery(sql);
		return createList(obj, fieldList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeQuery(java.lang.String,
	 * T, java.lang.String[], java.lang.String, int, int)
	 */
	public ArrayList<T> executeQuery(String sql, T obj, String[] fieldList, String pkFieldName, int pageSize,
			int currentPage) {
		ConnectToDatabase();
		_Conn.executeQueryPage(sql, pkFieldName, currentPage, pageSize);
		return createList(obj, fieldList);
	}

	/**
	 * 获取指定字段的对象列表
	 * 
	 * @param sql
	 * @param fieldList
	 * @param pkFieldName
	 * @param pageSize
	 * @param currentPage
	 * @return
	 */
	public ArrayList<Object[]> executeQueryByFields(String sql, Integer[] fieldList, String pkFieldName, int pageSize,
			int currentPage) {
		ConnectToDatabase();
		_Conn.executeQueryPage(sql, pkFieldName, currentPage, pageSize);
		return createList(fieldList);
	}

	/**
	 * 执行分页并返回DTTable
	 * 
	 * @param sql
	 * @param pkFieldName
	 * @param pageSize
	 * @param currentPage
	 * @throws Exception
	 */
	public DTTable executeQuery(String sql, String pkFieldName, int pageSize, int currentPage) throws Exception {
		ConnectToDatabase();
		_Conn.executeQueryPage(sql, pkFieldName, currentPage, pageSize);
		if (_Conn.getErrorMsg() != null && _Conn.getErrorMsg().trim().length() > 0) {
			_Conn.close();
			throw new Exception(_Conn.getErrorMsg());
		}
		DTTable t = new DTTable();
		t.initData(_Conn.getLastResult().getResultSet());
		_Conn.getLastResult().getResultSet().close();
		_Conn.close();
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#executeQuery(java.lang.String,
	 * com.gdxsoft.easyweb.script.RequestValue, T, java.lang.String[])
	 */
	public ArrayList<T> executeQuery(String sql, RequestValue requestValue, T obj, String[] fieldList) {
		ConnectToDatabase();
		_Conn.setRequestValue(requestValue);
		_Conn.executeQuery(sql);
		return createList(obj, fieldList);
	}

	/**
	 * 链接到数据库
	 */
	private void ConnectToDatabase() {
		if (_Conn == null) {
			_Conn = new DataConnection();
			_Conn.setConfigName(_ConfigName);
			if (this._rv != null) {
				this._Conn.setRequestValue(_rv);
			}
		}
	}

	/**
	 * 检查是否一致
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	boolean checkEquals(Object from, Object to) {
		if (from == null && to != null) {
			return false;
		}
		if (from != null && to == null) {
			return false;
		}
		if (from == null && to == null) {
			return true;
		}
		if (from.equals(to)) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<T> createList(T obj, String[] fieldList) {
		ArrayList<T> al = new ArrayList<T>();
		int m = 0;
		UObjectValue ov = new UObjectValue();
		try {
			if (this._Conn.getErrorMsg() != null) {
				this._ErrorMsg = this._Conn.getErrorMsg();
				_Conn.close();
				return null;
			}
			ResultSet rs = _Conn.getLastResult().getResultSet();

			while (rs.next()) {
				if (m > 500) {
					break;
				}
				T newObject = (T) obj.getClass().getDeclaredConstructor().newInstance();
				ov.setObject(newObject);
				ov.setDaoValue(rs, fieldList);
				al.add(newObject);
				m++;
			}
		} catch (Exception e) {
			this._ErrorMsg = e.getMessage();
			System.err.println(e.getMessage());
			return null;
		} finally {
			_Conn.close();
		}
		return al;
	}

	private ArrayList<Object[]> createList(Integer[] fieldList) {
		ArrayList<Object[]> al = new ArrayList<Object[]>();
		if (this._Conn.getErrorMsg() != null) {
			this._ErrorMsg = this._Conn.getErrorMsg();
			_Conn.close();
			return null;
		}
		ResultSet rs = _Conn.getLastResult().getResultSet();
		try {
			while (rs.next()) {
				Object[] o = new Object[fieldList.length];
				for (int i = 0; i < fieldList.length; i++) {
					int idx = fieldList[i];
					o[i] = rs.getObject(idx);
				}
				al.add(o);
			}
		} catch (Exception e) {
			this._ErrorMsg = e.getMessage();
			System.err.println(e.getMessage());
			return null;
		} finally {
			_Conn.close();
		}
		return al;
	}

	public String[] getSqlFields() {
		return this.fields;
	}

	public String getSqlSelect() {
		String pks = this.createPkSql();
		String sql = this.createSelectSql(tableName, pks, null);
		return sql;
	}

	public String getSqlDelete() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("DELETE FROM ");
		stringBuilder.append(this.tableName);
		stringBuilder.append(" WHERE ");
		String pk = this.createPkSql();
		stringBuilder.append(pk);
		return stringBuilder.toString();
	}

	public String getSqlInsert() {
		return this.sqlInsert;
	}

	/**
	 * @param sqlInsert the sqlInsert to set
	 */
	public void setSqlInsert(String sqlInsert) {
		this.sqlInsert = sqlInsert;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#getConfigName()
	 */
	public String getConfigName() {
		return _ConfigName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.datasource.IClassDao#setConfigName(java.lang.String)
	 */
	public void setConfigName(String configName) {
		_ConfigName = configName;
	}

	public DataConnection getConn() {
		if (this._Conn == null) {
			this.ConnectToDatabase();
		}
		return this._Conn;
	}

	public RequestValue getRv() {
		return _rv;
	}

	public void setRv(RequestValue rv) {
		this._rv = rv;
		if (this._Conn != null) {
			this._Conn.setRequestValue(rv);
		}
	}

	/**
	 * @return the instanceClass
	 */
	public Class<?> getInstanceClass() {
		return instanceClass;
	}

	/**
	 * @param instanceClass the instanceClass to set
	 */
	public void setInstanceClass(Class<?> instanceClass) {
		this.instanceClass = instanceClass;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the fields
	 */
	public String[] getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(String[] fields) {
		this.fields = fields;
	}

	/**
	 * @return the keyFields
	 */
	public String[] getKeyFields() {
		return keyFields;
	}

	/**
	 * @param keyFields the keyFields to set
	 */
	public void setKeyFields(String[] keyFields) {
		this.keyFields = keyFields;
	}

	/**
	 * 数据库
	 * 
	 * @return
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * 数据库
	 * 
	 * @param database
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * 自增主键
	 * 
	 * @return the autoKey
	 */
	public String getAutoKey() {
		return autoKey;
	}

	/**
	 * 自增主键
	 * 
	 * @param autoKey the autoKey to set
	 */
	public void setAutoKey(String autoKey) {
		this.autoKey = autoKey;
	}

}
