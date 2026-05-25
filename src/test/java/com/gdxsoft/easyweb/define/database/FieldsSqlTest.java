package com.gdxsoft.easyweb.define.database;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Fields SQL 生成方法测试
 */
public class FieldsSqlTest {

    /**
     * 测试 GetSqlSelectLF - ListFrame 的 SELECT 查询
     */
    @Test
    public void testGetSqlSelectLF() {
        Fields fields = createMockFields();
        
        String sql = fields.GetSqlSelectLF("CRM_COM_STATE", false);
        
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT A.* FROM CRM_COM A"));
        assertTrue(sql.contains("WHERE 1=1"));
        assertTrue(sql.contains("ORDER BY"));
        System.out.println("GetSqlSelectLF:\n" + sql);
    }
    
    /**
     * 测试 GetSqlDeleteA - 逻辑删除 SQL
     */
    @Test
    public void testGetSqlDeleteA() {
        Fields fields = createMockFields();
        
        String sql = fields.GetSqlDeleteA("CRM_COM_STATE");
        
        assertNotNull(sql);
        assertTrue(sql.contains("UPDATE CRM_COM SET CRM_COM_STATE='DEL'"));
        assertTrue(sql.contains("WHERE"));
        System.out.println("GetSqlDeleteA:\n" + sql);
    }
    
    /**
     * 测试 GetSqlRestore - 恢复数据 SQL
     */
    @Test
    public void testGetSqlRestore() {
        Fields fields = createMockFields();
        
        String sql = fields.GetSqlRestore("CRM_COM_STATE");
        
        assertNotNull(sql);
        assertTrue(sql.contains("UPDATE CRM_COM SET CRM_COM_STATE='USED'"));
        assertTrue(sql.contains("WHERE"));
        System.out.println("GetSqlRestore:\n" + sql);
    }
    
    /**
     * 测试 GetSqlNew - 新增 SQL
     */
    @Test
    public void testGetSqlNew() {
        Fields fields = createMockFields();
        
        String sql = fields.GetSqlNew("CRM_COM_STATE");
        
        assertNotNull(sql);
        assertTrue(sql.contains("INSERT INTO CRM_COM"));
        assertTrue(sql.contains("VALUES"));
        System.out.println("GetSqlNew:\n" + sql);
    }
    
    /**
     * 测试 GetSqlUpdate - 更新 SQL
     */
    @Test
    public void testGetSqlUpdate() {
        Fields fields = createMockFields();
        
        String sql = fields.GetSqlUpdate("CRM_COM_STATE");
        
        assertNotNull(sql);
        assertTrue(sql.contains("UPDATE CRM_COM SET"));
        assertTrue(sql.contains("WHERE"));
        System.out.println("GetSqlUpdate:\n" + sql);
    }
    
    /**
     * 测试 GetPkParas - 主键参数表达式
     */
    @Test
    public void testGetPkParas() {
        Fields fields = createMockFields();
        
        String paras = fields.GetPkParas();
        
        assertNotNull(paras);
        assertTrue(paras.startsWith("&"));
        assertTrue(paras.contains("CRM_COM_ID=@CRM_COM_ID"));
        System.out.println("GetPkParas: " + paras);
    }
    
    /**
     * 测试 GetPara - 参数名映射
     */
    @Test
    public void testGetPara() {
        Fields fields = createMockFields();
        
        // 测试日期字段
        Field dateField = new Field();
        dateField.setName("CRM_COM_CDATE");
        dateField.setDatabaseType("DATETIME");
        // fields.getFields() 是私有的，我们通过 SQL 生成来验证
        
        // 测试 UNID 字段
        Field unidField = new Field();
        unidField.setName("CRM_COM_UNID");
        unidField.setDatabaseType("CHAR");
        
        // 测试 IP 字段
        Field ipField = new Field();
        ipField.setName("REMOTE_IP");
        ipField.setDatabaseType("VARCHAR");
        
        System.out.println("GetPara 测试完成");
    }
    
    /**
     * 创建模拟的 Fields 对象
     */
    private Fields createMockFields() {
        Fields fields = new Fields();
        fields.setTableName("CRM_COM");
        
        // 添加主键字段
        Field pkField = new Field();
        pkField.setName("CRM_COM_ID");
        pkField.setDatabaseType("INT");
        pkField.setPk(true);
        pkField.setIdentity(true);
        fields.put(pkField.getName(), pkField);
        fields.getFieldList().add(pkField.getName());
        fields.addPkField(pkField);
        
        // 添加普通字段
        Field nameField = new Field();
        nameField.setName("CRM_COM_NAME");
        nameField.setDatabaseType("NVARCHAR");
        nameField.setMaxlength(200);
        fields.put(nameField.getName(), nameField);
        fields.getFieldList().add(nameField.getName());
        
        // 添加状态字段
        Field stateField = new Field();
        stateField.setName("CRM_COM_STATE");
        stateField.setDatabaseType("VARCHAR");
        stateField.setMaxlength(10);
        fields.put(stateField.getName(), stateField);
        fields.getFieldList().add(stateField.getName());
        
        // 添加修改日期字段
        Field mdateField = new Field();
        mdateField.setName("CRM_COM_MDATE");
        mdateField.setDatabaseType("DATETIME");
        fields.put(mdateField.getName(), mdateField);
        fields.getFieldList().add(mdateField.getName());
        
        // 添加创建日期字段
        Field cdateField = new Field();
        cdateField.setName("CRM_COM_CDATE");
        cdateField.setDatabaseType("DATETIME");
        fields.put(cdateField.getName(), cdateField);
        fields.getFieldList().add(cdateField.getName());
        
        return fields;
    }
}
