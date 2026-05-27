package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfAdmin;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.define.UpdateCfgScriptSetData;
import com.gdxsoft.easyweb.define.UpdateCfgSqlSetData;
import com.gdxsoft.easyweb.define.UpdateCfgXItemData;
import com.gdxsoft.easyweb.define.UpdateCfgXml;
import com.gdxsoft.easyweb.define.servlets.ApiTokenValidator.ValidationResult;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * XML 配置管理工具 — Servlet 入口
 *
 * 仅负责 HTTP 请求解析和响应，XML 操作全部委托给 UpdateCfgXml。
 * 所有写操作（save/delete/move）需通过 ApiTokenValidator 验证管理员权限。
 * 读操作（list/get）同样需要验证，防止未授权访问配置信息。
 *
 * @author guolei
 * @date 2026-05-26
 */
public class UpdateCfgAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCfgAction.class);

	/**
	 * 无需验证的方法（目前无 — 所有操作都涉及配置数据）
	 */
	private static final String[] WHITELIST_METHODS = {};

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");

		PrintWriter out = response.getWriter();

		// 1. 检查是否允许 define 操作
		if (!ConfDefine.isAllowDefine()) {
			out.print(authError("define 功能未启用，请在配置中设置 allowDefine=true", 403));
			response.setStatus(403);
			return;
		}

		RequestValue rv = new RequestValue(request);
		String method = rv.getString("method");

		if (method == null) {
			out.print(new JSONObject().put("success", false).put("message", "缺少 method 参数").toString(2));
			return;
		}

		// 2. 权限验证（白名单方法除外）
		if (!isWhitelisted(method)) {
			ValidationResult authResult = ApiTokenValidator.validate(request);
			if (!authResult.isValid()) {
				response.setStatus(authResult.getErrorCode());
				out.print(authError(authResult.getErrorMessage(), authResult.getErrorCode()));
				return;
			}

			try {
				JSONObject result = dispatch(rv, method, authResult.getAdmin());
				out.print(result.toString(2));
			} catch (Exception e) {
				LOGGER.error("UpdateCfgAction 执行失败: method=" + method + ", admin=" + authResult.getLoginId(), e);
				out.print(new JSONObject().put("success", false).put("message", "执行失败: " + e.getMessage()).toString(2));
			}
		} else {
			try {
				JSONObject result = dispatch(rv, method, null);
				out.print(result.toString(2));
			} catch (Exception e) {
				LOGGER.error("UpdateCfgAction 执行失败: method=" + method, e);
				out.print(new JSONObject().put("success", false).put("message", "执行失败: " + e.getMessage()).toString(2));
			}
		}

		out.flush();
	}

	/**
	 * 检查 method 是否在白名单中（无需权限验证）
	 */
	private boolean isWhitelisted(String method) {
		for (String wm : WHITELIST_METHODS) {
			if (wm.equalsIgnoreCase(method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 构建权限错误响应
	 */
	private String authError(String message, int code) {
		return new JSONObject()
				.put("success", false)
				.put("message", message)
				.put("errorCode", code)
				.toString(2);
	}

	/**
	 * 根据 method 分发到 UpdateCfgXml 的对应方法
	 *
	 * @param admin 已验证的管理员（写操作会记录操作人）
	 */
	private JSONObject dispatch(RequestValue rv, String method, ConfAdmin admin) {
		UpdateCfgXml cfg = new UpdateCfgXml();
		String xmlPath = rv.getString("XML_PATH");

		switch (method) {
		case "listXmlFiles":
			return cfg.listXmlFiles();

		case "listItems":
			return cfg.listItems(xmlPath);

		case "listXItems":
			return cfg.listXItems(xmlPath, rv.getString("ITEM_NAME"));

		case "getXItem":
			return cfg.getXItem(xmlPath, rv.getString("ITEM_NAME"), rv.getString("XITEM_NAME"));

		case "saveXItem":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.saveXItem(xmlPath, rv.getString("ITEM_NAME"), buildXItemData(rv));

		case "deleteXItem":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.deleteXItem(xmlPath, rv.getString("ITEM_NAME"), rv.getString("XITEM_NAME"));

		case "moveXItem":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.moveXItem(xmlPath, rv.getString("ITEM_NAME"), rv.getString("XITEM_NAME"),
					rv.getString("direction"));

		case "listActions":
			return cfg.listActions(xmlPath, rv.getString("ITEM_NAME"));

		case "getSqlSet":
			return cfg.getSqlSet(xmlPath, rv.getString("ITEM_NAME"), rv.getString("SQL_NAME"));

		case "saveSqlSet":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.saveSqlSet(xmlPath, rv.getString("ITEM_NAME"), buildSqlSetData(rv));

		case "deleteSqlSet":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.deleteSqlSet(xmlPath, rv.getString("ITEM_NAME"), rv.getString("SQL_NAME"));

		case "getScriptSet":
			return cfg.getScriptSet(xmlPath, rv.getString("ITEM_NAME"), rv.getString("SCRIPT_NAME"));

		case "saveScriptSet":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.saveScriptSet(xmlPath, rv.getString("ITEM_NAME"), buildScriptSetData(rv));

		case "deleteScriptSet":
			if (admin != null) cfg.setOperator(admin.getLoginId());
			return cfg.deleteScriptSet(xmlPath, rv.getString("ITEM_NAME"), rv.getString("SCRIPT_NAME"));

		default:
			return new JSONObject().put("success", false).put("message", "未知方法: " + method);
		}
	}

	/** 从 RequestValue 构建 XItemData */
	private UpdateCfgXItemData buildXItemData(RequestValue rv) {
		UpdateCfgXItemData d = new UpdateCfgXItemData();
		d.name = rv.getString("XITEM_NAME");
		d.oldName = rv.getString("OLD_XITEM_NAME");
		d.tag = rv.getString("TAG");
		d.descZh = rv.getString("DESC_ZH");
		d.descEn = rv.getString("DESC_EN");
		d.dataField = rv.getString("DATA_FIELD");
		d.dataType = rv.getString("DATA_TYPE");
		d.isEncrypt = rv.getString("IS_ENCRYPT");
		d.valid = rv.getString("VALID");
		d.format = rv.getString("FORMAT");
		d.isMustInput = rv.getString("IS_MUST_INPUT");
		d.maxLength = rv.getString("MAX_LENGTH");
		d.minLength = rv.getString("MIN_LENGTH");
		d.maxValue = rv.getString("MAX_VALUE");
		d.minValue = rv.getString("MIN_VALUE");
		d.isOrder = rv.getString("IS_ORDER");
		d.searchType = rv.getString("SEARCH_TYPE");
		d.orderExp = rv.getString("ORDER_EXP");
		d.style = rv.getString("STYLE");
		d.parentStyle = rv.getString("PARENT_STYLE");
		d.xstyle = rv.getString("XSTYLE");
		d.eventName = rv.getString("EVENT_NAME");
		d.eventType = rv.getString("EVENT_TYPE");
		d.eventValue = rv.getString("EVENT_VALUE");
		d.callAction = rv.getString("CALL_ACTION");
		d.confirmInfo = rv.getString("CONFIRM_INFO");
		d.listSql = rv.getString("LIST_SQL");
		d.listValueList = rv.getString("LIST_VALUE_LIST");
		d.listDisplayList = rv.getString("LIST_DISPLAY_LIST");
		d.userSet = rv.getString("USER_SET");
		return d;
	}

	/** 从 RequestValue 构建 SqlSetData */
	private UpdateCfgSqlSetData buildSqlSetData(RequestValue rv) {
		UpdateCfgSqlSetData d = new UpdateCfgSqlSetData();
		d.name = rv.getString("SQL_NAME");
		d.oldName = rv.getString("OLD_SQL_NAME");
		d.sqlType = rv.getString("SQL_TYPE");
		d.transType = rv.getString("TRANS_TYPE");
		d.sqlContent = rv.getString("SQL_CONTENT");
		return d;
	}

	/** 从 RequestValue 构建 ScriptSetData */
	private UpdateCfgScriptSetData buildScriptSetData(RequestValue rv) {
		UpdateCfgScriptSetData d = new UpdateCfgScriptSetData();
		d.name = rv.getString("SCRIPT_NAME");
		d.oldName = rv.getString("OLD_SCRIPT_NAME");
		d.scriptType = rv.getString("SCRIPT_TYPE");
		d.scriptContent = rv.getString("SCRIPT_CONTENT");
		return d;
	}
}
