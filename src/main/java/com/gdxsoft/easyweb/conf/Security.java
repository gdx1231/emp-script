/**
 * 
 */
package com.gdxsoft.easyweb.conf;

public class Security {
	private String algorithm;
	private String key;
	private String iv;
	private String aad;
	private int macBitSize = 128;

	private boolean base64Encoded;

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public String getAad() {
		return aad;
	}

	public void setAad(String aad) {
		this.aad = aad;
	}

	public int getMacBitSize() {
		return macBitSize;
	}

	public void setMacBitSize(int macBitSize) {
		this.macBitSize = macBitSize;
	}

	public boolean isBase64Encoded() {
		return base64Encoded;
	}

	public void setBase64Encoded(boolean base64Encoded) {
		this.base64Encoded = base64Encoded;
	}

}
