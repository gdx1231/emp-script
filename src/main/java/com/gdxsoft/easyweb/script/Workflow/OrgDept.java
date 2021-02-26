package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;

public class OrgDept {
	private String _DeptId;
	private String _DeptName;
	private OrgDept _UpDept;
	private HashMap<String, OrgUser> _Users;
	private HashMap<String, OrgDept> _LowDepts;
	

	/**
	 * @return the _DeptId
	 */
	public String getDeptId() {
		return _DeptId;
	}

	/**
	 * @param deptId
	 *            the _DeptId to set
	 */
	public void setDeptId(String deptId) {
		_DeptId = deptId;
	}

	/**
	 * @return the _DeptName
	 */
	public String getDeptName() {
		return _DeptName;
	}

	/**
	 * @param deptName
	 *            the _DeptName to set
	 */
	public void setDeptName(String deptName) {
		_DeptName = deptName;
	}

	/**
	 * @return the _UpDept
	 */
	public OrgDept getUpDept() {
		return _UpDept;
	}

	/**
	 * @param upDept
	 *            the _UpDept to set
	 */
	public void setUpDept(OrgDept upDept) {
		_UpDept = upDept;
	}

	/**
	 * @return the _LowDepts
	 */
	public HashMap<String, OrgDept> getLowDepts() {
		return _LowDepts;
	}

	/**
	 * @param lowDepts
	 *            the _LowDepts to set
	 */
	public void setLowDepts(HashMap<String, OrgDept> lowDepts) {
		_LowDepts = lowDepts;
	}

	/**
	 * @return the _Users
	 */
	public HashMap<String, OrgUser> getUsers() {
		return _Users;
	}

	/**
	 * @param users
	 *            the _Users to set
	 */
	public void setUsers(HashMap<String, OrgUser> users) {
		_Users = users;
	}
}
