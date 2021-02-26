package com.gdxsoft.easyweb.script.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.cache.SqlCachedValue;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UImages;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.fileConvert.File2Html;

public class ServletResources extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletResources() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");

		// response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setHeader("X-EWA", "V2.2;gdxsoft.com");
		GZipOut out1 = new GZipOut(request, response);

		if (request.getParameter("method") == null) { // 获取脚本
			// PrintWriter out = response.getWriter();
			HttpSession session = request.getSession();
			String lang = "zhcn";
			if ("enus".equals(request.getParameter("oplang"))) {
				lang = "enus"; // 传递参数，强制语言
			} else {
				if (session.getAttribute("SYS_EWA_LANG") != null) {
					lang = session.getAttribute("SYS_EWA_LANG").toString().toLowerCase();
				}
				if (!lang.equals("enus")) {
					lang = "zhcn";
				}
			}
			String IfNoneMatch = request.getHeader("If-None-Match");
			String js = "";
			try {
				js = EwaGlobals.instance().createJs(lang);

			} catch (Exception e) {
				js = e.getMessage();
				IfNoneMatch = null;
			}

			String etag = "EWA_RES/" + lang + "/" + js.hashCode();

			if (IfNoneMatch != null && IfNoneMatch.equals(etag)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			response.addHeader("Cache-Control", "public, max-age=1231");
			response.addHeader("ETag", etag);
			response.setContentType("text/javascript");
			// out.println(js);
			out1.outContent(js);
			return;
		} else if (request.getParameter("method").equalsIgnoreCase("HtmlImages")) {// html获取图片信息
			// 从 HtmlEditor调用
			response.setContentType("text/x-json");
			try {
				String rst = this.handleGetImages(request, response);
				out1.outContent(rst);
			} catch (Exception e) {
				e.printStackTrace();
				out1.outContent("{rst:false,msg:\"" + Utils.textToJscript(e.getMessage()) + "\"}");
			}
		} else if (request.getParameter("method").equalsIgnoreCase("2Html")) {// html获取图片信息
			// 从 HtmlEditor调用
			response.setContentType("text/x-json");
			try {
				String rst = this.handleCreateHtml(request, response);
				out1.outContent(rst);
			} catch (Exception e) {
				e.printStackTrace();
				out1.outContent("{rst:false,msg:\"" + Utils.textToJscript(e.getMessage()) + "\"}");
			}
		} else if (request.getParameter("method").equalsIgnoreCase("combine")) {// 合并脚本或css
			String rst = this.handleCombineMaster(request, response);
			if (rst != null) {
				out1.outContent(rst);
			}
		} else {
			return;
		}
	}

	/**
	 * 处理合并资源的请求
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private String handleCombineMaster(HttpServletRequest request, HttpServletResponse response) {
		String base = request.getScheme() + "://" + request.getServerName()
				+ (request.getServerPort() == 80 ? "" : ":" + request.getServerPort()) + "/";
		String refer = request.getHeader("Referer");
		if (refer == null || refer.trim().length() == 0 || refer.indexOf(base) != 0) {
			// out1.outContent("NO REFERER");
			// return;
		}
		String span = request.getParameter("span");
		int iSpan = 60 * 60 * 10; // 600分钟
		if (span != null && span.trim().length() > 0) {
			try {
				iSpan = Integer.parseInt(span);
			} catch (Exception err) {

			}
		}
		if (request.getParameter("new") == null) {
			String IfNoneMatch = request.getHeader("If-None-Match");
			if (IfNoneMatch != null) {
				SqlCachedValue sc = SqlCached.getInstance().getText(IfNoneMatch);
				if (sc != null && !sc.checkOvertime(iSpan)) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return null;
				}
			}
		}
		if (request.getParameter("files") == null || request.getParameter("files").trim().length() == 0) {
			try {
				String rst = this.handleCombineTag(request, response, iSpan);
				String etag = "EWAREST/" + rst.hashCode();
				etag = etag.replace("-", "gdx");
				SqlCached.getInstance().add(etag, etag);
				response.addHeader("ETag", etag);
				response.addHeader("Cache-Control", "public, max-age=1201");
				return rst;
			} catch (Exception e) {
				e.printStackTrace();
				return "{rst:false,msg:\"" + Utils.textToJscript(e.getMessage()) + "\"}";
			}
		} else {
			if (request.getParameter("files").indexOf(".css") > 0) {
				response.addHeader("Content-Type", "text/css;charset=UTF-8");
				response.setContentType("text/css");
			} else {
				response.addHeader("Content-Type", "text/javascript;charset=UTF-8");
				response.setContentType("text/javascript");
			}
			try {
				String rst = this.handleCombine(request, response, iSpan);
				String etag = "EWARESF/" + rst.hashCode();
				etag = etag.replace("-", "gdx");
				SqlCached.getInstance().add(etag, etag);
				response.addHeader("ETag", etag);
				response.addHeader("Cache-Control", "public, max-age=101");
				return rst;
			} catch (Exception e) {
				e.printStackTrace();
				return "{rst:false,msg:\"" + Utils.textToJscript(e.getMessage()) + "\"}";
			}
		}
	}

	/**
	 * 合并js或 css 文件
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String handleCombineTag(HttpServletRequest request, HttpServletResponse response, int iSpan)
			throws Exception {
		String tag = request.getParameter("tag");
		if (tag == null || tag.length() == 0) {
			return "/* no resources-(tag) */";
		}
		String cache_name = request.getServerName() + "|||" + tag;
		SqlCachedValue sc = SqlCached.getInstance().getText(cache_name);
		// 返回cache
		if (sc != null && !sc.checkOvertime(iSpan) && request.getParameter("new") == null) {
			// 输出头，否则console会提示警告
			if (tag.indexOf("css") >= 0) {
				response.addHeader("Content-Type", "text/css;charset=UTF-8");
				response.setContentType("text/css");
			} else {
				response.addHeader("Content-Type", "text/javascript;charset=UTF-8");
				response.setContentType("text/javascript");
			}

			response.addHeader("X-GDX", "cached");
			return sc.toString();
		}

		Document doc = UPath.getCfgXmlDoc();
		NodeList nl = doc.getElementsByTagName("combineResources");

		Element combineResources = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String name = ele.getAttribute("Name");
			if (name.equals(tag)) {
				combineResources = ele;
				break;
			}
		}

		if (combineResources == null) {
			return "/* no resources-(tag error) */";
		}

		nl = combineResources.getElementsByTagName("resource");
		String host = combineResources.getAttribute("Host");
		String type = combineResources.getAttribute("Type");

		if (type != null && type.equals("css")) {
			response.addHeader("Content-Type", "text/css;charset=UTF-8");
			response.setContentType("text/css");
		} else if (type != null && type.equals("js")) {
			response.addHeader("Content-Type", "text/javascript;charset=UTF-8");
			response.setContentType("text/javascript");
		}

		boolean is_debug = request.getParameter("js_debug") != null;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);

			String eleDebug = ele.getAttribute("Debug");
			String eleNormal = ele.getAttribute("Normal");

			String href = is_debug ? eleDebug : eleNormal;

			if (href == null || href.trim().length() == 0) {
				href = is_debug ? eleNormal : eleDebug;
			}
			// 没有资源定义
			if (href == null || href.trim().length() == 0) {
				continue;
			}
			UNet net = new UNet();
			net.setIsShowLog(true);
			net.setEncode("utf-8");

			sb.append("\n\n/** " + href + " **/\n\n");

			String url = host + href;
			try {
				String rst = net.doGet(url); // 获取文件内容
				if (href.indexOf("font-awesome") > 0) {
					int loc = href.lastIndexOf("/");
					String p1 = href.substring(0, loc + 1);
					rst = rst.replace("url('", "url('" + p1);
				} else if (href.indexOf("bootstrap") > 0) {
					int loc = href.lastIndexOf("/");
					String p1 = href.substring(0, loc + 1);
					rst = rst.replace("url(", "url(" + p1);
				}
				sb.append("\n");
				sb.append(rst);
			} catch (Exception err) {
				sb.append("\n/**" + err.getMessage() + "**/");
				System.out.println(err.getMessage());
			}
		}

		SqlCached.getInstance().add(cache_name, sb.toString());
		return sb.toString();
	}

	/**
	 * 合并js或 css 文件
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String handleCombine(HttpServletRequest request, HttpServletResponse response, int iSpan) throws Exception {
		String files = request.getParameter("files");
		if (files == null || files.length() == 0) {
			return "/* no resources */";
		}
		SqlCachedValue sc = SqlCached.getInstance().getText(files);
		if (sc == null || sc.checkOvertime(iSpan) || request.getParameter("new") != null) {
			String[] filesArr = files.split(";");
			String domain = request.getServerName();
			// 检查域名是否合法
			if (!UPath.checkIsValidDomain(domain)) {
				return "domain not valid ";
			}
			String base;
			String host = UPath.getVALID_DOMAINS().get("___HOST___");
			if (host != null && host.trim().length() > 0) {
				base = host;
			} else {
				base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < filesArr.length; i++) {
				String file = filesArr[i].trim();
				if (file.length() == 0) {
					continue;
				}
				if (file.indexOf("xmlname") >= 0) {
					return "deny<xmlname>";
				}
				if (file.indexOf("?") >= 0) {
					return "deny<?>";
				}
				if (file.indexOf("<") >= 0) {
					return "deny<tag lt>";
				}
				String url;
				url = base + file;

				UNet net = new UNet();
				net.setIsShowLog(true);

				net.setEncode("utf-8");
				sb.append("\n\n/** " + file + " **/\n\n");
				try {
					String rst = net.doGet(url); // 获取文件内容
					if (file.indexOf("font-awesome") > 0) {
						int loc = file.lastIndexOf("/");
						String p1 = file.substring(0, loc + 1);
						rst = rst.replace("url('", "url('" + p1);
					} else if (file.indexOf("bootstrap") > 0) {
						int loc = file.lastIndexOf("/");
						String p1 = file.substring(0, loc + 1);
						rst = rst.replace("url(", "url(" + p1);
					}
					sb.append("\n");
					sb.append(rst);
				} catch (Exception err) {
					sb.append("\n/**" + err.getMessage() + "**/");
					System.out.println(err.getMessage());
				}

			}
			SqlCached.getInstance().add(files, sb.toString());
			return sb.toString();
		} else {
			return sc.toString();
		}
	}

	/**
	 * 转换文件到Html EWA_STYLE/js/js_jquery/src/misc/EWA_MiscPasteTool.js
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String handleCreateHtml(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 获取传递的参数
		JSONObject rst = new JSONObject();
		rst.put("rst", true);

		String f = request.getParameter("name");
		if (f == null || f.equals("")) {
			f = "nonme.doc";
		}
		String ext = UFile.getFileExt(f);
		if (ext == null || ext.trim().length() == 0) {
			ext = "doc";
		}

		if (ext.indexOf('?') > 0) {
			ext = ext.split("\\?")[0];
		}
		if (ext.length() > 4) {
			ext = "doc";
		}
		ext = ext.replace("?", "g").replace("*", "d").replace("&", "d").replace(":", "d").replace("/", "d")
				.replace("\\", "d").replace("|", "d").replace("%", "d");
		ext = ext.toLowerCase();

		String base64 = request.getParameter("src");
		if (base64 == null) {
			rst.put("rst", false);
			rst.put("msg", "上传文件内容为空");
			return rst.toString();
		}
		int loc = base64.indexOf(",");
		base64 = base64.substring(loc + 1);
		byte[] buf = UConvert.FromBase64String(base64);
		String name = "base64" + Utils.md5(buf).toLowerCase() + "." + ext;

		JSONObject r1 = this.saveFile(buf, name, f);

		String localName = r1.getString("local_ph");
		String htmlFile = localName + ".html";

		File fileHtml = new File(htmlFile);
		String cnt;

		// 操作系统
		String os = System.getProperty("os.name");

		// mac/linux下系统转换默认为utf-8，win下是gb2312
		String default_charset = "utf-8";
		if (os != null && os.toLowerCase().indexOf("windows") >= 0) {
			default_charset = "gbk";
		}

		byte[] buf_html;
		if (!fileHtml.exists()) {
			File2Html f2 = new File2Html();
			f2.convert2Html(localName, htmlFile);
		}

		buf_html = UFile.readFileBytes(fileHtml.getAbsolutePath());

		cnt = new String(buf_html, default_charset);

		String charset = this.checkCharsetOfHtml(cnt, default_charset);

		if (charset.equals("gb2312")) {
			charset = "gbk";
		}

		if (!default_charset.equals(charset)) {
			cnt = new String(buf, charset);
		}

		rst.put("cnt", cnt);
		rst.put("img_root", r1.getString("img_root"));

		return rst.toString();
	}

	/**
	 * 检测上传创建文件的编码
	 * 
	 * @param cnt
	 * @return
	 */
	private String checkCharsetOfHtml(String cnt, String default_charset) {
		org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(cnt, default_charset);
		org.jsoup.select.Elements eles1 = doc.getElementsByTag("meta");
		String charset = default_charset;
		for (int i = 0; i < eles1.size(); i++) {
			String http_equiv = eles1.get(i).attr("http-equiv");
			if (http_equiv != null && http_equiv.equalsIgnoreCase("CONTENT-TYPE")) {
				String att_content = eles1.get(i).attr("content");
				if (att_content != null) {
					int loc0 = att_content.toLowerCase().indexOf("charset=");
					if (loc0 >= 0) {
						charset = att_content.substring(loc0 + 8);
						charset = charset.split(";")[0];
						break;
					}
				}
			}
		}

		return charset.toLowerCase();
	}

	/**
	 * 处理文件获取（html获取图片信息） javascript call from
	 * EWA_STYLE/js/js_jquery/src/misc/EWA_MiscPasteTool.js
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String handleGetImages(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession();
		RequestValue rv = new RequestValue(request, session);
		// 获取传递的参数
		String data = rv.s("d");
		if (data == null) {
			throw new Exception("获取图片数据为空，可能是Tomcat的maxPostSize设置太小。");
		}
		JSONArray files = new JSONArray(data);

		JSONObject rst = new JSONObject();
		rst.put("rst", true);
		JSONArray arr = new JSONArray();
		rst.put("rsts", arr);

		for (int i = 0; i < files.length(); i++) {
			JSONObject file = files.getJSONObject(i);
			String mode = file.getString("mode");
			if (mode == null || mode.trim().length() == 0) {
				continue;
			}
			// 获取方式
			if (mode.equals("normal") || mode.equals("background")) {

				String f = file.getString("src");

				String ext = UFile.getFileExt(f);
				if (ext == null || ext.trim().length() == 0) {
					ext = "jpg";
				}

				if (ext.indexOf('?') > 0) {
					ext = ext.split("\\?")[0];
				}
				if (ext.length() > 4) {
					ext = "jpg";
				}
				ext = ext.replace("?", "g").replace("*", "d").replace("&", "d").replace(":", "d").replace("/", "d")
						.replace("\\", "d").replace("|", "d").replace("%", "d");
				ext = ext.toLowerCase();

				String name = (f.hashCode() + ".").replace("-", "gdx") + ext;

				JSONObject r1;
				String prefix = this.checkExists(name);
				if (prefix != null) {
					r1 = new JSONObject();
					r1.put("id", file.getString("id"));
					r1.put("local", UPath.getPATH_UPLOAD_URL() + prefix);
					String imgPath = UPath.getPATH_UPLOAD() + prefix;
					r1.put("local_ph", imgPath);
				} else {
					UNet net = new UNet();
					byte[] buf = net.downloadData(f);
					r1 = this.saveFile(buf, name, file.getString("id"));
					// r1.remove("local_ph");

				}
				r1.put("mode", mode);
				arr.put(r1);
			} else if (mode.equals("base64")) {
				String base64 = file.getString("src");
				int loc = base64.indexOf(",");
				// data:image/jpeg;base64,
				String metaData = base64.substring(0, loc);
				String ext = metaData.split("\\:")[1].split("\\;")[0].split("\\/")[1];
				base64 = base64.substring(loc + 1);
				byte[] buf = UConvert.FromBase64String(base64);
				String name = "base64" + Utils.md5(buf).toLowerCase() + "." + ext;
				JSONObject r1 = this.saveFile(buf, name, file.getString("id"));

				r1.put("mode", mode);
				arr.put(r1);
			}
		}

		this.resizeImages(rv, arr);

		// 清除本地保存信息
		for (int i = 0; i < arr.length(); i++) {
			JSONObject d = arr.getJSONObject(i);
			if (d.has("local_ph")) {
				d.remove("local_ph");
			}
		}
		return rst.toString();
	}

	/**
	 * 上传图片缩放
	 * 
	 * @param rv
	 * @param arr
	 */
	private void resizeImages(RequestValue rv, JSONArray arr) {
		String imageReiszes = rv.s("ImageReiszes");
		if (imageReiszes == null || imageReiszes.trim().length() == 0) {
			return;
		}
		List<java.awt.Dimension> dimResizes = new ArrayList<java.awt.Dimension>();
		try {
			String[] arrImageReiszes = imageReiszes.split(",");
			for (int ai = 0; ai < arrImageReiszes.length; ai++) {
				String resize = arrImageReiszes[ai].trim();
				String[] resizes = resize.toLowerCase().split("x");
				if (resizes.length == 2) {

					int w = Integer.parseInt(resizes[0]);
					int h = Integer.parseInt(resizes[1]);
					if (w <= 1920 && w >= 100 && h <= 1920 && h >= 100) {
						java.awt.Dimension d = new java.awt.Dimension(w, h);
						dimResizes.add(d);
					}

				}
			}

		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
		if (dimResizes.size() == 0) {
			return;
		}

		java.awt.Dimension[] dims = new java.awt.Dimension[dimResizes.size()];
		for (int i = 0; i < dimResizes.size(); i++) {
			dims[i] = dimResizes.get(i);
		}

		try {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject d = arr.getJSONObject(i);
				String local_ph = d.optString("local_ph");
				File[] fs = UImages.createResized(local_ph, dims);
				JSONArray arrFs = new JSONArray();
				d.put("resizes", arrFs);
				for (int m = 0; m < fs.length; m++) {
					arrFs.put(fs[m].getName());
				}
			}

		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}

	/**
	 * 保存文件信息
	 * 
	 * @param buf
	 * @param name
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private JSONObject saveFile(byte[] buf, String name, String id) throws Exception {
		String prefix = createLocalName(name);
		String imgPath = UPath.getPATH_UPLOAD() + prefix;
		UFile.createBinaryFile(imgPath, buf, true);
		JSONObject r1 = new JSONObject();
		r1.put("id", id);
		r1.put("local", UPath.getPATH_UPLOAD_URL() + prefix);
		r1.put("local_ph", imgPath);

		String local = r1.getString("local");
		int loc = local.lastIndexOf("/");
		r1.put("img_root", local.substring(0, loc));
		return r1;
	}

	/**
	 * 创建本地文件（分解字符串）
	 * 
	 * @param name
	 * @return
	 */
	private String createLocalName(String name) {
		String prefix = "/htmledit/" + UFile.createSplitDirPath(name, 4) + "/" + name;
		return prefix;
	}

	/**
	 * 检查文件是否存在（返回空表示不存在）
	 * 
	 * @param name
	 * @return
	 */
	private String checkExists(String name) {
		String prefix = createLocalName(name);
		String imgPath = UPath.getPATH_UPLOAD() + prefix;

		File f = new File(imgPath);
		if (f.exists()) {
			if (f.length() > 0) {
				return prefix;
			}
		}
		return null;

	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	/**
	 * Returns information about the servlet, such as author, version, and
	 * copyright.
	 * 
	 * @return String information about this servlet
	 */
	public String getServletInfo() {
		return "EWA(v2.0)";
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
