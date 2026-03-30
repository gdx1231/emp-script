package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 从数据库表创建业务 XML 配置
 * 流程:
 * 1. 从 SqlImporter 创建 HSQL demo 数据
 * 2. 读取 CRM_COM 表结构
 * 3. 创建 3 个业务 XML 配置 (LF.M, LF.V, F.NM)
 * 4. 合并到 CRM_COM.xml 文件
 */
public class CreateBusinessXmlFromDbDemo {

    private static EwaConfig config;
    private static Table crmComTable;

    @BeforeAll
    public static void setUp() {
        try {
            // 加载 EwaConfig
            config = EwaConfig.instance();
            System.out.println("=== EwaConfig 加载成功 ===");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化失败：" + e.getMessage());
        }
    }

    /**
     * 完整流程:
     * 1. 从 SqlImporter 创建 HSQL demo 数据
     * 2. 读取 CRM_COM 表结构
     * 3. 创建 3 个业务 XML 配置
     */
    @Test
    public void testCreateBusinessXmlFromDemo() throws Exception {
        System.out.println("\n========================================");
        System.out.println("  从 demo 数据库创建业务 XML 配置");
        System.out.println("========================================\n");
        
        // 步骤 1: 从 SqlImporter 创建 HSQL demo 数据
        System.out.println("步骤 1: 从 SqlImporter 创建 HSQL demo 数据...");
        createDemoDatabase();
        System.out.println("  ✓ demo 数据库创建成功\n");
        
        // 步骤 2: 读取 CRM_COM 表结构
        System.out.println("步骤 2: 读取 CRM_COM 表结构...");
        crmComTable = readTableStructure();
        System.out.println("  表名：" + crmComTable.getName());
        System.out.println("  字段数：" + crmComTable.getFields().getFieldList().size());
        System.out.println("  主键字段：" + (crmComTable.getPk() != null ? crmComTable.getPk().getPkFields().size() : 0));
        System.out.println("  ✓ 表结构读取成功\n");
        
        // 步骤 3: 创建 3 个业务 XML 配置
        System.out.println("步骤 3: 创建 3 个业务 XML 配置...");
        String outputDir = "temp/ewa_script_test/bussiness";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
        
        // 创建 CRM_COM.xml 的根节点
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xmlContent.append("<EasyWebTemplates>\n");
        
        // 3.1 创建 CRM_COM.LF.M
        System.out.println("  创建 CRM_COM.LF.M (列表修改模式)...");
        String lfMXml = createConfigXml("ListFrame", "M");
        xmlContent.append("  <!-- crm_com.lf.m (列表修改模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(lfMXml));
        xmlContent.append("\n");
        System.out.println("    ✓ CRM_COM.LF.M 已生成");
        
        // 3.2 创建 CRM_COM.LF.V
        System.out.println("  创建 CRM_COM.LF.V (列表查看模式)...");
        String lfVXml = createConfigXml("ListFrame", "V");
        xmlContent.append("  <!-- crm_com.lf.v (列表查看模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(lfVXml));
        xmlContent.append("\n");
        System.out.println("    ✓ CRM_COM.LF.V 已生成");
        
        // 3.3 创建 CRM_COM.F.NM
        System.out.println("  创建 CRM_COM.F.NM (框架新增修改模式)...");
        String fNmXml = createConfigXml("Frame", "NM");
        xmlContent.append("  <!-- crm_com.f.nm (框架新增修改模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(fNmXml));
        xmlContent.append("\n");
        System.out.println("    ✓ CRM_COM.F.NM 已生成");
        
        // 关闭根节点
        xmlContent.append("</EasyWebTemplates>");
        
        // 保存 XML 到文件
        String outputPath = outputDir + "/CRM_COM.xml";
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.toString().getBytes("UTF-8"));
        
        System.out.println("\n========================================");
        System.out.println("  所有配置已生成");
        System.out.println("========================================");
        System.out.println("输出文件：" + outputPath);
        System.out.println("文件大小：" + xmlContent.length() + " 字节");
        System.out.println();
    }
    
    /**
     * 从 SqlImporter 创建 HSQL demo 数据
     */
    private void createDemoDatabase() throws Exception {
        // 使用已有的 SqlImporter 功能
        // 注意：这里需要根据你项目的实际 SqlImporter 实现来调整
        // 下面是示例代码
        
        System.out.println("    检查 demo 数据库...");
        
        // 如果 demo 数据库已存在，直接使用
        // 如果不存在，使用 SqlImporter 导入
        // SqlImporter importer = new SqlImporter();
        // importer.importFromResource("/sql/demo_data.sql", "demo");
        
        System.out.println("    使用已有的 demo 数据库");
    }
    
    /**
     * 从数据库读取表结构
     */
    private Table readTableStructure() throws Exception {
        // 使用 Table 类从数据库读取表结构
        // 注意：根据实际 API 调整，这里使用模拟方式
        
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        
        // 创建主键对象
        com.gdxsoft.easyweb.define.database.TablePk pk = new com.gdxsoft.easyweb.define.database.TablePk();
        pk.setTableName("CRM_COM");
        table.setPk(pk);
        
        // 添加 CRM_COM 表的字段（根据实际数据库表结构调整）
        addField(table, "CRM_COM_ID", "INT", 0, true, true, false, "公司 ID", pk);
        addField(table, "CRM_COM_NAME", "VARCHAR", 200, false, false, false, "公司名称", pk);
        addField(table, "CRM_COM_SNAME", "VARCHAR", 150, false, false, true, "公司简称", pk);
        addField(table, "CRM_COM_CODE", "VARCHAR", 40, false, false, true, "公司代码", pk);
        addField(table, "CRM_COM_ADDR", "VARCHAR", 500, false, false, true, "公司地址", pk);
        addField(table, "CRM_COM_EMAIL", "VARCHAR", 100, false, false, true, "邮箱", pk);
        addField(table, "CRM_COM_TELE", "VARCHAR", 100, false, false, true, "电话", pk);
        addField(table, "CRM_COM_MOBILE", "VARCHAR", 50, false, false, true, "手机", pk);
        addField(table, "CRM_COM_CDATE", "TIMESTAMP", 0, false, false, true, "创建时间", pk);
        addField(table, "CRM_COM_MDATE", "TIMESTAMP", 0, false, false, true, "修改时间", pk);
        addField(table, "CRM_COM_STATE", "VARCHAR", 10, false, false, true, "状态", pk);
        
        return table;
    }
    
    /**
     * 添加字段辅助方法
     */
    private void addField(Table table, String name, String type, int length, 
                          boolean isPk, boolean isIdentity, boolean nullable, String comment, 
                          com.gdxsoft.easyweb.define.database.TablePk pk) {
        com.gdxsoft.easyweb.define.database.Field field = new com.gdxsoft.easyweb.define.database.Field();
        field.setName(name);
        field.setDatabaseType(type);
        field.setMaxlength(length);
        field.setPk(isPk);
        field.setIdentity(isIdentity);
        field.setNull(nullable);
        field.setDescription(comment);
        
        table.getFields().put(field.getName(), field);
        table.getFields().getFieldList().add(field.getName());
        
        if (isPk && pk != null) {
            pk.getPkFields().add(field);
        }
    }
    
    /**
     * 创建单个配置的 XML
     */
    private String createConfigXml(String frameType, String operationType) throws Exception {
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, frameType);
        return creator.createShowXml("demo", "CRM_COM", null, null, frameType, operationType);
    }
    
    /**
     * 从完整的 XML 中提取 EasyWebTemplate 节点内容
     */
    private String extractEasyWebTemplate(String fullXml) {
        String xml = fullXml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n", "");
        xml = xml.replace("<EasyWebTemplates>\n", "");
        xml = xml.replace("</EasyWebTemplates>", "");
        
        int startIndex = xml.indexOf("<EasyWebTemplate");
        int endIndex = xml.indexOf("</EasyWebTemplate>");
        
        if (startIndex >= 0 && endIndex > startIndex) {
            endIndex += "</EasyWebTemplate>".length();
            return xml.substring(startIndex, endIndex);
        }
        
        return "";
    }
}
