package com.gdxsoft.easyweb.data;

public interface IBinaryHandle {

	/**
	 * 处理二进制的方法
	 * 
	 * @param buf
	 *            二进制
	 * @param contentpath
	 *            基础路径
	 * @return
	 */
	public String handle(byte[] buf, String contentpath);

	/**
	 * 处理二进制的方法，转换成可下载文件
	 * 
	 * @param buf
	 *            二进制
	 * @param path
	 *            基础路径
	 * @param url
	 *            http前缀，例如 http://www.gezz.cn/aaa/
	 * @return
	 */
	public String handle(byte[] buf, String path, String url);
}