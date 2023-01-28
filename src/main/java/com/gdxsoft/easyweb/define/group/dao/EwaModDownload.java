package com.gdxsoft.easyweb.define.group.dao;

import java.util.*;
import com.gdxsoft.easyweb.datasource.ClassBase;

/**
 * 表ewa_mod_download映射类
 * 
 * @author gdx 时间：Sat Jan 28 2023 11:27:35 GMT+0800 (中国标准时间)
 */
public class EwaModDownload extends ClassBase {// 下载序号, int identity, length:10, null:false, pk:true
	private Integer modDlId_;
// 模块编码, nvarchar, length:100, null:false, pk:false
	private String modCode_;
// 模块版本, nvarchar, length:30, null:false, pk:false
	private String modVer_;
// 模块名称, nvarchar, length:100, null:false, pk:false
	private String modName_;
// 模块名称-英文, nvarchar, length:100, null:true, pk:false
	private String modNameEn_;
// 是否开源-y/n, char, length:1, null:true, pk:false
	private String modOpenSource_;
// 公司, nvarchar, length:100, null:true, pk:false
	private String modCompany_;
// 联系人, nvarchar, length:100, null:true, pk:false
	private String modContact_;
// 网址, nvarchar, length:200, null:true, pk:false
	private String modWeb_;
// 地址邮件, nvarchar, length:100, null:true, pk:false
	private String modEmail_;
// 开源协议, nvarchar, length:100, null:true, pk:false
	private String modOsp_;
// 模块说明, nvarchar, length:2147483647, null:true, pk:false
	private String modMemo_;
// 模块说明英文, nvarchar, length:2147483647, null:true, pk:false
	private String modMemoEn_;
// 创建时间, datetime, length:23, null:true, pk:false
	private Date modCdate_;
// 修改时间, datetime, length:23, null:true, pk:false
	private Date modMdate_;
// 商户, int, length:10, null:true, pk:false
	private Integer modSupId_;
// 版本编号, bigint, length:19, null:true, pk:false
	private Long modVerId_;
// 创建时间, datetime, length:23, null:true, pk:false
	private Date modVerCdate_;
// 修改时间, datetime, length:23, null:true, pk:false
	private Date modVerMdate_;
// 模块版本说明, nvarchar, length:2147483647, null:true, pk:false
	private String modVerMemo_;
// 模块版本说明英文, nvarchar, length:2147483647, null:true, pk:false
	private String modVerMemoEn_;
// 模块状态, nvarchar, length:10, null:true, pk:false
	private String modVerStatus_;
// 包大小, int, length:10, null:false, pk:false
	private Integer pkgLen_;
// 包md5, nvarchar, length:32, null:false, pk:false
	private String pkgMd5_;
// 包内容, varbinary, length:2147483647, null:true, pk:false
	private byte[] pkgFile_;
// 创建时间, datetime, length:23, null:false, pk:false
	private Date modDlCdate_;
// 修改时间, datetime, length:23, null:false, pk:false
	private Date modDlMdate_;
// 模块状态- used,del, nvarchar, length:10, null:false, pk:false
	private String modDlStatus_;
// 商户, int, length:10, null:false, pk:false
	private Integer modDlSupId_;
// 下载网址, nvarchar, length:700, null:true, pk:false
	private String modDlUrl_;
// 导入数据的连接名称, nvarchar, length:50, null:true, pk:false
	private String importDataConn_;
// 替换元数据库名称, nvarchar, length:64, null:true, pk:false
	private String replaceMetaDatabasename_;
// 替换工作据库名称, nvarchar, length:64, null:true, pk:false
	private String replaceWorkDatabasename_;

	/**
	 * 获取 下载序号
	 *
	 * @return 下载序号, int identity, length:10, null:false, pk:true
	 */
	public Integer getModDlId() {
		return this.modDlId_;
	}

	/**
	 * 赋值 下载序号
	 * 
	 * @param paraModDlId 下载序号, int identity, length:10, null:false, pk:true
	 */

	public void setModDlId(Integer paraModDlId) {
		super.recordChanged("mod_dl_id", this.modDlId_, paraModDlId);
		this.modDlId_ = paraModDlId;
	}

	/**
	 * 获取 模块编码
	 *
	 * @return 模块编码, nvarchar, length:100, null:false, pk:false
	 */
	public String getModCode() {
		return this.modCode_;
	}

	/**
	 * 赋值 模块编码
	 * 
	 * @param paraModCode 模块编码, nvarchar, length:100, null:false, pk:false
	 */

