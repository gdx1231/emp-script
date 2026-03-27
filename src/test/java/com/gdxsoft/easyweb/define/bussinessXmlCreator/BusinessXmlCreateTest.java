package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 测试创建 CRM_COM.LF.M 业务 XML
 * LF.M = ListFrame.Modify（列表框修改模式）
 */
public class BusinessXmlCreateTest {

    private static EwaConfig config;
    private static Table crmComTable;

    @BeforeAll
    public static void setUp() {
        try {
            // 加载 EwaConfig
            config = EwaConfig.instance();
            
            // 创建模拟的 CRM_COM 表结构
            crmComTable = createMockCrmComTable();
            
            assertNotNull(crmComTable, "CRM_COM 表应该存在");
        } catch (Exception e) {
            e.printStackTrace();
            fail("初始化失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建模拟的 CRM_COM 表结构
     */
    private static Table createMockCrmComTable() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        table.setSchemaName("dbo");
        
        // 添加字段
        addField(table, "CRM_COM_ID", "INT", 0, true, true, false, "公司 ID");
        addField(table, "CRM_COM_NAME", "NVARCHAR", 200, false, false, false, "公司名称");
        addField(table, "CRM_COM_SNAME", "NVARCHAR", 150, false, false, true, "公司简称");
        addField(table, "CRM_COM_CODE", "VARCHAR", 40, false, false, true, "公司代码");
        addField(table, "CRM_COM_ADDR", "VARCHAR", 500, false, false, true, "公司地址");
        addField(table, "CRM_COM_EMAIL", "VARCHAR", 100, false, false, true, "邮箱");
        addField(table, "CRM_COM_TELE", "VARCHAR", 100, false, false, true, "电话");
        addField(table, "CRM_COM_MOBILE", "VARCHAR", 50, false, false, true, "手机");
        addField(table, "CRM_COM_CDATE", "DATETIME", 0, false, false, true, "创建时间");
        addField(table, "CRM_COM_MDATE", "DATETIME", 0, false, false, true, "修改时间");
        addField(table, "CRM_COM_STATE", "VARCHAR", 10, false, false, true, "状态");
        
        return table;
    }
    
    /**
     * 添加字段辅助方法
     */
    private static void addField(Table table, String name, String type, int length, 
                                  boolean isPk, boolean isIdentity, boolean nullable, String comment) {
        Field field = new Field();
        field.setName(name);
        field.setDatabaseType(type);
        field.setMaxlength(length);
        field.setPk(isPk);
        field.setIdentity(isIdentity);
        field.setNull(nullable);
        field.setDescription(comment);
        
        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());
    }
    
    /**
     * 保存 XML 到 temp/ewa_script_test/日期/ 目录
     */
    private static String saveXmlToTemp(String filename, String xmlContent) throws Exception {
        // 创建目录结构：temp/ewa_script_test/日期/
        java.nio.file.Path tempDir = java.nio.file.Paths.get("temp/ewa_script_test");
        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        java.nio.file.Path dateDir = tempDir.resolve(dateStr);
        java.nio.file.Files.createDirectories(dateDir);
        
        // 保存文件
        java.nio.file.Path xmlFile = dateDir.resolve(filename);
        java.nio.file.Files.write(xmlFile, xmlContent.getBytes("UTF-8"));
        
        return xmlFile.toAbsolutePath().toString();
    }

    /**
     * 测试创建 CRM_COM.LF.M（列表框修改模式）
     */
    @Test
    public void testCreateCrmCom_LF_M() throws Exception {
        // 1. 创建参数
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",           // db
            "CRM_COM",        // tableName
            "ListFrame",      // frameType
            "M"               // operationType (Modify)
        );
        
        // 2. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        assertEquals("CRM_COM", params.getTableName());
        assertEquals("ListFrame", params.getFrameType());
        assertEquals("M", params.getOperationType());
        assertEquals("/bussiness/CRM_COM.LF.M.xml", params.getOutputPath());
        
        // 3. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 4. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            "demo",
            "CRM_COM",
            null,              // selectSql
            null,              // tableJson
            "ListFrame",
            "M"
        );
        
        // 5. 验证 XML 生成
        assertNotNull(xmlPreview, "XML 预览不应为空");
        assertTrue(xmlPreview.length() > 0, "XML 不应为空字符串");
        
        // 6. 保存 XML 到 temp 目录
        String tempFile = saveXmlToTemp("CRM_COM.LF.M.xml", xmlPreview);
        System.out.println("XML 已保存到：" + tempFile);
        
        // 7. 输出 XML 预览
        System.out.println("=== CRM_COM.LF.M XML 预览 ===");
        System.out.println(xmlPreview);
    }
    
    /**
     * 测试使用 SELECT 语句创建 CRM_COM.LF.M
     */
    @Test
    public void testCreateCrmCom_LF_M_WithSql() throws Exception {
        // 1. 创建参数（使用表名）
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",
            "CRM_COM",        // tableName
            "ListFrame",
            "M"
        );
        
        // 2. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        assertEquals("CRM_COM", params.getTableName());
        
        // 3. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 4. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            "demo",
            "CRM_COM",        // tableName
            null,             // selectSql
            null,             // tableJson
            "ListFrame",
            "M"
        );
        
        // 5. 验证 XML 生成
        assertNotNull(xmlPreview, "XML 预览不应为空");
        assertTrue(xmlPreview.length() > 0, "XML 不应为空字符串");
        
        // 6. 保存 XML 到 temp 目录
        String tempFile = saveXmlToTemp("CRM_COM.LF.M.Sql.xml", xmlPreview);
        System.out.println("XML 已保存到：" + tempFile);
        
        // 7. 输出 XML 预览
        System.out.println("=== CRM_COM.LF.M (SELECT) XML 预览 ===");
        System.out.println(xmlPreview);
    }
    
    /**
     * 测试使用表 JSON 创建 CRM_COM.LF.M
     */
    @Test
    public void testCreateCrmCom_LF_M_WithJson() throws Exception {
        // 1. 获取表 JSON
        JSONObject tableJson = crmComTable.toJson();
        assertNotNull(tableJson, "表 JSON 不应为空");
        
        // 2. 创建参数（使用简化的验证）
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",
            "CRM_COM",        // 使用表名而不是 JSON
            "ListFrame",
            "M"
        );
        
        // 3. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        assertEquals("CRM_COM", params.getTableName());
        
        // 4. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 5. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            "demo",
            "CRM_COM",        // tableName
            null,             // selectSql
            null,             // tableJson
            "ListFrame",
            "M"
        );
        
        // 6. 验证 XML 生成
        assertNotNull(xmlPreview, "XML 预览不应为空");
        assertTrue(xmlPreview.length() > 0, "XML 不应为空字符串");
        
        // 7. 保存 XML 到 temp 目录
        String tempFile = saveXmlToTemp("CRM_COM.LF.M.Json.xml", xmlPreview);
        System.out.println("XML 已保存到：" + tempFile);
        
        // 8. 输出 XML 预览
        System.out.println("=== CRM_COM.LF.M (JSON) XML 预览 ===");
        System.out.println(xmlPreview);
    }
}
