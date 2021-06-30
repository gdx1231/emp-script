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
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UUrl;

public class ServletRestful extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletRestful.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4725107647089996010L;

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

	public void outContent(HttpServletRequest request, HttpServletResponse response, String cnt)
			throws ServletException, IOException {
		GZipOut out = new GZipOut(request, response);
		out.outContent(cnt);
	}

	public String ewaRestfulHandler(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RestfulResult<Object> result = new RestfulResult<>();

		UUrl u = new UUrl(request);
		String path = u.getName();

		// 创建帮助文档
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
		if ("GET".equalsIgnoreCase(httpMethod)) {
			rv = new RequestValue(request);
		} else if ("POST".equalsIgnoreCase(httpMethod) && contentType != null
				&& contentType.toLowerCase().indexOf("multipart/form-data;") >= 0) {
			// 上传文件
			rv = new RequestValue(request);
			isUpload = true;

		} else {
			// true 表示body提交的json参数
			rv = new RequestValue(request, true);
		}

		ConfRestful conf = ConfRestfuls.getInstance().getConfRestful(path, httpMethod, rv, result);

		if (conf == null) {
			response.setContentType("application/json");
			response.setStatus(result.getHttpStatusCode());
			return result.toString();
		}

		if ("DOWNLOAD-INLINE".equalsIgnoreCase(rv.s("ewa_ajax")) || (conf.getParameters() != null
				&& conf.getParameters().toUpperCase().indexOf("EWA_AJAX=DOWNLOAD-INLINE") >= 0)) {
			// output the file bytes e.g. image, pdf
			isOutImage = true;
		} else if ("DOWNLOAD".equalsIgnoreCase(rv.s("ewa_ajax")) || (conf.getParameters() != null
				&& conf.getParameters().toUpperCase().indexOf("EWA_AJAX=DOWNLOAD") >= 0)) {
			// download the file
			isDownload = true;
		}

		if (isOutImage) {
			this.handleImage(conf, rv, response, result);
			if (result.isSuccess()) {
				return null;
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
		
		// 记录执行结束时间
		result.setEnd(System.currentTimeMillis());

		// CORS policy
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

	public void handleDownload(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = conf.getParameters();
		// force ewa_restful is yes
		rv.addOrUpdateValue("ewa_restful", "1");

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// request header authorization
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

		// The download saved name's filed name
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

	public void handleImage(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = conf.getParameters();
		// force ewa_restful is yes
		rv.addOrUpdateValue("ewa_restful", "1");

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// request header authorization
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
		String resize = rv.s("ewa_image_resize");
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
	 * 创建帮助文档
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private String ewaHelpDocuments(HttpServletRequest request, HttpServletResponse response) {
		/*
		 * RequestValue rv = new RequestValue(request); Map<String, Map<String, ConfRestful>> map =
		 * ConfRestfuls.getConfs(); JSONObject paths = new JSONObject(); map.forEach((key, v) -> { JSONObject path = new
		 * JSONObject(); v.forEach((method, conf) -> { String parameters = this.createEwaParameters(conf); parameters =
		 * parameters.replace("EWA_AJAX=", "aa=").replace("EWA_ACTION=", "a="); parameters +=
		 * "&ewa_ajax=json_ext&EWA_ACTION=xlsdfosd2389490234908239048239";
		 * 
		 * HtmlControl ht = new HtmlControl(); ht.setSkipAcl(true);
		 * 
		 * ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response); ht.getHtmlCreator().setAcl(null);
		 * String rst = ht.getHtml();
		 * 
		 * JSONObject obj = new JSONObject(rst); path.put("path", conf.getRestfulPath()); path.put(method, obj); });
		 * paths.put(path.getString("path"), path); }); return paths.toString();
		 */
		return "";
	}

	private String createEwaParameters(ConfRestful conf) {
		String parameters = conf.getParameters();
		if (StringUtils.isBlank(parameters)) {
			parameters = "EWA_RESTFUL=1";
		} else {
			parameters += "&EWA_RESTFUL=1";
		}

		if ("GET".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1) {
				parameters += "&EWA_AJAX=JSON_EXT";
			}
		} else if ("POST".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_ACTION=") == -1) {
				parameters += "&EWA_ACTION=OnPagePost";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1) {
				parameters += "&EWA_AJAX=JSON";
			}
			if (parameters.indexOf("EWA_MTYPE=") == -1) {
				parameters += "&EWA_MTYPE=N"; // 新增
			}
		} else if ("PUT".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_ACTION=") == -1) {
				parameters += "&EWA_ACTION=OnPagePost";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1) {
				parameters += "&EWA_AJAX=JSON";
			}
			if (parameters.indexOf("EWA_MTYPE=") == -1) {
				parameters += "&EWA_MTYPE=M"; // 修改
			}
		} else if ("PATCH".equals(conf.getMethod())) {
			if (parameters.indexOf("EWA_AJAX=") == -1) {
				parameters += "&EWA_AJAX=JSON";
			}
			// 默认恢复数据
			if (parameters.indexOf("EWA_ACTION=") == -1) {
				parameters += "&EWA_ACTION=OnFrameRestore";
			}
		} else if ("DELETE".equals(conf.getMethod())) {
			// 删除默认调用 OnFrameDelete
			if (parameters.indexOf("EWA_ACTION=") == -1) {
				parameters += "&EWA_ACTION=OnFrameDelete";
			}
			if (parameters.indexOf("EWA_AJAX=") == -1) {
				parameters += "&EWA_AJAX=JSON";
			}
		}

		return parameters;
	}

	public void initUploadParameters(ConfRestful conf, RequestValue rv, RestfulResult<Object> result) throws Exception {
		rv.addOrUpdateValue("xmlname", conf.getXmlName());
		rv.addOrUpdateValue("itemname", conf.getItemName());
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

		String params = this.createEwaParameters(conf);

		ht.init(conf.getXmlName(), conf.getItemName(), params, rv, null);

		// request header authorization
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

	private void handleConf(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {

		HtmlControl ht = new HtmlControl();

		String parameters = this.createEwaParameters(conf);

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// request header authorization
		if (!this.checkAcl(ht, result)) {
			return;
		}
		if (!this.checkHtRunError(ht, result)) {
			return;
		}

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

		if (ht.getLastTable().getCount() == 1) {
			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			result.setData(ht.getLastTable().getRow(0).toJson());
		} else if (ht.getLastTable().getCount() > 1) {
			result.setSuccess(true);
			result.setHttpStatusCode(HttpServletResponse.SC_OK);
			result.setData(ht.getLastTable().toJSONArray());
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
