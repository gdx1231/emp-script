<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP Demo</title>
</head>
<body>
    <h1>JSP 演示页</h1>
    <p>当前时间: <%= new java.util.Date() %></p>
    <p>服务器信息: <%= application.getServerInfo() %></p>
    <p>Session ID: <%= session.getId() %></p>
    <p><a href="/">返回首页</a></p>
</body>
</html>
