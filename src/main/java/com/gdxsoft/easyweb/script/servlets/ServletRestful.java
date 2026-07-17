package com.gdxsoft.easyweb.script.servlets;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfRestful;
import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameFrame;
import com.gdxsoft.easyweb.script.display.frame.FrameList;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.restful.RestfulResult;
import com.gdxsoft.easyweb.script.restful.ApiDocumentation;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.FileUpload;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.uploader.UploadUtils;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.acl.IAcl;

/**
 * ServletRestful
 * <p>
 * 提供基于配置的 RESTful API 入口，统一处理 GET/POST/PUT/PATCH/DELETE 等方法。 具体的行为由 ConfRestful
 * 配置文件定义，并通过 HtmlControl 执行相应的操作。
 * </p>
 * 主要职责： - 解析请求（包括 multipart 上传） - 根据请求路径和方法查找 ConfRestful - 根据 ConfRestful
 * 执行对应逻辑（上传/下载/图片/数据操作） - 封装统一的 JSON 响应格式（RestfulResult）并设置 HTTP 状态码
 */
public class ServletRestful extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletRestful.class);
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 4725107647089996010L;

	/**
	 * 重写 service 方法，统一捕获异常并返回 500 错误 1. 调用 ewaRestfulHandler 处理请求 2. 使用 outContent
	 * 输出结果（支持 GZip）
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String resultContent = this.ewaRestfulHandler(req, resp);
			if (resultContent == null) { // download
				return;
			}
			this.outContent(req, resp, resultContent);
		} catch (Exception err) {
			// 带 throwable 才能完整保留堆栈与 cause chain
			LOGGER.error("Restful service error, uri={}", req.getRequestURI(), err);
			try {
				if (!resp.isCommitted()) {
					resp.reset();
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					resp.setContentType("application/json; charset=utf-8");
					// 用 RestfulResult 包装错误体，与正常路径响应结构保持一致
					RestfulResult<Object> errResult = new RestfulResult<>();
					errResult.setSuccess(false);
					errResult.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					errResult.setHttpStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					errResult.setMessage("Inner error");
					errResult.setData(errResult.toJson());
					this.outContent(req, resp, errResult.toString());
					return;
				}
			} catch (Exception inner) {
				LOGGER.error("Restful service error while writing inner-error response", inner);
			}
			// 兜底：若 response 已提交或二次 reset 失败，至少保证状态码与错误文本
			resp.setStatus(500);
			this.outContent(req, resp, "Inner error");
		}
	}

	/**
	 * 输出内容到 response，封装 GZip 支持
	 */
	public void outContent(HttpServletRequest request, HttpServletResponse response, String cnt)
			throws ServletException, IOException {
		GZipOut out = new GZipOut(request, response);
		out.outContent(cnt);
	}

	/**
	 * 核心处理函数：根据请求路径和方法处理 RESTful 请求 返回值说明： - 返回 null：表示已经直接通过 response
	 * 输出（例如二进制文件），无需再写入字符串 - 返回 JSON 字符串：由调用者通过 outContent 输出
	 */
	public String ewaRestfulHandler(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RestfulResult<Object> result = new RestfulResult<>();

		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String path = uri.substring(ctx.length());

		LOGGER.info("Restful request: {}, {}", request.getMethod(), path);
//		UUrl u = new UUrl(request);
//		String path = u.getName();

		// 特殊接口：生成帮助文档（HTML/JSON）
		if (path.endsWith("/ewa-help-documents")) {
			return this.ewaHelpDocuments(request, response);
		}

		String httpMethod = request.getMethod();
		httpMethod = httpMethod == null ? "" : httpMethod.toUpperCase().trim();
		String contentType = request.getContentType();

		RequestValue rv;
		boolean isUpload = false;
		boolean isOutImage = false;
		boolean isDownload = false;
		// 根据请求方法和 Content-Type 选择参数解析方式
		if ("GET".equalsIgnoreCase(httpMethod)) {
			rv = new RequestValue(request);
		} else if ("POST".equalsIgnoreCase(httpMethod) && contentType != null
				&& contentType.toLowerCase().indexOf("multipart/form-data;") >= 0) {
			// multipart/form-data 上传
			rv = new RequestValue(request);
			isUpload = true;

		} else {
			// 其他情况：将请求体解析为 JSON 参数
			rv = new RequestValue(request, true);
		}

		// 根据路径和方法查找配置对象 ConfRestful
		ConfRestful conf = ConfRestfuls.getInstance().getConfRestful(path, httpMethod, rv, result);

		if (conf == null) {
			// 未找到配置时，返回 JSON 错误信息并设置相应 HTTP 状态码
			response.setContentType("application/json; charset=utf-8");
			this.applyResponseHeaders(request, response, result);
			return result.toString();
		}

		// 判断是否为下载/内联下载（图片/文件）。用 &-split 解析 KV，避免 indexOf
		// 子串误判（如"EWA_AJAX=DOWNLOAD-OTHER"被误判为"DOWNLOAD-INLINE"）
		String ewaAjax = rv.s(FrameParameters.EWA_AJAX);
		String confEwaAjax = lookupConfParameter(conf.getParameters(), "EWA_AJAX");
		if ("DOWNLOAD-INLINE".equalsIgnoreCase(ewaAjax) || "DOWNLOAD-INLINE".equalsIgnoreCase(confEwaAjax)) {
			// 以字节输出，例如图片、PDF
			isOutImage = true;
		} else if ("DOWNLOAD".equalsIgnoreCase(ewaAjax) || "DOWNLOAD".equalsIgnoreCase(confEwaAjax)) {
			// 直接下载二进制附件
			isDownload = true;
		}

		// 根据前面判断调用相应的处理器
		if (isOutImage) {
			// Image: handleImage 通过 FileOut 直接写字节流
			// 先预设 200 状态码和 CORS，因为写字节后 response 可能立即 committed
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			this.applyResponseHeaders(request, response, result);
			this.handleImage(conf, rv, response, result);
			if (result.isSuccess()) {
				return null;
			}
			// 失败时若 response 已提交或输出流已使用，不能再写 JSON
			if (response.isCommitted()) {
				return null;
			}
			return result.toString();
		} else if (isDownload) {
			// 下载走二进制 attachment，先预设 200 和 CORS
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			this.applyResponseHeaders(request, response, result);
			this.handleDownload(conf, rv, response, result);
			if (result.isSuccess()) {
				return null; // FileOut 已写出附件字节流，避免再次写入 JSON
			}
			// 失败时若 response 已提交或输出流已使用，不能再写 JSON
			if (response.isCommitted()) {
				return null;
			}
			// 失败回退为 JSON 错误体
			response.setContentType("application/json; charset=utf-8");
		} else if (isUpload) {
			// 仅当配置支持上传（UserConfig 中存在 h5upload 项）才真正处理上传，否则降级为 handleConf
			if (this.isUploadSupported(conf)) {
				response.setContentType("application/json; charset=utf-8");
				this.handleUpload(conf, rv, request, response, result);
			} else {
				// 配置不支持上传，按普通 conf 处理
				response.setContentType("application/json; charset=utf-8");
				this.handleConf(conf, rv, response, result);
			}
		} else {
			response.setContentType("application/json; charset=utf-8");
			this.handleConf(conf, rv, response, result);
		}

		// 记录执行结束时间（用于性能统计）
		result.setEnd(System.currentTimeMillis());

		// 统一应用 CORS 与状态码
		this.applyResponseHeaders(request, response, result);

		return result.toString();

	}

	/**
	 * 根据 Result 设置 HTTP 状态码及响应头（包括 CORS）。 供各分支处理器复用，避免遗漏 CORS 等公共响应头。
	 * <p>
	 * 若 httpStatusCode 为 null，回退到 500；同样的，CORS 缺省时不设置。
	 * </p>
	 */
	private void applyResponseHeaders(HttpServletRequest request, HttpServletResponse response,
			RestfulResult<Object> result) {
		String cors = ConfRestfuls.getInstance().getCors();
		if (StringUtils.isNotBlank(cors)) {
			if ("*".equals(cors) && StringUtils.isNotBlank(request.getHeader("origin"))) {
				cors = request.getHeader("origin");
			}
			response.setHeader("Access-Control-Allow-Origin", cors);
		}
		Integer status = result.getHttpStatusCode();
		if (status == null) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		response.setStatus(status);
	}

	/**
	 * 在 conf.parameters 字符串中按 KV 精确匹配指定 key 的 value（大小写不敏感）。 替代原先的
	 * {@code indexOf("EWA_AJAX=DOWNLOAD")}
	 * 形式，避免"EWA_AJAX=DOWNLOAD-OTHER"被误判为"DOWNLOAD"。
	 *
	 * @param parameters conf.parameters 字符串（如 "EWA_RESTFUL=1&EWA_AJAX=DOWNLOAD"）
	 * @param key        要查询的键名
	 * @return key 对应的 value；不存在返回 null
	 */
	private static String lookupConfParameter(String parameters, String key) {
		if (StringUtils.isBlank(parameters)) {
			return null;
		}
		String target = key.toUpperCase();
		String[] parts = parameters.split("&");
		for (String p : parts) {
			if (StringUtils.isBlank(p)) {
				continue;
			}
			int eq = p.indexOf('=');
			if (eq <= 0) {
				continue;
			}
			String k = p.substring(0, eq).toUpperCase();
			if (!target.equals(k)) {
				continue;
			}
			String v = p.substring(eq + 1);
			return v;
		}
		return null;
	}

	/**
	 * 检查 conf 对应的 XML/ITEM 配置是否声明了 h5upload 项。
	 * <p>
	 * 用于 B11：避免把普通的 multipart POST 误当成上传请求处理。仅在确实声明了上传项时才进入 upload 流程。
	 * </p>
	 */
	private boolean isUploadSupported(ConfRestful conf) {
		try {
			UserConfig uc = UserConfig.instance(conf.getXmlName(), conf.getItemName(), null);
			UserXItems items = uc.getUserXItems();
			if (items == null) {
				return false;
			}
			for (int i = 0; i < items.count(); i++) {
				UserXItem item = items.getItem(i);
				String tag = item.getSingleValue("Tag");
				if ("h5upload".equals(tag)) {
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("isUploadSupported load UserConfig error: xml={}, item={}, err={}", conf.getXmlName(),
					conf.getItemName(), e.getMessage());
		}
		return false;
	}

	/**
	 * 检查访问控制列表（ACL）。如果未通过则设置 result 为 401 并返回 false
	 */
	public boolean checkAcl(HtmlControl ht, RestfulResult<Object> result) {
		if (ht.getHtmlCreator().checkAcl()) {
			return true;
		}

		result.setHttpStatusCode(HttpServletResponse.SC_UNAUTHORIZED); // 401
		result.setSuccess(false);
		try {
			JSONObject msg = new JSONObject(ht.getHtmlCreator().getAcl().getDenyMessage());
			if (msg.has("code")) {
				result.setCode(msg.optInt("code"));
			}
			if (msg.has("message")) {
				result.setMessage(msg.optString("message"));
			}
		} catch (Exception err) {
			LOGGER.warn(err.getMessage());
		}
		return false;
	}

	/**
	 * 检查 HtmlControl 执行过程中是否发生错误（系统错误、异常或 SQL 错误），并设置合适的 HTTP 状态码
	 */
	public boolean checkHtRunError(HtmlControl ht, RestfulResult<Object> result) {
		// 系统执行出现 err_out
		if (ht.getHtmlCreator().isErrOut()) {
			result.setHttpStatusCode(HttpServletResponse.SC_FORBIDDEN); // 403
			result.setSuccess(false);
			result.setMessage(ht.getHtmlCreator().getErrOutMessage());
			return false;
		}

		// exception
		if (ht.isError()) {
			result.setHttpStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
			result.setSuccess(false);
			result.setMessage(ht.getHtmlCreator().getDataConn().getErrorMsgOnly());
			return false;
		}

		// sql error
		if (ht.getHtmlCreator().getAction().getChkErrorMsg() != null) {
			result.setHttpStatusCode(HttpServletResponse.SC_BAD_REQUEST); // 400
			result.setSuccess(false);
			result.setMessage(ht.getHtmlCreator().getAction().getChkErrorMsg());
			return false;
		}
		return true;
	}

	/**
	 * 处理文件下载请求（Content-Disposition: attachment） 通过 HtmlControl 获取文件路径并将文件写入
	 * response
	 */
	public void handleDownload(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = conf.getParameters();
		// 强制设置为 RESTful 模式
		rv.addOrUpdateValue("ewa_restful", "1");

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// 请求头授权校验
		if (!this.checkAcl(ht, result)) {
			return;
		}

		if (!this.checkHtRunError(ht, result)) {
			return;
		}

		String fileStr = ht.getHtml();
		if (fileStr == null) {
			result.setHttpStatusCode(404);
			result.setSuccess(false);
			return;
		}

		File file = new File(fileStr);

		if (!file.exists()) {
			result.setHttpStatusCode(404);
			result.setSuccess(false);
			return;
		}

		// 下载保存名称的字段名
		String downloadNameField = rv.s("EWA_DOWNLOAD_NAME");
		String downloadFile = null;
		if (StringUtils.isNotBlank(downloadNameField)) {
			String name = ht.getHtmlCreator().getValueFromFrameTables(downloadNameField);
			if (StringUtils.isBlank(name)) {
				downloadFile = "invalid_parameter";
			} else {
				downloadFile = name;
			}
		}

		FileOut fo = new FileOut(rv.getRequest(), response);
		fo.initFile(file);

		fo.download(downloadFile);

		result.setSuccess(true);
		result.setHttpStatusCode(200);

	}

	/**
	 * 处理图片及内联展示（Content-Disposition inline），支持 resize 参数
	 */
	public void handleImage(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = conf.getParameters();
		// 强制设置为 RESTful 模式
		rv.addOrUpdateValue("ewa_restful", "1");

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// 请求头授权校验
		if (!this.checkAcl(ht, result)) {
			return;
		}

		if (!this.checkHtRunError(ht, result)) {
			return;
		}

		String fileStr = ht.getHtml();
		if (fileStr == null) {
			result.setHttpStatusCode(404);
			result.setSuccess(false);
			return;
		}

		File image = new File(fileStr);

		if (!image.exists()) {
			result.setHttpStatusCode(404);
			result.setSuccess(false);
			LOGGER.warn("The download file not found: {}, root: {}", fileStr, UPath.getPATH_UPLOAD());
			return;
		}
		String resize = rv.s(FrameParameters.EWA_IMAGE_RESIZE);
		if (StringUtils.isNotBlank(resize)) {
			String accept = rv.getRequest().getHeader("accept"); // 获取请求头中的accept字段
			File imgSize = FileOut.getOrCreateImageResizedFile(image, resize, true, accept);
			if (imgSize != null && imgSize.exists()) {
				image = imgSize;
			} else {
				result.setHttpStatusCode(404);
				result.setSuccess(false);

				LOGGER.warn("The download file not found: {}, resize: {}, root: {}", fileStr, resize,
						UPath.getPATH_UPLOAD());
				return;
			}
		}

		FileOut fo = new FileOut(rv.getRequest(), response);
		fo.initFile(image);

		long oneWeek = 604800L; // seconds
		long len = fo.outFileBytesInline(true, oneWeek);
		if (len >= 0) {
			result.setSuccess(true);
			result.setHttpStatusCode(200);
		} else {
			result.setSuccess(false);
			result.setHttpStatusCode(500);
		}

	}

	/**
	 * 生成帮助文档（API自我描述），支持 HTML/JSON 双格式输出 优先级：query 参数 format > Accept 头 > 默认 HTML
	 */
	private String ewaHelpDocuments(HttpServletRequest request, HttpServletResponse response) {
		boolean wantJson = false;

		// 1. query 参数优先
		String format = request.getParameter("format");
		if ("json".equalsIgnoreCase(format)) {
			wantJson = true;
		} else if ("html".equalsIgnoreCase(format)) {
			wantJson = false;
		} else {
			// 2. Accept 头
			String accept = request.getHeader("Accept");
			if (accept != null) {
				boolean hasJson = accept.contains("application/json");
				boolean hasHtml = accept.contains("text/html");
				if (hasJson && !hasHtml) {
					wantJson = true;
				}
			}
			// 3. 默认 HTML（浏览器访问）
		}

		if (wantJson) {
			response.setContentType("application/json; charset=utf-8");
			JSONObject result = ApiDocumentation.getApiDocumentation();
			return result.toString();
		} else {
			response.setContentType("text/html; charset=utf-8");
			return ApiDocumentation.getApiDocumentationHtml();
		}
	}

	/**
	 * 生成默认的 EWA 参数集合，用于在调用 HtmlControl 前补充必要参数 根据不同 HTTP 方法，追加不同的 EWA 参数（例如
	 * EWA_AJAX、EWA_ACTION、EWA_MTYPE）
	 */
	private String createEwaParameters(ConfRestful conf, RequestValue rv) {
		String parameters = conf.getParameters();
		if (StringUtils.isBlank(parameters)) {
			parameters = "EWA_RESTFUL=1";
		} else {
			parameters += "&EWA_RESTFUL=1";
		}

		if ("GET".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1 && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON_EXT";
			}
		} else if ("POST".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_ACTION=") == -1 && rv.isBlank("EWA_ACTION")) {
				parameters += "&EWA_ACTION=OnPagePost";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1 && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
			if (parameters.indexOf("EWA_MTYPE=") == -1 && rv.isBlank("EWA_MTYPE")) {
				parameters += "&EWA_MTYPE=N"; // 新增
			}
		} else if ("PUT".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_ACTION=") == -1 && rv.isBlank("EWA_ACTION")) {
				parameters += "&EWA_ACTION=OnPagePost";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1 && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
			if (parameters.indexOf("EWA_MTYPE=") == -1 && rv.isBlank("EWA_MTYPE")) {
				parameters += "&EWA_MTYPE=M"; // 修改
			}
		} else if ("PATCH".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1 && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
			// 默认恢复数据
			if (parameters.indexOf("EWA_ACTION=") == -1 && rv.isBlank("EWA_ACTION")) {
				parameters += "&EWA_ACTION=OnFrameRestore";
			}
		} else if ("DELETE".equals(conf.getMethod())) {
			// 删除默认调用 OnFrameDelete
			if (parameters.indexOf("EWA_ACTION=") == -1 && rv.isBlank("EWA_ACTION")) {
				parameters += "&EWA_ACTION=OnFrameDelete";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1 && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
		}

		return parameters;
	}

	/**
	 * 初始化上传所需的参数并进行 ACL 校验。 直接从 UserConfig 加载 ACL 类进行轻量校验，不创建 HtmlControl， 避免与后续
	 * handleConf 重复执行重量级的 HtmlCreator.init()。
	 */
	public void initUploadParameters(ConfRestful conf, RequestValue rv, RestfulResult<Object> result) throws Exception {
		rv.addOrUpdateValue(FrameParameters.XMLNAME, conf.getXmlName());
		rv.addOrUpdateValue(FrameParameters.ITEMNAME, conf.getItemName());
		String uploadName = null;
		UserConfig uc = UserConfig.instance(conf.getXmlName(), conf.getItemName(), null);
		UserXItems items = uc.getUserXItems();
		for (int i = 0; i < items.count(); i++) {
			UserXItem item = items.getItem(i);
			String tag = item.getSingleValue("Tag");
			if ("h5upload".equals(tag)) {
				uploadName = item.getName();
			}
		}
		rv.addOrUpdateValue("name", uploadName);

		// 轻量 ACL 校验：直接从 UserConfig 加载 ACL 类，不创建 HtmlControl
		if (!uc.getUserPageItem().testName("Acl") || uc.getUserPageItem().getItem("Acl").count() == 0) {
			return;
		}
		String aclExp = uc.getUserPageItem().getItem("Acl").getItem(0).getItem("Acl").trim();
		if (aclExp.length() <= 5) {
			return;
		}

		UObjectValue ov = new UObjectValue();
		Object o = ov.loadClass(aclExp, null);
		if (!(o instanceof IAcl)) {
			return;
		}
		IAcl acl = (IAcl) o;
		acl.setXmlName(conf.getXmlName());
		acl.setItemName(conf.getItemName());
		acl.setRequestValue(rv);

		if (acl.canRun()) {
			return;
		}

		// ACL 校验未通过
		result.setHttpStatusCode(HttpServletResponse.SC_UNAUTHORIZED); // 401
		result.setSuccess(false);
		try {
			JSONObject msg = new JSONObject(acl.getDenyMessage());
			if (msg.has("code")) {
				result.setCode(msg.optInt("code"));
			}
			if (msg.has("message")) {
				result.setMessage(msg.optString("message"));
			}
		} catch (Exception err) {
			LOGGER.warn(err.getMessage());
		}
	}

	/**
	 * 处理上传逻辑：解析 multipart 请求并调用 Upload 组件完成文件保存
	 */
	public void handleUpload(ConfRestful conf, RequestValue rv, HttpServletRequest request,
			HttpServletResponse response, RestfulResult<Object> result) {
		try {
			this.initUploadParameters(conf, rv, result);
		} catch (Exception e2) {
			result.setSuccess(false);
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setData(e2.getMessage());
			LOGGER.error(e2.getMessage());
			return;
		}

		try {
			Upload up = UploadUtils.parseAndUpload(request, rv);

			// 将上传文件元数据映射到 RequestValue（参照 ActionFrame.createUploadPara）
			mapUploadParametersToRv(conf, up, rv);

			// 执行 Item 的 Action SQL（如 INSERT/UPDATE），
			// handleConf 会设置 result 的 success/code/data 等字段
			this.handleConf(conf, rv, response, result);

		} catch (Exception err) {
			result.setSuccess(false);
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setData(err.getMessage());
			LOGGER.error(err.getMessage());
		}
	}

	/**
	 * 将上传文件元数据映射到 RequestValue，使 Item Action SQL 可引用 @字段名, @字段名_EXT 等参数。 参照
	 * ActionFrame.createUploadPara() 的逻辑。
	 *
	 * @param conf RESTful 配置
	 * @param up   已完成 upload() 的 Upload 实例
	 * @param rv   请求参数对象
	 */
	public static void mapUploadParametersToRv(ConfRestful conf, Upload up, RequestValue rv) throws Exception {
		List<FileUpload> alFiles = up.getAlFiles();
		if (alFiles == null || alFiles.isEmpty()) {
			return;
		}

		UserConfig uc = UserConfig.instance(conf.getXmlName(), conf.getItemName(), null);
		UserXItems items = uc.getUserXItems();

		for (int i = 0; i < items.count(); i++) {
			UserXItem item = items.getItem(i);
			String tag = item.getSingleValue("Tag");
			if (!("h5upload".equals(tag) || "image".equals(tag) || "swffile".equals(tag))) {
				continue;
			}
			if (!item.testName("Upload")) {
				continue;
			}

			String uploadName = item.getName();
			String dataType = item.getSingleValue("DataItem", "DataType");

			UploadUtils.createUploadPara(uploadName, dataType, alFiles, up.getUploadDir(), rv);
		}
	}

	/**
	 * 处理基于 ConfRestful 的常规数据操作（包括 GET/POST/PUT/PATCH/DELETE） 根据 HtmlControl
	 * 执行后把结果封装进 RestfulResult 并设置 HTTP 状态码
	 */
	private void handleConf(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = this.createEwaParameters(conf, rv);

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// 请求头授权校验
		if (!this.checkAcl(ht, result)) {
			return;
		}
		if (!this.checkHtRunError(ht, result)) {
			return;
		}

		// 列表查询（路径以 s 结尾表示集合资源）
		if ("GET".equals(conf.getMethod())) {
			if (ht.getLastTable() == null) {
				result.setSuccess(false);
				result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND); // not found
				return;
			}
			if (ht.getHtmlCreator().getFrame() instanceof FrameList) {
				FrameList f = (FrameList) ht.getHtmlCreator().getFrame();
				f.createJsonPageInfo();
				// 分页信息
				if (ht.getPageSplit() != null) {
					result.setEwaPageCur(ht.getPageSplit().getPageCurrent());
					result.setEwaPageSize(ht.getPageSplit().getPageSize());
					result.setPageCount(ht.getPageSplit().getPageCount());
					result.setRecordCount(ht.getPageSplit().getRecordCount());
				}
				JSONObject data = new JSONObject(ht.getHtml());
				result.setData(data.optJSONArray("DATA"));
			} else if (ht.getHtmlCreator().getFrame() instanceof FrameFrame) {
				FrameFrame f = (FrameFrame) ht.getHtmlCreator().getFrame();
				try {
					f.createJsonContent(false);
				} catch (Exception e) {
					LOGGER.error("frameFrame {}", e);
					result.setSuccess(false);
					result.setHttpStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					result.setMessage(e.getLocalizedMessage());
					return;
				}
			}

			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_OK);

			return;
		}

		// 创建资源（POST）返回 201
		if ("POST".equals(conf.getMethod())) {
			result.setSuccess(true);
			result.setHttpStatusCode(201); // created
			if (ht.getLastTable() != null) {
				// 返回创建的资源集合
				result.setData(ht.getLastTable().toJSONArray());
			}
			return;
		}
		// 没有任何数据
		if (ht.getLastTable() == null) {
			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_NO_CONTENT); // 204
			return;
		}

		if (ht.getLastTable().getCount() >= 1) {
			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			String rst = ht.getHtml().trim();
			if (rst.startsWith("{") && rst.endsWith("}")) {
				result.setData(new JSONObject(rst));
			} else if (rst.startsWith("[") && rst.endsWith("]")) {
				result.setData(new JSONArray(rst));
			} else {
				result.setData(rst);
			}
		} else {// no data
			if ("GET".equals(conf.getMethod())) {
				result.setSuccess(false);
				result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND); // 404
			} else {
				// no data
				result.setSuccess(true);
				result.setHttpStatusCode(HttpServletResponse.SC_NO_CONTENT); // 204
			}
		}

	}
}