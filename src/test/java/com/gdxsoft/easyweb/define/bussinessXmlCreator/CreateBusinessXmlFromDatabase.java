package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.script.template.EwaConfig;

/**
 * 从数据库表创建业务 XML 配置
 * 表：demo.crm_com
 */
public class CreateBusinessXmlFromDatabase {

    private static EwaConfig config;
    private static Table crmComTable;

    @BeforeAll
    public static void setUp() {
        try {
            // 加载 EwaConfig
            config = EwaConfig.instance();
            
            // 创建模拟的 CRM_COM 表结构（实际使用时从数据库读取）
            crmComTable = createMockCrmComTable();
            
            System.out.println("表名：" + crmComTable.getName());
            System.out.println("字段数：" + crmComTable.getFields().getFieldList().size());
            System.out.println("主键字段：" + (crmComTable.getPk() != null ? crmComTable.getPk().getPkFields().size() : 0));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建模拟的 CRM_COM 表结构
     * 实际使用时，可以从数据库读取：Table.getJdbcTable("CRM_COM", "demo")
     */
    private static Table createMockCrmComTable() throws Exception {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        table.setSchemaName("dbo");
        
        // 先创建主键对象
        com.gdxsoft.easyweb.define.database.TablePk pk = new com.gdxsoft.easyweb.define.database.TablePk();
        pk.setTableName("CRM_COM");
        table.setPk(pk);
        
        // 添加字段
        addField(table, "CRM_COM_ID", "INT", 0, true, true, false, "公司 ID", pk);
        addField(table, "CRM_COM_NAME", "NVARCHAR", 200, false, false, false, "公司名称", pk);
        addField(table, "CRM_COM_SNAME", "NVARCHAR", 150, false, false, true, "公司简称", pk);
        addField(table, "CRM_COM_CODE", "VARCHAR", 40, false, false, true, "公司代码", pk);
        addField(table, "CRM_COM_ADDR", "VARCHAR", 500, false, false, true, "公司地址", pk);
        addField(table, "CRM_COM_EMAIL", "VARCHAR", 100, false, false, true, "邮箱", pk);
        addField(table, "CRM_COM_TELE", "VARCHAR", 100, false, false, true, "电话", pk);
        addField(table, "CRM_COM_MOBILE", "VARCHAR", 50, false, false, true, "手机", pk);
        addField(table, "CRM_COM_CDATE", "DATETIME", 0, false, false, true, "创建时间", pk);
        addField(table, "CRM_COM_MDATE", "DATETIME", 0, false, false, true, "修改时间", pk);
        addField(table, "CRM_COM_STATE", "VARCHAR", 10, false, false, true, "状态", pk);
        
        return table;
    }
    
    /**
     * 添加字段辅助方法
     */
    private static void addField(Table table, String name, String type, int length, 
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
        
        // 如果是主键字段，添加到 Table 的 Pk 对象
        if (isPk && pk != null) {
            pk.getPkFields().add(field);
        }
    }

    /**
     * 创建 CRM_COM.LF.M（列表修改模式）
     */
    @Test
    public void testCreateCrmCom_LF_M() throws Exception {
        System.out.println("\n=== 创建 CRM_COM.LF.M ===");
        
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");
        
        // 生成 XML
        String xmlContent = creator.createShowXml(
            "demo",           // db
            "CRM_COM",        // tableName
            null,             // selectSql
            null,             // jsonParams
            "ListFrame",      // frameType
            "M"               // operationType
        );
        
        // 保存 XML 到文件
        String outputPath = "temp/ewa_script_test/bussiness/CRM_COM.LF.M.xml";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("temp/ewa_script_test/bussiness"));
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.getBytes("UTF-8"));
        
        System.out.println("XML 已生成：" + outputPath);
        System.out.println("XML 长度：" + xmlContent.length());
    }

    /**
     * 创建 CRM_COM.LF.V（列表查看模式）
     */
    @Test
    public void testCreateCrmCom_LF_V() throws Exception {
        System.out.println("\n=== 创建 CRM_COM.LF.V ===");
        
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "ListFrame");
        
        // 生成 XML
        String xmlContent = creator.createShowXml(
            "demo",           // db
            "CRM_COM",        // tableName
            null,             // selectSql
            null,             // jsonParams
            "ListFrame",      // frameType
            "V"               // operationType
        );
        
