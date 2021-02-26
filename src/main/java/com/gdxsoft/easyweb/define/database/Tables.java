package com.gdxsoft.easyweb.define.database;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;

import java.sql.ResultSet;

import com.gdxsoft.easyweb.datasource.DataConnection;

public class Tables extends HashMap<String, Table> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6874688571514981545L;

	private ArrayList<String> _TableList;

	private DatabaseMetaData _dmd = null;

	public Tables() {
	}

	public void initTables(String connectionConfigName) {
		DataConnection _InnerDataSource = new DataConnection();
		_InnerDataSource.setConfigName(connectionConfigName);
		String SchemaName = _InnerDataSource.getSchemaName();

		_TableList = new ArrayList<String>();
		try {
			_InnerDataSource.connect();
			this._dmd = _InnerDataSource.getConnection().getMetaData();
			String[] filter = { "TABLE", "VIEW" };
			ResultSet rs = _dmd.getTables(null, SchemaName, null, filter);

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String tableType = rs.getString("TABLE_TYPE");

				Table table = new Table(tableName, SchemaName, "  "
						+ tableType.toUpperCase().trim() + "  ",
						connectionConfigName);
				super.put(tableName, table);
				_TableList.add(tableName);
			}
			rs.close();
		} catch (java.sql.SQLException e) {
			System.err.println(e.getMessage());
		} finally {
			_InnerDataSource.close();
		}

	}

	public ArrayList<String> getTableList() {
		return _TableList;
	}

}
