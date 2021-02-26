package com.gdxsoft.easyweb.script.userConfig;

import java.io.Serializable;

import com.gdxsoft.easyweb.script.template.SetBase;
import com.gdxsoft.easyweb.script.template.XItemParameter;

public class UserXItemValues extends SetBase<UserXItemValue> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4383610277800206967L;
	private XItemParameter _Parameter;


	/**
	 * XItem 名
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return this._Parameter.getName();
	}

	/**
	 * 获取用户配置信息中对应的 XItemParameter定义(EwaConfig.xml)
	 * 
	 * @return the _Parameter
	 */
	public XItemParameter getParameter() {
		return _Parameter;
	}

	/**
	 * 设置用户配置信息中对应的 XItemParameter定义(EwaConfig.xml)
	 * 
	 * @param parameter
	 *            the _Parameter to set
	 */
	public void setParameter(XItemParameter parameter) {
		_Parameter = parameter;
	}
}
