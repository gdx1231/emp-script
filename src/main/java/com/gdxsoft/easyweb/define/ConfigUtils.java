package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ScriptPath;
import com.gdxsoft.easyweb.conf.ScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfigUtils {
	public static String RECYCLE_NAME = "__recycle__";
	public static String XML_ROOT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><EasyWebTemplates />";
	private static Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

	/**
	 * 获取Xml操作对象
	 * 
	 * @param xmlName
	 * @return
	 */
	public static IUpdateXml getUpdateXml(String xmlName) {
		IUpdateXml up = null;
		IConfig configType = UserConfig.getConfig(xmlName, null);
		if (configType == null) {
			return null;
		}
		if (configType.getScriptPath().isJdbc()) {
			up = new UpdateXmlJdbcImpl(configType);
		} else if (configType.getScriptPath().isResources()) {
		} else { // file
			up = new UpdateXmlImpl(configType);
		}
		return up;
	}

	/**
	 * 获取Xml操作对象
	 * 
	 * @param xmlName
	 * @return
	 */
	public static IUpdateXml getUpdateXmlByPath(String xmlPath) {
		ScriptPaths sps = ScriptPaths.getInstance();
		for (int i = 0; i < sps.getLst().size(); i++) {
			ScriptPath sp = sps.getLst().get(i);
			if (sp.isResources()) {
				continue;
			} else if (sp.isJdbc()) {
				JdbcConfigOperation op = new JdbcConfigOperation(sp);
				if (op.checkPathExists(xmlPath)) {
					UpdateXmlJdbcImpl o = new UpdateXmlJdbcImpl(sp);
					o.setXmlName(xmlPath);
					return o;
				}
			} else { // file
				String xmlPath1 = UserConfig.filterXmlName(xmlPath);
				String root = (sp.getPath() + xmlPath1);
				File f = new File(root);
				if (f.exists()) {
					UpdateXmlImpl o = new UpdateXmlImpl(sp);
					o.setXmlName(xmlPath1);
					return o;
				}
			}
		}
		return null;
	}

	private ScriptPath scriptPath;

	public ConfigUtils() {

	}

	public ConfigUtils(ScriptPath sp) {
		scriptPath = sp;
	}

	/**
	 * 加载ddl
	 * 
	 * @return
	 */
	public String loadDdls() {
		String cnt = null;

		if (scriptPath.isJdbc()) {
			JdbcConfigOperation op = new JdbcConfigOperation(scriptPath);
			StringBuilder sb = new StringBuilder();
			sb.append("var ddls=");
			sb.append(op.getOth("ewa_drop_list.json"));
			cnt = sb.toString();
		} else if (scriptPath.isResources()) {
			cnt = null;
		} else {
			String root = UPath.getScriptPath();
			String ddl_file = root + "/ewa_drop_list.json";

			File f = new File(ddl_file);
			if (f.exists()) {
				try {
					String json = UFile.readFileText(f.getAbsolutePath());
					JSONArray arr = new JSONArray(json);
					StringBuilder sb = new StringBuilder();
					sb.append("var ddls=");
					sb.append(arr.toString());
					cnt = sb.toString();
				} catch (Exception err) {
					cnt = "var ddls=[]";
					LOGGER.error(err.getLocalizedMessage());
				}
			}
		}
		if (cnt == null) {
			cnt = "var ddls=[];";
		}
		return cnt;
	}

	/**
	 * 刷新DDL
	 * 
	 * @return
	 * @throws Exception
	 */
	public int renewDdls(String admId) throws Exception {
		ScriptPath sp = this.scriptPath;
		if (sp.isJdbc()) {
			return this.renewDdlsJdbc(admId);
		} else if (sp.isJdbc()) {
			return 0;
		} else {
			return this.renewDdlsFile(admId);
		}
	}

	private int renewDdlsFile(String admId) throws Exception {
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();

		ScriptPath sp = this.scriptPath;
		String root = sp.getPath();
		handleFiles(root, map);

		JSONArray ddl = new JSONArray();
		int inc = 0;
		for (String key : map.keySet()) {
			ddl.put(map.get(key));
			inc++;
		}
		String ddl_file = root + "/ewa_drop_list.json";
		UFile.createNewTextFile(ddl_file, ddl.toString());

		return inc;
	}

	private int renewDdlsJdbc(String admId) throws Exception {
		ScriptPath sp = this.scriptPath;
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		JdbcConfigOperation op = new JdbcConfigOperation(sp);
		DTTable tb = op.getAllXmlnames();
		for (int ia = 0; ia < tb.getCount(); ia++) {
			String xmlName = tb.getCell(ia, "XMLNAME").toString();
			String xml = op.getXml(xmlName);
			Document doc = UXml.asDocument(xml);
			NodeList nl = doc.getElementsByTagName("DopListShow");
			for (int i = 0; i < nl.getLength(); i++) {
				Element ele = (Element) nl.item(i);
				handleDdl(ele, map);
			}
		}
		JSONArray ddl = new JSONArray();
		int inc = 0;
		for (String key : map.keySet()) {
			ddl.put(map.get(key));
			inc++;
		}

		op.updateOth("ewa_drop_list.json", ddl.toString(), admId);
		return inc;
	}

	private void handleFiles(String root, Map<String, JSONObject> map) {
		File froot = new File(root);
		File[] files = froot.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				handleFiles(f.getAbsolutePath(), map);
			} else {
				handleFile(f, map);
			}
		}
	}

	private void handleFile(File f, Map<String, JSONObject> map) {
		if (!f.getName().toLowerCase().endsWith(".xml")) {
			return;
		}
		try {
			Document doc = UXml.retDocument(f.getAbsolutePath());
			NodeList nl = doc.getElementsByTagName("DopListShow");
			for (int i = 0; i < nl.getLength(); i++) {
				Element ele = (Element) nl.item(i);
				handleDdl(ele, map);
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	private void handleDdl(Element ele, Map<String, JSONObject> map) {
		NodeList sets = ele.getElementsByTagName("Set");
		if (sets.getLength() == 0) {
			return;
		}

		Element set = (Element) sets.item(0);
		String DlsAction = set.getAttribute("DlsAction");
		String DlsShow = set.getAttribute("DlsShow");

		if (DlsAction == null || DlsAction.trim().length() == 0 || DlsShow == null || DlsShow.trim().length() == 0) {
			return;
		}

		Element p = (Element) ele.getParentNode();
		NodeList frames = p.getElementsByTagName("Frame");
		if (frames.getLength() == 0) {
			return;
		}

		Element frame = (Element) frames.item(0);

		NodeList sets1 = frame.getElementsByTagName("Set");
		if (sets1.getLength() == 0) {
			return;
		}

		Element frame_set = (Element) sets1.item(0);

		// <Set CallItemName="dl_city" CallPara="a"
		// CallXmlName="|2014_b2b|common|common.xml"/>

		String CallItemName = frame_set.getAttribute("CallItemName");
		String CallPara = frame_set.getAttribute("CallPara");
		String CallXmlName = frame_set.getAttribute("CallXmlName");

		if (CallItemName == null || CallItemName.trim().length() == 0 || CallPara == null
				|| CallPara.trim().length() == 0 || CallXmlName == null || CallXmlName.trim().length() == 0) {
			return;
		}

		JSONObject obj = new JSONObject();
		obj.put("CallItemName", CallItemName);
		obj.put("CallPara", CallPara);
		obj.put("CallXmlName", CallXmlName);
		obj.put("DlsAction", DlsAction);
		obj.put("DlsShow", DlsShow);

		String cnt = obj.toString();
		if (!map.containsKey(cnt)) {
			map.put(cnt, obj);
		}
	}

	public String rename(String path, String newName) throws IOException {

		checkInvalidCharInNameAndThrow(newName);
		boolean isRenameItem = path.indexOf("*") > 0;

		String sourcePath = isRenameItem ? path.split("\\*")[0] : path;
		String sourceItemName = isRenameItem ? path.split("\\*")[1] : null;
		boolean isXml = sourcePath.endsWith(".xml");

		IUpdateXml ux = getUpdateXml(sourcePath);
		if (ux == null) {
			ux = getUpdateXmlByPath(sourcePath);
		}

		if (ux == null) {
			LOGGER.warn("Not found ScriptPath");
			throw new IOException("Not found ScriptPath");
		}
		if (isRenameItem) {
			ux.renameItem(sourcePath, sourceItemName, newName);
		} else if (isXml) {
			ux.renameXmlFile(sourcePath, newName);
		} else {
			ux.renamePath(sourcePath, newName);
		}
		return "";
	}

	public String copyXmlFile(String fromFileName, String toPath, String toFileName) throws IOException {
		IUpdateXml ux = getUpdateXml(fromFileName);
		checkInvalidCharInNameAndThrow(toFileName);
		// key_out 生成的主键
		// PARAMETERS_OUT,附加的参数，用","分割,
		// MENUGROUP_OUT 菜单组
		// 用于替换页面上的新节点属性
		String out = ux.copyXmlFile(fromFileName, toPath, toFileName);
		return out;
	}

	public String createNewXml(String xmlName, String path) throws IOException {
		checkInvalidCharInNameAndThrow(xmlName);

		IUpdateXml ux = getUpdateXmlByPath(path);

		// String out = "key=" + path + "|" + xmlName + "&type=0|1";
		String out = ux.createNewXml(xmlName, path);
		return out;

	}

	private void checkInvalidCharInNameAndThrow(String xmlName) throws IOException {
		if (this.checkInvalidCharInName(xmlName)) {
			LOGGER.warn("Invalid char in name " + xmlName);
			throw new IOException("Invalid char in name " + xmlName);
		}
	}

	public boolean checkInvalidCharInName(String xmlName) {
		if (xmlName.indexOf("|") >= 0 || xmlName.indexOf("/") >= 0 || xmlName.indexOf("\\") >= 0
				|| xmlName.indexOf("?") >= 0) {

			return true;
		} else {
			return false;
		}
	}

	public String deleteFile(String xmlName) {
		IUpdateXml ux = getUpdateXml(xmlName);
		return ux.deleteFile(xmlName);
	}
}
