package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;
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
		IUpdateXml up;

		if (JdbcConfig.isJdbcResources()) {
			up = new UpdateXmlJdbcImpl(xmlName);
		} else {
			up = new UpdateXmlImpl(xmlName);
		}
		return up;
	}

	public ConfigUtils() {

	}

	/**
	 * 加载ddl
	 * 
	 * @return
	 */
	public String loadDdls() {
		String cnt = null;

		if (JdbcConfig.isJdbcResources()) {
			StringBuilder sb = new StringBuilder();
			sb.append("var ddls=");
			sb.append(JdbcConfig.getOth("ewa_drop_list.json"));
			cnt = sb.toString();
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
		HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
		String root = UPath.getScriptPath();
		if (JdbcConfig.isJdbcResources()) {
			DTTable tb = JdbcConfig.getAllXmlnames();
			for (int ia = 0; ia < tb.getCount(); ia++) {
				String xmlName = tb.getCell(ia, "XMLNAME").toString();
				String xml = JdbcConfig.getXml(xmlName);
				Document doc = UXml.asDocument(xml);
				NodeList nl = doc.getElementsByTagName("DopListShow");
				for (int i = 0; i < nl.getLength(); i++) {
					Element ele = (Element) nl.item(i);
					handleDdl(ele, map);
				}
			}
		} else {
			handleFiles(root, map);
		}

		JSONArray ddl = new JSONArray();
		int inc = 0;
		for (String key : map.keySet()) {
			ddl.put(map.get(key));
			inc++;
		}
		if (JdbcConfig.isJdbcResources()) {
			JdbcConfig.updateOth("ewa_drop_list.json", ddl.toString(), admId);
		} else {
			String ddl_file = root + "/ewa_drop_list.json";
			UFile.createNewTextFile(ddl_file, ddl.toString());
		}

		return inc;
	}

	private void handleFiles(String root, HashMap<String, JSONObject> map) {
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

	private void handleFile(File f, HashMap<String, JSONObject> map) {
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

	private void handleDdl(Element ele, HashMap<String, JSONObject> map) {
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

	public String rename(String path, String newName) {
		if (path.indexOf("*") < 0) {
			if (JdbcConfig.isJdbcResources()) {
				JdbcConfig.renameTree(path, newName);
			} else {
				UFile.renameFile(path, newName);
			}
		} else {
			path = path.replace("|", "/");
			String[] paths = path.split("\\*");
			IUpdateXml ux = getUpdateXml(paths[0]);
			ux.rename(paths[1], newName);
		}
		return "";
	}

	public String copyXmlFile(String fromFileName, String toPath, String toFileName) throws IOException {
		if (JdbcConfig.isJdbcResources()) {
			String from = fromFileName.replace("|", "/");
			String to = toPath.replace("|", "/") + "/" + toFileName;
			JdbcConfig.copyXml(from, to, "");
		} else {
			String from = UPath.getScriptPath() + fromFileName.replace("|", "/");
			String to = UPath.getScriptPath() + toPath.replace("|", "/") + "/" + toFileName;
			UFile.copyFile(from, to);
		}
		// key_out 生成的主键
		// PARAMETERS_OUT,附加的参数，用","分割,
		// MENUGROUP_OUT 菜单组
		// 用于替换页面上的新节点属性
		String out = "key=" + toPath + "|" + toFileName + "&type=1";
		return out;
	}

	public String createNewXml(String xmlName, String path) throws IOException {
		if (xmlName.endsWith(".xml")) {
			if (JdbcConfig.isJdbcResources()) {
				String path1 = path.replace("|", "/");
				String fileName = path1 + "/" + xmlName;

				JdbcConfig.createXml(fileName, "");
			} else {
				String s1 = XML_ROOT;
				String path1 = UPath.getScriptPath() + path.replace("|", "/");
				String fileName = path1 + "/" + xmlName;
				UFile.createNewTextFile(fileName, s1);
			}
			// key_out 生成的主键
			// PARAMETERS_OUT,附加的参数，用","分割,
			// MENUGROUP_OUT 菜单组
			// 用于替换页面上的新节点属性
			String out = "key=" + path + "|" + xmlName + "&type=1";
			return out;
		} else {
			if (JdbcConfig.isJdbcResources()) {
				String path1 = path.replace("|", "/");
				String fileName = path1 + "/" + xmlName;
				JdbcConfig.createPath(fileName);
			} else {
				String path1 = UPath.getScriptPath() + path.replace("|", "/");
				String fileName = path1 + "/" + xmlName;
				File f = new File(fileName);
				f.mkdirs();
			}
			String out = "key=" + path + "|" + xmlName + "&type=0";
			return out;
		}
	}

	public String deleteFile(String key) {
		if (JdbcConfig.isJdbcResources()) {
			JdbcConfig.removeItem(key, "");
		} else {
			String path1 = UPath.getScriptPath() + key.replace("|", "/");
			String pathRecycle = UPath.getScriptPath() + RECYCLE_NAME;
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
		}
		return "";
	}
}
