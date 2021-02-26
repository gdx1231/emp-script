package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;


public class File2Pdf {

	/**
	 * 转换成PDF
	 * 
	 * @param inputFile
	 * @param pdfFile
	 */
	public void convert2PDF(File inputFile, File pdfFile) {
		OpenOfficeInstance.convert(inputFile, pdfFile);
	}

	/**
	 * 转换成PDF
	 * 
	 * @param inputFile
	 * @param pdfFile
	 */
	public void convert2PDF(String inputFile, String pdfFile) {
		this.convert2PDF(new File(inputFile), new File(pdfFile));
	}

	 
}
