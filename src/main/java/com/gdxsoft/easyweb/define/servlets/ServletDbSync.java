package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfDefine;

/**
 * 数据库结构同步 Servlet，参考 ServletRemoteSync。
 * 提供数据库 schema 对比和同步的 HTTP 接口。
 *
 * 支持的 method 参数：
 * - listConnections: 列出可用数据库连接
 * - extract: 提取指定连接的 schema
 * - compare: 远程对比（接收源库 schema，返回差异）
 * - compareLocal: 本地对比
 * - execute: 在目标库执行同步 SQL
 */
public class ServletDbSync extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletDbSync.class);

	private static final long serialVersionUID = 983L;

	public ServletDbSync() {
		super();
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ConfDefine.isAllowDefine()) {
			response.setStatus(404);
			LOGGER.info("Not allow define, request URI: {}", request == null ? "NO request?" : request.getRequestURI());
			return;
		}

		HandleDbSync h = new HandleDbSync();
		h.handle(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	@Override
	public String getServletInfo() {
		return "EWA Database Sync";
	}

	@Override
	public void init() throws ServletException {
	}

	@Override
	public void destroy() {
		super.destroy();
	}
}
