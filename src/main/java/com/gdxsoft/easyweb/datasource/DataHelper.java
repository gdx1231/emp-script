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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.utils.msnet.MStr;
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
	private static final Map<String, DataSource> DATASOURCES = new ConcurrentHashMap<>();
	// Per-datasource locks to avoid concurrent creation and potential deadlocks
	// 使用一个静态的锁工厂来创建每个数据源专用的锁，避免锁的频繁创建和销毁
	private static final ConcurrentHashMap<String, Object> DS_LOCK_FACTORIES = new ConcurrentHashMap<>();

	/**
	 * Close all datasources created and clear the DATASOURCES map to avoid
	 * classloader leaks on webapp reload.
	 */
	public static void closeAllDataSources() {
		if (DATASOURCES == null || DATASOURCES.isEmpty()) {
			return;
		}
		for (Map.Entry<String, DataSource> e : new ArrayList<>(DATASOURCES.entrySet())) {
			String name = e.getKey();
			DataSource ds = e.getValue();
			try {
				if (ds instanceof HikariDataSource) {
					LOGGER.info("Closing HikariDataSource {}", name);
					((HikariDataSource) ds).close();
				} else if (ds instanceof DruidDataSource) {
					LOGGER.info("Closing DruidDataSource {}", name);
					((DruidDataSource) ds).close();
				} else {
					// attempt reflective close
					try {
						java.lang.reflect.Method m = ds.getClass().getMethod("close");
						m.setAccessible(true);
						m.invoke(ds);
						LOGGER.info("Invoked close() on DataSource {} of type {}", name, ds.getClass().getName());
					} catch (NoSuchMethodException nsme) {
						LOGGER.debug("DataSource {} has no close() method: {}", name, ds.getClass().getName());
					}
				}
			} catch (Throwable t) {
				LOGGER.error("Error closing DataSource {}: {}", name, t.getMessage(), t);
			}
		}
		DATASOURCES.clear();
		DS_LOCK_FACTORIES.clear(); // 清理锁工厂
	}

	private Connection _conn;
	private boolean _connected;
	private ConnectionConfig _Cfg;
	private String _ErrorMsg;
	private ArrayList<Statement> _ListStatement = new ArrayList<>();
	private ArrayList<PreparedStatement> _ListPrepared = new ArrayList<>();
	private ArrayList<CallableStatement> _ListCallable = new ArrayList<>();

	public DataHelper(ConnectionConfig cfg) {
		_Cfg = cfg;
	}

	public Connection getConnection() {
		return _conn;
	}

	/**
	 * 外部设置 数据库连接
	 *
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		_conn = connection;
		this._connected = true;
	}

	/**
	 * 创建 JNDI 数据源连接
	 *
	 * @return
	 * @throws Exception
	 */
	private boolean createJiniDataSource() throws Exception {
		try {
			InitialContext ctx = new InitialContext();
			Context env = (Context) ctx.lookup("java:comp/env");
			DataSource pool;
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
			LOGGER.error(_ErrorMsg, ee); // 添加异常堆栈信息
			throw ee;
		}
		return _connected;
	}

	/**
	 * 连接到数据库 优化：在获取锁前先检查缓存，减少不必要的同步开销。
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

		// 如果配置了外部JNDI数据源
		if (_Cfg.getPool() == null) {
			return createJiniDataSource();
		}

		// --- 自定义连接池逻辑 ---
		String key = _Cfg.getName().toUpperCase().trim();

		// 1. 优化点：首先尝试从缓存获取数据源，避免进入锁逻辑
		DataSource cachedDataSource = DATASOURCES.get(key);
		if (cachedDataSource != null) {
			try {
				// 尝试从现有池中获取连接，这通常是非常快的操作
				this._conn = cachedDataSource.getConnection();
				_connected = true;
				return _connected;
			} catch (SQLException e) {
				// 如果连接失败，可能是池有问题，但这种情况较少见，仍需处理
				_connected = false;
				this._ErrorMsg = e.getMessage();
				LOGGER.error("Failed to get connection from existing pool for key: " + key, e);
				throw e;
			}
		}

		// 2. 缓存未命中，开始创建数据源的流程
		// 获取一个专门用于创建此数据源的锁对象
		Object lockObj = DS_LOCK_FACTORIES.computeIfAbsent(key, k -> new Object());

		// 使用 synchronized 块，其性能在现代JVM中对于简单场景已足够好，且代码更简洁
		synchronized (lockObj) {
			// 3. 双重检查：在获取锁后再次检查缓存，防止重复创建
			DataSource dataSourceInCache = DATASOURCES.get(key);
			if (dataSourceInCache != null) {
				// 在锁内发现另一个线程已经创建了数据源，直接复用
				try {
					this._conn = dataSourceInCache.getConnection();
					_connected = true;
				} catch (SQLException e) {
					_connected = false;
					this._ErrorMsg = e.getMessage();
					LOGGER.error("Failed to get connection from concurrently created pool for key: " + key, e);
					throw e;
				}
				return _connected;
			}

			// 4. 当前线程负责创建数据源
			try {
				DataSource newlyCreatedDs = this.createMyDatasource();
				// createMyDatasource() 内部已经将新创建的数据源放入了 DATASOURCES 缓存
				// 为保证一致性，再次从缓存获取并尝试连接
				DataSource finalDs = DATASOURCES.get(key);
				if (finalDs == null || finalDs != newlyCreatedDs) {
					// 理论上不应该发生，但如果发生了，说明内部逻辑有问题
					throw new IllegalStateException("Data source creation failed internally for key: " + key);
				}
				this._conn = finalDs.getConnection();
				_connected = true;
			} catch (Exception e) {
				_connected = false;
				this._ErrorMsg = e.getMessage();
				LOGGER.error("Failed to create or get connection from new pool for key: " + key, e);
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
			// 默认使用 HikariCP
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
		LOGGER.info("Using HikariCP for config: {}", _Cfg.getName());
		String driverClassName = _Cfg.getPool().get("driverClassName");
		MStr errors = new MStr();
		if (StringUtils.isBlank(driverClassName)) {
			LOGGER.error("driverClassName not defined in config: {}", _Cfg.getName());
			errors.al("driverClassName not defined");
		}
		String url = _Cfg.getPool().get("url");
		if (StringUtils.isBlank(url)) {
			LOGGER.error("url not defined in config: {}", _Cfg.getName());
			errors.al("url not defined");
		}
		String username = _Cfg.getPool().get("username");
		if (StringUtils.isBlank(username)) {
			LOGGER.error("username not defined in config: {}", _Cfg.getName());
			errors.al("username not defined");
		}

		if (errors.length() > 0) {
			throw new Exception(errors.toString());
		}

		String password = _Cfg.getPool().get("password");
		String maxActiveStr = _Cfg.getPool().get("maxActive");
		String maxWaitStr = _Cfg.getPool().get("maxWait");

		HikariDataSource cpds = new HikariDataSource();
		cpds.setDriverClassName(driverClassName);
		cpds.setJdbcUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);

		try {
			int maxActive = Integer.parseInt(maxActiveStr);
			cpds.setMaximumPoolSize(Math.max(1, maxActive)); // 确保至少为1
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid maxActive value '{}', using default 40", maxActiveStr);
			cpds.setMaximumPoolSize(40);
		}

		cpds.setMinimumIdle(1);

		// 设置连接最大生存时间，避免长连接问题
		long maxLifetimeMs = 1800000L; // 30分钟，默认值
		try {
			long maxWaitSec = Long.parseLong(maxWaitStr);
			if (maxWaitSec > 0) {
				maxLifetimeMs = Math.max(30000L, maxWaitSec * 1000L); // 转换为毫秒，并确保最小值
			}
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid maxWait value '{}', using default 30 minutes for maxLifetime", maxWaitStr);
		}
		cpds.setMaxLifetime(maxLifetimeMs);

		String key = _Cfg.getName().toUpperCase().trim();
		DATASOURCES.put(key, cpds);
		LOGGER.info("HikariCP DataSource '{}' created successfully.", key);
		return cpds;
	}

	/**
	 * 使用 Druid
	 *
	 * @return
	 * @throws Exception
	 */
	private DataSource createMyDatasourcesDruids() throws Exception {
		LOGGER.info("Using Druid for config: {}", _Cfg.getName());
		String driverClassName = _Cfg.getPool().get("driverClassName");
		MStr errors = new MStr();
		if (StringUtils.isBlank(driverClassName)) {
			LOGGER.error("driverClassName not defined in config: {}", _Cfg.getName());
			errors.al("driverClassName not defined");
		}
		String url = _Cfg.getPool().get("url");
		if (StringUtils.isBlank(url)) {
			LOGGER.error("url not defined in config: {}", _Cfg.getName());
			errors.al("url not defined");
		}
		String username = _Cfg.getPool().get("username");
		if (StringUtils.isBlank(username)) {
			LOGGER.error("username not defined in config: {}", _Cfg.getName());
			errors.al("username not defined");
		}
		String password = _Cfg.getPool().get("password");
		if (StringUtils.isBlank(password)) {
			LOGGER.error("password not defined in config: {}", _Cfg.getName());
			errors.al("password not defined");
		}

		if (errors.length() > 0) {
			throw new Exception(errors.toString());
		}

		String maxActiveStr = _Cfg.getPool().get("maxActive");
		String maxWaitStr = _Cfg.getPool().get("maxWait");

		DruidDataSource cpds = new DruidDataSource();
		cpds.setDriverClassName(driverClassName);
		cpds.setUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);

		try {
			int maxActive = Integer.parseInt(maxActiveStr);
			cpds.setMaxActive(Math.max(1, maxActive));
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid maxActive value '{}', using default 8", maxActiveStr);
			cpds.setMaxActive(8);
		}

		cpds.setInitialSize(1);
		cpds.setMinIdle(1);
		cpds.setTestOnBorrow(false);
		cpds.setTestOnReturn(false);
		cpds.setTestWhileIdle(false);
		cpds.setValidationQueryTimeout(60);

		try {
			int maxWaitMs = Integer.parseInt(maxWaitStr);
			cpds.setMaxWait(Math.max(1000, maxWaitMs)); // 确保最小值
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid maxWait value '{}', using default 60000ms", maxWaitStr);
			cpds.setMaxWait(60000);
		}

		String key = _Cfg.getName().toUpperCase().trim();
		DATASOURCES.put(key, cpds);
		LOGGER.info("Druid DataSource '{}' created successfully.", key);
		return cpds;
	}

	/**
	 * 获取 Statement 对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public Statement getStatement() throws Exception {
		this.connect();
		Statement statement = this._conn.createStatement();
		this._ListStatement.add(statement);
		return statement;
	}

	/**
	 * 获取 PreparedStatement 对象
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

	/**
	 * 获取 PreparedStatement 对象，并且设置为自动返回自增主键
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public PreparedStatement getPreparedStatementAutoIncrement(String sql) throws Exception {
		this.connect();
		PreparedStatement pst = this._conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		this._ListPrepared.add(pst);
		return pst;
	}

	/**
	 * 获取 CallableStatement 对象
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
	 * 关闭数据库连接
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
			if (_conn != null) {
				_conn.close();
			}
			_connected = false;
		} catch (SQLException ee) {
			this._ErrorMsg += "\r\n" + ee.getMessage();
		}
	}

	/**
	 * 获取错误信息
	 * 
	 * @return
	 */
	public String getErrorMsg() {
		return _ErrorMsg;
	}
}