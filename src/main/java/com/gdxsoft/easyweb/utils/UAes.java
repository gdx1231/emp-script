package com.gdxsoft.easyweb.utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.gdxsoft.easyweb.Cert;

/**
 * 使用DES加密与解密,可对byte[],String类型进行加密与解密 密文可使用String,byte[]存储. 方法: void
 * getKey(String strKey)从strKey的字条生成一个Key String getEncString(String
 * strMing)对strMing进行加密,返回String密文 String getDesString(String
 * strMi)对strMin进行解密,返回String明文 byte[] getEncCode(byte[] byteS)byte[]型的加密 byte[]
 * getDesCode(byte[] byteD)byte[]型的解密
 */
public class UAes {
	private static UAes instance;
	/*
	 * AES/CBC/NoPadding 要求 密钥必须是16位的；Initialization vector (IV) 必须是16位
	 * 待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出如下异常： javax.crypto.IllegalBlockSizeException:
	 * Input length not multiple of 16 bytes
	 * 
	 * 由于固定了位数，所以对于被加密数据有中文的, 加、解密不完整
	 * 
	 * 可 以看到，在原始数据长度为16的整数n倍时，假如原始数据长度等于16*n，则使用NoPadding时加密后数据长度等于16*n， 其它情况下加密数据长
	 * 度等于16*(n+1)。在不足16的整数倍的情况下，假如原始数据长度等于16*n+m[其中m小于16]， 除了NoPadding填充之外的任何方
	 * 式，加密数据长度都等于16*(n+1).
	 */
	private static String DEF_METHOD = "AES/CBC/NoPadding";
	private SecretKeySpec keySpec;
	private IvParameterSpec ivSpec;
	private Cipher encCipher;
	private Cipher deCipher;
	private String method; // aes transformation 加密模式
	static {
		//如果是PKCS7Padding填充方式，则必须加上下面这行
		Security.addProvider(new BouncyCastleProvider());
	}
	public synchronized static UAes getInstance() throws Exception {
		if (instance == null) {

			Cert ins = Cert.instance();
			if (ins != null && ins.isOk()) {
				instance = new UAes();

				instance.createKey(ins.getByteKey());

			} else {
				instance = null;
				throw new Exception("CERT错误:" + ins.getErr());

			}
		}
		return instance;
	}

	public UAes() {

	}

