package com.gdxsoft.easyweb.utils.fileConvert;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalConverter.Builder;
import org.jodconverter.local.filter.PageCounterFilter;
import org.jodconverter.local.filter.PagesSelectorFilter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfSOffice;
import com.gdxsoft.easyweb.utils.UFile;

public class OpenOfficeInstance {
	private static Logger LOGGER = LoggerFactory.getLogger(OpenOfficeInstance.class);

	private static String HTML = "<!DOCTYPE HTML><html><head>\n"
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">\n"
			+ "<meta  charset=\"UTF-8\">\n</head>\n<body>";
	public static OfficeManager officeManager;
	public static boolean SERVICE_START = false;

	public synchronized static void startService() {
		ConfSOffice conf = ConfSOffice.getInstance();
		String officeHome = conf.getSofficePath();
		int[] ports = conf.getPorts();

		if (ports.length == 0) {
			// 未定义端口时，使用随机端口，避免多个项目之间冲突
			int randomPort = ConfSOffice.getAvailablePort();
			LOGGER.info("Start office using random port: {}", randomPort);
			ports = new int[1];
			ports[0] = randomPort;
			conf.setPorts(ports);
		}

		if (StringUtils.isBlank(officeHome)) {
			// auto search LibreOffice or OpenOffice
			LOGGER.info("Auto search LibreOffice or OpenOffice");
			officeManager = LocalOfficeManager.builder().portNumbers(ports).build();
		} else {
			LOGGER.info("Start office with: {}", officeHome);
			officeManager = LocalOfficeManager.builder().officeHome(officeHome).portNumbers(ports).build();
		}

		try {
			officeManager.start();
			LOGGER.info("Started. {}", officeHome);
			SERVICE_START = true;
		} catch (OfficeException ce) {
			// 寻找soffice.bin时，自动添加program文件夹。如果再配置成D:\\Program Files (x86)\\OpenOffice
			// 4\\program，则它在验证是否有soffice.bin文件时。 实际路径为：D:\\Program Files
			// (x86)\\OpenOffice 4\\program\\program\\soffice.bin。所以报错。
			SERVICE_START = false;
			LOGGER.error(officeHome, ce);
		}

	}

	public synchronized static void stopService() {
		if (officeManager != null && SERVICE_START) {
			try {
				officeManager.stop();
			} catch (OfficeException ce) {
				LOGGER.error("Stop office convert", ce);
			}
			SERVICE_START = false;
		}
	}

