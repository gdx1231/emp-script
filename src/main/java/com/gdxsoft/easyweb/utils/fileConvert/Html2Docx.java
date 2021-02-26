package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;


/**
 * Html文件转换成 docx
 * 
 * @author guolei
 *
 */
public class Html2Docx {

	/**
	 * Html文件转换成 docx
	 * 
	 * @param htmlFilePathAndName
	 * @param docxFilePathAndName
	 */
	public void convert2Docx(File htmlFilePathAndName, File docxFilePathAndName) {
		OpenOfficeInstance.convert(htmlFilePathAndName, docxFilePathAndName);
	}

	/**
	 * Html文件转换成 docx
	 * 
	 * @param htmlFilePathAndName
	 * @param docxFilePathAndName
	 */
	public void convert2Docx(String htmlFilePathAndName, String docxFilePathAndName) {
		this.convert2Docx(new File(htmlFilePathAndName), new File(docxFilePathAndName));
	}

}
