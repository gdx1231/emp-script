package com.gdxsoft.easyweb.define.database;

import com.gdxsoft.easyweb.datasource.DataConnection;


public class Schema {

	private DataConnection _SqlConnection;

	private String _SchemaName;
	private String _DatabaseType;
	private String _ConnectionConfigName;
	private Tables _Tables;
	public Schema(String connectionConfigName) {
		_ConnectionConfigName = connectionConfigName;
		_SqlConnection = new DataConnection();
		_SqlConnection.setConfigName(connectionConfigName);
		_SchemaName = this._SqlConnection.getSchemaName();
		_DatabaseType = this._SqlConnection.getDatabaseType();
		initObjects();
	}

	private void initObjects() {
		_Tables = new Tables();
		_Tables.initTables(_ConnectionConfigName);
	}

	public String getDatabaseType() {
		return _DatabaseType;
	}

	public String getSchemaName() {
		return _SchemaName;
	}

	public Tables getTables() {
		if (_Tables == null) {
			this.initObjects();
		}
		return _Tables;
	}

	/**
	 * @return the _ConnectionConfigName
	 */
	public String getConnectionConfigName() {
		return _ConnectionConfigName;
	}
}
