package com.gdxsoft.easyweb.h5Storage;

import java.util.HashMap;

public class Tag {
	private String name_;
	private HashMap<String, Group> groups_;

	public Tag() {
		this.groups_ = new HashMap<String, Group>();
	}

	public Group getGroup(String name) {
		if (this.groups_.containsKey(name)) {
			return this.groups_.get(name);
		} else {
			return null;
		}
	}

	public void addGroup(Group g) {
		g.setTag(this);
		this.groups_.put(g.getName(), g);
	}

	public String getName() {
		return name_;
	}

	public void setName(String name) {
		this.name_ = name;
	}

	public HashMap<String, Group> getGroups() {
		return groups_;
	}

	public void setGroups(HashMap<String, Group> groups) {
		this.groups_ = groups;
	}
}
