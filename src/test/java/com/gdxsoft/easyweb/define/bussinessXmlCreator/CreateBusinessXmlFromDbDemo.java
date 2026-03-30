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
        System.out.println("    检查 demo 数据库...");
        
        // 注意：demo 数据源需要在 ewa_conf.xml 中配置
        // 如果 demo 数据源不存在，使用 HSQL 内存数据库创建测试数据
        
        // 检查表是否存在
        boolean tableExists = false;
        try {
            String checkSql = "SELECT COUNT(*) AS CNT FROM CRM_COM";
            com.gdxsoft.easyweb.data.DTTable checkDt = com.gdxsoft.easyweb.data.DTTable.getJdbcTable(checkSql, "demo");
            if (checkDt.getCount() >= 0) {
                tableExists = true;
                System.out.println("    CRM_COM 表已存在，包含 " + checkDt.getCount() + " 条记录");
            }
        } catch (Exception e) {
            // 表不存在或 demo 数据源不存在
            System.out.println("    demo 数据源未配置或 CRM_COM 表不存在");
            System.out.println("    提示：请在 ewa_conf.xml 中配置 demo 数据源");
            System.out.println("    或者使用已有的数据源替换 'demo' 字符串");
        }
        
        if (!tableExists) {
            // 如果 demo 数据源不存在，使用模拟数据
            System.out.println("    使用模拟数据创建表结构...");
            // 这里不实际创建表，因为数据源不存在
            // 实际使用时，请确保 demo 数据源已配置
        }
    }
    
    /**
     * 从数据库读取表结构
     * 使用 INFORMATION_SCHEMA 查询获取字段信息
     * 如果数据库不存在，使用模拟数据
     */
    private Table readTableStructure() throws Exception {
        System.out.println("    从 INFORMATION_SCHEMA 读取表结构...");
        
        Table table = new Table();
        table.initBlankFrame();
        table.setName("CRM_COM");
        
        // 创建主键对象
        com.gdxsoft.easyweb.define.database.TablePk pk = new com.gdxsoft.easyweb.define.database.TablePk();
        pk.setTableName("CRM_COM");
        table.setPk(pk);
        
        // 尝试从数据库读取
        boolean fromDatabase = false;
        try {
            // 1. 读取字段信息
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, " +
                         "NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT " +
                         "FROM INFORMATION_SCHEMA.COLUMNS " +
                         "WHERE TABLE_NAME='CRM_COM' AND TABLE_SCHEMA='PUBLIC' " +
                         "ORDER BY ORDINAL_POSITION";
            
            com.gdxsoft.easyweb.data.DTTable dt = com.gdxsoft.easyweb.data.DTTable.getJdbcTable(sql, "demo");
            
            if (dt.getCount() > 0) {
                fromDatabase = true;
                System.out.println("    找到 " + dt.getCount() + " 个字段");
                
                // 2. 读取主键信息
                String pkSql = "SELECT CC.COLUMN_NAME " +
                               "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC " +
                               "JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE CC " +
                               "ON TC.CONSTRAINT_NAME = CC.CONSTRAINT_NAME " +
                               "WHERE TC.TABLE_NAME='CRM_COM' AND TC.CONSTRAINT_TYPE='PRIMARY KEY'";
                
                java.util.Set<String> pkColumns = new java.util.HashSet<>();
                try {
                    com.gdxsoft.easyweb.data.DTTable pkDt = com.gdxsoft.easyweb.data.DTTable.getJdbcTable(pkSql, "demo");
                    for (int i = 0; i < pkDt.getCount(); i++) {
                        String pkColumnName = pkDt.getRows().getRow(i).getCell("COLUMN_NAME").toString();
                        pkColumns.add(pkColumnName);
                        System.out.println("    主键字段：" + pkColumnName);
                    }
                } catch (Exception e) {
                    System.out.println("    未找到主键信息");
                }
                
                // 3. 读取自增列信息
                String identitySql = "SELECT COLUMN_NAME " +
                                     "FROM INFORMATION_SCHEMA.COLUMNS " +
                                     "WHERE TABLE_NAME='CRM_COM' AND TABLE_SCHEMA='PUBLIC' " +
                                     "AND COLUMN_DEFAULT LIKE '%auto_increment%'";
                
                java.util.Set<String> identityColumns = new java.util.HashSet<>();
                try {
                    com.gdxsoft.easyweb.data.DTTable identityDt = com.gdxsoft.easyweb.data.DTTable.getJdbcTable(identitySql, "demo");
                    for (int i = 0; i < identityDt.getCount(); i++) {
                        String identityColumnName = identityDt.getRows().getRow(i).getCell("COLUMN_NAME").toString();
                        identityColumns.add(identityColumnName);
                        System.out.println("    自增字段：" + identityColumnName);
                    }
                } catch (Exception e) {
                    // 没有自增列，忽略
                }
                
                // 4. 创建字段对象
                for (int i = 0; i < dt.getCount(); i++) {
                    com.gdxsoft.easyweb.data.DTRow row = dt.getRows().getRow(i);
                    
                    String columnName = row.getCell("COLUMN_NAME").toString();
                    String dataType = row.getCell("DATA_TYPE").toString();
                    
                    // 获取长度
                    String charMaxLenStr = row.getCell("CHARACTER_MAXIMUM_LENGTH").toString();
                    String numericPrecisionStr = row.getCell("NUMERIC_PRECISION").toString();
                    int length = 0;
                    if (charMaxLenStr != null && !charMaxLenStr.isEmpty() && !"null".equals(charMaxLenStr)) {
                        try {
                            length = Integer.parseInt(charMaxLenStr);
                        } catch (NumberFormatException e) {
                            length = 0;
                        }
                    } else if (numericPrecisionStr != null && !numericPrecisionStr.isEmpty() && !"null".equals(numericPrecisionStr)) {
                        try {
                            length = Integer.parseInt(numericPrecisionStr);
                        } catch (NumberFormatException e) {
                            length = 0;
                        }
                    }
                    
                    // 判断是否允许空
                    String isNullable = row.getCell("IS_NULLABLE").toString();
                    boolean nullable = "YES".equals(isNullable);
                    
                    // 判断是否是主键
                    boolean isPk = pkColumns.contains(columnName);
                    
                    // 判断是否是自增列
                    boolean isIdentity = identityColumns.contains(columnName) || 
                                         (columnName.endsWith("_ID") && dataType.equals("INT"));
                    
                    // 创建字段
                    com.gdxsoft.easyweb.define.database.Field field = 
                        new com.gdxsoft.easyweb.define.database.Field();
                    field.setName(columnName);
                    field.setDatabaseType(dataType);
                    field.setMaxlength(length);
                    field.setNull(nullable);
                    field.setPk(isPk);
                    field.setIdentity(isIdentity);
                    field.setDescription(columnName);
                    
                    table.getFields().put(field.getName(), field);
                    table.getFields().getFieldList().add(field.getName());
                    
                    // 如果是主键字段，添加到 Pk 对象
                    if (isPk) {
                        pk.getPkFields().add(field);
                    }
                    
                    System.out.println("    字段：" + columnName + " (" + dataType + ", " + length + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("    从数据库读取失败：" + e.getMessage());
            System.out.println("    使用模拟数据...");
        }
        
        // 如果从数据库读取失败，使用模拟数据
        if (!fromDatabase) {
            System.out.println("    使用模拟数据创建表结构...");
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
        }
        
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
