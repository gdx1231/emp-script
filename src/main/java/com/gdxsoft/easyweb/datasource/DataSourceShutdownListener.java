package com.gdxsoft.easyweb.datasource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener that shuts down DataSources and deregisters JDBC drivers on webapp stop to avoid
 * memory leaks when Tomcat reloads the web application.
 */
@WebListener
public class DataSourceShutdownListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceShutdownListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	LOGGER.info("Context initialized: DataSourceShutdownListener is active");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            LOGGER.info("Context destroyed: closing application DataSources");
            DataHelper.closeAllDataSources();
            LOGGER.info("Application DataSources closed");
        } catch (Throwable t) {
            LOGGER.error("Error while closing DataSources: {}", t.getMessage(), t);
        }

        // Deregister JDBC drivers that were registered by this webapp's classloader
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == cl) {
                    try {
                        DriverManager.deregisterDriver(driver);
                        LOGGER.info("Deregistered JDBC driver: {}", driver);
                    } catch (SQLException ex) {
                        LOGGER.warn("Error deregistering driver {}: {}", driver, ex.getMessage());
                    }
                } else {
                    LOGGER.debug("Not deregistering driver {} as it does not belong to this webapp classloader", driver);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Error while deregistering JDBC drivers: {}", t.getMessage(), t);
        }

        // Some JDBC drivers (e.g., MySQL) start threads. Try to stop known cleanup threads via reflection if present.
        try {
            // Example: com.mysql.cj.jdbc.AbandonedConnectionCleanupThread (older mysql-connector)
            try {
                Class<?> cleanup = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
                Method shutdown = cleanup.getMethod("checkedShutdown");
                shutdown.invoke(null);
                LOGGER.info("Invoked com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown()");
            } catch (ClassNotFoundException cnfe) {
                // try old classname
                try {
                    Class<?> cleanupOld = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
                    Method shutdownOld = cleanupOld.getMethod("checkedShutdown");
                    shutdownOld.invoke(null);
                    LOGGER.info("Invoked com.mysql.jdbc.AbandonedConnectionCleanupThread.checkedShutdown()");
                } catch (ClassNotFoundException cnfe2) {
                    // no cleanup thread class
                }
            } catch (Throwable t) {
                LOGGER.warn("Could not shutdown MySQL cleanup thread: {}", t.getMessage());
            }

            // Some drivers register other resources; attempt to clear DriverManager's driver deregistration map if any leftover (best effort)
            try {
                Field field = DriverManager.class.getDeclaredField("drivers");
                field.setAccessible(true);
                Object obj = field.get(null);
                LOGGER.debug("DriverManager.drivers type: {}", obj == null ? "null" : obj.getClass().getName());
            } catch (Throwable t) {
                // ignore - reflection may fail on newer JVMs
            }

        } catch (Throwable t) {
            LOGGER.warn("Error during extra JDBC cleanup: {}", t.getMessage());
        }
    }
}
