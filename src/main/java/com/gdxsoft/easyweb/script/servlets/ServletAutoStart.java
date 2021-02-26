package com.gdxsoft.easyweb.script.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.UPath;

@SuppressWarnings("serial")
public class ServletAutoStart extends HttpServlet {
	@Override
	/**
	 *  自动启动，加载参数等
	 */
	public void init() throws ServletException {
		// cache 启动
		String path = this.getServletContext().getRealPath("/");
		System.out.println("启动 Context[" + path + "]");
		System.out.println("启动 SqlCached ...");
		
		//设置Context 所在物理目录
		UPath.PATH_REAL=path+"WEB-INF/classes/";
		System.out.println("PATH_REAL="+UPath.getRealPath());
		System.out.println("PATH_REAL_Context="+UPath.getRealContextPath());
		
		
		SqlCached aa = SqlCached.getInstance();
		boolean rst = aa.add(
				"com.gdxsoft.easyweb.script.servlets.ServletAutoStart",
				"Start!");
		if (!rst) {
			throw new ServletException("ServletAutoStart-SqlCached 不能启动");
		}
		System.out.println("启动 ULogic ...");
		// logic 启动
		ULogic.runLogic("1=1");

		System.out.println("ServletAutoStart 启动 完成");
	}
}