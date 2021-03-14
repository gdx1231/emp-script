package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class UpdateXmlImpl extends UpdateXmlBase implements IUpdateXml {
	private static Logger LOGGER = LoggerFactory.getLogger(UpdateXmlImpl.class);
	private int _BakFilesCount;

	public UpdateXmlImpl(IConfig configType) {
		super._XmlName = configType.getXmlName();
		super.configType = configType;
		super.scriptPath = configType.getScriptPath();

		_Document = this.getDoc();
	}

	/**
	 * for batch update
	 * 
	 * @param scriptPath
	 */
	public UpdateXmlImpl(ConfScriptPath scriptPath) {
		super.scriptPath = scriptPath;
	}

	/**
	 * 获取文档的xml字符串
	 */
	public String getDocXml() {
		return UXml.asXml(_Document);
	}

	public String deleteFile(String xmlName) {
		String path1 = UPath.getScriptPath() + UserConfig.filterXmlName(xmlName);
		String pathRecycle = UPath.getScriptPath() + ConfigUtils.RECYCLE_NAME;
		File file1 = new File(pathRecycle);
		if (!file1.exists()) {
			file1.mkdirs();
		}

		File file = new File(path1);
		if (file.isFile()) {
			String path2 = pathRecycle + "/" + file.getName() + "." + System.currentTimeMillis() + ".bak";
			File file2 = new File(path2);
			file.renameTo(file2);
		}
		return "";
	}

	public String createNewXml(String xmlName, String path) throws IOException {
		String path1 = this.scriptPath.getPath() + UserConfig.filterXmlName(path);
		String fileName = path1 + "/" + xmlName;

		if (xmlName.endsWith(".xml")) {
			String s1 = ConfigUtils.XML_ROOT;
			UFile.createNewTextFile(fileName, s1);

			// key_out 生成的主键
			// PARAMETERS_OUT,附加的参数，用","分割,
			// MENUGROUP_OUT 菜单组
			// 用于替换页面上的新节点属性
			String out = "key=" + path + "|" + xmlName + "&type=1";
			return out;
		} else {
			File f = new File(fileName);
			f.mkdirs();

			String out = "key=" + path + "|" + xmlName + "&type=0";
			return out;
		}
	}

	/**
	 * 导入xml配置
	 * 
	 * @param path              路径
	 * @param name              xml名称
	 * @param sourceXmlFilePath 要导入的xml文件
	 * @return
	 */
	public JSONObject importXml(String path, String name, String sourceXmlFilePath) {
		JSONObject obj = new JSONObject();

		String xmlname = path + "/" + name;
		xmlname = UserConfig.filterXmlName(xmlname);
		this._XmlName = xmlname;

		String xmlFilePath = this.configType.getScriptPath().getPath() + this._XmlName;
		super.setXmlFilePath(xmlFilePath);

		File f0 = new File(xmlFilePath);
		if (f0.exists()) {
			LOGGER.error(xmlname + "已经存在");

			obj.put("RST", false);
			obj.put("ERR", xmlname + "已经存在");

			return obj;
		}

		File f1 = new File(sourceXmlFilePath);

		if (!f1.exists()) {
			LOGGER.error(sourceXmlFilePath + "找不到");

			obj.put("RST", false);
			obj.put("ERR", sourceXmlFilePath + "找不到");

			return obj;
		}
		Document doc = null;
		try {
			doc = UXml.retDocument(f1.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		} catch (SAXException e) {
			LOGGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		}

		this.writeXml(doc);

		obj.put("RST", true);

		return obj;
	}

	/**
	 * 删除备份文件
	 * 
	 * @param path
	 * @return
	 */
	public int deleteBaks(String xmlname) {
		String path = this.configType.getScriptPath().getPath() + UserConfig.filterXmlName(xmlname);

		if (xmlname.equals("EWA_TREE_ROOT")) { // 根节点
			path = this.configType.getScriptPath().getPath();
		}

		this._BakFilesCount = 0;
		this.deleteBaksdo(path);
		return this._BakFilesCount;
	}

	private void deleteBaksdo(String path) {
		java.io.File f = new File(path);
		int m = 0;
		if (!f.exists()) {
			return;
		}

		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f1 = files[i];
			if (f1.isFile() && f1.getName().toLowerCase().endsWith(".bak")) {
				files[i].delete();
				m++;
			} else if (f1.isDirectory()) {
				deleteBaks(f1.getAbsolutePath());
			}
		}
		_BakFilesCount += m;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#getSqls()
	 */
	@Override
	public String getSqls() {
		MStr sb = new MStr();
		NodeList nl0 = this._Document.getElementsByTagName(_ItemUri);
		for (int k = 0; k < nl0.getLength(); k++) {
			Element eleItem = (Element) nl0.item(k);
			String itemName = eleItem.getAttribute("Name");
			NodeList nl = eleItem.getElementsByTagName("Sql");
			for (int i = 0; i < nl.getLength(); i++) {
				Element ele = (Element) nl.item(i);
				String sql = ele.getTextContent();
				Element eleParent = (Element) ele.getParentNode();
				String name = eleParent.getAttribute("Name");
				sb.al("/* {'ITEMNAME': '" + itemName + "', 'SQL': '" + name + "'} */");
				sb.al(sql);
			}
			sb.al("\t");
		}
		return sb.toString();
	}

	public boolean renamePath(String pathName, String newPathNameWithoutPath) {
		String path = this.configType.getScriptPath().getPath() + UserConfig.filterXmlName(pathName);
		UFile.renameFile(path, newPathNameWithoutPath);
		return true;
	}

	public boolean renameXmlFile(String xmlNameAndPath, String newXmlNameWithoutPath) {
		String path = this.configType.getScriptPath().getPath() + UserConfig.filterXmlName(xmlNameAndPath);
		UFile.renameFile(path, newXmlNameWithoutPath);
		return true;
	}

	public boolean renameItem(String xmlName, String itemName, String newItemName) {
		this._XmlName = xmlName;
		Node node = this.queryItem(itemName);
		if (node == null)
			return false;
		Element e1 = (Element) node;
		e1.setAttribute("Name", newItemName);
		Node nodeName = UXml.retNode(node, "Name/Set");
		e1 = (Element) nodeName;
		e1.setAttribute("Name", newItemName);
		this.writeXml(this._Document);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#updateItem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean updateItem(String itemName, String xml) {
		return updateItem(itemName, xml, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#updateItem(java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	public boolean updateItem(String itemName, String xml, boolean isUpdateTime) {
		Node curNode = this.queryItem(itemName);
		if (curNode != null) {
			queryNodeCreateDate(curNode);
			saveBackup();
			this.removeItem(itemName, false);
		} else {
			this._CreateDate = Utils.getDateTimeString(new Date());
		}

		xml = super.fixXml(xml, itemName);

		Document newDoc = UXml.appendNode(this._Document, xml, this._RootUri);
		if (newDoc == null)
			return false;
		_Document = newDoc;

		curNode = this.queryItem(itemName);
		if (curNode == null) {
			NodeList nl = UXml.retNodeList(this._Document, this._RootUri + "/" + this._ItemUri);
			curNode = nl.item(nl.getLength() - 1);
		}
		if (curNode == null) {
			recoverFile();
		}
		if (isUpdateTime) {
			this.updateTime(curNode);
		}
		Element ele = (Element) curNode;
		if (super.getAdmin() != null) {
			// 设定作者
			ele.setAttribute("Author", super.getAdmin());
		}
		return writeXml(newDoc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#saveXml(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean saveXml(String itemName, String xml) {
		return this.updateItem(itemName, xml);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#writeXml(org.w3c.dom.Document)
	 */
	@Override
	public boolean writeXml(Document doc) {
		String path = super.getXmlFilePath();
		boolean rst = UXml.saveDocument(doc, path);
		LOGGER.info("SAVE(" + rst + "): " + path);
		return rst;
	}

	private Document getDoc() {
		Document doc = null;
		try {
			doc = this.getConfigType().loadConfiguration();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		if (doc == null) {
			doc = UXml.createBlankDocument();
			Element root = doc.createElement(_RootUri);
			root.setAttribute("Author", "");
			doc.appendChild(root);
		}
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#removeItem(java.lang.String)
	 */
	@Override
	public boolean removeItem(String itemName) {
		return removeItem(itemName, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#removeItems(java.lang.String)
	 */
	@Override
	public boolean removeItems(String itemNames) {
		String[] s1 = itemNames.split(",");
		boolean b = true;
		;
		for (int i = 0; i < s1.length; i++) {
			b = b & this.removeItem(s1[i], false);
		}
		if (b) {
			this.saveBackup();
			b = b & this.writeXml(this._Document);
		}
		if (!b) {
			this.recoverFile();
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#removeItem(java.lang.String,
	 * boolean)
	 */
	@Override
	public boolean removeItem(String itemName, boolean isWrite) {
		if (isWrite) {
			saveBackup();
		}
		boolean b = UXml.removeNode(_Document, _RootUri + "/" + _ItemUri, "Name", itemName);
		if (b && isWrite) {
			b = b & this.writeXml(_Document);
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#queryItem(java.lang.String)
	 */
	@Override
	public Node queryItem(String itemName) {
		String tagPath = _RootUri + "/" + _ItemUri;
		String attributeName = "Name";
		if (this._Document == null)
			return null;
		return UXml.queryNode(this._Document, attributeName, itemName, tagPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#queryItemXml(java.lang.String)
	 */
	@Override
	public String queryItemXml(String itemName) {

		Node node = this.queryItem(itemName);
		if (node == null)
			return null;
		return UXml.asXmlAll(node);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#copyItem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean copyItem(String souceItemName, String newItemName) {

		this._CreateDate = Utils.getDateTimeString(new Date());

		Node node = this.queryItem(souceItemName);
		if (node == null)
			return false;

		saveBackup();

		Node newNode = node.cloneNode(true);
		newNode.getAttributes().getNamedItem("Name").setNodeValue(newItemName);
		this.updateTime(newNode);
		node.getParentNode().appendChild(newNode);
		return this.writeXml(_Document);
	}

	public String copyXmlFile(String fromFileName, String toPath, String toFileName) throws IOException {

		String from = UPath.getScriptPath() + fromFileName.replace("|", "/");
		String to = UPath.getScriptPath() + toPath.replace("|", "/") + "/" + toFileName;
		UFile.copyFile(from, to);

		// key_out 生成的主键
		// PARAMETERS_OUT,附加的参数，用","分割,
		// MENUGROUP_OUT 菜单组
		// 用于替换页面上的新节点属性
		String out = "key=" + toPath + "|" + toFileName + "&type=1";
		return out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#setFrameType(java.lang.String)
	 */
	@Override
	public void setFrameType(String frameType) {
		_FrameType = frameType;
		_Document = this.getDoc();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#saveBackup()
	 */
	@Override
	public void saveBackup() {
		long a = System.currentTimeMillis();

		String newName = this.getXmlFilePath() + "." + a + ".bak";
		this._BackUpXmlName = newName;
		File f1 = new File(this.getXmlFilePath());
		File f2 = new File(newName);

		if (f1.exists() && !f1.isDirectory()) {
			if (f2.exists()) {
				f2.delete();
			}
			f1.renameTo(f2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#recoverFile()
	 */
	@Override
	public void recoverFile() {
		File f2 = new File(this.getXmlFilePath());
		File f1 = new File(this._BackUpXmlName);
		if (f1.exists() && !f1.isDirectory()) {
			if (f2.exists()) {
				f2.delete();
			}
			f1.renameTo(f2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gdxsoft.easyweb.define.IUpdateXml#updateDescription(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void updateDescription(String itemName, String des) {
		Node node = this.queryItem(itemName);
		if (node == null) {
			return;
		}
		NodeList nodes = UXml.retNodeList(node, "Page/DescriptionSet/Set");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			if (e.getAttribute("Lang").equalsIgnoreCase("zhcn")) {
				saveBackup();

				e.setAttribute("Info", des);
				queryNodeCreateDate(node);
				this.updateTime(node);
				this.writeXml(this._Document);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.IUpdateXml#batchUpdate(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdate(String itemNames, String paraName, String paraValue) {
		String[] items = itemNames.split(",");
		saveBackup();
		String tagPath = this._RootUri;
		if (this._ItemUri.length() > 0)
			tagPath = tagPath + "/" + this._ItemUri;
		NodeList nl = UXml.retNodeList(this._Document, tagPath);
		for (int a = 0; a < nl.getLength(); a++) {
			Node node = nl.item(a);
			String name = UXml.retNodeValue(node, "Name");
			boolean isok = false;
			for (int i = 0; i < items.length; i++) {
				if (name.equals(items[i])) {
					isok = true;
					break;
				}
			}
			if (!isok) {
				continue;
			}
			Node n = UXml.retNode(node, "Page/" + paraName + "/Set");
			if (n != null) {
				Element e = (Element) n;
				e.setAttribute(paraName, paraValue);
			}
			queryNodeCreateDate(node);
			this.updateTime(node);
		}
		this.writeXml(this._Document);
	}

}
