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

import com.gdxsoft.easyweb.conf.ScriptPath;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 
 * @author admin
 *
 */
public class UpdateXmlJdbcImpl extends UpdateXmlBase implements IUpdateXml {
	private static Logger LOGER = LoggerFactory.getLogger(UpdateXmlJdbcImpl.class);
	private Integer _Hash;
	private String _ItemName;
	private boolean _IsBatch;
	private String _Md5;
	private JdbcConfigOperation op;

	public UpdateXmlJdbcImpl(IConfig configType) {
		super._XmlName = configType.getXmlName();
		super.configType = configType;
		super.scriptPath = configType.getScriptPath();
		op = new JdbcConfigOperation(super.scriptPath);
	}

	public UpdateXmlJdbcImpl(ScriptPath scriptPath) {
		super.scriptPath = scriptPath;
		op = new JdbcConfigOperation(scriptPath);
	}

	public String deleteFile(String xmlName) {
		op.removeItem(xmlName, "");
		return "";
	}

	public String createNewXml(String xmlName, String path) {
		String path1 = UserConfig.filterXmlName(path);
		String fileName = path1 + "/" + xmlName;
		if (xmlName.endsWith(".xml")) {
			op.createXml(fileName, "");
			// key_out 生成的主键
			// PARAMETERS_OUT,附加的参数，用","分割,
			// MENUGROUP_OUT 菜单组
			// 用于替换页面上的新节点属性
			String out = "key=" + path + "|" + xmlName + "&type=1";
			return out;
		} else {
			op.createPath(fileName);
			String out = "key=" + path + "|" + xmlName + "&type=0";
			return out;
		}
	}

