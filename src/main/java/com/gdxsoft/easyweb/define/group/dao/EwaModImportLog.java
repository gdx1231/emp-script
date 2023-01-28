package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;


/**
 * 表ewa_mod_import_log映射类
 * 
 * @author gdx 时间：Sat Jan 28 2023 11:43:38 GMT+0800 (中国标准时间)
 */
public class EwaModImportLog extends ClassBase {// 日志编号, INT, length:10, null:false, pk:true
	private Integer logId_;
// 下载序号, INT, length:10, null:true, pk:false
	private Integer modDlId_;
// 模块编码, VARCHAR, length:100, null:true, pk:false
	private String modCode_;
// 模块版本, VARCHAR, length:30, null:true, pk:false
	private String modVer_;
// 模块名称, VARCHAR, length:100, null:true, pk:false
	private String modName_;
// 导入数据的连接名称, VARCHAR, length:50, null:true, pk:false
	private String importDataConn_;
// 替换元数据库名称, VARCHAR, length:64, null:true, pk:false
	private String replaceMetaDatabasename_;
// 替换工作据库名称, VARCHAR, length:64, null:true, pk:false
	private String replaceWorkDatabasename_;
// 开始时间, DATETIME, length:19, null:true, pk:false
	private Date logBegin_;
// 结束时间, DATETIME, length:19, null:true, pk:false
	private Date logEnd_;
// 日志内容, LONGTEXT, length:2147483647, null:true, pk:false
	private String logContent_;
// 日志错误, LONGTEXT, length:2147483647, null:true, pk:false
	private String logErrors_;
// 执行结果记录, LONGTEXT, length:2147483647, null:true, pk:false
	private String logResult_;
// ip地址, VARCHAR, length:40, null:true, pk:false
	private String logIp_;
// UA, VARCHAR, length:500, null:true, pk:false
	private String logUa_;
// referer, VARCHAR, length:1000, null:true, pk:false
	private String logRef_;
// 状态, VARCHAR, length:4, null:true, pk:false
	private String logStatus_;

	/**
	 * 获取 日志编号
	 *
	 * @return 日志编号, INT, length:10, null:false, pk:true
	 */
	public Integer getLogId() {
		return this.logId_;
	}

	/**
	 * 赋值 日志编号
	 * 
	 * @param paraLogId 日志编号, INT, length:10, null:false, pk:true
	 */

	public void setLogId(Integer paraLogId) {
		super.recordChanged("log_id", this.logId_, paraLogId);
		this.logId_ = paraLogId;
	}

	/**
	 * 获取 下载序号
	 *
	 * @return 下载序号, INT, length:10, null:true, pk:false
	 */
	public Integer getModDlId() {
		return this.modDlId_;
	}

	/**
	 * 赋值 下载序号
	 * 
	 * @param paraModDlId 下载序号, INT, length:10, null:true, pk:false
	 */

	public void setModDlId(Integer paraModDlId) {
		super.recordChanged("mod_dl_id", this.modDlId_, paraModDlId);
		this.modDlId_ = paraModDlId;
	}

	/**
	 * 获取 模块编码
	 *
	 * @return 模块编码, VARCHAR, length:100, null:true, pk:false
	 */
	public String getModCode() {
		return this.modCode_;
	}

	/**
	 * 赋值 模块编码
	 * 
	 * @param paraModCode 模块编码, VARCHAR, length:100, null:true, pk:false
	 */

	public void setModCode(String paraModCode) {
		super.recordChanged("mod_code", this.modCode_, paraModCode);
		this.modCode_ = paraModCode;
	}

	/**
	 * 获取 模块版本
	 *
	 * @return 模块版本, VARCHAR, length:30, null:true, pk:false
	 */
	public String getModVer() {
		return this.modVer_;
	}

	/**
	 * 赋值 模块版本
	 * 
	 * @param paraModVer 模块版本, VARCHAR, length:30, null:true, pk:false
	 */

