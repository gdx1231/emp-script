package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TablePk;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.datasource.DataConnection;

/**
 * 从 SQL Server 数据库读取 bas_TAG 表创建 FrameTree 配置
 * 数据库：sqlserver (OneWorld)
 * 表：bas_TAG
 */
public class CreateBasTagTreeFromSqlServerTest {

    private static EwaConfig config;
    private static Table basTagTable;
    private static DataConnection dbConnection;

    @BeforeAll
    public static void setUp() {
        try {
            // 加载 EwaConfig
            config = EwaConfig.instance();
            System.out.println("=== EwaConfig 加载成功 ===\n");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化失败：" + e.getMessage());
        }
    }

    /**
     * 从 SQL Server 读取 bas_TAG 表创建 Tree 配置
     */
    @Test
    public void testCreateBasTagTree() throws Exception {
        System.out.println("\n========================================");
        System.out.println("  从 SQL Server 读取 bas_TAG 创建 Tree 配置");
        System.out.println("========================================\n");
        
        // 步骤 1: 连接 SQL Server 数据库
        System.out.println("步骤 1: 连接 SQL Server 数据库...");
        dbConnection = connectSqlServer();
        if (dbConnection == null || dbConnection.getConnection() == null) {
            System.out.println("  ✗ 数据库连接失败，使用模拟数据\n");
        } else {
            System.out.println("  ✓ 数据库连接成功\n");
        }
        
        // 步骤 2: 读取 bas_TAG 表结构
        System.out.println("步骤 2: 读取 bas_TAG 表结构...");
        basTagTable = readTableStructure(dbConnection);
        System.out.println("  表名：" + basTagTable.getName());
        System.out.println("  字段数：" + basTagTable.getFields().getFieldList().size());
        System.out.println("  主键字段：" + (basTagTable.getPk() != null ? basTagTable.getPk().getPkFields().size() : 0));
        System.out.println("  ✓ 表结构读取成功\n");
        
        // 步骤 3: 创建 Tree 配置
        System.out.println("步骤 3: 创建 bas_TAG.T.M Tree 配置...");
        String outputDir = "temp/ewa_script_test/bussiness";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
        
        // 创建 bas_TAG.xml 的根节点
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xmlContent.append("<EasyWebTemplates>\n");
        
        // 创建 Tree 配置
        System.out.println("  创建 bas_TAG.T.M (Tree 模式)...");
        String treeXml = createTreeConfig("Tree", "M");
        xmlContent.append("  <!-- bas_tag.t.m (Tree 模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(treeXml));
        xmlContent.append("\n");
        System.out.println("    ✓ bas_TAG.T.M 已生成");
        
        // 关闭根节点
        xmlContent.append("</EasyWebTemplates>");
        
        // 保存 XML 到文件
        String outputPath = outputDir + "/bas_TAG.xml";
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.toString().getBytes("UTF-8"));
        
        System.out.println("\n========================================");
        System.out.println("  Tree 配置已生成");
        System.out.println("========================================");
        System.out.println("输出文件：" + outputPath);
        System.out.println("文件大小：" + xmlContent.length() + " 字节");
        System.out.println();
    }
    
    /**
     * 连接 SQL Server 数据库
     */
    private DataConnection connectSqlServer() throws Exception {
        System.out.println("    连接 sqlserver 数据源...");
        try {
            DataConnection cnn = new DataConnection();
            cnn.setConfigName("sqlserver");
            cnn.connect();
            
            // 检查 bas_TAG 表是否存在
            try {
                String checkSql = "SELECT COUNT(*) AS CNT FROM bas_TAG";
                DTTable checkDt = DTTable.getJdbcTable(checkSql, cnn);
                if (checkDt.getCount() >= 0) {
                    System.out.println("    bas_TAG 表已存在，包含 " + checkDt.getCount() + " 条记录");
                }
            } catch (Exception e) {
                System.out.println("    警告：bas_TAG 表不存在或无法访问：" + e.getMessage());
            }
            
            return cnn;
        } catch (Exception e) {
            System.out.println("    SQL Server 连接失败：" + e.getMessage());
            System.out.println("    使用模拟的 bas_TAG 表结构");
            return null;
        }
    }
    
