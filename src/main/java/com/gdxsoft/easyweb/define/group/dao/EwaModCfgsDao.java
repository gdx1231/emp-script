package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** ewa_mod_cfgs
 * @author gdx date: Wed Apr 26 2023 18:08:15 GMT+0800 (中国标准时间) */
public class EwaModCfgsDao extends ClassDaoBase < EwaModCfgs > implements IClassDao < EwaModCfgs > {
    public final static String TABLE_NAME = "ewa_mod_cfgs";
    public final static String[] KEY_LIST = {
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:true */ ,
        "xmlname" /* 配置文件, VARCHAR, length:200, null:false, pk:true */ ,
        "itemname" /* 配置项, VARCHAR, length:100, null:false, pk:true */
    };
    public final static String[] FIELD_LIST = {
        "mod_ver_id" /* 版本编号, BIGINT, length:19, null:false, pk:true */ ,
        "xmlname" /* 配置文件, VARCHAR, length:200, null:false, pk:true */ ,
        "itemname" /* 配置项, VARCHAR, length:100, null:false, pk:true */ ,
        "description" /* 描述, VARCHAR, length:500, null:false, pk:false */ ,
        "emc_cdate" /* 创建数据, DATETIME, length:19, null:false, pk:false */ ,
        "emc_mdate" /* 修改时间, DATETIME, length:19, null:false, pk:false */ ,
        "emc_adm_id" /* 用户, INT, length:10, null:false, pk:false */ ,
        "emc_sup_id" /* 商户, INT, length:10, null:false, pk:false */ ,
        "emc_def_xmlname" /* 默认文件, VARCHAR, length:200, null:false, pk:false */ ,
        "emc_def_itemname" /* 默认配置项名称, VARCHAR, length:100, null:false, pk:false */
    };
    public EwaModCfgsDao() {
        // 设置数据库连接配置名称，在 ewa_conf.xml中定义
        // super.setConfigName("ewa");
        super.setInstanceClass(EwaModCfgs.class);
        super.setTableName(TABLE_NAME);
        super.setFields(FIELD_LIST);
        super.setKeyFields(KEY_LIST);
    }
    /**
     * 生成一条记录
     * @param para  表ewa_mod_cfgs的映射类
     * @return true/false
     */
    public boolean newRecord(EwaModCfgs para) {
        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return this.newRecord(para, updateFields);
    }
    /**
     * 生成一条记录
     * @param para 表ewa_mod_cfgs的映射类
     * @param updateFields 变化的字段Map
     * @return true/false
     */
    public boolean newRecord(EwaModCfgs para, Map < String, Boolean > updateFields) {
        String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
        if (sql == null) { //没有可更新数据
            return false;
        }
        RequestValue rv = this.createRequestValue(para);
        return super.executeUpdate(sql, rv);
    }
    /**
     * 更新一条记录，全字段
     *@param para 表ewa_mod_cfgs的映射类
     *@return 是否成功 
     */
    public boolean updateRecord(EwaModCfgs para) {

        Map < String, Boolean > updateFields = super.createAllUpdateFields(FIELD_LIST);
        return updateRecord(para, updateFields);
    }
    /**
     * 更新一条记录，根据类的字段变化
     * 
     * @param para
     *            表ewa_mod_cfgs的映射类
     * @param updateFields
     *            变化的字段Map
     * @return
     */
    public boolean updateRecord(EwaModCfgs para, Map < String, Boolean > updateFields) {
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
     *@param paraXmlname 配置文件
     *@param paraItemname 配置项
     *@return 记录类(EwaModCfgs)
     */
    public EwaModCfgs getRecord(Long paraModVerId, String paraXmlname, String paraItemname) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_VER_ID", paraModVerId, "Long", 19);
        rv.addValue("XMLNAME", paraXmlname, "String", 200);
        rv.addValue("ITEMNAME", paraItemname, "String", 100);
        String sql = super.getSqlSelect();
        ArrayList < EwaModCfgs > al = super.executeQuery(sql, rv, new EwaModCfgs(), FIELD_LIST);
        if (al.size() > 0) {
            EwaModCfgs o = al.get(0);
            al.clear();
            return o;
        } else {
            return null;
        }
    }
    /**
     * 根据主键删除一条记录
     *@param paraModVerId 版本编号
     *@param paraXmlname 配置文件
     *@param paraItemname 配置项
     *@return 是否成功
     */
    public boolean deleteRecord(Long paraModVerId, String paraXmlname, String paraItemname) {
        RequestValue rv = new RequestValue();
        rv.addValue("MOD_VER_ID", paraModVerId, "Long", 19);
        rv.addValue("XMLNAME", paraXmlname, "String", 200);
        rv.addValue("ITEMNAME", paraItemname, "String", 100);
        return super.executeUpdate(super.createDeleteSql(), rv);
    }
}