package com.gdxsoft.easyweb.websocket;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfWebSocket;
import com.gdxsoft.easyweb.conf.ConfWebSockets;

/**
 * 从 ewa_conf.xml 的 &lt;wss&gt; 节点读取 WebSocket endpoint 配置，
 * 通过 {@link ServerContainer} 手动注册。
 *
 * <p>同时支持两种发现方式，互不冲突：</p>
 * <ul>
 *   <li>&#064;WebListener — Servlet 3.0+ 容器自动扫描</li>
 *   <li>web.xml — 容器不扫注解时手动声明</li>
 * </ul>
 *
 * <p>通过静态锁保证只注册一次，即使两个方式同时生效也不会重复。</p>
 *
 * <p>web.xml 配置：</p>
 * <pre>
 * &lt;listener&gt;
 *     &lt;listener-class&gt;com.gdxsoft.easyweb.websocket.WsContextListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 */
@WebListener
public class WsContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WsContextListener.class);

    /** 保证 contextInitialized 只执行一次，防止 @WebListener + web.xml 双重触发 */
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!REGISTERED.compareAndSet(false, true)) {
            LOGGER.info("WebSocket 已注册，跳过重复调用（可能 @WebListener + web.xml 双重触发）");
            return;
        }
        ServletContext ctx = sce.getServletContext();

        ConfWebSockets conf = ConfWebSockets.getInstance();
        if (conf.size() == 0) {
            LOGGER.info("ewa_conf.xml 未配置 <wss>，跳过 WebSocket 注册");
            return;
        }

        // 从 ServletContext 获取 WebSocket 容器
        Object obj = ctx.getAttribute("javax.websocket.server.ServerContainer");
        if (obj == null) {
            // Jakarta 版本
            obj = ctx.getAttribute("jakarta.websocket.server.ServerContainer");
        }
        if (!(obj instanceof ServerContainer)) {
            LOGGER.warn("ServletContext 中未找到 ServerContainer，无法注册 WebSocket。"
                    + "请确认容器支持 WebSocket（Tomcat 7.0.47+ / Jetty 9.1+）");
            return;
        }
        ServerContainer container = (ServerContainer) obj;

        // 逐个注册
        for (ConfWebSocket ws : conf.getList()) {
            ServerEndpointConfig cfg = buildConfig(ws);
            if (cfg == null) {
                continue;
            }
            try {
                container.addEndpoint(cfg);
                LOGGER.info("WebSocket 已注册: {} -> {}", ws.getEndpoint(), ws.getClazz());
            } catch (DeploymentException e) {
                LOGGER.error("注册 WebSocket 失败: {} — {}", ws.getEndpoint(), e.getMessage(), e);
            }
        }
    }

    private ServerEndpointConfig buildConfig(ConfWebSocket ws) {
        // 加载 endpoint class（注解模式 POJO 不需要 extends Endpoint）
        Class<?> endpointClass;
        try {
            endpointClass = Class.forName(ws.getClazz());
        } catch (ClassNotFoundException e) {
            LOGGER.warn("ws {} 的 class {} 未找到，跳过", ws.getName(), ws.getClazz());
            return null;
        }

        // 构建
        ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder
                .create(endpointClass, ws.getEndpoint());

        // configurator（可选）
        String cfgClass = ws.getConfigurator();
        if (cfgClass != null && !cfgClass.trim().isEmpty()) {
            try {
                Class<?> cfgClz = Class.forName(cfgClass.trim());
                Object cfgInst = cfgClz.getDeclaredConstructor().newInstance();
                if (cfgInst instanceof ServerEndpointConfig.Configurator) {
                    builder.configurator((ServerEndpointConfig.Configurator) cfgInst);
                }
            } catch (Exception e) {
                LOGGER.warn("ws {} 的 configurator {} 实例化失败: {}", ws.getName(), cfgClass, e.getMessage());
            }
        }

        return builder.build();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing
    }
}
