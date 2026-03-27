package com.gdxsoft.easyweb.testutils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.gdxsoft.easyweb.datasource.DataConnection;

/**
 * 测试工具 - 导入 SQL 文件到数据库
 */
public class SqlImporter {

    /**
     * 导入 SQL 文件到数据库
     * 
     * @param db       数据库连接名称
     * @param sqlFile  SQL 文件路径
     * @throws Exception 导入失败抛出异常
     */
    public static void importSqlFile(String db, String sqlFile) throws Exception {
        DataConnection cnn = new DataConnection();
        cnn.setConfigName(db);
        
        try {
            // 读取 SQL 文件
            String sqlContent = readFile(sqlFile);
            
            // 分割 SQL 语句（按分号分割）
            String[] sqlStatements = sqlContent.split(";");
            
            // 执行每条 SQL 语句
            cnn.transBegin();
            for (String sql : sqlStatements) {
                String trimmed = sql.trim();
                // 跳过空语句和注释
                if (trimmed.length() == 0 || trimmed.startsWith("--") || trimmed.startsWith("SET")) {
                    continue;
                }
                
                try {
                    cnn.executeUpdate(trimmed);
                } catch (Exception e) {
                    // 忽略表已存在等错误
                    System.out.println("SQL 执行跳过：" + e.getMessage());
                }
            }
            cnn.transCommit();
            System.out.println("SQL 文件导入成功：" + sqlFile);
        } catch (Exception e) {
            cnn.transRollback();
            System.err.println("SQL 文件导入失败：" + e.getMessage());
            throw e;
        } finally {
            cnn.close();
        }
    }
    
    /**
     * 读取文件内容
     */
    private static String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * 初始化测试数据库（导入 SQL 文件）
     */
    public static void initTestDatabase() {
        try {
            String sqlFile = "src/test/resources/sqls/demo_crm_hsql.sql";
            if (Files.exists(Paths.get(sqlFile))) {
                importSqlFile("demo", sqlFile);
            } else {
                System.err.println("SQL 文件不存在：" + sqlFile);
            }
        } catch (Exception e) {
            System.err.println("初始化测试数据库失败：" + e.getMessage());
        }
    }
}
