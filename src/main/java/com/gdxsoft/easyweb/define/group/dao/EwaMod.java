package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod映射类
 * @author gdx 时间：Wed Apr 26 2023 18:06:51 GMT+0800 (中国标准时间)*/
public class EwaMod extends ClassBase { // 模块编码, VARCHAR, length:100, null:false, pk:true
    private String modCode_;
    // 模块名称, VARCHAR, length:100, null:false, pk:false
    private String modName_;
    // 模块名称-英文, VARCHAR, length:100, null:false, pk:false
    private String modNameEn_;
    // 是否开源-y/n, CHAR, length:1, null:false, pk:false
    private String modOpenSource_;
    // 公司, VARCHAR, length:100, null:true, pk:false
    private String modCompany_;
    // 联系人, VARCHAR, length:100, null:true, pk:false
    private String modContact_;
    // 网址, VARCHAR, length:200, null:true, pk:false
    private String modWeb_;
    // 地址邮件, VARCHAR, length:100, null:true, pk:false
    private String modEmail_;
    // 开源协议, VARCHAR, length:100, null:true, pk:false
    private String modOsp_;
    // 模块说明, LONGTEXT, length:2147483647, null:true, pk:false
    private String modMemo_;
    // 模块说明英文, LONGTEXT, length:2147483647, null:true, pk:false
    private String modMemoEn_;
    // 创建时间, DATETIME, length:19, null:false, pk:false
    private Date modCdate_;
    // 修改时间, DATETIME, length:19, null:false, pk:false
    private Date modMdate_;
    // 模块状态- new,dlv,undlv,del, VARCHAR, length:10, null:false, pk:false
    private String modStatus_;
    // 用户, INT, length:10, null:false, pk:false
    private Integer modAdmId_;
    // 商户, INT, length:10, null:false, pk:false
    private Integer modSupId_;
    // 元数据库名称, VARCHAR, length:64, null:true, pk:false
    private String modMetaDbName_;
    // 工作数据库名称, VARCHAR, length:64, null:true, pk:false
    private String modWorkDbName_;
    // ewa数据库连接池, VARCHAR, length:67, null:false, pk:false
    private String modEwaConn_;

    /**
     * 获取 模块编码
     *
     * @return 模块编码, VARCHAR, length:100, null:false, pk:true
     */
    public String getModCode() {
        return this.modCode_;
    }
    /**
    * 赋值 模块编码

    * @param paraModCode
    * 模块编码, VARCHAR, length:100, null:false, pk:true
     */

    public void setModCode(String paraModCode) {
        super.recordChanged("mod_code", this.modCode_, paraModCode);
        this.modCode_ = paraModCode;
    }


    /**
     * 获取 模块名称
     *
     * @return 模块名称, VARCHAR, length:100, null:false, pk:false
     */
    public String getModName() {
        return this.modName_;
    }
    /**
    * 赋值 模块名称

    * @param paraModName
    * 模块名称, VARCHAR, length:100, null:false, pk:false
     */

    public void setModName(String paraModName) {
        super.recordChanged("mod_name", this.modName_, paraModName);
        this.modName_ = paraModName;
    }


    /**
     * 获取 模块名称-英文
     *
     * @return 模块名称-英文, VARCHAR, length:100, null:false, pk:false
     */
    public String getModNameEn() {
        return this.modNameEn_;
    }
    /**
    * 赋值 模块名称-英文

    * @param paraModNameEn
    * 模块名称-英文, VARCHAR, length:100, null:false, pk:false
     */

    public void setModNameEn(String paraModNameEn) {
        super.recordChanged("mod_name_en", this.modNameEn_, paraModNameEn);
        this.modNameEn_ = paraModNameEn;
    }


    /**
     * 获取 是否开源-y/n
     *
     * @return 是否开源-y/n, CHAR, length:1, null:false, pk:false
     */
    public String getModOpenSource() {
        return this.modOpenSource_;
    }
    /**
    * 赋值 是否开源-y/n

    * @param paraModOpenSource
    * 是否开源-y/n, CHAR, length:1, null:false, pk:false
     */

