package com.gdxsoft.easyweb.define.group.dao;

import java.util.Date;
import com.gdxsoft.easyweb.datasource.ClassBase;
/**表ewa_mod_download映射类
* @author gdx 时间：Wed Dec 01 2021 12:38:19 GMT+0800 (中国标准时间)*/
public class EwaModDownload extends ClassBase{private Integer modDlId_; // 下载序号
private String modCode_; // 模块编码
private String modVer_; // 模块版本
private String modName_; // 模块名称
private String modNameEn_; // 模块名称-英文
private String modOpenSource_; // 是否开源-Y/N
private String modCompany_; // 公司
private String modContact_; // 联系人
private String modWeb_; // 网址
private String modEmail_; // 地址邮件
private String modOsp_; // 开源协议
private String modMemo_; // 模块说明
private String modMemoEn_; // 模块说明英文
private Date modCdate_; // 创建时间
private Date modMdate_; // 修改时间
private Integer modSupId_; // 商户
private Long modVerId_; // 版本编号
private Date modVerCdate_; // 创建时间
private Date modVerMdate_; // 修改时间
private String modVerMemo_; // 模块版本说明
private String modVerMemoEn_; // 模块版本说明英文
private String modVerStatus_; // 模块状态
private Integer pkgLen_; // 包大小
private String pkgMd5_; // 包MD5
private byte[] pkgFile_; // 包内容
private Date modDlCdate_; // 创建时间
private Date modDlMdate_; // 修改时间
private String modDlStatus_; // 模块状态- used,del
private Integer modDlSupId_; // 商户
private String modDlUrl_; // 下载网址

/**
 * 获取 下载序号
 *
* @return 下载序号
*/
public Integer getModDlId() {return this.modDlId_;}
/**
* 赋值 下载序号

* @param paraModDlId
* 下载序号
 */

public void setModDlId(Integer paraModDlId){
  super.recordChanged("mod_dl_id", this.modDlId_, paraModDlId);
  this.modDlId_ = paraModDlId;
}


/**
 * 获取 模块编码
 *
* @return 模块编码
*/
public String getModCode() {return this.modCode_;}
/**
* 赋值 模块编码

* @param paraModCode
* 模块编码
 */

public void setModCode(String paraModCode){
  super.recordChanged("mod_code", this.modCode_, paraModCode);
  this.modCode_ = paraModCode;
}


/**
 * 获取 模块版本
 *
* @return 模块版本
*/
public String getModVer() {return this.modVer_;}
/**
* 赋值 模块版本

* @param paraModVer
* 模块版本
 */

public void setModVer(String paraModVer){
  super.recordChanged("mod_ver", this.modVer_, paraModVer);
  this.modVer_ = paraModVer;
}


/**
 * 获取 模块名称
 *
* @return 模块名称
*/
public String getModName() {return this.modName_;}
/**
* 赋值 模块名称

* @param paraModName
* 模块名称
 */

public void setModName(String paraModName){
  super.recordChanged("mod_name", this.modName_, paraModName);
  this.modName_ = paraModName;
}


/**
 * 获取 模块名称-英文
 *
* @return 模块名称-英文
*/
public String getModNameEn() {return this.modNameEn_;}
/**
* 赋值 模块名称-英文

* @param paraModNameEn
* 模块名称-英文
 */

public void setModNameEn(String paraModNameEn){
  super.recordChanged("mod_name_en", this.modNameEn_, paraModNameEn);
  this.modNameEn_ = paraModNameEn;
}


/**
 * 获取 是否开源-Y/N
 *
* @return 是否开源-Y/N
*/
public String getModOpenSource() {return this.modOpenSource_;}
/**
* 赋值 是否开源-Y/N

* @param paraModOpenSource
* 是否开源-Y/N
 */

public void setModOpenSource(String paraModOpenSource){
  super.recordChanged("mod_open_source", this.modOpenSource_, paraModOpenSource);
  this.modOpenSource_ = paraModOpenSource;
}


/**
 * 获取 公司
 *
* @return 公司
*/
public String getModCompany() {return this.modCompany_;}
/**
* 赋值 公司

* @param paraModCompany
* 公司
 */

public void setModCompany(String paraModCompany){
  super.recordChanged("mod_company", this.modCompany_, paraModCompany);
  this.modCompany_ = paraModCompany;
}


/**
 * 获取 联系人
 *
* @return 联系人
*/
public String getModContact() {return this.modContact_;}
/**
* 赋值 联系人

* @param paraModContact
* 联系人
 */

public void setModContact(String paraModContact){
  super.recordChanged("mod_contact", this.modContact_, paraModContact);
  this.modContact_ = paraModContact;
}


/**
 * 获取 网址
 *
* @return 网址
*/
public String getModWeb() {return this.modWeb_;}
/**
* 赋值 网址

* @param paraModWeb
* 网址
 */

public void setModWeb(String paraModWeb){
  super.recordChanged("mod_web", this.modWeb_, paraModWeb);
  this.modWeb_ = paraModWeb;
}


/**
 * 获取 地址邮件
 *
* @return 地址邮件
*/
public String getModEmail() {return this.modEmail_;}
/**
* 赋值 地址邮件

* @param paraModEmail
* 地址邮件
 */

public void setModEmail(String paraModEmail){
  super.recordChanged("mod_email", this.modEmail_, paraModEmail);
  this.modEmail_ = paraModEmail;
}


/**
 * 获取 开源协议
 *
* @return 开源协议
*/
public String getModOsp() {return this.modOsp_;}
/**
* 赋值 开源协议

* @param paraModOsp
* 开源协议
 */

public void setModOsp(String paraModOsp){
  super.recordChanged("mod_osp", this.modOsp_, paraModOsp);
  this.modOsp_ = paraModOsp;
}


/**
 * 获取 模块说明
 *
* @return 模块说明
*/
public String getModMemo() {return this.modMemo_;}
/**
* 赋值 模块说明

* @param paraModMemo
* 模块说明
 */

public void setModMemo(String paraModMemo){
  super.recordChanged("mod_memo", this.modMemo_, paraModMemo);
  this.modMemo_ = paraModMemo;
}


/**
 * 获取 模块说明英文
 *
* @return 模块说明英文
*/
public String getModMemoEn() {return this.modMemoEn_;}
/**
* 赋值 模块说明英文

* @param paraModMemoEn
* 模块说明英文
 */

public void setModMemoEn(String paraModMemoEn){
  super.recordChanged("mod_memo_en", this.modMemoEn_, paraModMemoEn);
  this.modMemoEn_ = paraModMemoEn;
}


/**
 * 获取 创建时间
 *
* @return 创建时间
*/
public Date getModCdate() {return this.modCdate_;}
/**
* 赋值 创建时间

* @param paraModCdate
* 创建时间
 */

public void setModCdate(Date paraModCdate){
  super.recordChanged("mod_cdate", this.modCdate_, paraModCdate);
  this.modCdate_ = paraModCdate;
}


/**
 * 获取 修改时间
 *
* @return 修改时间
*/
public Date getModMdate() {return this.modMdate_;}
/**
* 赋值 修改时间

* @param paraModMdate
* 修改时间
 */

public void setModMdate(Date paraModMdate){
  super.recordChanged("mod_mdate", this.modMdate_, paraModMdate);
  this.modMdate_ = paraModMdate;
}


/**
 * 获取 商户
 *
* @return 商户
*/
public Integer getModSupId() {return this.modSupId_;}
/**
* 赋值 商户

* @param paraModSupId
* 商户
 */

public void setModSupId(Integer paraModSupId){
  super.recordChanged("mod_sup_id", this.modSupId_, paraModSupId);
  this.modSupId_ = paraModSupId;
}


/**
 * 获取 版本编号
 *
* @return 版本编号
*/
public Long getModVerId() {return this.modVerId_;}
/**
* 赋值 版本编号

* @param paraModVerId
* 版本编号
 */

public void setModVerId(Long paraModVerId){
  super.recordChanged("mod_ver_id", this.modVerId_, paraModVerId);
  this.modVerId_ = paraModVerId;
}


/**
 * 获取 创建时间
 *
* @return 创建时间
*/
public Date getModVerCdate() {return this.modVerCdate_;}
/**
* 赋值 创建时间

* @param paraModVerCdate
* 创建时间
 */

public void setModVerCdate(Date paraModVerCdate){
  super.recordChanged("mod_ver_cdate", this.modVerCdate_, paraModVerCdate);
  this.modVerCdate_ = paraModVerCdate;
}


/**
 * 获取 修改时间
 *
* @return 修改时间
*/
public Date getModVerMdate() {return this.modVerMdate_;}
/**
* 赋值 修改时间

* @param paraModVerMdate
* 修改时间
 */

public void setModVerMdate(Date paraModVerMdate){
  super.recordChanged("mod_ver_mdate", this.modVerMdate_, paraModVerMdate);
  this.modVerMdate_ = paraModVerMdate;
}


/**
 * 获取 模块版本说明
 *
* @return 模块版本说明
*/
public String getModVerMemo() {return this.modVerMemo_;}
/**
* 赋值 模块版本说明

* @param paraModVerMemo
* 模块版本说明
 */

public void setModVerMemo(String paraModVerMemo){
  super.recordChanged("mod_ver_memo", this.modVerMemo_, paraModVerMemo);
  this.modVerMemo_ = paraModVerMemo;
}


/**
 * 获取 模块版本说明英文
 *
* @return 模块版本说明英文
*/
public String getModVerMemoEn() {return this.modVerMemoEn_;}
/**
* 赋值 模块版本说明英文

* @param paraModVerMemoEn
* 模块版本说明英文
 */

public void setModVerMemoEn(String paraModVerMemoEn){
  super.recordChanged("mod_ver_memo_en", this.modVerMemoEn_, paraModVerMemoEn);
  this.modVerMemoEn_ = paraModVerMemoEn;
}


/**
 * 获取 模块状态
 *
* @return 模块状态
*/
public String getModVerStatus() {return this.modVerStatus_;}
/**
* 赋值 模块状态

* @param paraModVerStatus
* 模块状态
 */

public void setModVerStatus(String paraModVerStatus){
  super.recordChanged("mod_ver_status", this.modVerStatus_, paraModVerStatus);
  this.modVerStatus_ = paraModVerStatus;
}


/**
 * 获取 包大小
 *
* @return 包大小
*/
public Integer getPkgLen() {return this.pkgLen_;}
/**
* 赋值 包大小

* @param paraPkgLen
* 包大小
 */

public void setPkgLen(Integer paraPkgLen){
  super.recordChanged("pkg_len", this.pkgLen_, paraPkgLen);
  this.pkgLen_ = paraPkgLen;
}


/**
 * 获取 包MD5
 *
* @return 包MD5
*/
public String getPkgMd5() {return this.pkgMd5_;}
/**
* 赋值 包MD5

* @param paraPkgMd5
* 包MD5
 */

public void setPkgMd5(String paraPkgMd5){
  super.recordChanged("pkg_md5", this.pkgMd5_, paraPkgMd5);
  this.pkgMd5_ = paraPkgMd5;
}


/**
 * 获取 包内容
 *
* @return 包内容
*/
public byte[] getPkgFile() {return this.pkgFile_;}
/**
* 赋值 包内容

* @param paraPkgFile
* 包内容
 */

public void setPkgFile(byte[] paraPkgFile){
  super.recordChanged("pkg_file", this.pkgFile_, paraPkgFile);
  this.pkgFile_ = paraPkgFile;
}


/**
 * 获取 创建时间
 *
* @return 创建时间
*/
public Date getModDlCdate() {return this.modDlCdate_;}
/**
* 赋值 创建时间

* @param paraModDlCdate
* 创建时间
 */

public void setModDlCdate(Date paraModDlCdate){
  super.recordChanged("mod_dl_cdate", this.modDlCdate_, paraModDlCdate);
  this.modDlCdate_ = paraModDlCdate;
}


/**
 * 获取 修改时间
 *
* @return 修改时间
*/
public Date getModDlMdate() {return this.modDlMdate_;}
/**
* 赋值 修改时间

* @param paraModDlMdate
* 修改时间
 */

public void setModDlMdate(Date paraModDlMdate){
  super.recordChanged("mod_dl_mdate", this.modDlMdate_, paraModDlMdate);
  this.modDlMdate_ = paraModDlMdate;
}


/**
 * 获取 模块状态- used,del
 *
* @return 模块状态- used,del
*/
public String getModDlStatus() {return this.modDlStatus_;}
/**
* 赋值 模块状态- used,del

* @param paraModDlStatus
* 模块状态- used,del
 */

public void setModDlStatus(String paraModDlStatus){
  super.recordChanged("mod_dl_status", this.modDlStatus_, paraModDlStatus);
  this.modDlStatus_ = paraModDlStatus;
}


/**
 * 获取 商户
 *
* @return 商户
*/
public Integer getModDlSupId() {return this.modDlSupId_;}
/**
* 赋值 商户

* @param paraModDlSupId
* 商户
 */

public void setModDlSupId(Integer paraModDlSupId){
  super.recordChanged("mod_dl_sup_id", this.modDlSupId_, paraModDlSupId);
  this.modDlSupId_ = paraModDlSupId;
}


/**
 * 获取 下载网址
 *
* @return 下载网址
*/
public String getModDlUrl() {return this.modDlUrl_;}
/**
* 赋值 下载网址

* @param paraModDlUrl
* 下载网址
 */

public void setModDlUrl(String paraModDlUrl){
  super.recordChanged("mod_dl_url", this.modDlUrl_, paraModDlUrl);
  this.modDlUrl_ = paraModDlUrl;
}
}