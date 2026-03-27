package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TablePk;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 测试从数据库表结构创建业务 XML
 * 表：demo.crm_com
 * 
 * 注意：由于无法直接从 HSQLDB 获取表结构，使用模拟数据
 */
public class DatabaseTableToXmlTest {

    private static EwaConfig config;
    private static Table crmComTable;

    @BeforeAll
    public static void setUp() {
        try {
            // 加载 EwaConfig
            config = EwaConfig.instance();
            
            // 创建模拟的 CRM_COM 表结构（模拟从数据库读取的表结构）
            crmComTable = createCrmComFromDatabase();
            
            assertNotNull(crmComTable, "CRM_COM 表应该存在");
            System.out.println("表名：" + crmComTable.getName());
            System.out.println("字段数：" + crmComTable.getFields().getFieldList().size());
            System.out.println("主键字段 (Table.Pk)：" + (crmComTable.getPk() != null ? crmComTable.getPk().getPkFields().size() : 0));
            System.out.println("主键字段 (Fields.PkFields)：" + crmComTable.getFields().getPkFields().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("初始化失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建模拟的 CRM_COM 表结构（模拟从数据库读取）
     */
    private static Table createCrmComFromDatabase() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        table.setSchemaName("dbo");
        
        // 先添加所有字段（不设置主键）
        addField(table, "CRM_COM_ID", "INT", 0, false, true, false, "公司 ID", null);
        addField(table, "CRM_COM_NAME", "NVARCHAR", 200, false, false, false, "公司名称", null);
        addField(table, "CRM_COM_SNAME", "NVARCHAR", 150, false, false, true, "公司简称", null);
        addField(table, "CRM_COM_CODE", "VARCHAR", 40, false, false, true, "公司代码", null);
        addField(table, "CRM_COM_ADDR", "VARCHAR", 500, false, false, true, "公司地址", null);
        addField(table, "CRM_COM_EMAIL", "VARCHAR", 100, false, false, true, "邮箱", null);
        addField(table, "CRM_COM_TELE", "VARCHAR", 100, false, false, true, "电话", null);
        addField(table, "CRM_COM_MOBILE", "VARCHAR", 50, false, false, true, "手机", null);
        addField(table, "CRM_COM_CDATE", "DATETIME", 0, false, false, true, "创建时间", null);
        addField(table, "CRM_COM_MDATE", "DATETIME", 0, false, false, true, "修改时间", null);
        addField(table, "CRM_COM_STATE", "VARCHAR", 10, false, false, true, "状态", null);
        
        // 所有字段添加完成后，再设置主键
        TablePk pk = new TablePk();
        pk.setTableName("CRM_COM");
        // 获取 CRM_COM_ID 字段并设置为主键
        Field pkField = table.getFields().get("CRM_COM_ID");
        pkField.setPk(true);
        pk.getPkFields().add(pkField);
        table.setPk(pk);
        
        // 重置 Fields 的主键初始化标志，以便重新初始化
        // 注意：这里不能调用 table.getFields()，因为这会触发初始化
        // 而是直接访问 _Fields 字段
        try {
            java.lang.reflect.Field fieldsField = Table.class.getDeclaredField("_Fields");
            fieldsField.setAccessible(true);
            com.gdxsoft.easyweb.define.database.Fields fields = 
                (com.gdxsoft.easyweb.define.database.Fields) fieldsField.get(table);
            if (fields != null) {
                fields.setPkInitialized(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return table;
    }
    
    /**
     * 添加字段辅助方法
     */
    private static void addField(Table table, String name, String type, int length, 
                                  boolean isPk, boolean isIdentity, boolean nullable, String comment, TablePk pk) {
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
     * 测试创建 CRM_COM.LF.M（列表修改模式）
     */
    @Test
    public void testCreateCrmCom_LF_M_FromDatabase() throws Exception {
        // 1. 创建参数
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",           // db
            "CRM_COM",        // tableName
            "ListFrame",      // frameType
            "M"               // operationType (Modify)
        );
        
        // 2. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        
        // 3. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 4. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            params.getDb(),
            params.getTableName(),
            null,  // selectSql
            null,  // jsonParams
            params.getFrameType(),
            params.getOperationType()
        );
        
        // 5. 验证 XML
        assertNotNull(xmlPreview);
        assertTrue(xmlPreview.contains("CRM_COM.LF.M"), "XML 应该包含正确的名称");
        assertTrue(xmlPreview.contains("ListFrame"), "XML 应该包含 ListFrame");
        assertTrue(xmlPreview.contains("CRM_COM_ID"), "XML 应该包含主键字段");
        assertTrue(xmlPreview.contains("butNew"), "XML 应该包含 butNew 按钮");
        assertTrue(xmlPreview.contains("butModify"), "XML 应该包含 butModify 按钮");
        assertTrue(xmlPreview.contains("butDelete"), "XML 应该包含 butDelete 按钮");
        
        // 6. 验证 PageSize 配置
        assertTrue(xmlPreview.contains("IsSplitPage=\"1\""), "应该启用分页");
        assertTrue(xmlPreview.contains("KeyField=\"CRM_COM_ID\""), "应该设置主键字段");
        assertTrue(xmlPreview.contains("PageSize=\"10\""), "应该设置每页 10 条");
        assertTrue(xmlPreview.contains("Recycle=\"1\""), "应该启用回收站");
        
        // 7. 验证 ListUI 配置
        assertTrue(xmlPreview.contains("luButtons=\"1\""), "应该显示按钮");
        assertTrue(xmlPreview.contains("luSearch=\"1\""), "应该显示搜索");
        assertTrue(xmlPreview.contains("luSelect=\"s\""), "应该是单选模式");
        
        // 8. 验证 OrderSearch 配置
        // 数字类型 (CRM_COM_ID) 应该 IsOrder=1, SearchType=""
        assertTrue(xmlPreview.contains("CRM_COM_ID"), "应该包含 CRM_COM_ID 字段");
        
        // 9. 打印 XML 预览（输出包含 DateItem 的部分）
        System.out.println("=== XML 预览 ===");
        int dataItemIndex = xmlPreview.indexOf("<DataItem");
        if (dataItemIndex > 0) {
            // 输出所有 DataItem
            String remaining = xmlPreview.substring(dataItemIndex);
            int endIdx = remaining.indexOf("</XItems>");
            if (endIdx > 0) {
                System.out.println(remaining.substring(0, endIdx));
            } else {
                System.out.println(remaining.substring(0, Math.min(3000, remaining.length())));
            }
        } else {
            System.out.println(xmlPreview.substring(0, Math.min(2000, xmlPreview.length())));
        }
        
        // 10. 保存完整 XML（使用临时文件）
        System.out.println("=== XML 已生成 ===");
        System.out.println("预览模式，未保存文件");
    }

    /**
     * 测试创建 CRM_COM.F.NM（框架新增修改模式）
     */
    @Test
    public void testCreateCrmCom_F_NM_FromDatabase() throws Exception {
        // 1. 创建参数
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",           // db
            "CRM_COM",        // tableName
            "Frame",          // frameType
            "NM"              // operationType (New/Modify)
        );
        
        // 2. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        
        // 3. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 4. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            params.getDb(),
            params.getTableName(),
            null,  // selectSql
            null,  // jsonParams
            params.getFrameType(),
            params.getOperationType()
        );
        
        // 5. 验证 XML
        assertNotNull(xmlPreview);
        assertTrue(xmlPreview.contains("CRM_COM.F.NM"), "XML 应该包含正确的名称");
        assertTrue(xmlPreview.contains("Frame"), "XML 应该包含 Frame");
        
        // 6. 打印 XML 预览
        System.out.println("=== Frame XML 预览 ===");
        System.out.println(xmlPreview.substring(0, Math.min(2000, xmlPreview.length())));
    }

    /**
     * 测试创建 CRM_COM.T.M（树形修改模式）
     */
    @Test
    public void testCreateCrmCom_T_M_FromDatabase() throws Exception {
        // 1. 创建参数
        BusinessXmlCreateParams params = new BusinessXmlCreateParams(
            "demo",           // db
            "CRM_COM",        // tableName
            "Tree",           // frameType
            "M"               // operationType (Modify)
        );
        
        // 2. 验证参数
        assertTrue(params.validate(), "参数应该有效");
        
        // 3. 创建 BusinessXmlCreator
        BusinessXmlCreator creator = new BusinessXmlCreator(config, crmComTable);
        
        // 4. 生成预览 XML
        String xmlPreview = creator.createShowXml(
            params.getDb(),
            params.getTableName(),
            null,  // selectSql
            null,  // jsonParams
            params.getFrameType(),
            params.getOperationType()
        );
        
        // 5. 验证 XML
        assertNotNull(xmlPreview);
        assertTrue(xmlPreview.contains("CRM_COM.T.M"), "XML 应该包含正确的名称");
        assertTrue(xmlPreview.contains("Tree"), "XML 应该包含 Tree");
        
        // 6. 打印 XML 预览
        System.out.println("=== Tree XML 预览 ===");
        System.out.println(xmlPreview.substring(0, Math.min(2000, xmlPreview.length())));
    }
}
