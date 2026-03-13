package com.gdxsoft.easyweb.script.servlets;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfRestful;
import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameList;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UPath;

/**
 * ServletRestful
 * <p>
 * 提供基于配置的 RESTful API 入口，统一处理 GET/POST/PUT/PATCH/DELETE 等方法。
 * 具体的行为由 ConfRestful 配置文件定义，并通过 HtmlControl 执行相应的操作。
 * </p>
 * 主要职责：
 * - 解析请求（包括 multipart 上传）
 * - 根据请求路径和方法查找 ConfRestful
 * - 根据 ConfRestful 执行对应逻辑（上传/下载/图片/数据操作）
 * - 封装统一的 JSON 响应格式（RestfulResult）并设置 HTTP 状态码
 */
public class ServletRestful extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletRestful.class);
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 4725107647089996010L;

	/**
	 * 重写 service 方法，统一捕获异常并返回 500 错误
	 * 1. 调用 ewaRestfulHandler 处理请求
	 * 2. 使用 outContent 输出结果（支持 GZip）
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String resultContent = this.ewaRestfulHandler(req, resp);
			this.outContent(req, resp, resultContent);
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
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
	 * 核心处理函数：根据请求路径和方法处理 RESTful 请求
	 * 返回值说明：
	 * - 返回 null：表示已经直接通过 response 输出（例如二进制文件），无需再写入字符串
	 * - 返回 JSON 字符串：由调用者通过 outContent 输出
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

		// 特殊接口：生成帮助文档（JSON）
		if (path.endsWith("/ewa-help-documents")) {
			response.setContentType("application/json");
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
			response.setContentType("application/json");
			response.setStatus(result.getHttpStatusCode());
			return result.toString();
		}

		// 判断是否为下载/内联下载（图片/文件）
		if ("DOWNLOAD-INLINE".equalsIgnoreCase(rv.s(FrameParameters.EWA_AJAX)) || (conf.getParameters() != null
				&& conf.getParameters().toUpperCase().indexOf("EWA_AJAX=DOWNLOAD-INLINE") >= 0)) {
			// 以字节输出，例如图片、PDF
			isOutImage = true;
		} else if ("DOWNLOAD".equalsIgnoreCase(rv.s(FrameParameters.EWA_AJAX)) || (conf.getParameters() != null
				&& conf.getParameters().toUpperCase().indexOf("EWA_AJAX=DOWNLOAD") >= 0)) {
			// 直接下载
			isDownload = true;
		}

		// 根据前面判断调用相应的处理器
		if (isOutImage) {
			this.handleImage(conf, rv, response, result);
			if (result.isSuccess()) {
				return null; // 已直接写入 response（文件字节），返回 null 表示无需再次输出 JSON
			} else {
				return result.toString();
			}
		} else if (isDownload) {
			this.handleDownload(conf, rv, response, result);
		} else if (isUpload) {
			response.setContentType("application/json");
			this.handleUpload(conf, rv, request, result);
		} else {
			response.setContentType("application/json");
			this.handleConf(conf, rv, response, result);
		}
		
		// 记录执行结束时间（用于性能统计）
		result.setEnd(System.currentTimeMillis());

		// CORS 策略：从 ConfRestfuls 中获取，若为 '*' 且存在 Origin header，则允许该 Origin
		String cors = ConfRestfuls.getInstance().getCors();
		if (StringUtils.isNotBlank(cors)) {
			if (cors.equals("*") && StringUtils.isNotBlank(request.getHeader("origin"))) {
				cors = request.getHeader("origin");
			}
			response.setHeader("Access-Control-Allow-Origin", cors);
		}
		response.setStatus(result.getHttpStatusCode());
		
		return result.toString();

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
	 * 处理文件下载请求（Content-Disposition: attachment）
	 * 通过 HtmlControl 获取文件路径并将文件写入 response
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
			File imgSize = FileOut.getImageResizedFile(image, resize);
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
		fo.outFileBytesInline(true, oneWeek);

	}

	/**
	 * 生成帮助文档（目前为空实现，保留接口）
	 */
	private String ewaHelpDocuments(HttpServletRequest request, HttpServletResponse response) {
		/*
		 * 该方法原本准备遍历 ConfRestfuls 中的配置并生成文档，这里保留接口，具体实现注释掉。
		 */
		return "";
	}

	/**
	 * 生成默认的 EWA 参数集合，用于在调用 HtmlControl 前补充必要参数
	 * 根据不同 HTTP 方法，追加不同的 EWA 参数（例如 EWA_AJAX、EWA_ACTION、EWA_MTYPE）
	 */
	private String createEwaParameters(ConfRestful conf, RequestValue rv) {
		String parameters = conf.getParameters();
		if (StringUtils.isBlank(parameters)) {
			parameters = "EWA_RESTFUL=1";
		} else {
			parameters += "&EWA_RESTFUL=1";
		}

		if ("GET".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1  && rv.isBlank("EWA_AJAX")) {
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
			if (parameters.indexOf("EWA_AJAX=") == -1  && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
			if (parameters.indexOf("EWA_MTYPE=") == -1 && rv.isBlank("EWA_MTYPE")) {
				parameters += "&EWA_MTYPE=M"; // 修改
			}
		} else if ("PATCH".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1  && rv.isBlank("EWA_AJAX")) {
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
			if (parameters.indexOf("EWA_AJAX=") == -1  && rv.isBlank("EWA_AJAX")) {
				parameters += "&EWA_AJAX=JSON";
			}
		}

		return parameters;
	}

	/**
	 * 初始化上传所需的参数并进行 ACL 校验
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

		HtmlControl ht = new HtmlControl();

		String params = this.createEwaParameters(conf, rv);

		ht.init(conf.getXmlName(), conf.getItemName(), params, rv, null);

		// 请求头授权校验
		if (!ht.getHtmlCreator().checkAcl()) {
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
			return;
		}

	}

	/**
	 * 处理上传逻辑：解析 multipart 请求并调用 Upload 组件完成文件保存
	 */
	public void handleUpload(ConfRestful conf, RequestValue rv, HttpServletRequest request,
			RestfulResult<Object> result) {
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
		Upload up = new Upload();
		up.setRv(rv);
		try {
			up.init(rv.getRequest());
		} catch (Exception e1) {
			result.setSuccess(false);
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setData(e1.getMessage());
			LOGGER.error(e1.getMessage());
			return;
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存缓冲区，超过后写入临时文件
		int bufferSize = 1024 * 1024 * 10; // 10M

		factory.setSizeThreshold(bufferSize);// 设置缓冲区大小，这里是10M
		// 设置临时文件存储位置
		File tempPath = new File(UPath.getPATH_UPLOAD() + "/" + Upload.DEFAULT_UPLOAD_PATH);
		factory.setRepository(tempPath);
		ServletFileUpload upload = new ServletFileUpload(factory);

		long maxSize = 1024 * 1024 * 1024 * 2; // 2g
		upload.setSizeMax(maxSize);

		List<?> items = null;
		try {
			items = upload.parseRequest(rv.getRequest());
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);
				if (item.isFormField()) {
					rv.addValue(item.getFieldName(), item.getString());
				} else {
					// 文件字段，Upload 组件稍后处理
				}
			}
		} catch (Exception err) {
			result.setSuccess(false);
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setData(err.getMessage());
			LOGGER.error(err.getMessage());
			return;
		}

		up.setUploadItems(items);

		try {
			String s = up.upload();
			result.setSuccess(true);
			result.setCode(200);
			result.setHttpStatusCode(200);
			result.setData(new JSONArray(s));
		} catch (Exception err) {
			result.setSuccess(false);
			result.setCode(500);
			result.setHttpStatusCode(500);
			result.setData(err.getMessage());
			LOGGER.error(err.getMessage());
		}
	}

	/**
	 * 处理基于 ConfRestful 的常规数据操作（包括 GET/POST/PUT/PATCH/DELETE）
	 * 根据 HtmlControl 执行后把结果封装进 RestfulResult 并设置 HTTP 状态码
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
		if ("GET".equals(conf.getMethod()) && conf.getPath().endsWith("s")) {
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
			}
			JSONObject data = new JSONObject(ht.getHtml());

			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			result.setData(data.optJSONArray("DATA"));
			
			return;
		}

		// 创建资源（POST）返回 201
		if ("POST".equals(conf.getMethod())) {
			result.setSuccess(true);
			result.setHttpStatusCode(201); // created
			if (ht.getLastTable() != null && ht.getLastTable().getCount() > 0) {
				result.setData(ht.getLastTable().getRow(0).toJson());
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