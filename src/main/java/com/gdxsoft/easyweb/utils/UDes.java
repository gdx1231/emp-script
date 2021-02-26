package com.gdxsoft.easyweb.utils;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 使用DES加密与解密,可对byte[],String类型进行加密与解密 密文可使用String,byte[]存储. 方法: void
 * getKey(String strKey)从strKey的字条生成一个Key String getEncString(String
 * strMing)对strMing进行加密,返回String密文 String getDesString(String
 * strMi)对strMin进行解密,返回String明文 byte[] getEncCode(byte[] byteS)byte[]型的加密 byte[]
 * getDesCode(byte[] byteD)byte[]型的解密
 */
public class UDes {
	private static Key DES_KEY;
	public static String DES_KEY_VALUE = "EWASCRIPT_V_2.01932aaskjjs12jjczxc21";
	public static String DES_IV_VALUE = "xxxsdskd";
	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

	public UDes() throws Exception {
		if (DES_KEY == null) {
			this.createKey(DES_KEY_VALUE);// 生成密匙
		}
	}

	/**
	 * 根据参数生成KEY
	 * 
	 * @throws Exception
	 */
	public synchronized void createKey(String strKey) throws Exception {
		try {
			DESKeySpec dks = new DESKeySpec(strKey.getBytes());

			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			DES_KEY = secretKey;

			// KeyGenerator _generator = KeyGenerator.getInstance("DES");
			// _generator.init(new SecureRandom(strKey.getBytes()));
			// DES_KEY = _generator.generateKey();
			// _generator = null;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 加密String明文输入,String密文输出
	 * 
	 * @throws Exception
	 */
	public String getEncString(String strMing) throws Exception {
		byte[] byteMi = null;
		byte[] byteMing = null;
		String strMi = "";
		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = this.getEncCode(byteMing);
			strMi = UConvert.ToBase64String(byteMi);
		} catch (Exception e) {
			throw e;
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMi;
	}

	/**
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param strMi
	 * @return
	 * @throws Exception
	 */
	public String getDesString(String strMi) throws Exception {
		byte[] byteMing = null;
		byte[] byteMi = null;
		String strMing = "";
		try {
			byteMi = UConvert.FromBase64String(strMi);
			byteMing = this.getDesCode(byteMi);
			strMing = new String(byteMing, "UTF8");
		} catch (Exception e) {
			throw e;
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMing;
	}

	/**
	 * 加密以byte[]明文输入,byte[]密文输出
	 * 
	 * @param byteS
	 * @return
	 * @throws Exception
	 */
	private byte[] getEncCode(byte[] byteS) throws Exception {
		byte[] byteFina = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec(DES_IV_VALUE.getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.ENCRYPT_MODE, DES_KEY, paramSpec);

			// cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
			// cipher.init(Cipher.ENCRYPT_MODE, DES_KEY);
			byteFina = cipher.doFinal(byteS);
		} catch (Exception e) {
			throw e;
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * 解密以byte[]密文输入,以byte[]明文输出
	 * 
	 * @param byteD
	 * @return
	 * @throws Exception
	 */
	private byte[] getDesCode(byte[] byteD) throws Exception {
		Cipher cipher;
		byte[] byteFina = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec(DES_IV_VALUE.getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.DECRYPT_MODE, DES_KEY, paramSpec);
			byteFina = cipher.doFinal(byteD);
		} catch (Exception e) {
			throw e;
		} finally {
			cipher = null;
		}
		return byteFina;
	}
}
