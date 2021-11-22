package com.gdxsoft.easyweb.resources;

public class Resource {

	private String path;
	private String type;
	private String content;
	private byte[] buffer;
	private int status = 200;
	private boolean binary;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setBinary(boolean b) {
		this.binary = b;
	}

	public boolean isBinary() {
		return binary;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append(", ");
		sb.append(this.status);
		sb.append(", ");
		sb.append(this.type);

		return sb.toString();
	}
}
