package com.gdxsoft.easyweb.script;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ShortNames {
	private static ShortNames SNS = null;
	private static String NAME = "__ewa_short_names.cfg";
	private MTable _Tables;
	private String _CfgPath;

	/**
	 * 获取唯一实例
	 * 
	 * @return
	 */
	public static ShortNames Instance() {
		if (SNS == null) {
			SNS = new ShortNames();
			SNS.init();
			return SNS;
		}
		// 检查文件是否改变
		if (UFileCheck.fileChanged(SNS._CfgPath)) {
			SNS.init();
		}
		return SNS;
	}

	/**
	 * 配置文件及配置项短名称
	 */
	private ShortNames() {

	}

	/**
	 * 获取ShortName
	 * 
	 * @param name
	 * @return
	 */
	public ShortName getShortName(String name) {
		if (name == null || name.trim().length() == 0) {
			return null;
		}
		String n = name.trim().toUpperCase();
		if (this._Tables.containsKey(n)) {
			return (ShortName) this._Tables.get(n);
		} else {
			return null;
		}
	}

	/**
	 * 初始化 <br>
	 * 模板：&lt;s n="T2" x="|global_travel_scm|scm_order.xml" i="ORD.LST.CON" d="测试"
	 * p="T=ORD_ADM_WAIT" h=""&gt;
	 */
	public synchronized void init() {
		this._Tables = new MTable();
		String cfgName = UPath.getScriptPath() + "/" + NAME;
		File f = new File(cfgName);
		if (!f.exists() || f.isDirectory()) {
			return;
		}
		_CfgPath = f.getAbsolutePath();
		UFileCheck.fileChanged(_CfgPath);

		try {
			Document doc = UXml.retDocument(_CfgPath);
			NodeList nl = doc.getElementsByTagName("s");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				initItem(e);
			}
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	private void initItem(Element e) {
		try {
			ShortName s = new ShortName();
			s.setDescription(e.getAttribute("d"));
			s.setItemName(e.getAttribute("i"));
			s.setXmlName(e.getAttribute("x"));
			s.setName(e.getAttribute("n"));
			s.setHiddens(e.getAttribute("h"));
			s.setParameters(e.getAttribute("p"));
			String name = s.getName().toUpperCase();
			if (this._Tables.containsKey(name)) {
				this._Tables.removeKey(name);
			}
			this._Tables.add(name, s);
		} catch (Exception ee) {
			System.out.println(ee.getMessage());
		}
	}

}
