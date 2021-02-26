package com.gdxsoft.easyweb.script.Workflow;

import org.json.JSONObject;

import com.gdxsoft.easyweb.datasource.ClassBase;

/**
 * 表_EWA_WF_CNN映射类
 * 
 * @author gdx 时间：Sat Jul 11 2020 20:59:07 GMT+0800 (中国标准时间)
 */
public class EwaWfCnn extends ClassBase {
	private String wfUnitFrom_; // 来源
	private String wfUnitTo_; // 目标
	private String wfId_; // 流程
	private String wfRefId_; // 参考来源
	private Integer wfCnnOrd_; // 排序
	private String wfCnnLogic_; // 逻辑
	private String wfCnnName_; // 连接名称
	private String wfCnnMemo_; // 连接备注

	private boolean _IsHaveLogic;
	private boolean _IsLogicOk;

	/**
	 * @return the _IsHaveLogic
	 */
	public boolean isHaveLogic() {
		return _IsHaveLogic;
	}

	/**
	 * @param isHaveLogic the _IsHaveLogic to set
	 */
	public void setIsHaveLogic(boolean isHaveLogic) {
		_IsHaveLogic = isHaveLogic;
	}

	/**
	 * @return the _IsLogicOk
	 */
	public boolean isLogicOk() {
		return _IsLogicOk;
	}

	/**
	 * @param isLogicOk the _IsLogicOk to set
	 */
	public void setIsLogicOk(boolean isLogicOk) {
		_IsLogicOk = isLogicOk;
	}

	/**
	 * 获取 来源
	 *
	 * @return 来源
	 */
	public String getWfUnitFrom() {
		return this.wfUnitFrom_;
	}

	/**
	 * 赋值 来源
	 * 
	 * @param paraWfUnitFrom 来源
	 */

	public void setWfUnitFrom(String paraWfUnitFrom) {
		super.recordChanged("WF_UNIT_FROM", this.wfUnitFrom_, paraWfUnitFrom);
		this.wfUnitFrom_ = paraWfUnitFrom;
	}

	/**
	 * 获取 目标
	 *
	 * @return 目标
	 */
	public String getWfUnitTo() {
		return this.wfUnitTo_;
	}

	/**
	 * 赋值 目标
	 * 
	 * @param paraWfUnitTo 目标
	 */

	public void setWfUnitTo(String paraWfUnitTo) {
		super.recordChanged("WF_UNIT_TO", this.wfUnitTo_, paraWfUnitTo);
		this.wfUnitTo_ = paraWfUnitTo;
	}

	/**
	 * 获取 流程
	 *
	 * @return 流程
	 */
	public String getWfId() {
		return this.wfId_;
	}

	/**
	 * 赋值 流程
	 * 
	 * @param paraWfId 流程
	 */

	public void setWfId(String paraWfId) {
		super.recordChanged("WF_ID", this.wfId_, paraWfId);
		this.wfId_ = paraWfId;
	}

	/**
	 * 获取 参考来源
	 *
	 * @return 参考来源
	 */
	public String getWfRefId() {
		return this.wfRefId_;
	}

	/**
	 * 赋值 参考来源
	 * 
	 * @param paraWfRefId 参考来源
	 */

	public void setWfRefId(String paraWfRefId) {
		super.recordChanged("WF_REF_ID", this.wfRefId_, paraWfRefId);
		this.wfRefId_ = paraWfRefId;
	}

	/**
	 * 获取 排序
	 *
	 * @return 排序
	 */
	public Integer getWfCnnOrd() {
		return this.wfCnnOrd_;
	}

	/**
	 * 赋值 排序
	 * 
	 * @param paraWfCnnOrd 排序
	 */

	public void setWfCnnOrd(Integer paraWfCnnOrd) {
		super.recordChanged("WF_CNN_ORD", this.wfCnnOrd_, paraWfCnnOrd);
		this.wfCnnOrd_ = paraWfCnnOrd;
	}

	/**
	 * 获取 逻辑
	 *
	 * @return 逻辑
	 */
	public String getWfCnnLogic() {
		return this.wfCnnLogic_;
	}

	/**
	 * 赋值 逻辑
	 * 
	 * @param paraWfCnnLogic 逻辑
	 */

	public void setWfCnnLogic(String paraWfCnnLogic) {
		super.recordChanged("WF_CNN_LOGIC", this.wfCnnLogic_, paraWfCnnLogic);
		this.wfCnnLogic_ = paraWfCnnLogic;
	}

	/**
	 * 获取 连接名称
	 *
	 * @return 连接名称
	 */
	public String getWfCnnName() {
		return this.wfCnnName_;
	}

	/**
	 * 赋值 连接名称
	 * 
	 * @param paraWfCnnName 连接名称
	 */

	public void setWfCnnName(String paraWfCnnName) {
		super.recordChanged("WF_CNN_NAME", this.wfCnnName_, paraWfCnnName);
		this.wfCnnName_ = paraWfCnnName;
	}

	/**
	 * 获取 连接备注
	 *
	 * @return 连接备注
	 */
	public String getWfCnnMemo() {
		return this.wfCnnMemo_;
	}

	/**
	 * 赋值 连接备注
	 * 
	 * @param paraWfCnnMemo 连接备注
	 */

	public void setWfCnnMemo(String paraWfCnnMemo) {
		super.recordChanged("WF_CNN_MEMO", this.wfCnnMemo_, paraWfCnnMemo);
		this.wfCnnMemo_ = paraWfCnnMemo;
	}

	public void setField(String filedName, Object val) {
		if (filedName == null) {
			return;
		}
		String v1 = val == null ? null : val.toString();
		String n = filedName.trim().toUpperCase();
		if (n.equalsIgnoreCase("WF_UNIT_FROM")) {
			this.wfUnitFrom_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_TO")) {
			this.wfUnitTo_ = v1;
		} else if (n.equalsIgnoreCase("WF_ID")) {
			this.wfId_ = v1;
		} else if (n.equalsIgnoreCase("WF_CNN_ORD")) {
			if (val != JSONObject.NULL) {
				this.wfCnnOrd_ = Integer.parseInt(v1);
			}
		} else if (n.equalsIgnoreCase("WF_CNN_LOGIC")) {
			this.wfCnnLogic_ = v1;
		} else if (n.equalsIgnoreCase("WF_CNN_NAME")) {
			this.wfCnnName_ = v1;
		} else if (n.equalsIgnoreCase("WF_CNN_MEMO")) {
			this.wfCnnMemo_ = v1;
		}
	}

}