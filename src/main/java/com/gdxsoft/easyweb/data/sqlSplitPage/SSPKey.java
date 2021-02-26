package com.gdxsoft.easyweb.data.sqlSplitPage;

public class SSPKey {

	/**
	 * 字段前缀，例如：A.USR_ID, 则为A
	 */
	private String _Prefix; 
	private String _FieldName;
	private String _AllName;

	/**
	 * @return the _Prefix
	 */
	public String getPrefix() {
		return _Prefix;
	}

	/**
	 * @param prefix
	 *            the _Prefix to set
	 */
	public void setPrefix(String prefix) {
		_Prefix = prefix;
	}

	/**
	 * @return the _FieldName
	 */
	public String getFieldName() {
		return _FieldName;
	}

	/**
	 * @param fieldName
	 *            the _FieldName to set
	 */
	public void setFieldName(String fieldName) {
		_FieldName = fieldName;
	}

	/**
	 * @return the _AllName
	 */
	public String getAllName() {
		return _AllName;
	}

	/**
	 * @param allName
	 *            the _AllName to set
	 */
	public void setAllName(String allName) {
		_AllName = allName.trim();
		String[] names = _AllName.split("\\.");
		if (names.length == 1 || names.length > 2) {
			this._Prefix = "";
			this._FieldName = _AllName;
		} else { // C.USR_ID
			this._Prefix = names[0];
			this._FieldName = names[1];
		}
	}

}
