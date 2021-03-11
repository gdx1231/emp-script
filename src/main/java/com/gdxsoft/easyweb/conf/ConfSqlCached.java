package com.gdxsoft.easyweb.conf;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class ConfSqlCached {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfSqlCached.class);
	private static ConfSqlCached INST = null;

	private static long PROP_TIME = 0;

	public static ConfSqlCached getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createSqlCached();
		return INST;
	}

	private synchronized static ConfSqlCached createSqlCached() {
		// <sqlCached cachedMethod="redis" redisName="r0" ></sqlCached>
		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		ConfSqlCached o = new ConfSqlCached();
		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("sqlCached");

		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			o.redisName = ele.getAttribute("redisName");
			o.cachedMethod = ele.getAttribute("cachedMethod");
			o.debug = Utils.cvtBool(ele.getAttribute("debug"));
		}
		if (StringUtils.isBlank(o.cachedMethod)) {
			o.cachedMethod = "hsqldb"; // default
		}

		if ("redis".equalsIgnoreCase(o.cachedMethod)) {
			o.confRedis = ConfRedises.getInstance().getScriptPath(o.redisName);

			if (o.confRedis == null) {
				LOGGER.warn("No redis configuration found by then redis name -> " + o.redisName);

				o.cachedMethod = "hsqldb"; // default
				LOGGER.warn("SqlCached change to default HSQLDB");
			}
		}

		return o;
	}

	private String cachedMethod;
	private String redisName;
	private boolean debug;

	private ConfRedis confRedis;

	public String getCachedMethod() {
		return cachedMethod;
	}

	public void setCachedMethod(String cachedMethod) {
		this.cachedMethod = cachedMethod;
	}

	public String getRedisName() {
		return redisName;
	}

	public void setRedisName(String redisName) {
		this.redisName = redisName;
	}

	public ConfRedis getConfRedis() {
		return confRedis;
	}

	public void setConfRedis(ConfRedis confRedis) {
		this.confRedis = confRedis;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
