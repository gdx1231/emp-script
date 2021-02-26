package com.gdxsoft.easyweb.data.export;

import java.io.File;
import java.sql.ResultSet;

import com.gdxsoft.easyweb.data.DTTable;

public interface IExport {

	/**
	 * 输出Xls文件
	 * 
	 * @param rs
	 * @param tmpXlsName
	 * @param destXlsName
	 * @throws Exception
	 */
	public abstract File export(ResultSet rs, String destXlsName)
			throws Exception;

	public abstract File export(DTTable table, String destXlsName)
			throws Exception;
}