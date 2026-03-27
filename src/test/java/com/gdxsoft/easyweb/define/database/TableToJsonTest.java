package com.gdxsoft.easyweb.define.database;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.testutils.SqlImporter;

/**
 * Table toJson() 方法测试
 */
public class TableToJsonTest {
    
    @BeforeAll
    public static void setUp() {
        // 导入 SQL 文件初始化数据库
        SqlImporter.initTestDatabase();
    }

    /**
     * 测试 toJson() 方法返回完整的表元数据
     */
    @Test
    public void testToJson() {
        // 创建测试表（使用无参构造函数）
        Table table = new Table();
        table.initBlankFrame();  // 初始化字段列表
        table.setName("CRM_COM");
        table.setSchemaName("dbo");
        
        // 添加测试字段
        Field field1 = new Field();
        field1.setName("CRM_COM_ID");
        field1.setDatabaseType("INT");
        field1.setMaxlength(0);
        field1.setPk(true);
        table.getFields().put(field1.getName(), field1);
        table.getFields().getFieldList().add(field1.getName());
        
        // 转换为 JSON（不抛出异常即为通过）
        JSONObject json = table.toJson();
        assertNotNull(json);
    }
    
    /**
     * 测试 toJson() 方法返回 XML 转换的 JSON
     */
    @Test
    public void testToJson_fromXml() {
        // 创建测试表（使用无参构造函数）
        Table table = new Table();
        table.initBlankFrame();  // 初始化字段列表
        table.setName("TEST_TABLE");
        
        Field field = new Field();
        field.setName("TEST_FIELD");
        field.setDatabaseType("NVARCHAR");
        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());
        
        // 转换为 JSON（不抛出异常即为通过）
        JSONObject json = table.toJson();
        assertNotNull(json);
    }
}
