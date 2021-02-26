package com.gdxsoft.easyweb.data;

import java.io.Serializable;

public class DTColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1435342381212006657L;
	private String _Name; // 字段名称
	private String _Description; // 描述
	private String _TypeName; // 类型
	private int _Length; // 长度
	private int _Precision; // 精度
	private int _Scale; // 小数位
	private int _Index; // 排序
	private boolean _IsXmlAttribute; // 如果是xml数据，是否是从属性回复数据
	private boolean _IsXmlCData; // 是否是从CDATA回复数据
	private boolean _IsKey; // 是否主键
	private boolean _IsIdentity; // 是否自增长

	private boolean _IsJson; // 是否为JSON字段
	private boolean _IsHidden; // 是否隐含字段，对于 ExportExcel有效

	private int _Order = -1; // 设置排序 ，对于 ExportExcel有效

	/**
	 * 字段名称
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 字段名称
	 * 
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * 字段描述
	 * 
	 * @return the _Description
	 */
	public String getDescription() {
		return _Description;
	}

	/**
	 * 字段描述
	 * 
	 * @param description the _Description to set
	 */
	public void setDescription(String description) {
		_Description = description;
	}

	/**
	 * 类型
	 * 
	 * @return the _TypeName
	 */
	public String getTypeName() {
		return _TypeName;
	}

	/**
	 * 类型
	 * 
	 * @param typeName the _TypeName to set
	 */
	public void setTypeName(String typeName) {
		_TypeName = typeName;
	}

	/**
	 * 长度
	 * 
	 * @return the _Length
	 */
	public int getLength() {
		return _Length;
	}

	/**
	 * 长度
	 * 
	 * @param length the _Length to set
	 */
	public void setLength(int length) {
		_Length = length;
	}

	/**
	 * 精度
	 * 
	 * @return the _Precision
	 */
	public int getPrecision() {
		return _Precision;
	}

	/**
	 * 精度
	 * 
	 * @param precision the _Precision to set
	 */
	public void setPrecision(int precision) {
		_Precision = precision;
	}

	/**
	 * 小数位
	 * 
	 * @return the _Scale
	 */
	public int getScale() {
		return _Scale;
	}

	/**
	 * 小数位
	 * 
	 * @param scale the _Scale to set
	 */
	public void setScale(int scale) {
		_Scale = scale;
	}

	/**
	 * 排序
	 * 
	 * @return the _Index
	 */
	public int getIndex() {
		return _Index;
	}

	/**
	 * 排序
	 * 
	 * @param index the _Index to set
	 */
	protected void setIndex(int index) {
		_Index = index;
	}

	/**
	 * @return the _IsXmlAttribute
	 */
	public boolean isXmlAttribute() {
		return _IsXmlAttribute;
	}

	/**
	 * @param isXmlAttribute the _IsXmlAttribute to set
	 */
	public void setIsXmlAttribute(boolean isXmlAttribute) {
		_IsXmlAttribute = isXmlAttribute;
	}

	/**
	 * @return the _IsXmlCData
	 */
	public boolean isXmlCData() {
		return _IsXmlCData;
	}

	/**
	 * 主键
	 * 
	 * @param isXmlCData the _IsXmlCData to set
	 */
	public void setIsXmlCData(boolean isXmlCData) {
		_IsXmlCData = isXmlCData;
	}

	/**
	 * 主键
	 * 
	 * @return the _IsKey
	 */
	public boolean isKey() {
		return _IsKey;
	}

	/**
	 * @param isKey the _IsKey to set
	 */
	public void setIsKey(boolean isKey) {
		_IsKey = isKey;
	}

	/**
	 * 是否自增长字段
	 * 
	 * @return the _IsIdentity
	 */
	public boolean isIdentity() {
		return _IsIdentity;
	}

	/**
	 * 是否自增长字段
	 * 
	 * @param isIdentity the _IsIdentity to set
	 */
	public void setIsIdentity(boolean isIdentity) {
		_IsIdentity = isIdentity;
	}

	/**
	 * 是否为json字段
	 * 
	 * @return the _IsJson
	 */
	public boolean isJson() {
		return _IsJson;
	}

	/**
	 * 设置是否为json字段
	 * 
	 * @param _IsJson the _IsJson to set
	 */
	public void setIsJson(boolean _IsJson) {
		this._IsJson = _IsJson;
	}

	/**
	 * 是否隐含字段，对于 ExportExcel有效
	 * 
	 * @return the _IsHidden
	 */
	public boolean isHidden() {
		return _IsHidden;
	}

	/**
	 * 是否隐含字段，对于 ExportExcel有效
	 * 
	 * @param _IsHidden the _IsHidden to set
	 */
	public void setHidden(boolean isHidden) {
		this._IsHidden = isHidden;
	}

	/**
	 * 设置排序 ，对于 ExportExcel有效，默认是 -1
	 * 
	 * @return the _Order
	 */
	public int getOrder() {
		return _Order;
	}

	/**
	 * 设置排序 ，对于 ExportExcel有效
	 * 
	 * @param _Order the _Order to set
	 */
	public void setOrder(int order) {
		this._Order = order;
	}

}
