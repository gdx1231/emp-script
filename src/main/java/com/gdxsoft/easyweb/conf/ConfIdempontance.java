package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.script.idempotance.IOp;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfIdempontance {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfIdempontance.class);
	private static ConfIdempontance INST = null;
	private static long PROP_TIME = 0;

	/**
	 * Return the instance of define
	 * 
	 * @return
	 */
	public static ConfIdempontance getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		initDefine();
		return INST;
	}

	private synchronized static void initDefine() {
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();
		INST = new ConfIdempontance();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("idempontance");
		if (nl.getLength() == 0) {
			INST.name = "session";
			INST.className = "com.gdxsoft.easyweb.script.idempotance.OpSessionImpl";
			LOGGER.info("Idempontance: not defined, Using default OpSessionImpl");
			return;
		}
		Element item = (Element) nl.item(0);
		INST.name = item.getAttribute("name");
		INST.className = item.getAttribute("className");

		LOGGER.info("Idempontance: {}, {}", INST.name, INST.className);

	}

	/**
	 * 获取幂等性操作类
	 * 
	 * @return
	 */
	public IOp getOp() {
		UObjectValue o = new UObjectValue();
		Object item = o.loadClass(className, null);

		if (item == null) {
			LOGGER.warn("Instance op error0: {}", className);
			return new com.gdxsoft.easyweb.script.idempotance.OpSessionImpl();
		}
		try {
			return (IOp) item;
		} catch (Exception e) {
			LOGGER.warn("Instance op error1: {}", e.getClass());

			return new com.gdxsoft.easyweb.script.idempotance.OpSessionImpl();
		}
	}

	private String name; // 名称
	private String className; // 类的名字，例如 com.gdxsoft.easyweb.script.idempotance.OpSessionImpl

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 类的名字，例如 com.gdxsoft.easyweb.script.idempotance.OpSessionImpl
	 * 
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

}
