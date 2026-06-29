package com.gdxsoft.emp.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嵌入式 Tomcat 10 + HSQLDB Server 启动入口
 * <p>
 * 启动顺序：检查并解压数据文件 → HSQLDB Server → Tomcat
 */
public class Tomcat10EmbeddedServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tomcat10EmbeddedServer.class);

    private static final int TOMCAT_PORT = 8080;
    private static final int HSQLDB_PORT = 11002;
    private static final String HSQLDB_DATA_DIR = "hsqldb";
    private static final String HSQLDB_ZIP_FILE = "hsqldb-data.zip";
    private static final String[] DATABASES = {"emp_ewa", "emp_portal"};

    private static HsqldbServerManager hsqldbServer;
    private static Tomcat tomcat;

    public static void main(String[] args) throws Exception {
        LOGGER.info("=== Tomcat 10 Embedded Server ===");

        // 0. 检查并解压 HSQLDB 数据文件
        ensureHsqldbData();

        // 1. 启动 HSQLDB Server
        hsqldbServer = new HsqldbServerManager(HSQLDB_PORT, HSQLDB_DATA_DIR, DATABASES);
        hsqldbServer.start();

        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down...");
            try {
                if (tomcat != null) {
                    tomcat.stop();
                    tomcat.destroy();
                }
            } catch (Exception e) {
                LOGGER.error("Tomcat stop error", e);
            }
            if (hsqldbServer != null) {
                hsqldbServer.stop();
            }
        }));

        // 2. 启动 Tomcat
        startTomcat();
    }

    private static void startTomcat() throws Exception {
        String webappDir = new File("src/main/webapp").getAbsolutePath();

        tomcat = new Tomcat();
        tomcat.setPort(TOMCAT_PORT);
        tomcat.getConnector(); // 触发默认 Connector 创建
        tomcat.setHostname("0.0.0.0");

        // 启用 JNDI
        tomcat.enableNaming();
        System.setProperty("java.naming.factory.initial", "org.apache.naming.java.javaURLContextFactory");

        // 添加 Web 应用
        Context ctx = tomcat.addWebapp("", webappDir);

        // 解决 Jasper (JSP) 的 ClassLoader 问题
        ctx.setParentClassLoader(Tomcat10EmbeddedServer.class.getClassLoader());

        // Session 超时（分钟），不在 web.xml 中定义 session-config
        ctx.setSessionTimeout(30);

        tomcat.start();
        LOGGER.info("Tomcat started on http://localhost:{}", TOMCAT_PORT);
        LOGGER.info("HSQLDB Server on port {}", HSQLDB_PORT);
        LOGGER.info("Press Ctrl+C to stop");

        tomcat.getServer().await();
    }

    /**
     * 检查 HSQLDB 数据文件是否存在，如果不存在则从 zip 文件解压
     */
    private static void ensureHsqldbData() throws Exception {
        File dataDir = new File(HSQLDB_DATA_DIR);
        File zipFile = new File(HSQLDB_ZIP_FILE);
        
        // 检查是否存在关键数据文件（.script 文件）
        boolean dataExists = true;
        for (String dbName : DATABASES) {
            File scriptFile = new File(dataDir, dbName + ".script");
            if (!scriptFile.exists()) {
                dataExists = false;
                break;
            }
        }
        
        if (dataExists) {
            LOGGER.info("HSQLDB data files found in {}", HSQLDB_DATA_DIR);
            return;
        }
        
        // 数据文件不存在，尝试从 zip 解压
        if (!zipFile.exists()) {
            LOGGER.error("HSQLDB data zip file not found: {}", HSQLDB_ZIP_FILE);
            LOGGER.error("Please run MysqlToHsqldbExporter to create the data files first");
            throw new RuntimeException("HSQLDB data files not found");
        }
        
        LOGGER.info("Extracting HSQLDB data from {}...", HSQLDB_ZIP_FILE);
        
        // 创建数据目录
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        // 解压 zip 文件
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            int count = 0;
            
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(entry.getName());
                
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    // 确保父目录存在
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    // 解压文件
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    count++;
                }
                zis.closeEntry();
            }
            
            LOGGER.info("Extracted {} files from {}", count, HSQLDB_ZIP_FILE);
        }
    }
}
