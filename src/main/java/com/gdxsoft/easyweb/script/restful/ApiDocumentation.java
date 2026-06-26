package com.gdxsoft.easyweb.script.restful;

import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        if (!xmlName.isEmpty() && !itemName.isEmpty()) {
            addXItemFields(endpoint, xmlName, itemName);
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
                JSONObject field = new JSONObject();
                
                field.put("name", item.getName());
                
                String tag = item.getSingleValue("Tag");
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
}