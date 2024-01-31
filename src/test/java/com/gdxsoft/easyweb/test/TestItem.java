package com.gdxsoft.easyweb.test;

import com.gdxsoft.easyweb.script.display.items.*;

public class TestItem {

	public static void main(String[] args) {
		TestItem t = new TestItem();
		try {
			t.testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testFileConvert() throws Exception {
		
		IItem item = new ItemEwaConfigItem();
		System.out.println(item.getClass().getSimpleName());
		ItemEwaConfigItem aa = (ItemEwaConfigItem)item;
		System.out.println(aa);
	}
}
