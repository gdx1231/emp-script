package com.gdxsoft.easyweb.script.restful;

import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RESTful API 文档生成工具类
 */
public class ApiDocumentation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDocumentation.class);

    /**
     * 获取所有 API 端点的完整描述
     * 
     * @return JSON 对象包含 API 文档
     */
    public static JSONObject getApiDocumentation() {
        JSONObject result = new JSONObject();
        try {
            JSONArray endpoints = new JSONArray();
            ConfRestfuls confs = ConfRestfuls.getInstance();
            
            if (confs.isJdbc()) {
                addJdbcEndpoints(endpoints);
            } else {
                addEwaConfEndpoints(endpoints);
            }
            
            result.put("success", true);
            result.put("endpoints", endpoints);
            result.put("generatedAt", new java.util.Date().toString());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            LOGGER.error("Error generating help documents", e);
        }
        
        return result;
    }

    /**
     * 获取 HTML 格式的 API 文档
     */
    public static String getApiDocumentationHtml() {
        JSONObject doc = getApiDocumentation();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>API Documentation</title>\n");
        html.append("<style>\n");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; ");
        html.append("line-height: 1.6; color: #333; background: #f5f7fa; padding: 20px; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; }\n");
        html.append("h1 { font-size: 24px; margin-bottom: 8px; color: #1a1a2e; }\n");
        html.append(".meta { color: #888; font-size: 13px; margin-bottom: 24px; }\n");
        html.append(".endpoint { background: #fff; border-radius: 8px; margin-bottom: 16px; ");
        html.append("box-shadow: 0 1px 3px rgba(0,0,0,0.08); overflow: hidden; }\n");
        html.append(".endpoint-header { padding: 16px 20px; cursor: pointer; display: flex; ");
        html.append("align-items: center; gap: 12px; border-bottom: 1px solid #eee; }\n");
        html.append(".endpoint-header:hover { background: #fafbfc; }\n");
        html.append(".method { font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: 4px; ");
        html.append("color: #fff; min-width: 60px; text-align: center; }\n");
        html.append(".method-GET { background: #61affe; }\n");
        html.append(".method-POST { background: #49cc90; }\n");
        html.append(".method-PUT { background: #fca130; }\n");
        html.append(".method-PATCH { background: #50e3c2; }\n");
        html.append(".method-DELETE { background: #f93e3e; }\n");
        html.append(".path { font-family: monospace; font-size: 14px; color: #1a1a2e; }\n");
        html.append(".name { color: #666; font-size: 13px; margin-left: auto; }\n");
        html.append(".endpoint-body { padding: 16px 20px; display: none; }\n");
        html.append(".endpoint.open .endpoint-body { display: block; }\n");
        html.append(".section-title { font-size: 14px; font-weight: 600; margin: 12px 0 8px; color: #555; }\n");
        html.append(".info-grid { display: grid; grid-template-columns: 120px 1fr; gap: 4px 12px; ");
        html.append("font-size: 13px; margin-bottom: 16px; }\n");
        html.append(".info-label { color: #888; }\n");
        html.append(".info-value { font-family: monospace; color: #333; word-break: break-all; }\n");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 13px; }\n");
        html.append("th { text-align: left; padding: 8px 10px; background: #f0f2f5; ");
        html.append("font-weight: 600; color: #555; border-bottom: 2px solid #ddd; }\n");
        html.append("td { padding: 8px 10px; border-bottom: 1px solid #eee; vertical-align: top; }\n");
        html.append("td:first-child { font-family: monospace; font-weight: 600; color: #1a1a2e; }\n");
        html.append(".required { color: #f93e3e; font-size: 11px; }\n");
        html.append(".tag { font-size: 11px; color: #888; background: #f0f2f5; padding: 1px 6px; border-radius: 3px; }\n");
        html.append(".arrow { transition: transform 0.2s; font-size: 12px; color: #aaa; }\n");
        html.append(".endpoint.open .arrow { transform: rotate(90deg); }\n");
        html.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");
        html.append("<h1>API Documentation</h1>\n");

        String generatedAt = doc.optString("generatedAt", "");
        html.append("<div class=\"meta\">Generated at: ").append(escapeHtml(generatedAt)).append("</div>\n");

        JSONArray endpoints = doc.optJSONArray("endpoints");
        if (endpoints != null) {
            for (int i = 0; i < endpoints.length(); i++) {
                JSONObject ep = endpoints.getJSONObject(i);
                renderEndpointHtml(ep, html);
            }
        }

        html.append("</div>\n");
        html.append("<script>\n");
        html.append("document.querySelectorAll('.endpoint-header').forEach(function(h){\n");
        html.append("  h.addEventListener('click',function(){ h.parentElement.classList.toggle('open'); });\n");
        html.append("});\n");
        html.append("</script>\n");
        html.append("</body>\n</html>");
        return html.toString();
    }

    private static void renderEndpointHtml(JSONObject ep, StringBuilder html) {
        String method = ep.optString("method", "GET");
        String path = ep.optString("path", "");
        String name = ep.optString("name", "");
        String nameEn = ep.optString("nameEn", "");
        String memo = ep.optString("memo", "");
        String memoEn = ep.optString("memoEn", "");
        String xmlName = ep.optString("xmlName", "");
        String itemName = ep.optString("itemName", "");
        String parameters = ep.optString("parameters", "");

        html.append("<div class=\"endpoint\">\n");
        html.append("<div class=\"endpoint-header\">\n");
        html.append("<span class=\"arrow\">&#9654;</span>\n");
        html.append("<span class=\"method method-").append(escapeHtml(method)).append("\">")
            .append(escapeHtml(method)).append("</span>\n");
        html.append("<span class=\"path\">").append(escapeHtml(path)).append("</span>\n");
        if (!name.isEmpty()) {
            html.append("<span class=\"name\">").append(escapeHtml(name)).append("</span>\n");
        }
        html.append("</div>\n");

        html.append("<div class=\"endpoint-body\">\n");

        // Basic info
        html.append("<div class=\"info-grid\">\n");
        if (!nameEn.isEmpty()) {
            html.append("<span class=\"info-label\">Name (EN)</span><span class=\"info-value\">")
                .append(escapeHtml(nameEn)).append("</span>\n");
        }
        if (!memo.isEmpty()) {
            html.append("<span class=\"info-label\">Description</span><span class=\"info-value\">")
                .append(escapeHtml(memo)).append("</span>\n");
        }
        if (!memoEn.isEmpty()) {
            html.append("<span class=\"info-label\">Desc (EN)</span><span class=\"info-value\">")
                .append(escapeHtml(memoEn)).append("</span>\n");
        }
        html.append("<span class=\"info-label\">XML</span><span class=\"info-value\">")
            .append(escapeHtml(xmlName)).append("</span>\n");
        html.append("<span class=\"info-label\">Item</span><span class=\"info-value\">")
            .append(escapeHtml(itemName)).append("</span>\n");
        if (!parameters.isEmpty()) {
            html.append("<span class=\"info-label\">Parameters</span><span class=\"info-value\">")
                .append(escapeHtml(parameters)).append("</span>\n");
        }
        html.append("</div>\n");

        // Fields table
        JSONArray fields = ep.optJSONArray("fields");
        if (fields != null && fields.length() > 0) {
            html.append("<div class=\"section-title\">Fields</div>\n");
            html.append("<table>\n<thead><tr>");
            html.append("<th>Name</th><th>Tag</th><th>Description</th><th>Data Field</th><th>Type</th><th>Required</th>");
            html.append("</tr></thead>\n<tbody>\n");

            for (int i = 0; i < fields.length(); i++) {
                JSONObject f = fields.getJSONObject(i);
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(f.optString("name", ""))).append("</td>");
                html.append("<td><span class=\"tag\">").append(escapeHtml(f.optString("tag", ""))).append("</span></td>");

                JSONObject desc = f.optJSONObject("description");
                String descZhcn = "";
                if (desc != null) {
                    JSONObject zhcn = desc.optJSONObject("zhcn");
                    if (zhcn != null) {
                        descZhcn = zhcn.optString("info", "");
                    }
                }
                html.append("<td>").append(escapeHtml(descZhcn)).append("</td>");

                JSONObject dataItem = f.optJSONObject("dataItem");
                String dataField = dataItem != null ? dataItem.optString("field", "") : "";
                String dataType = dataItem != null ? dataItem.optString("type", "") : "";
                html.append("<td>").append(escapeHtml(dataField)).append("</td>");
                html.append("<td>").append(escapeHtml(dataType)).append("</td>");

                boolean required = f.optBoolean("isRequired", false);
                html.append("<td>").append(required ? "<span class=\"required\">Required</span>" : "").append("</td>");
                html.append("</tr>\n");
            }
            html.append("</tbody>\n</table>\n");
        }

        // Fields error
        String fieldsError = ep.optString("fieldsError", "");
        if (!fieldsError.isEmpty()) {
            html.append("<div style=\"color:#f93e3e;font-size:13px;margin-top:8px;\">")
                .append(escapeHtml(fieldsError)).append("</div>\n");
        }

        html.append("</div>\n</div>\n");
    }

    private static String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * 从数据库加载所有 RESTful 配置并添加到文档中
     */
    private static void addJdbcEndpoints(JSONArray endpoints) {
        try {
            String sqlCatalog = "SELECT * FROM ewa_restful_catalog WHERE cat_status='USED' ORDER BY cat_path_full";
            RequestValue rvCatalog = new RequestValue();
            DTTable tbCatalog = DTTable.getJdbcTable(sqlCatalog, ConfRestfuls.getInstance().getDataSource(), rvCatalog);
            
            for (int i = 0; i < tbCatalog.getCount(); i++) {
                DTRow catalogRow = tbCatalog.getRow(i);
                String catUid = catalogRow.getCell("cat_uid").toString();
                
                String sqlMethods = "SELECT * FROM ewa_restful WHERE cat_uid = @cat_uid AND rs_status='USED' ORDER BY rs_method";
                RequestValue rvMethods = new RequestValue();
                rvMethods.addOrUpdateValue("cat_uid", catUid);
                DTTable tbMethods = DTTable.getJdbcTable(sqlMethods, ConfRestfuls.getInstance().getDataSource(), rvMethods);
                
                for (int j = 0; j < tbMethods.getCount(); j++) {
                    DTRow methodRow = tbMethods.getRow(j);
                    JSONObject endpoint = createEndpointDescription(methodRow, catalogRow);
                    endpoints.put(endpoint);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error adding JDBC endpoints", e);
        }
    }

    /**
     * 从 XML 配置加载所有 RESTful 配置并添加到文档中
     */
    private static void addEwaConfEndpoints(JSONArray endpoints) {
        try {
            LOGGER.warn("EwaConf endpoints documentation not implemented yet");
        } catch (Exception e) {
            LOGGER.error("Error adding EwaConf endpoints", e);
        }
    }

    /**
     * 创建单个端点的描述
     */
    private static JSONObject createEndpointDescription(DTRow methodRow, DTRow catalogRow) throws Exception {
        JSONObject endpoint = new JSONObject();
        
        endpoint.put("path", catalogRow.getCell("cat_path_full") != null ? catalogRow.getCell("cat_path_full").toString() : "");
        endpoint.put("method", methodRow.getCell("rs_method") != null ? methodRow.getCell("rs_method").toString() : "");
        endpoint.put("name", methodRow.getCell("rs_name") != null ? methodRow.getCell("rs_name").toString() : "");
        endpoint.put("nameEn", methodRow.getCell("rs_name_en") != null ? methodRow.getCell("rs_name_en").toString() : "");
        endpoint.put("memo", methodRow.getCell("rs_memo") != null ? methodRow.getCell("rs_memo").toString() : "");
        endpoint.put("memoEn", methodRow.getCell("rs_memo_en") != null ? methodRow.getCell("rs_memo_en").toString() : "");
        endpoint.put("xmlName", methodRow.getCell("rs_xmlname") != null ? methodRow.getCell("rs_xmlname").toString() : "");
        endpoint.put("itemName", methodRow.getCell("rs_itemname") != null ? methodRow.getCell("rs_itemname").toString() : "");
        endpoint.put("parameters", methodRow.getCell("rs_parameters") != null ? methodRow.getCell("rs_parameters").toString() : "");
        
        String xmlName = endpoint.optString("xmlName");
        String itemName = endpoint.optString("itemName");
        String method = endpoint.optString("method");

        if (!xmlName.isEmpty() && !itemName.isEmpty()) {
            if ("DELETE".equalsIgnoreCase(method)) {
                addDeleteSqlParameters(endpoint, xmlName, itemName);
            } else {
                addXItemFields(endpoint, xmlName, itemName);
            }
        }
        
        return endpoint;
    }

    /**
     * 加载 XML 配置并添加字段信息到端点描述中
     */
    private static void addXItemFields(JSONObject endpoint, String xmlName, String itemName) {
        try {
            UserConfig uc = UserConfig.instance(xmlName, itemName, null);
            UserXItems items = uc.getUserXItems();
            
            JSONArray fields = new JSONArray();
            
            for (int i = 0; i < items.count(); i++) {
                UserXItem item = items.getItem(i);

                String tag = item.getSingleValue("Tag");
                if ("button".equalsIgnoreCase(tag) || "submit".equalsIgnoreCase(tag)) {
                    continue;
                }

                JSONObject field = new JSONObject();

                field.put("name", item.getName());
                field.put("tag", tag);
                
                String desInfoZhcn = item.getItemValue("DescriptionSet", "Lang=zhcn", "Info");
                String desInfoEnus = item.getItemValue("DescriptionSet", "Lang=enus", "Info");
                String desMemoZhcn = item.getItemValue("DescriptionSet", "Lang=zhcn", "Memo");
                String desMemoEnus = item.getItemValue("DescriptionSet", "Lang=enus", "Memo");
                field.put("description", new JSONObject()
                    .put("zhcn", new JSONObject()
                        .put("info", desInfoZhcn)
                        .put("memo", desMemoZhcn))
                    .put("enus", new JSONObject()
                        .put("info", desInfoEnus)
                        .put("memo", desMemoEnus)));
                
                String dataField = item.getSingleValue("DataItem", "DataField");
                String dataType = item.getSingleValue("DataItem", "DataType");
                String dataFormat = item.getSingleValue("DataItem", "Format");
                JSONObject dataItem = new JSONObject()
                    .put("field", dataField)
                    .put("type", dataType)
                    .put("format", dataFormat);
                field.put("dataItem", dataItem);
                
                String isMustInput = item.getSingleValue("IsMustInput");
                field.put("isRequired", "1".equals(isMustInput) || "true".equalsIgnoreCase(isMustInput));
                
                String maxLength = item.getSingleValue("MaxMinLength", "MaxLength");
                String minLength = item.getSingleValue("MaxMinLength", "MinLength");
                JSONObject length = new JSONObject()
                    .put("max", maxLength)
                    .put("min", minLength);
                field.put("length", length);
                
                String listSql = item.getSingleValue("List", "Sql");
                String listDisplayField = item.getSingleValue("List", "DisplayField");
                String listValueField = item.getSingleValue("List", "ValueField");
                JSONObject listConfig = new JSONObject()
                    .put("sql", listSql)
                    .put("displayField", listDisplayField)
                    .put("valueField", listValueField);
                field.put("listConfig", listConfig);
                
                fields.put(field);
            }
            
            endpoint.put("fields", fields);

        } catch (Exception e) {
            LOGGER.warn("Error loading XML config for {}: {}", xmlName + "/" + itemName, e.getMessage());
            endpoint.put("fieldsError", e.getMessage());
        }
    }

    /**
     * 为 DELETE 端点从 SQL 中提取参数（而非从 XItem 字段）
     * 遍历 SqlSet 中包含 DELETE 语句的 SQL，提取 @param 占位符
     */
    private static void addDeleteSqlParameters(JSONObject endpoint, String xmlName, String itemName) {
        try {
            UserConfig uc = UserConfig.instance(xmlName, itemName, null);
            UserXItem actionItem = uc.getUserActionItem();
            if (actionItem == null || !actionItem.testName("SqlSet")) {
                LOGGER.warn("No SqlSet found for DELETE endpoint {}/{}", xmlName, itemName);
                return;
            }

            UserXItemValues sqlSets = actionItem.getItem("SqlSet");
            List<String> paramNames = new ArrayList<>();
            String deleteSql = null;

            for (int i = 0; i < sqlSets.count(); i++) {
                UserXItemValue sqlItem = sqlSets.getItem(i);
                if (!sqlItem.testName("Sql")) {
                    continue;
                }
                String sql = sqlItem.getItem("Sql");
                if (sql != null && sql.toUpperCase().contains("DELETE")) {
                    deleteSql = sql;
                    paramNames.addAll(extractSqlParameters(sql));
                    break;
                }
            }

            // 未找到 DELETE SQL，回退使用 OnFrameDelete
            if (deleteSql == null) {
                for (int i = 0; i < sqlSets.count(); i++) {
                    UserXItemValue sqlItem = sqlSets.getItem(i);
                    if (!sqlItem.testName("Sql")) {
                        continue;
                    }
                    String sql = sqlItem.getItem("Sql");
                    if (sql != null && sql.toUpperCase().contains("WHERE")) {
                        deleteSql = sql;
                        paramNames.addAll(extractSqlParameters(sql));
                        break;
                    }
                }
            }

            if (deleteSql != null) {
                endpoint.put("sql", deleteSql.trim());
            }

            if (!paramNames.isEmpty()) {
                JSONArray fields = new JSONArray();
                for (String param : paramNames) {
                    JSONObject field = new JSONObject();
                    field.put("name", param);
                    field.put("source", "sql");
                    field.put("isRequired", true);
                    fields.put(field);
                }
                endpoint.put("fields", fields);
            }
        } catch (Exception e) {
            LOGGER.warn("Error extracting DELETE SQL params for {}: {}", xmlName + "/" + itemName, e.getMessage());
            endpoint.put("fieldsError", e.getMessage());
        }
    }

    /**
     * 从 SQL 文本中提取 @paramName 参数
     */
    private static List<String> extractSqlParameters(String sql) {
        List<String> params = new ArrayList<>();
        Pattern pattern = Pattern.compile("@([A-Za-z_][A-Za-z0-9_]*)");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String param = matcher.group(1);
            if (!params.contains(param)) {
                params.add(param);
            }
        }
        return params;
    }
}