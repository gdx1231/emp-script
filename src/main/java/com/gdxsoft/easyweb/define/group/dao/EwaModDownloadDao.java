package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;

/**
 * 表ewa_mod_download操作类
 * 
 * @author gdx 时间：Wed Dec 01 2021 12:37:01 GMT+0800 (中国标准时间)
 */
public class EwaModDownloadDao extends ClassDaoBase<EwaModDownload> implements IClassDao<EwaModDownload> {

	private static String SQL_INSERT = "INSERT INTO ewa_mod_download(mod_code, mod_ver, mod_name, mod_name_en, mod_open_source, mod_company, mod_contact, mod_web, mod_email, mod_osp, mod_memo, mod_memo_en, mod_cdate, mod_mdate, mod_sup_id, mod_ver_id, mod_ver_cdate, mod_ver_mdate, mod_ver_memo, mod_ver_memo_en, mod_ver_status, pkg_len, pkg_md5, pkg_file, mod_dl_cdate, mod_dl_mdate, mod_dl_status, mod_dl_sup_id, mod_dl_url) 	VALUES(@mod_code, @mod_ver, @mod_name, @mod_name_en, @mod_open_source, @mod_company, @mod_contact, @mod_web, @mod_email, @mod_osp, @mod_memo, @mod_memo_en, @mod_cdate, @mod_mdate, @mod_sup_id, @mod_ver_id, @mod_ver_cdate, @mod_ver_mdate, @mod_ver_memo, @mod_ver_memo_en, @mod_ver_status, @pkg_len, @pkg_md5, @pkg_file, @mod_dl_cdate, @mod_dl_mdate, @mod_dl_status, @mod_dl_sup_id, @mod_dl_url)";
	public static String TABLE_NAME = "ewa_mod_download";
	public static String[] KEY_LIST = { "mod_dl_id" };
	public static String[] FIELD_LIST = { "mod_dl_id", "mod_code", "mod_ver", "mod_name", "mod_name_en",
			"mod_open_source", "mod_company", "mod_contact", "mod_web", "mod_email", "mod_osp", "mod_memo",
			"mod_memo_en", "mod_cdate", "mod_mdate", "mod_sup_id", "mod_ver_id", "mod_ver_cdate", "mod_ver_mdate",
			"mod_ver_memo", "mod_ver_memo_en", "mod_ver_status", "pkg_len", "pkg_md5", "pkg_file", "mod_dl_cdate",
			"mod_dl_mdate", "mod_dl_status", "mod_dl_sup_id", "mod_dl_url" };

	public EwaModDownloadDao() {
		// 设置数据库连接配置名称，在 ewa_conf.xml中定义
		// super.setConfigName("ewa");
		super.setInstanceClass(EwaModDownload.class);
		super.setTableName(TABLE_NAME);
		super.setFields(FIELD_LIST);
		super.setKeyFields(KEY_LIST);
		super.setSqlInsert(SQL_INSERT);
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para 表ewa_mod_download的映射类
	 * 
	 * @return 是否成功
	 */

	public boolean newRecord(EwaModDownload para) {

		RequestValue rv = this.createRequestValue(para);

		int autoKey = super.executeUpdateAutoIncrement(SQL_INSERT, rv);
		if (autoKey > 0) {
			para.setModDlId(autoKey);// 自增
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para         表ewa_mod_download的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean newRecord(EwaModDownload para, HashMap<String, Boolean> updateFields) {
		String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		int autoKey = super.executeUpdateAutoIncrement(SQL_INSERT, rv);
		if (autoKey > 0) {
			para.setModDlId(autoKey);
			return true;
		} else {
			return false;
		}
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