	public String copyXmlFile(String fromFileName, String toPath, String toFileName) {

		String from = UserConfig.filterXmlName(fromFileName);
		String to = UserConfig.filterXmlName(toPath) + "/" + toFileName;
		op.copyXml(from, to, "");

		// key_out 生成的主键
		// PARAMETERS_OUT,附加的参数，用","分割,
		// MENUGROUP_OUT 菜单组
		// 用于替换页面上的新节点属性
		String out = "key=" + toPath + "|" + toFileName + "&type=1";
		return out;
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
		String xmlname = path + "|" + name;
		xmlname = UserConfig.filterXmlNameByJdbc(xmlname);

		DTTable tb = op.getXmlMeta(xmlname);
		if (tb.getCount() > 0) {
			LOGER.error(xmlname + "已经存在");

			obj.put("RST", false);
			obj.put("ERR", xmlname + "已经存在");

			return obj;
		}

		File file = new File(sourceXmlFilePath);
		if (!file.exists()) {
			LOGER.error(sourceXmlFilePath + "找不到");

			obj.put("RST", false);
			obj.put("ERR", sourceXmlFilePath + "找不到");

			return obj;
		}

		// 检查xml文件是否正确
		try {
			UXml.retDocument(file.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			LOGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		} catch (SAXException e) {
			LOGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		} catch (IOException e) {
			LOGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		}

		try {
			op.importXml(file, xmlname);
			obj.put("RST", true);

			return obj;
		} catch (Exception e) {
			LOGER.error(e.getLocalizedMessage());
			obj.put("RST", false);
			obj.put("ERR", e.getMessage());

			return obj;
		}

	}

	/**
	 * 获取文档的xml字符串
	 */
	public String getDocXml() {
		return op.getXml(this._XmlName);
	}

	@Override
	public int deleteBaks(String xmlname) {
		if (xmlname.equals("EWA_TREE_ROOT")) { // 根节点
			xmlname = "";
		}
		return op.deleteBaks(xmlname);
	}

	@Override
	public String getSqls() {
		return null;
	}

	public boolean renamePath(String pathName, String newPathNameWithoutPath) {
		op.renameTree(pathName, newPathNameWithoutPath);
		return true;
	}

	public boolean renameXmlFile(String xmlNameAndPath, String newXmlNameWithoutPath) {
		op.renameTree(xmlNameAndPath, newXmlNameWithoutPath);
		return true;
	}

	@Override
	public boolean renameItem(String xmlName, String itemName, String newItemName) {
		this._XmlName = xmlName;

		Node node1 = this.queryItem(newItemName);
		if (node1 != null) {
			// 名称重复
			return false;
		}
		Node node = this.queryItem(itemName);
		if (node == null)
			return false;
		Element e1 = (Element) node;
		e1.setAttribute("Name", newItemName);
		Node nodeName = UXml.retNode(node, "Name/Set");
		e1 = (Element) nodeName;
		e1.setAttribute("Name", newItemName);

		String xml = UXml.asXml(node);
		op.renameItem(_XmlName, newItemName, itemName, xml);

		this.writeXml(null);

		return true;
	}

	@Override
	public boolean updateItem(String itemName, String xml) {
		this._ItemName = itemName;

		return this.updateItem(itemName, xml, true);
	}

	@Override
	public boolean updateItem(String itemName, String xml, boolean isUpdateTime) {
		this._ItemName = itemName;
		xml = super.fixXml(xml, itemName);

		Node node = UXml.asNode(xml);
		Element ele = (Element) node;

		// 基本的xml ,不包含其他信息Author, CreateDate, UpdateDate 信息
		int hashCode = xml.hashCode();
		String md5 = Utils.md5(xml);

		Node curNode = this.queryItem(itemName);
		if (curNode != null) {
			if (hashCode != this._Hash || !md5.equals(this._Md5)) {
				saveBackup();
			} else { // 没有变化
				LOGER.info("NO CHANGE: " + this._XmlName + ", " + itemName);
				return true;
			}
		} else {
			this._CreateDate = Utils.getDateTimeString(new Date());
		}

		if (isUpdateTime) {
			this.updateTime(node);
		}
		ele.setAttribute("Author", super.getAdmin());

		xml = UXml.asXml(node);

		op.updateItem(_XmlName, itemName, xml, super.getAdmin(), hashCode, md5);

		if (!this._IsBatch) {
			this.writeXml(null);
		}
		return true;
	}

	@Override
	public boolean saveXml(String itemName, String xml) {
		return this.updateItem(itemName, xml);
	}

	@Override
	public boolean writeXml(Document doc) {

		op.combine2OneXml(this._XmlName);
		return true;
	}

	@Override
	public boolean removeItem(String itemName) {
		return removeItem(itemName, true);
	}

	@Override
	public boolean removeItem(String itemName, boolean isWrite) {
		op.removeItem(_XmlName, itemName);
		if (isWrite) {
			this.writeXml(null);
		}
		return true;
	}

	@Override
	public boolean removeItems(String itemNames) {
		String[] s1 = itemNames.split(",");
		boolean b = true;
		for (int i = 0; i < s1.length; i++) {
			b = b & this.removeItem(s1[i], false);
		}

		this.writeXml(null);

		return b;
	}

	/**
	 * 获取配置项
	 */
	public Node queryItem(String itemName) {
		String xml = this.queryItemXml(itemName);
		if (xml == null) {
			return null;
		} else {
			Node curNode = UXml.asNode(xml);
			queryNodeCreateDate(curNode);
			return curNode;
		}
	}

	@Override
	/**
	 * 获取配置项的 xml
	 */
	public String queryItemXml(String itemName) {
		DTTable tb = op.getJdbcItem(this._XmlName, itemName);
		if (tb.getCount() == 0) {
			return null;
		}
		String xml;
		try {
			xml = tb.getCell(0, "XMLDATA").toString();
			this._Hash = tb.getCell(0, "HASH_CODE").toInt();
			this._Md5 = tb.getCell(0, "MD5").toString();
			return xml;
		} catch (Exception e) {
			LOGER.error(e.getLocalizedMessage());
			return null;
		}

	}

	@Override
	public boolean copyItem(String souceItemName, String newItemName) {
		this._CreateDate = Utils.getDateTimeString(new Date());

		Node node = this.queryItem(souceItemName);
		if (node == null)
			return false;

		Node newNode = node.cloneNode(true);
		newNode.getAttributes().getNamedItem("Name").setNodeValue(newItemName);
		this.updateTime(newNode);

		String xml = UXml.asXml(newNode);
		return this.updateItem(newItemName, xml, true);
	}

	@Override
	public void setFrameType(String frameType) {
		_FrameType = frameType;
	}

	@Override
	public void saveBackup() {
		op.saveBackup(_XmlName, this._ItemName);
	}

	@Override
	public void recoverFile() {

	}

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
				e.setAttribute("Info", des);
				queryNodeCreateDate(node);
				this.updateTime(node);
				String xml = UXml.asXml(node);
				this.updateItem(itemName, xml);
				break;
			}
		}
	}

	/**
	 * 循环更新目录下所有xml配置的item的参数
	 * 
	 * @param paraName  参数名称
	 * @param paraValue 参数值
	 */
	public void batchUpdateByDir(String paraName, String paraValue) {
		String path = UserConfig.filterXmlNameByJdbc(this._XmlName);

		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("pathlike", path + "|%");
		String sql1 = "select XMLNAME from ewa_cfg ec where XMLNAME like @pathlike and ITEMNAME ='' order by XMLNAME";
		DTTable tbXml = op.getJdbcTable(sql1, rv);
		for (int i = 0; i < tbXml.getCount(); i++) {
			String xmlName = tbXml.getCell(i, 0).toString();
			rv.addOrUpdateValue("@xmlname", xmlName);
			String sql2 = "select ITEMNAME from ewa_cfg ec where XMLNAME = @xmlname ";
			DTTable tbItems = op.getJdbcTable(sql2, rv);
			String itemNames = tbItems.joinIds("ITEMNAME", false);

			UpdateXmlJdbcImpl ux = new UpdateXmlJdbcImpl(this.scriptPath);
			ux.setXmlName(xmlName);
			ux.batchUpdate(itemNames, paraName, paraValue);

		}

	}

	@Override
	public void batchUpdate(String itemNames, String paraName, String paraValue) {
		if (!("DataSource".equals(paraName) || "Acl".equals(paraName) || "Log".equals(paraName)
				|| "SkinName".equals(paraName))) {
			LOGER.error("invalid paraName: " + paraName);
			return;
		}

		if ("__batchChangeParam__".equals(itemNames)) {
			// 批处理目录下的所有配置项
			batchUpdateByDir(paraName, paraValue);
			return;
		}
		String[] names = itemNames.split(",");
		this._IsBatch = true;

		for (int i = 0; i < names.length; i++) {
			String itemName = names[i].trim();
			Node node = this.queryItem(itemName);
			if (node == null) {
				continue;
			}

			Node n = UXml.retNode(node, "Page/" + paraName + "/Set");
			if (n != null) {
				Element e = (Element) n;
				e.setAttribute(paraName, paraValue);
			}
			queryNodeCreateDate(node);
			super.updateTime(node);

			String xml = UXml.asXml(node);
			this.updateItem(itemName, xml);
		}
		this._IsBatch = false;
		this.writeXml(null);
	}

}
