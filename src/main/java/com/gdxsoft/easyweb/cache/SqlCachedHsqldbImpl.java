package com.gdxsoft.easyweb.cache;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

public class SqlCachedHsqldbImpl implements ISqlCached {
	private static Logger LOGGER = LoggerFactory.getLogger(SqlCachedHsqldbImpl.class);
	private static SqlCachedHsqldbImpl INSTANCE;

	public static String CONN_STR = "____ewa_cached_hsqldb__"; // 必须小写

	private static String CONN_URL = "jdbc:hsqldb:mem:EWA_CACHED";

	public static Boolean DEBUG = false;

	static {
		if (INSTANCE == null) {
			LOGGER.info("static INIT");

			INSTANCE = new SqlCachedHsqldbImpl();
			SqlCachedHsqldbImpl.init();

		} else {
			LOGGER.info("exists INIT ??????????");
		}
	}

	public static SqlCachedHsqldbImpl getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SqlCachedHsqldbImpl();
			boolean rst = SqlCachedHsqldbImpl.init();
			if (!rst) {
				LOGGER.error(rst + "");
			}
		}
		return INSTANCE;
	}

	/**
	 * Add a cache
	 * 
	 * @param key   the cache key
	 * @param value the cache value
	 * @return successful
	 */
	public boolean add(String key, String value) {
		return this.add(key, value, null);
	}

	/**
	 * Add a cache
	 * 
	 * @param key   the cache key
	 * @param value the cache value
	 * @param memo  the cache memo
	 * @return successful
	 */
	public boolean add(String key, String value, String memo) {
		long t0 = DEBUG ? System.currentTimeMillis() : 0;

		this.remove(key, "TEXT");

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO CACHED(KEY1,VAL_TYPE,VAL_TXT,last_time, VAL_MEMO)"
				+ " VALUES(@PARAM_KEY,'TEXT',@PARAM_VAL, @sys_date, @PARAM_MEMO)");
		String sql = sb.toString();

		RequestValue rv = new RequestValue();

		rv.addValue("PARAM_KEY", this.getParameterKey(key));
		rv.addValue("PARAM_VAL", value, "string", value.length());
		rv.addValue("PARAM_MEMO", memo == null ? null : memo.length() > 200 ? memo.substring(0, 200) : memo);
		String[] sqls = new String[1];
		sqls[0] = sql;

		boolean o = this.addToCache(sqls, rv);
		if (DEBUG) {
			long t1 = System.currentTimeMillis();
			LOGGER.info("SQLCACHED-PUT: " + key + " (" + key.hashCode() + " ):" + (t1 - t0) + "ms");
			LOGGER.info("\t" + sql);
		}
		return o;
	}

	/**
	 * return the key md5
	 * 
	 * @param key
	 * @return
	 */
	private String getParameterKey(String key) {
		String paramKey = Utils.md5(key);
		return paramKey;
	}

	/**
	 * Cached a binary data
	 * 
	 * @param key   the cached key
	 * @param value the cached value
	 * @param memo  the cached memo
	 * @return successful
	 */
	public boolean add(String key, byte[] value, String memo) {
		String paramKey = getParameterKey(key);

		long t0 = DEBUG ? System.currentTimeMillis() : 0;

		this.remove(key, "BIN");

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO CACHED(KEY1,VAL_TYPE,VAL_BIN,last_time, VAL_MEMO)"
				+ " VALUES (@PARAM_KEY,'BIN', @PARAM_VAL, @sys_date , @PARAM_MEMO)");
		String sql = sb.toString();
		RequestValue rv = new RequestValue();

		rv.addValue("PARAM_KEY", paramKey);
		rv.addValue("PARAM_VAL", value, "binary", value.length);
		rv.addValue("PARAM_MEMO", memo == null ? null : memo.length() > 200 ? memo.substring(0, 200) : memo);

		String[] sqls = new String[1];
		sqls[0] = sql;

		boolean o = this.addToCache(sqls, rv);

		if (DEBUG) {
			long t1 = System.currentTimeMillis();
			LOGGER.info("SQLCACHED-PUT: " + key + " (" + key + " ) :" + (t1 - t0) + "ms");
			LOGGER.info("\t" + sql);
		}

		return o;
	}

	/**
	 * Cached a binary data
	 * 
	 * @param key   the cached key
	 * @param value the cached value
	 * @return successful
	 */
	public boolean add(String key, byte[] value) {
		return this.add(key, value, null);
	}

	/**
	 * Get a binary data
	 * 
	 * @param key the cached key
	 * @return
	 */
	public SqlCachedValue getBinary(String key) {
		return this.get(key, "BIN");
	}

	/**
	 * Get a text data
	 * 
	 * @param key the cached key
	 * @return
	 */
	public SqlCachedValue getText(String key) {
		return this.get(key, "TEXT");
	}

	/**
	 * Get a cached object
	 * 
	 * @param key  the cached key
	 * @param type the cached type(BIN/ TEXT)
	 * @return
	 */
	public SqlCachedValue get(String key, String type) {
		long t0 = DEBUG ? System.currentTimeMillis() : 0;

		String paramKey = getParameterKey(key);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM CACHED WHERE KEY1='");
		sb.append(paramKey);
		sb.append("' AND val_type='");
		sb.append(type.replace("'", "''").toUpperCase());
		sb.append("'");
		String sql = sb.toString();

		DTTable tb = DTTable.getJdbcTable(sql, CONN_STR);

		if (tb == null || !tb.isOk() || tb.getCount() == 0) {
			if (DEBUG) {
				long t1 = System.currentTimeMillis();
				LOGGER.info("SQLCACHED-GET:" + key + " !!NO_DATA!! :" + (t1 - t0) + "ms");
				LOGGER.info("\t" + sql);
			}
			return null;
		}
		SqlCachedValue cv = new SqlCachedValue();
		try {
			cv.setLastTime(tb.getCell(0, "last_time").toTime());
			cv.setType(tb.getCell(0, "VAL_TYPE").toString());
			cv.setMemo(tb.getCell(0, "VAL_MEMO").toString());

			if (type.equals("BIN")) {// 二进制
				cv.setValue(tb.getCell(0, "VAL_BIN").getValue());
			} else {// 文本
				cv.setValue(tb.getCell(0, "val_txt").getValue());
			}
		} catch (Exception err) {
			cv.setValue(err.getMessage());
		}
		if (DEBUG) {
			long t1 = System.currentTimeMillis();
			LOGGER.info("SQLCACHED-GET:" + key + ", " + tb.getCount() + " records : " + (t1 - t0) + "ms");
			LOGGER.info("\t" + sql);
		}
		return cv;
	}

	/**
	 * 删除单个缓存
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public boolean remove(String key, String type) {
		String[] keys = new String[1];
		keys[0] = key;
		return this.removes(keys, type);
	}

	/**
	 * 删除多个缓存
	 * 
	 * @param keys
	 * @param type
	 * @return
	 */
	public boolean removes(String[] keys, String type) {
		if (keys == null || keys.length == 0) {
			return true;
		}
		long t0 = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM CACHED WHERE val_type='" + type.replace("'", "''").toUpperCase() + "' and key1 in(");
		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("'");
			sb.append(getParameterKey(keys[i]));
			sb.append("'");
		}
		sb.append(")");

		DataConnection cnn = new DataConnection();
		cnn.setConfigName(CONN_STR);
		cnn.executeUpdate(sb.toString());

		boolean isok = true;
		if (cnn.getErrorMsg() != null) {
			isok = false;
			LOGGER.error(cnn.getErrorMsg());
		}
		cnn.close();

		if (DEBUG) {
			long t1 = System.currentTimeMillis();
			LOGGER.info("SQLCACHED-DELETE:" + sb.toString() + ":" + (t1 - t0) + "ms");
		}
		return isok;
	}

	private boolean addToCache(String[] sqls, RequestValue rv) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName(CONN_STR);
		cnn.setRequestValue(rv);
		for (int i = 0; i < sqls.length; i++) {
			cnn.executeUpdate(sqls[i]);
		}

		boolean isok = true;
		if (cnn.getErrorMsg() != null) {
			isok = false;
			LOGGER.error(cnn.getErrorMsg());
		}
		cnn.close();

		return isok;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	synchronized private static boolean init() {
		try {
			ConnectionConfigs c1 = ConnectionConfigs.instance();
			if (c1.containsKey(CONN_STR)) {
				return true;
			}

			ConnectionConfig poolCfg = new ConnectionConfig();
			poolCfg.setName(CONN_STR);
			poolCfg.setType("HSQLDB");
			poolCfg.setConnectionString(CONN_STR);
			poolCfg.setSchemaName("PUBLIC");

			MTableStr poolParams = new MTableStr();
			poolParams.put("driverClassName", "org.hsqldb.jdbcDriver");
			poolParams.put("url", CONN_URL);
			poolParams.put("username", "sa");
			poolParams.put("password", "");
			poolParams.put("maxActive", 10);
			poolParams.put("maxIdle", 100);

			poolCfg.setPool(poolParams);
			c1.put(CONN_STR, poolCfg);

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}

		StringBuilder sb1 = new StringBuilder();
		sb1.append("create table CACHED(key1 varchar(32),val_type varchar(5),val_bin LONGVARBINARY");
		sb1.append(",val_txt longvarchar,last_time datetime, val_memo varchar(200), primary key(key1,val_type))");

		List<String> sqls = new ArrayList<String>();
		sqls.add(sb1.toString());
		boolean isok = createTable("CACHED", sqls);

		if (isok) {
			// DebugInfo 记录用表
			sb1 = new StringBuilder();
			sb1.append("create table FRAME_DEBUG(D_ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)");
			sb1.append(" NOT NULL PRIMARY KEY");
			sb1.append(", D_XMLNAME varchar(100), D_ITEMNAME varchar(200), D_DATE TIMESTAMP");
			sb1.append(", D_HTML CLOB(10M), D_IP varchar(40), D_CGI VARCHAR(2000)");
			sb1.append(", D_USER_AGENT varchar(1000), D_REFERER varchar(2000) )");
			sqls = new ArrayList<String>();
			sqls.add(sb1.toString());

			String idx0 = "create index idx_FRAME_DEBUG_xmlname on FRAME_DEBUG(D_XMLNAME)";
			sqls.add(idx0);

			String idx1 = "create index idx_FRAME_DEBUG_ITEMNAME on FRAME_DEBUG(D_ITEMNAME)";
			sqls.add(idx1);

			createTable("FRAME_DEBUG", sqls);
		}
		return isok;
	}

	/**
	 * 如果不存在则创建表
	 * 
	 * @param tableName
	 * @param sqlCreate
	 * @return
	 */
	public static boolean createTable(String tableName, List<String> sqlCreates) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName(CONN_STR);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1 a FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='");
		sb.append(tableName);
		sb.append("'");
		String sqlexist = sb.toString();
		DTTable tb = DTTable.getJdbcTable(sqlexist, cnn);

		boolean isok = true;
		if (tb.getCount() == 0) {

			cnn.executeUpdateBatch(sqlCreates);
			if (cnn.getErrorMsg() != null) {
				isok = false;
				LOGGER.error(cnn.getErrorMsg());
			} else {
				LOGGER.info("CREATED TABLE: " + tableName);
			}
		}
		cnn.close();
		return isok;
	}
}
