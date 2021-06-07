package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfRestful;
import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameList;
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
		// true 表示body提交的json参数
		RequestValue rv = new RequestValue(request, true);

		RestfulResult<Object> result = new RestfulResult<>();

		String httpMethod = request.getMethod();
		httpMethod = httpMethod == null ? "" : httpMethod.toUpperCase().trim();

		UUrl u = new UUrl(request);
		String path = u.getName();

		Map<String, Map<String, ConfRestful>> map = ConfRestfuls.getConfs();

		ConfRestful conf = this.getConfRestful(map, path, httpMethod, rv, result);

		response.setContentType("application/json");
		if (conf != null) {
			this.handleConf(conf, rv, response, result);
			response.setStatus(result.getHttpStatusCode());
			return result.toString();
		} else {
			response.setStatus(result.getHttpStatusCode());
			return result.toString();
		}
	}

	private ConfRestful getConfRestful(Map<String, Map<String, ConfRestful>> map, String path, String httpMethod,
			RequestValue rv, RestfulResult<Object> result) {
		if (map.containsKey(path)) {
			Map<String, ConfRestful> mapMethod = map.get(path);
			if (mapMethod.containsKey(httpMethod)) {
				return mapMethod.get(httpMethod);
			}
			result.setMessage("not implemented");
			result.setHttpStatusCode(501);// Method is not implemented
			return null;
		}
		String[] requestPathsDepth = path.split("/");

		for (String restfulPath : map.keySet()) {
			String[] paths = restfulPath.split("/");
			boolean isMatched = this.findMapMethod(requestPathsDepth, paths);
			if (!isMatched) {
				continue;
			}

			// GET/POST/PUT/DELETE/PATCH
			Map<String, ConfRestful> mapMethod = map.get(restfulPath);
			if (mapMethod.containsKey(httpMethod)) {
				for (int i = 0; i < requestPathsDepth.length; i++) {
					String reqPath0 = requestPathsDepth[i];
					String path0 = paths[i];
					if (path0.startsWith("@")) {
						// Change path parameter to rv parameter
						String rvName = path0.substring(1);
						String rvValue = reqPath0;

						rv.addOrUpdateValue(rvName, rvValue);

					}
				}

				return mapMethod.get(httpMethod);
			} else {
				result.setMessage("not implemented");
				result.setHttpStatusCode(501);// Method is not implemented
				return null;
			}
		}
		result.setMessage("not found");
		result.setHttpStatusCode(404);
		return null;
	}

	private boolean findMapMethod(String[] requestPathsDepth, String[] paths) {

		if (paths.length != requestPathsDepth.length) {
			return false;
		}
		boolean isMatched = true;
		for (int i = 0; i < requestPathsDepth.length; i++) {
			String reqPath0 = requestPathsDepth[i];
			String path0 = paths[i];
			if (!reqPath0.equals(path0) && !path0.startsWith("@")) {
				isMatched = false;
				break;
			}
		}

		return isMatched;
	}

	private void handleConf(ConfRestful conf, RequestValue rv, HttpServletResponse response,
			RestfulResult<Object> result) {
		HtmlControl ht = new HtmlControl();
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
			if (parameters.indexOf("EWA_MTYPE=") == -1) {
				parameters += "&EWA_MTYPE=M"; // 修改
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

		ht.init(conf.getXmlName(), conf.getItemName(), parameters, rv, response);

		// request header authorization
		if (!ht.getHtmlCreator().checkAcl()) {
			result.setHttpStatusCode(401);
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

			}
			return;
		}

		if (ht.isError()) {
			result.setHttpStatusCode(500);
			result.setSuccess(false);
			result.setMessage(ht.getHtmlCreator().getDataConn().getErrorMsgOnly());
			return;
		}

		if ("GET".equals(conf.getMethod()) && conf.getPath().endsWith("s")) {
			if (ht.getLastTable() == null) {
				result.setSuccess(false);
				result.setHttpStatusCode(404); // not found
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
			result.setHttpStatusCode(200);
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

		if (ht.getLastTable() == null) {
			result.setSuccess(true);
			result.setHttpStatusCode(204); // no content
			return;
		}

		if (ht.getLastTable().getCount() == 1) {
			result.setSuccess(true);
			result.setHttpStatusCode(200);
			result.setData(ht.getLastTable().getRow(0).toJson());
		} else if (ht.getLastTable().getCount() > 1) {
			result.setSuccess(true);
			result.setHttpStatusCode(200);
			result.setData(ht.getLastTable().toJSONArray());
		}

	}
}
