package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GZIPEncodableResponse extends HttpServletResponseWrapper {
	private GZIPServletStream wrappedOut;

	public GZIPEncodableResponse(HttpServletResponse response)
			throws IOException {
		super(response);
		wrappedOut = new GZIPServletStream(response.getOutputStream());
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return wrappedOut;
	}

	private PrintWriter wrappedWriter;

	public PrintWriter getWriter() throws IOException {
		if (wrappedWriter == null) {
			wrappedWriter = new PrintWriter(new OutputStreamWriter(
					getOutputStream(), getCharacterEncoding()));
		}
		return wrappedWriter;
	}

	public void flush() throws IOException {
		if (wrappedWriter != null) {
			wrappedWriter.flush();
		}
		wrappedOut.finish();
	}
}