	public void setModVer(String paraModVer) {
		super.recordChanged("mod_ver", this.modVer_, paraModVer);
		this.modVer_ = paraModVer;
	}

	/**
	 * 获取 模块名称
	 *
	 * @return 模块名称, VARCHAR, length:100, null:true, pk:false
	 */
	public String getModName() {
		return this.modName_;
	}

	/**
	 * 赋值 模块名称
	 * 
	 * @param paraModName 模块名称, VARCHAR, length:100, null:true, pk:false
	 */

	public void setModName(String paraModName) {
		super.recordChanged("mod_name", this.modName_, paraModName);
		this.modName_ = paraModName;
	}

	/**
	 * 获取 导入数据的连接名称
	 *
	 * @return 导入数据的连接名称, VARCHAR, length:50, null:true, pk:false
	 */
	public String getImportDataConn() {
		return this.importDataConn_;
	}

	/**
	 * 赋值 导入数据的连接名称
	 * 
	 * @param paraImportDataConn 导入数据的连接名称, VARCHAR, length:50, null:true, pk:false
	 */

	public void setImportDataConn(String paraImportDataConn) {
		super.recordChanged("import_data_conn", this.importDataConn_, paraImportDataConn);
		this.importDataConn_ = paraImportDataConn;
	}

	/**
	 * 获取 替换元数据库名称
	 *
	 * @return 替换元数据库名称, VARCHAR, length:64, null:true, pk:false
	 */
	public String getReplaceMetaDatabasename() {
		return this.replaceMetaDatabasename_;
	}

	/**
	 * 赋值 替换元数据库名称
	 * 
	 * @param paraReplaceMetaDatabasename 替换元数据库名称, VARCHAR, length:64, null:true,
	 *                                    pk:false
	 */

	public void setReplaceMetaDatabasename(String paraReplaceMetaDatabasename) {
		super.recordChanged("replace_meta_databasename", this.replaceMetaDatabasename_, paraReplaceMetaDatabasename);
		this.replaceMetaDatabasename_ = paraReplaceMetaDatabasename;
	}

	/**
	 * 获取 替换工作据库名称
	 *
	 * @return 替换工作据库名称, VARCHAR, length:64, null:true, pk:false
	 */
	public String getReplaceWorkDatabasename() {
		return this.replaceWorkDatabasename_;
	}

	/**
	 * 赋值 替换工作据库名称
	 * 
	 * @param paraReplaceWorkDatabasename 替换工作据库名称, VARCHAR, length:64, null:true,
	 *                                    pk:false
	 */

	public void setReplaceWorkDatabasename(String paraReplaceWorkDatabasename) {
		super.recordChanged("replace_work_databasename", this.replaceWorkDatabasename_, paraReplaceWorkDatabasename);
		this.replaceWorkDatabasename_ = paraReplaceWorkDatabasename;
	}

	/**
	 * 获取 开始时间
	 *
	 * @return 开始时间, DATETIME, length:19, null:true, pk:false
	 */
	public Date getLogBegin() {
		return this.logBegin_;
	}

	/**
	 * 赋值 开始时间
	 * 
	 * @param paraLogBegin 开始时间, DATETIME, length:19, null:true, pk:false
	 */

	public void setLogBegin(Date paraLogBegin) {
		super.recordChanged("log_begin", this.logBegin_, paraLogBegin);
		this.logBegin_ = paraLogBegin;
	}

	/**
	 * 获取 结束时间
	 *
	 * @return 结束时间, DATETIME, length:19, null:true, pk:false
	 */
	public Date getLogEnd() {
		return this.logEnd_;
	}

	/**
	 * 赋值 结束时间
	 * 
	 * @param paraLogEnd 结束时间, DATETIME, length:19, null:true, pk:false
	 */

