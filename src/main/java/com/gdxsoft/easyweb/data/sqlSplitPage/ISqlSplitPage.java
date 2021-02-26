package com.gdxsoft.easyweb.data.sqlSplitPage;

public interface ISqlSplitPage {

	public abstract String getSplitSql(int pageCur,int pageSize, String sql, String keys); 
}
