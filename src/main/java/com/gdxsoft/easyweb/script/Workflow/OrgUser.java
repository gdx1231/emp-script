package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;

public class OrgUser {
	private OrgDept _Dept;
	private String _UId;
	private String _UName;
	private HashMap<String ,OrgPost> _Posts;
	/**
	 * @return the _UId
	 */
	public String getUId() {
		return _UId;
	}
	/**
	 * @param id the _UId to set
	 */
	public void setUId(String id) {
		_UId = id;
	}
	/**
	 * @return the _UName
	 */
	public String getUName() {
		return _UName;
	}
	/**
	 * @param name the _UName to set
	 */
	public void setUName(String name) {
		_UName = name;
	}
	/**
	 * @return the _Dept
	 */
	public OrgDept getDept() {
		return _Dept;
	}
	/**
	 * @param dept the _Dept to set
	 */
	public void setDept(OrgDept dept) {
		_Dept = dept;
	}
	/**
	 * @return the _Posts
	 */
	public HashMap<String, OrgPost> getPosts() {
		return _Posts;
	}
	/**
	 * @param posts the _Posts to set
	 */
	public void setPosts(HashMap<String, OrgPost> posts) {
		_Posts = posts;
	}
	
}
