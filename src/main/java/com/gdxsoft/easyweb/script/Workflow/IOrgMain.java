package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTTable;

public interface IOrgMain {

	public abstract void initDept(DTTable deptTb, String deptId,
			String deptName, String deptParent) throws Exception;

	public abstract void initPost(DTTable postTb, String postId,
			String postName, String deptId, String isMaster) throws Exception;

	public abstract void initUser(DTTable userTb, String userId,
			String userName, String deptId, String postId) throws Exception;

	/**
	 * @return the _Depts
	 */
	public abstract HashMap<String, OrgDept> getDepts();

	/**
	 * @return the _Users
	 */
	public abstract HashMap<String, OrgUser> getUsers();

	/**
	 * @return the _Posts
	 */
	public abstract HashMap<String, OrgPost> getPosts();

	/**
	 * @return the _DeptRoot
	 */
	public abstract OrgDept getDeptRoot();

}