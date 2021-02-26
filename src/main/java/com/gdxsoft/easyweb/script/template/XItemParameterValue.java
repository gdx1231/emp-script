package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class XItemParameterValue implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8744800364063206026L;
	/*
	 * <Values> <Value Type="string" Name="Name"> <Description Lang="zh-cn"
	 * Value="名称" /> </Value> </Values>
	 */
	private String _Name; // 名称
	private String _Type; // 类型
	private String _Ref; // 参照
	private Descriptions _Descriptions;// 描述信息
	private boolean _IsCDATA = false;// 是否使用CDATA标签
	private boolean _IsUnique = false;
	private boolean _IsNotJsShow = false;// 是否在页面显示JS

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Type
	 */
	public String getType() {
		return _Type;
	}

	/**
	 * @param type
	 *            the _Type to set
	 */
	public void setType(String type) {
		_Type = type;
	}

	/**
	 * @return the _Ref
	 */
	public String getRef() {
		return _Ref;
	}

	/**
	 * @param ref
	 *            the _Ref to set
	 */
	public void setRef(String ref) {
		_Ref = ref;
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
	 * @return the _IsCDATA
	 */
	public boolean isCDATA() {
		return _IsCDATA;
	}

	/**
	 * @param isCDATA
	 *            the _IsCDATA to set
	 */
	public void setIsCDATA(boolean isCDATA) {
		_IsCDATA = isCDATA;
	}

	/**
	 * @return the _IsUnique
	 */
	public boolean isUnique() {
		return _IsUnique;
	}

	/**
	 * @param isUnique
	 *            the _IsUnique to set
	 */
	public void setIsUnique(boolean isUnique) {
		_IsUnique = isUnique;
	}

 

	/**
	 * @return the _IsNotJsShow
	 */
	public boolean isNotJsShow() {
		return _IsNotJsShow;
	}

	/**
	 * @param isNotJsShow the _IsNotJsShow to set
	 */
	public void setIsNotJsShow(boolean isNotJsShow) {
		_IsNotJsShow = isNotJsShow;
	}

}
