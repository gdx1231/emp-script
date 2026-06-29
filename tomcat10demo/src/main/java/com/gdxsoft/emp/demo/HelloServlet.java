package com.gdxsoft.emp.demo;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 演示 Servlet
 */
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Hello</title></head><body>");
            out.println("<h1>Hello from Tomcat 10 Embedded!</h1>");
            out.println("<p>emp-script 嵌入式演示服务运行中</p>");
            out.println("<ul>");
            out.println("<li><a href='/'>首页</a></li>");
            out.println("<li><a href='/hello.jsp'>JSP 演示页</a></li>");
            out.println("</ul>");
            out.println("</body></html>");
        }
    }
}
