package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfScriptPaths {

	private static ConfScriptPaths INST = null;

	private static long PROP_TIME = 0;

	public static ConfScriptPaths getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static ConfScriptPaths createNewScriptPaths() {
		/*
		 * <scriptPaths> <scriptPath name="/ewa" path="resources:/user.xml/ewa" />
		 * <scriptPath name="/" path="jdbc:ewa" /> </scriptPaths>
		 */
		ConfScriptPaths sps = new ConfScriptPaths();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("scriptPath");
		if (nl.getLength() == 0) {
			return null;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			UObjectValue uv = new UObjectValue();
			ConfScriptPath sp = new ConfScriptPath();
			uv.setObject(sp);
			uv.setAllValue(item);

			if (sp.isResources()) { // force read-only mode
				sp.setReadOnly(true);
			}
			sps.lst.add(sp);
		}

		return sps;
	}

	private List<ConfScriptPath> lst = new ArrayList<>();

	/**
	 * Get the ScriptPath by the name
	 * 
	 * @param name the ScriptPath name
	 * @return ScriptPath
	 */
	public ConfScriptPath getScriptPath(String name) {
		for (int i = 0; i < this.lst.size(); i++) {
			if (this.lst.get(i).getName().equalsIgnoreCase(name)) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	public List<ConfScriptPath> getLst() {
		return lst;
	}
}
