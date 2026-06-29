<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Tomcat 10 Embedded Demo</title>
    <style>
        body { font-family: -apple-system, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #333; }
        .card { background: #f5f5f5; border-radius: 8px; padding: 20px; margin: 20px 0; }
        a { color: #0066cc; text-decoration: none; }
        a:hover { text-decoration: underline; }
        ul { line-height: 2; }
    </style>
</head>
<body>
    <h1>Tomcat 10 Embedded Demo</h1>
    <p>emp-script 嵌入式演示服务</p>

    <div class="card">
        <h3>演示链接</h3>
        <ul>
        	<li><a target=_blank href="/EWA_DEFINE/index.jsp">EWA frames 管理</a> - 用户名：demo, 密码：demo12345</li>
            <li><a target=_blank href="/back_admin/login.jsp">后台管理</a> - 管理系统入口，用户名：root, 密码：demo12345</li>
        </ul>
    </div>

    <div class="card">
        <h3>数据库</h3>
        <p>HSQLDB Server 模式，端口 11002</p>
        <ul>
            <li>emp_ewa</li>
            <li>emp_portal </li>
        </ul>
    </div>
</body>
</html>