    public void setModOpenSource(String paraModOpenSource) {
        super.recordChanged("mod_open_source", this.modOpenSource_, paraModOpenSource);
        this.modOpenSource_ = paraModOpenSource;
    }


    /**
     * 获取 公司
     *
     * @return 公司, VARCHAR, length:100, null:true, pk:false
     */
    public String getModCompany() {
        return this.modCompany_;
    }
    /**
    * 赋值 公司

    * @param paraModCompany
    * 公司, VARCHAR, length:100, null:true, pk:false
     */

    public void setModCompany(String paraModCompany) {
        super.recordChanged("mod_company", this.modCompany_, paraModCompany);
        this.modCompany_ = paraModCompany;
    }


    /**
     * 获取 联系人
     *
     * @return 联系人, VARCHAR, length:100, null:true, pk:false
     */
    public String getModContact() {
        return this.modContact_;
    }
    /**
    * 赋值 联系人

    * @param paraModContact
    * 联系人, VARCHAR, length:100, null:true, pk:false
     */

    public void setModContact(String paraModContact) {
        super.recordChanged("mod_contact", this.modContact_, paraModContact);
        this.modContact_ = paraModContact;
    }


    /**
     * 获取 网址
     *
     * @return 网址, VARCHAR, length:200, null:true, pk:false
     */
    public String getModWeb() {
        return this.modWeb_;
    }
    /**
    * 赋值 网址

    * @param paraModWeb
    * 网址, VARCHAR, length:200, null:true, pk:false
     */

    public void setModWeb(String paraModWeb) {
        super.recordChanged("mod_web", this.modWeb_, paraModWeb);
        this.modWeb_ = paraModWeb;
    }


    /**
     * 获取 地址邮件
     *
     * @return 地址邮件, VARCHAR, length:100, null:true, pk:false
     */
    public String getModEmail() {
        return this.modEmail_;
    }
    /**
    * 赋值 地址邮件

    * @param paraModEmail
    * 地址邮件, VARCHAR, length:100, null:true, pk:false
     */

    public void setModEmail(String paraModEmail) {
        super.recordChanged("mod_email", this.modEmail_, paraModEmail);
        this.modEmail_ = paraModEmail;
    }


    /**
     * 获取 开源协议
     *
     * @return 开源协议, VARCHAR, length:100, null:true, pk:false
     */
    public String getModOsp() {
        return this.modOsp_;
    }
    /**
    * 赋值 开源协议

    * @param paraModOsp
    * 开源协议, VARCHAR, length:100, null:true, pk:false
     */

    public void setModOsp(String paraModOsp) {
        super.recordChanged("mod_osp", this.modOsp_, paraModOsp);
        this.modOsp_ = paraModOsp;
    }


    /**
     * 获取 模块说明
     *
     * @return 模块说明, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getModMemo() {
        return this.modMemo_;
    }
    /**
    * 赋值 模块说明

    * @param paraModMemo
    * 模块说明, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setModMemo(String paraModMemo) {
        super.recordChanged("mod_memo", this.modMemo_, paraModMemo);
        this.modMemo_ = paraModMemo;
    }


    /**
     * 获取 模块说明英文
     *
     * @return 模块说明英文, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getModMemoEn() {
        return this.modMemoEn_;
    }
    /**
    * 赋值 模块说明英文

    * @param paraModMemoEn
    * 模块说明英文, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setModMemoEn(String paraModMemoEn) {
        super.recordChanged("mod_memo_en", this.modMemoEn_, paraModMemoEn);
        this.modMemoEn_ = paraModMemoEn;
    }


    /**
     * 获取 创建时间
     *
     * @return 创建时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getModCdate() {
        return this.modCdate_;
    }
    /**
    * 赋值 创建时间

    * @param paraModCdate
    * 创建时间, DATETIME, length:19, null:false, pk:false
     */

    public void setModCdate(Date paraModCdate) {
        super.recordChanged("mod_cdate", this.modCdate_, paraModCdate);
        this.modCdate_ = paraModCdate;
    }


