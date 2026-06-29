<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.gdxsoft.easyweb.script.*"%>
<%@ include file="/inc/inc_exit_login.jsp"%>
<%
/**
 * 处理用户退出登录逻辑的JSP页面
 * 
 * 功能：
 * 1. 当请求参数包含"app"时，返回JSON格式的退出成功响应
 * 2. 当请求参数包含"ref"时，重定向到指定URL
 * 3. 默认情况下根据语言设置重定向到登录页面(login.jsp)
 * 
 * 安全处理：
 * - 对语言参数进行校验，防止XSS攻击
 * - 默认使用中文(zhcn)，仅当明确指定enus时才使用英文
 * 
 * 依赖：
 * - 引入inc_exit_login.jsp处理公共退出逻辑
 * - 使用RequestValue类处理请求参数
 */
//退出登录状态
if (request.getParameter("app") != null) {
	out.println("{\"RST\":true}");
	return;
}  
String ref = request.getParameter("ref");
if (ref != null && ref.trim().length() > 0) {
	System.out.println("跳转到: " + ref);
	response.sendRedirect(ref);
	return;
}
RequestValue g_rv = new RequestValue(request, session);
String _tmp_lang_999 = g_rv.s("EWA_LANG");
if (_tmp_lang_999 == null) {
	_tmp_lang_999 = g_rv.s("SYS_EWA_LANG");
}
//避免跨站脚本攻击漏洞
if (_tmp_lang_999 != null && _tmp_lang_999.equalsIgnoreCase("enus")) {
	_tmp_lang_999 = "enus";
} else {
	_tmp_lang_999 = "zhcn";
}
response.sendRedirect("login.jsp?ewa_lang=" + _tmp_lang_999);
%>