	public UAes(String key, String iv) {
		byte[] ivBuf = iv.getBytes();
		byte[] keyBuf = key.getBytes();

		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long
		System.arraycopy(ivBuf, 0, ivBytes, 0, ivBytes.length);

		byte[] key128Bits = new byte[16]; // 128bit

		System.arraycopy(keyBuf, 0, key128Bits, 0, key128Bits.length);

		SecretKeySpec keyspec = new SecretKeySpec(key128Bits, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

		this.keySpec = keyspec;
		this.ivSpec = ivspec;
	}

	public UAes(byte[] keyBuf, byte[] ivBuf) {
		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long
		System.arraycopy(ivBuf, 0, ivBytes, 0, ivBytes.length);

		/**
		 * 设置AES密钥长度
		 * AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
		 * 如需192位或256位，则需要到oracle官网找到对应版本的jdk下载页，在"Additional Resources"中找到
		 * "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files",点击[DOWNLOAD]下载
		 * 将下载后的local_policy.jar和US_export_policy.jar放到jdk安装目录下的jre/lib/security/目录下，替换该目录下的同名文件
		 */
		
		byte[] key128Bits = new byte[16]; // 128bit

		System.arraycopy(keyBuf, 0, key128Bits, 0, key128Bits.length);

		SecretKeySpec keyspec = new SecretKeySpec(key128Bits, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

		this.keySpec = keyspec;
		this.ivSpec = ivspec;
	}

	/**
	 * 加密明文
	 * 
	 * @param strMing 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	public String encode(String strMing) throws Exception {
		return this.getEncString(strMing);
	}

	/**
	 * 解密
	 * 
	 * @param base64Mi base64编码的密文
	 * @return
	 * @throws Exception
	 */
	public String decode(String base64Mi) throws Exception {
		return this.getDesString(base64Mi);
	}

	/**
	 * 加密明文
	 * 
	 * @param strMing 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	public String getEncString(String strMing) throws Exception {

		try {
			byte[] byteMi = this.getEncBytes(strMing);
			String strMi = UConvert.ToBase64String(byteMi);
			return strMi;
		} catch (Exception e) {
			throw e;
		} finally {
		}

	}

	/**
	 * 加密明文
	 * 
	 * @param strMing 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	public byte[] getEncBytes(String strMing) throws Exception {
		byte[] byteMi = null;
		byte[] byteMing = null;

		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = this.getEncCode(byteMing);

		} catch (Exception e) {
			throw e;
		}
		return byteMi;
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
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param strMi
	 * @return
	 * @throws Exception
	 */
	public String getDesString(byte[] byteMi) throws Exception {
		byte[] byteMing = null;
		String strMing = "";
		try {
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
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param strMi
	 * @return
	 * @throws Exception
	 */
	public byte[] getDesBytes(byte[] byteMi) throws Exception {
		byte[] byteMing = null;
		try {
			byteMing = this.getDesCode(byteMi);
		} catch (Exception e) {
			throw e;
		}
		return byteMing;
	}

	public String getAesMethod() {
		if (this.method == null) {
			return DEF_METHOD;
		} else {
			return this.method;
		}
	}

	/**
	 * 加密以byte[]明文输入,byte[]密文输出
	 * 
	 * @param byteS
	 * @return
	 * @throws Exception
	 */
	private byte[] getEncCode(byte[] byteS) throws Exception {
		// AES/CBC/PKCS5Padding
		// AES/CBC/NoPadding
		if (this.encCipher == null) {
			Cipher cipher = Cipher.getInstance(getAesMethod());
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

			this.encCipher = cipher;
		}
		byte[] byteFina = null;

		// 填充Padding
		int blockSize = encCipher.getBlockSize();
		int plaintextLength = byteS.length;
		if (plaintextLength % blockSize != 0) {
			plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
		}
		byte[] plaintext = new byte[plaintextLength];
		System.arraycopy(byteS, 0, plaintext, 0, byteS.length);

		byteFina = encCipher.doFinal(plaintext);

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
		byte[] byteFina = null;
		if (deCipher == null) {
			Cipher cipher = Cipher.getInstance(getAesMethod());
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			this.deCipher = cipher;
		}

		byteFina = this.deCipher.doFinal(byteD);
		return byteFina;
	}

	/**
	 * 根据参数生成KEY
	 * 
	 * @throws Exception
	 */
	public void createKey(byte[] keyBytes) throws Exception {
		if (keyBytes.length < 16) {
			throw new Exception("key长度>=16bytes");
		}
		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long
		for (int i = 0; i < ivBytes.length; i++) {
			ivBytes[i] = keyBytes[keyBytes.length - 1 - i];
		}
		// System.arraycopy(keyBytes, 0, ivBytes, 0, 16);

		byte[] key128Bits = new byte[16]; // 128bit

		System.arraycopy(keyBytes, 0, key128Bits, 0, key128Bits.length);

		SecretKeySpec keyspec = new SecretKeySpec(key128Bits, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

		this.keySpec = keyspec;
		this.ivSpec = ivspec;
	}

	public SecretKeySpec getKeySpec() {
		return keySpec;
	}

	public void setKeySpec(SecretKeySpec keySpec) {
		this.keySpec = keySpec;
	}

	public IvParameterSpec getIvSpec() {
		return ivSpec;
	}

	public void setIvSpec(IvParameterSpec ivSpec) {
		this.ivSpec = ivSpec;
	}

	public Cipher getEncCipher() {
		return encCipher;
	}

	public void setEncCipher(Cipher encCipher) {
		this.encCipher = encCipher;
	}

	public Cipher getDeCipher() {
		return deCipher;
	}

	public void setDeCipher(Cipher deCipher) {
		this.deCipher = deCipher;
	}

	// AES transformation 加密模式
	public String getMethod() {
		return method;
	}

	// AES transformation 加密模式
	public void setMethod(String method) {
		this.method = method;
	}

	public static void main(String[] args) {
		byte[] keyBytes = "12893sjdfdjklshfjkdsh129830912831hdsjfhdsjfsd1283910283019238".getBytes();
		byte[] ivBytes = "ksdjfsdlkfjsdjfsdflsdmf撒旦卡仕达上岛咖啡多少ksd".getBytes();
		
		UAes aa = new UAes(keyBytes, ivBytes);
		aa.setMethod("AES/CBC/PKCS7Padding");
		String cnt = "1234567890北京 abc";
		System.out.println(cnt);
		try {
			String bb = aa.getEncString(cnt);
			System.out.println(bb);
			String cc = aa.getDesString(bb).trim();
			System.out.println(cc);
			System.out.println(cc.equals(cnt));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
