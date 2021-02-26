package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;


public class File2Html {

	/**
	 * 转换成 Html
	 * 
	 * @param inputFile
	 * @param htmlFile
	 */
	public void convert2Html(File inputFile, File htmlFile) {
		OpenOfficeInstance.convert(inputFile, htmlFile);
	}

	/**
	 * 转换成 Html
	 * 
	 * @param inputFile
	 * @param htmlFile
	 */
	public void convert2Html(String inputFile, String htmlFile) {
		this.convert2Html(new File(inputFile), new File(htmlFile));
	}

	 
}
