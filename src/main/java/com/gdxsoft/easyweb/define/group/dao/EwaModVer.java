package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod_ver映射类
 * @author gdx 时间：Wed Apr 26 2023 18:08:28 GMT+0800 (中国标准时间)*/
public class EwaModVer extends ClassBase { // 版本编号, BIGINT, length:19, null:false, pk:true
    private Long modVerId_;
    // 模块编码, VARCHAR, length:100, null:false, pk:false
    private String modCode_;
    // 模块版本, VARCHAR, length:30, null:false, pk:false
    private String modVer_;
    // 创建时间, DATETIME, length:19, null:false, pk:false
    private Date modVerCdate_;
    // 修改时间, DATETIME, length:19, null:false, pk:false
    private Date modVerMdate_;
    // 模块版本说明, LONGTEXT, length:2147483647, null:true, pk:false
    private String modVerMemo_;
    // 模块版本说明英文, LONGTEXT, length:2147483647, null:true, pk:false
    private String modVerMemoEn_;
    // 模块状态- used,del, VARCHAR, length:10, null:false, pk:false
    private String modVerStatus_;
    // 用户, INT, length:10, null:false, pk:false
    private Integer modVerAdmId_;
    // 商户, INT, length:10, null:false, pk:false
    private Integer modVerSupId_;

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
     * 获取 模块编码
     *
     * @return 模块编码, VARCHAR, length:100, null:false, pk:false
     */
    public String getModCode() {
        return this.modCode_;
    }
    /**
    * 赋值 模块编码

    * @param paraModCode
    * 模块编码, VARCHAR, length:100, null:false, pk:false
     */

    public void setModCode(String paraModCode) {
        super.recordChanged("mod_code", this.modCode_, paraModCode);
        this.modCode_ = paraModCode;
    }


    /**
     * 获取 模块版本
     *
     * @return 模块版本, VARCHAR, length:30, null:false, pk:false
     */
    public String getModVer() {
        return this.modVer_;
    }
    /**
    * 赋值 模块版本

    * @param paraModVer
    * 模块版本, VARCHAR, length:30, null:false, pk:false
     */

    public void setModVer(String paraModVer) {
        super.recordChanged("mod_ver", this.modVer_, paraModVer);
        this.modVer_ = paraModVer;
    }


    /**
     * 获取 创建时间
     *
     * @return 创建时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getModVerCdate() {
        return this.modVerCdate_;
    }
    /**
    * 赋值 创建时间

    * @param paraModVerCdate
    * 创建时间, DATETIME, length:19, null:false, pk:false
     */

    public void setModVerCdate(Date paraModVerCdate) {
        super.recordChanged("mod_ver_cdate", this.modVerCdate_, paraModVerCdate);
        this.modVerCdate_ = paraModVerCdate;
    }


    /**
     * 获取 修改时间
     *
     * @return 修改时间, DATETIME, length:19, null:false, pk:false
     */
    public Date getModVerMdate() {
        return this.modVerMdate_;
    }
    /**
    * 赋值 修改时间

    * @param paraModVerMdate
    * 修改时间, DATETIME, length:19, null:false, pk:false
     */

    public void setModVerMdate(Date paraModVerMdate) {
        super.recordChanged("mod_ver_mdate", this.modVerMdate_, paraModVerMdate);
        this.modVerMdate_ = paraModVerMdate;
    }


    /**
     * 获取 模块版本说明
     *
     * @return 模块版本说明, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getModVerMemo() {
        return this.modVerMemo_;
    }
    /**
    * 赋值 模块版本说明

    * @param paraModVerMemo
    * 模块版本说明, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setModVerMemo(String paraModVerMemo) {
        super.recordChanged("mod_ver_memo", this.modVerMemo_, paraModVerMemo);
        this.modVerMemo_ = paraModVerMemo;
    }


    /**
     * 获取 模块版本说明英文
     *
     * @return 模块版本说明英文, LONGTEXT, length:2147483647, null:true, pk:false
     */
    public String getModVerMemoEn() {
        return this.modVerMemoEn_;
    }
    /**
    * 赋值 模块版本说明英文

    * @param paraModVerMemoEn
    * 模块版本说明英文, LONGTEXT, length:2147483647, null:true, pk:false
     */

    public void setModVerMemoEn(String paraModVerMemoEn) {
        super.recordChanged("mod_ver_memo_en", this.modVerMemoEn_, paraModVerMemoEn);
        this.modVerMemoEn_ = paraModVerMemoEn;
    }


    /**
     * 获取 模块状态- used,del
     *
     * @return 模块状态- used,del, VARCHAR, length:10, null:false, pk:false
     */
    public String getModVerStatus() {
        return this.modVerStatus_;
    }
    /**
    * 赋值 模块状态- used,del

    * @param paraModVerStatus
    * 模块状态- used,del, VARCHAR, length:10, null:false, pk:false
     */

    public void setModVerStatus(String paraModVerStatus) {
        super.recordChanged("mod_ver_status", this.modVerStatus_, paraModVerStatus);
        this.modVerStatus_ = paraModVerStatus;
    }


    /**
     * 获取 用户
     *
     * @return 用户, INT, length:10, null:false, pk:false
     */
    public Integer getModVerAdmId() {
        return this.modVerAdmId_;
    }
    /**
    * 赋值 用户

    * @param paraModVerAdmId
    * 用户, INT, length:10, null:false, pk:false
     */

    public void setModVerAdmId(Integer paraModVerAdmId) {
        super.recordChanged("mod_ver_adm_id", this.modVerAdmId_, paraModVerAdmId);
        this.modVerAdmId_ = paraModVerAdmId;
    }


    /**
     * 获取 商户
     *
     * @return 商户, INT, length:10, null:false, pk:false
     */
    public Integer getModVerSupId() {
        return this.modVerSupId_;
    }
    /**
    * 赋值 商户

    * @param paraModVerSupId
    * 商户, INT, length:10, null:false, pk:false
     */

    public void setModVerSupId(Integer paraModVerSupId) {
        super.recordChanged("mod_ver_sup_id", this.modVerSupId_, paraModVerSupId);
        this.modVerSupId_ = paraModVerSupId;
    }
}