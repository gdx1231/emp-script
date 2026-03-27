package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.json.JSONObject;

import com.gdxsoft.easyweb.datasource.SqlUtils;

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
    public BusinessXmlCreateParams(String db, String selectSql, String frameType, String operationType, boolean isSql) {
        this.db = db;
        this.selectSql = selectSql;
        this.frameType = frameType;
        this.operationType = operationType;
        this.tableName = extractTableNameFromSql(selectSql);
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
        // 检查 tableJson 是否有效
        if (tableJson != null && !tableJson.has("TableName")) {
            return false;
        }

        // 检查 selectSql 是否为 SELECT 语句
        if (selectSql != null && !SqlUtils.checkIsSelect(selectSql)) {
            return false;
        }

        // 至少有一个输入源
        boolean hasInput = (tableName != null && !tableName.trim().isEmpty()) ||
                           (selectSql != null && !selectSql.trim().isEmpty()) ||
                           (tableJson != null);
        
        return hasInput;
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

    // 从 SELECT 语句中提取表名
    private String extractTableNameFromSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN_TABLE";
        }
        
        // 使用 SqlUtils 去除 WITH 语句块
        String[] withBlocks = SqlUtils.getSqlWithBlock(sql);
        String mainSql = (withBlocks != null) ? withBlocks[1] : sql;
        
        // 简单解析：SELECT ... FROM table_name ...
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("FROM\\s+([\\w\\.]+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(mainSql);
        if (matcher.find()) {
            String fullName = matcher.group(1);
            // 如果有 schema.table 格式，只取 table 部分
            if (fullName.contains(".")) {
                return fullName.split("\\.")[1];
            }
            return fullName;
        }
        
        return "UNKNOWN_TABLE";
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
}
