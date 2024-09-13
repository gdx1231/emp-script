package com.gdxsoft.easyweb.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class MsWord implements IWord {
	/**
	 * 生成文件
	 * 
	 * @param tmplate    模板
	 * @param exportName 输出文件名
	 * @param rv         参数表
	 * @return
	 * @throws Exception
	 */
	public String doWork(String tmplate, String exportName, RequestValue rv) throws Exception {
		this.templateName = tmplate;
		this.rv = rv;
		String root = unzipTemplate();
		rootPath = root;

		File f1 = new File(exportName);
		String name = f1.getName();
		File froot = new File(root);
		String rootParent = froot.getParent();
		this.exportName = rootParent + "/" + name;

		String xml = rv.getString("xml");
		
		// 删除 table 的空表，即没有w:p的和空内容的，避免word打不开文档
		Document doc = UXml.asDocument(xml);
		NodeList nl = doc.getElementsByTagName("w:tbl");
		List<Element> notTrs = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			if (item.getElementsByTagName("w:p").getLength() == 0) {
				notTrs.add(item);
				continue;
			}
			String text = item.getTextContent();
			if(text.length() == 0) {
				notTrs.add(item);
				continue;
			}
			
			NodeList nlTr = doc.getElementsByTagName("w:tr");
			for(int m=0;m<nlTr.getLength();m++) {
				Element tr = (Element) nlTr.item(m);
				if (tr.getElementsByTagName("w:p").getLength() == 0) {
					notTrs.add(tr);
					continue;
				}
				String text1 = tr.getTextContent();
				if(text1.length() == 0) {
					notTrs.add(tr);
				}
			}
		}

		for (int i = 0; i < notTrs.size(); i++) {
			Element item = notTrs.get(i);
			try {
				item.getParentNode().removeChild(item);
			} catch (Exception err) {

			}
		}

		xml = UXml.asXml(doc);

		String pics = rv.getString("pics");
		createMedia(pics);

		String docmentName = this.rootPath + "/word/document.xml";
		String cnt = UFile.readFileText(docmentName);

		int locTemplateBodyStart = cnt.indexOf("<w:body>") + "<w:body>".length();
		int locTemplateBodyEnd = cnt.indexOf("</w:body>");
		// 模板文档描述部分 <w:document xmlns ...
		String docTemplateMeta = cnt.substring(0, locTemplateBodyStart);
		// 模板文档结束部分 </w:document>
		String docTemplateEnd = cnt.substring(locTemplateBodyEnd);

		int loc0 = cnt.indexOf("<w:sectPr");
		int loc1 = cnt.indexOf("</w:sectPr>") + "</w:sectPr>".length();
		// 模板文档w:sectPr部分
		String wsetcPr = cnt.substring(loc0, loc1);

		int locBodyStart = xml.indexOf("<w:body>") + "<w:body>".length();
		int locaBodyEnd = xml.indexOf("</w:body>");
		// 提交的文档正文部分，不包含w:body
		String documentContent = xml.substring(locBodyStart, locaBodyEnd);

		StringBuilder sb = new StringBuilder();
		sb.append(docTemplateMeta);
		sb.append(documentContent);
		sb.append(wsetcPr);
		sb.append(docTemplateEnd);

		UFile.createNewTextFile(docmentName, sb.toString());

		modifyNumers(rv.getInt("ols"));
		// UFile.zipPaths(root, this.exportName);

		// 将目录打包成文件
		if (DocUtils.compress(this.exportName, root)) {
			DocUtils.clearTempPath(root);
		}
		return "";
	}

	public String downloadUrl() {
		File f1 = new File(this.exportName);
		String c1 = UPath.getPATH_IMG_CACHE();
		File fc = new File(c1);
		return f1.getAbsolutePath().replace(fc.getAbsolutePath(), UPath.getPATH_IMG_CACHE_URL());
	}

	private void createMedia(String pics) throws Exception {
		if (pics == null || pics.trim().length() == 0) {
			return;
		}
		String[] ps = pics.split(",");
		UNet net = new UNet();
		String ck = rv.getRequest().getHeader("Cookie");
		if (ck != null && ck.trim().length() > 0) {
			net.setCookie(ck);
		}
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < ps.length; i++) {
			String u = ps[i];
			int loc = u.lastIndexOf("/");
			String name = u.substring(loc + 1);
			name = name.split("\\?")[0];
			int loc1 = name.lastIndexOf(".");
			String ext = ".jpg";
			if (loc1 >= 0) {
				ext = name.substring(loc1);
				if (ext.equals(".")) {
					ext = ".jpg";
				}
			}
			ext = ext.toLowerCase();
			if (!map.containsKey(ext)) {
				map.put(ext, 1);
			}
			u = u.replace("$", "%24"); //$resized/1000x1000
			byte[] buf = net.downloadData(u);
			if (buf == null) {
				buf = net.getLastErr().getBytes("utf-8");
			}
			String fixed = "00000" + i;
			fixed = fixed.substring(fixed.length() - 5);
			String name1 = "ewa_v2_doc" + fixed + ext;
			String path = this.rootPath + "/word/media/" + name1;
			UFile.createBinaryFile(path, buf, true);
		}
		modifyRels();
		modifyTypes(map);

	}

	private void modifyNumers(int olNum) throws ParserConfigurationException, SAXException, IOException {
		String name = this.rootPath + "/word/numbering.xml";
		Document doc = UXml.retDocument(name);
		NodeList nl = doc.getElementsByTagName("w:abstractNum");
		Node nodeBullet = null;
		Node nodeDecimal = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			Element ele = (Element) node;
			NodeList vals = ele.getElementsByTagName("w:numFmt");
			if (vals.getLength() > 0) {
				// w:val="decimal"
				Element ele1 = (Element) vals.item(0);
				String val = ele1.getAttribute("w:val");
				val = val == null ? "" : val;
				if (val.equals("decimal")) {
					nodeDecimal = node.cloneNode(true);
				} else if (val.equals("bullet")) {
					nodeBullet = node.cloneNode(true);
				}

				if (nodeBullet != null && nodeDecimal != null) {
					break;
				}
			}
		}
		// <w:num w:numId="1">
		// <w:abstractNumId w:val="5"/>
		// </w:num>
		NodeList wnums = doc.getElementsByTagName("w:num");
		Node num = wnums.item(0).cloneNode(true);
		doc.getFirstChild().setTextContent("");

		for (int i = 0; i < olNum; i++) {
			Node n1 = nodeDecimal.cloneNode(true);
			modifyNumId(n1, "w:nsid", i);
			modifyNumId(n1, "w:tmpl", i);

			doc.getFirstChild().appendChild(n1);

		}
		modifyNumId(nodeBullet, "w:nsid", 21);
		modifyNumId(nodeBullet, "w:tmpl", 21);
		doc.getFirstChild().appendChild(nodeBullet);

		for (int i = 0; i < olNum; i++) {
			Node n2 = num.cloneNode(true);
			modifyNumRef(n2, i);

			doc.getFirstChild().appendChild(n2);
		}

		Node n2 = num.cloneNode(true);
		modifyNumRef(n2, 21);
		doc.getFirstChild().appendChild(n2);

		UXml.saveDocument(doc, name);
	}

	private void modifyNumRef(Node n2, int i) {
		Element e2 = (Element) n2;
		e2.setAttribute("w:numId", i + 1 + "");
		Element e21 = (Element) e2.getElementsByTagName("w:abstractNumId").item(0);
		e21.setAttribute("w:val", i + "");
	}

	private void modifyNumId(Node node, String tag, int index) {
		// <w:nsid w:val="7FE95206"/>
		// <w:tmpl w:val="CBBEC51E"/>
		Element e1 = (Element) node;
		e1.setAttribute("w:abstractNumId", index + "");
		Element nsid = (Element) e1.getElementsByTagName(tag).item(0);
		String val = nsid.getAttribute("w:val");
		String idx = index > 10 ? index + "" : "0" + index;
		String newID = val.substring(0, 6) + idx;
		nsid.setAttribute("w:val", newID);
	}

	private void modifyTypes(HashMap<String, Integer> map)
			throws ParserConfigurationException, SAXException, IOException {
		String name = this.rootPath + "/[Content_Types].xml";
		Document doc = UXml.retDocument(name);

		NodeList nl = doc.getElementsByTagName("Default");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String Extension = ele.getAttribute("Extension");
			String s1 = "." + Extension;
			if (map.containsKey(s1)) {
				map.remove(s1);
			}
		}
		// <Default Extension="gif" ContentType="image/gif"/>
		for (String key : map.keySet()) {
			String ext = key.replace(".", "");
			Node n = doc.createElement("Default");
			Element ele = (Element) n;
			ele.setAttribute("Extension", ext);
			ele.setAttribute("ContentType", "image/" + ext);
			doc.getFirstChild().appendChild(ele);
		}

		UXml.saveDocument(doc, name);
	}

	private void modifyRels() throws ParserConfigurationException, SAXException, IOException {
		// <Relationship Id="rId9"
		// Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
		// Target="media/image2.png"/>
		String name = this.rootPath + "/word/_rels/document.xml.rels";
		Document doc = UXml.retDocument(name);
		NodeList nl = doc.getElementsByTagName("Relationship");
		File media = new File(this.rootPath + "/word/media");
		File[] fs = media.listFiles();
		for (int i = 0; i < fs.length; i++) {
			Node n = doc.createElement("Relationship");
			Element ele = (Element) n;
			ele.setAttribute("Id", "pic" + i);
			ele.setAttribute("Type", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
			ele.setAttribute("Target", "media/" + fs[i].getName());
			nl.item(0).getParentNode().appendChild(ele);
		}
		UXml.saveDocument(doc, name);
	}

	private String unzipTemplate() throws IOException {
		String tmpName = DocUtils.builderTempPath(this.templateName);
		List<String> lst = UFile.unZipFile(tmpName);
		String root = "";
		int length = Integer.MAX_VALUE;
		for (int i = 0; i < lst.size(); i++) {
			File f1 = new File(lst.get(i));
			// 查找最短的路径
			if (f1.getParent().length() < length) {
				root = f1.getParent();
				length = f1.getParent().length();
			}
		}
		return root;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getExportName() {
		return exportName;
	}

	public void setExportName(String exportName) {
		this.exportName = exportName;
	}

	private String templateName;
	private String exportName;
	private RequestValue rv;
	private String rootPath;
}
