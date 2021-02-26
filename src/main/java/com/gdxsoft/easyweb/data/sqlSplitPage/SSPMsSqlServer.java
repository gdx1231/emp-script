package com.gdxsoft.easyweb.data.sqlSplitPage;

import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class SSPMsSqlServer extends SSPBase implements ISqlSplitPage {

	public String getSplitSql(int pageCur, int pageSize, String sql, String keys) {
		super.setValue(pageCur, pageSize, sql, keys);

		return null;
	}

	 
}
