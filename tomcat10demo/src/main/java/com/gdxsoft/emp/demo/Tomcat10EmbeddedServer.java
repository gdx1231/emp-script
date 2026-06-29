package com.gdxsoft.emp.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嵌入式 Tomcat 10 启动入口
 * <p>
 * HSQLDB 使用 file 模式，无需启动独立服务器进程
 */
public class Tomcat10EmbeddedServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tomcat10EmbeddedServer.class);

    private static Tomcat tomcat;

    public static void main(String[] args) throws Exception {
        LOGGER.info("=== Tomcat 10 Embedded Server ===");
        LOGGER.info("Configuration: tomcat.port={}, hsqldb.mode={}, hsqldb.data.dir={}", 
            ConfigLoader.getTomcatPort(), ConfigLoader.getHsqldbMode(), ConfigLoader.getHsqldbDataDir());

        // 0. 检查并解压 HSQLDB 数据文件
        ensureHsqldbData();

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
        }));

        // 1. 启动 Tomcat
        startTomcat();
    }

    private static void startTomcat() throws Exception {
        String webappDir = new File("src/main/webapp").getAbsolutePath();

        tomcat = new Tomcat();
        tomcat.setPort(ConfigLoader.getTomcatPort());
        tomcat.getConnector(); // 触发默认 Connector 创建
        tomcat.setHostname(ConfigLoader.getTomcatHost());

        // 启用 JNDI
        tomcat.enableNaming();
        System.setProperty("java.naming.factory.initial", "org.apache.naming.java.javaURLContextFactory");

        // 添加 Web 应用
        Context ctx = tomcat.addWebapp("", webappDir);

        // 解决 Jasper (JSP) 的 ClassLoader 问题
        ctx.setParentClassLoader(Tomcat10EmbeddedServer.class.getClassLoader());

        // Session 超时（分钟）
        ctx.setSessionTimeout(30);

        tomcat.start();
        LOGGER.info("Tomcat started on http://{}:{}", ConfigLoader.getTomcatHost(), ConfigLoader.getTomcatPort());
        LOGGER.info("HSQLDB mode: file (data dir: {})", ConfigLoader.getHsqldbDataDir());
        LOGGER.info("Press Ctrl+C to stop");

        tomcat.getServer().await();
    }

    /**
     * 检查 HSQLDB 数据文件是否存在，如果不存在则从 zip 文件解压
     */
    private static void ensureHsqldbData() throws Exception {
        String dataDir = ConfigLoader.getHsqldbDataDir();
        String zipFile = "hsqldb-data.zip";
        String[] databases = ConfigLoader.getHsqldbDatabases();
        
        File dataDirFile = new File(dataDir);
        File zipFilePath = new File(zipFile);
        
        // 检查是否存在关键数据文件（.script 文件）
        boolean dataExists = true;
        for (String dbName : databases) {
            File scriptFile = new File(dataDirFile, dbName + ".script");
            if (!scriptFile.exists()) {
                dataExists = false;
                break;
            }
        }
        
        if (dataExists) {
            LOGGER.info("HSQLDB data files found in {}", dataDir);
            return;
        }
        
        // 数据文件不存在，尝试从 zip 解压
        if (!zipFilePath.exists()) {
            LOGGER.error("HSQLDB data zip file not found: {}", zipFile);
            LOGGER.error("Please run MysqlToHsqldbExporter to create the data files first");
            throw new RuntimeException("HSQLDB data files not found");
        }
        
        LOGGER.info("Extracting HSQLDB data from {}...", zipFile);
        
        // 创建数据目录
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        
        // 解压 zip 文件
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
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
            
            LOGGER.info("Extracted {} files from {}", count, zipFile);
        }
    }
}
