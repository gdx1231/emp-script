package com.gdxsoft.easyweb.data;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.UConvert;

/**
 * 二进制转换为base64字符串
 * 
 * @author admin
 *
 */
public class Bin2Base64BinaryHandle implements IBinaryHandle, Serializable {

	private static final long serialVersionUID = 1847204375152402289L;

	/**
	 * 二进制转换为base64字符串
	 */
	public Bin2Base64BinaryHandle() {
	}

	@Override
	public String handle(byte[] buf, String contentpath) {
		if (buf == null) {
			return null;
		}

		return UConvert.ToBase64String(buf);
	}

	@Override
	public String handle(byte[] buf, String path, String url) {
		if (buf == null) {
			return null;
		}

		return UConvert.ToBase64String(buf);
	}
}
