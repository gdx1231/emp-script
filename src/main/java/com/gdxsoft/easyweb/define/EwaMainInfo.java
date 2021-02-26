package com.gdxsoft.easyweb.define;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class EwaMainInfo {
	private String _Copyright; // 版权
	private String _Company; // 公司
	private String _Website; // 网站
	private String _Contact; // 联系人
	private String _Email; // 电子邮件
	private String _Telephone; // 电话
	private String _Version; // 外部版本

	private static EwaMainInfo MAIN_INFO;

	/**
	 * 获取公司信息
	 * @return
	 */
	public synchronized static EwaMainInfo getInstance() {
		if (MAIN_INFO != null) {
			//return MAIN_INFO;
		}

		MAIN_INFO = new EwaMainInfo();
		String xmlPath = UPath.getConfigPath()+"/EwaMainInfo.xml";
		try {
			Document doc = UXml.retDocument(xmlPath);
			Node node = UXml.retNode(doc.getFirstChild(), "MainInfo");
			UObjectValue.fromXmlNodes((Element) node, MAIN_INFO);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return MAIN_INFO;
	}
	

	/**
	 * @return the _Copyright
	 */
	public String getCopyright() {
		return _Copyright;
	}

	/**
	 * @param copyright
	 *            the _Copyright to set
	 */
	public void setCopyright(String copyright) {
		_Copyright = copyright;
	}

	/**
	 * @return the _Company
	 */
	public String getCompany() {
		return _Company;
	}

	/**
	 * @param company
	 *            the _Company to set
	 */
	public void setCompany(String company) {
		_Company = company;
	}

	/**
	 * @return the _Website
	 */
	public String getWebsite() {
		return _Website;
	}

	/**
	 * @param website
	 *            the _Website to set
	 */
	public void setWebsite(String website) {
		_Website = website;
	}

	/**
	 * @return the _Contact
	 */
	public String getContact() {
		return _Contact;
	}

	/**
	 * @param contact
	 *            the _Contact to set
	 */
	public void setContact(String contact) {
		_Contact = contact;
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

	/**
	 * @return the _Telephone
	 */
	public String getTelephone() {
		return _Telephone;
	}

	/**
	 * @param telephone
	 *            the _Telephone to set
	 */
	public void setTelephone(String telephone) {
		_Telephone = telephone;
	}

	/**
	 * @return the _Version
	 */
	public String getVersion() {
		return _Version;
	}

	/**
	 * @param version
	 *            the _Version to set
	 */
	public void setVersion(String version) {
		_Version = version;
	}

}
