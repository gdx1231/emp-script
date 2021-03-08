/**
 * 
 */
package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.IUSymmetricEncyrpt;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UDes;

public class Security {
	private static Logger LOGGER = LoggerFactory.getLogger(Security.class);
	private String algorithm;
	private String key;
	private String iv;
	private String aad;
	private int macBitSize = 128;

	private boolean base64Encoded;

	/**
	 * Create a new symmetric instance (AES/DES)
	 * 
	 * @return the new symmetric instance
	 */
	public IUSymmetricEncyrpt createSymmetric() {
		if (this.algorithm.toUpperCase().indexOf("AES") == 0) {
			UAes aes = new UAes(this.key, this.iv, this.algorithm.toUpperCase());
			if (aes.getBlockCipherMode().equals("GCM") || aes.getBlockCipherMode().equals("CCM")) {
				aes.setAdditionalAuthenticationData(aad);
				aes.setMacSizeBits(macBitSize);

			}
			return aes;
		} else if (this.algorithm.toUpperCase().indexOf("DES") == 0) {
			try {
				UDes des = new UDes(this.key, this.iv);
				return des;
			} catch (Exception e) {
				LOGGER.warn("DES", e.getLocalizedMessage());
				return null;
			}
		} else {
			LOGGER.warn("Invalid algorithm " + this.algorithm);
			return null;
		}
	}

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
