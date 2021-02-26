package com.gdxsoft.easyweb.data.sqlSplitPage;

public class SSPBase {

	private int _PageCur;
	private int _PageSize;
	private String _SqlOri;
	private SSPKey[] _Keys;

	void setKeys(String keys) {
		String[] pks = keys.split(",");
		_Keys = new SSPKey[pks.length];
		for (int i = 0; i < pks.length; i++) {
			_Keys[i] = new SSPKey();
			_Keys[i].setAllName(pks[i]);
		}
	}

	void setValue(int pageCur, int pageSize, String sql, String keys) {
		this._PageCur = pageCur;
		this._PageSize = pageSize;
		this._SqlOri = sql;
		this.setKeys(keys);
	}

	/**
	 * @return the _PageCur
	 */
	public int getPageCur() {
		return _PageCur;
	}

	/**
	 * @param pageCur
	 *            the _PageCur to set
	 */
	public void setPageCur(int pageCur) {
		_PageCur = pageCur;
	}

	/**
	 * @return the _PageSize
	 */
	public int getPageSize() {
		return _PageSize;
	}

	/**
	 * @param pageSize
	 *            the _PageSize to set
	 */
	public void setPageSize(int pageSize) {
		_PageSize = pageSize;
	}

	/**
	 * @return the _SqlOri
	 */
	public String getSqlOri() {
		return _SqlOri;
	}

	/**
	 * @param sqlOri
	 *            the _SqlOri to set
	 */
	public void setSqlOri(String sqlOri) {
		_SqlOri = sqlOri;
	}

	/**
	 * @return the _Keys
	 */
	public SSPKey[] getKeys() {
		return _Keys;
	}

}
