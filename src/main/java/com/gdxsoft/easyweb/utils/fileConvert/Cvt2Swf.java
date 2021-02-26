package com.gdxsoft.easyweb.utils.fileConvert;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

public class Cvt2Swf {

	private static String NO_PDF_EXTS = "pdf"; //,jpg,jpeg,gif
	private static MTableStr MAP_NO_PDF;

	public Cvt2Swf() {
		if (MAP_NO_PDF == null) {
			MAP_NO_PDF = new MTableStr();
			String[] ss = NO_PDF_EXTS.split(",");
			for (int i = 0; i < ss.length; i++) {
				MAP_NO_PDF.add(ss[i].toLowerCase().trim(), true);
			}
		}
	}

	public boolean deleteSource() {
		return UFile.delete(this._Source);
	}

	public boolean deletePdf() {
		return UFile.delete(this._Pdf);
	}

	public boolean deleteSwf() {
		return UFile.delete(this._Swf);
	}

	/**
	 * 文件转换成 flash
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean cvt2Swf(String source, String target) {
		this._Source = source;
		this._Swf = target;
		String pdfPath = source + ".pdf";

		// 生成Pdf文件，Pdf文件名称会保留在 _Pdf里
		boolean isPdfOk = this.cvt2Pdf(source, pdfPath);
		if (!isPdfOk) {
			return false;
		}

		Pdf2Swf swf = new Pdf2Swf();
		try {
			// 转换成Swf，pdf文件取_Pdf参数
			return swf.cvt2Swf(this._Pdf, target);

		} catch (Exception err) {
			_Err = err.getMessage();
			return false;
		} finally {

		}
	}

	/**
	 * 转换成Pdf
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean cvt2Pdf(String source, String target) {
		this._Source = source;
		this._Pdf = target;
		File2Pdf pdf = new File2Pdf();

		String ext = UFile.getFileExt(source).toLowerCase();

		if (!MAP_NO_PDF.containsKey(ext)) {
			try {
				this._Pdf = target + ".pdf";
				pdf.convert2PDF(source, this._Pdf);
				return true;
			} catch (Exception err) {
				_Err = err.getMessage();
				return false;
			}
		} else {
			this._Pdf = source;
			return true;
		}

	}

	private String _Err;

	/**
	 * 源文件地址
	 * 
	 * @return the _Source
	 */
	public String getSource() {
		return _Source;
	}

	/**
	 * 生成的Pdf
	 * 
	 * @return the _Pdf
	 */
	public String getPdf() {
		return _Pdf;
	}

	/**
	 * 生成的Swf
	 * 
	 * @return the _Swf
	 */
	public String getSwf() {
		return _Swf;
	}

	private String _Source;
	private String _Pdf;
	private String _Swf;

	/**
	 * @return the _Err
	 */
	public String getErr() {
		return _Err;
	}

}
