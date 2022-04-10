package com.gdxsoft.easyweb.h5Storage;

import org.json.JSONObject;

public class Resource {

	private String id_;
	private String hash_;
	private String content_;
	private Group group_;
	private JSONObject source_;

	private String allPath_;
	private String path_;

	private boolean jarResource;
	private String classLoader;
	
	private int index_;

	public String getId() {
		return id_;
	}

	public void setId(String id) {
		this.id_ = id;
	}

	public String getHash() {
		return hash_;
	}

	public void setHash(String hash) {
		this.hash_ = hash;
	}

	public String getContent() {
		return content_;
	}

	public void setContent(String content) {
		this.content_ = content;
	}

	public Group getGroup() {
		return group_;
	}

	public void setGroup(Group group) {
		this.group_ = group;
	}

	public JSONObject getSource() {
		return source_;
	}

	public void setSource(JSONObject source) {
		this.source_ = source;
	}

	/**
	 * 完整的物理路径
	 * 
	 * @return
	 */
	public String getAllPath() {
		return allPath_;
	}

	/**
	 * 完整的物理路径
	 * 
	 * @param allPath
	 */
	public void setAllPath(String allPath) {
		this.allPath_ = allPath;
	}

	/**
	 * 相对路径
	 * 
	 * @return
	 */
	public String getPath() {
		return path_;
	}

	/**
	 * 相对路径
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path_ = path;
	}

	/**
	 * 资源的顺序号
	 * 
	 * @return
	 */
	public int getIndex() {
		return index_;
	}

	public void setIndex(int index) {
		this.index_ = index;
	}

	/**
	 * 是否是资源文件
	 * 
	 * @return
	 */
	public boolean isJarResource() {
		return jarResource;
	}

	public void setJarResource(boolean jarResource) {
		this.jarResource = jarResource;
	}

	/**
	 * 读取资源所用的class全名
	 * @return the classLoader
	 */
	public String getClassLoader() {
		return classLoader;
	}

	/**读取资源所用的class全名
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(String classLoader) {
		this.classLoader = classLoader;
	}

}
