/**
 * 
 */
package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;

/**
 * @author Administrator
 * 
 */
public class OrgPost {

	private String _PostId;
	private String _PostName;
	private boolean _IsMaster;
	private OrgDept _Dept;
	
	private HashMap<String,OrgUser> _Users;

	/**
	 * @return the _PostId
	 */
	public String getPostId() {
		return _PostId;
	}

	/**
	 * @param postId
	 *            the _PostId to set
	 */
	public void setPostId(String postId) {
		_PostId = postId;
	}

	/**
	 * @return the _PostName
	 */
	public String getPostName() {
		return _PostName;
	}

	/**
	 * @param postName
	 *            the _PostName to set
	 */
	public void setPostName(String postName) {
		_PostName = postName;
	}

	/**
	 * @return the _IsMaster
	 */
	public boolean isMaster() {
		return _IsMaster;
	}

	/**
	 * @param isMaster
	 *            the _IsMaster to set
	 */
	public void setIsMaster(boolean isMaster) {
		_IsMaster = isMaster;
	}

	/**
	 * @return the _Dept
	 */
	public OrgDept getDept() {
		return _Dept;
	}

	/**
	 * @param dept
	 *            the _Dept to set
	 */
	public void setDept(OrgDept dept) {
		_Dept = dept;
	}

	/**
	 * @return the _Users
	 */
	public HashMap<String, OrgUser> getUsers() {
		return _Users;
	}

	/**
	 * @param users the _Users to set
	 */
	public void setUsers(HashMap<String, OrgUser> users) {
		_Users = users;
	}
}