	public void setModCode(String paraModCode) {
		super.recordChanged("mod_code", this.modCode_, paraModCode);
		this.modCode_ = paraModCode;
	}

	/**
	 * 获取 模块版本
	 *
	 * @return 模块版本, nvarchar, length:30, null:false, pk:false
	 */
	public String getModVer() {
		return this.modVer_;
	}

	/**
	 * 赋值 模块版本
	 * 
	 * @param paraModVer 模块版本, nvarchar, length:30, null:false, pk:false
	 */

	public void setModVer(String paraModVer) {
		super.recordChanged("mod_ver", this.modVer_, paraModVer);
		this.modVer_ = paraModVer;
	}

	/**
	 * 获取 模块名称
	 *
	 * @return 模块名称, nvarchar, length:100, null:false, pk:false
	 */
	public String getModName() {
		return this.modName_;
	}

	/**
	 * 赋值 模块名称
	 * 
	 * @param paraModName 模块名称, nvarchar, length:100, null:false, pk:false
	 */

	public void setModName(String paraModName) {
		super.recordChanged("mod_name", this.modName_, paraModName);
		this.modName_ = paraModName;
	}

	/**
	 * 获取 模块名称-英文
	 *
	 * @return 模块名称-英文, nvarchar, length:100, null:true, pk:false
	 */
	public String getModNameEn() {
		return this.modNameEn_;
	}

	/**
	 * 赋值 模块名称-英文
	 * 
	 * @param paraModNameEn 模块名称-英文, nvarchar, length:100, null:true, pk:false
	 */

	public void setModNameEn(String paraModNameEn) {
		super.recordChanged("mod_name_en", this.modNameEn_, paraModNameEn);
		this.modNameEn_ = paraModNameEn;
	}

	/**
	 * 获取 是否开源-y/n
	 *
	 * @return 是否开源-y/n, char, length:1, null:true, pk:false
	 */
	public String getModOpenSource() {
		return this.modOpenSource_;
	}

	/**
	 * 赋值 是否开源-y/n
	 * 
	 * @param paraModOpenSource 是否开源-y/n, char, length:1, null:true, pk:false
	 */

	public void setModOpenSource(String paraModOpenSource) {
		super.recordChanged("mod_open_source", this.modOpenSource_, paraModOpenSource);
		this.modOpenSource_ = paraModOpenSource;
	}

	/**
	 * 获取 公司
	 *
	 * @return 公司, nvarchar, length:100, null:true, pk:false
	 */
	public String getModCompany() {
		return this.modCompany_;
	}

	/**
	 * 赋值 公司
	 * 
	 * @param paraModCompany 公司, nvarchar, length:100, null:true, pk:false
	 */

	public void setModCompany(String paraModCompany) {
		super.recordChanged("mod_company", this.modCompany_, paraModCompany);
		this.modCompany_ = paraModCompany;
	}

	/**
	 * 获取 联系人
	 *
	 * @return 联系人, nvarchar, length:100, null:true, pk:false
	 */
	public String getModContact() {
		return this.modContact_;
	}

	/**
	 * 赋值 联系人
	 * 
	 * @param paraModContact 联系人, nvarchar, length:100, null:true, pk:false
	 */

	public void setModContact(String paraModContact) {
		super.recordChanged("mod_contact", this.modContact_, paraModContact);
		this.modContact_ = paraModContact;
	}

	/**
	 * 获取 网址
	 *
	 * @return 网址, nvarchar, length:200, null:true, pk:false
	 */
	public String getModWeb() {
		return this.modWeb_;
	}

	/**
	 * 赋值 网址
	 * 
	 * @param paraModWeb 网址, nvarchar, length:200, null:true, pk:false
	 */

	public void setModWeb(String paraModWeb) {
		super.recordChanged("mod_web", this.modWeb_, paraModWeb);
		this.modWeb_ = paraModWeb;
	}

	/**
	 * 获取 地址邮件
	 *
	 * @return 地址邮件, nvarchar, length:100, null:true, pk:false
	 */
	public String getModEmail() {
		return this.modEmail_;
	}

	/**
	 * 赋值 地址邮件
	 * 
	 * @param paraModEmail 地址邮件, nvarchar, length:100, null:true, pk:false
	 */

	public void setModEmail(String paraModEmail) {
		super.recordChanged("mod_email", this.modEmail_, paraModEmail);
		this.modEmail_ = paraModEmail;
	}

	/**
	 * 获取 开源协议
	 *
	 * @return 开源协议, nvarchar, length:100, null:true, pk:false
	 */
	public String getModOsp() {
		return this.modOsp_;
	}

