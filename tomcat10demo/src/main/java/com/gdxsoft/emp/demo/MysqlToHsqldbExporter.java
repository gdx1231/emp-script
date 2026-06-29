package com.gdxsoft.emp.demo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HSQLDB 初始化工具
 * <p>
 * 支持两种模式：
 * <ol>
 * <li>SQL 文件导入模式：执行 sql/ 目录下的 .sql 文件建表</li>
 * <li>MySQL 同步模式：从 MySQL 导出表结构和数据</li>
 * </ol>
 * <p>
 * SQL 文件导入（不需要 MySQL）：
 * <pre>
 * mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter"
 * </pre>
 * <p>
 * MySQL 同步（通过 -D 参数传入凭据）：
 * <pre>
 * mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter" \
 *   -Dmysql.url="jdbc:mysql://192.168.1.252:53306" \
 *   -Dmysql.user=root \
 *   -Dmysql.password="xxx" \
 *   -Dmysql.databases="emp_ewa,emp_main_data,emp_portal"
 * </pre>
 */
public class MysqlToHsqldbExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlToHsqldbExporter.class);

    private static final String[] DEFAULT_DATABASES = {"emp_ewa", "emp_main_data", "emp_portal"};
    private static final String SQL_DIR = "sql";

    public static void main(String[] args) throws Exception {
        String mysqlUrl = System.getProperty("mysql.url");
        String mysqlUser = System.getProperty("mysql.user");
        String mysqlPassword = System.getProperty("mysql.password");
        String mysqlDatabases = System.getProperty("mysql.databases");
        String hsqldbDataDir = System.getProperty("hsqldb.data.dir", "hsqldb");
        int hsqldbPort = Integer.parseInt(System.getProperty("hsqldb.port", "11002"));
        String sqlDir = System.getProperty("sql.dir", SQL_DIR);

        String[] databases = mysqlDatabases != null ? mysqlDatabases.split(",") : DEFAULT_DATABASES;

        boolean mysqlMode = mysqlUrl != null && mysqlUser != null && mysqlPassword != null;

        LOGGER.info("=== HSQLDB 初始化工具 ===");
        LOGGER.info("模式: {}", mysqlMode ? "MySQL 同步" : "SQL 文件导入");
        LOGGER.info("目标数据库: {}", String.join(", ", databases));
        LOGGER.info("HSQLDB 数据目录: {}", hsqldbDataDir);

        // 启动 HSQLDB Server
        HsqldbServerManager hsqldb = new HsqldbServerManager(hsqldbPort, hsqldbDataDir, databases);
        hsqldb.start();

        try {
            if (mysqlMode) {
                LOGGER.info("MySQL URL: {}", mysqlUrl);
                LOGGER.info("MySQL User: {}", mysqlUser);
                for (String dbName : databases) {
                    exportDatabase(mysqlUrl, mysqlUser, mysqlPassword, dbName, hsqldbPort);
                }
            } else {
                // SQL 文件导入模式
                for (String dbName : databases) {
                    importSqlFile(dbName, hsqldbPort, sqlDir);
                }
            }

            // 通过 JDBC 对每个库执行 SHUTDOWN，确保 .log 合并到 .script
            for (String dbName : databases) {
                String url = "jdbc:hsqldb:hsql://localhost:" + hsqldbPort + "/" + dbName;
                try (Connection conn = DriverManager.getConnection(url, "sa", "");
                     Statement st = conn.createStatement()) {
                    st.execute("SHUTDOWN COMPACT");
                    LOGGER.info("数据库 {} 已 SHUTDOWN COMPACT", dbName);
                } catch (Exception e) {
                    LOGGER.debug("SHUTDOWN {}: {}", dbName, e.getMessage());
                }
            }
        } finally {
            hsqldb.stop();
        }

        LOGGER.info("=== 完成 ===");
    }

    /**
     * 执行 SQL 文件初始化 HSQLDB
     */
    private static void importSqlFile(String dbName, int hsqldbPort, String sqlDir) throws Exception {
        File sqlFile = new File(sqlDir, dbName + ".sql");
        if (!sqlFile.exists()) {
            LOGGER.warn("SQL 文件不存在: {}", sqlFile.getAbsolutePath());
            return;
        }

        LOGGER.info("--- 导入 SQL 文件: {} ({} bytes) ---", sqlFile.getName(), sqlFile.length());

        String hsqldbUrl = "jdbc:hsqldb:hsql://localhost:" + hsqldbPort + "/" + dbName;
        String sqlContent = new String(Files.readAllBytes(sqlFile.toPath()), "UTF-8");
        LOGGER.debug("SQL 内容长度: {} 字符", sqlContent.length());

        try (Connection conn = DriverManager.getConnection(hsqldbUrl, "sa", "");
             Statement st = conn.createStatement()) {

            // 按分号分割并执行每条 SQL
            String[] statements = splitSql(sqlContent);
            LOGGER.info("解析出 {} 条 SQL 语句", statements.length);
            int count = 0;
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty() || sql.startsWith("--")) {
                    continue;
                }
                LOGGER.info("执行 SQL: {}", sql.substring(0, Math.min(100, sql.length())).replace("\n", " "));
                try {
                    st.execute(sql);
                    count++;
                } catch (Exception e) {
                    // 忽略已存在的表/索引
                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("already exists") || msg.contains("duplicate"))) {
                        LOGGER.debug("跳过已存在: {}", sql.substring(0, Math.min(50, sql.length())));
                    } else {
                        LOGGER.error("执行失败: {} - {}", sql.substring(0, Math.min(100, sql.length())), msg);
                    }
                }
            }
            conn.commit();
            // 强制写入磁盘
            try {
                st.execute("CHECKPOINT SYNC");
            } catch (Exception e) {
                // 忽略
            }
            LOGGER.info("成功执行 {} 条 SQL 语句", count);
        }
    }

    /**
     * 分割 SQL 内容，处理注释和多行语句
     */
    private static String[] splitSql(String content) {
        // 先移除单行注释
        StringBuilder cleaned = new StringBuilder();
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue; // 跳过注释行
            }
            cleaned.append(line).append("\n");
        }

        // 按分号分割
        String[] parts = cleaned.toString().split(";");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String sql = part.trim();
            if (!sql.isEmpty()) {
                result.add(sql);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * 从 MySQL 导出数据库到 HSQLDB
     */
    private static void exportDatabase(String mysqlUrl, String user, String password,
                                       String dbName, int hsqldbPort) throws Exception {
        LOGGER.info("--- 开始导出数据库: {} ---", dbName);

        String fullMysqlUrl = mysqlUrl.contains("/") && mysqlUrl.lastIndexOf('/') > mysqlUrl.indexOf("://") + 2
                ? mysqlUrl + "/" + dbName
                : mysqlUrl;
        if (!fullMysqlUrl.contains("/" + dbName)) {
            fullMysqlUrl = mysqlUrl.endsWith("/") ? mysqlUrl + dbName : mysqlUrl + "/" + dbName;
        }

        String hsqldbUrl = "jdbc:hsqldb:hsql://localhost:" + hsqldbPort + "/" + dbName;

        try (Connection mysqlConn = DriverManager.getConnection(fullMysqlUrl, user, password);
             Connection hsqlConn = DriverManager.getConnection(hsqldbUrl, "sa", "")) {

            List<String> tables = getTables(mysqlConn, dbName);
            LOGGER.info("数据库 {} 共 {} 张表", dbName, tables.size());

            for (String table : tables) {
                exportTable(mysqlConn, hsqlConn, dbName, table);
            }

            hsqlConn.commit();
            LOGGER.info("--- 数据库 {} 导出完成 ---", dbName);
        }
    }

    private static List<String> getTables(Connection mysqlConn, String dbName) throws Exception {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";
        try (PreparedStatement ps = mysqlConn.prepareStatement(sql)) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        return tables;
    }

    private static void exportTable(Connection mysqlConn, Connection hsqlConn,
                                    String dbName, String tableName) throws Exception {
        String probeSql = "SELECT * FROM `" + tableName + "` LIMIT 0";
        ResultSetMetaData meta;
        try (Statement st = mysqlConn.createStatement();
             ResultSet rs = st.executeQuery(probeSql)) {
            meta = rs.getMetaData();
        }

        int colCount = meta.getColumnCount();

        StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS \"");
        createSql.append(tableName.toUpperCase()).append("\" (");

        List<String> primaryKeys = getPrimaryKeys(mysqlConn, dbName, tableName);

        for (int i = 1; i <= colCount; i++) {
            if (i > 1) createSql.append(", ");
            String colName = meta.getColumnName(i).toUpperCase();
            int sqlType = meta.getColumnType(i);
            int precision = meta.getPrecision(i);
            int scale = meta.getScale(i);
            boolean autoIncrement = meta.isAutoIncrement(i);
            boolean nullable = meta.isNullable(i) == ResultSetMetaData.columnNullable;

            createSql.append("\"").append(colName).append("\" ");
            createSql.append(mapType(sqlType, precision, scale, autoIncrement));

            if (autoIncrement) {
                createSql.append(" GENERATED BY DEFAULT AS IDENTITY");
            }
            if (!nullable && !autoIncrement) {
                createSql.append(" NOT NULL");
            }
        }

        if (!primaryKeys.isEmpty()) {
            createSql.append(", PRIMARY KEY (");
            for (int i = 0; i < primaryKeys.size(); i++) {
                if (i > 0) createSql.append(", ");
                createSql.append("\"").append(primaryKeys.get(i).toUpperCase()).append("\"");
            }
            createSql.append(")");
        }

        createSql.append(")");

        try (Statement st = hsqlConn.createStatement()) {
            st.execute(createSql.toString());
        } catch (Exception e) {
            if (!e.getMessage().contains("already exists")) {
                LOGGER.warn("创建表 {} 失败: {}", tableName, e.getMessage());
            }
        }

        String selectSql = "SELECT * FROM `" + tableName + "`";
        String insertSql = buildInsertSql(tableName, colCount);

        try (Statement st = mysqlConn.createStatement();
             ResultSet rs = st.executeQuery(selectSql);
             PreparedStatement ps = hsqlConn.prepareStatement(insertSql)) {

            int count = 0;
            int batch = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    int sqlType = meta.getColumnType(i);
                    boolean autoIncrement = meta.isAutoIncrement(i);

                    if (autoIncrement) {
                        ps.setNull(i, Types.NULL);
                    } else {
                        ps.setObject(i, rs.getObject(i), sqlType);
                    }
                }
                ps.addBatch();
                count++;
                batch++;

                if (batch >= 1000) {
                    ps.executeBatch();
                    batch = 0;
                }
            }
            if (batch > 0) {
                ps.executeBatch();
            }
            LOGGER.info("  表 {}: {} 行", tableName, count);
        }
    }

    private static List<String> getPrimaryKeys(Connection mysqlConn, String dbName, String tableName) throws Exception {
        List<String> keys = new ArrayList<>();
        try (ResultSet rs = mysqlConn.getMetaData().getPrimaryKeys(dbName, null, tableName)) {
            while (rs.next()) {
                keys.add(rs.getString("COLUMN_NAME"));
            }
        }
        return keys;
    }

    private static String buildInsertSql(String tableName, int colCount) {
        StringBuilder sb = new StringBuilder("INSERT INTO \"");
        sb.append(tableName.toUpperCase()).append("\" VALUES (");
        for (int i = 0; i < colCount; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    private static String mapType(int sqlType, int precision, int scale, boolean autoIncrement) {
        if (autoIncrement) {
            return "BIGINT";
        }
        switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.REAL:
            case Types.FLOAT:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.NUMERIC:
            case Types.DECIMAL:
                if (scale > 0) {
                    return "DECIMAL(" + precision + "," + scale + ")";
                }
                return "DECIMAL(" + precision + ")";
            case Types.CHAR:
                return "CHAR(" + Math.max(1, precision) + ")";
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                // 大文本使用 CLOB，避免截断
                if (precision > 100000 || precision <= 0) {
                    return "CLOB";
                }
                return "VARCHAR(" + precision + ")";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
            case Types.NCLOB:
                return "CLOB";
            default:
                return "VARCHAR(1000)";
        }
    }
}
