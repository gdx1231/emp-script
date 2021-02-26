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

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.uploader.PostData;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.uploader.UploaderPage;
import com.gdxsoft.easyweb.utils.UPath;

public class ServletUpload extends HttpServlet {

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
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		RequestValue rv = new RequestValue(request, request.getSession());

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
					rv.addValue(item.getFieldName(), item.getString());
				}
			}
		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
		request.setCharacterEncoding("utf-8");
		String uploadType = rv.getString("EWA_UP_TYPE");
		PrintWriter out = response.getWriter();

		if (uploadType != null && uploadType.equals("SWFUPLOAD")) {
			Upload up = new Upload();
			up.setRv(rv);
			up.setUploadItems(items);
			up.init(request);

			try {
				String s = up.upload();
				out.println(s);
				response.setHeader("X-EWA_UP_RET", s);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<html>");
			out.println("<head>");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			out.println("<title>UPLOAD</title>");
			out.println("<script type=\"text/javascript\">");
			out.println("var CONTEXT_PATH=\"" + request.getContextPath() + "\";");
			out.println("</script>");
			out.println("</head>");
			out.println("<body style='margin: 0px; overflow: hidden'>");
			UploaderPage up = new UploaderPage();
			try {
				up.init(rv);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String upType = request.getParameter("EWA_UP_STATUS");
			upType = upType == null ? "" : upType.trim().toUpperCase();
			if (upType.equals("")) {
				out.println(up.getHtml());
			} else {
				PostData pd = new PostData();
				pd.init(request, response, up.getUpPath());
				if (upType.equals("CANCEL")) {
					pd.cancelUpload();
				} else if (upType.equals("STATUS")) {
					pd.queryStatus();
				} else if (upType.equals("UPLOAD")) {
					pd.doPost();
				}
			}
			out.println("</body></html>");
		}
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
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
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
