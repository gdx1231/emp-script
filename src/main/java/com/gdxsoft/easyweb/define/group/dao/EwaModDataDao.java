package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** ewa_mod_data
 * @author gdx date: Wed Apr 26 2023 18:08:21 GMT+0800 (中国标准时间) */
public class EwaModDataDao extends ClassDaoBase < EwaModData > implements IClassDao < EwaModData > {
    public final static String TABLE_NAME = "ewa_mod_data";
    public final static String[] KEY_LIST = {};
    public final static String[] FIELD_LIST = {
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:false */ ,
        "table_catalog" /* 数据库目录, VARCHAR, length:64, null:false, pk:false */ ,
        "table_schema" /* 数据库, VARCHAR, length:64, null:false, pk:false */ ,
        "table_name" /* 表名称, VARCHAR, length:64, null:false, pk:false */ ,
        "data_index" /* 记录行, INT, length:10, null:false, pk:false */ ,
        "data_row" /* 行记录, LONGTEXT, length:2147483647, null:false, pk:false */
    };
    public EwaModDataDao() {
        // 设置数据库连接配置名称，在 ewa_conf.xml中定义
        // super.setConfigName("ewa");
        super.setInstanceClass(EwaModData.class);
        super.setTableName(TABLE_NAME);
        super.setFields(FIELD_LIST);
        super.setKeyFields(KEY_LIST);
    }
    /**
     * 生成一条记录
     * @param para  表ewa_mod_data的映射类
     * @return true/false
     */
    public boolean newRecord(EwaModData para) {
        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return this.newRecord(para, updateFields);
    }
    /**
     * 生成一条记录
     * @param para 表ewa_mod_data的映射类
     * @param updateFields 变化的字段Map
     * @return true/false
     */
    public boolean newRecord(EwaModData para, Map < String, Boolean > updateFields) {
        String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        return super.executeUpdate(sql, rv);
    }
    /**
     * 更新一条记录，全字段
     *@param para 表ewa_mod_data的映射类
     *@return 是否成功 
     */
    public boolean updateRecord(EwaModData para) {

        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return updateRecord(para, updateFields);
    }
    /**
     * 更新一条记录，根据类的字段变化
     * 
     * @param para
     *            表ewa_mod_data的映射类
     * @param updateFields
     *            变化的字段Map
     * @return
     */
    public boolean updateRecord(EwaModData para, Map < String, Boolean > updateFields) {
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
     *@return 记录类(EwaModData)
     */
    public EwaModData getRecord() {
        RequestValue rv = new RequestValue();
        String sql = super.getSqlSelect();
        ArrayList < EwaModData > al = super.executeQuery(sql, rv, new EwaModData(), FIELD_LIST);
        if (al.size() > 0) {
            EwaModData o = al.get(0);
            al.clear();
            return o;
        } else {
            return null;
        }
    }
    /**
     * 根据主键删除一条记录
     *@return 是否成功
     */
    public boolean deleteRecord() {
        RequestValue rv = new RequestValue();
        return super.executeUpdate(super.createDeleteSql(), rv);
    }
}