package com.gdxsoft.easyweb.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;

/**
 * <p>
 * 这是一个与数据库联系的类,它与数据库连接,数据库查询,插入更新.
 * </p>
 * <p>
 * 它是其它与数据库联系类的超类.
 * </p>
 */
public class DataHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataHelper.class);
	// 记录使用的数据库连接池
	private static Map<String, DataSource> DATASOURCES = new ConcurrentHashMap<String, DataSource>();

	private Connection _conn;
	private boolean _connected;
	private ConnectionConfig _Cfg;
	private String _ErrorMsg;
	private ArrayList<Statement> _ListStatement = new ArrayList<Statement>();
	private ArrayList<PreparedStatement> _ListPrepared = new ArrayList<PreparedStatement>();
	private ArrayList<CallableStatement> _ListCallable = new ArrayList<CallableStatement>();

	public DataHelper(ConnectionConfig cfg) {
		_Cfg = cfg;
	}

	public Connection getConnection() {
		return _conn;
	}

	/**
	 * 连接到数据库
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean connect() throws Exception {
		if (_connected) {
			return true;
		}
		if (this._Cfg == null) {
			throw new Exception("ConnectionConfig 没有设置");
		}

		DataSource pool;
		if (_Cfg.getPool() == null) {
			try {
				InitialContext ctx = new InitialContext();
				Context env = (Context) ctx.lookup("java:comp/env");
				if (env != null) {
					pool = (DataSource) env.lookup(_Cfg.getConnectionString());
				} else {
					String connString = "java:comp/env/" + _Cfg.getConnectionString();
					pool = (DataSource) ctx.lookup(connString);
				}
				_conn = pool.getConnection();
				_connected = true;
			} catch (Exception ee) {
				_connected = false;
				this._ErrorMsg = ee.getMessage();
				LOGGER.error(_ErrorMsg);
				throw ee;
			}
		} else {
			String key = _Cfg.getName().toUpperCase().trim();
			DataSource cpds;
			if (DATASOURCES == null || !DATASOURCES.containsKey(key)) {
				try {
					cpds = this.createMyDatasource();
				} catch (Exception e) {
					_connected = false;
					this._ErrorMsg = e.getMessage();
					throw e;
				}
			} else {
				cpds = DATASOURCES.get(key);
			}

			try {
				this._conn = cpds.getConnection();
				_connected = true;
			} catch (SQLException e) {
				_connected = false;
				this._ErrorMsg = e.getMessage();
				LOGGER.error(_ErrorMsg);
				throw e;
			}
		}

		return _connected;
	}

	/**
	 * 创建自定义的数据库连接池
	 * 
	 * @return
	 * @throws Exception
	 */
	private DataSource createMyDatasource() throws Exception {
		String poolType = this._Cfg.getPool().get("poolType");
		if ("druid".equalsIgnoreCase(poolType)) {
			return this.createMyDatasourcesDruids();
		} else {
			return this.createMyDatasourcesHikariCP();
		}
	}

	/**
	 * 使用 HikariCP（默认）
	 * 
	 * @return
	 * @throws Exception
	 */
	private DataSource createMyDatasourcesHikariCP() throws Exception {
		LOGGER.info("Using HikariCP");
		String driverClassName = _Cfg.getPool().get("driverClassName");
		String url = _Cfg.getPool().get("url");
		String username = _Cfg.getPool().get("username");
		String password = _Cfg.getPool().get("password");
		String maxActive = _Cfg.getPool().get("maxActive");
		// String maxIdle = _Cfg.getPool().get("maxIdle");

		HikariDataSource cpds = new HikariDataSource();

		cpds.setDriverClassName(driverClassName);
		cpds.setJdbcUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		try {
			int maxActive1 = Integer.parseInt(maxActive);
			cpds.setMaximumPoolSize(maxActive1);
		} catch (Exception err) {
			cpds.setMaximumPoolSize(40);
		}

		cpds.setMinimumIdle(1);

		try {
			// 最大空闲时间,60秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
			String maxWait = _Cfg.getPool().get("maxWait");
			long maxWaitMs = Long.parseLong(maxWait) * 1000;
			cpds.setIdleTimeout(maxWaitMs);
		} catch (Exception err) {
			// 最大空闲时间,60秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
			cpds.setIdleTimeout(60 * 1000);
		}
		DATASOURCES.put(_Cfg.getName().toUpperCase().trim(), cpds);
		return cpds;
	}

	/**
	 * 使用 Druid
	 * 
	 * @return
	 * @throws Exception
	 */
	private DataSource createMyDatasourcesDruids() throws Exception {
		LOGGER.info("Using Druid");

		String driverClassName = _Cfg.getPool().get("driverClassName");
		String url = _Cfg.getPool().get("url");
		String username = _Cfg.getPool().get("username");
		String password = _Cfg.getPool().get("password");
		String maxActive = _Cfg.getPool().get("maxActive");
		// String maxIdle = _Cfg.getPool().get("maxIdle");

		DruidDataSource cpds = new DruidDataSource();

		cpds.setDriverClassName(driverClassName);
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		cpds.setMaxActive(Integer.parseInt(maxActive));

		cpds.setInitialSize(1);
		cpds.setMinIdle(1);
		cpds.setFilters("stat");

		cpds.setTestOnBorrow(false);
		cpds.setTestOnReturn(false);
		cpds.setTestWhileIdle(false);

		// 查询超时 60 s
		cpds.setValidationQueryTimeout(60);
		// cpds.setConnectionProperties("druid.stat.mergeSql=true");
		// cpds.setValidationQuery("select 1");
		try {
			// 最大空闲时间,60秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
			String maxWait = _Cfg.getPool().get("maxWait");
			cpds.setMaxWait(Integer.parseInt(maxWait));
		} catch (Exception err) {
			cpds.setMaxWait(60);
		}
		DATASOURCES.put(_Cfg.getName().toUpperCase().trim(), cpds);
		return cpds;
	}

	/**
	 * 获取Statement
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Statement getStatement() throws Exception {
		this.connect();
		Statement statement = this._conn.createStatement();
		this._ListStatement.add(statement);
		return statement;
	}

	/**
	 * 获取PreparedStatement
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public PreparedStatement getPreparedStatement(String sql) throws Exception {
		this.connect();
		PreparedStatement pst = this._conn.prepareStatement(sql);
		this._ListPrepared.add(pst);
		return pst;
	}

	public PreparedStatement getPreparedStatementAutoIncrement(String sql) throws Exception {
		this.connect();
		PreparedStatement pst = this._conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		this._ListPrepared.add(pst);
		return pst;
	}

	/**
	 * 获取CallableStatement
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public CallableStatement getCallableStatement(String sql) throws Exception {
		this.connect();
		CallableStatement cst = this._conn.prepareCall(sql);
		this._ListCallable.add(cst);
		return cst;
	}

	/**
	 * 关闭给数据库的连接
	 */
	public void close() {
		if (!_connected) {
			return;
		}
		if (_ErrorMsg == null)
			_ErrorMsg = "";

		for (int i = 0; i < this._ListCallable.size(); i++) {
			try {
				this._ListCallable.get(i).close();
			} catch (SQLException e) {
				this._ErrorMsg += "\r\n" + e.getMessage();
			}
		}
		for (int i = 0; i < this._ListStatement.size(); i++) {
			try {
				this._ListStatement.get(i).close();
			} catch (SQLException e) {
				this._ErrorMsg += "\r\n" + e.getMessage();
			}
		}
		for (int i = 0; i < this._ListPrepared.size(); i++) {
			try {
				this._ListPrepared.get(i).close();
			} catch (SQLException e) {
				this._ErrorMsg += "\r\n" + e.getMessage();
			}
		}
		try {
			// rset.close();
			if (_connected) {
				if (_conn != null) {
					_conn.close();
				}
				_connected = false;
			}
		} catch (SQLException ee) {
			this._ErrorMsg += "\r\n" + ee.getMessage();
		}

	}

	/**
	 * @return the _ErrorMsg
	 */
	public String getErrorMsg() {
		return _ErrorMsg;
	}

}