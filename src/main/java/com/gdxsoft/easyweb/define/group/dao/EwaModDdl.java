package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod_ddl映射类
 * @author gdx 时间：Wed Apr 26 2023 18:47:11 GMT+0800 (中国标准时间)*/
public class EwaModDdl extends ClassBase { // 编号, BIGINT, length:19, null:false, pk:true
    private Long emdId_;
    // 版本编号, BIGINT, length:19, null:false, pk:false
    private Long modVerId_;
    // 数据库链接名称, VARCHAR, length:40, null:false, pk:false
    private String emdEwaConn_;
    // 表名称, VARCHAR, length:64, null:false, pk:false
    private String tableName_;
    // 数据库目录, VARCHAR, length:64, null:false, pk:false
    private String tableCatalog_;
    // 数据库, VARCHAR, length:64, null:false, pk:false
    private String tableSchema_;
    // 表类型, VARCHAR, length:20, null:false, pk:false
    private String emdDatabaseType_;
    // 表类型, VARCHAR, length:20, null:false, pk:false
    private String emdType_;
    // 创建用的 sql, LONGTEXT, length:2147483647, null:true, pk:false
    private String emdDdlSql_;
    // 是否导出数据, VARCHAR, length:10, null:false, pk:false
    private String emdExport_;
    // 导出数据条件, VARCHAR, length:2500, null:false, pk:false
    private String emdExportWhere_;
    // 创建时间, DATETIME, length:19, null:false, pk:false
    private Date emdCdate_;
    // 修改时间, DATETIME, length:19, null:false, pk:false
    private Date emdMdate_;
    // 用户, INT, length:10, null:false, pk:false
    private Integer emdAdmId_;
    // 商户, INT, length:10, null:false, pk:false
    private Integer emdSupId_;
    // 导出的xml格式, LONGTEXT, length:2147483647, null:true, pk:false
    private String emdXml_;

    /**
     * 获取 编号
     *
     * @return 编号, BIGINT, length:19, null:false, pk:true
     */
    public Long getEmdId() {
        return this.emdId_;
    }
    /**
    * 赋值 编号

    * @param paraEmdId
    * 编号, BIGINT, length:19, null:false, pk:true
     */

    public void setEmdId(Long paraEmdId) {
        super.recordChanged("emd_id", this.emdId_, paraEmdId);
        this.emdId_ = paraEmdId;
    }


    /**
     * 获取 版本编号
     *
     * @return 版本编号, BIGINT, length:19, null:false, pk:false
     */
    public Long getModVerId() {
        return this.modVerId_;
    }
    /**
    * 赋值 版本编号

    * @param paraModVerId
    * 版本编号, BIGINT, length:19, null:false, pk:false
     */

    public void setModVerId(Long paraModVerId) {
        super.recordChanged("mod_ver_id", this.modVerId_, paraModVerId);
        this.modVerId_ = paraModVerId;
    }


    /**
     * 获取 数据库链接名称
     *
     * @return 数据库链接名称, VARCHAR, length:40, null:false, pk:false
     */
    public String getEmdEwaConn() {
        return this.emdEwaConn_;
    }
    /**
    * 赋值 数据库链接名称

    * @param paraEmdEwaConn
    * 数据库链接名称, VARCHAR, length:40, null:false, pk:false
     */

    public void setEmdEwaConn(String paraEmdEwaConn) {
        super.recordChanged("emd_ewa_conn", this.emdEwaConn_, paraEmdEwaConn);
        this.emdEwaConn_ = paraEmdEwaConn;
    }


    /**
     * 获取 表名称
     *
     * @return 表名称, VARCHAR, length:64, null:false, pk:false
     */
    public String getTableName() {
        return this.tableName_;
    }
    /**
    * 赋值 表名称

    * @param paraTableName
    * 表名称, VARCHAR, length:64, null:false, pk:false
     */

    public void setTableName(String paraTableName) {
        super.recordChanged("table_name", this.tableName_, paraTableName);
        this.tableName_ = paraTableName;
    }


    /**
     * 获取 数据库目录
     *
     * @return 数据库目录, VARCHAR, length:64, null:false, pk:false
     */
    public String getTableCatalog() {
        return this.tableCatalog_;
    }
    /**
    * 赋值 数据库目录

    * @param paraTableCatalog
    * 数据库目录, VARCHAR, length:64, null:false, pk:false
     */

    public void setTableCatalog(String paraTableCatalog) {
        super.recordChanged("table_catalog", this.tableCatalog_, paraTableCatalog);
        this.tableCatalog_ = paraTableCatalog;
    }


    /**
     * 获取 数据库
     *
     * @return 数据库, VARCHAR, length:64, null:false, pk:false
     */
    public String getTableSchema() {
        return this.tableSchema_;
    }
    /**
    * 赋值 数据库

    * @param paraTableSchema
    * 数据库, VARCHAR, length:64, null:false, pk:false
     */

    public void setTableSchema(String paraTableSchema) {
        super.recordChanged("table_schema", this.tableSchema_, paraTableSchema);
        this.tableSchema_ = paraTableSchema;
    }


