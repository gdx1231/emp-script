package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GZipOut {

	private boolean _IsGZip;
	private String _GZipTag;
	private HttpServletResponse _Response;
	private HttpServletRequest _Request;

	public GZipOut(HttpServletRequest request, HttpServletResponse response) {
		_Response = response;
		_Request = request;
		checkGZIPEncoding();
	}

	public void outContent(String cnt) throws ServletException, IOException {
		if (cnt == null) {
			return;
		}
		if (this._IsGZip && cnt.length() > 0) {
			_Response.setHeader("X-EWA", "gzip");
			this.outGZip(cnt);
		} else {
			_Response.setHeader("X-EWA", "nogzip");
			try {
				PrintWriter out = _Response.getWriter();
				out.print(cnt);
				out.flush();
				out.close();
			} catch (IllegalStateException e) {
				// getOutputStream() 已被调用（如图片/文件下载），降级用 OutputStream 输出
				ServletOutputStream out = _Response.getOutputStream();
				out.print(cnt);
				out.flush();
				out.close();
			}
		}
	}

	public void outGZip(String cnt) throws ServletException, IOException {
		_Response.setHeader("Content-Encoding", "gzip");
		ServletOutputStream output;
		try {
			output = _Response.getOutputStream();
		} catch (IllegalStateException e) {
			// getWriter() 已被调用，降级用 Writer 输出（放弃 gzip）
			PrintWriter writer = _Response.getWriter();
			writer.print(cnt);
			writer.flush();
			writer.close();
			return;
		}
		GZIPOutputStream o = new GZIPOutputStream(output);
		
//		int loc0=cnt.toString().indexOf("EWA_ITEMS_XML_");
//		int loc1=cnt.toString().indexOf("\";",loc0);
//		String tmp=cnt.toString().substring(loc0,loc1);
//		System.out.println(tmp);
		
		byte[] buf = cnt.getBytes("UTF-8");
		//_Response.setHeader("Content-Length", buf.length + "");
		o.write(buf);
		o.flush();
		o.close();
		output.flush();
		output.close();
		//long b = System.currentTimeMillis();
		
		//System.out.println(b-a);
	}

	private void checkGZIPEncoding() {
		String acceptEncoding = _Request.getHeader("Accept-Encoding");
		this._GZipTag = acceptEncoding;
		this._IsGZip = false;
		if (acceptEncoding == null)
			return;
		acceptEncoding = acceptEncoding.toLowerCase();
		if (acceptEncoding.indexOf("x-gzip") >= 0) {
			this._IsGZip = true;
			return;
		}
		if (acceptEncoding.indexOf("gzip") >= 0) {
			this._IsGZip = true;
			return;
		}
	}

	/**
	 * @return the _IsGZip
	 */
	public boolean isGZip() {
		return _IsGZip;
	}

	/**
	 * @return the _GZipTag
	 */
	public String getGZipTag() {
		return _GZipTag;
	}

	/**
	 * @return the _Response
	 */
	public HttpServletResponse getResponse() {
		return _Response;
	}

	/**
	 * @return the _Request
	 */
	public HttpServletRequest getRequest() {
		return _Request;
	}

}
