package com.gdxsoft.easyweb.define;


import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.data.DTTable;

public class EwaDefineMenu {
	private static final String CFG_NAME = "EwaDefineConfig.xml";

	public EwaDefineMenu() {

	}

	public DTTable getMenus() throws Exception {
		String xml = SystemXmlUtils.getSystemConfContent(CFG_NAME);
		DTTable tb = new DTTable();
		tb.initData(xml, "EwaDefine/Menus/Menu");

		return tb;
	}
}
