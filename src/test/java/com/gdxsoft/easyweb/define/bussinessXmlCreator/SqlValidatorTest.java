package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * SqlValidator 测试
 */
public class SqlValidatorTest {

    /**
     * 测试从简单 SELECT 语句提取表名
     */
    @Test
    public void testExtractTableName_simple() {
        String sql = "SELECT * FROM CRM_COM WHERE CRM_COM_ID > 0";
        String tableName = SqlValidator.extractTableNameFromSql(sql);
        assertEquals("CRM_COM", tableName);
    }

    /**
     * 测试从 schema.table 格式提取表名
     */
    @Test
    public void testExtractTableName_withSchema() {
        String sql = "SELECT * FROM dbo.CRM_COM WHERE CRM_COM_ID > 0";
        String tableName = SqlValidator.extractTableNameFromSql(sql);
        assertEquals("CRM_COM", tableName);
    }

    /**
     * 测试从 WITH 语句提取表名
     */
    @Test
    public void testExtractTableName_withWith() {
        String sql = "WITH t1 AS (SELECT * FROM CRM_COM) SELECT * FROM t1";
        String tableName = SqlValidator.extractTableNameFromSql(sql);
        // 从主查询提取，应该是 t1
        assertEquals("t1", tableName);
    }

    /**
     * 测试从多表关联提取表名
     */
    @Test
    public void testExtractTableName_withJoin() {
        String sql = "SELECT a.*, b.CRM_COM_NAME FROM CRM_CUS a INNER JOIN CRM_COM b ON a.CRM_COM_ID = b.CRM_COM_ID";
        String tableName = SqlValidator.extractTableNameFromSql(sql);
        // 提取第一个表
        assertEquals("CRM_CUS", tableName);
    }

    /**
     * 测试检查 SELECT 语句
     */
    @Test
    public void testCheckIsSelect() {
        assertTrue(SqlUtils.checkIsSelect("SELECT * FROM table"));
        assertTrue(SqlUtils.checkIsSelect("  SELECT * FROM table"));
        assertTrue(SqlUtils.checkIsSelect("-- comment\nSELECT * FROM table"));
    }

    /**
     * 测试检查非 SELECT 语句
     */
    @Test
    public void testCheckIsNotSelect() {
        assertFalse(SqlUtils.checkIsSelect("INSERT INTO table VALUES (1)"));
        assertFalse(SqlUtils.checkIsSelect("UPDATE table SET col=1"));
        assertFalse(SqlUtils.checkIsSelect("DELETE FROM table"));
    }
}
