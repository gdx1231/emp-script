package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TablePk;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 测试有状态字段的 Tree SQL 生成
 */
public class TreeSqlWithStatusTest {

    private static EwaConfig config;
    private static Table testTable;

    @BeforeAll
    public static void setUp() {
        try {
            config = EwaConfig.instance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试有 _STATUS 字段的 Tree SQL
     */
    @Test
    public void testTreeWithStatus() {
        System.out.println("\n=== 测试有 _STATUS 字段的 Tree SQL ===\n");
        
        // 创建有 _STATUS 字段的表
        testTable = createTableWithStatus();
        
        // 生成 Tree 配置
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, testTable, "Tree");
        String xml = creator.createShowXml("test", "TEST_CAT", null, null, "Tree", "M");
        
        System.out.println("生成的 SQL:");
        
        // 提取 SQL
        String[] lines = xml.split("\n");
        for (String line : lines) {
            if (line.contains("<Sql>")) {
                System.out.println(line.trim());
            }
        }
        
        // 验证 SQL
        assert xml.contains("WHERE BT_STATUS='USED'") : "OnPageLoad SQL 应该包含 WHERE BT_STATUS='USED'";
        assert xml.contains("SET BT_STATUS='DEL'") : "OnTreeNodeDelete SQL 应该是 UPDATE SET BT_STATUS='DEL'";
        assert xml.contains("'USED' BT_STATUS") : "OnTreeNodeNew SQL 应该包含 'USED' BT_STATUS";
        
        System.out.println("\n✓ 所有断言通过\n");
    }
    
    /**
     * 测试有 _STATE 字段的 Tree SQL
     */
    @Test
    public void testTreeWithState() {
        System.out.println("\n=== 测试有 _STATE 字段的 Tree SQL ===\n");
        
        // 创建有 _STATE 字段的表
        testTable = createTableWithState();
        
        // 生成 Tree 配置
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, testTable, "Tree");
        String xml = creator.createShowXml("test", "TEST_CAT", null, null, "Tree", "M");
        
        System.out.println("生成的 SQL:");
        
        // 提取 SQL
        String[] lines = xml.split("\n");
        for (String line : lines) {
            if (line.contains("<Sql>")) {
                System.out.println(line.trim());
            }
        }
        
        // 验证 SQL
        assert xml.contains("WHERE BT_STATE='USED'") : "OnPageLoad SQL 应该包含 WHERE BT_STATE='USED'";
        assert xml.contains("SET BT_STATE='DEL'") : "OnTreeNodeDelete SQL 应该是 UPDATE SET BT_STATE='DEL'";
        assert xml.contains("'USED' BT_STATE") : "OnTreeNodeNew SQL 应该包含 'USED' BT_STATE";
        
        System.out.println("\n✓ 所有断言通过\n");
    }
    
    /**
     * 创建有 _STATUS 字段的测试表
     */
    private Table createTableWithStatus() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("TEST_CAT");
        
        TablePk pk = new TablePk();
        pk.setTableName("TEST_CAT");
        table.setPk(pk);
        
        // BT_ID: 主键
        addField(table, "BT_ID", "int", 0, true, true, false, "分类 ID", pk);
        // BT_PID: 父 ID
        addField(table, "BT_PID", "int", 0, false, false, true, "父分类 ID", pk);
        // BT_LVL: 层级
        addField(table, "BT_LVL", "int", 0, false, false, true, "层级", pk);
        // BT_ORD: 排序
        addField(table, "BT_ORD", "int", 0, false, false, true, "排序", pk);
        // BT_NAME: 名称
        addField(table, "BT_NAME", "nvarchar", 100, false, false, false, "分类名称", pk);
        // BT_STATUS: 状态
        addField(table, "BT_STATUS", "nvarchar", 20, false, false, true, "状态", pk);
        
        return table;
    }
    
    /**
     * 创建有 _STATE 字段的测试表
     */
    private Table createTableWithState() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("TEST_CAT");
        
        TablePk pk = new TablePk();
        pk.setTableName("TEST_CAT");
        table.setPk(pk);
        
        // BT_ID: 主键
        addField(table, "BT_ID", "int", 0, true, true, false, "分类 ID", pk);
        // BT_PID: 父 ID
        addField(table, "BT_PID", "int", 0, false, false, true, "父分类 ID", pk);
        // BT_LVL: 层级
        addField(table, "BT_LVL", "int", 0, false, false, true, "层级", pk);
        // BT_ORD: 排序
        addField(table, "BT_ORD", "int", 0, false, false, true, "排序", pk);
        // BT_NAME: 名称
        addField(table, "BT_NAME", "nvarchar", 100, false, false, false, "分类名称", pk);
        // BT_STATE: 状态
        addField(table, "BT_STATE", "nvarchar", 20, false, false, true, "状态", pk);
        
        return table;
    }
    
    /**
     * 添加字段辅助方法
     */
    private void addField(Table table, String name, String type, int length, 
                          boolean isPk, boolean isIdentity, boolean nullable, String comment, 
                          TablePk pk) {
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
        
        if (isPk) {
            pk.getPkFields().add(field);
            table.getFields().getPkFields().add(field);
        }
    }
}