    /**
     * 从数据库读取表结构
     * 如果数据库连接失败，使用模拟的表结构
     */
    private Table readTableStructure(DataConnection cnn) throws Exception {
        if (cnn == null || cnn.getConnection() == null) {
            // 使用模拟的 bas_TAG 表结构
            System.out.println("    创建模拟的 bas_TAG 表结构...");
            return createMockBasTagTable();
        }
        
        System.out.println("    使用 Table 类读取表结构...");
        
        try {
            // SQL Server 的 schema 是 dbo
            String schemaName = "dbo";
            String configName = cnn.getCurrentConfig().getName();
            System.out.println("    Config: " + configName + ", Schema: " + schemaName);
            
            // 使用 Table 的构造函数读取表结构
            Table table = new Table("bas_TAG", schemaName, configName);
            table.init();  // 初始化表结构
            
            System.out.println("    表名：" + table.getName());
            System.out.println("    字段数：" + table.getFields().getFieldList().size());
            System.out.println("    主键字段：" + (table.getPk() != null ? table.getPk().getPkFields().size() : 0));
            
            return table;
        } catch (Exception e) {
            System.out.println("    读取表结构失败：" + e.getMessage());
            System.out.println("    使用模拟的 bas_TAG 表结构...");
            return createMockBasTagTable();
        }
    }
    
    /**
     * 创建模拟的 bas_TAG 表结构
     * 根据 bas_TAG 表的实际结构定义
     */
    private Table createMockBasTagTable() {
        Table table = new Table();
        table.initBlankFrame();
        table.setName("bas_TAG");
        
        // 创建主键对象
        TablePk pk = new TablePk();
        pk.setTableName("bas_TAG");
        table.setPk(pk);
        
        // 添加 bas_TAG 表的字段（根据实际表结构）
        // TAG_ID: 主键
        addField(table, "TAG_ID", "int", 0, true, true, false, "标签 ID", pk);
        // TAG_PID: 父 ID (树形结构)
        addField(table, "TAG_PID", "int", 0, false, false, true, "父标签 ID", pk);
        // TAG_LVL: 层级
        addField(table, "TAG_LVL", "int", 0, false, false, true, "层级", pk);
        // TAG_ORD: 排序
        addField(table, "TAG_ORD", "int", 0, false, false, true, "排序", pk);
        // TAG_NAME: 名称
        addField(table, "TAG_NAME", "nvarchar", 100, false, false, false, "标签名称", pk);
        // TAG_NAME_EN: 英文名称
        addField(table, "TAG_NAME_EN", "nvarchar", 100, false, false, true, "标签英文名称", pk);
        // TAG_CODE: 编码
        addField(table, "TAG_CODE", "nvarchar", 50, false, false, true, "标签编码", pk);
        // TAG_STATUS: 状态
        addField(table, "TAG_STATUS", "nvarchar", 20, false, false, true, "状态", pk);
        // TAG_CDATE: 创建时间
        addField(table, "TAG_CDATE", "datetime", 0, false, false, true, "创建时间", pk);
        // TAG_MDATE: 修改时间
        addField(table, "TAG_MDATE", "datetime", 0, false, false, true, "修改时间", pk);
        // TAG_MEMO: 备注
        addField(table, "TAG_MEMO", "nvarchar", 500, false, false, true, "备注", pk);
        
        System.out.println("    模拟表结构创建成功");
        System.out.println("    字段数：" + table.getFields().getFieldList().size());
        System.out.println("    主键字段：" + pk.getPkFields().size());
        
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
    
    /**
     * 创建 Tree 配置
     */
    private String createTreeConfig(String frameType, String operationType) throws Exception {
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, basTagTable, frameType);
        return creator.createShowXml("sqlserver", "bas_TAG", null, null, frameType, operationType);
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
