<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%
// 清除所有登录状态
if (session != null) {
	java.util.Enumeration<?> sessions = session.getAttributeNames();
	while (sessions.hasMoreElements()) {
		String RKey = sessions.nextElement().toString().trim();
		session.removeAttribute(RKey);
	}
}
if (request.getCookies() != null) {
	for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setValue("");

		response.addCookie(cookie);
	}
	for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
		cookie.setMaxAge(0);
		cookie.setPath(request.getContextPath());
		cookie.setValue("");

		response.addCookie(cookie);
	}
}
%>
