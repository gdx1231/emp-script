package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;

/**
 * ewa_mod_download
 * 
 * @author gdx date: Sat Jan 28 2023 11:28:31 GMT+0800 (中国标准时间)
 */
public class EwaModDownloadDao extends ClassDaoBase<EwaModDownload> implements IClassDao<EwaModDownload> {
	public final static String TABLE_NAME = "ewa_mod_download";
	public final static String[] KEY_LIST = { "mod_dl_id" /* 下载序号, int identity, length:10, null:false, pk:true */ };
	public final static String[] FIELD_LIST = { "mod_dl_id" /* 下载序号, int identity, length:10, null:false, pk:true */,
			"mod_code" /* 模块编码, nvarchar, length:100, null:false, pk:false */,
			"mod_ver" /* 模块版本, nvarchar, length:30, null:false, pk:false */,
			"mod_name" /* 模块名称, nvarchar, length:100, null:false, pk:false */,
			"mod_name_en" /* 模块名称-英文, nvarchar, length:100, null:true, pk:false */,
			"mod_open_source" /* 是否开源-y/n, char, length:1, null:true, pk:false */,
			"mod_company" /* 公司, nvarchar, length:100, null:true, pk:false */,
			"mod_contact" /* 联系人, nvarchar, length:100, null:true, pk:false */,
			"mod_web" /* 网址, nvarchar, length:200, null:true, pk:false */,
			"mod_email" /* 地址邮件, nvarchar, length:100, null:true, pk:false */,
			"mod_osp" /* 开源协议, nvarchar, length:100, null:true, pk:false */,
			"mod_memo" /* 模块说明, nvarchar, length:2147483647, null:true, pk:false */,
			"mod_memo_en" /* 模块说明英文, nvarchar, length:2147483647, null:true, pk:false */,
			"mod_cdate" /* 创建时间, datetime, length:23, null:true, pk:false */,
			"mod_mdate" /* 修改时间, datetime, length:23, null:true, pk:false */,
			"mod_sup_id" /* 商户, int, length:10, null:true, pk:false */,
			"mod_ver_id" /* 版本编号, bigint, length:19, null:true, pk:false */,
			"mod_ver_cdate" /* 创建时间, datetime, length:23, null:true, pk:false */,
			"mod_ver_mdate" /* 修改时间, datetime, length:23, null:true, pk:false */,
			"mod_ver_memo" /* 模块版本说明, nvarchar, length:2147483647, null:true, pk:false */,
			"mod_ver_memo_en" /* 模块版本说明英文, nvarchar, length:2147483647, null:true, pk:false */,
			"mod_ver_status" /* 模块状态, nvarchar, length:10, null:true, pk:false */,
			"pkg_len" /* 包大小, int, length:10, null:false, pk:false */,
			"pkg_md5" /* 包md5, nvarchar, length:32, null:false, pk:false */,
			"pkg_file" /* 包内容, varbinary, length:2147483647, null:true, pk:false */,
			"mod_dl_cdate" /* 创建时间, datetime, length:23, null:false, pk:false */,
			"mod_dl_mdate" /* 修改时间, datetime, length:23, null:false, pk:false */,
			"mod_dl_status" /* 模块状态- used,del, nvarchar, length:10, null:false, pk:false */,
			"mod_dl_sup_id" /* 商户, int, length:10, null:false, pk:false */,
			"mod_dl_url" /* 下载网址, nvarchar, length:700, null:true, pk:false */,
			"import_data_conn" /* 导入数据的连接名称, nvarchar, length:50, null:true, pk:false */,
			"replace_meta_databasename" /* 替换元数据库名称, nvarchar, length:64, null:true, pk:false */,
			"replace_work_databasename" /* 替换工作据库名称, nvarchar, length:64, null:true, pk:false */ };

	public EwaModDownloadDao() {
		// 设置数据库连接配置名称，在 ewa_conf.xml中定义
		// super.setConfigName("ewa");
		super.setInstanceClass(EwaModDownload.class);
		super.setTableName(TABLE_NAME);
		super.setFields(FIELD_LIST);
		super.setKeyFields(KEY_LIST);
		// 自增字段
		super.setAutoKey("mod_dl_id");
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para 表ewa_mod_download的映射类
	 * @return true/false
	 */
	public boolean newRecord(EwaModDownload para) {
		Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);
		return this.newRecord(para, updateFields);
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para         表ewa_mod_download的映射类
	 * @param updateFields 变化的字段Map
	 * @return true/false
	 */
	public boolean newRecord(EwaModDownload para, Map<String, Boolean> updateFields) {
		String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		int autoKey = super.executeUpdateAutoIncrement(sql, rv);
		if (autoKey > 0) {
			para.setModDlId(autoKey);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新一条记录，全字段
	 * 
	 * @param para 表ewa_mod_download的映射类
	 * @return 是否成功
	 */
	public boolean updateRecord(EwaModDownload para) {

		Map<String, Boolean> updateFields = super.createAllUpdateFields(FIELD_LIST);
		return updateRecord(para, updateFields);
	}

	/**
	 * 更新一条记录，根据类的字段变化
	 * 
	 * @param para         表ewa_mod_download的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean updateRecord(EwaModDownload para, Map<String, Boolean> updateFields) {
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
	 * @param paraModDlId 下载序号
	 * @return 记录类(EwaModDownload)
	 */
	public EwaModDownload getRecord(Integer paraModDlId) {
		RequestValue rv = new RequestValue();
		rv.addValue("MOD_DL_ID", paraModDlId, "Integer", 10);
		String sql = super.getSqlSelect();
		ArrayList<EwaModDownload> al = super.executeQuery(sql, rv, new EwaModDownload(), FIELD_LIST);
		if (al.size() > 0) {
			EwaModDownload o = al.get(0);
			al.clear();
			return o;
		} else {
			return null;
		}
	}

	/**
	 * 根据主键删除一条记录
	 * 
	 * @param paraModDlId 下载序号
	 * @return 是否成功
	 */
	public boolean deleteRecord(Integer paraModDlId) {
		RequestValue rv = new RequestValue();
		rv.addValue("MOD_DL_ID", paraModDlId, "Integer", 10);
		return super.executeUpdate(super.createDeleteSql(), rv);
	}
}