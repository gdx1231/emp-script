package com.gdxsoft.easyweb.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.utils.msnet.MList;

/**
 * Manages the JDBC connection lifecycle — connect, transaction control,
 * Statement/ResultSet tracking, and cleanup. Extracted from DataConnection
 * to reduce the God Class anti-pattern.
 * 
 * <p>Owns: {@link DataHelper}, {@link Connection}, {@link PreparedStatement},
 * {@link Statement} (query), {@link MList} of {@link DataResult},
 * and the active {@link ConnectionConfig}.
 * 
 * <p><b>Thread Safety:</b> Not thread-safe. One instance per
 * {@link DataConnection}.
 */
public class DataConnectionSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataConnectionSession.class);

	private DataHelper ds;
	private PreparedStatement pst;
	private Statement queryStatement;
	private Connection connection;
	private boolean isTrans;

	private final MList resultSetList = new MList();

	private final ConnectionConfigs configs;
	private ConnectionConfig currentConfig;

	private String connectionString;
	private String schemaName;
	private String databaseType;
	private String dataBaseName;

	/**
	 * Minimal constructor — picks the first configured data-source.
	 */
	public DataConnectionSession() throws Exception {
		this.configs = ConnectionConfigs.instance();
		this.currentConfig = this.configs.getConfig(0);
		initConnection();
	}

	/**
	 * Constructor that picks a named configuration (or the first if the name is
	 * unknown).
	 */
	public DataConnectionSession(String configName) throws Exception {
		this.configs = ConnectionConfigs.instance();
		setConfigName(configName);
	}

	// ── Configuration ────────────────────────────────────────────────

	/**
	 * Select the active database configuration by name.
	 */
	public void setConfigName(String configName) {
		if (configName == null || !this.configs.containsKey(configName.trim().toLowerCase())) {
			this.currentConfig = this.configs.getConfig(0);
		} else {
			this.currentConfig = this.configs.get(configName.trim().toLowerCase());
		}
		initConnection();
	}

	/**
	 * Select the active database configuration and optional database name.
	 */
	public void setConfigName(String configName, String dataBaseName) {
		setConfigName(configName);
		initConnection();
	}

	/**
	 * Replace the active configuration entirely.
	 */
	public void setCurrentConfig(ConnectionConfig cfg) {
		this.currentConfig = cfg;
		initConnection();
	}

	// ── Initialization & Connection ──────────────────────────────────

	private void initConnection() {
		this.ds = new DataHelper(this.currentConfig);
		this.connectionString = this.currentConfig.getConnectionString();
		this.schemaName = this.currentConfig.getSchemaName();
		this.databaseType = this.currentConfig.getType();
	}

	/**
	 * Explicitly connect if not already connected.
	 */
	public boolean connect() {
		try {
			if (this.ds == null) {
				initConnection();
			}
			this.ds.connect();
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * Switch the active database on MySQL / SQL Server.
	 */
	public boolean useDatabase() throws Exception {
		if (this.dataBaseName == null || this.dataBaseName.trim().length() == 0) {
			return false;
		}
		String sql = null;
		if (SqlUtils.isMySql(this.databaseType)) {
			sql = "USE `" + this.dataBaseName + "`";
		} else if (SqlUtils.isSqlServer(this.databaseType)) {
			sql = "USE [" + this.dataBaseName + "]";
		}
		if (sql != null) {
			Statement st = this.ds.getStatement();
			try {
				st.executeUpdate(sql);
			} finally {
				closeStatment(st);
			}
			return true;
		}
		return false;
	}

	// ── Transaction ──────────────────────────────────────────────────

	public boolean transBegin() {
		try {
			ds.connect();
			this.connection = ds.getConnection();
		} catch (Exception e) {
			this.isTrans = false;
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
		try {
			this.isTrans = true;
			this.connection.setAutoCommit(false);
			LOGGER.debug("Start transaction");
			return true;
		} catch (SQLException e) {
			this.isTrans = false;
			try {
				this.connection.close();
			} catch (SQLException e1) {
				LOGGER.error(e1.getLocalizedMessage());
			}
			LOGGER.error(e.getLocalizedMessage());
			return false;
		}
	}

	public boolean transCommit() {
		try {
			this.connection.commit();
			LOGGER.debug("Commit transaction");
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			close();
			return false;
		} finally {
			this.isTrans = false;
		}
	}

	public void transRollback() {
		try {
			this.connection.rollback();
			LOGGER.debug("Rollback transaction");
		} catch (SQLException e) {
			LOGGER.error(e.getLocalizedMessage());
		} finally {
			close();
		}
	}

	public void transClose() {
		close();
	}

	// ── Statement & ResultSet Management ─────────────────────────────

	public String closeStatment(Statement stmt) {
		if (stmt == null) {
			return null;
		}
		try {
			stmt.close();
			return null;
		} catch (SQLException e) {
			String err = e.getLocalizedMessage();
			LOGGER.error("Close the statement {}", err);
			return err;
		}
	}

	public DataResult addResult(ResultSet rs, String sqlExecute, String sqlOrigin) {
		DataResult dr = new DataResult();
		dr.setIsEof(false);
		dr.setIsNext(false);
		dr.setResultSet(rs);
		dr.setSqlExecute(sqlExecute);
		dr.setSqlOrigin(sqlOrigin);
		this.resultSetList.add(dr);
		return dr;
	}

	public List<DataResult> getMoreResults() throws SQLException {
		if (this.queryStatement == null) {
			return null;
		}
		List<DataResult> lst = new ArrayList<>();
		int inc = 0;
		while (this.queryStatement.getMoreResults()) {
			ResultSet rs = this.queryStatement.getResultSet();
			DataResult dr = addResult(rs, "more", inc + "");
			inc++;
			lst.add(dr);
		}
		return lst;
	}

	public void clearResultSets() {
		if (this.resultSetList == null || this.resultSetList.size() == 0) {
			return;
		}
		for (int i = 0; i < this.resultSetList.size(); i++) {
			DataResult r = (DataResult) resultSetList.get(i);
			try {
				r.getResultSet().close();
			} catch (SQLException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}
		this.resultSetList.clear();
	}

	// ── Lifecycle ────────────────────────────────────────────────────

	public void close() {
		clearResultSets();
		closeStatment(this.pst);
		closeStatment(this.queryStatement);
		if (this.ds != null) {
			this.ds.close();
		}
	}

	// ── Getters / Setters ────────────────────────────────────────────

	public Connection getConnection() {
		if (this.connection == null) {
			this.connection = ds.getConnection();
		}
		return this.connection;
	}

	public void setConnection(Connection cnn) {
		this.connection = cnn;
	}

	public DataHelper getDataHelper() {
		return ds;
	}

	public void setDataHelper(DataHelper dh) {
		this.ds = dh;
	}

	public PreparedStatement getPst() {
		return pst;
	}

	public void setPst(PreparedStatement pst) {
		this.pst = pst;
	}

	public Statement getQueryStatement() {
		return queryStatement;
	}

	public void setQueryStatement(Statement stmt) {
		this.queryStatement = stmt;
	}

	public boolean isTrans() {
		return isTrans;
	}

	public ConnectionConfig getCurrentConfig() {
		return currentConfig;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getDataBaseName() {
		return dataBaseName;
	}

	public void setDataBaseName(String name) {
		this.dataBaseName = name;
	}

	public MList getResultSetList() {
		return resultSetList;
	}

	public DataResult getLastResult() {
		return (DataResult) this.resultSetList.getLast();
	}
}
