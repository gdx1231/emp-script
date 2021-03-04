package com.gdxsoft.easyweb.test;

import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.Skin;

public class TestEwaConfig {

	public static void main(String[] args) {
		TestEwaConfig t = new TestEwaConfig();
		try {
			t.test();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void test() throws Exception {
		EwaConfig a = EwaConfig.instance();
		a.getConfigFrames();

		Skin skin = Skin.instance();
		skin.getBodyStart();

		EwaGlobals g = EwaGlobals.instance();
		System.out.println(g.createJs("enus").substring(0,100));
	}
}
