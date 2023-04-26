package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod_data映射类
 * @author gdx 时间：Wed Apr 26 2023 18:08:19 GMT+0800 (中国标准时间)*/
public class EwaModData extends ClassBase { // 版本编号, BIGINT, length:19, null:false, pk:false
    private Long modVerId_;
    // 数据库目录, VARCHAR, length:64, null:false, pk:false
    private String tableCatalog_;
    // 数据库, VARCHAR, length:64, null:false, pk:false
    private String tableSchema_;
    // 表名称, VARCHAR, length:64, null:false, pk:false
    private String tableName_;
    // 记录行, INT, length:10, null:false, pk:false
    private Integer dataIndex_;
    // 行记录, LONGTEXT, length:2147483647, null:false, pk:false
    private String dataRow_;

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
     * 获取 记录行
     *
     * @return 记录行, INT, length:10, null:false, pk:false
     */
    public Integer getDataIndex() {
        return this.dataIndex_;
    }
    /**
    * 赋值 记录行

    * @param paraDataIndex
    * 记录行, INT, length:10, null:false, pk:false
     */

    public void setDataIndex(Integer paraDataIndex) {
        super.recordChanged("data_index", this.dataIndex_, paraDataIndex);
        this.dataIndex_ = paraDataIndex;
    }


    /**
     * 获取 行记录
     *
     * @return 行记录, LONGTEXT, length:2147483647, null:false, pk:false
     */
    public String getDataRow() {
        return this.dataRow_;
    }
    /**
    * 赋值 行记录

    * @param paraDataRow
    * 行记录, LONGTEXT, length:2147483647, null:false, pk:false
     */

    public void setDataRow(String paraDataRow) {
        super.recordChanged("data_row", this.dataRow_, paraDataRow);
        this.dataRow_ = paraDataRow;
    }
}