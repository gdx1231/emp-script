package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * 业务 XML 创建入口参数
 */
public class BusinessXmlCreateParams {
    private String db;              // 数据库连接配置名
    private String tableName;       // 表名（与 selectSql/tableJson 二选一）
    private String selectSql;       // SELECT 语句（与 tableName/tableJson 二选一）
    private JSONObject tableJson;   // 表 JSON 对象（与 tableName/selectSql 二选一）
    private String frameType;       // Frame 类型 (Frame, ListFrame, Tree)
    private String operationType;   // 操作类型 (NM, V, M)
    private String outputPath;      // 输出路径或 XML 名称

    // 构造方法 - 使用表名
    public BusinessXmlCreateParams(String db, String tableName, String frameType, String operationType) {
        this.db = db;
        this.tableName = tableName;
        this.frameType = frameType;
        this.operationType = operationType;
        this.outputPath = generateOutputPath();
    }

    // 构造方法 - 使用 SELECT 语句
    public BusinessXmlCreateParams(String db, String selectSql, String frameType, String operationType) {
        this.db = db;
        this.selectSql = selectSql;
        this.frameType = frameType;
        this.operationType = operationType;
        this.tableName = SqlValidator.extractTableNameFromSql(selectSql);
        this.outputPath = generateOutputPath();
    }

    // 构造方法 - 使用表 JSON 对象
    public BusinessXmlCreateParams(String db, JSONObject tableJson, String frameType, String operationType) {
        this.db = db;
        this.tableJson = tableJson;
        this.frameType = frameType;
        this.operationType = operationType;
        this.tableName = extractTableNameFromJson(tableJson);
        this.outputPath = generateOutputPath();
    }

    // 验证参数
    public boolean validate() {
        // tableName、selectSql、tableJson 必须三选一
        int count = 0;
        if (tableName != null && !tableName.trim().isEmpty())
            count++;
        if (selectSql != null && !selectSql.trim().isEmpty())
            count++;
        if (tableJson != null)
            count++;

        if (count != 1) {
            return false;
        }

        // 如果提供了 selectSql，检查是否为 SELECT 语句
        if (selectSql != null && !SqlValidator.checkIsSelect(selectSql)) {
            return false;
        }

        // 如果提供了 tableJson，检查是否为有效的 JSON 对象
        if (tableJson != null && !tableJson.has("TableName")) {
            return false;
        }

        return true;
    }

    // 生成输出路径
    private String generateOutputPath() {
        // 格式：/bussiness/group/op.xml
        String groupPath = "/bussiness/";
        String opName = tableName + "." + getFrameTypeShort() + "." + operationType;
        return groupPath + opName + ".xml";
    }

    // Frame 类型简写
    private String getFrameTypeShort() {
        switch (frameType.toUpperCase()) {
            case "FRAME":
                return "F";
            case "LISTFRAME":
                return "LF";
            case "TREE":
                return "T";
            default:
                return frameType;
        }
    }

    // 从 tableJson 中提取表名
    private String extractTableNameFromJson(JSONObject tableJson) {
        if (tableJson != null && tableJson.has("TableName")) {
            return tableJson.getString("TableName");
        }
        return "UNKNOWN_TABLE";
    }

    // Getters
    public String getDb() {
        return db;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public JSONObject getTableJson() {
        return tableJson;
    }

    public String getFrameType() {
        return frameType;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getFrameTypeShort() {
        return getFrameTypeShort();
    }
}
