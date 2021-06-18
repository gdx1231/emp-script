package com.gdxsoft.easyweb.script.servlets;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;

public class FileOut {
	private static Map<String, String> MAP = new ConcurrentHashMap<String, String>();
	public static String DEF_DOWNLOAD_TYPE = "application/octet-stream";
	static {
		MAP.put("acp", "audio/x-mei-aac");
		MAP.put("aif", "audio/aiff");
		MAP.put("aiff", "audio/aiff");
		MAP.put("asa", "text/asa");
		MAP.put("asp", "text/asp");
		MAP.put("au", "audio/basic");
		MAP.put("awf", "application/vnd.adobe.workflow");
		MAP.put("apk", "application/vnd.android.package-archive");

		MAP.put("bmp", "application/x-bmp");
		MAP.put("bas", "text/plain");
		MAP.put("bcpio", "application/x-bcpio");
		MAP.put("bin", "application/octet-stream");
		MAP.put("bld", "application/bld");

		MAP.put("c4t", "application/x-c4t");
		MAP.put("cal", "application/x-cals");
		MAP.put("cdf", "application/x-netcdf");
		MAP.put("cel", "application/x-cel");
		MAP.put("cg4", "application/x-g4");
		MAP.put("cit", "application/x-cit");
		MAP.put("cml", "text/xml");
		MAP.put("cmx", "application/x-cmx");
		MAP.put("crl", "application/pkix-crl");
		MAP.put("csi", "application/x-csi");
		MAP.put("cut", "application/x-cut");

		MAP.put("dbm", "application/x-dbm");
		MAP.put("dcd", "text/xml");
		MAP.put("der", "application/x-x509-ca-cert");
		MAP.put("dib", "application/x-dib");
		MAP.put("doc", "application/msword");
		MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		MAP.put("dwf", "Model/vnd.dwf");
		MAP.put("dwg", "application/x-dwg");
		MAP.put("dxf", "application/x-dxf");

		MAP.put("ebk", "application/x-expandedbook");
		MAP.put("emb", "chemical/x-embl-dl-nucleotide");
		MAP.put("embl", "chemical/x-embl-dl-nucleotide");
		MAP.put("eps", "application/postscript");
		MAP.put("epub", "application/epub+zip");
		MAP.put("etc", "application/x-earthtime");
		MAP.put("etx", "text/x-setext");
		MAP.put("exe", "application/octet-stream");

		MAP.put("flv", "flv-application/octet-stream");
		MAP.put("fm", "application/x-maker");
		MAP.put("fpx", "image/x-fpx");
		MAP.put("fhc", "image/x-freehand");

		MAP.put("gbr", "application/x-gbr");
		MAP.put("gif", "image/gif");
		MAP.put("gp4", "application/x-gp4");
		MAP.put("gz", "application/x-gzip");

		MAP.put("h", "text/plain");
		MAP.put("hdf", "application/x-hdf");
		MAP.put("hdm", "text/x-hdml");
		MAP.put("htc", "text/x-component");
		MAP.put("htm", "text/html");
		MAP.put("html", "text/html");
		MAP.put("hts", "text/html");
		MAP.put("htt", "text/webviewhtml");

		MAP.put("ice", "x-conference/x-cooltalk");
		MAP.put("ico", "image/x-icon");
		MAP.put("ief", "image/ief");
		MAP.put("ifm", "image/gif");
		MAP.put("ifs", "image/ifs");
		MAP.put("iii", "application/x-iphone");
		MAP.put("imy", "audio/melody");
		MAP.put("ins", "application/x-internet-signup");
		MAP.put("ips", "application/x-ipscript");
		MAP.put("ipx", "application/x-ipix");
		MAP.put("isp", "application/x-internet-signup");
		MAP.put("itz", "audio/x-mod");
		MAP.put("ivr", "i-world/i-vrml");

		MAP.put("j2k", "image/j2k");
		MAP.put("jad", "text/vnd.sun.j2me.app-descriptor");
		MAP.put("jam", "application/x-jam");
		MAP.put("jar", "application/java-archive");
		MAP.put("java", "text/plain");
		MAP.put("json", "application/json");
		MAP.put("jfif", "image/pipeg");
		MAP.put("jnlp", "application/x-java-jnlp-file");
		MAP.put("jpe", "image/jpeg");
		MAP.put("jpeg", "image/jpeg");
		MAP.put("jpg", "image/jpeg");
		MAP.put("jpz", "image/jpeg");
		MAP.put("js", "application/x-javascript");
		MAP.put("jwc", "application/jwc");

		MAP.put("kjx", "application/x-kjx");

		MAP.put("lak", "x-lml/x-lak");
		MAP.put("latex", "application/x-latex");
		MAP.put("lcc", "application/fastman");
		MAP.put("lcl", "application/x-digitalloca");
		MAP.put("lcr", "application/x-digitalloca");
		MAP.put("lgh", "application/lgh");
		MAP.put("lha", "application/octet-stream");
		MAP.put("lml", "x-lml/x-lml");
		MAP.put("lmlpack", "x-lml/x-lmlpack");
		MAP.put("log", "text/plain");
		MAP.put("lsf", "video/x-la-asf");
		MAP.put("lsx", "video/x-la-asf");
		MAP.put("lzh", "application/octet-stream");

		MAP.put("m2v", "video/x-mpeg");
		MAP.put("m4e", "video/mpeg4");
		MAP.put("man", "application/x-troff-man");
		MAP.put("mdb", "application/msaccess");
		MAP.put("mfp", "application/x-shockwave-flash");
		MAP.put("mhtml", "message/rfc822");
		MAP.put("mid", "audio/mid");
		MAP.put("mil", "application/x-mil");
		MAP.put("mnd", "audio/x-musicnet-download");
		MAP.put("mocha", "application/x-javascript");
		MAP.put("mp1", "audio/mp1");
		MAP.put("mp2v", "video/mpeg");
		MAP.put("mp4", "video/mpeg4");
		MAP.put("mpd", "application/vnd.ms-project");
		MAP.put("mpeg", "video/mpg");
		MAP.put("mpga", "audio/rn-mpeg");
		MAP.put("mps", "video/x-mpeg");
		MAP.put("mpv", "video/mpg");
		MAP.put("mpw", "application/vnd.ms-project");
		MAP.put("mtx", "text/xml");

		MAP.put("net", "image/pnetvue");
		MAP.put("nws", "message/rfc822");
		MAP.put("nar", "application/zip");
		MAP.put("nbmp", "image/nbmp");
		MAP.put("nva", "application/x-neva1");
		MAP.put("npx", "application/x-netfpx");

		MAP.put("ogg", "audio/ogg");
		MAP.put("oda", "application/oda");
		MAP.put("oom", "application/x-AtlasMate-Plugin");
		MAP.put("out", "application/x-out");

		MAP.put("p12", "application/x-pkcs12");
		MAP.put("p7c", "application/pkcs7-mime");
		MAP.put("p7r", "application/x-pkcs7-certreqresp");
		MAP.put("pc5", "application/x-pc5");
		MAP.put("pcl", "application/x-pcl");
		MAP.put("pdf", "application/pdf");
		MAP.put("pdx", "application/vnd.adobe.pdx");
		MAP.put("pgl", "application/x-pgl");
		MAP.put("pko", "application/vnd.ms-pki.pko");
		MAP.put("plg", "text/html");
		MAP.put("plt", "application/x-plt");
		MAP.put("png", "application/x-png");
		MAP.put("ppa", "application/vnd.ms-powerpoint");
		MAP.put("pps", "application/vnd.ms-powerpoint");
		MAP.put("ppt", "application/x-ppt");
		MAP.put("prf", "application/pics-rules");
		MAP.put("prt", "application/x-prt");
		MAP.put("ps", "application/postscript");
		MAP.put("pwz", "application/vnd.ms-powerpoint");

		MAP.put("qcp", "audio/vnd.qcelp");
		MAP.put("qt", "video/quicktime");
		MAP.put("qti", "image/x-quicktime");
		MAP.put("qtif", "image/x-quicktime");

		MAP.put("r3t", "text/vnd.rn-realtext3d");
		MAP.put("ra", "audio/x-pn-realaudio");
		MAP.put("ram", "audio/x-pn-realaudio");
		MAP.put("rar", "application/octet-stream");
		MAP.put("ras", "image/x-cmu-raster");
		MAP.put("rc", "text/plain");
		MAP.put("rdf", "application/rdf+xml");
		MAP.put("rf", "image/vnd.rn-realflash");
		MAP.put("rgb", "image/x-rgb");
		MAP.put("rlf", "application/x-richlink");
		MAP.put("rm", "audio/x-pn-realaudio");
		MAP.put("rmf", "audio/x-rmf");
		MAP.put("rmi", "audio/mid");
		MAP.put("rmm", "audio/x-pn-realaudio");
		MAP.put("rmvb", "audio/x-pn-realaudio");
		MAP.put("rnx", "application/vnd.rn-realplayer");
		MAP.put("roff", "application/x-troff");
		MAP.put("rp", "image/vnd.rn-realpix");
		MAP.put("rpm", "audio/x-pn-realaudio-plugin");
		MAP.put("rt", "text/vnd.rn-realtext");
		MAP.put("rte", "x-lml/x-gps");
		MAP.put("rtf", "application/rtf");
		MAP.put("rtg", "application/metastream");
		MAP.put("rtx", "text/richtext");
		MAP.put("rv", "video/vnd.rn-realvideo");
		MAP.put("rwc", "application/x-rogerwilco");

		MAP.put("sat", "application/x-sat");
		MAP.put("sam", "application/x-sam");
		MAP.put("sdp", "application/sdp");
		MAP.put("sdw", "application/x-sdw");
		MAP.put("slb", "application/x-slb");
		MAP.put("slk", "drawing/x-slk");
		MAP.put("smil", "application/smil");
		MAP.put("snd", "audio/basic");
		MAP.put("sor", "text/plain");
		MAP.put("spl", "application/futuresplash");
		MAP.put("ssm", "application/streamingmedia");
		MAP.put("stl", "application/vnd.ms-pki.stl");
		MAP.put("sty", "application/x-sty");
		MAP.put("swf", "application/x-shockwave-flash");
		MAP.put("sit", "application/x-stuffit");
		MAP.put("sld", "application/x-sld");
		MAP.put("smi", "application/smil");
		MAP.put("smk", "application/x-smk");
		MAP.put("sol", "text/plain");
		MAP.put("spc", "application/x-pkcs7-certificates");
		MAP.put("spp", "text/xml");
		MAP.put("sst", "application/vnd.ms-pki.certstore");
		MAP.put("stm", "text/html");
		MAP.put("svg", "text/xml");

		MAP.put("tg4", "application/x-tg4");
		MAP.put("tif", "image/tiff");
		MAP.put("tiff", "image/tiff");
		MAP.put("top", "drawing/x-top");
		MAP.put("tsd", "text/xml");
		MAP.put("tdf", "application/x-tdf");
		MAP.put("tga", "application/x-tga");
		MAP.put("tif", "application/x-tif");
		MAP.put("tld", "text/xml");
		MAP.put("torrent", "application/x-bittorrent");
		MAP.put("txt", "text/plain");

		MAP.put("uin", "application/x-icq");
		MAP.put("uls", "text/iuls");
		MAP.put("ustar", "application/x-ustar");
		MAP.put("uu", "application/x-uuencode");

		MAP.put("vcf", "text/x-vcard");
		MAP.put("vdx", "application/vnd.visio");
		MAP.put("vpg", "application/x-vpeg005");
		MAP.put("vsd", "application/x-vsd");
		MAP.put("vst", "application/vnd.visio");
		MAP.put("vsw", "application/vnd.visio");
		MAP.put("vtx", "application/vnd.visio");
		MAP.put("vda", "application/x-vda");
		MAP.put("vml", "text/xml");
		MAP.put("vsd", "application/vnd.visio");
		MAP.put("vss", "application/vnd.visio");
		MAP.put("vst", "application/x-vst");
		MAP.put("vsx", "application/vnd.visio");
		MAP.put("vxml", "text/xml");

		MAP.put("wav", "audio/wav");
		MAP.put("wb1", "application/x-wb1");
		MAP.put("wiz", "application/msword");
		MAP.put("wk4", "application/x-wk4");
		MAP.put("wks", "application/x-wks");
		MAP.put("wma", "audio/x-ms-wma");
		MAP.put("wmf", "application/x-wmf");
		MAP.put("wmv", "video/x-ms-wmv");
		MAP.put("wmz", "application/x-ms-wmz");
		MAP.put("wpd", "application/x-wpd");
		MAP.put("wpl", "application/vnd.ms-wpl");
		MAP.put("wr1", "application/x-wr1");
		MAP.put("wrk", "application/x-wrk");
		MAP.put("wsdl", "text/xml");
		MAP.put("wax", "audio/x-ms-wax");
		MAP.put("wbmp", "image/vnd.wap.wbmp");
		MAP.put("wkq", "application/x-wkq");
		MAP.put("wm", "video/x-ms-wm");
		MAP.put("wmd", "application/x-ms-wmd");
		MAP.put("wml", "text/vnd.wap.wml");
		MAP.put("wmx", "video/x-ms-wmx");
		MAP.put("wp6", "application/x-wp6");
		MAP.put("wpg", "application/x-wpg");
		MAP.put("wri", "application/x-wri");
		MAP.put("ws", "application/x-ws");
		MAP.put("wsc", "text/scriptlet");
		MAP.put("wvx", "video/x-ms-wvx");

		MAP.put("xdp", "application/vnd.adobe.xdp");
		MAP.put("xfd", "application/vnd.adobe.xfd");
		MAP.put("xhtml", "text/html");
		MAP.put("xls", "application/x-xls");
		MAP.put("xls", "application/vnd.ms-excel");
		MAP.put("xml", "text/xml");
		MAP.put("xq", "text/xml");
		MAP.put("xquery", "text/xml");
		MAP.put("xsl", "text/xml");
		MAP.put("xwd", "application/x-xwd");
		MAP.put("xdr", "text/xml");
		MAP.put("xfdf", "application/vnd.adobe.xfdf");
		MAP.put("xlw", "application/x-xlw");
		MAP.put("xpl", "audio/scpls");
		MAP.put("xql", "text/xml");
		MAP.put("xsd", "text/xml");
		MAP.put("xslt", "text/xml");
		MAP.put("x_b", "application/x-x_b");
		MAP.put("xyz", "chemical/x-pdb");

		MAP.put("yz1", "application/x-yz1");

		MAP.put("z", "application/x-compress");
		MAP.put("zac", "application/x-zaurus-zac");
		MAP.put("zip", "application/zip");

		MAP.put("7z", "application/octet-stream");
		MAP.put("001", "application/x-001");
		MAP.put("301", "application/x-301");
		MAP.put("323", "text/h323");
		MAP.put("906", "application/x-906");
		MAP.put("907", "drawing/907");
	}
	private static Logger LOGGER = LoggerFactory.getLogger(FileOut.class);