	/**
	 * 赋值 开源协议
	 * 
	 * @param paraModOsp 开源协议, nvarchar, length:100, null:true, pk:false
	 */

	public void setModOsp(String paraModOsp) {
		super.recordChanged("mod_osp", this.modOsp_, paraModOsp);
		this.modOsp_ = paraModOsp;
	}

	/**
	 * 获取 模块说明
	 *
	 * @return 模块说明, nvarchar, length:2147483647, null:true, pk:false
	 */
	public String getModMemo() {
		return this.modMemo_;
	}

	/**
	 * 赋值 模块说明
	 * 
	 * @param paraModMemo 模块说明, nvarchar, length:2147483647, null:true, pk:false
	 */

	public void setModMemo(String paraModMemo) {
		super.recordChanged("mod_memo", this.modMemo_, paraModMemo);
		this.modMemo_ = paraModMemo;
	}

	/**
	 * 获取 模块说明英文
	 *
	 * @return 模块说明英文, nvarchar, length:2147483647, null:true, pk:false
	 */
	public String getModMemoEn() {
		return this.modMemoEn_;
	}

	/**
	 * 赋值 模块说明英文
	 * 
	 * @param paraModMemoEn 模块说明英文, nvarchar, length:2147483647, null:true, pk:false
	 */

	public void setModMemoEn(String paraModMemoEn) {
		super.recordChanged("mod_memo_en", this.modMemoEn_, paraModMemoEn);
		this.modMemoEn_ = paraModMemoEn;
	}

	/**
	 * 获取 创建时间
	 *
	 * @return 创建时间, datetime, length:23, null:true, pk:false
	 */
	public Date getModCdate() {
		return this.modCdate_;
	}

	/**
	 * 赋值 创建时间
	 * 
	 * @param paraModCdate 创建时间, datetime, length:23, null:true, pk:false
	 */

	public void setModCdate(Date paraModCdate) {
		super.recordChanged("mod_cdate", this.modCdate_, paraModCdate);
		this.modCdate_ = paraModCdate;
	}

	/**
	 * 获取 修改时间
	 *
	 * @return 修改时间, datetime, length:23, null:true, pk:false
	 */
	public Date getModMdate() {
		return this.modMdate_;
	}

	/**
	 * 赋值 修改时间
	 * 
	 * @param paraModMdate 修改时间, datetime, length:23, null:true, pk:false
	 */

	public void setModMdate(Date paraModMdate) {
		super.recordChanged("mod_mdate", this.modMdate_, paraModMdate);
		this.modMdate_ = paraModMdate;
	}

	/**
	 * 获取 商户
	 *
	 * @return 商户, int, length:10, null:true, pk:false
	 */
	public Integer getModSupId() {
		return this.modSupId_;
	}

	/**
	 * 赋值 商户
	 * 
	 * @param paraModSupId 商户, int, length:10, null:true, pk:false
	 */

	public void setModSupId(Integer paraModSupId) {
		super.recordChanged("mod_sup_id", this.modSupId_, paraModSupId);
		this.modSupId_ = paraModSupId;
	}

	/**
	 * 获取 版本编号
	 *
	 * @return 版本编号, bigint, length:19, null:true, pk:false
	 */
	public Long getModVerId() {
		return this.modVerId_;
	}

	/**
	 * 赋值 版本编号
	 * 
	 * @param paraModVerId 版本编号, bigint, length:19, null:true, pk:false
	 */

	public void setModVerId(Long paraModVerId) {
		super.recordChanged("mod_ver_id", this.modVerId_, paraModVerId);
		this.modVerId_ = paraModVerId;
	}

	/**
	 * 获取 创建时间
	 *
	 * @return 创建时间, datetime, length:23, null:true, pk:false
	 */
	public Date getModVerCdate() {
		return this.modVerCdate_;
	}

	/**
	 * 赋值 创建时间
	 * 
	 * @param paraModVerCdate 创建时间, datetime, length:23, null:true, pk:false
	 */

	public void setModVerCdate(Date paraModVerCdate) {
		super.recordChanged("mod_ver_cdate", this.modVerCdate_, paraModVerCdate);
		this.modVerCdate_ = paraModVerCdate;
	}

	/**
	 * 获取 修改时间
	 *
	 * @return 修改时间, datetime, length:23, null:true, pk:false
	 */
	public Date getModVerMdate() {
		return this.modVerMdate_;
	}