        // 保存 XML 到文件
        String outputPath = "temp/ewa_script_test/bussiness/CRM_COM.LF.V.xml";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("temp/ewa_script_test/bussiness"));
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.getBytes("UTF-8"));
        
        System.out.println("XML 已生成：" + outputPath);
        System.out.println("XML 长度：" + xmlContent.length());
    }

    /**
     * 创建 CRM_COM.F.NM（框架新增修改模式）
     */
    @Test
    public void testCreateCrmCom_F_NM() throws Exception {
        System.out.println("\n=== 创建 CRM_COM.F.NM ===");
        
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, "Frame");
        
        // 生成 XML
        String xmlContent = creator.createShowXml(
            "demo",           // db
            "CRM_COM",        // tableName
            null,             // selectSql
            null,             // jsonParams
            "Frame",          // frameType
            "NM"              // operationType
        );
        
        // 保存 XML 到文件
        String outputPath = "temp/ewa_script_test/bussiness/CRM_COM.F.NM.xml";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("temp/ewa_script_test/bussiness"));
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.getBytes("UTF-8"));
        
        System.out.println("XML 已生成：" + outputPath);
        System.out.println("XML 长度：" + xmlContent.length());
    }

    /**
     * 一次性创建所有 3 个配置，合并到同一个 CRM_COM.xml 文件
     */
    @Test
    public void testCreateAllCrmComConfigs() throws Exception {
        System.out.println("\n=== 一次性创建所有 CRM_COM 配置 ===");
        
        // 创建输出目录
        String outputDir = "temp/ewa_script_test/bussiness";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
        
        // 创建 CRM_COM.xml 的根节点
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xmlContent.append("<EasyWebTemplates>\n");
        
        // 1. 创建 CRM_COM.LF.M
        String lfMXml = createConfigXml("ListFrame", "M");
        xmlContent.append("  <!-- crm_com.lf.m (列表修改模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(lfMXml));
        xmlContent.append("\n");
        System.out.println("  ✓ CRM_COM.LF.M 已添加");
        
        // 2. 创建 CRM_COM.LF.V
        String lfVXml = createConfigXml("ListFrame", "V");
        xmlContent.append("  <!-- crm_com.lf.v (列表查看模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(lfVXml));
        xmlContent.append("\n");
        System.out.println("  ✓ CRM_COM.LF.V 已添加");
        
        // 3. 创建 CRM_COM.F.NM
        String fNmXml = createConfigXml("Frame", "NM");
        xmlContent.append("  <!-- crm_com.f.nm (框架新增修改模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(fNmXml));
        xmlContent.append("\n");
        System.out.println("  ✓ CRM_COM.F.NM 已添加");
        
        // 关闭根节点
        xmlContent.append("</EasyWebTemplates>");
        
        // 保存 XML 到文件
        String outputPath = outputDir + "/CRM_COM.xml";
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.toString().getBytes("UTF-8"));
        
        System.out.println("\n=== 所有配置已生成 ===");
        System.out.println("输出文件：" + outputPath);
        System.out.println("文件大小：" + xmlContent.length() + " 字节");
    }
    
    /**
     * 从完整的 XML 中提取 EasyWebTemplate 节点内容
     */
    private String extractEasyWebTemplate(String fullXml) {
        // 移除 XML 声明
        String xml = fullXml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n", "");
        
        // 移除外层 EasyWebTemplates 标签
        xml = xml.replace("<EasyWebTemplates>\n", "");
        xml = xml.replace("</EasyWebTemplates>", "");
        
        // 找到 <EasyWebTemplate 和 </EasyWebTemplate>
        int startIndex = xml.indexOf("<EasyWebTemplate");
        int endIndex = xml.indexOf("</EasyWebTemplate>");
        
        if (startIndex >= 0 && endIndex > startIndex) {
            endIndex += "</EasyWebTemplate>".length();
            return xml.substring(startIndex, endIndex);
        }
        
        return "";
    }
    
    /**
     * 创建单个配置的 XML
     */
    private String createConfigXml(String frameType, String operationType) throws Exception {
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, crmComTable, frameType);
        
        // 生成 XML
        return creator.createShowXml(
            "demo",
            "CRM_COM",
            null,
            null,
            frameType,
            operationType
        );
    }
}
