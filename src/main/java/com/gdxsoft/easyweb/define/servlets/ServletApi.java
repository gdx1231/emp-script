package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.conf.ConfAdmin;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UpdateXmlImpl;
import com.gdxsoft.easyweb.define.UpdateXmlJdbcImpl;
import com.gdxsoft.easyweb.define.bussinessXmlCreator.BusinessXmlCreator;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.Tables;
import com.gdxsoft.easyweb.define.servlets.ApiTokenValidator.ValidationResult;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * Servlet API for configuration management
 * 
 * Methods: - getConfXml: Get full XML configuration file - getConfItem: Get
 * specific configuration item - runConfItem: Copy configuration item -
 * updateConfItem: Update configuration item - login: Generate API token -
 * logout: Revoke API token
 * 
 * Authentication: - HMAC Signature (recommended for server-to-server) - JWT
 * Token (for client sessions) - Simple Token (legacy, not recommended)
 * 
 * @see ApiTokenValidator for authentication details
 */
public class ServletApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ServletApi.class);

	private static final String PARAM_METHOD = "method";
	private static final String PARAM_XMLNAME = "xmlname";
	private static final String PARAM_ITEMNAME = "itemname";
	private static final String PARAM_XML = "xml";
	private static final String PARAM_LOGIN_ID = "login_id";
	private static final String PARAM_PASSWORD = "password";
	private static final String PARAM_TOKEN = "token";
	private static final String PARAM_OUTPUT = "output";
	private static final String PARAM_DB = "db";
	private static final String PARAM_TABLENAME = "tablename";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_WHERE = "where";
	private static final String PARAM_PAGE = "page";
	private static final String PARAM_PAGESIZE = "pagesize";
	private static final String PARAM_PK = "pk";
	private static final String PARAM_FRAMETYPE = "frametype";
	private static final String PARAM_OPERATIONTYPE = "operationtype";
	private static final String PARAM_ADMID = "admid";
	private static final String PARAM_SCRIPTPATH = "scriptpath";
	private static final String PARAM_NEW_ITEMNAME = "new_itemname";

	// 输出格式常量
	private static final String OUTPUT_XML = "xml";
	private static final String OUTPUT_JSON = "json";
	private static final String OUTPUT_CSV = "csv";

	// getTableData 分页限制
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;

	public ServletApi() {
		super();
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Process API requests
	 */
	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");

		PrintWriter out = response.getWriter();

		// Check if define is allowed
		if (!ConfDefine.isAllowDefine()) {
			out.println(createErrorResponse("Define not allowed", 403));
			response.setStatus(403);
			return;
		}

		String method = request.getParameter(PARAM_METHOD);
		if (StringUtils.isBlank(method)) {
			out.println(createErrorResponse("Missing method parameter", 400));
			response.setStatus(400);
			return;
		}

		// Login method doesn't require authentication
		if ("login".equalsIgnoreCase(method)) {
			JSONObject result = handleLogin(request);
			out.println(result.toString());
			return;
		}

		// Help method doesn't require authentication
		if ("help".equalsIgnoreCase(method)) {
			JSONObject result = getHelp();
			out.println(result.toString());
			return;
		}

		// Logout method
		if ("logout".equalsIgnoreCase(method)) {
			String token = request.getParameter(PARAM_TOKEN);
			if (StringUtils.isBlank(token)) {
				token = request.getHeader(ApiTokenValidator.HEADER_TOKEN);
			}
			if (StringUtils.isBlank(token)) {
				String authHeader = request.getHeader(ApiTokenValidator.HEADER_AUTHORIZATION);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					token = authHeader.substring(7);
				}
			}
			ApiTokenValidator.revokeToken(token);
			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("MSG", "Logged out successfully");
			out.println(result.toString());
			return;
		}

		// Validate authentication
		ValidationResult authResult = ApiTokenValidator.validate(request);
		if (!authResult.isValid()) {
			out.println(createErrorResponse(authResult.getErrorMessage(), authResult.getErrorCode()));
			response.setStatus(authResult.getErrorCode());
			return;
		}

		ConfAdmin admin = authResult.getAdmin();

		JSONObject result;
		try {
			RequestValue rv = new RequestValue(request);

			switch (method.toLowerCase()) {
			case "getconfxml":
				result = getConfXml(rv);
				break;
			case "getconfitem":
				result = getConfItem(rv);
				break;
			case "runconfitem":
				result = runConfItem(rv, admin);
				break;
			case "updateconfitem":
				result = updateConfItem(rv, admin);
				break;
			case "deleteconfitem":
				result = deleteConfItem(rv, admin);
				break;
			case "gettables":
				result = getTables(rv);
				break;
			case "gettable":
				result = getTable(rv);
				break;
			case "gettabledata":
				result = getTableData(rv, response);
				break;
			case "createbusinessxml":
				result = createBusinessXml(rv, admin);
				break;
			case "previewbusinessxml":
				result = previewBusinessXml(rv);
				break;
			case "showscriptpaths":
				result = showScriptPaths();
				break;
			case "getxmlfile":
				result = getXmlFile(rv);
				break;
			case "validatesql":
				result = validateSql(rv);
				break;
			default:
				result = createErrorResponse("Unknown method: " + method, 400);
				response.setStatus(400);
			}
		} catch (Exception e) {
			LOGGER.error("Error processing request", e);
			result = createErrorResponse("Error: " + e.getMessage(), 500);
			response.setStatus(500);
		}

		out.println(result.toString());
	}

	/**
	 * Handle login request
	 */
	private JSONObject handleLogin(HttpServletRequest request) {
		String loginId = request.getParameter(PARAM_LOGIN_ID);
		String password = request.getParameter(PARAM_PASSWORD);

		if (StringUtils.isBlank(loginId)) {
			return UJSon.rstFalse("Missing login_id parameter");
		}
		if (StringUtils.isBlank(password)) {
			return UJSon.rstFalse("Missing password parameter");
		}

		JSONObject result = ApiTokenValidator.generateToken(loginId, password);
		if (result == null) {
			return UJSon.rstFalse("Invalid login credentials");
		}

		return result;
	}

	/**
	 * Get the full XML configuration file content
	 * 
	 * @param rv RequestValue containing parameters - xmlname: configuration name -
	 *           output: output format (xml/json), default xml
	 * @return JSON result with XML or JSON content
	 */
	private JSONObject getConfXml(RequestValue rv) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String output = rv.getString(PARAM_OUTPUT);

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		// 默认输出格式为 xml
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		try {
			IConfig configType = UserConfig.getConfig(xmlName, null);
			if (configType == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			Document doc = configType.loadConfiguration();
			if (doc == null) {
				return UJSon.rstFalse("Failed to load configuration: " + xmlName);
			}

			String xml = UXml.asXml(doc);
			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("XMLNAME", xmlName);
			result.put("OUTPUT", output);

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				// 将 XML 转换为 JSON
				JSONObject xmlAsJson = XML.toJSONObject(xml);
				result.put("DATA", xmlAsJson);
			} else {
				// 默认输出 XML 格式
				result.put("XML", xml);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error getting conf xml", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Get a specific configuration item from XML
	 * 
	 * @param rv RequestValue containing parameters - xmlname: configuration name -
	 *           itemname: item name - output: output format (xml/json), default xml
	 * @return JSON result with XML or JSON content
	 */
	private JSONObject getConfItem(RequestValue rv) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String itemName = rv.getString(PARAM_ITEMNAME);
		String output = rv.getString(PARAM_OUTPUT);

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		if (StringUtils.isBlank(itemName)) {
			return UJSon.rstFalse("Missing itemname parameter");
		}

		// 默认输出格式为 xml
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		try {
			IUpdateXml updateXml = getUpdateXml(xmlName);
			if (updateXml == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			String itemXml = updateXml.queryItemXml(itemName);
			if (itemXml == null) {
				return UJSon.rstFalse("Item not found: " + itemName);
			}

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("XMLNAME", xmlName);
			result.put("ITEMNAME", itemName);
			result.put("OUTPUT", output);

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				// 将 XML 转换为 JSON
				JSONObject xmlAsJson = XML.toJSONObject(itemXml);
				result.put("DATA", xmlAsJson);
			} else {
				// 默认输出 XML 格式
				result.put("XML", itemXml);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error getting conf item", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Read XML file content directly from configuration storage (file or JDBC).
	 *
	 * @param rv RequestValue with xmlname, optional scriptpath
	 * @return JSON result with raw XML content
	 */
	private JSONObject getXmlFile(RequestValue rv) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String scriptPath = rv.getString(PARAM_SCRIPTPATH);
		String output = rv.getString(PARAM_OUTPUT);

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		try {
			IConfig configType;
			if (StringUtils.isNotBlank(scriptPath)) {
				configType = getConfigByPath(scriptPath, xmlName);
				if (configType == null) {
					return UJSon.rstFalse("Configuration not found: " + scriptPath + " / " + xmlName);
				}
			} else {
				configType = UserConfig.getConfig(xmlName, null);
			}

			if (configType == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			// 读取 XML 内容
			String xmlContent;
			ConfScriptPath sp = configType.getScriptPath();
			if (sp != null && sp.isJdbc()) {
				// JDBC 模式：从数据库加载
				Document doc = configType.loadConfiguration();
				if (doc == null) {
					return UJSon.rstFalse("Failed to load configuration from JDBC: " + xmlName);
				}
				xmlContent = UXml.asXml(doc);
			} else {
				// File 模式：直接读取文件
				String rootPath = sp != null ? sp.getPath() : configType.getPath();
				String filePath = rootPath + UserConfig.filterXmlName(xmlName);
				File file = new File(filePath);
				if (!file.exists()) {
					return UJSon.rstFalse("XML file not found: " + filePath);
				}
				xmlContent = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
			}

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("XMLNAME", xmlName);
			result.put("OUTPUT", output);

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				JSONObject xmlAsJson = XML.toJSONObject(xmlContent);
				result.put("DATA", xmlAsJson);
			} else {
				result.put("XML", xmlContent);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error reading XML file", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Run/execute a configuration item (copy item)
	 */
	private JSONObject runConfItem(RequestValue rv, ConfAdmin admin) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String itemName = rv.getString(PARAM_ITEMNAME);
		String newItemName = rv.getString("new_itemname");

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		if (StringUtils.isBlank(itemName)) {
			return UJSon.rstFalse("Missing itemname parameter");
		}

		if (StringUtils.isBlank(newItemName)) {
			return UJSon.rstFalse("Missing new_itemname parameter");
		}

		try {
			IUpdateXml updateXml = getUpdateXml(xmlName);
			if (updateXml == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			updateXml.setAdmin(admin.getLoginId());

			boolean success = updateXml.copyItem(itemName, newItemName);
			if (success) {
				JSONObject result = new JSONObject();
				result.put("RST", true);
				result.put("MSG", "Item copied successfully");
				result.put("XMLNAME", xmlName);
				result.put("SOURCE_ITEM", itemName);
				result.put("NEW_ITEM", newItemName);
				return result;
			} else {
				return UJSon.rstFalse("Failed to copy item");
			}
		} catch (Exception e) {
			LOGGER.error("Error running conf item", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Update a configuration item in XML
	 */
	private JSONObject updateConfItem(RequestValue rv, ConfAdmin admin) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String itemName = rv.getString(PARAM_ITEMNAME);
		String xml = rv.getString(PARAM_XML);

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		if (StringUtils.isBlank(itemName)) {
			return UJSon.rstFalse("Missing itemname parameter");
		}

		if (StringUtils.isBlank(xml)) {
			return UJSon.rstFalse("Missing xml parameter");
		}

		// 校验 XML 合法性和 Tag 值
		ConfigValidator.ValidationResult vr = ConfigValidator.validateItemXml(xml, itemName);
		if (!vr.isValid()) {
			return UJSon.rstFalse("XML 校验不通过: " + vr.getErrorMessage());
		}

		try {
			IUpdateXml updateXml = getUpdateXml(xmlName);
			if (updateXml == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			updateXml.setAdmin(admin.getLoginId());

			boolean success = updateXml.updateItem(itemName, xml);
			if (success) {
				JSONObject result = new JSONObject();
				result.put("RST", true);
				result.put("MSG", "Item updated successfully");
				result.put("XMLNAME", xmlName);
				result.put("ITEMNAME", itemName);
				return result;
			} else {
				return UJSon.rstFalse("Failed to update item");
			}
		} catch (Exception e) {
			LOGGER.error("Error updating conf item", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Delete a configuration item
	 */
	private JSONObject deleteConfItem(RequestValue rv, ConfAdmin admin) {
		String xmlName = rv.getString(PARAM_XMLNAME);
		String itemName = rv.getString(PARAM_ITEMNAME);

		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		if (StringUtils.isBlank(itemName)) {
			return UJSon.rstFalse("Missing itemname parameter");
		}

		try {
			IUpdateXml updateXml = getUpdateXml(xmlName);
			if (updateXml == null) {
				return UJSon.rstFalse("Configuration not found: " + xmlName);
			}

			updateXml.setAdmin(admin.getLoginId());

			boolean success = updateXml.removeItem(itemName);
			if (success) {
				JSONObject result = new JSONObject();
				result.put("RST", true);
				result.put("MSG", "Item deleted successfully");
				result.put("XMLNAME", xmlName);
				result.put("ITEMNAME", itemName);
				return result;
			} else {
				return UJSon.rstFalse("Failed to delete item");
			}
		} catch (Exception e) {
			LOGGER.error("Error deleting conf item", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Get database tables list
	 * 
	 * @param rv RequestValue containing parameters - db: database connection name -
	 *           filter: table name filter (optional, supports % wildcard) - output:
	 *           output format (xml/json), default xml
	 * @return JSON result with tables list
	 */
	private JSONObject getTables(RequestValue rv) {
		String db = rv.getString(PARAM_DB);
		String filter = rv.getString(PARAM_FILTER);
		String output = rv.getString(PARAM_OUTPUT);

		if (StringUtils.isBlank(db)) {
			return UJSon.rstFalse("Missing db parameter");
		}

		// 默认输出格式为 xml
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		try {
			Tables tables = new Tables();
			tables.initTables(db);

			ArrayList<String> tableList = tables.getTableList();
			if (tableList == null || tableList.isEmpty()) {
				return UJSon.rstFalse("No tables found or database connection failed");
			}

			// 过滤表名
			if (StringUtils.isNotBlank(filter)) {
				String filterLower = filter.toLowerCase();
				ArrayList<String> filteredList = new ArrayList<>();
				for (String tableName : tableList) {
					if (filter.contains("%")) {
						// 支持 % 通配符，转换为正则表达式
						String regex = filter.replace("%", ".*").toLowerCase();
						if (tableName.toLowerCase().matches(regex)) {
							filteredList.add(tableName);
						}
					} else {
						// 不含 %，则进行包含匹配
						if (tableName.toLowerCase().contains(filterLower)) {
							filteredList.add(tableName);
						}
					}
				}
				tableList = filteredList;
			}

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("DB", db);
			result.put("OUTPUT", output);
			result.put("COUNT", tableList.size());

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				// 输出 JSON 格式
				JSONArray tablesArray = new JSONArray();
				for (String tableName : tableList) {
					Table table = tables.get(tableName);
					JSONObject tableInfo = new JSONObject();
					tableInfo.put("name", tableName);
					if (table != null) {
						tableInfo.put("type", table.getTableType() != null ? table.getTableType().trim() : "TABLE");
						tableInfo.put("schema", table.getSchemaName());
					}
					tablesArray.put(tableInfo);
				}
				result.put("TABLES", tablesArray);
			} else {
				// 默认输出 XML 格式
				StringBuilder xml = new StringBuilder();
				xml.append("<Tables db=\"").append(db).append("\">");
				for (String tableName : tableList) {
					Table table = tables.get(tableName);
					xml.append("<Table name=\"").append(tableName).append("\"");
					if (table != null) {
						xml.append(" type=\"")
								.append(table.getTableType() != null ? table.getTableType().trim() : "TABLE")
								.append("\"");
						xml.append(" schema=\"").append(table.getSchemaName() != null ? table.getSchemaName() : "")
								.append("\"");
					}
					xml.append("/>");
				}
				xml.append("</Tables>");
				result.put("XML", xml.toString());
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error getting tables", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Get single table structure details
	 * 
	 * @param rv RequestValue containing parameters - db: database connection name -
	 *           tablename: table name - output: output format (xml/json), default
	 *           xml
	 * @return JSON result with table structure
	 */
	private JSONObject getTable(RequestValue rv) {
		String db = rv.getString(PARAM_DB);
		String tableName = rv.getString(PARAM_TABLENAME);
		String output = rv.getString(PARAM_OUTPUT);

		if (StringUtils.isBlank(db)) {
			return UJSon.rstFalse("Missing db parameter");
		}

		if (StringUtils.isBlank(tableName)) {
			return UJSon.rstFalse("Missing tablename parameter");
		}

		// 默认输出格式为 xml
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		try {
			Table table = new Table(tableName, db);
			table.init();

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("DB", db);
			result.put("TABLENAME", tableName);
			result.put("OUTPUT", output);

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				// 输出 JSON 格式
				String tableXml = table.toXml();
				JSONObject tableJson = XML.toJSONObject(tableXml);
				result.put("DATA", tableJson);
			} else {
				// 默认输出 XML 格式
				String tableXml = table.toXml();
				result.put("XML", tableXml);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error getting table", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Get table data with pagination
	 *
	 * @param rv       RequestValue containing parameters - db: database connection
	 *                 name - tablename: table name - where: optional WHERE clause
	 *                 (without the WHERE keyword) - page: page number (default 1) -
	 *                 pagesize: page size (default 10, max 100) - pk: primary key
	 *                 field (optional, for pagination) - output: output format
	 *                 (xml/json/csv), default json
	 * @param response HttpServletResponse for setting content type
	 * @return JSON result with table data
	 */
	private JSONObject getTableData(RequestValue rv, HttpServletResponse response) {
		String db = rv.getString(PARAM_DB);
		String tableName = rv.getString(PARAM_TABLENAME);
		String whereClause = rv.getString(PARAM_WHERE);
		String output = rv.getString(PARAM_OUTPUT);
		String pkField = rv.getString(PARAM_PK);

		// 分页参数
		int page = 1;
		int pageSize = DEFAULT_PAGE_SIZE;
		try {
			String pageStr = rv.getString(PARAM_PAGE);
			if (StringUtils.isNotBlank(pageStr)) {
				page = Integer.parseInt(pageStr);
			}
			String pageSizeStr = rv.getString(PARAM_PAGESIZE);
			if (StringUtils.isNotBlank(pageSizeStr)) {
				pageSize = Integer.parseInt(pageSizeStr);
			}
		} catch (NumberFormatException e) {
			// 使用默认值
		}

		if (StringUtils.isBlank(db)) {
			return UJSon.rstFalse("Missing db parameter");
		}

		if (StringUtils.isBlank(tableName)) {
			return UJSon.rstFalse("Missing tablename parameter");
		}

		// 默认输出格式为 json
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_JSON;
		}

		// 限制页码和每页记录数
		if (page < 1)
			page = 1;
		if (pageSize < 1)
			pageSize = DEFAULT_PAGE_SIZE;
		if (pageSize > MAX_PAGE_SIZE)
			pageSize = MAX_PAGE_SIZE;

		try {
			// 构建 SQL 查询
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM ").append(tableName);

			// 添加可选的 WHERE 条件
			if (StringUtils.isNotBlank(whereClause)) {
				// 简单的 SQL 注入防护：移除危险关键字
				String sanitizedWhere = whereClause.replaceAll("(?i)(DROP|DELETE|TRUNCATE|ALTER|CREATE|INSERT|UPDATE)",
						"");
				sql.append(" WHERE ").append(sanitizedWhere);
			}

			// 如果没有指定主键，尝试使用第一个字段
			if (StringUtils.isBlank(pkField)) {
				pkField = "1"; // 默认使用第一列
			}

			LOGGER.info("getTableData SQL: {}, page: {}, pageSize: {}", sql.toString(), page, pageSize);

			// 使用分页查询
			DTTable dtTable = DTTable.getJdbcTable(sql.toString(), pkField, pageSize, page, db, rv);

			if (dtTable == null) {
				return UJSon.rstFalse("Query failed");
			}

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("DB", db);
			result.put("TABLENAME", tableName);
			result.put("OUTPUT", output);
			result.put("PAGE", page);
			result.put("PAGESIZE", pageSize);
			result.put("COUNT", dtTable.getCount());

			if (OUTPUT_CSV.equalsIgnoreCase(output)) {
				// CSV 格式输出 - 使用 DTTable.toCSV()
				result.put("CSV", dtTable.toCSV());
				response.setContentType("text/csv; charset=UTF-8");

			} else if (OUTPUT_XML.equalsIgnoreCase(output)) {
				// XML 格式输出 - 使用 DTTable.toXml()
				result.put("XML", dtTable.toXml(rv));

			} else {
				// 默认 JSON 格式输出 - 使用 DTTable.toJSONArray()
				result.put("DATA", dtTable.toJSONArray());
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error getting table data", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Create business XML from database table and save it
	 *
	 * @param rv    RequestValue containing parameters - db: database connection
	 *              name - tablename: table name - frametype: Frame type
	 *              (ListFrame/Frame/Tree) - operationtype: Operation type
	 *              (N/M/V/NM) - xmlname: configuration file name (e.g., ewa/m) -
	 *              itemname: configuration item name - admid: admin ID
	 * @param admin ConfAdmin instance
	 * @return JSON result with success/failure status
	 */
	private JSONObject createBusinessXml(RequestValue rv, ConfAdmin admin) {
		String db = rv.getString(PARAM_DB);
		String tableName = rv.getString(PARAM_TABLENAME);
		String frameType = rv.getString(PARAM_FRAMETYPE);
		String operationType = rv.getString(PARAM_OPERATIONTYPE);
		String xmlName = rv.getString(PARAM_XMLNAME);
		String itemName = rv.getString(PARAM_ITEMNAME);
		String admId = rv.getString(PARAM_ADMID);

		// 参数验证
		if (StringUtils.isBlank(db)) {
			return UJSon.rstFalse("Missing db parameter");
		}
		if (StringUtils.isBlank(tableName)) {
			return UJSon.rstFalse("Missing tablename parameter");
		}
		if (StringUtils.isBlank(frameType)) {
			return UJSon.rstFalse("Missing frametype parameter");
		}
		if (StringUtils.isBlank(operationType)) {
			return UJSon.rstFalse("Missing operationtype parameter");
		}
		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}
		if (StringUtils.isBlank(itemName)) {
			return UJSon.rstFalse("Missing itemname parameter");
		}

		// 验证 Frame 类型
		ConfigValidator.ValidationResult ftResult = ConfigValidator.validateFrameType(frameType);
		if (!ftResult.isValid()) {
			return UJSon.rstFalse(ftResult.getErrorMessage());
		}
		String frameTypeUpper = frameType.toUpperCase();

		// 验证操作类型
		ConfigValidator.ValidationResult otResult = ConfigValidator.validateOperationType(operationType);
		if (!otResult.isValid()) {
			return UJSon.rstFalse(otResult.getErrorMessage());
		}
		String operationTypeUpper = operationType.toUpperCase();

		// 使用 admin 的 loginId 作为 admId（如果未提供）
		if (StringUtils.isBlank(admId)) {
			admId = admin.getLoginId();
		}

		try {
			// 读取数据库表结构
			Table table = new Table(tableName, db);
			table.init();
			if (table.getFields().size() == 0) {
				return UJSon.rstFalse("Table has no fields or failed to read table structure: " + tableName);
			}
			// 获取配置路径（容器不存在则自动创建）
			String scriptPath = rv.getString(PARAM_SCRIPTPATH);
			IConfig configType = null;
			if (StringUtils.isNotBlank(scriptPath)) {
				configType = getConfigByPath(scriptPath, xmlName);
				if (configType == null) {
					return UJSon.rstFalse("Configuration not found in specified path: " + scriptPath + " / " + xmlName);
				}
			} else {
				configType = UserConfig.getConfig(xmlName, null);
				if (configType == null) {
					configType = autoCreateConfig(xmlName, admId);
					if (configType == null) {
						return UJSon.rstFalse("Configuration not found and auto-create failed: " + xmlName);
					}
				}
			}

			// 创建 BusinessXmlCreator
			BusinessXmlCreator creator = BusinessXmlCreator.create(configType, table, frameTypeUpper);

			// 生成并保存 XML
			boolean success = creator.createAndSave(db, tableName, null, null, frameTypeUpper, operationTypeUpper,
					xmlName, itemName, admId);

			if (success) {
				JSONObject result = new JSONObject();
				result.put("RST", true);
				result.put("MSG", "Business XML created and saved successfully");
				result.put("XMLNAME", xmlName);
				result.put("ITEMNAME", itemName);
				result.put("FRAMETYPE", frameTypeUpper);
				result.put("OPERATIONTYPE", operationTypeUpper);
				return result;
			} else {
				return UJSon.rstFalse("Failed to create or save business XML");
			}
		} catch (Exception e) {
			LOGGER.error("Error creating business XML", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	 * Preview business XML without saving
	 *
	 * @param rv RequestValue containing parameters - db: database connection name -
	 *           tablename: table name - frametype: Frame type
	 *           (ListFrame/Frame/Tree) - operationtype: Operation type (N/M/V/NM) -
	 *           xmlname: configuration file name (e.g., ewa/m) - output: output
	 *           format (xml/json), default xml
	 * @return JSON result with XML content
	 */
	private JSONObject previewBusinessXml(RequestValue rv) {
		String db = rv.getString(PARAM_DB);
		String tableName = rv.getString(PARAM_TABLENAME);
		String frameType = rv.getString(PARAM_FRAMETYPE);
		String operationType = rv.getString(PARAM_OPERATIONTYPE);
		String xmlName = rv.getString(PARAM_XMLNAME);
		String output = rv.getString(PARAM_OUTPUT);

		// 参数验证
		if (StringUtils.isBlank(db)) {
			return UJSon.rstFalse("Missing db parameter");
		}
		if (StringUtils.isBlank(tableName)) {
			return UJSon.rstFalse("Missing tablename parameter");
		}
		if (StringUtils.isBlank(frameType)) {
			return UJSon.rstFalse("Missing frametype parameter");
		}
		if (StringUtils.isBlank(operationType)) {
			return UJSon.rstFalse("Missing operationtype parameter");
		}
		if (StringUtils.isBlank(xmlName)) {
			return UJSon.rstFalse("Missing xmlname parameter");
		}

		// 默认输出格式为 xml
		if (StringUtils.isBlank(output)) {
			output = OUTPUT_XML;
		}

		// 验证 Frame 类型
		ConfigValidator.ValidationResult ftResult = ConfigValidator.validateFrameType(frameType);
		if (!ftResult.isValid()) {
			return UJSon.rstFalse(ftResult.getErrorMessage());
		}
		String frameTypeUpper = frameType.toUpperCase();

		// 验证操作类型
		ConfigValidator.ValidationResult otResult = ConfigValidator.validateOperationType(operationType);
		if (!otResult.isValid()) {
			return UJSon.rstFalse(otResult.getErrorMessage());
		}
		String operationTypeUpper = operationType.toUpperCase();

		try {
			// 读取数据库表结构
			Table table = new Table(tableName, db);
			table.init();

			// 创建 EwaConfig（容器不存在则自动创建）
			String scriptPath = rv.getString(PARAM_SCRIPTPATH);
			IConfig configType = null;
			if (StringUtils.isNotBlank(scriptPath)) {
				configType = getConfigByPath(scriptPath, xmlName);
				if (configType == null) {
					return UJSon.rstFalse("Configuration not found in specified path: " + scriptPath + " / " + xmlName);
				}
			} else {
				configType = UserConfig.getConfig(xmlName, null);
				if (configType == null) {
					configType = autoCreateConfig(xmlName, "api");
					if (configType == null) {
						return UJSon.rstFalse("Configuration not found and auto-create failed: " + xmlName);
					}
				}
			}

			// 创建 BusinessXmlCreator
			BusinessXmlCreator creator = BusinessXmlCreator.create(configType, table, frameTypeUpper);

			// 生成预览 XML
			String xmlContent = creator.createShowXml(db, tableName, null, null, frameTypeUpper, operationTypeUpper);

			if (xmlContent == null) {
				return UJSon.rstFalse("Failed to generate business XML");
			}

			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("XMLNAME", xmlName);
			result.put("FRAMETYPE", frameTypeUpper);
			result.put("OPERATIONTYPE", operationTypeUpper);
			result.put("OUTPUT", output);

			if (OUTPUT_JSON.equalsIgnoreCase(output)) {
				// 将 XML 转换为 JSON
				JSONObject xmlAsJson = XML.toJSONObject(xmlContent);
				result.put("DATA", xmlAsJson);
			} else {
				// 默认输出 XML 格式
				result.put("XML", xmlContent);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("Error previewing business XML", e);
			return UJSon.rstFalse("Error: " + e.getMessage());
		}
	}

	/**
	/**
	 * Get IConfig from a specific script path by name.
	 *
	 * @param scriptPathName script path name (e.g., "pf", "b2b")
	 * @param xmlName        configuration file name
	 * @return IConfig if found, null otherwise
	 */
	private IConfig getConfigByPath(String scriptPathName, String xmlName) {
		ConfScriptPaths sps = ConfScriptPaths.getInstance();
		for (ConfScriptPath sp : sps.getLst()) {
			if (scriptPathName.equals(sp.getName())) {
				return UserConfig.createConfig(sp, xmlName, null);
			}
		}
		return null;
	}

	/**
	 * Auto-create XML configuration container in the first writable
	 * ConfScriptPath. Supports both JDBC and File storage modes.
	 *
	 * @param xmlName configuration file name (e.g., /business/camp/camp_act_template.xml)
	 * @param admId   admin login ID for the creator (nullable, defaults to "api")
	 * @return IConfig if created successfully, null if all writable paths failed
	 */
	private IConfig autoCreateConfig(String xmlName, String admId) {
		if (StringUtils.isBlank(admId)) {
			admId = "api";
		}
		ConfScriptPaths sps = ConfScriptPaths.getInstance();

		for (ConfScriptPath sp : sps.getLst()) {
			// Skip resource-based and read-only paths
			if (sp.isResources() || sp.isReadOnly()) {
				continue;
			}

			try {
				if (sp.isJdbc()) {
					// JDBC mode: create container in EWA_CFG_TREE + EWA_CFG tables
					JdbcConfigOperation op = new JdbcConfigOperation(sp);
					op.createXml(xmlName, admId);
				} else {
					// File mode: create container XML file on disk
					String root = sp.getPath();
					String filteredName = UserConfig.filterXmlName(xmlName);
					java.io.File file = new java.io.File(root + filteredName);
					java.io.File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					UFile.createNewTextFile(file.getAbsolutePath(), ConfigUtils.XML_ROOT);
				}

				// Verify creation succeeded
				IConfig configType = UserConfig.createConfig(sp, xmlName, null);
				if (configType != null && configType.checkConfigurationExists()) {
					LOGGER.info("Auto-created XML container: {} in path: {}", xmlName, sp.getPath());
					return configType;
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to auto-create config in path {}: {}", sp.getPath(), xmlName, e);
			}
		}

		LOGGER.error("Failed to auto-create config in any writable path: {}", xmlName);
		return null;
	}
	/**
	 * Show all available script paths for configuration storage.
	 *
	 * @return JSON result with script paths list
	 */
	private JSONObject showScriptPaths() {
		ConfScriptPaths sps = ConfScriptPaths.getInstance();
		JSONObject result = new JSONObject();
		result.put("RST", true);

		JSONArray paths = new JSONArray();
		for (ConfScriptPath sp : sps.getLst()) {
			JSONObject spObj = new JSONObject();
			spObj.put("name", sp.getName());
			spObj.put("path", sp.getPath());
			spObj.put("isResources", sp.isResources());
			spObj.put("isJdbc", sp.isJdbc());
			spObj.put("isReadOnly", sp.isReadOnly());
			paths.put(spObj);
		}
		result.put("scriptPaths", paths);
		return result;
	}

	/**
	 * Validate SQL syntax against a database connection. Executes the SQL in a
	 * transaction and rolls back, so no data is modified.
	 *
	 * @param rv RequestValue with db (database name) and sql (SQL to validate)
	 * @return JSON result with RST=true on success, or RST=false with error details
	 */
	private JSONObject validateSql(RequestValue rv) {
		String db = rv.getString(PARAM_DB);
		String sql = rv.getString("sql");

		ConfigValidator.ValidationResult vr = ConfigValidator.validateSql(db, sql);
		if (vr.isValid()) {
			JSONObject result = new JSONObject();
			result.put("RST", true);
			result.put("DB", db);
			result.put("MSG", "SQL 语法校验通过");
			return result;
		} else {
			return UJSon.rstFalse(vr.getErrorMessage());
		}
	}

	/**
	 * Get API help documentation
	 */
	private JSONObject getHelp() {
		JSONObject result = new JSONObject();
		result.put("RST", true);

		JSONObject methods = new JSONObject();
		methods.put("login", "Generate API token (params: login_id, password)");
		methods.put("logout", "Revoke API token");
		methods.put("getConfXml", "Get full XML configuration (params: xmlname, output[xml/json])");
		methods.put("getConfItem", "Get configuration item (params: xmlname, itemname, output[xml/json])");
		methods.put("runConfItem", "Copy configuration item (params: xmlname, itemname, new_itemname)");
		methods.put("updateConfItem", "Update configuration item (params: xmlname, itemname, xml)");
		methods.put("deleteConfItem", "Delete configuration item (params: xmlname, itemname)");
		methods.put("getTables", "Get database tables list (params: db, filter, output[xml/json])");
		methods.put("getTable", "Get table structure details (params: db, tablename, output[xml/json])");
		methods.put("getTableData",
				"Get table data with pagination (params: db, tablename, [where, page, pagesize, pk, output])");
		methods.put("createBusinessXml",
				"Create and save business XML from table (params: db, tablename, frametype, operationtype, xmlname, itemname, [admid])");
		methods.put("previewBusinessXml",
				"Preview business XML without saving (params: db, tablename, frametype, operationtype, xmlname, [output, scriptpath])");
		methods.put("getXmlFile",
				"Read XML file content directly from storage (params: xmlname, [output, scriptpath])");
		methods.put("validateSql",
				"Validate SQL syntax against database (params: db, sql)");

		result.put("methods", methods);

		JSONObject params = new JSONObject();
		params.put("xmlname", "Configuration file name (e.g., ewa/m)");
		params.put("itemname", "Configuration item name");
		params.put("output", "Output format: xml, json, or csv");
		params.put("xml", "XML content for update");
		params.put("sql", "SQL statement(s) to validate (supports multiple statements separated by ;)");
		params.put("new_itemname", "New item name for copy operation");
		params.put("db", "Database connection name (defined in ewa_conf.xml)");
		params.put("tablename", "Table name");
		params.put("filter", "Table name filter (supports % wildcard)");
		params.put("where", "WHERE clause for getTableData (without WHERE keyword)");
		params.put("page", "Page number for getTableData (default 1)");
		params.put("pagesize", "Page size for getTableData (default 10, max 100)");
		params.put("pk", "Primary key field for pagination");
		params.put("frametype", "Frame type for business XML: ListFrame, Frame, or Tree");
		params.put("operationtype", "Operation type for business XML: N (new), M (modify), V (view), NM (new+modify)");
		params.put("admid", "Admin ID for business XML creation (optional, uses authenticated user if not provided)");
		params.put("scriptpath", "Optional script path name for createBusinessXml/previewBusinessXml (e.g., \"pf\"). If omitted, auto-creates in first writable path.");

		result.put("parameters", params);
		result.put("authentication", ApiTokenValidator.getSignatureAlgorithmDoc());

		return result;
	}

	/**
	 * Get IUpdateXml instance for the given xmlName
	 */
	private IUpdateXml getUpdateXml(String xmlName) {
		try {
			IConfig configType = UserConfig.getConfig(xmlName, null);
			if (configType == null) {
				return null;
			}

			ConfScriptPath scriptPath = configType.getScriptPath();
			if (scriptPath == null) {
				return null;
			}
			if (scriptPath.isResources()) {
				throw new UnsupportedOperationException(
						"Resource-based configurations are read-only and do not support updates");
			} else if (scriptPath.isReadOnly()) {
				throw new UnsupportedOperationException("This configuration is read-only and does not support updates");
			} else if (scriptPath.isJdbc()) {
				return new UpdateXmlJdbcImpl(configType);
			} else {
				return new UpdateXmlImpl(configType);
			}
		} catch (Exception e) {
			LOGGER.error("Error getting UpdateXml for: " + xmlName, e);
			return null;
		}
	}

	/**
	 * Create a JSON error response
	 */
	private JSONObject createErrorResponse(String message, int code) {
		JSONObject result = new JSONObject();
		result.put("RST", false);
		result.put("ERR", message);
		result.put("CODE", code);
		return result;
	}

	@Override
	public String getServletInfo() {
		return "EWA Servlet API v2.0";
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}
}