	/**
	 * 赋值 修改时间
	 * 
	 * @param paraModVerMdate 修改时间, datetime, length:23, null:true, pk:false
	 */

	public void setModVerMdate(Date paraModVerMdate) {
		super.recordChanged("mod_ver_mdate", this.modVerMdate_, paraModVerMdate);
		this.modVerMdate_ = paraModVerMdate;
	}

	/**
	 * 获取 模块版本说明
	 *
	 * @return 模块版本说明, nvarchar, length:2147483647, null:true, pk:false
	 */
	public String getModVerMemo() {
		return this.modVerMemo_;
	}

	/**
	 * 赋值 模块版本说明
	 * 
	 * @param paraModVerMemo 模块版本说明, nvarchar, length:2147483647, null:true,
	 *                       pk:false
	 */

	public void setModVerMemo(String paraModVerMemo) {
		super.recordChanged("mod_ver_memo", this.modVerMemo_, paraModVerMemo);
		this.modVerMemo_ = paraModVerMemo;
	}

	/**
	 * 获取 模块版本说明英文
	 *
	 * @return 模块版本说明英文, nvarchar, length:2147483647, null:true, pk:false
	 */
	public String getModVerMemoEn() {
		return this.modVerMemoEn_;
	}

	/**
	 * 赋值 模块版本说明英文
	 * 
	 * @param paraModVerMemoEn 模块版本说明英文, nvarchar, length:2147483647, null:true,
	 *                         pk:false
	 */

	public void setModVerMemoEn(String paraModVerMemoEn) {
		super.recordChanged("mod_ver_memo_en", this.modVerMemoEn_, paraModVerMemoEn);
		this.modVerMemoEn_ = paraModVerMemoEn;
	}

	/**
	 * 获取 模块状态
	 *
	 * @return 模块状态, nvarchar, length:10, null:true, pk:false
	 */
	public String getModVerStatus() {
		return this.modVerStatus_;
	}

	/**
	 * 赋值 模块状态
	 * 
	 * @param paraModVerStatus 模块状态, nvarchar, length:10, null:true, pk:false
	 */

	public void setModVerStatus(String paraModVerStatus) {
		super.recordChanged("mod_ver_status", this.modVerStatus_, paraModVerStatus);
		this.modVerStatus_ = paraModVerStatus;
	}

	/**
	 * 获取 包大小
	 *
	 * @return 包大小, int, length:10, null:false, pk:false
	 */
	public Integer getPkgLen() {
		return this.pkgLen_;
	}

	/**
	 * 赋值 包大小
	 * 
	 * @param paraPkgLen 包大小, int, length:10, null:false, pk:false
	 */

	public void setPkgLen(Integer paraPkgLen) {
		super.recordChanged("pkg_len", this.pkgLen_, paraPkgLen);
		this.pkgLen_ = paraPkgLen;
	}

	/**
	 * 获取 包md5
	 *
	 * @return 包md5, nvarchar, length:32, null:false, pk:false
	 */
	public String getPkgMd5() {
		return this.pkgMd5_;
	}

	/**
	 * 赋值 包md5
	 * 
	 * @param paraPkgMd5 包md5, nvarchar, length:32, null:false, pk:false
	 */

	public void setPkgMd5(String paraPkgMd5) {
		super.recordChanged("pkg_md5", this.pkgMd5_, paraPkgMd5);
		this.pkgMd5_ = paraPkgMd5;
	}

	/**
	 * 获取 包内容
	 *
	 * @return 包内容, varbinary, length:2147483647, null:true, pk:false
	 */
	public byte[] getPkgFile() {
		return this.pkgFile_;
	}

	/**
	 * 赋值 包内容
	 * 
	 * @param paraPkgFile 包内容, varbinary, length:2147483647, null:true, pk:false
	 */

	public void setPkgFile(byte[] paraPkgFile) {
		super.recordChanged("pkg_file", this.pkgFile_, paraPkgFile);
		this.pkgFile_ = paraPkgFile;
	}

	/**
	 * 获取 创建时间
	 *
	 * @return 创建时间, datetime, length:23, null:false, pk:false
	 */
	public Date getModDlCdate() {
		return this.modDlCdate_;
	}

	/**
	 * 赋值 创建时间
	 * 
	 * @param paraModDlCdate 创建时间, datetime, length:23, null:false, pk:false
	 */

	public void setModDlCdate(Date paraModDlCdate) {
		super.recordChanged("mod_dl_cdate", this.modDlCdate_, paraModDlCdate);
		this.modDlCdate_ = paraModDlCdate;
	}

