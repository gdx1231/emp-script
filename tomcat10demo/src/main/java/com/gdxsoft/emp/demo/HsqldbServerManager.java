package com.gdxsoft.emp.demo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.hsqldb.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HSQLDB Server 模式管理器
 * <p>
 * 启动 HSQLDB 服务器，支持多个数据库。
 * 数据文件存储在指定目录下，每个数据库对应一组 .script/.log/.properties 文件。
 */
public class HsqldbServerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HsqldbServerManager.class);

    private final int port;
    private final String dataDir;
    private final String[] databaseNames;
    private Server server;

    /**
     * @param port          HSQLDB Server 监听端口
     * @param dataDir       数据文件存储目录
     * @param databaseNames 数据库名称列表
     */
    public HsqldbServerManager(int port, String dataDir, String[] databaseNames) {
        this.port = port;
        this.dataDir = dataDir;
        this.databaseNames = databaseNames;
    }

    public synchronized void start() {
        if (server != null) {
            LOGGER.warn("HSQLDB Server already running on port {}", port);
            return;
        }

        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        server = new Server();
        server.setPort(port);
        server.setSilent(true);

        for (int i = 0; i < databaseNames.length; i++) {
            String dbPath = new File(dir, databaseNames[i]).getAbsolutePath();
            server.setDatabaseName(i, databaseNames[i]);
            server.setDatabasePath(i, dbPath);
            LOGGER.info("HSQLDB database [{}] -> {}", databaseNames[i], dbPath);
        }

        server.start();
        LOGGER.info("HSQLDB Server started on port {}, databases: {}", port, String.join(", ", databaseNames));

        // 初始化数据库：启用 Oracle 语法兼容模式
        initDatabases();
    }

    /**
     * 初始化数据库，启用 Oracle 语法兼容模式
     */
    private void initDatabases() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("HSQLDB driver not found", e);
            return;
        }

        for (String dbName : databaseNames) {
            String url = "jdbc:hsqldb:hsql://localhost:" + port + "/" + dbName;
            try (Connection conn = DriverManager.getConnection(url, "sa", "");
                 Statement st = conn.createStatement()) {
                // 启用 Oracle 语法兼容（DUAL, NVL, SYSDATE 等）
                st.execute("SET DATABASE SQL SYNTAX ORA TRUE");
                conn.commit();
                LOGGER.info("Database [{}] Oracle syntax mode enabled", dbName);
            } catch (Exception e) {
                LOGGER.warn("Failed to initialize database [{}]: {}", dbName, e.getMessage());
            }
        }
    }

    public synchronized void stop() {
        if (server == null) {
            return;
        }
        LOGGER.info("Shutting down HSQLDB Server...");
        server.shutdown();
        server = null;
        LOGGER.info("HSQLDB Server stopped");
    }

    public boolean isRunning() {
        return server != null && server.getState() == ServerConstants.S_SERVER_RUNNING;
    }

    public int getPort() {
        return port;
    }

    public String getDataDir() {
        return dataDir;
    }

    public String[] getDatabaseNames() {
        return databaseNames;
    }

    /**
     * HSQLDB Server 状态常量
     */
    private static class ServerConstants {
        static final int S_SERVER_RUNNING = 1;
    }
}
