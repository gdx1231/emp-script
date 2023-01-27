package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;

/**
 * 根据数据库的类型，表名或字段名的前后缀
 * @author admin
 *
 */
public class FixTableOrField {

	public static FixTableOrField getInstance(String databaseType) {

		boolean isMySql = SqlUtils.isMySql(databaseType);
		boolean isSqlServer = SqlUtils.isSqlServer(databaseType);
		FixTableOrField t = new FixTableOrField();
		if (isSqlServer) {
			t.fixCharBefore = "[";
			t.fixCharAfter = "]";
		} else if (isMySql) {
			t.fixCharBefore = "`";
			t.fixCharAfter = "`";
		}

		return t;
	}

	public static FixTableOrField getInstance(DataConnection cnn) {
		return getInstance(cnn == null ? "" : cnn.getDatabaseType());
	}

	private String fixCharBefore = "";
	private String fixCharAfter = "";

	/**
	 * 表名或字段名的前缀
	 * @return
	 */
	public String getFixCharBefore() {
		return fixCharBefore;
	}

	public void setFixCharBefore(String fixCharBefore) {
		this.fixCharBefore = fixCharBefore;
	}

	/**
	 * 表名或字段名的后缀
	 * @return
	 */
	public String getFixCharAfter() {
		return fixCharAfter;
	}

	public void setFixCharAfter(String fixCharAfter) {
		this.fixCharAfter = fixCharAfter;
	}

}
