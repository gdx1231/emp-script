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

		String sql1="update ewa_mod_download set import_data_conn=@import_data_conn, replace_meta_databaseName=@replace_meta_databaseName, replace_work_databaseName=@replace_work_databaseName where mod_dl_id = " + modDlId;
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

		String id = moduleCode + "_" + moduleVersion;

		Exchange ex = new Exchange(id, this.importDataConn);
		ex.setReplaceMetaDatabaseName("visa_main_data");
		ex.setReplaceWorkDatabaseName("visa");

		IUpdateXml ux = ConfigUtils.getDefaultUpdateXml();

		try {
			ex.importGroup(modulePackageFile);
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}

		try {
			ex.importTableAndData();
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}

		ex.importCfgsAutoPath(ux);

		ex.removeImportTempFiles();

		return UJSon.rstTrue(modulePackageFile);
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
}
