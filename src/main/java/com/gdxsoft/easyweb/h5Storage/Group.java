package com.gdxsoft.easyweb.h5Storage;

import java.util.HashMap;

public class Group {
	private Tag tag_;
	private String name_;
	private String loadBy_;
	private HashMap<String, Resource> resources_;

	public Group() {
		resources_ = new HashMap<String, Resource>();
	}

	/**
	 * 获取资源
	 * 
	 * @param id
	 * @return
	 */
	public Resource getResource(String id) {
		if (this.resources_.containsKey(id)) {
			return this.resources_.get(id);
		} else {
			return null;
		}
	}

	/**
	 * 添加资源
	 * 
	 * @param r
	 */
	public void addResource(Resource r) {
		r.setGroup(this);
		this.resources_.put(r.getId(), r);
	}

	public String getName() {
		return name_;
	}

	public void setName(String name) {
		this.name_ = name;
	}

	public String getLoadBy() {
		return loadBy_;
	}

	public void setLoadBy(String loadBy) {
		this.loadBy_ = loadBy;
	}

	public HashMap<String, Resource> getResources() {
		return resources_;
	}

	public void setResources(HashMap<String, Resource> resources) {
		this.resources_ = resources;
	}

	public Tag getTag() {
		return tag_;
	}

	public void setTag(Tag tag) {
		this.tag_ = tag;
	}

}