	/**
	 * 获取 修改时间
	 *
	 * @return 修改时间, datetime, length:23, null:false, pk:false
	 */
	public Date getModDlMdate() {
		return this.modDlMdate_;
	}

	/**
	 * 赋值 修改时间
	 * 
	 * @param paraModDlMdate 修改时间, datetime, length:23, null:false, pk:false
	 */

	public void setModDlMdate(Date paraModDlMdate) {
		super.recordChanged("mod_dl_mdate", this.modDlMdate_, paraModDlMdate);
		this.modDlMdate_ = paraModDlMdate;
	}

	/**
	 * 获取 模块状态- used,del
	 *
	 * @return 模块状态- used,del, nvarchar, length:10, null:false, pk:false
	 */
	public String getModDlStatus() {
		return this.modDlStatus_;
	}

	/**
	 * 赋值 模块状态- used,del
	 * 
	 * @param paraModDlStatus 模块状态- used,del, nvarchar, length:10, null:false,
	 *                        pk:false
	 */

	public void setModDlStatus(String paraModDlStatus) {
		super.recordChanged("mod_dl_status", this.modDlStatus_, paraModDlStatus);
		this.modDlStatus_ = paraModDlStatus;
	}

	/**
	 * 获取 商户
	 *
	 * @return 商户, int, length:10, null:false, pk:false
	 */
	public Integer getModDlSupId() {
		return this.modDlSupId_;
	}

	/**
	 * 赋值 商户
	 * 
	 * @param paraModDlSupId 商户, int, length:10, null:false, pk:false
	 */

	public void setModDlSupId(Integer paraModDlSupId) {
		super.recordChanged("mod_dl_sup_id", this.modDlSupId_, paraModDlSupId);
		this.modDlSupId_ = paraModDlSupId;
	}

	/**
	 * 获取 下载网址
	 *
	 * @return 下载网址, nvarchar, length:700, null:true, pk:false
	 */
	public String getModDlUrl() {
		return this.modDlUrl_;
	}

	/**
	 * 赋值 下载网址
	 * 
	 * @param paraModDlUrl 下载网址, nvarchar, length:700, null:true, pk:false
	 */

	public void setModDlUrl(String paraModDlUrl) {
		super.recordChanged("mod_dl_url", this.modDlUrl_, paraModDlUrl);
		this.modDlUrl_ = paraModDlUrl;
	}

	/**
	 * 获取 导入数据的连接名称
	 *
	 * @return 导入数据的连接名称, nvarchar, length:50, null:true, pk:false
	 */
	public String getImportDataConn() {
		return this.importDataConn_;
	}

	/**
	 * 赋值 导入数据的连接名称
	 * 
	 * @param paraImportDataConn 导入数据的连接名称, nvarchar, length:50, null:true, pk:false
	 */

	public void setImportDataConn(String paraImportDataConn) {
		super.recordChanged("import_data_conn", this.importDataConn_, paraImportDataConn);
		this.importDataConn_ = paraImportDataConn;
	}

	/**
	 * 获取 替换元数据库名称
	 *
	 * @return 替换元数据库名称, nvarchar, length:64, null:true, pk:false
	 */
	public String getReplaceMetaDatabasename() {
		return this.replaceMetaDatabasename_;
	}

	/**
	 * 赋值 替换元数据库名称
	 * 
	 * @param paraReplaceMetaDatabasename 替换元数据库名称, nvarchar, length:64, null:true,
	 *                                    pk:false
	 */

	public void setReplaceMetaDatabasename(String paraReplaceMetaDatabasename) {
		super.recordChanged("replace_meta_databasename", this.replaceMetaDatabasename_, paraReplaceMetaDatabasename);
		this.replaceMetaDatabasename_ = paraReplaceMetaDatabasename;
	}

	/**
	 * 获取 替换工作据库名称
	 *
	 * @return 替换工作据库名称, nvarchar, length:64, null:true, pk:false
	 */
	public String getReplaceWorkDatabasename() {
		return this.replaceWorkDatabasename_;
	}

	/**
	 * 赋值 替换工作据库名称
	 * 
	 * @param paraReplaceWorkDatabasename 替换工作据库名称, nvarchar, length:64, null:true,
	 *                                    pk:false
	 */

	public void setReplaceWorkDatabasename(String paraReplaceWorkDatabasename) {
		super.recordChanged("replace_work_databasename", this.replaceWorkDatabasename_, paraReplaceWorkDatabasename);
		this.replaceWorkDatabasename_ = paraReplaceWorkDatabasename;
	}
}