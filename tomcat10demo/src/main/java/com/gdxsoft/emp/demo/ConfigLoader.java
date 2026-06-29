package com.gdxsoft.emp.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置加载器
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "application.properties";
    
    private static Properties props;
    
    static {
        props = new Properties();
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                LOGGER.info("Loaded configuration from {}", CONFIG_FILE);
            } else {
                LOGGER.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration", e);
        }
    }
    
    public static String get(String key) {
        return props.getProperty(key);
    }
    
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    public static int getInt(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid integer value for {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    // Tomcat 配置
    public static int getTomcatPort() {
        return getInt("tomcat.port", 8080);
    }
    
    public static String getTomcatHost() {
        return get("tomcat.host", "0.0.0.0");
    }
    
    // HSQLDB 配置
    public static String getHsqldbMode() {
        return get("hsqldb.mode", "file");
    }
    
    public static String getHsqldbDataDir() {
        return get("hsqldb.data.dir", "hsqldb");
    }
    
    public static int getHsqldbPort() {
        return getInt("hsqldb.port", 11002);
    }
    
    public static String[] getHsqldbDatabases() {
        String dbs = get("hsqldb.databases", "emp_ewa,emp_portal");
        return dbs.split(",");
    }
    
    // P6Spy 配置
    public static boolean isP6spyEnabled() {
        return getBoolean("p6spy.enabled", true);
    }
}
