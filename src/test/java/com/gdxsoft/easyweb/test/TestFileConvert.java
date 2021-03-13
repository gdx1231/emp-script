package com.gdxsoft.easyweb.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.fileConvert.OpenOfficeInstance;

public class TestFileConvert {

	public static void main(String[] args) {
		TestFileConvert t = new TestFileConvert();
		t.testFileConvert();
	}

	public void testFileConvert() {
		String root = UPath.getRealPath() + "/docs";
		// String[] exts = { "doc", "docx", "txt", "ppt", "pptx", "xls", "xlsx", "wps"
		// };
		String[] exts = { "ppt", "pptx" };
		File[] docs = UFile.getFiles(root, exts);

		final Map<String, Object> filterData = new HashMap<>();

		/*
		 * filterData.put("PixelWidth", 2550); filterData.put("PixelHeight", 3300);
		 * filterData.put("logicalWidth", 2550); filterData.put("logicalHeight", 3300);
		 */
		filterData.put("Quality", 90);
		
		Map<String, Object> outProperties = new HashMap<>();
		outProperties.put("Overwrite", true);
		outProperties.put("FilterData", filterData);

		for (int i = 0; i < docs.length; i++) {
			File from = docs[i];
			File to = new File(from.getAbsoluteFile() + ".html");
			OpenOfficeInstance.convert(from, to, outProperties);
		}

		OpenOfficeInstance.stopService();
	}
}
