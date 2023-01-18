package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
//import javax.servlet.WriteListener;
import javax.servlet.WriteListener;




public class GZIPServletStream extends ServletOutputStream {
	private GZIPOutputStream outputStream;

	public GZIPServletStream(OutputStream source) throws IOException {
		outputStream = new GZIPOutputStream(source);
	}

	public void finish() throws IOException {
		outputStream.finish();
	}

	public void write(byte[] buf) throws IOException {
		outputStream.write(buf);
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		outputStream.write(buf, off, len);
	}

	public void write(int c) throws IOException {
		outputStream.write(c);
	}

	public void flush() throws IOException {
		outputStream.flush();
	}

	public void close() throws IOException {
		outputStream.close();
	}

	@Override
	public boolean isReady() {
		 
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
		 
		
	}
}