    /**
     * 获取 表类型
     *
     * @return 表类型, VARCHAR, length:20, null:false, pk:false
     */
    public String getEmdDatabaseType() {
        return this.emdDatabaseType_;
    }
    /**
    * 赋值 表类型

    * @param paraEmdDatabaseType
    * 表类型, VARCHAR, length:20, null:false, pk:false
     */

    public void setEmdDatabaseType(String paraEmdDatabaseType) {
        super.recordChanged("emd_database_type", this.emdDatabaseType_, paraEmdDatabaseType);
        this.emdDatabaseType_ = paraEmdDatabaseType;
    }


    /**
     * 获取 表类型
     *
     * @return 表类型, VARCHAR, length:20, null:false, pk:false
     */
    public String getEmdType() {
        return this.emdType_;
    }
    /**
    * 赋值 表类型

    * @param paraEmdType
    * 表类型, VARCHAR, length:20, null:false, pk:false
     */

    public void setEmdType(String paraEmdType) {
        super.recordChanged("emd_type", this.emdType_, paraEmdType);
        this.emdType_ = paraEmdType;
    }


    /**
     * 获取 创建用的 sql
     *
     * @return 创建用的 sql, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getEmdDdlSql() {
        return this.emdDdlSql_;
    }
    /**
    * 赋值 创建用的 sql

    * @param paraEmdDdlSql
    * 创建用的 sql, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setEmdDdlSql(String paraEmdDdlSql) {
        super.recordChanged("emd_ddl_sql", this.emdDdlSql_, paraEmdDdlSql);
        this.emdDdlSql_ = paraEmdDdlSql;
    }


    /**
     * 获取 是否导出数据
     *
     * @return 是否导出数据, VARCHAR, length:10, null:false, pk:false
     */
    public String getEmdExport() {
        return this.emdExport_;
    }
    /**
    * 赋值 是否导出数据

    * @param paraEmdExport
    * 是否导出数据, VARCHAR, length:10, null:false, pk:false
     */

    public void setEmdExport(String paraEmdExport) {
        super.recordChanged("emd_export", this.emdExport_, paraEmdExport);
        this.emdExport_ = paraEmdExport;
    }


    /**
     * 获取 导出数据条件
     *
     * @return 导出数据条件, VARCHAR, length:2500, null:false, pk:false
     */
    public String getEmdExportWhere() {
        return this.emdExportWhere_;
    }
    /**
    * 赋值 导出数据条件

    * @param paraEmdExportWhere
    * 导出数据条件, VARCHAR, length:2500, null:false, pk:false
     */

    public void setEmdExportWhere(String paraEmdExportWhere) {
        super.recordChanged("emd_export_where", this.emdExportWhere_, paraEmdExportWhere);
        this.emdExportWhere_ = paraEmdExportWhere;
    }


    /**
     * 获取 创建时间
     *
     * @return 创建时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getEmdCdate() {
        return this.emdCdate_;
    }
    /**
    * 赋值 创建时间

    * @param paraEmdCdate
    * 创建时间, DATETIME, length:19, null:false, pk:false
     */

    public void setEmdCdate(Date paraEmdCdate) {
        super.recordChanged("emd_cdate", this.emdCdate_, paraEmdCdate);
        this.emdCdate_ = paraEmdCdate;
    }


    /**
     * 获取 修改时间
     *
     * @return 修改时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getEmdMdate() {
        return this.emdMdate_;
    }
    /**
    * 赋值 修改时间

    * @param paraEmdMdate
    * 修改时间, DATETIME, length:19, null:false, pk:false
     */

    public void setEmdMdate(Date paraEmdMdate) {
        super.recordChanged("emd_mdate", this.emdMdate_, paraEmdMdate);
        this.emdMdate_ = paraEmdMdate;
    }


    /**
     * 获取 用户
     *
     * @return 用户, INT, length:10, null:false, pk:false
     */
    public Integer getEmdAdmId() {
        return this.emdAdmId_;
    }
    /**
    * 赋值 用户

    * @param paraEmdAdmId
    * 用户, INT, length:10, null:false, pk:false
     */

    public void setEmdAdmId(Integer paraEmdAdmId) {
        super.recordChanged("emd_adm_id", this.emdAdmId_, paraEmdAdmId);
        this.emdAdmId_ = paraEmdAdmId;
    }


    /**
     * 获取 商户
     *
     * @return 商户, INT, length:10, null:false, pk:false
     */
    public Integer getEmdSupId() {
        return this.emdSupId_;
    }
    /**
    * 赋值 商户

    * @param paraEmdSupId
    * 商户, INT, length:10, null:false, pk:false
     */

    public void setEmdSupId(Integer paraEmdSupId) {
        super.recordChanged("emd_sup_id", this.emdSupId_, paraEmdSupId);
        this.emdSupId_ = paraEmdSupId;
    }


    /**
     * 获取 导出的xml格式
     *
     * @return 导出的xml格式, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getEmdXml() {
        return this.emdXml_;
    }
    /**
    * 赋值 导出的xml格式

    * @param paraEmdXml
    * 导出的xml格式, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setEmdXml(String paraEmdXml) {
        super.recordChanged("emd_xml", this.emdXml_, paraEmdXml);
        this.emdXml_ = paraEmdXml;
    }
}