	/**
	 * Convert the file type
	 * 
	 * @param inputFile     the input file
	 * @param outFile       the output file
	 * @param outProperties the output properties
	 */
	public static void convert(File inputFile, File outFile, Map<String, Object> outProperties) {
		if (!SERVICE_START) {
			startService();
		}

		// ### TextDocument
		/*
		 * bib:"BibTeX_Writer" doc:"MS Word 97" doc:"MS WinWord 6.0" doc:"MS Word 95"
		 * xml:"DocBook File" docx:"Office Open XML Text" docx:"MS Word 2007 XML"
		 * fodt:"OpenDocument Text Flat XML" html:"HTML (StarWriter)" ltx:"LaTeX_Writer"
		 * txt:"MediaWiki" odt:"writer8" xml:"MS Word 2003 XML" ott:"writer8_template"
		 * pdb:"AportisDoc Palm DB" pdf:"writer_pdf_Export" psw:"PocketWord File"
		 * rtf:"Rich Text Format" sdw:"StarWriter 5.0" sdw:"StarWriter 4.0"
		 * sdw:"StarWriter 3.0" stw:"writer_StarOffice_XML_Writer_Template"
		 * sxw:"StarOffice XML (Writer)" txt:"Text (encoded)" txt:"Text" uot:"UOF text"
		 * vor:"StarWriter 5.0 Vorlage/Template" vor:"StarWriter 4.0 Vorlage/Template"
		 * vor:"StarWriter 3.0 Vorlage/Template" html:"XHTML Writer File"
		 */

		// ### * WebDocument
		/*
		 * txt:"Text (encoded) (StarWriter/Web)"
		 * html:"writer_web_StarOffice_XML_Writer_Web_Template" html:"HTML"
		 * html:"writerweb8_writer_template" txt:"MediaWiki_Web"
		 * pdf:"writer_web_pdf_Export" sdw:"StarWriter 3.0 (StarWriter/Web)"
		 * sdw:"StarWriter 4.0 (StarWriter/Web)" sdw:"StarWriter 5.0 (StarWriter/Web)"
		 * txt:"writerweb8_writer" txt:"writer_web_StarOffice_XML_Writer"
		 * txt:"Text (StarWriter/Web)" vor:"StarWriter/Web 4.0 Vorlage/Template"
		 * vor:"StarWriter/Web 5.0 Vorlage/Template" ### Spreadsheet
		 * csv:"Text - txt - csv (StarCalc)" dbf:"dBase" dif:"DIF"
		 * fods:"OpenDocument Spreadsheet Flat XML" html:"HTML (StarCalc)" ods:"calc8"
		 * xml:"MS Excel 2003 XML" ots:"calc8_template" pdf:"calc_pdf_Export"
		 * pxl:"Pocket Excel" sdc:"StarCalc 5.0" sdc:"StarCalc 4.0" sdc:"StarCalc 3.0"
		 * slk:"SYLK" stc:"calc_StarOffice_XML_Calc_Template"
		 * sxc:"StarOffice XML (Calc)" uos:"UOF spreadsheet"
		 * vor:"StarCalc 3.0 Vorlage/Template" vor:"StarCalc 4.0 Vorlage/Template"
		 * vor:"StarCalc 5.0 Vorlage/Template" xhtml:"XHTML Calc File" xls:"MS Excel 97"
		 * xls:"MS Excel 5.0/95" xls:"MS Excel 95" xlt:"MS Excel 97 Vorlage/Template"
		 * xlt:"MS Excel 5.0/95 Vorlage/Template" xlt:"MS Excel 95 Vorlage/Template"
		 */

		// ### * Graphics
		/*
		 * bmp:"draw_bmp_Export" emf:"draw_emf_Export" eps:"draw_eps_Export"
		 * fodg:"OpenDocument Drawing Flat XML" gif:"draw_gif_Export"
		 * html:"draw_html_Export" jpg:"draw_jpg_Export" met:"draw_met_Export"
		 * odd:"draw8" otg:"draw8_template" pbm:"draw_pbm_Export" pct:"draw_pct_Export"
		 * pdf:"draw_pdf_Export" pgm:"draw_pgm_Export" png:"draw_png_Export"
		 * ppm:"draw_ppm_Export" ras:"draw_ras_Export"
		 * std:"draw_StarOffice_XML_Draw_Template" svg:"draw_svg_Export"
		 * svm:"draw_svm_Export" swf:"draw_flash_Export" sxd:"StarOffice XML (Draw)"
		 * sxd:"StarDraw 3.0" sxd:"StarDraw 5.0" sxw:"StarOffice XML (Draw)"
		 * tiff:"draw_tif_Export" vor:"StarDraw 5.0 Vorlage" vor:"StarDraw 3.0 Vorlage"
		 * wmf:"draw_wmf_Export" xhtml:"XHTML Draw File" xpm:"draw_xpm_Export"
		 */

		// ### Presentation
		/*
		 * bmp:"impress_bmp_Export" emf:"impress_emf_Export" eps:"impress_eps_Export"
		 * fodp:"OpenDocument Presentation Flat XML" gif:"impress_gif_Export"
		 * html:"impress_html_Export" jpg:"impress_jpg_Export" met:"impress_met_Export"
		 * odg:"impress8_draw" odp:"impress8" otp:"impress8_template"
		 * pbm:"impress_pbm_Export" pct:"impress_pct_Export" pdf:"impress_pdf_Export"
		 * pgm:"impress_pgm_Export" png:"impress_png_Export"
		 * potm:"Impress MS PowerPoint 2007 XML Template" pot:"MS PowerPoint 97 Vorlage"
		 * ppm:"impress_ppm_Export" pptx:"Impress MS PowerPoint 2007 XML"
		 * pps:"MS PowerPoint 97 Autoplay" ppt:"MS PowerPoint 97" pwp:"placeware_Export"
		 * ras:"impress_ras_Export" sda:"StarDraw 5.0 (StarImpress)"
		 * sdd:"StarImpress 5.0" sdd:"StarDraw 3.0 (StarImpress)" sdd:"StarImpress 4.0"
		 * sxd:"impress_StarOffice_XML_Draw"
		 * sti:"impress_StarOffice_XML_Impress_Template" svg:"impress_svg_Export"
		 * svm:"impress_svm_Export" swf:"impress_flash_Export"
		 * sxi:"StarOffice XML (Impress)" tiff:"impress_tif_Export"
		 * uop:"UOF presentation" vor:"StarImpress 5.0 Vorlage"
		 * vor:"StarDraw 3.0 Vorlage (StarImpress)" vor:"StarImpress 4.0 Vorlage"
		 * vor:"StarDraw 5.0 Vorlage (StarImpress)" wmf:"impress_wmf_Export"
		 * xml:"XHTML Impress File" xpm:"impress_xpm_Export"
		 */
		String extOut = UFile.getFileExt(outFile.getName());
		DocumentFormatRegistry dfr = DefaultDocumentFormatRegistry.getInstance();
		DocumentFormat formatInput = dfr.getFormatByExtension(UFile.getFileExt(inputFile.getName()));

		PageCounterFilter pageCounterFilter = new PageCounterFilter();
		PagesSelectorFilter firstSelectorFilter = null;

		boolean isPpt = formatInput.getInputFamily() == DocumentFamily.PRESENTATION;

		File outputFile = new File(outFile.getAbsolutePath());

		StringBuilder sbHtml = new StringBuilder();
		boolean isOutHtml = extOut.equalsIgnoreCase("html") || extOut.equalsIgnoreCase("htm");
		if (isPpt && (extOut.equalsIgnoreCase("jpeg") || extOut.equalsIgnoreCase("bmp")
				|| extOut.equalsIgnoreCase("jpg") || extOut.equalsIgnoreCase("png") || isOutHtml)) {

			if (isOutHtml) {
				outputFile = getIndexFile(outFile, 1, "jpg");
				sbHtml.append(HTML);
			} else {
				outputFile = getIndexFile(outFile, 1);
			}
			firstSelectorFilter = new PagesSelectorFilter(1);
		}

		try {

			OpenOfficeInstance.convert(inputFile, outputFile, outProperties, pageCounterFilter, firstSelectorFilter);
			if (firstSelectorFilter == null) {
				return;
			}
			if (isOutHtml) {
				sbHtml.append("<div><img src=\"" + outputFile.getName() + "\"></div>");
			}
			for (int i = 2; i <= pageCounterFilter.getPageCount(); i++) {
				File indexed;
				if (isOutHtml) {
					indexed = getIndexFile(outFile, i, "jpg");
				} else {
					indexed = getIndexFile(outFile, i);
				}
				PagesSelectorFilter indexedFilter = new PagesSelectorFilter(i);
				OpenOfficeInstance.convert(inputFile, indexed, outProperties, pageCounterFilter, indexedFilter);
				if (isOutHtml) {
					sbHtml.append("<div><img src=\"" + indexed.getName() + "\"></div>");
				}
			}
			if (isOutHtml) {
				sbHtml.append("</body></html>");
				try {
					UFile.createNewTextFile(outFile.getAbsolutePath(), sbHtml.toString());
				} catch (IOException e) {
					LOGGER.error("Fail to create html file ", e.getLocalizedMessage());
				}
			}
		} catch (OfficeException e) {
			LOGGER.error("Convert fail", e.getLocalizedMessage());
		}

	}

