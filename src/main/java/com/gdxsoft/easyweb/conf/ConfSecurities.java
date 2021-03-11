package com.gdxsoft.easyweb.conf;

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
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static ConfSecurities createNewScriptPaths() {
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
			return null;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			Map<String, String> attrs = UXml.getElementAttributes(item, true);

			ConfSecurity sp = new ConfSecurity();
			String algorithm = attrs.get("algorithm");
			String key = attrs.get("key");
			if (StringUtils.isBlank(algorithm) || StringUtils.isBlank(key)) {
				LOGGER.warn("Invalid cfg: " + UXml.asXml(item));
				continue;
			}
			
			String iv = attrs.get("iv"); // when iv is blank , using auto iv
			String base64Encoded = attrs.get("base64encoded");
			String macBitSize = attrs.get("macbitsize");
			String defaultConf = attrs.get("default");
			String aad = attrs.get("aad");
			
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
					continue;
				}

			}

			sp.setAlgorithm(algorithm);
			sp.setAad(aad);
			sp.setIv(iv);
			sp.setKey(key);
			if (StringUtils.isNotBlank(macBitSize)) {
				try {
					int size = Integer.parseInt(macBitSize);
					if (size < 32 || size > 128 || size % 8 != 0) {
						LOGGER.warn("Invalid macBitSize, must >=32 and <=128 and %8 = 0");
						continue;
					}
					sp.setMacBitSize(size);
				} catch (Exception err) {
					LOGGER.warn("Invalid macBitSize " + macBitSize);
					continue;
				}
			}
			sps.getLst().add(sp);

			if (Utils.cvtBool(defaultConf)) {
				sps.defaultSecurity = sp;
			}

			if (sp.getAlgorithm().equalsIgnoreCase("des")) {
				UDes.initDefaultKey(sp.getKey(), sp.getIv());
			} else {
				UAes.initDefaultKey(sp.getAlgorithm(), sp.getKey(), sp.getIv(), sp.getMacBitSize(), sp.getAad());
			}
		}

		if (sps.defaultSecurity == null && sps.lst.size() > 0) {
			// the first conf is default
			sps.defaultSecurity = sps.lst.get(0);
		}

		return sps;
	}

	private ConfSecurity defaultSecurity;
	private List<ConfSecurity> lst;

	public ConfSecurities() {
		this.lst = new ArrayList<>();
	}

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
