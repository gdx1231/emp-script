package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;

import org.json.JSONObject;

import com.gdxsoft.easyweb.datasource.ClassBase;

/**
 * 表_EWA_WF_UNIT映射类
 * 
 * @author gdx 时间：Fri Feb 15 2019 09:15:32 GMT+0800 (中国标准时间)
 */
public class EwaWfUnit extends ClassBase {

	private ArrayList<EwaWfCnn> _FromCnns = new ArrayList<EwaWfCnn>();
	private ArrayList<EwaWfCnn> _ToCnns = new ArrayList<EwaWfCnn>();

	/**
	 * @return the _FromCnns
	 */
	public ArrayList<EwaWfCnn> getFromCnns() {
		return _FromCnns;
	}

	/**
	 * @return the _ToCnns
	 */
	public ArrayList<EwaWfCnn> getToCnns() {
		return _ToCnns;
	}

	public String toJson() {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < EwaWfUnitDao.FIELD_LIST.length; i++) {
			String f = EwaWfUnitDao.FIELD_LIST[i];
			Object o = this.getField(f);
			String v = o == null ? null : o.toString().trim();
			obj.put(f, v);
		}
		return obj.toString();
	}

	@Override
	public String toString() {
		return this.wfUnitId_ + "[" + this.wfUnitName_ + "]";
	}

	private String wfUnitId_; // 编号
	private String wfId_; // 流程编号
	private String wfRefId_; // 来源
	private String wfUnitName_; // 名称
	private String wfUnitMemo_; // 备注
	private String wfUnitType_; // 类型
	private String wfUnitAdm_; // 操作者类型
	private String wfUnitAdmLst_; // 操作者列表
	private String wfUnitPara0_; // WF_UNIT_PARA0
	private String wfUnitPara1_; // WF_UNIT_PARA1
	private String wfUnitPara2_; // WF_UNIT_PARA2
	private String wfUnitPara3_; // 检查参考
	private String wfUnitPara4_; // 执行SQL参考
	private Integer wfUnitX_; // X坐标
	private Integer wfUnitY_; // Y坐标
	private String wfUnitActBef_; // 执行前检查
	private String wfUnitActAft_; // 执行后事件
	private String wfUnitSelfDept_; // 是否本部门
	private String wfUnitMemoEn_; // 英文备注
	private String wfUnitNameEn_; // 英文名称
	private String wfUnitNotify_; // 通知

	/**
	 * 获取 编号
	 *
	 * @return 编号
	 */
	public String getWfUnitId() {
		return this.wfUnitId_;
	}

	/**
	 * 赋值 编号
	 * 
	 * @param paraWfUnitId 编号
	 */

	public void setWfUnitId(String paraWfUnitId) {
		super.recordChanged("WF_UNIT_ID", this.wfUnitId_, paraWfUnitId);
		this.wfUnitId_ = paraWfUnitId;
	}

	/**
	 * 获取 流程编号
	 *
	 * @return 流程编号
	 */
	public String getWfId() {
		return this.wfId_;
	}

	/**
	 * 赋值 流程编号
	 * 
	 * @param paraWfId 流程编号
	 */

	public void setWfId(String paraWfId) {
		super.recordChanged("WF_ID", this.wfId_, paraWfId);
		this.wfId_ = paraWfId;
	}

	/**
	 * 获取 来源
	 *
	 * @return 来源
	 */
	public String getWfRefId() {
		return this.wfRefId_;
	}

	/**
	 * 赋值 来源
	 * 
	 * @param paraWfRefId 来源
	 */

	public void setWfRefId(String paraWfRefId) {
		super.recordChanged("WF_REF_ID", this.wfRefId_, paraWfRefId);
		this.wfRefId_ = paraWfRefId;
	}

	/**
	 * 获取 名称
	 *
	 * @return 名称
	 */
	public String getWfUnitName() {
		return this.wfUnitName_;
	}

	/**
	 * 赋值 名称
	 * 
	 * @param paraWfUnitName 名称
	 */

	public void setWfUnitName(String paraWfUnitName) {
		super.recordChanged("WF_UNIT_NAME", this.wfUnitName_, paraWfUnitName);
		this.wfUnitName_ = paraWfUnitName;
	}

	/**
	 * 获取 备注
	 *
	 * @return 备注
	 */
	public String getWfUnitMemo() {
		return this.wfUnitMemo_;
	}

	/**
	 * 赋值 备注
	 * 
	 * @param paraWfUnitMemo 备注
	 */

	public void setWfUnitMemo(String paraWfUnitMemo) {
		super.recordChanged("WF_UNIT_MEMO", this.wfUnitMemo_, paraWfUnitMemo);
		this.wfUnitMemo_ = paraWfUnitMemo;
	}

	/**
	 * 获取 类型
	 *
	 * @return 类型
	 */
	public String getWfUnitType() {
		return this.wfUnitType_;
	}

	/**
	 * 赋值 类型
	 * 
	 * @param paraWfUnitType 类型
	 */

	public void setWfUnitType(String paraWfUnitType) {
		super.recordChanged("WF_UNIT_TYPE", this.wfUnitType_, paraWfUnitType);
		this.wfUnitType_ = paraWfUnitType;
	}

	/**
	 * 获取 操作者类型
	 *
	 * @return 操作者类型
	 */
	public String getWfUnitAdm() {
		return this.wfUnitAdm_;
	}

	/**
	 * 赋值 操作者类型
	 * 
	 * @param paraWfUnitAdm 操作者类型
	 */

	public void setWfUnitAdm(String paraWfUnitAdm) {
		super.recordChanged("WF_UNIT_ADM", this.wfUnitAdm_, paraWfUnitAdm);
		this.wfUnitAdm_ = paraWfUnitAdm;
	}

	/**
	 * 获取 操作者列表
	 *
	 * @return 操作者列表
	 */
	public String getWfUnitAdmLst() {
		return this.wfUnitAdmLst_;
	}

	/**
	 * 赋值 操作者列表
	 * 
	 * @param paraWfUnitAdmLst 操作者列表
	 */

	public void setWfUnitAdmLst(String paraWfUnitAdmLst) {
		super.recordChanged("WF_UNIT_ADM_LST", this.wfUnitAdmLst_, paraWfUnitAdmLst);
		this.wfUnitAdmLst_ = paraWfUnitAdmLst;
	}

	/**
	 * 获取 WF_UNIT_PARA0
	 *
	 * @return WF_UNIT_PARA0
	 */
	public String getWfUnitPara0() {
		return this.wfUnitPara0_;
	}

	/**
	 * 赋值 WF_UNIT_PARA0
	 * 
	 * @param paraWfUnitPara0 WF_UNIT_PARA0
	 */

	public void setWfUnitPara0(String paraWfUnitPara0) {
		super.recordChanged("WF_UNIT_PARA0", this.wfUnitPara0_, paraWfUnitPara0);
		this.wfUnitPara0_ = paraWfUnitPara0;
	}

	/**
	 * 获取 WF_UNIT_PARA1
	 *
	 * @return WF_UNIT_PARA1
	 */
	public String getWfUnitPara1() {
		return this.wfUnitPara1_;
	}

	/**
	 * 赋值 WF_UNIT_PARA1
	 * 
	 * @param paraWfUnitPara1 WF_UNIT_PARA1
	 */

	public void setWfUnitPara1(String paraWfUnitPara1) {
		super.recordChanged("WF_UNIT_PARA1", this.wfUnitPara1_, paraWfUnitPara1);
		this.wfUnitPara1_ = paraWfUnitPara1;
	}

	/**
	 * 获取 WF_UNIT_PARA2
	 *
	 * @return WF_UNIT_PARA2
	 */
	public String getWfUnitPara2() {
		return this.wfUnitPara2_;
	}

	/**
	 * 赋值 WF_UNIT_PARA2
	 * 
	 * @param paraWfUnitPara2 WF_UNIT_PARA2
	 */

	public void setWfUnitPara2(String paraWfUnitPara2) {
		super.recordChanged("WF_UNIT_PARA2", this.wfUnitPara2_, paraWfUnitPara2);
		this.wfUnitPara2_ = paraWfUnitPara2;
	}

	/**
	 * 获取 检查参考
	 *
	 * @return 检查参考
	 */
	public String getWfUnitPara3() {
		return this.wfUnitPara3_;
	}

	/**
	 * 赋值 检查参考
	 * 
	 * @param paraWfUnitPara3 检查参考
	 */

	public void setWfUnitPara3(String paraWfUnitPara3) {
		super.recordChanged("WF_UNIT_PARA3", this.wfUnitPara3_, paraWfUnitPara3);
		this.wfUnitPara3_ = paraWfUnitPara3;
	}

	/**
	 * 获取 执行SQL参考
	 *
	 * @return 执行SQL参考
	 */
	public String getWfUnitPara4() {
		return this.wfUnitPara4_;
	}

	/**
	 * 赋值 执行SQL参考
	 * 
	 * @param paraWfUnitPara4 执行SQL参考
	 */

	public void setWfUnitPara4(String paraWfUnitPara4) {
		super.recordChanged("WF_UNIT_PARA4", this.wfUnitPara4_, paraWfUnitPara4);
		this.wfUnitPara4_ = paraWfUnitPara4;
	}

	/**
	 * 获取 X坐标
	 *
	 * @return X坐标
	 */
	public Integer getWfUnitX() {
		return this.wfUnitX_;
	}

	/**
	 * 赋值 X坐标
	 * 
	 * @param paraWfUnitX X坐标
	 */

	public void setWfUnitX(Integer paraWfUnitX) {
		super.recordChanged("WF_UNIT_X", this.wfUnitX_, paraWfUnitX);
		this.wfUnitX_ = paraWfUnitX;
	}

	/**
	 * 获取 Y坐标
	 *
	 * @return Y坐标
	 */
	public Integer getWfUnitY() {
		return this.wfUnitY_;
	}

	/**
	 * 赋值 Y坐标
	 * 
	 * @param paraWfUnitY Y坐标
	 */

	public void setWfUnitY(Integer paraWfUnitY) {
		super.recordChanged("WF_UNIT_Y", this.wfUnitY_, paraWfUnitY);
		this.wfUnitY_ = paraWfUnitY;
	}

	/**
	 * 获取 执行前检查
	 *
	 * @return 执行前检查
	 */
	public String getWfUnitActBef() {
		return this.wfUnitActBef_;
	}

	/**
	 * 赋值 执行前检查
	 * 
	 * @param paraWfUnitActBef 执行前检查
	 */

	public void setWfUnitActBef(String paraWfUnitActBef) {
		super.recordChanged("WF_UNIT_ACT_BEF", this.wfUnitActBef_, paraWfUnitActBef);
		this.wfUnitActBef_ = paraWfUnitActBef;
	}

	/**
	 * 获取 执行后事件
	 *
	 * @return 执行后事件
	 */
	public String getWfUnitActAft() {
		return this.wfUnitActAft_;
	}

	/**
	 * 赋值 执行后事件
	 * 
	 * @param paraWfUnitActAft 执行后事件
	 */

	public void setWfUnitActAft(String paraWfUnitActAft) {
		super.recordChanged("WF_UNIT_ACT_AFT", this.wfUnitActAft_, paraWfUnitActAft);
		this.wfUnitActAft_ = paraWfUnitActAft;
	}

	/**
	 * 获取 是否本部门
	 *
	 * @return 是否本部门
	 */
	public String getWfUnitSelfDept() {
		return this.wfUnitSelfDept_;
	}

	/**
	 * 赋值 是否本部门
	 * 
	 * @param paraWfUnitSelfDept 是否本部门
	 */

	public void setWfUnitSelfDept(String paraWfUnitSelfDept) {
		super.recordChanged("WF_UNIT_SELF_DEPT", this.wfUnitSelfDept_, paraWfUnitSelfDept);
		this.wfUnitSelfDept_ = paraWfUnitSelfDept;
	}

	/**
	 * 获取 英文备注
	 *
	 * @return 英文备注
	 */
	public String getWfUnitMemoEn() {
		return this.wfUnitMemoEn_;
	}

	/**
	 * 赋值 英文备注
	 * 
	 * @param paraWfUnitMemoEn 英文备注
	 */

	public void setWfUnitMemoEn(String paraWfUnitMemoEn) {
		super.recordChanged("WF_UNIT_MEMO_EN", this.wfUnitMemoEn_, paraWfUnitMemoEn);
		this.wfUnitMemoEn_ = paraWfUnitMemoEn;
	}

	/**
	 * 获取 英文名称
	 *
	 * @return 英文名称
	 */
	public String getWfUnitNameEn() {
		return this.wfUnitNameEn_;
	}

	/**
	 * 赋值 英文名称
	 * 
	 * @param paraWfUnitNameEn 英文名称
	 */

	public void setWfUnitNameEn(String paraWfUnitNameEn) {
		super.recordChanged("WF_UNIT_NAME_EN", this.wfUnitNameEn_, paraWfUnitNameEn);
		this.wfUnitNameEn_ = paraWfUnitNameEn;
	}

	/**
	 * 获取 通知
	 *
	 * @return 通知
	 */
	public String getWfUnitNotify() {
		return this.wfUnitNotify_;
	}

	/**
	 * 赋值 通知
	 * 
	 * @param paraWfUnitNotify 通知
	 */

	public void setWfUnitNotify(String paraWfUnitNotify) {
		super.recordChanged("WF_UNIT_NOTIFY", this.wfUnitNotify_, paraWfUnitNotify);
		this.wfUnitNotify_ = paraWfUnitNotify;
	}

	/**
	 * 根据字段名称获取值，如果名称为空或字段未找到，返回空值
	 * 
	 * @param filedName 字段名称
	 * @return 字段值
	 */
	public Object getField(String filedName) {
		if (filedName == null) {
			return null;
		}
		String n = filedName.trim().toUpperCase();
		if (n.equalsIgnoreCase("WF_UNIT_ID")) {
			return this.wfUnitId_;
		}
		if (n.equalsIgnoreCase("WF_ID")) {
			return this.wfId_;
		}
		if (n.equalsIgnoreCase("WF_REF_ID")) {
			return this.wfRefId_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_NAME")) {
			return this.wfUnitName_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_MEMO")) {
			return this.wfUnitMemo_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_TYPE")) {
			return this.wfUnitType_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_ADM")) {
			return this.wfUnitAdm_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_ADM_LST")) {
			return this.wfUnitAdmLst_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_PARA0")) {
			return this.wfUnitPara0_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_PARA1")) {
			return this.wfUnitPara1_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_PARA2")) {
			return this.wfUnitPara2_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_PARA3")) {
			return this.wfUnitPara3_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_PARA4")) {
			return this.wfUnitPara4_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_X")) {
			return this.wfUnitX_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_Y")) {
			return this.wfUnitY_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_ACT_BEF")) {
			return this.wfUnitActBef_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_ACT_AFT")) {
			return this.wfUnitActAft_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_SELF_DEPT")) {
			return this.wfUnitSelfDept_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_MEMO_EN")) {
			return this.wfUnitMemoEn_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_NAME_EN")) {
			return this.wfUnitNameEn_;
		}
		if (n.equalsIgnoreCase("WF_UNIT_NOTIFY")) {
			return this.wfUnitNotify_;
		}
		return null;
	}

	/*
	 * 对象赋值
	 */
	public void setField(String filedName, Object val) {
		if (filedName == null) {
			return;
		}
		String v1 = val == null ? null : val.toString();
		String n = filedName.trim().toUpperCase();
		if (n.equalsIgnoreCase("WF_UNIT_ID")) {
			this.wfUnitId_ = v1;
		} else if (n.equalsIgnoreCase("WF_ID")) {
			this.wfId_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_NAME")) {
			this.wfUnitName_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_MEMO")) {
			this.wfUnitMemo_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_TYPE")) {
			this.wfUnitType_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_ADM")) {
			this.wfUnitAdm_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_ADM_LST")) {
			this.wfUnitAdmLst_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_PARA0")) {
			this.wfUnitPara0_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_PARA1")) {
			this.wfUnitPara1_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_PARA2")) {
			this.wfUnitPara2_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_PARA3")) {
			this.wfUnitPara3_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_PARA4")) {
			this.wfUnitPara4_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_X")) {
			this.wfUnitX_ = Integer.parseInt(v1);
		} else if (n.equalsIgnoreCase("WF_UNIT_Y")) {
			this.wfUnitY_ = Integer.parseInt(v1);
		} else if (n.equalsIgnoreCase("WF_UNIT_ACT_BEF")) {
			this.wfUnitActBef_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_ACT_AFT")) {
			this.wfUnitActAft_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_SELF_DEPT")) {
			this.wfUnitSelfDept_ = v1;
		} else if (n.equalsIgnoreCase("WF_UNIT_NOTIFY")) {
			this.wfUnitNotify_ = v1;
		}
	}

}