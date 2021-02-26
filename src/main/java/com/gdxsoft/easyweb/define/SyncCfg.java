package com.gdxsoft.easyweb.define;

public class SyncCfg {

	private String _Id;
	public String getId() {
		return _Id;
	}
	public void setId(String id) {
		_Id = id;
	}
	public String getName() {
		return _Name;
	}
	public void setName(String name) {
		_Name = name;
	}
	public String getSource() {
		return _Source;
	}
	public void setSource(String source) {
		_Source = source;
	}
	public String getTarget() {
		return _Target;
	}
	public void setTarget(String target) {
		_Target = target;
	}
	public String getFilter() {
		return _Filter;
	}
	public void setFilter(String filter) {
		_Filter = filter;
	}
	private String _Name;
	private String _Source;
	private String _Target;
	private String _Filter;
	
}
