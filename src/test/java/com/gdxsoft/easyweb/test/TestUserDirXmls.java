package com.gdxsoft.easyweb.test;

import java.io.IOException;
import java.util.ArrayList;

import com.gdxsoft.easyweb.define.Dir;
import com.gdxsoft.easyweb.define.Dirs;

public class TestUserDirXmls {

	public static void main(String[] args) throws IOException {
		TestUserDirXmls t = new TestUserDirXmls();
		t.testFileConvert();
	}

	public void testFileConvert() throws IOException {
		Dirs dirs = new Dirs("/Users/admin/java/work_xml/user.config_robert.xml", true);
		//Dirs dirs = new Dirs("/home/admin/java/robert/erp_aus/documents/user.config.xml", true);
		
		String[] filter = { "xml" };
		dirs.setFiletes(filter);
		dirs.init();
		ArrayList<Dir> ds = dirs.getDirs();
		for (int i = 0; i < ds.size(); i++) {
			Dir d = ds.get(i);
			System.out.println(d.getPath());
		}
	}
}
