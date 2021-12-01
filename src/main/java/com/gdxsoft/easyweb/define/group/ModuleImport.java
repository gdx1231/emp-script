package com.gdxsoft.easyweb.define.group;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;

/**
 * 导入模块
 * 
 * @author admin
 *
 */
public class ModuleImport extends ModuleBase {

	private String importDataConn;
	private String jdbcConfigName;

	private boolean importTables; // 导入表
	private boolean importData; // 导入数据
	private boolean importXItems; // 导入配置项

	public ModuleImport(String moduleCode, String moduleVersion, String importDataConn, String replaceMetaDatabaseName,
			String replaceWorkDatabaseName) {
		this.moduleCode = moduleCode;
		this.moduleVersion = moduleVersion;
		this.importDataConn = importDataConn;
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;

		ConfScriptPaths.getInstance().getLst().forEach(conf -> {
			if (this.jdbcConfigName == null && conf.isJdbc()) {
				// 获取第一个jdbc连接池
				this.jdbcConfigName = conf.getJdbcConfigName();
			}
		});

	}

	public ModuleImport(String importDataConn, String replaceMetaDatabaseName, String replaceWorkDatabaseName) {
		this.importDataConn = importDataConn;
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;

		ConfScriptPaths.getInstance().getLst().forEach(conf -> {
			if (this.jdbcConfigName == null && conf.isJdbc()) {
				// 获取第一个jdbc连接池
				this.jdbcConfigName = conf.getJdbcConfigName();
			}
		});
	}

	public JSONObject importModuleFromDownloadModule(int modDlId, RequestValue rv) {
		String sql = "select * from ewa_mod_download where mod_dl_id = " + modDlId;
		DTTable tb = DTTable.getJdbcTable(sql, this.jdbcConfigName);
		if (tb.getCount() == 0) {
			return UJSon.rstFalse("No data");
		}

		String sql1 = "update ewa_mod_download set import_data_conn=@import_data_conn, replace_meta_databaseName=@replace_meta_databaseName, replace_work_databaseName=@replace_work_databaseName where mod_dl_id = "
				+ modDlId;
		DataConnection.updateAndClose(sql1, this.jdbcConfigName, rv);

		File tempFile;
		try {
			tempFile = File.createTempFile("importModuleFromDownloadModule", ".zip");
		} catch (IOException e) {
			return UJSon.rstFalse(e.getMessage());
		}
		byte[] buf;
		try {
			buf = (byte[]) tb.getCell(0, "pkg_file").getValue();
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}
		try {
			UFile.createBinaryFile(tempFile.getAbsolutePath(), buf, true);
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}

		return this.importModule(tempFile.getAbsolutePath());

	}

	public JSONObject importModule(String modulePackageFile) {

		File file = new File(modulePackageFile);
		if (!file.exists()) {
			return UJSon.rstFalse("The file " + modulePackageFile + " not exists");
		}

		JSONObject result = UJSon.rstTrue();

		String id = moduleCode + "_" + moduleVersion;
		result.put(id, id);
		result.put("replaceMetaDatabaseName", replaceMetaDatabaseName);
		result.put("replaceWorkDatabaseName", replaceWorkDatabaseName);
		result.put("modulePackageFile", modulePackageFile);
		result.put("importXItems", importXItems);
		result.put("importTables", importTables);
		result.put("importData", importData);
		result.put("importDataConn", importDataConn);
		
		Exchange ex = new Exchange(id, this.importDataConn);
		ex.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
		ex.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);

		try {
			ex.importGroup(modulePackageFile);
		} catch (Exception e) {
			ex.removeImportTempFiles();
			UJSon.rstSetFalse(result, e.getMessage());
			return result;
		}
		if (this.importXItems) {
			IUpdateXml ux = ConfigUtils.getDefaultUpdateXml();
			JSONObject importResult = ex.importCfgsAutoPath(ux);
			result.put("cfgs", importResult);
		}

		try {
			String errorMsgs = ex.importTableAndData(this.importTables, this.importData);
			result.put("importDataOrTableError", errorMsgs);
		} catch (Exception e) {
			ex.removeImportTempFiles();
			UJSon.rstSetFalse(result, e.getMessage());
			return result;
		}

		ex.removeImportTempFiles();

		return result;
	}

	/**
	 * 导入数据用的数据库连接池
	 * 
	 * @return the importDataConn
	 */
	public String getImportDataConn() {
		return importDataConn;
	}

	/**
	 * 导入数据用的数据库连接池
	 * 
	 * @param importDataConn the importDataConn to set
	 */
	public void setImportDataConn(String importDataConn) {
		this.importDataConn = importDataConn;
	}

	/**
	 * @return the jdbcConfigName
	 */
	public String getJdbcConfigName() {
		return jdbcConfigName;
	}

	/**
	 * @param jdbcConfigName the jdbcConfigName to set
	 */
	public void setJdbcConfigName(String jdbcConfigName) {
		this.jdbcConfigName = jdbcConfigName;
	}

	/**
	 * @return the importTables
	 */
	public boolean isImportTables() {
		return importTables;
	}

	/**
	 * @param importTables the importTables to set
	 */
	public void setImportTables(boolean importTables) {
		this.importTables = importTables;
	}

	/**
	 * @return the importData
	 */
	public boolean isImportData() {
		return importData;
	}

	/**
	 * @param importData the importData to set
	 */
	public void setImportData(boolean importData) {
		this.importData = importData;
	}

	/**
	 * @return the importXItems
	 */
	public boolean isImportXItems() {
		return importXItems;
	}

	/**
	 * @param importXItems the importXItems to set
	 */
	public void setImportXItems(boolean importXItems) {
		this.importXItems = importXItems;
	}
}
