package com.gdxsoft.easyweb.define.group;

import java.io.File;

import org.json.JSONObject;

import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.utils.UJSon;

/**
 * 导入模块
 * @author admin
 *
 */
public class ModuleImport extends ModuleBase {

	private String importDataConn;

	public ModuleImport(String moduleCode, String moduleVersion, String importDataConn, String replaceMetaDatabaseName,
			String replaceWorkDatabaseName) {
		this.moduleCode = moduleCode;
		this.moduleVersion = moduleVersion;
		this.importDataConn = importDataConn;
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;

	}

	public JSONObject importModule(String modulePackageFile) {
		
		File file = new File(modulePackageFile);
		if(!file.exists()) {
			return UJSon.rstFalse("The file "+modulePackageFile + " not exists");
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
