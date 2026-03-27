package com.gdxsoft.easyweb.define.database;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;

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
        field1.setDatabaseType("INT");
        field1.setMaxlength(0);
        field1.setPk(true);
        field1.setIdentity(true);
        field1.setNull(false);
        field1.setDescription("公司 ID");
        table.getFields().put(field1.getName(), field1);
        table.getFields().getFieldList().add(field1.getName());
        
        Field field2 = new Field();
        field2.setName("CRM_COM_NAME");
        field2.setDatabaseType("NVARCHAR");
        field2.setMaxlength(200);
        field2.setPk(false);
        field2.setNull(false);
        field2.setDescription("公司名称");
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
        assertEquals("INT", fields.getString("#text"));
        
        // 验证主键
        assertTrue(json.has("Pk"));
        
        // 验证外键和索引
        assertTrue(json.has("Fks"));
        assertTrue(json.has("Indexes"));
    }
    
    /**
     * 测试 toJson() 方法返回 XML 转换的 JSON
     */
    @Test
    public void testToJson_fromXml() {
        Table table = new Table("TEST_TABLE", "globaltravel");
        
        Field field = new Field();
        field.setName("TEST_FIELD");
        field.setDatabaseType("NVARCHAR");
        field.setMaxlength(100);
        field.setDescription("测试字段");
        field.setNull(true);
        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());
        
        JSONObject json = table.toJson();
        
        // 验证基本结构
        assertNotNull(json);
        assertTrue(json.has("TableName"));
        assertTrue(json.has("Fields"));
    }
}
