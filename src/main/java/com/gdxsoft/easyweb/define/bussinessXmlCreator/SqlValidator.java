package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.database.SqlSyntaxCheck;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * SQL 验证工具类
 */
public class SqlValidator {

    /**
     * 检查 SELECT 语句语法
     * 
     * @param db        数据库连接配置名
     * @param selectSql SELECT 语句
     * @return 检查结果 {RST: true/false, ERR: 错误信息}
     */
    public static JSONObject checkSqlSyntax(String db, String selectSql) {
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("DB", db);
        rv.addOrUpdateValue("SQL", selectSql);

        SqlSyntaxCheck syntaxCheck = new SqlSyntaxCheck(rv);
        String result = syntaxCheck.checkSyntax();

        return new JSONObject(result);
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean success = false;
        private String error;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    /**
     * 验证并获取字段元数据
     * 
     * @param db        数据库连接配置名
     * @param selectSql SELECT 语句
     * @return 验证结果
     */
    public static ValidationResult validateSelectSql(String db, String selectSql) {
        ValidationResult result = new ValidationResult();

        // 1. 使用 SqlUtils 检查是否为 SELECT 语句（支持 WITH 语句）
        if (!SqlUtils.checkIsSelect(selectSql)) {
            result.setError("不是 SELECT 语句");
            return result;
        }

        // 2. 检查 SQL 语法
        JSONObject checkResult = checkSqlSyntax(db, selectSql);

        if (!checkResult.getBoolean("RST")) {
            result.setError(checkResult.getString("ERR"));
            return result;
        }

        // 3. 执行 SELECT 获取字段元数据（使用 WHERE 1=2 不返回数据）
        String testSql = "SELECT * FROM (" + selectSql + ") temp WHERE 1=2";
        DTTable tb = DTTable.getJdbcTable(testSql, db);

        if (!tb.isOk()) {
            result.setError(tb.getErrorInfo());
            return result;
        }

        // 4. 验证字段元数据
        for (int i = 0; i < tb.getColumns().getCount(); i++) {
            DTColumn col = tb.getColumns().getColumn(i);
            // 可以添加字段验证逻辑
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 从 SELECT 语句中提取表名（支持 WITH 语句）
     * 
     * @param selectSql SELECT 语句
     * @return 表名
     */
    public static String extractTableNameFromSql(String selectSql) {
        // 1. 使用 SqlUtils 去除 WITH 语句块，获取主 SELECT 语句
        String[] withBlocks = SqlUtils.getSqlWithBlock(selectSql);
        String mainSql = (withBlocks != null) ? withBlocks[1] : selectSql;

        // 2. 从主查询中提取第一个表名
        Pattern pattern = Pattern.compile("FROM\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mainSql);
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
}
