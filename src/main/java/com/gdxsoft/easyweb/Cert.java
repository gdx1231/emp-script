package com.gdxsoft.easyweb;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class Cert {
	private static Cert CERT;
	private static String DEFAULT_KEY = "dskfrefreio92320934ufsdklfjsdEWASCRIPT_V_2.01932aaskjjs12jjczxc21";

	public synchronized static Cert instance() {

		if (CERT == null) {
			CERT = new Cert();
			CERT._IsOk = false;
			try {
				CERT.init();
				CERT._IsOk = true;
			} catch (Exception err) {
				CERT._Err = err.getMessage();

			}
		}

		return CERT;
	}

	private Cert() {

	}

	private void init() throws ParserConfigurationException, SAXException, IOException {
		String path = UPath.getRealPath() + "/ewa_conf.xml";
		Document doc = UXml.retDocument(path);

		NodeList nl = doc.getElementsByTagName("cert");
		if (nl.getLength() == 0) {
			// 使用默认的key
			this._ByteKey = DEFAULT_KEY.getBytes();
			return;
		}

		Element ele = (Element) nl.item(0);

		_Own = this.getVal(ele, "own");
		_Web = this.getVal(ele, "web");
		this._Start = Utils.getDate(this.getVal(ele, "begin"));
		this._End = Utils.getDate(this.getVal(ele, "end"));
		this._Key = this.getVal(ele, "key");

		if (this._Key == null || this._Key.length() == 0) {
			this._Err = "KEY not exists";
			System.err.println(this._Err);
			return;
		}
		Date today = new Date();
		if (this._Start.getTime() > today.getTime()) {
			this._Err = "未开始" + this._Start;
			System.err.println(this._Err);
			return;
		}

		if (this._End.getTime() < today.getTime()) {
			this._Err = "已经结束" + this._End;
			System.err.println(this._Err);
			return;
		}

		byte[] bytes = null;
		try {
			bytes = UConvert.FromBase64String(this._Key.replace(" ", "").replace("\t", ""));

		} catch (IOException e) {
			this._Err = "Key不合法";
			System.err.println(this._Err);
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("OWN=");
		sb.append(_Own);
		sb.append("WEB=");
		sb.append(_Web);
		sb.append("START=");
		sb.append(this._Start);
		sb.append("END=");
		sb.append(this._End);

		String keyCode = "a" + sb.toString().hashCode();
		byte[] buf1 = keyCode.getBytes();

		byte[] buf2 = new byte[buf1.length + bytes.length];
		System.arraycopy(buf1, 0, buf2, 0, buf1.length);
		System.arraycopy(bytes, 0, buf2, buf1.length, bytes.length);

		this._ByteKey = buf2;

	}

	private String getVal(Element ele, String tagName) {
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl.getLength() == 0) {
			return null;
		}
		String v = nl.item(0).getTextContent();
		if (v == null) {
			return null;
		}
		return v.trim();
	}

	public String getOwn() {
		return _Own;
	}

	public String getWeb() {
		return _Web;
	}

	public Date getStart() {
		return _Start;
	}

	public Date getEnd() {
		return _End;
	}

	public String getKey() {
		return _Key;
	}

	public byte[] getByteKey() {
		return _ByteKey;
	}

	private String _Own;
	private String _Web;
	private Date _Start;
	private Date _End;
	private String _Key;
	private byte[] _ByteKey;
	private boolean _IsOk;
	private String _Err;

	public String getErr() {
		return _Err;
	}

	public boolean isOk() {
		return _IsOk;
	}
}