	public static File getImageResizedFile(File image, String resize) {

		// 获取尺寸表达式，同时过滤非法的字符或路径
		Dimension size = FileOut.getImageResize(resize);
		if (size == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(image.getAbsolutePath());
		sb.append("$resized");
		sb.append(File.separator);
		sb.append(size.width);
		sb.append("x");
		sb.append(size.height);
		sb.append(".jpg");
		String imgSizePath = sb.toString();

		File imgSize = new File(imgSizePath);
		if (imgSize.exists()) {
			return imgSize;
		} else {
			return null;
		}
	}

	/**
	 * Parse the size string to the dimension, filter invalid char
	 * 
	 * @param resize 800x600
	 * @return null or dimension
	 */
	public static Dimension getImageResize(String resize) {
		if (StringUtils.isBlank(resize)) {
			return null;
		}

		String[] s1 = resize.toLowerCase().split("x");
		;
		if (s1.length != 2) {
			return null;
		}
		int w = 0;
		int h = 0;
		try {
			w = Integer.parseInt(s1[0]);
			h = Integer.parseInt(s1[1]);

			Dimension d = new Dimension();
			d.setSize(w, h);

			return d;
		} catch (Exception e) {
			LOGGER.error("ImageResize: {},{}", resize, e.getMessage());
			return null;
		}
	}

	private HttpServletRequest request;
	private HttpServletResponse response;
	private File file;
	private int httpStatusCode;

	public FileOut(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public boolean initFile(String filePath) {
		if (filePath == null) {
			httpStatusCode = 404;
			response.setStatus(404);
			return false;
		}
		File file = new File(filePath);

		return this.initFile(file);

	}

	public boolean initFile(File file) {

		if (!file.exists()) {
			httpStatusCode = 404;
			response.setStatus(404);
			return false;
		}
		if (file.isDirectory()) {
			// Bad Request
			httpStatusCode = 400;
			response.setStatus(400);
			return false;
		}
		if (!file.canRead()) {
			// Forbidden
			httpStatusCode = 403;
			response.setStatus(403);
			return false;
		}
		httpStatusCode = 200;
		this.file = file;
		return true;

	}

	public void outContetType() {
		String ext = UFile.getFileExt(file.getName()).toLowerCase();
		if (MAP.containsKey(ext)) {
			response.setContentType(MAP.get(ext));
		} else {
			response.setContentType(DEF_DOWNLOAD_TYPE);
		}
	}

	public void addCacheControl(Long cacheLife) {
		if (cacheLife != null) {
			response.setHeader("Cache-Control", "" + cacheLife);
			response.setHeader("Age", "" + cacheLife);
			response.setDateHeader("Expires", System.currentTimeMillis() + cacheLife * 1000);
		}
	}

	public boolean chcekIfNotModified() {
		String lastModified = Utils.getDateGMTString(new Date(file.lastModified()));
		if (lastModified.equals(request.getHeader("If-Modified-Since"))) {
			// 资源没有变化，返回 HTTP 304（Not Changed.）
			response.setStatus(304);
			return true;
		}
		response.setHeader("Last-Modified", lastModified);
		return false;
	}

	/**
	 * Download the file
	 * 
	 * @param downloadName
	 */

	public int download(String downloadName) {
		String name = file.getName();
		String ext = UFile.getFileExt(file.getName());
		if (ext.length() == 0) {
			ext = "bin";
			name = name + "." + ext;
		}
		if (!StringUtils.isBlank(downloadName)) {
			// \ / : * ? " < > |
			downloadName = downloadName.replace("/", "_").replace("\\", "_").replace("?", "_").replace("*", "_")
					.replace("|", "_").replace(":", "_").replace("<", "_").replace(">", "_").replace("\"", "_");
			
			String fileNoExit = UFile.getFileNoExt(downloadName);
			
			// Keep the file extension consistent 
			name = fileNoExit + "." + ext; 
		}
		name = Utils.textToUrl(name);

		response.setHeader("Location", name);
		response.setHeader("Cache-Control", "max-age=0");
		response.setHeader("Content-Disposition", "attachment; filename=" + name);

		response.setContentType("image/oct");

		return this.outFileBytesToClient();
	}

	/**
	 * Out file bytes in-line, e.g. image, pdf, and check the header flag If-Modified(304) and output the header
	 * Cache-control(OneWeek)
	 * 
	 * @return The length of the file
	 */
	public int outFileBytesInline() {
		long oneWeek = 604800L; // seconds
		return this.outFileBytesInline(true, oneWeek);
	}

	/**
	 * Out file bytes in-line, e.g. image, pdf
	 * 
	 * @param checkIfModified Whether to check the header If-Modified flag (304)
	 * @param cacheLife       Whether to output the Cache-Control header
	 * @return The length of the file
	 */
	public int outFileBytesInline(boolean checkIfModified, Long cacheLife) {

		// check If-Modified-Since 304
		if (checkIfModified && chcekIfNotModified()) {
			return -1;
		}

		// image type
		outContetType();
		// cache-control,age
		addCacheControl(cacheLife);

		// Out the file's bytes to user client
		return outFileBytesToClient();

	}

	public int outFileBytesInline(RestfulResult<Object> result, boolean checkIfModified, Long cacheLife) {
		if (httpStatusCode != 200) {
			result.setHttpStatusCode(httpStatusCode);
			result.setSuccess(false);
			return -1;
		}

		// 检查是否被修改 If-Modified-Since
		if (checkIfModified && chcekIfNotModified()) {
			result.setSuccess(true);
			result.setHttpStatusCode(304); // not modified
			return -1;
		}

		outContetType();
		addCacheControl(cacheLife);

		// Out the file's bytes to user client
		int length = this.outFileBytesToClient();
		if (length == -1) {
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setSuccess(false);
		} else {
			result.setCode(200);
			result.setHttpStatusCode(200);
			result.setSuccess(true);
		}
		return length;
	}

	/**
	 * Out the file's bytes to user client(response)
	 * 
	 * @param file     the file
	 * @param response HttpServletResponse
	 * @return out bytes length
	 */
	public int outFileBytesToClient() {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			return IOUtils.copy(input, response.getOutputStream());
		} catch (Exception err) {
			response.setStatus(500);
			LOGGER.error("out image: {}, {}", file.getAbsolutePath(), err.getMessage());
			return -1;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception err1) {
					LOGGER.error("out image, close input: {}, {}", file.getAbsolutePath(), err1.getMessage());
				}
			}
		}
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public File getFile() {
		return file;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}
}
