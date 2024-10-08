package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;

public class UserDirXmls {
	private static Logger LOGGER = LoggerFactory.getLogger(UserDirXmls.class);
	private StringBuilder _Xml;
	private boolean _IsShowItems = false;
	private static HashMap<Integer, String> _XmlItems = new HashMap<Integer, String>();

	private RequestValue rv;

	public UserDirXmls(String group) {
		if (group != null && group.trim().equalsIgnoreCase("group")) {
			this._IsShowItems = false;
		} else {
			this._IsShowItems = true;
		}

		_Xml = new StringBuilder();
		this.initExcludes();
		this.createTreeNode();
	}

	/**
	 * 获取过滤的目录
	 */
	private String initExcludes() {
		MList lst = new MList();
		lst.add("CVS");
		lst.add(ConfigUtils.RECYCLE_NAME);

		String xmlName = UPath.getRealPath() + "/ewa_conf.xml";
		File f = new File(xmlName);
		if (!f.exists()) {
			return lst.join(",");
		}

		Document doc;

		try {
			doc = UXml.retDocument(f.getAbsolutePath());
			NodeList nl = doc.getElementsByTagName("debug");
			if (nl.getLength() == 0) {
				return lst.join(",");
			}
			Element ele = (Element) nl.item(0);

			// The unincludes is old version , misspelling
			String excludes = ele.hasAttribute("excludes") ? ele.getAttribute("excludes")
					: ele.getAttribute("unincludes");
			if (excludes == null || excludes.trim().length() == 0) {
				return lst.join(",");
			}
			lst.add(excludes);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return lst.join(",");
	}

	public UserDirXmls() {
		this._IsShowItems = true;
		_Xml = new StringBuilder();
		// this.createTreeNode();
	}

	public Dirs getCfgsByJdbc(ConfScriptPath scriptPath) {
		JdbcConfigOperation op = new JdbcConfigOperation(scriptPath);
		DTTable tb = op.getJdbcCfgDirs();
		if (tb.getCount() == 0) {
			return null;
		}
		Dirs dirs = new Dirs();

		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < tb.getCount(); i++) {
			String xmlname = tb.getCell(i, 0).toString();
			Integer cfgCount = tb.getCell(i, 1).toInt();
			String[] paths = xmlname.split("\\|");
			String parentPath = "";
			for (int k = 1; k < paths.length; k++) {
				String name = paths[k];
				String path = parentPath + "/" + name;

				if (!map.containsKey(path)) {
					// public Dir(String name, String path, String parentPath,boolean isFile)
					boolean isFile = false;
					if (k == paths.length - 1 && cfgCount != null) {
						isFile = true;
					}
					Dir dir = new Dir(name, path, parentPath, isFile);
					dirs.addDir(dir);
					map.put(path, "");
				}

				parentPath = path;
			}

		}
		return dirs;
	}

	private void createTreeNode() {
		ConfScriptPaths sps = ConfScriptPaths.getInstance();

		long t_start = System.currentTimeMillis();
		ArrayList<Dir> al = new ArrayList<Dir>();
		for (int i = 0; i < sps.getLst().size(); i++) {
			ConfScriptPath sp = sps.getLst().get(i);
			Dirs dirs;
			if (sp.isReadOnly()) {
				continue;
			} else if (sp.isJdbc()) { // jdbc
				dirs = this.getCfgsByJdbc(sp);
				if (dirs != null) {
					ArrayList<Dir> a = dirs.getDirs();
					al.addAll(a);
				}
			} else { // file

				String scriptPath = sp.getPath();
				File f = new File(scriptPath);
				if (!f.exists()) {
					LOGGER.warn("The conf path not exists, " + f.getAbsolutePath());
					continue;
				}

				dirs = new Dirs(scriptPath, true);
				dirs.initUnIncludes(this.initExcludes());

				String[] filter = { "xml" };

				dirs.setFiletes(filter);
				ArrayList<Dir> a = dirs.getDirs();
				if (a != null) {
					al.addAll(a);
				}
			}

		}

		// 获取目录和文件
		Dir[] dirArr = new Dir[al.size()];
		al.toArray(dirArr);
		// 按目录名称排序
		Arrays.sort(dirArr, new Comparator<Dir>() {
			@Override
			public int compare(Dir o1, Dir o2) {
				return o1.getPath().toLowerCase().compareTo(o2.getPath().toLowerCase());
			}
		});
		long t_dirs_and_files = System.currentTimeMillis();

		_Xml.append("<FrameData>");
		for (int i = 0; i < dirArr.length; i++) {
			Dir d = dirArr[i];
			if (d.getName().startsWith(".")) {
				continue;
			}
			if (!d.isFile()) {
				this.createTreeNode(d);
			}
		}
		for (int i = 0; i < dirArr.length; i++) {
			Dir d = dirArr[i];
			if (d.isFile()) {
				this.createTreeNode(d);
			}
		}
		_Xml.append("</FrameData>");

		long t_xml = System.currentTimeMillis();
		String msg = "DIRS_AND_FILES: " + (t_dirs_and_files - t_start) + ", XMLS: " + (t_xml - t_dirs_and_files)
				+ ", TOTAL: " + (t_xml - t_start);
		LOGGER.info(msg);

	}

