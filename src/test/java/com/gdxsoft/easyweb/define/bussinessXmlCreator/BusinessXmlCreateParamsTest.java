package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * BusinessXmlCreateParams 测试
 */
public class BusinessXmlCreateParamsTest {

    /**
     * 测试使用表名构造参数
     */
    @Test
    public void testConstructor_withTableName() {
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "globaltravel",
            "CRM_COM",
            "Frame",
            "NM"
        );

        assertEquals("CRM_COM", params.getTableName());
        assertEquals("Frame", params.getFrameType());
        assertEquals("NM", params.getOperationType());
        assertEquals("/bussiness/CRM_COM.F.NM.xml", params.getOutputPath());
        assertTrue(params.validate());
    }

    /**
     * 测试使用 SELECT 语句构造参数
     */
    @Test
    public void testConstructor_withSelectSql() {
        String selectSql = "SELECT * FROM CRM_COM WHERE CRM_COM_ID > 0";
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "globaltravel",
            selectSql,
            "Frame",
            "NM",
            true
        );

        assertEquals("CRM_COM", params.getTableName());
        assertEquals(selectSql, params.getSelectSql());
        assertEquals("/bussiness/CRM_COM.F.NM.xml", params.getOutputPath());
        assertTrue(params.validate());
    }

    /**
     * 测试使用表 JSON 对象构造参数
     */
    @Test
    public void testConstructor_withTableJson() {
        JSONObject tableJson = new JSONObject();
        tableJson.put("TableName", "CRM_COM");
        tableJson.put("SchemaName", "dbo");

        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "globaltravel",
            tableJson,
            "Frame",
            "NM"
        );

        assertEquals("CRM_COM", params.getTableName());
        assertEquals(tableJson, params.getTableJson());
        assertEquals("/bussiness/CRM_COM.F.NM.xml", params.getOutputPath());
        assertTrue(params.validate());
    }

    /**
     * 测试 Frame 类型简写（通过 outputPath 验证）
     */
    @Test
    public void testGetFrameTypeShort() {
        BusinessXmlCreateParams params1 = new BusinessXmlCreateParams(
            "globaltravel", "CRM_COM", "Frame", "NM"
        );
        assertEquals("/bussiness/CRM_COM.F.NM.xml", params1.getOutputPath());

        BusinessXmlCreateParams params2 = new BusinessXmlCreateParams(
            "globaltravel", "CRM_COM", "ListFrame", "M"
        );
        assertEquals("/bussiness/CRM_COM.LF.M.xml", params2.getOutputPath());

        BusinessXmlCreateParams params3 = new BusinessXmlCreateParams(
            "globaltravel", "CRM_COM", "Tree", "V"
        );
        assertEquals("/bussiness/CRM_COM.T.V.xml", params3.getOutputPath());
    }

    /**
     * 测试参数验证 - 无效输入
     */
    @Test
    public void testValidate_invalid() {
        // 无输入 - 使用 null 表名构造
        BusinessXmlCreateParams params1 = new BusinessXmlCreateParams(
            "globaltravel", (String)null, "Frame", "NM"
        );
        assertFalse(params1.validate());

        // JSON 缺少 TableName
        JSONObject invalidJson = new JSONObject();
        invalidJson.put("SchemaName", "dbo");
        BusinessXmlCreateParams params2 = new BusinessXmlCreateParams(
            "globaltravel", invalidJson, "Frame", "NM"
        );
        assertFalse(params2.validate());
    }

    /**
     * 测试输出路径生成
     */
    @Test
    public void testOutputPath_generation() {
        BusinessXmlCreateParams params1 = new BusinessXmlCreateParams(
            "globaltravel", "CRM_COM", "Frame", "NM"
        );
        assertEquals("/bussiness/CRM_COM.F.NM.xml", params1.getOutputPath());

        BusinessXmlCreateParams params2 = new BusinessXmlCreateParams(
            "globaltravel", "CRM_COM", "ListFrame", "M"
        );
        assertEquals("/bussiness/CRM_COM.LF.M.xml", params2.getOutputPath());
    }
    
    /**
     * 测试从 SELECT 语句提取表名
     */
    @Test
    public void testExtractTableNameFromSql() {
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "globaltravel",
            "SELECT * FROM dbo.CRM_COM WHERE 1=1",
            "Frame",
            "NM",
            true
        );
        assertEquals("CRM_COM", params.getTableName());
    }
}
