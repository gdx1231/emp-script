package com.gdxsoft.easyweb.document;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class OoWord implements IWord{
	/**
	 * 生成文件
	 * 
	 * @param tmplate
	 *            模板
	 * @param exportName
	 *            输出文件名
	 * @param rv
	 *            参数表
	 * @return
	 * @throws Exception
	 */
	public String doWork(String tmplate, String exportName, RequestValue rv)
			throws Exception {
		this.templateName = tmplate;
		File f1 = new File(exportName);
		String name = f1.getName();

		this.rv = rv;
		String root = unzipTemplate();
		rootPath = root;
		File froot=new File(root);
		String rootParent=froot.getParent();
		this.exportName = rootParent + "/" + name;

		String xml = rv.getString("xml");
		String pics = rv.getString("pics");
		String s1 = createMedia(pics, xml);

		String docmentName = this.rootPath + "/content.xml";

		UFile.createNewTextFile(docmentName, s1);

		if(DocUtils.compress(this.exportName, root)){
			DocUtils.clearTempPath(root);
		}
		return "";
	}

	public String downloadUrl() {
		File f1 = new File(this.exportName);
		String c1=UPath.getPATH_IMG_CACHE();
		File fc=new File(c1);
		return f1.getAbsolutePath().replace(fc.getAbsolutePath(),
				UPath.getPATH_IMG_CACHE_URL());
	 
	}

	private String createMedia(String pics, String xml) throws Exception {
		if (pics == null || pics.trim().length() == 0) {
			return xml;
		}
		String[] ps = pics.split(",");
		UNet net = new UNet();
		String ck = rv.getRequest().getHeader("Cookie");
		if (ck != null && ck.trim().length() > 0) {
			net.setCookie(ck);
		}
		HashMap<String, String> map = new HashMap<String, String>();
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
			if (!(ext.equals(".jpg") || ext.equals(".png")
					|| ext.equals(".bmp") || ext.equals(".jpeg"))) {
				ext = ".jpg";
			}
			byte[] buf = net.downloadData(u);
			if(buf==null){
				buf=net.getLastErr().getBytes("utf-8");
			}
			String fixed = "00000" + i;
			fixed = fixed.substring(fixed.length() - 5);
			String name1 = "ewa_v2_doc" + fixed + ext;
			String path = this.rootPath + "/Pictures/" + name1;
			UFile.createBinaryFile(path, buf, true);

			String ref = "Pictures/" + name1;
			map.put(ref, ext.replace(".", ""));
			xml = xml.replace("{[PIC" + i + "]}", ref);
		}
		modifyTypes(map);
		return xml;
	}

	/**
	 * 添加文件引用
	 * 
	 * @param map
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void modifyTypes(HashMap<String, String> map)
			throws ParserConfigurationException, SAXException, IOException {
		// <manifest:file-entry manifest:media-type="image/png"
		// manifest:full-path="Pictures/10000201000000A0000000A0214A9447.png"/>
		String name = this.rootPath + "/META-INF/manifest.xml";
		Document doc = UXml.retDocument(name);

		for (String key : map.keySet()) {
			Node n = doc.createElement("manifest:file-entry");
			String t = map.get(key);
			Element ele = (Element) n;
			ele.setAttribute("manifest:media-type", "image/" + t);
			ele.setAttribute("manifest:full-path", key);
			doc.getFirstChild().appendChild(ele);
		}

		UXml.saveDocument(doc, name);
	}

	private String unzipTemplate() throws IOException {
		String tmpName = DocUtils.builderTempPath(this.templateName);
		List<String> lst = UFile.unZipFile(tmpName);
		String root = "";

		for (int i = 0; i < lst.size(); i++) {
			if (i == 0) {
				File f1 = new File(lst.get(0));
				root = f1.getParent();
				break;
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
