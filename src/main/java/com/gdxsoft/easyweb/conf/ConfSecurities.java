package com.gdxsoft.easyweb.conf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UDes;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class ConfSecurities {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfSecurities.class);
	private static ConfSecurities INST = null;

	private static long PROP_TIME = 0;

	public static ConfSecurities getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createNewConfSecurities();
		return INST;
	}

	private synchronized static ConfSecurities createNewConfSecurities() {
//		<security default="true" aligorithm="aes-192-gcm"
//				macBitSize="32" aad="xxx"
//				key="xxxx" iv="xxx" base64Encoded="true"/>
		ConfSecurities sps = new ConfSecurities();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("security");
		if (nl.getLength() == 0) {
			ConfSecurity compatible = getCompatibleConf();
			if (compatible != null) {
				sps.defaultSecurity = compatible;
				sps.lst.add(compatible);
				LOGGER.info("Added the security conf -> {}, {} ", compatible.getName(), compatible.getAlgorithm());
			}
			return sps;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);

			ConfSecurity sp = createConfSecurity(item);
			if (sp == null) {
				LOGGER.warn("Skip the security conf -> {} " , UXml.asXml(item));
				continue;
			}

			LOGGER.info("Added the security conf -> {}, {} ", sp.getName(), sp.getAlgorithm());

			sps.getLst().add(sp);

			if (sp.isDefaultConf()) {
				sps.defaultSecurity = sp;
				initSymmetricalDefault(sp);
				LOGGER.info("Set the default Symmetrical Security -> {}, {} ", sps.defaultSecurity.getName(),
						sps.defaultSecurity.getAlgorithm());
			}

		}

		if (sps.defaultSecurity == null && sps.lst.size() > 0) {
			// set the first conf is default
			sps.defaultSecurity = sps.lst.get(0);
			initSymmetricalDefault(sps.defaultSecurity);
			LOGGER.info("Set the default Symmetrical Security -> {}, {} ", sps.defaultSecurity.getName(),
					sps.defaultSecurity.getAlgorithm());
		}

		if (!sps.defaultSecurity.getAlgorithm().equalsIgnoreCase("des")) {
			// for compatibility, set the default UDes
			for (int i = 0; i < sps.lst.size(); i++) {
				ConfSecurity sp = sps.lst.get(i);
				if (sp.getAlgorithm().equalsIgnoreCase("des")) {
					initSymmetricalDefault(sp);
					LOGGER.info("Set the default UDES -> {}, {} ", sp.getName(), sp.getAlgorithm());
					break;
				}
			}
		}
		return sps;
	}

	private static ConfSecurity getCompatibleConf() {
		// <!-- Cookie加密解密DES -->
		// <des desKeyValue="xxxxxxxxxxxxxxxxx" desIvValue="xxxsdskd" />
		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("des");
		if (nl.getLength() == 0) {
			return null;
		}

		Element item = (Element) nl.item(0);
		String key = item.getAttribute("desKeyValue");
		String iv = item.getAttribute("xxxsdskd");

		ConfSecurity conf = new ConfSecurity();
		conf.setAlgorithm("des");
		conf.setDefaultConf(true);
		conf.setKey(key);
		conf.setIv(iv);
		conf.setName("OLD_CONF_DES should be replacement with AES-GCM");
		UDes.initDefaultKey(key, iv);
		return conf;

	}

	private static void initSymmetricalDefault(ConfSecurity sp) {
		if (sp.getAlgorithm().equalsIgnoreCase("des")) {
			UDes.initDefaultKey(sp.getKey(), sp.getIv());
		} else {
			UAes.initDefaultKey(sp.getAlgorithm(), sp.getKey(), sp.getIv(), sp.getMacBitSize(), sp.getAad());
		}
	}

	private static ConfSecurity createConfSecurity(Element item) {
		Map<String, String> attrs = UXml.getElementAttributes(item, true);

		ConfSecurity sp = new ConfSecurity();
		String algorithm = attrs.get("algorithm");
		String key = attrs.get("key");
		if (StringUtils.isBlank(algorithm)) {
			LOGGER.warn("Invalid security algorithm: " + UXml.asXml(item));
			return null;
		}

		if (StringUtils.isBlank(key) || key.equals("change-to-your-key") || key.equals("更改为你的密码")) {
			try {
				key = new String(UAes.generateRandomBytes(32), StandardCharsets.UTF_8);
				LOGGER.info("Generate 32 bytes random key");
			} catch (Exception e) {
				LOGGER.warn("Invalid generate key ");
				return null;
			}
		}

		String iv = attrs.get("iv"); // when iv is blank , using auto iv
		String base64Encoded = attrs.get("base64encoded");
		String macBitSize = attrs.get("macbitsize");
		String defaultConf = attrs.get("default");
		String aad = attrs.get("aad");
		String name = attrs.get("name");

		if (Utils.cvtBool(base64Encoded)) {
			try {
				key = new String(UConvert.FromBase64String(key), "UTF-8");
				if (StringUtils.isNotBlank(iv)) {
					iv = new String(UConvert.FromBase64String(iv), "UTF-8");
				}
				if (StringUtils.isNotBlank(aad)) {
					aad = new String(UConvert.FromBase64String(aad), "UTF-8");
				}
			} catch (Exception e) {
				LOGGER.warn("Invalid base64encoded key/iv/aad ");
				return null;
			}

		}

		sp.setAlgorithm(algorithm);
		sp.setAad(aad);
		sp.setIv(iv);
		sp.setKey(key);
		sp.setDefaultConf(Utils.cvtBool(defaultConf));
		sp.setName(name);

		if (StringUtils.isNotBlank(macBitSize)) {
			try {
				int size = Integer.parseInt(macBitSize);
				if (size < 32 || size > 128 || size % 8 != 0) {
					LOGGER.warn("Invalid macBitSize, must >=32 and <=128 and %8 = 0");
					return null;
				}
				sp.setMacBitSize(size);
			} catch (Exception err) {
				LOGGER.warn("Invalid macBitSize " + macBitSize);
				return null;
			}
		}

		return sp;
	}

	private ConfSecurity defaultSecurity;
	private List<ConfSecurity> lst;

	ConfSecurities() {
		this.lst = new ArrayList<>();
	}

	/**
	 * Get a ConfSecurity through the name
	 * 
	 * @param name the name
	 * @return the conf
	 */
	public ConfSecurity getConf(String name) {
		if (name == null) {
			return null;
		}
		for (int i = 0; i < this.lst.size(); i++) {
			if (name.equals(this.lst.get(i).getName())) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	/**
	 * Get the default ConfSecurity
	 * 
	 * @return the default ConfSecurity
	 */
	public ConfSecurity getDefaultSecurity() {
		return defaultSecurity;
	}

	public void setDefaultSecurity(ConfSecurity defaultSecurity) {
		this.defaultSecurity = defaultSecurity;
	}

	public List<ConfSecurity> getLst() {
		return lst;
	}

	public void setLst(List<ConfSecurity> lst) {
		this.lst = lst;
	}

}
