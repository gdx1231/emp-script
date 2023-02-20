package com.gdxsoft.easyweb.script.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UPath;

public class ServletUpload extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletUpload.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2804223580338447121L;

	/**
	 * Constructor of the object.
	 */
	public ServletUpload() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		JSONObject result = new JSONObject();

		PrintWriter out = response.getWriter();

		// 这样初始化的话，参数不完整，需要在下面继续提取。
		RequestValue rv = new RequestValue(request, request.getSession());

		Upload up = new Upload();
		up.setRv(rv);
		try {
			up.init(request);
		} catch (Exception e1) {
			UJSon.rstSetFalse(result, e1.getMessage());
			out.print(result);
			LOGGER.error(e1.getMessage());
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存缓冲区，超过后写入临时文件
		int bufferSize = 1024 * 1024 * 10; // 10M
		factory.setSizeThreshold(bufferSize);// 设置缓冲区大小，这里是10M
		// 设置临时文件存储位置
		File tempPath = new File(UPath.getPATH_UPLOAD() + "/" + Upload.DEFAULT_UPLOAD_PATH);
		factory.setRepository(tempPath);
		ServletFileUpload upload = new ServletFileUpload(factory);
		long maxSize = 1024 * 1024 * 1024 * 2; // 2g
		upload.setSizeMax(maxSize);

		List<?> items = null;
		try {
			items = upload.parseRequest(request);
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem) items.get(i);
				if (item.isFormField()) {
					// 重点，提取出上传的参数放到rv中
					rv.addValue(item.getFieldName(), item.getString());
				} else {
					// this is a file
				}
			}
		} catch (Exception err) {
			UJSon.rstSetFalse(result, err.getMessage());
			out.print(result);
			LOGGER.error(err.getMessage());
			return;
		}
		
		up.setUploadItems(items);

		try {
			String s = up.upload();
			out.println(s);
			response.setHeader("X-EWA_UP_RET", s);
		} catch (Exception e) {
			UJSon.rstSetFalse(result, e.getMessage());
			out.print(result);
			LOGGER.error(e.getMessage());
		}

	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	/**
	 * Returns information about the servlet, such as author, version, and
	 * copyright.
	 * 
	 * @return String information about this servlet
	 */
	public String getServletInfo() {
		return "EWA(v2.0)";
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
