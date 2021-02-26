package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 已作废，请使用 UNet
 * 
 * @author admin
 *
 */
@Deprecated
public class UHttpClient {
	public static String AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; chromeframe/10.0.648.151; chromeframe;)";
	public static int C_TIME_OUT = 500000;
	public static int R_TIME_OUT = 500000;
	private String _LastUrl;
	private boolean _IsShowLog = true;
	private HashMap<String, String> _Headers;
	private HashMap<String, String> _Cookies;

	public UHttpClient() {
		this._Headers = new HashMap<String, String>();
		_Cookies = new HashMap<String, String>();
	}

	public void addHeader(String key, String v) {
		if (this._Headers.containsKey(key)) {
			this._Headers.remove(key);
		}
		this._Headers.put(key, v);
	}

	public void clearHeaders() {
		this._Headers.clear();
	}

	public UHttpClient(String cookie, String encoder) {
		this._Headers = new HashMap<String, String>();
		_Cookies = new HashMap<String, String>();

		this._Cookie = cookie;
		this._Encode = encoder;
		this.initCookies();

	}

	public String getCookies() {
		MStr s = new MStr();
		Iterator<String> it = this._Cookies.keySet().iterator();
		while (it.hasNext()) {
			if (s.length() > 0) {
				s.a("; ");
			}
			String k = it.next();
			String v = _Cookies.get(k);
			s.a(k);
			s.a("=");
			s.a(v);
		}
		return s.toString();
	}

	void initCookies() {
		String[] cks = _Cookie.split(";");
		for (int i = 0; i < cks.length; i++) {
			String[] kk = cks[i].split("=");
			if (kk.length == 2) {
				this.addCookie(kk[0], kk[1]);
			}
		}
	}

	void addCookie(String name, String val) {
		name = name.trim();
		if (this._Cookies.containsKey(name)) {
			this._Cookies.remove(name);
		}
		this._Cookies.put(name, val);
	}

	public byte[] downloadData(String url) {
		Date t1 = new Date();

		if (this._IsShowLog) {
			System.out.print("G " + url);
		}
		URLConnection con = null;

		try {
			con = this.createConn(url);

			this._LastUrl = url;

			byte[] s1 = this.readData(con);
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s1.length);
			}
			return s1;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		} finally {
			if (con != null) {
				con = null;
			}
		}
	}

	public String doGet(String url) {
		Date t1 = new Date();

		if (this._IsShowLog) {
			System.out.print("G " + url);
		}
		URLConnection con = null;

		try {
			con = this.createConn(url);
			// Iterator<String> it1 =
			// con.getRequestProperties().keySet().iterator();
			// while (it1.hasNext()) {
			// String k = it1.next();
			// List<String> v = con.getRequestProperties().get(k);
			// System.out.println(k + "=" + v);
			// }
			this._LastUrl = url;

			String s1 = this.readContent(con);
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s1.length());
			}
			return s1;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		} finally {
			if (con != null) {
				con = null;
			}
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param url       地址
	 * @param fieldName 上传域名
	 * @param filePath  文件地址
	 * @return
	 */
	public String doUpload(String url, String fieldName, String filePath) {
		File f = new File(filePath);

		HttpURLConnection url_con = null;
		String BOUNDARY = "---------------------------7d4a6d158c9"; // 分隔符
		StringBuffer sb = new StringBuffer();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + f.getName() + "\"\r\n");

		// 上传文件类型
		String ct = "application/octet-stream";
		if (f.getName().toUpperCase().endsWith(".GIF")) {
			ct = "image/gif";
		} else if (f.getName().toUpperCase().endsWith(".JPG")) {
			ct = "image/jpeg";
		} else if (f.getName().toUpperCase().endsWith(".PNG")) {
			ct = "image/png";
		} else if (f.getName().toUpperCase().endsWith(".BMP")) {
			ct = "image/bmp";
		}

		sb.append("Content-Type: " + ct + "\r\n\r\n");

		try {
			url_con = (HttpURLConnection) this.createConn(url);
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(C_TIME_OUT);// （单位：毫秒）jdk
			url_con.setReadTimeout(R_TIME_OUT);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);

			byte[] data = sb.toString().getBytes();
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

			// 上传内容长度
			long contentLength = data.length + f.length() + end_data.length;

			url_con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); // 设置表单类型和分隔符
			url_con.setRequestProperty("Content-Length", String.valueOf(contentLength)); // 设置内容长度

			OutputStream os = url_con.getOutputStream();
			os.write(data);

			// 要上传的文件
			FileInputStream fis = new FileInputStream(f);
			int rn2;
			byte[] buf2 = new byte[1024];
			while ((rn2 = fis.read(buf2, 0, 1024)) > 0) {
				os.write(buf2, 0, rn2);
			}

			os.write(end_data);
			os.flush();
			os.close();
			fis.close();

			return this.readContent((URLConnection) url_con);
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		}
	}

	public String doPost(String url, java.util.HashMap<String, String> vals) {
		Date t1 = new Date();
		if (this._IsShowLog) {
			System.out.println("P " + url);
		}
		HttpURLConnection url_con = null;
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = vals.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String val = vals.get(key);
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(key);
			sb.append("=");
			try {
				sb.append(URLEncoder.encode(val, this._Encode == null ? "UTF-8" : _Encode));
			} catch (UnsupportedEncodingException e) {
				this._LastErr = e.getLocalizedMessage();
				return null;
			}
		}

		try {
			url_con = (HttpURLConnection) this.createConn(url);
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(C_TIME_OUT);// （单位：毫秒）jdk
			url_con.setReadTimeout(R_TIME_OUT);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);
			byte[] b = sb.toString().getBytes();
			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			String s = this.readContent((URLConnection) url_con);

			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s);
			}
			return s;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		}

	}

	byte[] readData(URLConnection u) throws IOException {
		_ReturnUrl = u.getURL();

		String sc = u.getHeaderField("Set-Cookie");
		if (sc != null) {
			String[] scs = sc.split(";");
			for (int i = 0; i < scs.length; i++) {
				String[] scc = scs[i].split("=");
				if (scc.length == 2) {
					if (scc[0].trim().equals("path") || scc[0].trim().equals("domain")) {
						continue;
					}
					this.addCookie(scc[0], scc[1]);
				}
			}
		}
		BufferedInputStream bis = new BufferedInputStream(u.getInputStream());
		ByteBuffer bb = ByteBuffer.allocate(1024 * 1024);
		byte[] buf = new byte[4096];
		int m = 0;
		int read;
		while ((read = bis.read(buf)) > 0) {
			m += read;
			bb.put(buf, 0, read);
		}
		String s;
		byte[] buf1 = new byte[m];
		System.arraycopy(bb.array(), 0, buf1, 0, buf1.length);
		bb.clear();

		return buf1;
	}

	String readContent(URLConnection u) throws IOException {
		_ReturnUrl = u.getURL();
		// Iterator<String> it = u.getHeaderFields().keySet().iterator();
		// while (it.hasNext()) {
		// String k = it.next();
		// List<String> v = u.getHeaderFields().get(k);
		// System.out.println(k + "=" + v);
		// }

		String sc = u.getHeaderField("Set-Cookie");
		if (sc != null) {
			String[] scs = sc.split(";");
			for (int i = 0; i < scs.length; i++) {
				String[] scc = scs[i].split("=");
				if (scc.length == 2) {
					if (scc[0].trim().equals("path") || scc[0].trim().equals("domain")) {
						continue;
					}
					this.addCookie(scc[0], scc[1]);
				}
			}
		}
		BufferedInputStream bis = new BufferedInputStream(u.getInputStream());
		ByteBuffer bb = ByteBuffer.allocate(1024 * 1024);
		byte[] buf = new byte[4096];
		int m = 0;
		int read;
		while ((read = bis.read(buf)) > 0) {
			m += read;
			bb.put(buf, 0, read);
		}
		String s;
		byte[] buf1 = new byte[m];
		System.arraycopy(bb.array(), 0, buf1, 0, buf1.length);
		bb.clear();
		if (this._Encode != null) {
			s = new String(buf1, _Encode);
		} else {
			s = new String(buf1);
		}
		bb.clear();

		bb = null;
		bis.close();

		return s;
	}

	URLConnection createConn(String url) throws IOException {
		URL u = new URL(url);
		URLConnection con = u.openConnection();
		con.addRequestProperty("User-Agent", AGENT);
		if (this._Cookie != null) {
			con.addRequestProperty("Cookie", this.getCookies());
		}
		if (this._LastUrl != null) {
			con.addRequestProperty("Referer", this._LastUrl);
		}

		Iterator<String> it = this._Headers.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String v = this._Headers.get(key);

			con.addRequestProperty(key, v);
		}
		return con;
	}

	private URL _ReturnUrl;

	private String _LastErr;

	/**
	 * @return the _LastErr
	 */
	public String getLastErr() {
		return _LastErr;
	}

	private String _Encode;

	/**
	 * @return the _Encode
	 */
	public String getEncode() {
		return _Encode;
	}

	/**
	 * @param encode the _Encode to set
	 */
	public void setEncode(String encode) {
		_Encode = encode;
	}

	private String _Cookie;

	/**
	 * @return the _Cookie
	 */
	public String getCookie() {
		return _Cookie;
	}

	/**
	 * @param cookie the _Cookie to set
	 */
	public void setCookie(String cookie) {
		_Cookie = cookie;
		this.initCookies();
	}

	/**
	 * @return the _LastUrl
	 */
	public String getLastUrl() {
		return _LastUrl;
	}

	/**
	 * referer
	 * 
	 * @param lastUrl the _LastUrl to set
	 */
	public void setLastUrl(String lastUrl) {
		_LastUrl = lastUrl;
	}

	public URL getReturnUrl() {
		return _ReturnUrl;
	}

	/**
	 * @return the _IsShowLog
	 */
	public boolean isShowLog() {
		return _IsShowLog;
	}

	/**
	 * @param isShowLog the _IsShowLog to set
	 */
	public void setIsShowLog(boolean isShowLog) {
		_IsShowLog = isShowLog;
	}

}
