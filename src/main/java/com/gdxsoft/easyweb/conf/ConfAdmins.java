package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class ConfAdmins {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfAdmins.class);
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

	/**
	 * The define admin login
	 * 
	 * @param loginId
	 * @param password
	 * @return
	 */
	public static ConfAdmin login(String loginId, String password) {
		if (!ConfDefine.isAllowDefine()) {
			// Deny configuration files management
			return null;
		}

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

			if (StringUtils.isBlank(sp.getLoginId())) {
				String randomLoginId = Utils.randomStr(8);
				sp.setLoginId(randomLoginId);
			}

			if (StringUtils.isBlank(sp.getPassword()) || sp.getPassword().length() < 8) {
				String randomPassword = Utils.randomStr(16);
				sp.setPassword(randomPassword);
				LOGGER.info("管理员：" + sp.getLoginId() + ", 临时密码：" + sp.getPassword());
				LOGGER.info("Admin: " + sp.getLoginId() + ", temp password is: " + sp.getPassword());
			}

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
