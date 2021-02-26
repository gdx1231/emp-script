package com.gdxsoft.easyweb.datasource;

import java.util.HashMap;

import com.gdxsoft.easyweb.data.DTRow;

public class UpdateChangeRow {
	private HashMap<String, UpdateChange> changes_;
	private DTRow rBefore_;
	private DTRow rAfter_;

	public DTRow getBefore() {
		return rBefore_;
	}

	public void setBefore(DTRow rBefore) {
		this.rBefore_ = rBefore;
	}

	public DTRow getAfter() {
		return rAfter_;
	}

	public void setAfter(DTRow rAfter) {
		this.rAfter_ = rAfter;
	}

	public HashMap<String, UpdateChange> getChanges() {
		return changes_;
	}

	public void setChanges(HashMap<String, UpdateChange> changes) {
		this.changes_ = changes;
	}

}