	private static File getIndexFile(File file, int index) {
		String ext = UFile.getFileExt(file.getName());
		String name = file.getParent() + "/" + UFile.getFileNoExt(file.getName()) + "." + index + "." + ext;
		return new File(name);
	}

	private static File getIndexFile(File file, int index, String newExt) {
		String name = file.getParent() + "/" + UFile.getFileNoExt(file.getName()) + "." + index + "." + newExt;
		return new File(name);
	}

	private static void convert(File inputFile, File outputFile, Map<String, Object> outProperties,
			PageCounterFilter pageCounterFilter, PagesSelectorFilter firstSelectorFilter) throws OfficeException {
		Builder builder = LocalConverter.builder().officeManager(officeManager);
		if (outProperties != null) {
			builder.storeProperties(outProperties);
		}
		if (firstSelectorFilter != null) {
			builder.filterChain(pageCounterFilter, firstSelectorFilter);
		}
		LocalConverter localConvert = builder.build();

		long t0 = System.currentTimeMillis();
		LOGGER.info("Start convert from " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());

		localConvert.convert(inputFile).to(outputFile).execute();

		long t1 = System.currentTimeMillis();
		long span = t1 - t0;
		LOGGER.info("Convert completed (" + span + "ms)");

	}

	public static void convert(String inputFilePath, String outFilePath, Map<String, Object> outProperties) {
		convert(new File(inputFilePath), new File(outFilePath), outProperties);
	}

	/**
	 * Convert the file type
	 * 
	 * @param inputFile the input file
	 * @param outFile   the output file
	 */
	public static void convert(File inputFile, File outFile) {
		convert(inputFile, outFile, null);

	}

	/**
	 * Convert the file type
	 * 
	 * @param inputFilePath the input file path
	 * @param outFilePath   the output file path
	 */
	public void convert(String inputFilePath, String outFilePath) {
		convert(inputFilePath, outFilePath, null);
	}
}
