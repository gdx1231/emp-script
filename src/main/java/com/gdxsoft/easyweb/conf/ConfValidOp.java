package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.script.validOp.IOp;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfValidOp {
	public static final String DEF_CLASS_NAME = "com.gdxsoft.easyweb.script.validOp,OpSessionImpl";
	private static Logger LOGGER = LoggerFactory.getLogger(ConfValidOp.class);
	private static ConfValidOp INST = null;
	private static long PROP_TIME = 0;

	public static IOp getDefaultOp() {
		return new com.gdxsoft.easyweb.script.validOp.OpSessionImpl();
	}

	/**
	 * Return the instance of define
	 * 
	 * @return
	 */
	public static ConfValidOp getInstance() {
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
		INST = new ConfValidOp();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("vaildOp");
		if (nl.getLength() == 0) {
			INST.name = "session";
			INST.className = DEF_CLASS_NAME;
			LOGGER.info("ConfValidOp: not defined, Using default vaildOp");
			return;
		}
		Element item = (Element) nl.item(0);
		INST.name = item.getAttribute("name");
		INST.className = item.getAttribute("className");

		LOGGER.info("ConfValidOp: {}, {}", INST.name, INST.className);

	}

	/**
	 * 获取幂等性操作类
	 * 
	 * @return
	 */
	public IOp getOp() {
		// 默认的就直接返回
		if (DEF_CLASS_NAME.equals(className)) {
			return getDefaultOp();
		}

		UObjectValue o = new UObjectValue();
		Object item = o.loadClass(className, null);

		if (item == null) {
			LOGGER.warn("Instance op error0: {}", className);
			return getDefaultOp();
		}
		try {
			return (IOp) item;
		} catch (Exception e) {
			LOGGER.warn("Instance op error1: {}", e.getClass());

			return getDefaultOp();
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
