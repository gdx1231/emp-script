package com.gdxsoft.easyweb.datasource;

import java.sql.ResultSet;

public class DataResult {

	private boolean _IsNext;
	private boolean _IsEof;
	private ResultSet _ResultSet;
	private String _SqlExecute;
	private String _SqlOrigin;
	
	/**
	 * @return the _IsNext
	 */
	public boolean isNext() {
		return _IsNext;
	}

	/**
	 * @param isNext
	 *            the _IsNext to set
	 */
	public void setIsNext(boolean isNext) {
		_IsNext = isNext;
	}

	/**
	 * @return the _IsEof
	 */
	public boolean isEof() {
		return _IsEof;
	}

	/**
	 * @param isEof
	 *            the _IsEof to set
	 */
	public void setIsEof(boolean isEof) {
		_IsEof = isEof;
	}

	/**
	 * @return the _ResultSet
	 */
	public ResultSet getResultSet() {
		return _ResultSet;
	}

	/**
	 * @param resultSet
	 *            the _ResultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		_ResultSet = resultSet;
	}

	/**
	 * @return the _SqlExecute
	 */
	public String getSqlExecute() {
		return _SqlExecute;
	}

	/**
	 * @param sqlExecute the _SqlExecute to set
	 */
	public void setSqlExecute(String sqlExecute) {
		_SqlExecute = sqlExecute;
	}

	/**
	 * @return the _SqlOrigin
	 */
	public String getSqlOrigin() {
		return _SqlOrigin;
	}

	/**
	 * @param sqlOrigin the _SqlOrigin to set
	 */
	public void setSqlOrigin(String sqlOrigin) {
		_SqlOrigin = sqlOrigin;
	}

	 

}
