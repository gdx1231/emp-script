package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfRequestValuesGlobal {
	private static ConfRequestValuesGlobal INST = null;

	private static long PROP_TIME = 0;

	public static ConfRequestValuesGlobal getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createSqlCached();
		return INST;
	}

	private List<ConfNameValuePair> lst = new ArrayList<ConfNameValuePair>();

	private static ConfRequestValuesGlobal createSqlCached() {

		ConfRequestValuesGlobal cvg = new ConfRequestValuesGlobal();
		if (UPath.getCfgXmlDoc() == null) {
			return cvg;
		}

		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("rv");
		for (int i = 0; i < nl.getLength(); i++) {
			ConfNameValuePair p = new ConfNameValuePair();
			UObjectValue.fromXml(nl.item(i), p);
			cvg.lst.add(p);
		}
		return cvg;
	}

	public List<ConfNameValuePair> getLst() {
		return lst;
	}
}
