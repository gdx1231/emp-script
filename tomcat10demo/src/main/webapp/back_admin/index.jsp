<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    import="com.gdxsoft.easyweb.script.HtmlControl,
            com.gdxsoft.easyweb.script.RequestValue"
%><%
    request.setCharacterEncoding("UTF-8");
    String adminId = (String) session.getAttribute("G_ADM_ID");
    String adminName = (String) session.getAttribute("G_ADM_NAME");

    // 未登录跳转
    if (adminId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // 退出
    String action = request.getParameter("action");
    if ("logout".equals(action)) {
        session.invalidate();
        response.sendRedirect("login.jsp");
        return;
    }

    // 通过 EWA HtmlControl 渲染页面
    String xmlName = request.getParameter("XMLNAME");
    String itemName = request.getParameter("ITEMNAME");

    // 默认加载后台管理首页
    if (xmlName == null || xmlName.isEmpty()) {
        xmlName = "/meta-data/menu/menu.xml";
        itemName = "adm_menu.Menu.Modify";
    }

    RequestValue rv = new RequestValue(request, session);
    HtmlControl ht = new HtmlControl();
    ht.init(xmlName, itemName, null, request, session, response);
    ht.setAjaxCallUrl(request.getContextPath() + "/ewa");

    // 验证码等特殊请求直接由 HtmlControl 处理输出
    if (rv.s("ewa_ajax") != null) {
        ht.getHtml();
        return;
    }

    // 普通页面请求
    String pageContent = ht.getAllHtml();
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().print(pageContent);
%>
