package com.gdxsoft.easyweb.script.Workflow;

import com.gdxsoft.easyweb.datasource.ClassBase;
/**表_EWA_WF映射类
* @author gdx 时间：Sat Jul 11 2020 20:59:31 GMT+0800 (中国标准时间)*/
public class EwaWf extends ClassBase{private String wfId_; // 流程编号
private String wfName_; // 名称
private String wfMemo_; // 备注
private String wfStatus_; // 状态
private String wfTable_; // 关联表
private String wfField_; // 关联字段
private String wfPara0_; // 写日志SQL
private String wfPara1_; // 参数1
private String wfPara2_; // 参数2
private String wfPara3_; // 参数3
private String wfPara4_; // 参数4
private String wfPara5_; // 参数5
private String wfPara6_; // 参数6
private String wfPara7_; // 参数7
private String wfPara8_; // 参数8
private String wfPara9_; // 参数9
private String wfNameEn_; // WF_NAME_EN
private String wfMemoEn_; // WF_MEMO_EN

/**
 * 获取 流程编号
 *
* @return 流程编号
*/
public String getWfId() {return this.wfId_;}
/**
* 赋值 流程编号

* @param paraWfId
* 流程编号
 */

public void setWfId(String paraWfId){
  super.recordChanged("WF_ID", this.wfId_, paraWfId);
  this.wfId_ = paraWfId;
}


/**
 * 获取 名称
 *
* @return 名称
*/
public String getWfName() {return this.wfName_;}
/**
* 赋值 名称

* @param paraWfName
* 名称
 */

public void setWfName(String paraWfName){
  super.recordChanged("WF_NAME", this.wfName_, paraWfName);
  this.wfName_ = paraWfName;
}


/**
 * 获取 备注
 *
* @return 备注
*/
public String getWfMemo() {return this.wfMemo_;}
/**
* 赋值 备注

* @param paraWfMemo
* 备注
 */

public void setWfMemo(String paraWfMemo){
  super.recordChanged("WF_MEMO", this.wfMemo_, paraWfMemo);
  this.wfMemo_ = paraWfMemo;
}


/**
 * 获取 状态
 *
* @return 状态
*/
public String getWfStatus() {return this.wfStatus_;}
/**
* 赋值 状态

* @param paraWfStatus
* 状态
 */

public void setWfStatus(String paraWfStatus){
  super.recordChanged("WF_STATUS", this.wfStatus_, paraWfStatus);
  this.wfStatus_ = paraWfStatus;
}


/**
 * 获取 关联表
 *
* @return 关联表
*/
public String getWfTable() {return this.wfTable_;}
/**
* 赋值 关联表

* @param paraWfTable
* 关联表
 */

public void setWfTable(String paraWfTable){
  super.recordChanged("WF_TABLE", this.wfTable_, paraWfTable);
  this.wfTable_ = paraWfTable;
}


/**
 * 获取 关联字段
 *
* @return 关联字段
*/
public String getWfField() {return this.wfField_;}
/**
* 赋值 关联字段

* @param paraWfField
* 关联字段
 */

public void setWfField(String paraWfField){
  super.recordChanged("WF_FIELD", this.wfField_, paraWfField);
  this.wfField_ = paraWfField;
}


/**
 * 获取 写日志SQL
 *
* @return 写日志SQL
*/
public String getWfPara0() {return this.wfPara0_;}
/**
* 赋值 写日志SQL

* @param paraWfPara0
* 写日志SQL
 */

public void setWfPara0(String paraWfPara0){
  super.recordChanged("WF_PARA0", this.wfPara0_, paraWfPara0);
  this.wfPara0_ = paraWfPara0;
}


/**
 * 获取 参数1
 *
* @return 参数1
*/
public String getWfPara1() {return this.wfPara1_;}
/**
* 赋值 参数1

* @param paraWfPara1
* 参数1
 */

public void setWfPara1(String paraWfPara1){
  super.recordChanged("WF_PARA1", this.wfPara1_, paraWfPara1);
  this.wfPara1_ = paraWfPara1;
}


/**
 * 获取 参数2
 *
* @return 参数2
*/
public String getWfPara2() {return this.wfPara2_;}
/**
* 赋值 参数2

* @param paraWfPara2
* 参数2
 */

public void setWfPara2(String paraWfPara2){
  super.recordChanged("WF_PARA2", this.wfPara2_, paraWfPara2);
  this.wfPara2_ = paraWfPara2;
}


/**
 * 获取 参数3
 *
* @return 参数3
*/
public String getWfPara3() {return this.wfPara3_;}
/**
* 赋值 参数3

* @param paraWfPara3
* 参数3
 */

public void setWfPara3(String paraWfPara3){
  super.recordChanged("WF_PARA3", this.wfPara3_, paraWfPara3);
  this.wfPara3_ = paraWfPara3;
}


/**
 * 获取 参数4
 *
* @return 参数4
*/
public String getWfPara4() {return this.wfPara4_;}
/**
* 赋值 参数4

* @param paraWfPara4
* 参数4
 */

public void setWfPara4(String paraWfPara4){
  super.recordChanged("WF_PARA4", this.wfPara4_, paraWfPara4);
  this.wfPara4_ = paraWfPara4;
}


/**
 * 获取 参数5
 *
* @return 参数5
*/
public String getWfPara5() {return this.wfPara5_;}
/**
* 赋值 参数5

* @param paraWfPara5
* 参数5
 */

public void setWfPara5(String paraWfPara5){
  super.recordChanged("WF_PARA5", this.wfPara5_, paraWfPara5);
  this.wfPara5_ = paraWfPara5;
}


/**
 * 获取 参数6
 *
* @return 参数6
*/
public String getWfPara6() {return this.wfPara6_;}
/**
* 赋值 参数6

* @param paraWfPara6
* 参数6
 */

public void setWfPara6(String paraWfPara6){
  super.recordChanged("WF_PARA6", this.wfPara6_, paraWfPara6);
  this.wfPara6_ = paraWfPara6;
}


/**
 * 获取 参数7
 *
* @return 参数7
*/
public String getWfPara7() {return this.wfPara7_;}
/**
* 赋值 参数7

* @param paraWfPara7
* 参数7
 */

public void setWfPara7(String paraWfPara7){
  super.recordChanged("WF_PARA7", this.wfPara7_, paraWfPara7);
  this.wfPara7_ = paraWfPara7;
}


/**
 * 获取 参数8
 *
* @return 参数8
*/
public String getWfPara8() {return this.wfPara8_;}
/**
* 赋值 参数8

* @param paraWfPara8
* 参数8
 */

public void setWfPara8(String paraWfPara8){
  super.recordChanged("WF_PARA8", this.wfPara8_, paraWfPara8);
  this.wfPara8_ = paraWfPara8;
}


/**
 * 获取 参数9
 *
* @return 参数9
*/
public String getWfPara9() {return this.wfPara9_;}
/**
* 赋值 参数9

* @param paraWfPara9
* 参数9
 */

public void setWfPara9(String paraWfPara9){
  super.recordChanged("WF_PARA9", this.wfPara9_, paraWfPara9);
  this.wfPara9_ = paraWfPara9;
}


/**
 * 获取 WF_NAME_EN
 *
* @return WF_NAME_EN
*/
public String getWfNameEn() {return this.wfNameEn_;}
/**
* 赋值 WF_NAME_EN

* @param paraWfNameEn
* WF_NAME_EN
 */

public void setWfNameEn(String paraWfNameEn){
  super.recordChanged("WF_NAME_EN", this.wfNameEn_, paraWfNameEn);
  this.wfNameEn_ = paraWfNameEn;
}


/**
 * 获取 WF_MEMO_EN
 *
* @return WF_MEMO_EN
*/
public String getWfMemoEn() {return this.wfMemoEn_;}
/**
* 赋值 WF_MEMO_EN

* @param paraWfMemoEn
* WF_MEMO_EN
 */

public void setWfMemoEn(String paraWfMemoEn){
  super.recordChanged("WF_MEMO_EN", this.wfMemoEn_, paraWfMemoEn);
  this.wfMemoEn_ = paraWfMemoEn;
}
}