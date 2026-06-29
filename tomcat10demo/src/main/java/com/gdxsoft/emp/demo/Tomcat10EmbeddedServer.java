package com.gdxsoft.emp.demo;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嵌入式 Tomcat 10 + HSQLDB Server 启动入口
 * <p>
 * 启动顺序：HSQLDB Server → Tomcat
 */
public class Tomcat10EmbeddedServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tomcat10EmbeddedServer.class);

    private static final int TOMCAT_PORT = 8080;
    private static final int HSQLDB_PORT = 11002;
    private static final String HSQLDB_DATA_DIR = "hsqldb";
    private static final String[] DATABASES = {"emp_ewa", "emp_portal"};

    private static HsqldbServerManager hsqldbServer;
    private static Tomcat tomcat;

    public static void main(String[] args) throws Exception {
        LOGGER.info("=== Tomcat 10 Embedded Server ===");

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
}
