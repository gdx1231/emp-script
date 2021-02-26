package com.gdxsoft.easyweb.script.Workflow;

public class WfUnit {

	private String _Name;
	private String _Des;
	private String _WfType;
	private String _WFABefore;
	private String _WFABeforeMsg;
	private String _WFAAfter;
	private String _WFANextYes;
	private String _WFANextNo;
	private boolean _IsLast;
	private boolean _IsFirst;
	private String _WfLogic;
	private String _WFAParent;
	/**
	 * 名称
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 名称
	 * 
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * 描述
	 * 
	 * @return the _Des
	 */
	public String getDes() {
		return _Des;
	}

	/**
	 * 描述
	 * 
	 * @param des
	 *            the _Des to set
	 */
	public void setDes(String des) {
		_Des = des;
	}

	/**
	 * 提交前检查的Action
	 * 
	 * @return the _WFABefore
	 */
	public String getWFABefore() {
		return _WFABefore;
	}

	/**
	 * 提交前检查的Action
	 * 
	 * @param before
	 *            the _WFABefore to set
	 */
	public void setWFABefore(String before) {
		_WFABefore = before;
	}

	/**
	 * 检查错误提示
	 * 
	 * @return the _WFABeforeMsg
	 */
	public String getWFABeforeMsg() {
		return _WFABeforeMsg;
	}

	/**
	 * 检查错误提示
	 * 
	 * @param beforeMsg
	 *            the _WFABeforeMsg to set
	 */
	public void setWFABeforeMsg(String beforeMsg) {
		_WFABeforeMsg = beforeMsg;
	}

	/**
	 * 执行的Action
	 * 
	 * @return the _WFAAfter
	 */
	public String getWFAAfter() {
		return _WFAAfter;
	}

	/**
	 * 执行的Action
	 * 
	 * @param after
	 *            the _WFAAfter to set
	 */
	public void setWFAAfter(String after) {
		_WFAAfter = after;
	}

	/**
	 * 审核通过的节点名称
	 * 
	 * @return the _WFANextYes
	 */
	public String getWFANextYes() {
		return _WFANextYes;
	}

	/**
	 * 审核通过的节点名称
	 * 
	 * @param nextYes
	 *            the _WFANextYes to set
	 */
	public void setWFANextYes(String nextYes) {
		_WFANextYes = nextYes;
	}

	/**
	 * 审核失败的节点名称
	 * 
	 * @return the _WFANextNo
	 */
	public String getWFANextNo() {
		return _WFANextNo;
	}

	/**
	 * 审核失败的节点名称
	 * 
	 * @param nextNo
	 *            the _WFANextNo to set
	 */
	public void setWFANextNo(String nextNo) {
		_WFANextNo = nextNo;
	}

	/**
	 * 是否最后一步
	 * 
	 * @return the _IsLast
	 */
	public boolean isLast() {
		return _IsLast;
	}

	/**
	 * 是否最后一步
	 * 
	 * @param isLast
	 *            the _IsLast to set
	 */
	public void setLast(boolean isLast) {
		_IsLast = isLast;
	}

	/**节点类型
	 * @return the _WfType
	 */
	public String getWfType() {
		return _WfType;
	}

	/**
	 * @param wfType the _WfType to set
	 */
	public void setWfType(String wfType) {
		_WfType = wfType;
	}

	/**
	 * 是否是第一个节点
	 * @return the _IsFirst
	 */
	public boolean isFirst() {
		return _IsFirst;
	}

	/**
	 * @param isFirst the _IsFirst to set
	 */
	public void setFirst(boolean isFirst) {
		_IsFirst = isFirst;
	}

	/**
	 * 测试的逻辑表达式
	 * @return the _WfLogic
	 */
	public String getWfLogic() {
		return _WfLogic;
	}

	/**
	 * @param wfLogic the _WfLogic to set
	 */
	public void setWfLogic(String wfLogic) {
		_WfLogic = wfLogic;
	}

	/**
	 * @return the _WFAParent
	 */
	public String getWFAParent() {
		return _WFAParent;
	}

	/**父节点
	 * @param parent the _WFAParent to set
	 */
	public void setWFAParent(String parent) {
		_WFAParent = parent;
	}

}
