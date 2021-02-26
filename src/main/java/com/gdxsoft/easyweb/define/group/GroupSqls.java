package com.gdxsoft.easyweb.define.group;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.utils.UXml;

public class GroupSqls {
	private String[] _Paras;
	private Document _Doc;

	public void init(String paras) {
		this._Paras = paras.split("\\|");
	}

	public String getXml() {
		if (this._Doc == null) {
			this._Doc = UXml.createBlankDocument();
			Element e = this._Doc.createElement("GROUP");
			this._Doc.adoptNode(e);
		}
		
		Element eleRoot = this._Doc.createElement("DATA");
		this._Doc.getFirstChild().appendChild(eleRoot);

		for (int i = 0; i < _Paras.length; i++) {
			this.initTable(eleRoot, this._Paras[i]);
		}

		return UXml.asXmlAll(this._Doc);
	}

	private void initTable(Element tableBody, String s1) {
		String[] s2 = s1.split(";");
		s2[0] = s2[0].trim().toUpperCase();

		if (s2[0].equals("TABLE")) {
			IGroupTable t = null;
			if (s2[3].equalsIgnoreCase("MSSQL")) {
				t = new GroupMsSqlTable();
			} else if (s2[3].equalsIgnoreCase("MYSQL")) {
				t = new GroupMySqlTable();
			}
			t.initTable(s2[1], s2[2], s2[4]);
			Element e = tableBody.getOwnerDocument().createElement("TABLE");
			e.setAttribute("Name", s2[2]);
			tableBody.appendChild(e);

			this.setCDATA(e, "BODY", t.createTableBody());
			this.setCDATA(e, "PK", t.createTablePk());
			this.setCDATA(e, "REMARKS", t.createTableRemarks());

		} else {
			IGroupView v = null;
			if (s2[3].equalsIgnoreCase("MSSQL")) {
				v = new GroupMsSqlView();
			} else if (s2[3].equalsIgnoreCase("MYSQL")) {
				v = new GroupMySqlView();
			}
			v.initView(s2[2], s2[4]);
			Element e = tableBody.getOwnerDocument().createElement("VIEW");
			e.setAttribute("Name", s2[2]);
			tableBody.appendChild(e);

			this.setCDATA(e, "BODY", v.createView());

		}
	}

	private void setCDATA(Element nodeParent, String nodeName, String text) {
		CDATASection cdata = nodeParent.getOwnerDocument().createCDATASection(
				text.replace("\r", ""));
		Element e = nodeParent.getOwnerDocument().createElement(nodeName);
		e.appendChild(cdata);
		nodeParent.appendChild(e);
	}

	public Document getDoc() {
		return _Doc;
	}

	public void setDoc(Document doc) {
		_Doc = doc;
	}
}
