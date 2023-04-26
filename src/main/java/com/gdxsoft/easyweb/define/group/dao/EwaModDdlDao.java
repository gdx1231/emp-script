package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** ewa_mod_ddl
 * @author gdx date: Wed Apr 26 2023 18:47:13 GMT+0800 (中国标准时间) */
public class EwaModDdlDao extends ClassDaoBase < EwaModDdl > implements IClassDao < EwaModDdl > {
    public final static String TABLE_NAME = "ewa_mod_ddl";
    public final static String[] KEY_LIST = {
        "emd_id" /* 编号, BIGINT, length:19, null:false, pk:true */
    };
    public final static String[] FIELD_LIST = {
        "emd_id" /* 编号, BIGINT, length:19, null:false, pk:true */ ,
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:false */ ,
        "emd_ewa_conn" /* 数据库链接名称, VARCHAR, length:40, null:false, pk:false */ ,
        "table_name" /* 表名称, VARCHAR, length:64, null:false, pk:false */ ,
        "table_catalog" /* 数据库目录, VARCHAR, length:64, null:false, pk:false */ ,
        "table_schema" /* 数据库, VARCHAR, length:64, null:false, pk:false */ ,
        "emd_database_type" /* 表类型, VARCHAR, length:20, null:false, pk:false */ ,
        "emd_type" /* 表类型, VARCHAR, length:20, null:false, pk:false */ ,
        "emd_ddl_sql" /* 创建用的 sql, LONGTEXT, length:2147483647, null:true, pk:false */ ,
        "emd_export" /* 是否导出数据, VARCHAR, length:10, null:false, pk:false */ ,
        "emd_export_where" /* 导出数据条件, VARCHAR, length:2500, null:false, pk:false */ ,
        "emd_cdate" /* 创建时间, DATETIME, length:19, null:false, pk:false */ ,
        "emd_mdate" /* 修改时间, DATETIME, length:19, null:false, pk:false */ ,
        "emd_adm_id" /* 用户, INT, length:10, null:false, pk:false */ ,
        "emd_sup_id" /* 商户, INT, length:10, null:false, pk:false */ ,
        "emd_xml" /* 导出的xml格式, LONGTEXT, length:2147483647, null:true, pk:false */
    };
    public EwaModDdlDao() {
        // 设置数据库连接配置名称，在 ewa_conf.xml中定义
        // super.setConfigName("ewa");
        super.setInstanceClass(EwaModDdl.class);
        super.setTableName(TABLE_NAME);
        super.setFields(FIELD_LIST);
        super.setKeyFields(KEY_LIST);
        // 自增字段 
        super.setAutoKey("emd_id");
    }
    /**
     * 生成一条记录
     * @param para  表ewa_mod_ddl的映射类
     * @return true/false
     */
    public boolean newRecord(EwaModDdl para) {
        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return this.newRecord(para, updateFields);
    }
    /**
     * 生成一条记录
     * @param para 表ewa_mod_ddl的映射类
     * @param updateFields 变化的字段Map
     * @return true/false
     */
    public boolean newRecord(EwaModDdl para, Map < String, Boolean > updateFields) {
        String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        long autoKey = super.executeUpdateAutoIncrementLong(sql, rv);
        if (autoKey > 0) {
            para.setEmdId(autoKey);
            return true;
        } else {
            return false;
        }
    }
    /**
     * 更新一条记录，全字段
     *@param para 表ewa_mod_ddl的映射类
     *@return 是否成功 
     */
    public boolean updateRecord(EwaModDdl para) {

        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return updateRecord(para, updateFields);
    }
    /**
     * 更新一条记录，根据类的字段变化
     * 
     * @param para
     *            表ewa_mod_ddl的映射类
     * @param updateFields
     *            变化的字段Map
     * @return
     */
    public boolean updateRecord(EwaModDdl para, Map < String, Boolean > updateFields) {
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
     *@param paraEmdId 编号
     *@return 记录类(EwaModDdl)
     */
    public EwaModDdl getRecord(Long paraEmdId) {
        RequestValue rv = new RequestValue();
        rv.addValue("EMD_ID", paraEmdId, "Long", 19);
        String sql = super.getSqlSelect();
        ArrayList < EwaModDdl > al = super.executeQuery(sql, rv, new EwaModDdl(), FIELD_LIST);
        if (al.size() > 0) {
            EwaModDdl o = al.get(0);
            al.clear();
            return o;
        } else {
            return null;
        }
    }
    /**
     * 根据主键删除一条记录
     *@param paraEmdId 编号
     *@return 是否成功
     */
    public boolean deleteRecord(Long paraEmdId) {
        RequestValue rv = new RequestValue();
        rv.addValue("EMD_ID", paraEmdId, "Long", 19);
        return super.executeUpdate(super.createDeleteSql(), rv);
    }
}