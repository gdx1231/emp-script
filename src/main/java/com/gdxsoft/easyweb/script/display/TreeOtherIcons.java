package com.gdxsoft.easyweb.script.display;

import java.util.ArrayList;
import java.util.regex.Matcher;

import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;

public class TreeOtherIcons {

	private ArrayList<TreeOtherIcon> _Icons;
	public TreeOtherIcons() {

	}

	public void init(UserConfig userConfig) throws Exception {
		UserXItem ux = userConfig.getUserPageItem();
		if (!ux.testName("TreeIconSet")) {
			return;
		}
		_Icons = new java.util.ArrayList<TreeOtherIcon>();
		UserXItemValues uvs = ux.getItem("TreeIconSet");

		for (int i = 0; i < uvs.count(); i++) {
			UserXItemValue v = uvs.getItem(i);
			TreeOtherIcon o = new TreeOtherIcon();
			o.setOpen(v.getItem("Open"));
			o.setClose(v.getItem("Close"));
			o.setFilter(v.getItem("Filter"));
			o.setTest(v.getItem("Test").trim().toUpperCase());
			_Icons.add(o);
		}
	}

	public String test(TreeViewNode node) {
		if (this._Icons.size() == 0)
			return "";

		for (int i = 0; i < this._Icons.size(); i++) {
			TreeOtherIcon o = this._Icons.get(i);
			String val = "";
			if (o.getTest().equals("KEY")) {
				val = node.getKey();
			} else if (o.getTest().equals("PARENTKEY")) {
				val = node.getParentKey();
			} else if (o.getTest().equals("TEXT")) {
				val = node.getDispVal();
			} else if (o.getTest().equals("P0")) {
				val = node.getAddParas().get(0);
			} else if (o.getTest().equals("P1")) {
				val = node.getAddParas().get(1);
			} else if (o.getTest().equals("P2")) {
				val = node.getAddParas().get(2);
			}
			if (val.length() > 0) {
				Matcher mat = o.getPattern().matcher(val);
				if (mat.find()) {
					return "" + i;
				}
			}
		}

		return "";
	}

	/**
	 * @return the _Icons
	 */
	public ArrayList<TreeOtherIcon> getIcons() {
		return _Icons;
	}
}
