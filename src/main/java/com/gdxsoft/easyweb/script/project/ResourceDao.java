package com.gdxsoft.easyweb.script.project;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UXml;

public class ResourceDao {

	public void addResource(String filePath, Document prjDoc) {
		Resource r = this.createResource(filePath);
		if (r == null) {
			return;
		}

		Node n = UXml.retNodeListByPath(prjDoc, ProjectDao.PATH_ResourcesNode)
				.item(0);
		Node rNode = UXml.queryNode(prjDoc, "Name", r.getName(),
				ProjectDao.PATH_Resources);
		Element e;
		if (rNode == null) {
			e = prjDoc.createElement("Resource");
			e.setAttribute("Name", r.getName());
		} else {
			e = (Element) rNode;
		}
		e.setAttribute("Encode", r.getEncoder());
		e.setAttribute("Type", r.getType());
		e.setTextContent(r.getInnerValue());
		if (rNode == null) {
			n.appendChild(e);
		}
	}

	private Resource createResource(String filePath) {
		File f1 = new File(filePath);
		if (!f1.exists()) {
			return null;
		}
		String[] exts = f1.getName().split("\\.");
		String ext = "BIN";
		if (exts.length > 1) {
			ext = exts[exts.length - 1].toLowerCase();
		}
		Resource r = new Resource();
		r.setType(ext.toUpperCase());
		r.setName(f1.getName());
		String s1 = "";
		// if (ext.equals("js") || ext.equals("txt") || ext.equals("htm")
		// || ext.equals("html") || ext.equals("log")) {
		// try {
		// s1 = UFile.readFileText(filePath);
		// } catch (Exception e) {
		// System.err.println(e.toString());
		// return null;
		// }
		// r.setEncoder("NONE");
		// } else {
		try {
			s1 = UFile.readFileGzipBase64(filePath);
		} catch (Exception e) {
			System.err.println(e.toString());
			return null;
		}
		r.setEncoder("BASE64");
		// }
		r.setInnerValue(s1);

		return r;
	}
}