	private void createTreeNode(Dir d) {
		if (!d.isFile()) {
			String xml = createNodeXml(d.getPath(), d.getName(), d.getParentPath(), "0", "");
			_Xml.append(xml);
		} else {
			String xml = createXmlItems(d);
			_Xml.append(xml);
		}

	}

	private static String createNodeXml(String key, String name, String pkey, String type, String des) {
		StringBuilder sb = new StringBuilder();
		sb.append("<Row Key=\"");
		sb.append(Utils.textToInputValue(key));
		sb.append("\" Name=\"");
		sb.append(Utils.textToInputValue(name));
		sb.append("\" ParentKey=\"");
		sb.append(Utils.textToInputValue(pkey));

		sb.append("\" Type=\"");
		sb.append(Utils.textToInputValue(type));

		sb.append("\" Des=\"");
		sb.append(Utils.textToInputValue(des));

		if (type.equals("1") || type.equals("0")) {
			sb.append("\" EWAMORECNT='1' />\r\n");
		} else {
			sb.append("\" EWAMORECNT='0' />\r\n");
		}
		return sb.toString();
	}

	/**
	 * 获取缓存的配置信息
	 * 
	 * @param fileName
	 * @return
	 */
	private static String getCntFromFiledCache(String fileName) {

		String name = getCachedName(fileName);

		File f2 = new File(name);
		if (f2.exists()) {
			try {
				String cnt = UFile.readFileText(name);
				return cnt;
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}

	}

	/**
	 * 根据配置文件生成缓存文件名称
	 * 
	 * @param xmlName
	 * @return
	 */
	private static String getCachedName(String xmlName) {
		File f = new File(xmlName);
		String path = f.getAbsolutePath().hashCode() + "";
		String code1 = f.getAbsolutePath() + "?" + f.length() + "?" + f.lastModified() + "?";
		String name = UPath.getCachedPath() + "/xmlcached/" + path + "/" + code1.hashCode() + ".xmlcache";

		return name;
	}

	/**
	 * 保存缓存文件
	 * 
	 * @param fileName
	 * @param cnt
	 */
	private static void saveCntToCache(String fileName, String cnt) {
		String name = getCachedName(fileName);

		// 寻找Cache目录
		File f = new File(name);
		File parent = f.getParentFile();
		// 删除已经存在的无效缓存
		if (parent.exists()) {
			File[] files = parent.listFiles();
			for (int i = 0; i < files.length; i++) {
				File ff = files[i];
				if (ff.isDirectory()) {
					continue;
				}
				try {
					ff.delete();
				} catch (Exception err) {
					LOGGER.error("Fail to remove xml-item cache, " + ff.getAbsolutePath() + ", " + err.getMessage());
				}
			}
		}

		try {
			UFile.createNewTextFile(name, cnt);
		} catch (IOException e) {
			LOGGER.error("Fail to save xml-item cache, " + e.getMessage());
		}
	}

	/**
	 * 加载配置文件的配置项,分级加载使用
	 * 
	 * @param xmlname
	 * @return
	 */
	public static String loadXmlItems(String xmlname) {
		StringBuilder sb = new StringBuilder();
		sb.append("<FrameData>");
		IConfig ct = UserConfig.getConfig(xmlname, null);
		if (ct.getScriptPath().isResources()) {
			sb.append("</FrameData>");
			return sb.toString();
		}

		if (ct.getScriptPath().isJdbc()) {
			JdbcConfigOperation op = new JdbcConfigOperation(ct.getScriptPath());
			DTTable tb = op.getJdbcItems(xmlname);

			for (int i = 0; i < tb.getCount(); i++) {
				String itemname = tb.getCell(i, 0).toString();
				String key = xmlname + "*" + itemname;
				String[] names = itemname.split("\\.");
				String name = names[0];
				for (int k = 1; k < names.length; k++) {
					if (k == 1) {
						name += ".<font color=red><b>" + names[1] + "</b></font>";
					} else if (k == 2) {
						name += ".<font color=darkred><b>" + names[2] + "</b></font>";
					} else {
						name += "." + names[k];
					}
				}
				String xml = createNodeXml(key, name, xmlname, "3", itemname);
				sb.append(xml);
			}
			sb.append("</FrameData>");
		} else {
			String xmlFilePath = ct.getScriptPath().getPath() + xmlname.replace("|", "/");

			String cachedValue = getCntFromFiledCache(xmlFilePath);
			if (cachedValue != null && cachedValue.indexOf("<FrameData>") == 0) {
				// 避免老版本cache错误
				return cachedValue;
			}

			UserXmls userXmls = new UserXmls(xmlname);
			userXmls.initXml();
			List<UserXml> a = userXmls.getXmls();

			String[] xmls = new String[a.size()];
			HashMap<String, UserXml> ha = new HashMap<String, UserXml>();
			for (int m = 0; m < a.size(); m++) {
				xmls[m] = a.get(m).getName();
				ha.put(a.get(m).getName(), a.get(m));
			}
			Arrays.sort(xmls);

			for (int m = 0; m < xmls.length; m++) {
				UserXml ux = ha.get(xmls[m]);
				String key = xmlname + "*" + ux.getName();
				String[] names = ux.getName().split("\\.");
				String name = names[0];
				for (int k = 1; k < names.length; k++) {
					if (k == 1) {
						name += ".<font color=red><b>" + names[1] + "</b></font>";
					} else if (k == 2) {
						name += ".<font color=darkred><b>" + names[2] + "</b></font>";
					} else {
						name += "." + names[k];
					}
				}
				String xml = createNodeXml(key, name, xmlname, "3", ux.getDescription());
				sb.append(xml);
			}
			sb.append("</FrameData>");
			saveCntToCache(xmlFilePath, sb.toString());
		}

		return sb.toString();
	}

	private String createXmlItems(Dir d) {
		String scriptPath = UPath.getScriptPath();
		String fileName = scriptPath + d.getPath().replace("|", "/");
		Integer id = Integer.valueOf(fileName.hashCode());
		StringBuilder sb = new StringBuilder();
		sb.append(createNodeXml(d.getPath(), d.getName(), d.getParentPath(), "1", ""));
		// why ? forget
		int abc = 1;
		if (!this._IsShowItems || abc == 1) {// 显示配置项信息
			return sb.toString();
		}
		if (!UFileCheck.fileChanged(fileName) && _XmlItems.containsKey(id)) {
			// 检查文件是否更改，无更改就直接从缓存中获取，加快运行效率
			return _XmlItems.get(id);
		}

		String cachedValue = getCntFromFiledCache(fileName);
		if (cachedValue != null) {
			return cachedValue;
		}

		UserXmls userXmls = new UserXmls(fileName);
		userXmls.initXml();
		List<UserXml> a = userXmls.getXmls();

		String[] xmls = new String[a.size()];
		HashMap<String, UserXml> ha = new HashMap<String, UserXml>();
		for (int m = 0; m < a.size(); m++) {
			xmls[m] = a.get(m).getName();
			ha.put(a.get(m).getName(), a.get(m));
		}
		Arrays.sort(xmls);

		for (int m = 0; m < xmls.length; m++) {
			UserXml ux = ha.get(xmls[m]);
			String key = d.getPath() + "*" + ux.getName();
			String[] names = ux.getName().split("\\.");
			String name = names[0];
			for (int k = 1; k < names.length; k++) {
				if (k == 1) {
					name += ".<font color=red><b>" + names[1] + "</b></font>";
				} else if (k == 2) {
					name += ".<font color=darkred><b>" + names[2] + "</b></font>";
				} else {
					name += "." + names[k];
				}
			}
			String xml = createNodeXml(key, name, d.getPath(), "3", ux.getDescription());
			sb.append(xml);
		}
		if (_XmlItems.containsKey(id)) {
			_XmlItems.remove(id);
		}
		_XmlItems.put(id, sb.toString());
		saveCntToCache(fileName, sb.toString());
		return sb.toString();
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {

		return _Xml.toString();
	}

	public RequestValue getRv() {
		return rv;
	}

	public void setRv(RequestValue rv) {
		this.rv = rv;
	}
}
