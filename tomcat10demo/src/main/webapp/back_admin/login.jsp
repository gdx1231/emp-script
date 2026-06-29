<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    import="com.gdxsoft.easyweb.conf.ConfAdmins,
            com.gdxsoft.easyweb.conf.ConfAdmin,
            com.gdxsoft.easyweb.script.HtmlControl,
            com.gdxsoft.easyweb.script.RequestValue"
%><%
    request.setCharacterEncoding("UTF-8");

    // 使用 HtmlControl 渲染 EWA 登录页面
    RequestValue rv = new RequestValue(request, session);
    HtmlControl ht = new HtmlControl();
    ht.init("/meta-data/organization/admin.xml", "ADM_USER.F.Login", null, request, session, response);

    // 验证码等特殊请求直接由 HtmlControl 处理输出
    if (rv.s("ewa_ajax") != null) {
        out.println( ht.getHtml());
        return;
    }

    // 普通页面请求
    String loginHtml = ht.getAllHtml();
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().print(loginHtml);
%>
