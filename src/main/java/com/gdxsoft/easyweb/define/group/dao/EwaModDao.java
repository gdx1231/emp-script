package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** ewa_mod
 * @author gdx date: Wed Apr 26 2023 18:08:03 GMT+0800 (中国标准时间) */
public class EwaModDao extends ClassDaoBase < EwaMod > implements IClassDao < EwaMod > {
    public final static String TABLE_NAME = "ewa_mod";
    public final static String[] KEY_LIST = {
        "mod_code" /* 模块编码, VARCHAR, length:100, null:false, pk:true */
    };
    public final static String[] FIELD_LIST = {
        "mod_code" /* 模块编码, VARCHAR, length:100, null:false, pk:true */ ,
        "mod_name" /* 模块名称, VARCHAR, length:100, null:false, pk:false */ ,
        "mod_name_en" /* 模块名称-英文, VARCHAR, length:100, null:false, pk:false */ ,
        "mod_open_source" /* 是否开源-y/n, CHAR, length:1, null:false, pk:false */ ,
        "mod_company" /* 公司, VARCHAR, length:100, null:true, pk:false */ ,
        "mod_contact" /* 联系人, VARCHAR, length:100, null:true, pk:false */ ,
        "mod_web" /* 网址, VARCHAR, length:200, null:true, pk:false */ ,
        "mod_email" /* 地址邮件, VARCHAR, length:100, null:true, pk:false */ ,
        "mod_osp" /* 开源协议, VARCHAR, length:100, null:true, pk:false */ ,
        "mod_memo" /* 模块说明, LONGTEXT, length:2147483647, null:true, pk:false */ ,
        "mod_memo_en" /* 模块说明英文, LONGTEXT, length:2147483647, null:true, pk:false */ ,
        "mod_cdate" /* 创建时间, DATETIME, length:19, null:false, pk:false */ ,
        "mod_mdate" /* 修改时间, DATETIME, length:19, null:false, pk:false */ ,
        "mod_status" /* 模块状态- new,dlv,undlv,del, VARCHAR, length:10, null:false, pk:false */ ,
        "mod_adm_id" /* 用户, INT, length:10, null:false, pk:false */ ,
        "mod_sup_id" /* 商户, INT, length:10, null:false, pk:false */ ,
        "mod_meta_db_name" /* 元数据库名称, VARCHAR, length:64, null:true, pk:false */ ,
        "mod_work_db_name" /* 工作数据库名称, VARCHAR, length:64, null:true, pk:false */ ,
        "mod_ewa_conn" /* ewa数据库连接池, VARCHAR, length:67, null:false, pk:false */
    };
    public EwaModDao() {
        // 设置数据库连接配置名称，在 ewa_conf.xml中定义
        // super.setConfigName("ewa");
        super.setInstanceClass(EwaMod.class);
        super.setTableName(TABLE_NAME);
        super.setFields(FIELD_LIST);
        super.setKeyFields(KEY_LIST);
    }
    /**
     * 生成一条记录
     * @param para  表ewa_mod的映射类
     * @return true/false
     */
    public boolean newRecord(EwaMod para) {
        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return this.newRecord(para, updateFields);
    }
    /**
     * 生成一条记录
     * @param para 表ewa_mod的映射类
     * @param updateFields 变化的字段Map
     * @return true/false
     */
    public boolean newRecord(EwaMod para, Map < String, Boolean > updateFields) {
        String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        return super.executeUpdate(sql, rv);
    }
    /**
     * 更新一条记录，全字段
     *@param para 表ewa_mod的映射类
     *@return 是否成功 
     */
    public boolean updateRecord(EwaMod para) {

        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return updateRecord(para, updateFields);
    }
    /**
     * 更新一条记录，根据类的字段变化
     * 
     * @param para
     *            表ewa_mod的映射类
     * @param updateFields
     *            变化的字段Map
     * @return
     */
    public boolean updateRecord(EwaMod para, Map < String, Boolean > updateFields) {
        // 没定义主键的话不能更新
        if (KEY_LIST.length == 0) {
            return false;
        }
        String sql = super.sqlUpdateChanged(TABLE_NAME, KEY_LIST, updateFields);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        return super.executeUpdate(sql, rv);
    }
    /**
     * 根据主键返回一条记录
     *@param paraModCode 模块编码
     *@return 记录类(EwaMod)
     */
    public EwaMod getRecord(String paraModCode) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_CODE", paraModCode, "String", 100);
        String sql = super.getSqlSelect();
        ArrayList < EwaMod > al = super.executeQuery(sql, rv, new EwaMod(), FIELD_LIST);
        if (al.size() > 0) {
            EwaMod o = al.get(0);
            al.clear();
            return o;
        } else {
            return null;
        }
    }
    /**
     * 根据主键删除一条记录
     *@param paraModCode 模块编码
     *@return 是否成功
     */
    public boolean deleteRecord(String paraModCode) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_CODE", paraModCode, "String", 100);
        return super.executeUpdate(super.createDeleteSql(), rv);
    }
}