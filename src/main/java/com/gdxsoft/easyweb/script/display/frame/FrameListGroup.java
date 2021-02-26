package com.gdxsoft.easyweb.script.display.frame;

import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;

/**
 * 用于ListFrame的分组显示的类
 * 
 * @author Administrator
 * 
 */
public class FrameListGroup {

	private String _Description;
	private int _ColSpan;
	private String _GroupType = "";
	private int _TestLength;
	private String _CurValue = null;
	private UserXItem _GroupUserXItem;
	private String _FrameGUID;

	public FrameListGroup() {

	}

	public String makeHtml(String val) {
		val = this.getGroupValue(val);
		String s1 = "";
		if (this._CurValue == null || !val.equalsIgnoreCase(this._CurValue)) {
			s1 = "<tr ewa_tag='group' ewa_group=''><td class='EWA_LF_GROUP' colspan='"
					+ _ColSpan
					+ "'><div style='cursor:pointer;' onclick='EWA.F.FOS[\""
					+ _FrameGUID
					+ "\"].GroupShowHidden(this);'>"
					+ _Description
					+ "<span>"
					+ val
					+ "</span><span></span></div></td></tr>\r\n";
			_CurValue = val;
		}
		return s1;
	}

	public void init(UserXItem uxi) {
		this._GroupUserXItem = uxi;

		UserXItemValues di;
		try {
			di = this._GroupUserXItem.getItem("DataItem");
			String dt = di.getItem(0).getItem("DataType");
			if (dt.equalsIgnoreCase("date")) {
				this._GroupType = "date"; // 日期
			}
			di = this._GroupUserXItem.getItem("OrderSearch");
			String groupTestLength = di.getItem(0).getItem("GroupTestLength");
			try {
				int m = Integer.parseInt(groupTestLength);
				this._TestLength = m <= 0 ? -1 : m;
			} catch (Exception e) {
				this._TestLength = -1;
			}
		} catch (Exception e) {
		}
	}

	public String getGroupValue(String v) {
		if (v == null) {
			return "";
		}
		if (this._GroupType.equalsIgnoreCase("date")) {
			return v.split(" ")[0];
		}
		if (this._TestLength > 0) {
			if (v.length() <= this._TestLength) {
				return v;
			} else {
				return v.substring(0, this._TestLength);
			}
		}
		return v;
	}

	/**
	 * @return the _Description
	 */
	public String getDescription() {
		return _Description;
	}

	/**
	 * @param description
	 *            the _Description to set
	 */
	public void setDescription(String description) {
		_Description = description;
	}

	/**
	 * @return the _ColSpan
	 */
	public int getColSpan() {
		return _ColSpan;
	}

	/**
	 * @param colSpan
	 *            the _ColSpan to set
	 */
	public void setColSpan(int colSpan) {
		_ColSpan = colSpan;
	}

	/**
	 * @return the _GroupType
	 */
	public String getGroupType() {
		return _GroupType;
	}

	/**
	 * @param groupType
	 *            the _GroupType to set
	 */
	public void setGroupType(String groupType) {
		_GroupType = groupType;
	}

	/**
	 * @return the _TestLength
	 */
	public int getTestLength() {
		return _TestLength;
	}

	/**
	 * @param testLength
	 *            the _TestLength to set
	 */
	public void setTestLength(int testLength) {
		_TestLength = testLength;
	}

	/**
	 * @return the _CurValue
	 */
	public String getCurValue() {
		return _CurValue;
	}

	/**
	 * @param curValue
	 *            the _CurValue to set
	 */
	public void setCurValue(String curValue) {
		_CurValue = curValue;
	}

	/**
	 * @return the _GroupUserXItem
	 */
	public UserXItem getGroupUserXItem() {
		return _GroupUserXItem;
	}

	/**
	 * @return the frameGUID
	 */
	public String getFrameGUID() {
		return _FrameGUID;
	}

	/**
	 * @param frameGUID
	 *            the frameGUID to set
	 */
	public void setFrameGUID(String frameGUID) {
		_FrameGUID = frameGUID;
	}
}
