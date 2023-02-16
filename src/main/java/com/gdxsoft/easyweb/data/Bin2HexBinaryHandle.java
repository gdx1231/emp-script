package com.gdxsoft.easyweb.data;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * 二进制转换为hex字符串
 * @author admin
 *
 */
public class Bin2HexBinaryHandle implements IBinaryHandle, Serializable {


	private static final long serialVersionUID = -8344010232410361421L;

	/**
	 * 二进制转换为hex字符串
	 */
	public Bin2HexBinaryHandle() {
	}

	@Override
	public String handle(byte[] buf, String contentpath) {
		if (buf == null) {
			return null;
		}

		return Utils.bytes2hex(buf);
	}

	@Override
	public String handle(byte[] buf, String path, String url) {
		if (buf == null) {
			return null;
		}

		return Utils.bytes2hex(buf);
	}
}
