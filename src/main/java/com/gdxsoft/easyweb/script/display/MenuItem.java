package com.gdxsoft.easyweb.script.display;

import com.gdxsoft.easyweb.data.DTRow;

public class MenuItem {
	private String _Key;
	private String _ParentKey;
	private String _Text;
	private String _JavascriptCmd;
	private String _Icon;
	private String _IconType;
	private DTRow _DTRow;
	private boolean _IsHaveChildren;
	/**
	 * @return the _Text
	 */
	public String getText() {
		return _Text;
	}

	/**
	 * @param text
	 *            the _Text to set
	 */
	public void setText(String text) {
		_Text = text;
	}

	/**
	 * @return the _JavascritCmd
	 */
	public String getJavascriptCmd() {
		return _JavascriptCmd;
	}

	/**
	 * @param javascritCmd
	 *            the _JavascritCmd to set
	 */
	public void setJavascriptCmd(String javascriptCmd) {
		_JavascriptCmd = javascriptCmd;
	}

	/**
	 * @return the _Icon
	 */
	public String getIcon() {
		return _Icon;
	}

	/**
	 * @param icon
	 *            the _Icon to set
	 */
	public void setIcon(String icon) {
		_Icon = icon;
	}

	/**
	 * @return the _Key
	 */
	public String getKey() {
		return _Key;
	}

	/**
	 * @param key
	 *            the _Key to set
	 */
	public void setKey(String key) {
		_Key = key;
	}

	/**
	 * @return the _ParentKey
	 */
	public String getParentKey() {
		return _ParentKey;
	}

	/**
	 * @param parentKey
	 *            the _ParentKey to set
	 */
	public void setParentKey(String parentKey) {
		_ParentKey = parentKey;
	}

	/**
	 * @return the _IconType
	 */
	public String getIconType() {
		return _IconType;
	}

	/**
	 * @param iconType the _IconType to set
	 */
	public void setIconType(String iconType) {
		_IconType = iconType;
	}

	/**
	 * @return the _DTRow
	 */
	public DTRow getDTRow() {
		return _DTRow;
	}

	/**
	 * @param row the _DTRow to set
	 */
	public void setDTRow(DTRow row) {
		_DTRow = row;
	}

	/**
	 * 是否有子节点
	 * @return
	 */
	public boolean isHaveChildren() {
		return _IsHaveChildren;
	}

	public void setIsHaveChildren(boolean _IsHaveChildren) {
		this._IsHaveChildren = _IsHaveChildren;
	}
}
