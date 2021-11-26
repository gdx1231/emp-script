package com.gdxsoft.easyweb.define.group;

public class ModuleBase {

	String moduleCode; // The module's code e.g. com.gdxsoft.backAdmin.menu
	String moduleVersion; // The module's version

	String replaceMetaDatabaseName; // DDL replace meta database name, e.g. `main_data_db`
	String replaceWorkDatabaseName; // DDL replace work database name, e.g. `work_db`

	/**
	 * @return the moduleCode
	 */
	public String getModuleCode() {
		return moduleCode;
	}

	/**
	 * @param moduleCode the moduleCode to set
	 */
	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	/**
	 * @return the moduleVersion
	 */
	public String getModuleVersion() {
		return moduleVersion;
	}

	/**
	 * @param moduleVersion the moduleVersion to set
	 */
	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	/**
	 * DDL replace meta database name, e.g. `main_data_db`
	 * 
	 * @return the replaceMetaDatabaseName
	 */
	public String getReplaceMetaDatabaseName() {
		return replaceMetaDatabaseName;
	}

	/**
	 * DDL replace meta database name, e.g. `main_data_db`
	 * 
	 * @param replaceMetaDatabaseName the replaceMetaDatabaseName to set
	 */
	public void setReplaceMetaDatabaseName(String replaceMetaDatabaseName) {
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
	}

	/**
	 * DDL replace work database name, e.g. `work_db`
	 * 
	 * @return the replaceWorkDatabaseName
	 */
	public String getReplaceWorkDatabaseName() {
		return replaceWorkDatabaseName;
	}

	/**
	 * DDL replace work database name, e.g. `work_db`
	 * 
	 * @param replaceWorkDatabaseName the replaceWorkDatabaseName to set
	 */
	public void setReplaceWorkDatabaseName(String replaceWorkDatabaseName) {
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;
	}

}
