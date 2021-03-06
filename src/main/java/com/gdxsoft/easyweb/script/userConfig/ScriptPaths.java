package com.gdxsoft.easyweb.script.userConfig;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

public class ScriptPaths {

	private static ScriptPaths INST = null;

	private static long PROP_TIME = 0;

	public static ScriptPaths getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static ScriptPaths createNewScriptPaths() {
		/*
		 * <scriptPaths> <scriptPath name="/ewa" path="resources:/user.xml/ewa" />
		 * <scriptPath name="/" path="jdbc:ewa" /> </scriptPaths>
		 */
		ScriptPaths sps = new ScriptPaths();

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
			ScriptPath sp = new ScriptPath();
			String name = item.getAttribute("name");
			String path = item.getAttribute("path");
			sp.setName(name);
			sp.setPath(path);

			sps.lst.add(sp);
		}

		return sps;
	}

	private List<ScriptPath> lst = new ArrayList<>();

	/**
	 * Get the ScriptPath by the name
	 * 
	 * @param name the ScriptPath name
	 * @return ScriptPath
	 */
	public ScriptPath getScriptPath(String name) {
		for (int i = 0; i < this.lst.size(); i++) {
			if (this.lst.get(i).getName().equalsIgnoreCase(name)) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	public List<ScriptPath> getLst() {
		return lst;
	}
}
