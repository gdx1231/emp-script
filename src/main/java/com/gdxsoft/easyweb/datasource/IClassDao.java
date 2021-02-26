package com.gdxsoft.easyweb.datasource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

public interface IClassDao<T> {

	public abstract String getSqlSelect();

	public abstract String getSqlUpdate();

	public abstract String getSqlDelete();

	public abstract String getSqlInsert();

	public abstract String[] getSqlFields();

	public abstract String getErrorMsg();

	public abstract int executeSequence(String seqName) throws SQLException;

	public abstract int executeInt(String sql) throws SQLException;

	public abstract double executeDouble(String sql) throws SQLException;

	public abstract int getRecordCount(String sql);

	public abstract boolean executeUpdate(String sql, RequestValue requestValue);

	public abstract HashMap<String, String> executeProcdure(String procName, RequestValue requestValue);

	public abstract ArrayList<T> executeQuery(String sql, T obj, String[] fieldList);

	public abstract ArrayList<T> executeQuery(String sql, T obj, String[] fieldList, String pkFieldName, int pageSize,
			int currentPage);

	/**
	 * 执行分页并返回DTTable
	 * 
	 * @param sql
	 * @param pkFieldName
	 * @param pageSize
	 * @param currentPage
	 * @throws Exception
	 */
	public abstract DTTable executeQuery(String sql, String pkFieldName, int pageSize, int currentPage)
			throws Exception;

	public abstract ArrayList<T> executeQuery(String sql, RequestValue requestValue, T obj, String[] fieldList);

	/**
	 * @return the _ConfigName
	 */
	public abstract String getConfigName();

	/**
	 * @param configName the _ConfigName to set
	 */
	public abstract void setConfigName(String configName);

	public abstract DataConnection getConn();

	public abstract RequestValue getRv();

	public abstract void setRv(RequestValue rv);
}