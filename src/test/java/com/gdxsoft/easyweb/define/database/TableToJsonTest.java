package com.gdxsoft.easyweb.define.database;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

/**
 * Table toJson() 方法测试
 */
public class TableToJsonTest {

    /**
     * 测试 toJson() 方法返回完整的表元数据
     */
    @Test
    public void testToJson() {
        // 创建测试表
        Table table = new Table("CRM_COM", "dbo", "globaltravel");
        
        // 添加测试字段
        Field field1 = new Field();
        field1.setName("CRM_COM_ID");
        field1.setType("INT");
        field1.setLength(0);
        field1.setPrimaryKey(true);
        field1.setIdentity(true);
        field1.setNullable(false);
        field1.setComment("公司 ID");
        table.getFields().put(field1.getName(), field1);
        table.getFields().getFieldList().add(field1.getName());
        
        Field field2 = new Field();
        field2.setName("CRM_COM_NAME");
        field2.setType("NVARCHAR");
        field2.setLength(200);
        field2.setPrimaryKey(false);
        field2.setNullable(false);
        field2.setComment("公司名称");
        table.getFields().put(field2.getName(), field2);
        table.getFields().getFieldList().add(field2.getName());
        
        // 转换为 JSON
        JSONObject json = table.toJson();
        
        // 验证表属性
        assertNotNull(json);
        assertEquals("CRM_COM", json.getString("TableName"));
        assertEquals("dbo", json.getString("SchemaName"));
        assertEquals("globaltravel", json.getString("ConnectionConfigName"));
        
        // 验证字段列表
        assertTrue(json.has("Fields"));
        JSONObject fields = json.getJSONArray("Fields").getJSONObject(0);
        assertEquals("CRM_COM_ID", fields.getString("Name"));
        assertEquals("INT", fields.getString("Type"));
        assertTrue(fields.getBoolean("IsPrimaryKey"));
        assertTrue(fields.getBoolean("IsIdentity"));
        
        // 验证主键
        assertTrue(json.has("Pk"));
        JSONObject pk = json.getJSONObject("Pk");
        assertTrue(pk.getJSONArray("Fields").length() > 0);
        
        // 验证外键和索引
        assertTrue(json.has("Fks"));
        assertTrue(json.has("Indexes"));
    }
    
    /**
     * 测试 fieldToJson() 方法
     */
    @Test
    public void testFieldToJson() {
        Field field = new Field();
        field.setName("TEST_FIELD");
        field.setType("NVARCHAR");
        field.setLength(100);
        field.setComment("测试字段");
        field.setNullable(true);
        
        Table table = new Table("TEST_TABLE", "globaltravel");
        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());
        
        JSONObject json = table.toJson();
        JSONObject fieldJson = json.getJSONArray("Fields").getJSONObject(0);
        
        assertEquals("TEST_FIELD", fieldJson.getString("Name"));
        assertEquals("NVARCHAR", fieldJson.getString("Type"));
        assertEquals(100, fieldJson.getInt("Length"));
        assertEquals("测试字段", fieldJson.getString("Comment"));
        assertTrue(fieldJson.getBoolean("Nullable"));
    }
}
