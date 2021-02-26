package com.gdxsoft.easyweb.define.database.maps;

import java.util.HashMap;

/**
 * 数据库字段备注信息提取和设置
 * 
 * @author Administrator
 * 
 */
public class MapSqlTemplate {

	private String _DatabaseType;
	private HashMap<String, String> _Templates = new HashMap<String, String>();

	/**
	 * @return the _DatabaseType
	 */
	public String getDatabaseType() {
		return _DatabaseType;
	}

	/**
	 * @param databaseType
	 *            the _DatabaseType to set
	 */
	public void setDatabaseType(String databaseType) {
		_DatabaseType = databaseType;
	}

	/**
	 * 放置模板
	 * @param tag
	 * @param val
	 */
	public void addSqlTemplate(String tag, String val) {
		if (tag == null || tag.trim().length() == 0) {
			return;
		}
		String t = tag.toUpperCase().trim();
		if (this._Templates.containsKey(t)) {
			this._Templates.remove(t);
		}
		this._Templates.put(t, val);
	}

	/**
	 * 根据类型（PrimaryKey, FieldCommentsGet, FieldCommentSet...）获取SQL模板
	 * 
	 * @param tag
	 *            （PrimaryKey, FieldCommentsGet, FieldCommentSet...）
	 * @return
	 */
	public String getSqlTemplate(String tag) {
		if (tag == null || tag.trim().length() == 0) {
			return null;
		}
		String t = tag.toUpperCase().trim();
		if (this._Templates.containsKey(t)) {
			return this._Templates.get(t);
		} else {
			return null;
		}
	}

}
