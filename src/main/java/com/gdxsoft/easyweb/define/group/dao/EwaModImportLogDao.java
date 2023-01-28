package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;

/**
 * ewa_mod_import_log
 * 
 * @author gdx date: Sat Jan 28 2023 11:44:20 GMT+0800 (中国标准时间)
 */
public class EwaModImportLogDao extends ClassDaoBase<EwaModImportLog> implements IClassDao<EwaModImportLog> {
	public final static String TABLE_NAME = "ewa_mod_import_log";
	public final static String[] KEY_LIST = { "log_id" /* 日志编号, INT, length:10, null:false, pk:true */ };
	public final static String[] FIELD_LIST = { "log_id" /* 日志编号, INT, length:10, null:false, pk:true */,
			"mod_dl_id" /* 下载序号, INT, length:10, null:true, pk:false */,
			"mod_code" /* 模块编码, VARCHAR, length:100, null:true, pk:false */,
			"mod_ver" /* 模块版本, VARCHAR, length:30, null:true, pk:false */,
			"mod_name" /* 模块名称, VARCHAR, length:100, null:true, pk:false */,
			"import_data_conn" /* 导入数据的连接名称, VARCHAR, length:50, null:true, pk:false */,
			"replace_meta_databasename" /* 替换元数据库名称, VARCHAR, length:64, null:true, pk:false */,
			"replace_work_databasename" /* 替换工作据库名称, VARCHAR, length:64, null:true, pk:false */,
			"log_begin" /* 开始时间, DATETIME, length:19, null:true, pk:false */,
			"log_end" /* 结束时间, DATETIME, length:19, null:true, pk:false */,
			"log_content" /* 日志内容, LONGTEXT, length:2147483647, null:true, pk:false */,
			"log_errors" /* 日志错误, LONGTEXT, length:2147483647, null:true, pk:false */,
			"log_result" /* 执行结果记录, LONGTEXT, length:2147483647, null:true, pk:false */,
			"log_ip" /* ip地址, VARCHAR, length:40, null:true, pk:false */,
			"log_ua" /* UA, VARCHAR, length:500, null:true, pk:false */,
			"log_ref" /* referer, VARCHAR, length:1000, null:true, pk:false */,
			"log_status" /* 状态, VARCHAR, length:4, null:true, pk:false */ };

	public EwaModImportLogDao() {
		// 设置数据库连接配置名称，在 ewa_conf.xml中定义
		// super.setConfigName("gyap");
		super.setInstanceClass(EwaModImportLog.class);
		super.setTableName(TABLE_NAME);
		super.setFields(FIELD_LIST);
		super.setKeyFields(KEY_LIST);
		// 自增字段
		super.setAutoKey("log_id");
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para 表ewa_mod_import_log的映射类
	 * @return true/false
	 */
	public boolean newRecord(EwaModImportLog para) {
		Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);
		return this.newRecord(para, updateFields);
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para         表ewa_mod_import_log的映射类
	 * @param updateFields 变化的字段Map
	 * @return true/false
	 */
	public boolean newRecord(EwaModImportLog para, Map<String, Boolean> updateFields) {
		String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		int autoKey = super.executeUpdateAutoIncrement(sql, rv);
		if (autoKey > 0) {
			para.setLogId(autoKey);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新一条记录，全字段
	 * 
	 * @param para 表ewa_mod_import_log的映射类
	 * @return 是否成功
	 */
	public boolean updateRecord(EwaModImportLog para) {

		Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);
		return updateRecord(para, updateFields);
	}

	/**
	 * 更新一条记录，根据类的字段变化
	 * 
	 * @param para         表ewa_mod_import_log的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean updateRecord(EwaModImportLog para, Map<String, Boolean> updateFields) {
		// 没定义主键的话不能更新
		if (KEY_LIST.length == 0) {
			return false;
		}
		String sql = super.sqlUpdateChanged(TABLE_NAME, KEY_LIST, updateFields);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		return super.executeUpdate(sql, rv);
	}

	/**
	 * 根据主键返回一条记录
	 * 
	 * @param paraLogId 日志编号
	 * @return 记录类(EwaModImportLog)
	 */
	public EwaModImportLog getRecord(Integer paraLogId) {
		RequestValue rv = new RequestValue();
		rv.addValue("LOG_ID", paraLogId, "Integer", 10);
		String sql = super.getSqlSelect();
		ArrayList<EwaModImportLog> al = super.executeQuery(sql, rv, new EwaModImportLog(), FIELD_LIST);
		if (al.size() > 0) {
			EwaModImportLog o = al.get(0);
			al.clear();
			return o;
		} else {
			return null;
		}
	}

	/**
	 * 根据主键删除一条记录
	 * 
	 * @param paraLogId 日志编号
	 * @return 是否成功
	 */
	public boolean deleteRecord(Integer paraLogId) {
		RequestValue rv = new RequestValue();
		rv.addValue("LOG_ID", paraLogId, "Integer", 10);
		return super.executeUpdate(super.createDeleteSql(), rv);
	}
}