    /**
     * 获取 修改时间
     *
     * @return 修改时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getModMdate() {
        return this.modMdate_;
    }
    /**
    * 赋值 修改时间

    * @param paraModMdate
    * 修改时间, DATETIME, length:19, null:false, pk:false
     */

    public void setModMdate(Date paraModMdate) {
        super.recordChanged("mod_mdate", this.modMdate_, paraModMdate);
        this.modMdate_ = paraModMdate;
    }


    /**
     * 获取 模块状态- new,dlv,undlv,del
     *
     * @return 模块状态- new,dlv,undlv,del, VARCHAR, length:10, null:false, pk:false
     */
    public String getModStatus() {
        return this.modStatus_;
    }
    /**
    * 赋值 模块状态- new,dlv,undlv,del

    * @param paraModStatus
    * 模块状态- new,dlv,undlv,del, VARCHAR, length:10, null:false, pk:false
     */

    public void setModStatus(String paraModStatus) {
        super.recordChanged("mod_status", this.modStatus_, paraModStatus);
        this.modStatus_ = paraModStatus;
    }


    /**
     * 获取 用户
     *
     * @return 用户, INT, length:10, null:false, pk:false
     */
    public Integer getModAdmId() {
        return this.modAdmId_;
    }
    /**
    * 赋值 用户

    * @param paraModAdmId
    * 用户, INT, length:10, null:false, pk:false
     */

    public void setModAdmId(Integer paraModAdmId) {
        super.recordChanged("mod_adm_id", this.modAdmId_, paraModAdmId);
        this.modAdmId_ = paraModAdmId;
    }


    /**
     * 获取 商户
     *
     * @return 商户, INT, length:10, null:false, pk:false
     */
    public Integer getModSupId() {
        return this.modSupId_;
    }
    /**
    * 赋值 商户

    * @param paraModSupId
    * 商户, INT, length:10, null:false, pk:false
     */

    public void setModSupId(Integer paraModSupId) {
        super.recordChanged("mod_sup_id", this.modSupId_, paraModSupId);
        this.modSupId_ = paraModSupId;
    }


    /**
     * 获取 元数据库名称
     *
     * @return 元数据库名称, VARCHAR, length:64, null:true, pk:false
     */
    public String getModMetaDbName() {
        return this.modMetaDbName_;
    }
    /**
    * 赋值 元数据库名称

    * @param paraModMetaDbName
    * 元数据库名称, VARCHAR, length:64, null:true, pk:false
     */

    public void setModMetaDbName(String paraModMetaDbName) {
        super.recordChanged("mod_meta_db_name", this.modMetaDbName_, paraModMetaDbName);
        this.modMetaDbName_ = paraModMetaDbName;
    }


    /**
     * 获取 工作数据库名称
     *
     * @return 工作数据库名称, VARCHAR, length:64, null:true, pk:false
     */
    public String getModWorkDbName() {
        return this.modWorkDbName_;
    }
    /**
    * 赋值 工作数据库名称

    * @param paraModWorkDbName
    * 工作数据库名称, VARCHAR, length:64, null:true, pk:false
     */

    public void setModWorkDbName(String paraModWorkDbName) {
        super.recordChanged("mod_work_db_name", this.modWorkDbName_, paraModWorkDbName);
        this.modWorkDbName_ = paraModWorkDbName;
    }


    /**
     * 获取 ewa数据库连接池
     *
     * @return ewa数据库连接池, VARCHAR, length:67, null:false, pk:false
     */
    public String getModEwaConn() {
        return this.modEwaConn_;
    }
    /**
    * 赋值 ewa数据库连接池

    * @param paraModEwaConn
    * ewa数据库连接池, VARCHAR, length:67, null:false, pk:false
     */

    public void setModEwaConn(String paraModEwaConn) {
        super.recordChanged("mod_ewa_conn", this.modEwaConn_, paraModEwaConn);
        this.modEwaConn_ = paraModEwaConn;
    }
}