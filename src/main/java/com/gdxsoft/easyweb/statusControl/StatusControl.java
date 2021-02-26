package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class StatusControl {

	private String _Name;
	private String _Des;

	private MTable _Sts;
	private MTable _Fms;
	private Fm _CurFm;

	public StatusControl() {
		_Sts = new MTable();
		_Fms = new MTable();
	}

	public String createFmLogicExp(String fmName) {
		String name = fmName.trim().toUpperCase();
		if (!_Fms.containsKey(name)) {
			return "not found " + fmName;
		}
		Fm fm = (Fm) _Fms.get(name);
		String logicExp = this.createFmLogicExp(fm);
		this._CurFm = fm;
		return logicExp;
	}

	/**
	 * 生成表单的逻辑表达式
	 * 
	 * @param fm
	 */
	private String createFmLogicExp(Fm fm) {
		MTable lgs = fm.getLgs();
		MStr s = new MStr();
		for (int i = 0; i < lgs.getCount(); i++) {
			Lg lg = (Lg) lgs.getByIndex(i);
			if (lg.getStName().startsWith("#")) {
				s.al(lg.getStName().replace("#", " "));
			} else {
				s.a("(");
				St st = (St) this._Sts.get(lg.getStName());
				lg.setSt(st);
				s.al(lg.createLogicExp() + ")");
			}
		}
		return s.toString();
	}

	public void load(String xmlName, String itemName) throws Exception {
		String path = UPath.getScriptPath()
				+ xmlName.replace("|", "/").replace("\\", "/");
		while (path.indexOf("./") >= 0) {
			path = path.replace("./", "/");
		}

		Document doc = UXml.retDocument(path);
		NodeList nl = doc.getElementsByTagName("sc");
		Node node = null;
		for (int i = 0; i < nl.getLength(); i++) {
			node = nl.item(i);
			if (node.getAttributes().getNamedItem("name").equals(itemName)) {
				break;
			}
		}
		if (node == null) {
			throw new Exception(itemName + "未发现在（" + xmlName + "）中");
		}
		this._Name = itemName;
		this._Des = UXml.retNodeValue(node, "des");

		Element ele = (Element) node;

		nl = ele.getElementsByTagName("st");
		for (int i = 0; i < nl.getLength(); i++) {
			St st = St.parseToSt(nl.item(i));
			this._Sts.add(st.getName(), st);
		}

		nl = ele.getElementsByTagName("fm");
		for (int i = 0; i < nl.getLength(); i++) {
			Fm fm = Fm.parseToFm(nl.item(i));
			this._Fms.add(fm.getName(), fm);
		}
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @return the _Des
	 */
	public String getDes() {
		return _Des;
	}

	/**
	 * @return the _Sts
	 */
	public MTable getSts() {
		return _Sts;
	}

	/**
	 * @return the _Fms
	 */
	public MTable getFms() {
		return _Fms;
	}

	/**
	 * @return the _CurFm
	 */
	public Fm getCurFm() {
		return _CurFm;
	}

}
