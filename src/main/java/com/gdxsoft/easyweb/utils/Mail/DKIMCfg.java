package com.gdxsoft.easyweb.utils.Mail;

/**
 * DKIM签名配置 DKIM for JavaMail needs the private key in DER format, you can
 * transform a PEM key openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out
 * private.key.der -outform der
 * 
 * @author admin
 *
 */
public class DKIMCfg {

	public DKIMCfg() {
	}

	private String domain_;
	private String select_ = "default";
	private String privateKeyPath_;

	/**
	 * DKIM签名的域名
	 * 
	 * @return the domain_
	 */
	public String getDomain() {
		return domain_;
	}

	/**
	 * DKIM签名的域名
	 * 
	 * @param domain_
	 *            the domain_ to set
	 */
	public void setDomain(String domain) {
		this.domain_ = domain;
	}

	/**
	 * DKIM签名的选择域
	 * 
	 * @return the select_
	 */
	public String getSelect() {
		return select_;
	}

	/**
	 * DKIM签名的选择域
	 * 
	 * @param select_
	 *            the select_ to set
	 */
	public void setSelect(String select) {
		this.select_ = select;
	}

	/**
	 * DKIM签名的私匙 <br>
	 * DKIM for JavaMail needs the private key in DER format, you can transform a
	 * PEM key <br>
	 * <b>openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der
	 * -outform der</b>
	 * 
	 * @return the privateKeyPath_
	 */
	public String getPrivateKeyPath() {
		return privateKeyPath_;
	}

	/**
	 * DKIM签名的私匙 <br>
	 * DKIM for JavaMail needs the private key in DER format, you can transform a
	 * PEM key <br>
	 * <b>openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der
	 * -outform der</b>
	 * 
	 * @param privateKeyPath_
	 *            the privateKeyPath_ to set
	 */
	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath_ = privateKeyPath;
	}
}
