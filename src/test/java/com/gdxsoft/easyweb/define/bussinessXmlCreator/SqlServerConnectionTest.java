package com.gdxsoft.easyweb.define.bussinessXmlCreator;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;

/**
 * 测试 SQL Server 数据库连接
 */
public class SqlServerConnectionTest {

    /**
     * 测试连接 SQL Server 并执行 SELECT 1
     */
    @Test
    public void testSqlServerConnection() throws Exception {
        System.out.println("\n========================================");
        System.out.println("  测试 SQL Server 数据库连接");
        System.out.println("========================================\n");
        
        DataConnection cnn = null;
        try {
            System.out.println("步骤 1: 创建数据连接...");
            cnn = new DataConnection();
            cnn.setConfigName("sqlserver");
            System.out.println("  配置名：sqlserver");
            
            System.out.println("步骤 2: 连接数据库...");
            cnn.connect();
            
            if (cnn.getConnection() != null) {
                System.out.println("  ✓ 数据库连接成功");
                System.out.println("  数据库类型：" + cnn.getCurrentConfig().getType());
                System.out.println("  连接字符串：" + cnn.getCurrentConfig().getConnectionString());
            } else {
                System.out.println("  ✗ 数据库连接失败：getConnection() 返回 null");
                return;
            }
            
            System.out.println("\n步骤 3: 执行 SELECT 1...");
            String sql = "SELECT 1 AS TEST_VALUE";
            boolean result = cnn.executeQuery(sql);
            
            if (result) {
                System.out.println("  ✓ SQL 执行成功");
                
                DTTable dt = new DTTable();
                dt.initData(cnn.getLastResult().getResultSet());
                
                if (dt.getCount() > 0) {
                    Object value = dt.getCell(0, "TEST_VALUE");
                    System.out.println("  查询结果：" + value);
                }
            } else {
                System.out.println("  ✗ SQL 执行失败：" + cnn.getErrorMsg());
            }
            
            System.out.println("\n步骤 4: 检查 bas_TAG 表...");
            String checkSql = "SELECT TOP 1 * FROM bas_TAG";
            try {
                DTTable checkDt = DTTable.getJdbcTable(checkSql, cnn);
                System.out.println("  ✓ bas_TAG 表存在");
                System.out.println("  字段数：" + checkDt.getColumns().getCount());
                System.out.println("  字段列表:");
                for (int i = 0; i < checkDt.getColumns().getCount(); i++) {
                    System.out.println("    - " + checkDt.getColumns().getColumn(i).getName());
                }
            } catch (Exception e) {
                System.out.println("  ✗ bas_TAG 表不存在或无法访问：" + e.getMessage());
            }
            
            System.out.println("\n========================================");
            System.out.println("  测试完成");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.out.println("\n✗ 测试失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cnn != null) {
                cnn.close();
                System.out.println("  数据库连接已关闭");
            }
        }
    }
}
