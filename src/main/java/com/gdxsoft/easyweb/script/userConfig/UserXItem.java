package com.gdxsoft.easyweb.script.userConfig;

import java.io.Serializable;

import com.gdxsoft.easyweb.script.template.SetBase;

public class UserXItem extends SetBase<UserXItemValues>  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3413162864856770560L;
	/**
	 * 
	 */
	private String _Name;
	private String _Html;
	private boolean _IsUsingEwaEvent = false;
	
	private Boolean usingRef;
	
	/**
	 * 本体的样式
	 * @return the _Style
	 */
	public String getStyle() {
		return _Style;
	}

	/**
	 * @param style the _Style to set
	 */
	public void setStyle(String style) {
		_Style = style;
	}

	/**
	 * 父体的样式
	 * @return the _ParentStyle
	 */
	public String getParentStyle() {
		return _ParentStyle;
	}

	/**
	 * @param parentStyle the _ParentStyle to set
	 */
	public void setParentStyle(String parentStyle) {
		_ParentStyle = parentStyle;
	}

	private String _Style;
	private String _ParentStyle;
	

	/**
	 * 获取用户定义条目的指定标记的值
	 * 
	 * @param itemTagName
	 *            要判断的Tag名称
	 * @param itemTagValue
	 *            要判断的值 ,例如 Lang=zhcn
	 * @param valueTag
	 *            取回值Tag的名称
	 * @return 值
	 */
	public String getItemValue(String itemTagName, String itemTagValue,
			String valueTag) {
		if (!super.testName(itemTagName)) {
			return itemTagName + "没有啊！";
		}

		try {
			String[] a = itemTagValue.split("=");
			if (a.length != 2) {
				return itemTagValue + "表达式不正确！";
			}

			UserXItemValues uvs = super.getItem(itemTagName);
			for (int i = 0; i < uvs.count(); i++) {
				UserXItemValue uv = uvs.getItem(i);
				String tagVal = uv.getItem(a[0]);
				if (tagVal.equalsIgnoreCase(a[1])) {
					return uv.getItem(valueTag);
				}
			}
			return itemTagName + "=" + itemTagValue + "没找到！";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getSingleValue(String itemTagName,String valueTag) {
		if (!super.testName(itemTagName)) {
			return itemTagName + "没有啊！";
		}
		try {
			UserXItemValues uvs = super.getItem(itemTagName);
			return uvs.getItem(0).getItem(valueTag);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	/**
	 * 返回指定Tag名称的单值
	 * 
	 * @param itemTagName
	 *            Tag名称
	 * @return
	 */
	public String getSingleValue(String itemTagName) {
		if (!super.testName(itemTagName)) {
			return itemTagName + "没有啊！";
		}
		try {
			UserXItemValues uvs = super.getItem(itemTagName);
			return uvs.getItem(0).getItem(0);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * XItem name 参数
	 * 
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * XItem name 参数
	 * 
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Html
	 */
	public String getHtml() {
		return _Html;
	}

	/**
	 * @param html
	 *            the _Html to set
	 */
	public void setHtml(String html) {
		_Html = html;
	}

	/**
	 * @return the _IsUsingEwaEvent
	 */
	public boolean isUsingEwaEvent() {
		return _IsUsingEwaEvent;
	}

	/**
	 * @param isUsingEwaEvent
	 *            the _IsUsingEwaEvent to set
	 */
	public void setIsUsingEwaEvent(boolean isUsingEwaEvent) {
		_IsUsingEwaEvent = isUsingEwaEvent;
	}

	/**
	 * 是否用dataRef
	 * @return the usingRef
	 */
	public Boolean getUsingRef() {
		return usingRef;
	}

	/**
	 * 是否用dataRef
	 * @param usingRef the usingRef to set
	 */
	public void setUsingRef(Boolean usingRef) {
		this.usingRef = usingRef;
	}
}
