package com.gdxsoft.easyweb.cache;

import com.gdxsoft.easyweb.utils.Utils;

public class JedisKey {

	public static JedisKey getKey(String key) {
		JedisKey jk = new JedisKey(key);
		return jk;
	}

	private String key;
	private String md5;

	public JedisKey(String key) {
		this.key = key;
		this.md5 = Utils.md5(key);
	}

	public String getBinaryKey() {
		String keyBin = "SC_BI_" + md5;
		return keyBin;
	}

	public String getTextKey() {
		String keyBin = "SC_TX_" + md5;
		return keyBin;
	}


	public String getKey() {
		return this.key;
	}

	public String getMd5() {
		return this.md5;
	}

}