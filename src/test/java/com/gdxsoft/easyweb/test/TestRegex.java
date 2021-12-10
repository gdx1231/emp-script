package com.gdxsoft.easyweb.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.gdxsoft.easyweb.utils.UFile;

public class TestRegex {

	public static void main(String[] args) {
		TestRegex t = new TestRegex();
		try {
			t.testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testFileConvert() throws Exception {
		 String s="abc_.def";
		 System.out.println(s.matches("[a-zA-Z0-9_.]*"));
		 System.out.println(s.matches("[a-zA-Z0-9_\u4e00-\u9fa5]*"));
	}
}
