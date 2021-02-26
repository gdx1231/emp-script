package com.gdxsoft.easyweb.utils.Mail;

/**
 * 邮件地址
 * 
 * @author Administrator
 *
 */
public class Addr {
	public Addr() {

	}

	public Addr(String email, String name) {
		this._Email = email;
		this._Name = name;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Email
	 */
	public String getEmail() {
		return _Email;
	}

	/**
	 * @param email
	 *            the _Email to set
	 */
	public void setEmail(String email) {
		_Email = email;
	}

	private String _Name;
	private String _Email;

}