	public void setLogEnd(Date paraLogEnd) {
		super.recordChanged("log_end", this.logEnd_, paraLogEnd);
		this.logEnd_ = paraLogEnd;
	}

	/**
	 * 获取 日志内容
	 *
	 * @return 日志内容, LONGTEXT, length:2147483647, null:true, pk:false
	 */
	public String getLogContent() {
		return this.logContent_;
	}

	/**
	 * 赋值 日志内容
	 * 
	 * @param paraLogContent 日志内容, LONGTEXT, length:2147483647, null:true, pk:false
	 */

	public void setLogContent(String paraLogContent) {
		super.recordChanged("log_content", this.logContent_, paraLogContent);
		this.logContent_ = paraLogContent;
	}

	/**
	 * 获取 日志错误
	 *
	 * @return 日志错误, LONGTEXT, length:2147483647, null:true, pk:false
	 */
	public String getLogErrors() {
		return this.logErrors_;
	}

	/**
	 * 赋值 日志错误
	 * 
	 * @param paraLogErrors 日志错误, LONGTEXT, length:2147483647, null:true, pk:false
	 */

	public void setLogErrors(String paraLogErrors) {
		super.recordChanged("log_errors", this.logErrors_, paraLogErrors);
		this.logErrors_ = paraLogErrors;
	}

	/**
	 * 获取 执行结果记录
	 *
	 * @return 执行结果记录, LONGTEXT, length:2147483647, null:true, pk:false
	 */
	public String getLogResult() {
		return this.logResult_;
	}

	/**
	 * 赋值 执行结果记录
	 * 
	 * @param paraLogResult 执行结果记录, LONGTEXT, length:2147483647, null:true, pk:false
	 */

	public void setLogResult(String paraLogResult) {
		super.recordChanged("log_result", this.logResult_, paraLogResult);
		this.logResult_ = paraLogResult;
	}

	/**
	 * 获取 ip地址
	 *
	 * @return ip地址, VARCHAR, length:40, null:true, pk:false
	 */
	public String getLogIp() {
		return this.logIp_;
	}

	/**
	 * 赋值 ip地址
	 * 
	 * @param paraLogIp ip地址, VARCHAR, length:40, null:true, pk:false
	 */

	public void setLogIp(String paraLogIp) {
		super.recordChanged("log_ip", this.logIp_, paraLogIp);
		this.logIp_ = paraLogIp;
	}

	/**
	 * 获取 UA
	 *
	 * @return UA, VARCHAR, length:500, null:true, pk:false
	 */
	public String getLogUa() {
		return this.logUa_;
	}

	/**
	 * 赋值 UA
	 * 
	 * @param paraLogUa UA, VARCHAR, length:500, null:true, pk:false
	 */

	public void setLogUa(String paraLogUa) {
		super.recordChanged("log_ua", this.logUa_, paraLogUa);
		this.logUa_ = paraLogUa;
	}

	/**
	 * 获取 referer
	 *
	 * @return referer, VARCHAR, length:1000, null:true, pk:false
	 */
	public String getLogRef() {
		return this.logRef_;
	}

	/**
	 * 赋值 referer
	 * 
	 * @param paraLogRef referer, VARCHAR, length:1000, null:true, pk:false
	 */

	public void setLogRef(String paraLogRef) {
		super.recordChanged("log_ref", this.logRef_, paraLogRef);
		this.logRef_ = paraLogRef;
	}

	/**
	 * 获取 状态
	 *
	 * @return 状态, VARCHAR, length:4, null:true, pk:false
	 */
	public String getLogStatus() {
		return this.logStatus_;
	}

	/**
	 * 赋值 状态
	 * 
	 * @param paraLogStatus 状态, VARCHAR, length:4, null:true, pk:false
	 */

	public void setLogStatus(String paraLogStatus) {
		super.recordChanged("log_status", this.logStatus_, paraLogStatus);
		this.logStatus_ = paraLogStatus;
	}
}
