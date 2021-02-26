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
		if (this._IsGZip && cnt != null && cnt.length() > 0) {
			_Response.setHeader("X-EWA", "gzip");
			this.outGZip(cnt);
		} else {
			_Response.setHeader("X-EWA", "nogzip");
			PrintWriter out = _Response.getWriter();
			out.print(cnt == null ? "" : cnt);
			out.flush();
			out.close();
		}
	}

	public void outGZip(String cnt) throws ServletException, IOException {
		//long a = System.currentTimeMillis();
		_Response.setHeader("Content-Encoding", "gzip");
		ServletOutputStream output = _Response.getOutputStream();
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
