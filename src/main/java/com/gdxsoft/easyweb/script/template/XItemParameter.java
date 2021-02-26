package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;
import java.util.HashMap;

public class XItemParameter implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7742065197159715009L;
	private String _Name;// 参数名称
	private boolean _IsSet = false;// 是否集合
	private boolean _IsShow = true;// 是否显示
	private boolean _IsMulti = false; // 是否多值
	private boolean _IsNotJsShow = false;// 是否在页面显示JS
	private Descriptions _Descriptions;// 描述信息
	private XItemParameterValues _Values; // 参数值
	private String _Html; // 用于页面元素的替换模式 Html="name=@Name id=@Name"
	private String _ChildrenParameters;// 子参数
	private HashMap<String, XItemParameter> _Children; // 子参数
	private boolean _IsJsShow = true;

	/**
	 * 获取标识名称
	 * 
	 * @return
	 */
	public String getUniqueName() {
		if (!this._IsSet) {
			return null;
		}
		try {
			for (int i = 0; i < this._Values.count(); i++) {
				if (this._Values.getItem(i).isUnique()) {
					return this._Values.getItem(i).getName();
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
	 * 参数名称
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 参数名称
	 * 
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _IsSet
	 */
	public boolean isSet() {
		return _IsSet;
	}

	/**
	 * @param isSet
	 *            the _IsSet to set
	 */
	public void setIsSet(boolean isSet) {
		_IsSet = isSet;
	}

	/**
	 * @return the _IsShow
	 */
	public boolean isShow() {
		return _IsShow;
	}

	/**
	 * @param isShow
	 *            the _IsShow to set
	 */
	public void setIsShow(boolean isShow) {
		_IsShow = isShow;
	}

	/**
	 * @return the _Descriptions
	 */
	public Descriptions getDescriptions() {
		return _Descriptions;
	}

	/**
	 * @param descriptions
	 *            the _Descriptions to set
	 */
	public void setDescriptions(Descriptions descriptions) {
		_Descriptions = descriptions;
	}

	/**
	 * @return the _Values
	 */
	public XItemParameterValues getValues() {
		return _Values;
	}

	/**
	 * @param values
	 *            the _Values to set
	 */
	public void setValues(XItemParameterValues values) {
		_Values = values;
	}

	/**
	 * 用于页面元素的替换模式 Html="name=@Name id=@Name"
	 * 
	 * @return the _Html
	 */
	public String getHtml() {
		return _Html;
	}

	/**
	 * 用于页面元素的替换模式 Html="name=@Name id=@Name"
	 * 
	 * @param html
	 *            the _Html to set
	 */
	public void setHtml(String html) {
		_Html = html;
	}

	/**
	 * 是否多值
	 * 
	 * @return the _IsMulti
	 */
	public boolean isMulti() {
		return _IsMulti;
	}

	/**
	 * 是否多值
	 * 
	 * @param isMulti
	 *            the _IsMulti to set
	 */
	public void setIsMulti(boolean isMulti) {
		_IsMulti = isMulti;
	}

	/**
	 * @return the _ChildrenParameters
	 */
	public String getChildrenParameters() {
		return _ChildrenParameters;
	}

	/**
	 * @param childrenParameters
	 *            the _ChildrenParameters to set
	 */
	public void setChildrenParameters(String childrenParameters) {
		_ChildrenParameters = childrenParameters;
	}

	/**
	 * @return the _Children
	 */
	public HashMap<String, XItemParameter> getChildren() {
		if (this._Children == null) {
			this._Children = new HashMap<String, XItemParameter>();
		}
		return _Children;
	}

	/**
	 * @param children
	 *            the _Children to set
	 */
	public void setChildren(HashMap<String, XItemParameter> children) {
		_Children = children;
	}

	/**
	 * @return the _IsJsShow
	 */
	public boolean isNotJsShow() {
		return _IsNotJsShow;
	}

	/**
	 * @param isJspShow
	 *            the _IsJsShow to set
	 */
	public void setIsNotJsShow(boolean isNotJsShow) {
		_IsNotJsShow = isNotJsShow;
	}

	/**
	 * @return the _IsJsShow
	 */
	public boolean isJsShow() {
		return _IsJsShow;
	}

	/**
	 * @param isJsShow the _IsJsShow to set
	 */
	public void setIsJsShow(boolean isJsShow) {
		_IsJsShow = isJsShow;
	}

}
