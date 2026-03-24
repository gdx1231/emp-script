package com.gdxsoft.easyweb.define.servlets;

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
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UpdateXmlImpl;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.Tables;
import com.gdxsoft.easyweb.define.servlets.ApiTokenValidator.ValidationResult;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * Servlet API for configuration management
 * 
 * Methods:
 * - getConfXml: Get full XML configuration file
 * - getConfItem: Get specific configuration item
 * - runConfItem: Copy configuration item
 * - updateConfItem: Update configuration item
 * - login: Generate API token
 * - logout: Revoke API token
 * 
 * Authentication:
 * - HMAC Signature (recommended for server-to-server)
 * - JWT Token (for client sessions)
 * - Simple Token (legacy, not recommended)
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
     * @param rv RequestValue containing parameters
     *           - xmlname: configuration name
     *           - output: output format (xml/json), default xml
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
     * @param rv RequestValue containing parameters
     *           - xmlname: configuration name
     *           - itemname: item name
     *           - output: output format (xml/json), default xml
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
     * @param rv RequestValue containing parameters
     *           - db: database connection name
     *           - filter: table name filter (optional, supports % wildcard)
     *           - output: output format (xml/json), default xml
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
                        xml.append(" type=\"").append(table.getTableType() != null ? table.getTableType().trim() : "TABLE").append("\"");
                        xml.append(" schema=\"").append(table.getSchemaName() != null ? table.getSchemaName() : "").append("\"");
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
     * @param rv RequestValue containing parameters
     *           - db: database connection name
     *           - tablename: table name
     *           - output: output format (xml/json), default xml
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
     * @param rv RequestValue containing parameters
     *           - db: database connection name
     *           - tablename: table name
     *           - where: optional WHERE clause (without the WHERE keyword)
     *           - page: page number (default 1)
     *           - pagesize: page size (default 10, max 100)
     *           - pk: primary key field (optional, for pagination)
     *           - output: output format (xml/json/csv), default json
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
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
        if (pageSize > MAX_PAGE_SIZE) pageSize = MAX_PAGE_SIZE;

        try {
            // 构建 SQL 查询
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(tableName);

            // 添加可选的 WHERE 条件
            if (StringUtils.isNotBlank(whereClause)) {
                // 简单的 SQL 注入防护：移除危险关键字
                String sanitizedWhere = whereClause.replaceAll("(?i)(DROP|DELETE|TRUNCATE|ALTER|CREATE|INSERT|UPDATE)", "");
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
        methods.put("getTableData", "Get table data with pagination (params: db, tablename, [where, page, pagesize, pk, output])");

        result.put("methods", methods);

        JSONObject params = new JSONObject();
        params.put("xmlname", "Configuration file name (e.g., ewa/m)");
        params.put("itemname", "Configuration item name");
        params.put("output", "Output format: xml, json, or csv");
        params.put("xml", "XML content for update");
        params.put("new_itemname", "New item name for copy operation");
        params.put("db", "Database connection name (defined in ewa_conf.xml)");
        params.put("tablename", "Table name");
        params.put("filter", "Table name filter (supports % wildcard)");
        params.put("where", "WHERE clause for getTableData (without WHERE keyword)");
        params.put("page", "Page number for getTableData (default 1)");
        params.put("pagesize", "Page size for getTableData (default 10, max 100)");
        params.put("pk", "Primary key field for pagination");

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

            return new UpdateXmlImpl(configType);
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