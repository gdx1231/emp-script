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
	 * 外部设置 数据库连接
	 * 
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		_conn = connection;
		this._connected = true;
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

		// connectionTimeout
		// 此属性控制客户端（即您）等待来自池的连接的最大毫秒数。如果超过此时间而没有可用的连接，则会抛出SQLException。可接受的最低连接超时为250
		// ms。 默认值：30000（30秒）

		// idleTimeout
		// 此属性控制允许连接在池中保持空闲状态的最长时间。 仅当minimumIdle定义为小于时，此设置才适用maximumPoolSize。池达到连接后，
		// 空闲连接将不会退出minimumIdle。连接是否以空闲状态退役，最大变化为+30秒，平均变化为+15秒。在此超时之前，连接永远不会因为闲置而退役。值为0表示永远不会从池中删除空闲连接。最小允许值为10000ms（10秒）。
		// 默认值：600000（10分钟）

		// maxLifetime
		// 此属性控制池中连接的最大生存期。使用中的连接永远不会停止使用，只有在关闭连接后才将其删除。在逐个连接的基础上，应用较小的负衰减以避免池中的质量消灭。
		// 我们强烈建议设置此值，它应该比任何数据库或基础结构施加的连接时间限制短几秒钟。
		// 值0表示没有最大寿命（无限寿命），当然要遵守该idleTimeout设置。最小允许值为30000ms（30秒）。 默认值：1800000（30分钟）

		// minimumIdle
		// 此属性控制HikariCP尝试在池中维护的最小空闲连接数。如果空闲连接下降到该值以下，并且池中的总连接数少于maximumPoolSize，则HikariCP将尽最大努力快速而有效地添加其他连接。但是，为了获得最佳性能和对峰值需求的响应能力，我们建议不要设置此值，而应让HikariCP充当固定大小的连接池。
		// 默认值：与maximumPoolSize相同

		// maximumPoolSize
		// 此属性控制允许池达到的最大大小，包括空闲和使用中的连接。基本上，此值将确定到数据库后端的最大实际连接数。合理的值最好由您的执行环境确定。当池达到此大小并且没有空闲连接可用时，对getConnection（）的调用将connectionTimeout在超时之前最多阻塞毫秒。请阅读有关池大小的信息。
		// 默认值：10

		LOGGER.info("Using HikariCP");
		String driverClassName = _Cfg.getPool().get("driverClassName");
		MStr errors = new MStr();
		if (StringUtils.isBlank(driverClassName)) {
			LOGGER.error("driverClassName not defined");
			errors.al("driverClassName not defined");
		}
		String url = _Cfg.getPool().get("url");
		if (StringUtils.isBlank(url)) {
			LOGGER.error("url not defined");
			errors.al("url not defined");
		}
		String username = _Cfg.getPool().get("username");
		if (StringUtils.isBlank(username)) {
			LOGGER.error("username not defined");
			errors.al("username not defined");
		}
		LOGGER.debug("driverClassName={}, username={}, url={}", driverClassName, username, url);
		if (errors.length() > 0) {
			throw new Exception(errors.toString());
		}

		String password = _Cfg.getPool().get("password");
		String maxActive = _Cfg.getPool().get("maxActive");
		// String maxIdle = _Cfg.getPool().get("maxIdle");

		HikariDataSource cpds = new HikariDataSource();

		cpds.setDriverClassName(driverClassName);
		cpds.setJdbcUrl(url);
		cpds.setUsername(username);
		cpds.setPassword(password);
		try {
			// 允许池达到的最大值
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
			cpds.setMaxLifetime(maxWaitMs);
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
		MStr errors = new MStr();
		if (StringUtils.isBlank(driverClassName)) {
			LOGGER.error("driverClassName not defined");
			errors.al("driverClassName not defined");
		}
		String url = _Cfg.getPool().get("url");
		if (StringUtils.isBlank(url)) {
			LOGGER.error("url not defined");
			errors.al("url not defined");
		}
		String username = _Cfg.getPool().get("username");
		if (StringUtils.isBlank(username)) {
			LOGGER.error("username not defined");
			errors.al("username not defined");
		}
		String password = _Cfg.getPool().get("password");
		if (StringUtils.isBlank(password)) {
			LOGGER.error("password not defined");
			errors.al("password not defined");
		}
		LOGGER.debug("driverClassName={}, username={}, password=xxx, url={}", driverClassName, username, url);
		if (errors.length() > 0) {
			throw new Exception(errors.toString());
		}

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