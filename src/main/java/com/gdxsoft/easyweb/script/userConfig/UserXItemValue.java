/**
 * 
 */
package com.gdxsoft.easyweb.script.userConfig;

import java.io.Serializable;
import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.SetBase;

/**
 * @author Administrator
 * 
 */
public class UserXItemValue extends SetBase<String> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8980112571426375500L;
	private HashMap<String,UserXItemValues> _Children;

	/**
	 * @return the _Children
	 */
	public HashMap<String, UserXItemValues> getChildren() {
		if(this._Children==null){
			this._Children=new HashMap<String, UserXItemValues>();
		}
		return _Children;
	}

	/**
	 * @param children the _Children to set
	 */
	public void setChildren(HashMap<String, UserXItemValues> children) {
		_Children = children;
	}

	private String _UniqueName;

	/**
	 * @return the _UniqueName
	 */
	public String getUniqueName() {
		return _UniqueName;
	}

	/**
	 * @param uniqueName the _UniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		_UniqueName = uniqueName;
	}
	
	private String _AddPara0;
	private String _AddPara1;
	private String _AddPara2;

	/**
	 * @return the _AddPara0
	 */
	public String getAddPara0() {
		return _AddPara0;
	}

	/**
	 * @param addPara0 the _AddPara0 to set
	 */
	public void setAddPara0(String addPara0) {
		_AddPara0 = addPara0;
	}

	/**
	 * @return the _AddPara1
	 */
	public String getAddPara1() {
		return _AddPara1;
	}

	/**
	 * @param addPara1 the _AddPara1 to set
	 */
	public void setAddPara1(String addPara1) {
		_AddPara1 = addPara1;
	}

	/**
	 * @return the _AddPara2
	 */
	public String getAddPara2() {
		return _AddPara2;
	}

	/**
	 * @param addPara2 the _AddPara2 to set
	 */
	public void setAddPara2(String addPara2) {
		_AddPara2 = addPara2;
	}
	
	
}
