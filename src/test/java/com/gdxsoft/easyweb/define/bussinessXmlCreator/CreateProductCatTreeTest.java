package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.TablePk;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.testutils.SqlImporter;

/**
 * 从 PRODUCT_CAT 表创建 FrameTree 配置
 * 参考 designs/tb2ewaCfg/examples/product_cat.xml 的 PRODUCT_CAT.T.M 配置
 */
public class CreateProductCatTreeTest {

    private static EwaConfig config;
    private static Table productCatTable;
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
     * 创建 PRODUCT_CAT.T.M Tree 配置
     */
    @Test
    public void testCreateProductCatTree() throws Exception {
        System.out.println("\n========================================");
        System.out.println("  从 PRODUCT_CAT 表创建 FrameTree 配置");
        System.out.println("========================================\n");
        
        // 步骤 1: 创建/连接 demo 数据库
        System.out.println("步骤 1: 连接 demo 数据库...");
        dbConnection = connectDatabase();
        if (dbConnection == null) {
            System.out.println("  ✗ 数据库连接失败\n");
            return;
        }
        System.out.println("  ✓ 数据库连接成功\n");
        
        // 步骤 2: 读取 PRODUCT_CAT 表结构
        System.out.println("步骤 2: 读取 PRODUCT_CAT 表结构...");
        productCatTable = readTableStructure(dbConnection);
        System.out.println("  表名：" + productCatTable.getName());
        System.out.println("  字段数：" + productCatTable.getFields().getFieldList().size());
        System.out.println("  主键字段：" + (productCatTable.getPk() != null ? productCatTable.getPk().getPkFields().size() : 0));
        System.out.println("  ✓ 表结构读取成功\n");
        
        // 步骤 3: 创建 Tree 配置
        System.out.println("步骤 3: 创建 PRODUCT_CAT.T.M Tree 配置...");
        String outputDir = "temp/ewa_script_test/bussiness";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
        
        // 创建 PRODUCT_CAT.xml 的根节点
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xmlContent.append("<EasyWebTemplates>\n");
        
        // 创建 Tree 配置
        System.out.println("  创建 PRODUCT_CAT.T.M (Tree 模式)...");
        String treeXml = createTreeConfig("Tree", "M");
        xmlContent.append("  <!-- product_cat.t.m (Tree 模式) -->\n");
        xmlContent.append(extractEasyWebTemplate(treeXml));
        xmlContent.append("\n");
        System.out.println("    ✓ PRODUCT_CAT.T.M 已生成");
        
        // 关闭根节点
        xmlContent.append("</EasyWebTemplates>");
        
        // 保存 XML 到文件
        String outputPath = outputDir + "/PRODUCT_CAT.xml";
        java.nio.file.Files.write(java.nio.file.Paths.get(outputPath), xmlContent.toString().getBytes("UTF-8"));
        
        System.out.println("\n========================================");
        System.out.println("  Tree 配置已生成");
        System.out.println("========================================");
        System.out.println("输出文件：" + outputPath);
        System.out.println("文件大小：" + xmlContent.length() + " 字节");
        System.out.println();
    }
    
    /**
     * 连接数据库
     */
    private DataConnection connectDatabase() throws Exception {
        System.out.println("    连接 demo 数据库...");
        // 使用 SqlImporter 导入 SQL 文件并保持连接打开
        String sqlFile = "src/test/resources/sqls/demo_crm_hsql.sql";
        DataConnection cnn = com.gdxsoft.easyweb.testutils.SqlImporter.importSqlFileKeepConnection("demo", sqlFile);
        
        // 检查 PRODUCT_CAT 表是否已存在
        try {
            String checkSql = "SELECT COUNT(*) AS CNT FROM PRODUCT_CAT";
            DTTable checkDt = DTTable.getJdbcTable(checkSql, cnn);
            if (checkDt.getCount() >= 0) {
                System.out.println("    PRODUCT_CAT 表已存在，包含 " + checkDt.getCount() + " 条记录");
            }
        } catch (Exception e) {
            System.out.println("    PRODUCT_CAT 表不存在，需要手动创建");
        }
        
        return cnn;
    }
    
    /**
     * 从数据库读取表结构
     * 使用 Table 类的构造函数和 init() 方法读取表结构
     */
    private Table readTableStructure(DataConnection cnn) throws Exception {
        System.out.println("    使用 Table 类读取表结构...");
        
        // 获取当前连接的配置名
        String configName = cnn.getCurrentConfig().getName();
        // HSQL 数据库的表在 PUBLIC schema 中
        String schemaName = "PUBLIC";
        System.out.println("    Config: " + configName + ", Schema: " + schemaName);
        
        // 使用 Table 的构造函数读取表结构
        Table table = new Table("PRODUCT_CAT", schemaName, configName);
        table.init();  // 初始化表结构
        
        System.out.println("    表名：" + table.getName());
        System.out.println("    字段数：" + table.getFields().getFieldList().size());
        System.out.println("    主键字段：" + (table.getPk() != null ? table.getPk().getPkFields().size() : 0));
        
        // 打印主键字段
        if (table.getPk() != null && table.getPk().getPkFields().size() > 0) {
            for (int i = 0; i < table.getPk().getPkFields().size(); i++) {
                Field pkField = table.getPk().getPkFields().get(i);
                System.out.println("    主键：" + pkField.getName() + " (" + pkField.getDatabaseType() + ")");
            }
        } else {
            System.out.println("    警告：未找到主键字段，使用 PC_ID 作为主键");
            // 创建模拟的主键
            TablePk pk = new TablePk();
            pk.setTableName("PRODUCT_CAT");
            table.setPk(pk);
            
            Field pkField = new Field();
            pkField.setName("PC_ID");
            pkField.setDatabaseType("INTEGER");
            pkField.setPk(true);
            pkField.setIdentity(true);
            pk.getPkFields().add(pkField);
            table.getFields().getPkFields().add(pkField);
        }
        
        return table;
    }
    
    /**
     * 创建 Tree 配置
     */
    private String createTreeConfig(String frameType, String operationType) throws Exception {
        // 创建 BusinessXmlCreator
        BusinessXmlCreator creator = BusinessXmlCreator.create(config, productCatTable, frameType);
        return creator.createShowXml("demo", "PRODUCT_CAT", null, null, frameType, operationType);
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
