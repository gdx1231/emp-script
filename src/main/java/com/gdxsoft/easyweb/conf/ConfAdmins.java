package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfAdmins {

	private static ConfAdmins INST = null;

	private static long PROP_TIME = 0;

	public static ConfAdmins getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createConfAdmins();
		return INST;
	}

	public static ConfAdmin login(String loginId, String password) {
		return getInstance().getAdm(loginId, password);
	}

	private synchronized static ConfAdmins createConfAdmins() {
		/*
		 * <scriptPaths> <scriptPath name="/ewa" path="resources:/user.xml/ewa" />
		 * <scriptPath name="/" path="jdbc:ewa" /> </scriptPaths>
		 */
		ConfAdmins sps = new ConfAdmins();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("Admins");
		if (nl.getLength() == 0) {
			nl = UPath.getCfgXmlDoc().getElementsByTagName("admins");
			
		}
		if (nl.getLength() == 0) {
			return null;
		}
		Element admins = (Element) nl.item(0);
		NodeList nlAdm = admins.getElementsByTagName("Admin");
		if (nlAdm.getLength() == 0) {
			nlAdm = admins.getElementsByTagName("admin");
		}
		
		if (nlAdm.getLength() == 0) {
			return null;
		}

		for (int i = 0; i < nlAdm.getLength(); i++) {
			Element item = (Element) nlAdm.item(i);
			ConfAdmin sp = new ConfAdmin();

			/*
			 * Map<String, String> vals = UXml.getElementAttributes(item, true);
			 * sp.setUserName( vals.get("username") );
			 * sp.setCreateDate(vals.get("createdate")); sp.setLoginId(vals.get("loginid"));
			 * sp.setPassword(vals.get("password"));
			 */
			UObjectValue uo = new UObjectValue();
			
			
			uo.setObject(sp);
			uo.setAllValue(item);

			sps.lst.add(sp);
		}

		return sps;
	}

	private List<ConfAdmin> lst = new ArrayList<>();

	/**
	 * Get the ConfAdmin by the user name
	 * 
	 * @param username the ConfAdmin.Username
	 * @return ScriptPath
	 */
	public ConfAdmin getAdm(String loginId, String password) {
		if (StringUtils.isBlank(password) || StringUtils.isBlank(password)) {
			return null;
		}
		for (int i = 0; i < this.lst.size(); i++) {
			ConfAdmin adm = this.lst.get(i);
			if (password.equals(adm.getPassword()) && loginId.equals(adm.getLoginId())) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	public List<ConfAdmin> getLst() {
		return lst;
	}
}
