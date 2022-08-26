package com.gdxsoft.easyweb.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.gdxsoft.easyweb.utils.UFile;

public class TestBootJar {

	public static void main(String[] args) {
		TestBootJar t = new TestBootJar();
		try {
			t.testFileConvert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testFileConvert() throws Exception {
		String root = "file:/Users/admin/com.gdxsoft/emp-script-help/target/emp-script-help-1.0.1.jar!/BOOT-INF/lib/emp-script-1.1.1.jar!/define.xml";
		File f = new File(root);
		if (!f.exists()) {
			System.out.println("The file not exists: " + f.getAbsolutePath());
		}
		String[] paths = root.split("\\!");
		for (int i = 0; i < paths.length; i++) {
			System.out.println(paths[i]);
		}
		String bootJar = paths[0];
		String jar = paths[1].substring(1);
		URL url1 = new URL(bootJar);
		File f1 = new File(url1.toURI());

		List<String> lst = UFile.getZipList(f1.getAbsolutePath());
		lst.forEach(fileName -> {
			if (fileName.equals(jar)) {
				System.out.println(fileName);

			}
		});
		byte[] buf = UFile.readZipBytes(f1.getAbsolutePath(), jar);
		System.out.println(buf.length);

		File temp = File.createTempFile("testrunoobtmp", ".txt");

		System.out.println(temp.getAbsolutePath());

		UFile.createBinaryFile(temp.getAbsolutePath(), buf, true);
		List<String> lst1 = UFile.getZipList(temp.getAbsolutePath());
		lst1.forEach(fileName -> {
			System.out.println(fileName);

		});
	}
}
