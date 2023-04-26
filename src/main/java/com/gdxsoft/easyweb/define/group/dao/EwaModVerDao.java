package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** ewa_mod_ver
 * @author gdx date: Wed Apr 26 2023 18:08:30 GMT+0800 (中国标准时间) */
public class EwaModVerDao extends ClassDaoBase < EwaModVer > implements IClassDao < EwaModVer > {
    public final static String TABLE_NAME = "ewa_mod_ver";
    public final static String[] KEY_LIST = {
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:true */
    };
    public final static String[] FIELD_LIST = {
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:true */ ,
        "mod_code" /* 模块编码, VARCHAR, length:100, null:false, pk:false */ ,
        "mod_ver" /* 模块版本, VARCHAR, length:30, null:false, pk:false */ ,
        "mod_ver_cdate" /* 创建时间, DATETIME, length:19, null:false, pk:false */ ,
        "mod_ver_mdate" /* 修改时间, DATETIME, length:19, null:false, pk:false */ ,
        "mod_ver_memo" /* 模块版本说明, LONGTEXT, length:2147483647, null:true, pk:false */ ,
        "mod_ver_memo_en" /* 模块版本说明英文, LONGTEXT, length:2147483647, null:true, pk:false */ ,
        "mod_ver_status" /* 模块状态- used,del, VARCHAR, length:10, null:false, pk:false */ ,
        "mod_ver_adm_id" /* 用户, INT, length:10, null:false, pk:false */ ,
        "mod_ver_sup_id" /* 商户, INT, length:10, null:false, pk:false */
    };
    public EwaModVerDao() {
        // 设置数据库连接配置名称，在 ewa_conf.xml中定义
        // super.setConfigName("ewa");
        super.setInstanceClass(EwaModVer.class);
        super.setTableName(TABLE_NAME);
        super.setFields(FIELD_LIST);
        super.setKeyFields(KEY_LIST);
    }
    /**
     * 生成一条记录
     * @param para  表ewa_mod_ver的映射类
     * @return true/false
     */
    public boolean newRecord(EwaModVer para) {
        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return this.newRecord(para, updateFields);
    }
    /**
     * 生成一条记录
     * @param para 表ewa_mod_ver的映射类
     * @param updateFields 变化的字段Map
     * @return true/false
     */
    public boolean newRecord(EwaModVer para, Map < String, Boolean > updateFields) {
        String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        return super.executeUpdate(sql, rv);
    }
    /**
     * 更新一条记录，全字段
     *@param para 表ewa_mod_ver的映射类
     *@return 是否成功 
     */
    public boolean updateRecord(EwaModVer para) {

        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return updateRecord(para, updateFields);
    }
    /**
     * 更新一条记录，根据类的字段变化
     * 
     * @param para
     *            表ewa_mod_ver的映射类
     * @param updateFields
     *            变化的字段Map
     * @return
     */
    public boolean updateRecord(EwaModVer para, Map < String, Boolean > updateFields) {
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
     *@param paraModVerId 版本编号
     *@return 记录类(EwaModVer)
     */
    public EwaModVer getRecord(Long paraModVerId) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_VER_ID", paraModVerId, "Long", 19);
        String sql = super.getSqlSelect();
        ArrayList < EwaModVer > al = super.executeQuery(sql, rv, new EwaModVer(), FIELD_LIST);
        if (al.size() > 0) {
            EwaModVer o = al.get(0);
            al.clear();
            return o;
        } else {
            return null;
        }
    }
    /**
     * 根据主键删除一条记录
     *@param paraModVerId 版本编号
     *@return 是否成功
     */
    public boolean deleteRecord(Long paraModVerId) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_VER_ID", paraModVerId, "Long", 19);
        return super.executeUpdate(super.createDeleteSql(), rv);
    }
}