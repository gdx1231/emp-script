package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod_cfgs映射类
 * @author gdx 时间：Wed Apr 26 2023 18:08:12 GMT+0800 (中国标准时间)*/
public class EwaModCfgs extends ClassBase { // 版本编号, BIGINT, length:19, null:false, pk:true
    private Long modVerId_;
    // 配置文件, VARCHAR, length:200, null:false, pk:true
    private String xmlname_;
    // 配置项, VARCHAR, length:100, null:false, pk:true
    private String itemname_;
    // 描述, VARCHAR, length:500, null:false, pk:false
    private String description_;
    // 创建数据, DATETIME, length:19, null:false, pk:false
    private Date emcCdate_;
    // 修改时间, DATETIME, length:19, null:false, pk:false
    private Date emcMdate_;
    // 用户, INT, length:10, null:false, pk:false
    private Integer emcAdmId_;
    // 商户, INT, length:10, null:false, pk:false
    private Integer emcSupId_;
    // 默认文件, VARCHAR, length:200, null:false, pk:false
    private String emcDefXmlname_;
    // 默认配置项名称, VARCHAR, length:100, null:false, pk:false
    private String emcDefItemname_;

    /**
     * 获取 版本编号
     *
     * @return 版本编号, BIGINT, length:19, null:false, pk:true
     */
    public Long getModVerId() {
        return this.modVerId_;
    }
    /**
    * 赋值 版本编号

    * @param paraModVerId
    * 版本编号, BIGINT, length:19, null:false, pk:true
     */

    public void setModVerId(Long paraModVerId) {
        super.recordChanged("mod_ver_id", this.modVerId_, paraModVerId);
        this.modVerId_ = paraModVerId;
    }


    /**
     * 获取 配置文件
     *
     * @return 配置文件, VARCHAR, length:200, null:false, pk:true
     */
    public String getXmlname() {
        return this.xmlname_;
    }
    /**
    * 赋值 配置文件

    * @param paraXmlname
    * 配置文件, VARCHAR, length:200, null:false, pk:true
     */

    public void setXmlname(String paraXmlname) {
        super.recordChanged("xmlname", this.xmlname_, paraXmlname);
        this.xmlname_ = paraXmlname;
    }


    /**
     * 获取 配置项
     *
     * @return 配置项, VARCHAR, length:100, null:false, pk:true
     */
    public String getItemname() {
        return this.itemname_;
    }
    /**
    * 赋值 配置项

    * @param paraItemname
    * 配置项, VARCHAR, length:100, null:false, pk:true
     */

    public void setItemname(String paraItemname) {
        super.recordChanged("itemname", this.itemname_, paraItemname);
        this.itemname_ = paraItemname;
    }


    /**
     * 获取 描述
     *
     * @return 描述, VARCHAR, length:500, null:false, pk:false
     */
    public String getDescription() {
        return this.description_;
    }
    /**
    * 赋值 描述

    * @param paraDescription
    * 描述, VARCHAR, length:500, null:false, pk:false
     */

    public void setDescription(String paraDescription) {
        super.recordChanged("description", this.description_, paraDescription);
        this.description_ = paraDescription;
    }


    /**
     * 获取 创建数据
     *
     * @return 创建数据, DATETIME, length:19, null:false, pk:false
     */
    public Date getEmcCdate() {
        return this.emcCdate_;
    }
    /**
    * 赋值 创建数据

    * @param paraEmcCdate
    * 创建数据, DATETIME, length:19, null:false, pk:false
     */

    public void setEmcCdate(Date paraEmcCdate) {
        super.recordChanged("emc_cdate", this.emcCdate_, paraEmcCdate);
        this.emcCdate_ = paraEmcCdate;
    }


    /**
     * 获取 修改时间
     *
     * @return 修改时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getEmcMdate() {
        return this.emcMdate_;
    }
    /**
    * 赋值 修改时间

    * @param paraEmcMdate
    * 修改时间, DATETIME, length:19, null:false, pk:false
     */

    public void setEmcMdate(Date paraEmcMdate) {
        super.recordChanged("emc_mdate", this.emcMdate_, paraEmcMdate);
        this.emcMdate_ = paraEmcMdate;
    }


    /**
     * 获取 用户
     *
     * @return 用户, INT, length:10, null:false, pk:false
     */
    public Integer getEmcAdmId() {
        return this.emcAdmId_;
    }
    /**
    * 赋值 用户

    * @param paraEmcAdmId
    * 用户, INT, length:10, null:false, pk:false
     */

    public void setEmcAdmId(Integer paraEmcAdmId) {
        super.recordChanged("emc_adm_id", this.emcAdmId_, paraEmcAdmId);
        this.emcAdmId_ = paraEmcAdmId;
    }


    /**
     * 获取 商户
     *
     * @return 商户, INT, length:10, null:false, pk:false
     */
    public Integer getEmcSupId() {
        return this.emcSupId_;
    }
    /**
    * 赋值 商户

    * @param paraEmcSupId
    * 商户, INT, length:10, null:false, pk:false
     */

    public void setEmcSupId(Integer paraEmcSupId) {
        super.recordChanged("emc_sup_id", this.emcSupId_, paraEmcSupId);
        this.emcSupId_ = paraEmcSupId;
    }


    /**
     * 获取 默认文件
     *
     * @return 默认文件, VARCHAR, length:200, null:false, pk:false
     */
    public String getEmcDefXmlname() {
        return this.emcDefXmlname_;
    }
    /**
    * 赋值 默认文件

    * @param paraEmcDefXmlname
    * 默认文件, VARCHAR, length:200, null:false, pk:false
     */

    public void setEmcDefXmlname(String paraEmcDefXmlname) {
        super.recordChanged("emc_def_xmlname", this.emcDefXmlname_, paraEmcDefXmlname);
        this.emcDefXmlname_ = paraEmcDefXmlname;
    }


    /**
     * 获取 默认配置项名称
     *
     * @return 默认配置项名称, VARCHAR, length:100, null:false, pk:false
     */
    public String getEmcDefItemname() {
        return this.emcDefItemname_;
    }
    /**
    * 赋值 默认配置项名称

    * @param paraEmcDefItemname
    * 默认配置项名称, VARCHAR, length:100, null:false, pk:false
     */

    public void setEmcDefItemname(String paraEmcDefItemname) {
        super.recordChanged("emc_def_itemname", this.emcDefItemname_, paraEmcDefItemname);
        this.emcDefItemname_ = paraEmcDefItemname;
    }
}