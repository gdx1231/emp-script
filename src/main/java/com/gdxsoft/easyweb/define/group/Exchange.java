package com.gdxsoft.easyweb.define.group;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UserXml;
import com.gdxsoft.easyweb.define.UserXmls;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.define.database.maps.MapFunction;
import com.gdxsoft.easyweb.define.database.maps.MapFunctions;
import com.gdxsoft.easyweb.define.database.maps.Maps;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class Exchange {
	private static Logger LOGGER = LoggerFactory.getLogger(Exchange.class);
	public static final String XML_CFG = "cfg.xml";
	public static final String XML_TABLE = "table.xml";
	public static final String XML_DATA = "data.xml";
	public static final String XML_RESOURCES = "res.xml";
	public static final String XML_DES = "des.xml";

	private String _Path; // 导入文件解压目录
	private Document _DocCfg;
	private Document _DocTable;
	private Document _DocData;
	private Document _DocRes;
	private Document _DocDes;

	private DataConnection _Conn;
	private String _ConnectionString;
	private String _Id;

	private String replaceMetaDatabaseName;
	private String replaceWorkDatabaseName;

	// 模块说明信息，主要用到 metaDatabase meta库名称和 workDatabase工作库名称
	private ModuleDescription moduleDescription;

	/**
	 * 模块说明信息，主要用到<br>
	 * metaDatabase meta库名称<br>
	 * workDatabase工作库名称
	 * 
	 * @return
	 */
	public ModuleDescription getModuleDescription() {
		return moduleDescription;
	}

	/**
	 * 模块说明信息，主要用到<br>
	 * metaDatabase meta库名称<br>
	 * workDatabase工作库名称
	 * 
	 * @param moduleDescription
	 */
	public void setModuleDescription(ModuleDescription moduleDescription) {
		this.moduleDescription = moduleDescription;
	}

	// 导入或导出的表列表
	private List<Table> tables = new ArrayList<>();

	public Exchange(String id) {
		this._Id = id;
	}

	public Exchange(String id, String connectionString) {
		this._Id = id;
		_ConnectionString = connectionString;
	}

	/**
	 * 导入数据与模块
	 * 
	 * @throws Exception
	 */
	public void importGroup() throws Exception {
		String fileName = this.getImportPath() + ".zip";
		this.importGroup(fileName);
	}

	/**
	 * 导入数据与模块
	 * 
	 * @throws Exception
	 */
	public void importGroup(String packageZipPath) throws Exception {
		LOGGER.info("Import module -> {}", packageZipPath);
		// 创建临时文件
		File tempFile = File.createTempFile(packageZipPath, ".zip");

		UFile.copyFile(packageZipPath, tempFile.getAbsolutePath());

		// 解压文件
		List<String> al = UFile.unZipFile(tempFile.getAbsolutePath());
		al.forEach(f -> {
			LOGGER.debug("\tUnpacked file {}", f);
		});

		// 删除临时文件
		UFile.delete(tempFile.getAbsolutePath());

		File f = new File(al.get(0));
		// 导入文件的解压目录
		this._Path = f.getParent();
		LOGGER.info("Unpacked file to -> {}", this._Path);

		this._DocCfg = this.readXmlDoc(XML_CFG);
		this._DocData = this.readXmlDoc(XML_DATA);
		this._DocTable = this.readXmlDoc(XML_TABLE);
		this._DocRes = this.readXmlDoc(XML_RESOURCES);
		this._DocDes = this.readXmlDoc(XML_DES);

		NodeList nl = this._DocDes.getElementsByTagName("Description");
		this.moduleDescription = new ModuleDescription();
		if (nl.getLength() > 0) {
			Element eleDescription = (Element) nl.item(0);
			UObjectValue.fromXmlNodes(eleDescription, moduleDescription);
		}

		this._Conn = new DataConnection();
		this._Conn.setConfigName(this._ConnectionString);
	}

	/**
	 * Delete the import's unpacked files
	 */
	public void removeImportTempFiles() {
		if (this._Path == null) {
			return;
		}

		File path = new File(this._Path);
		if (!path.exists()) {
			return;
		}

		try {
			FileUtils.deleteDirectory(path);
		} catch (IOException e) {
			LOGGER.warn("Delete the path {}, {}", path, e.getMessage());
		}
	}

	/**
	 * 导入表，表数据和视图
	 * 
	 * @return 所有的错误信息
	 * @throws Exception
	 */
	public String importTableAndData() throws Exception {
		return this.importTableAndData(true, true);
	}

	/**
	 * 导入表，表数据和视图
	 * 
	 * @param importTbs  是否导入表
	 * @param importData 是否导入数据
	 * @return 所有的错误信息
	 * @throws Exception
	 */
	public String importTableAndData(boolean importTbs, boolean importData) throws Exception {
		StringBuilder sbErr = new StringBuilder();
		ImportTables importTables = new ImportTables(this._DocTable, this._DocData, this._Conn);

		importTables.setModuleDescription(this.getModuleDescription());

		importTables.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
		importTables.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);
		if (importTbs) {
			String s = importTables.importTables();
			sbErr.append(s);
		}
		if (importData) {
			if (!importTbs) {
				// 导入表
				importTables.readTables();
				String databaseType = this._Conn.getDatabaseType();
				importTables.getSqlTables(databaseType);
			}
			String s1 = importTables.importDatas();
			sbErr.append(s1);
		}
		String logs = importTables.getSqls().toString();
		String logName = UPath.getGroupPath() + "/log/import_" + System.currentTimeMillis() + ".sql";
		try {
			UFile.createNewTextFile(logName, logs);
			LOGGER.info("LOG: {}", logName);
		} catch (IOException err) {
			LOGGER.warn("write log {}, {}", logName, err.getMessage());
		}
		return sbErr.toString();
	}

	public String importReses() {
		StringBuilder sb = new StringBuilder();
		NodeList nl = UXml.retNodeList(this._DocRes, "Resources/Resource");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			String name = UXml.retNodeValue(node, "FileName");
			String path = UXml.retNodeValue(node, "FilePath");
			path = path.replace("\\", "/");
			String[] paths = path.split("\\/");
			path = "";
			for (int m = 2; m < paths.length - 1; m++) {
				path += "/" + paths[m];
			}
			String p = UPath.getRealContextPath() + "/" + path + "/" + name;
			String cnt = UXml.retNodeValue(node, "FileContent");
			try {
				byte[] bytes = UConvert.FromBase64String(cnt);
				UFile.createBinaryFile(p, bytes, false);
				File f = new File(p);
				sb.append("\r\n" + f.getPath());
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				sb.append(e.getMessage() + "\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 导入根据 ExportDefaultXmlname 自动导入配置到指定目录
	 * 
	 * @param ux 导入对象
	 * @return
	 */
	public JSONObject importCfgsAutoPath(IUpdateXml ux) {
		NodeList nl = this._DocCfg.getElementsByTagName("EasyWebTemplate");
		Map<String, List<String>> xmlCfgGroup = new HashMap<>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element cfg = (Element) nl.item(i);

			String defaultItmename = cfg.getAttribute("ExportDefaultItmename");
			cfg.setAttribute("ItemName", defaultItmename);

			this.replaceCfgDatasource(cfg);
			this.replaceCfgSqlFunctions(cfg);

			String cfgXml = UXml.asXml(cfg);

			String defaultXmlName = cfg.getAttribute("ExportDefaultXmlname");
			defaultXmlName = UserConfig.filterXmlNameByJdbc(defaultXmlName);

			// 按照输出xmlname分组保存
			if (!xmlCfgGroup.containsKey(defaultXmlName)) {
				List<String> al = new ArrayList<>();
				xmlCfgGroup.put(defaultXmlName, al);
			}
			xmlCfgGroup.get(defaultXmlName).add(cfgXml);
		}
		JSONObject rst = UJSon.rstTrue(null);
		xmlCfgGroup.forEach((importXmlName, al) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EasyWebTemplates>");
			al.forEach(xml -> {
				sb.append(xml);
			});
			sb.append("</EasyWebTemplates>");
			// 按照 xmlname 分组进行导入
			JSONObject r = this.importCfgsByXmlContent(ux, importXmlName, sb.toString());
			rst.put(importXmlName, r);
		});

		return rst;
	}

	/**
	 * 根据xmlName导入配置信息
	 * 
	 * @param ux      导入对象
	 * @param xmlName 导入的配置文件名称
	 * @param cfgsXml
	 * @return
	 */
	private JSONObject importCfgsByXmlContent(IUpdateXml ux, String importXmlName, String cfgsXml) {
		try {
			// 创建临时文件
			File temp = File.createTempFile("importCfgsByXmlContent_", ".xml");
			UFile.createNewTextFile(temp.getAbsolutePath(), cfgsXml);
			LOGGER.info("Import cfg file {}", temp.getAbsolutePath());

			// 导入配置
			JSONObject rst = ux.importXml("", importXmlName, temp.getAbsolutePath());
			LOGGER.info("Import result: {}", rst);

			// 删除临时文件
			UFile.delete(temp.getAbsolutePath());

			return rst;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return UJSon.rstFalse(e.getMessage());
		}

	}

	private void replaceCfgDatasource(Element parent) {
		// 替换数据库连接池
		NodeList nl = parent.getElementsByTagName("DataSource");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getChildNodes().getLength() == 1) {
				Element ele = (Element) node.getChildNodes().item(0);
				ele.setAttribute("DataSource", this._ConnectionString);
			}
		}
	}

	private void replaceCfgSqlFunctions(Element parent) {
		// 提取所有SQL
		NodeList nl = parent.getElementsByTagName("Sql");
		ArrayList<CDATASection> al = new ArrayList<CDATASection>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			for (int mm = 0; mm < node.getChildNodes().getLength(); mm++) {
				Node nm = node.getChildNodes().item(mm);
				if (nm.getNodeType() == Node.CDATA_SECTION_NODE) {
					al.add((CDATASection) nm);
				}
			}
		}

		String targetDatabaseType = this._Conn.getDatabaseType();
		// 转换不同类型数据库的 SQL function
		try {
			MapFunctions funcs = Maps.instance().getMapFunctions();
			HashMap<String, MapFunction> types = funcs.getTypes(targetDatabaseType);

			for (int i = 0; i < al.size(); i++) {
				CDATASection sec = al.get(i);
				String sql = sec.getTextContent();
				if (sql == null || sql.trim().length() == 0) {
					continue;
				}
				Iterator<String> it = types.keySet().iterator();
				while (it.hasNext()) {
					MapFunction map = types.get(it.next());
					sql = sql.replace(map.getEwa().getName(), map.getName());
				}
				sec.setTextContent(sql);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	/**
	 * @deprecated 导入配置文件
	 * 
	 * @param path 文件路径及文件名 例如：|test|oa_master.xml
	 * @return 生成文件的路径
	 * @throws IOException
	 */
	public String importCfgs(String path, IUpdateXml ux) throws IOException {
		if (ux.getScriptPath().isJdbc()) {
		} else {
			String path1 = ux.getScriptPath().getPath() + UserConfig.filterXmlName(path);
			File f = new File(path1);

			int m = 0;
			File fRen = new File(path1);
			while (fRen.exists()) {
				String f1 = f.getPath() + "." + m + ".bak";
				fRen = new File(f1);
				m++;
				if (m > 100) {
					break;
				}
			}
			if (!fRen.exists()) {
				f.renameTo(fRen);
			}
			UFile.buildPaths(f.getParent());
		}

		String xml = UXml.asXmlAll(this._DocCfg);
		xml = xml.replace("{#XMLNAME}", path);
		this._DocCfg = UXml.asDocument(xml);

		// 替换数据库连接池
		NodeList nl = this._DocCfg.getElementsByTagName("DataSource");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getChildNodes().getLength() == 1) {
				Element ele = (Element) node.getChildNodes().item(0);
				ele.setAttribute("DataSource", this._ConnectionString);
			}
		}

		// 提取所有SQL
		nl = this._DocCfg.getElementsByTagName("Sql");
		ArrayList<CDATASection> al = new ArrayList<CDATASection>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			for (int mm = 0; mm < node.getChildNodes().getLength(); mm++) {
				Node nm = node.getChildNodes().item(mm);
				if (nm.getNodeType() == Node.CDATA_SECTION_NODE) {
					al.add((CDATASection) nm);
				}
			}
		}

		// 转换不同类型数据库的 SQL function
		try {
			MapFunctions funcs = Maps.instance().getMapFunctions();
			HashMap<String, MapFunction> types = funcs.getTypes(this._Conn.getDatabaseType());

			for (int i = 0; i < al.size(); i++) {
				CDATASection sec = al.get(i);
				String sql = sec.getTextContent();
				Iterator<String> it = types.keySet().iterator();
				while (it.hasNext()) {
					MapFunction map = types.get(it.next());
					sql = sql.replace(map.getEwa().getName(), map.getName());
				}
				sec.setTextContent(sql);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		al.clear();

		// 创建和临时文件
		File temp = File.createTempFile(Utils.getGuid(), "xml");
		UXml.saveDocument(this._DocCfg, temp.getAbsolutePath());

		// 导入配置
		JSONObject rst = ux.importXml("", path, temp.getAbsolutePath());

		UFile.delete(temp.getAbsolutePath());
		return rst.toString();
	}

	private Document readXmlDoc(String fileName) throws Exception {
		String path = this._Path + "/" + fileName;
		return UXml.retDocument(path);
	}

	/**
	 * 导出模块Zip文件
	 * 
	 * @return 模块Zip文件地址
	 * @throws Exception
	 */
	public String exportGroup() throws Exception {
		this._Path = this.getExportPath() + "/";
		this._DocCfg = this.createDoc(XML_CFG, "EasyWebTemplates");
		this._DocData = this.createDoc(XML_DATA, "Datas");
		this._DocTable = this.createDoc(XML_TABLE, "Tables");
		this._DocRes = this.createDoc(XML_RESOURCES, "Resources");
		this._DocDes = UXml.retDocument(this._Path + XML_DES);

		// export table and data
		NodeList nl = UXml.retNodeList(this._DocDes, "Tables/Table");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			String tableName = UXml.retNodeValue(node, "TableName");
			String dataSource = UXml.retNodeValue(node, "DataSource");
			String tableType = UXml.retNodeValue(node, "TableType");

			Table table = this.exportTable(tableName, dataSource, tableType);

			// 来源参考
			String refId = UXml.retNodeValue(node, "RefId");
			table.setRefId(refId);

			this.tables.add(table);

			String IsExport = UXml.retNodeValue(node, "IsExport");
			String where = UXml.retNodeValue(node, "ExportWhere");

			// 视图 VIEW 不输出数据
			if (IsExport.equals("true")) {
				this.exportData(table, where);
			}
		}

		nl = UXml.retNodeList(this._DocDes, "EasyWebTemplates/EasyWebTemplate");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);

			this.exportItem((Element) node);
		}
		this.replaceFunctions(); // 替换配置文件中的Sql方法

		nl = UXml.retNodeList(this._DocDes, "Resources/Resource");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			String fileUrl = UXml.retNodeValue(node, "FileUrl");
			String fileName = UXml.retNodeValue(node, "FileName");

			this.exportRes(fileUrl, fileName);
		}

		return this.saveAndZip();
	}

	public String saveAndZip() throws IOException {
		saveDoc(this._DocData, XML_DATA);
		saveDoc(this._DocTable, XML_TABLE);
		saveDoc(this._DocRes, XML_RESOURCES);
		saveDoc(this._DocCfg, XML_CFG);

		String f = UFile.zipPath(this._Path);
		return f;
	}

	public void exportRes(String fileUrl, String fileName) {
		try {
			URI u = new URI(fileUrl);
			String p = UPath.getRealContextPath() + "/../" + u.getPath();
			String s = UFile.readFileBase64(p);
			Element ele = this._DocRes.createElement("Resource");
			ele.setAttribute("FileName", fileName);
			ele.setAttribute("FilePath", u.getPath());
			ele.setAttribute("FileContent", s);

			this._DocRes.getFirstChild().appendChild(ele);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

	}

	/**
	 * 输出表
	 * 
	 * @param tableName
	 * @param oriDataSource
	 * @return
	 */
	public Table exportTable(String tableName, String oriDataSource, String tableType) {
		Table t = new Table(tableName, oriDataSource);
		t.setTableType(tableType);

		t.setReplaceMetaDatabaseName(this.replaceMetaDatabaseName);
		t.setReplaceWorkDatabaseName(this.replaceWorkDatabaseName);

		t.getFields();
		t.toXml((Element) this._DocTable.getFirstChild());

		return t;
	}

	/**
	 * 输出数据
	 * 
	 * @param t
	 * @param where
	 * @throws Exception
	 */
	public void exportData(Table t, String where) throws Exception {
		if (where == null || where.trim().length() == 0) {
			where = "1=1";
		}
		t.writeDataToXml(where, (Element) this._DocData.getFirstChild());
	}

	/**
	 * 输出EasyWebTemplate
	 * 
	 * @param cfgEle 配置信息
	 */
	public void exportItem(Element cfgEle) {
		String xmlName = UXml.retNodeValue(cfgEle, "XmlName");
		String itemName = UXml.retNodeValue(cfgEle, "ItemName");
		String description = UXml.retNodeValue(cfgEle, "Description");
		// 默认输出目录
		String exportDefaultXmlname = UXml.retNodeValue(cfgEle, "ExportDefaultXmlname");
		// 默认输出项名称
		String exportDefaultItmename = UXml.retNodeValue(cfgEle, "ExportDefaultItmename");

		UserXmls ux = new UserXmls(xmlName);
		if (ux.getUpdateXml() == null) {
			LOGGER.warn("Can't get conf: {}", xmlName);
			return;
		}
		ux.initXml();
		List<UserXml> al = ux.getXmls();
		String itemName1 = itemName.trim().toUpperCase();
		for (int i = 0; i < al.size(); i++) {
			UserXml u = al.get(i);
			if (!u.getName().trim().toUpperCase().equals(itemName1)) {
				continue;
			}

			LOGGER.info("Export the cfg {} {} {}", xmlName, itemName, description);

			String xml = this.replaceParas(u.getXml(), xmlName);

			Element itemNode = (Element) UXml.asNode(xml);

			itemNode.setAttribute("ExportSourceXmlname", xmlName);
			itemNode.setAttribute("ExportSourceItemname", itemName);
			// 默认输出目录
			itemNode.setAttribute("ExportDefaultXmlname", exportDefaultXmlname);
			// 默认输出项名称
			itemNode.setAttribute("ExportDefaultItmename", exportDefaultItmename);

			itemNode.setAttribute("ExportTime", Utils.getDateTimeString(new Date()));

			String xml1 = UXml.asXml(itemNode);
			this._DocCfg = UXml.appendNode(this._DocCfg, xml1, _DocCfg.getFirstChild().getNodeName());
			break;
		}
	}

	/**
	 * @deprecated 输出EasyWebTemplate
	 * 
	 * @param itemName
	 * @param xmlName
	 */
	public void exportItem(String itemName, String xmlName) {
		UserXmls ux = new UserXmls(xmlName);
		ux.initXml();
		List<UserXml> al = ux.getXmls();
		String itemName1 = itemName.trim().toUpperCase();
		for (int i = 0; i < al.size(); i++) {
			UserXml u = al.get(i);
			if (!u.getName().trim().toUpperCase().equals(itemName1)) {
				continue;
			}

			String xml = this.replaceParas(u.getXml(), xmlName);

			this._DocCfg = UXml.appendNode(this._DocCfg, xml, _DocCfg.getFirstChild().getNodeName());
			break;
		}
	}

	/**
	 * 替换Item中的XMLNAME等参数
	 * 
	 * @param source
	 * @param xmlName
	 * @return
	 */
	private String replaceParas(String source, String xmlName) {

		String s1 = xmlName;
		if (xmlName.substring(0, 1).equals("|")) {
			s1 = xmlName.substring(1);
		}

		s1 = s1.replace("|", "\\|");
		Pattern pat = Pattern.compile("\\b" + s1 + "\\b", Pattern.CASE_INSENSITIVE);
		Matcher m = pat.matcher(source);
		while (m.find()) {
			MatchResult r = m.toMatchResult();
			source = source.replace(r.group(), "{#XMLNAME}");

			System.out.println(r.start() + "-" + r.end() + " " + r.group());
		}

		return source;
	}

	/**
	 * 替换不同数据库的function
	 */
	private void replaceFunctions() {
		NodeList nl0 = UXml.retNodeList(this._DocCfg, "EasyWebTemplates/EasyWebTemplate");
		ConnectionConfigs connCfgs = null;
		try {
			connCfgs = ConnectionConfigs.instance();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		for (int k = 0; k < nl0.getLength(); k++) {
			Node node0 = nl0.item(k);
			NodeList nlDataSource = UXml.retNodeListByPath(node0, "Page/DataSource/Set");
			if (nlDataSource == null || nlDataSource.getLength() == 0) {
				continue;
			}
			String dataSource = ((Element) nlDataSource.item(0)).getAttribute("DataSource");
			ConnectionConfig connCfg = connCfgs.get(dataSource.toLowerCase().trim());
			if (connCfg == null) {
				continue;
			}

			NodeList nl = UXml.retNodeList(node0, "Sql");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				for (int m = 0; m < n.getChildNodes().getLength(); m++) {
					Node n1 = n.getChildNodes().item(m);
					if (n1.getNodeType() == Node.CDATA_SECTION_NODE) {
						replaceFunctions((CDATASection) n1, connCfg);
					}
				}
			}
		}
	}

	private void replaceFunctions(CDATASection sec, ConnectionConfig connCfg) {
		try {
			MapFunctions funcs = Maps.instance().getMapFunctions();
			HashMap<String, MapFunction> types = funcs.getTypes(connCfg.getType().toUpperCase());
			Iterator<String> it = types.keySet().iterator();
			String sql = sec.getTextContent();
			while (it.hasNext()) {
				MapFunction t = types.get(it.next());
				sql = sql.replace(t.getName(), t.getEwa().getName());
			}
			sec.setTextContent(sql);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private Document createDoc(String fileName, String rootTagName) {
		String path = this._Path + "/" + fileName;
		return UXml.createSavedDocument(path, rootTagName);
	}

	private boolean saveDoc(Document doc, String fileName) {
		String path = this._Path + "/" + fileName;

		return UXml.saveDocument(doc, path);
	}

	/**
	 * 导入文件解压目录
	 * 
	 * @return
	 */
	public String getPath() {
		return _Path;
	}

	public Document getDocCfg() {
		return _DocCfg;
	}

	public void setDocCfg(Document docCfg) {
		_DocCfg = docCfg;
	}

	public Document getDocTable() {
		return _DocTable;
	}

	public void setDocTable(Document docTable) {
		_DocTable = docTable;
	}

	public Document getDocData() {
		return _DocData;
	}

	public void setDocData(Document docData) {
		_DocData = docData;
	}

	public Document getDocRes() {
		return _DocRes;
	}

	public void setDocRes(Document docRes) {
		_DocRes = docRes;
	}

	public Document getDocDes() {
		return _DocDes;
	}

	public void setDocDes(Document docDes) {
		_DocDes = docDes;
	}

	public DataConnection getConn() {
		return _Conn;
	}

	public void setConn(DataConnection conn) {
		_Conn = conn;
	}

	public String getConnectionString() {
		return _ConnectionString;
	}

	public void setConnectionString(String connectionString) {
		_ConnectionString = connectionString;
	}

	public String getId() {
		return _Id;
	}

	public void setId(String id) {
		_Id = id;
	}

	/**
	 * @return the replaceMetaDatabaseName
	 */
	public String getReplaceMetaDatabaseName() {
		return replaceMetaDatabaseName;
	}

	/**
	 * @param replaceMetaDatabaseName the replaceMetaDatabaseName to set
	 */
	public void setReplaceMetaDatabaseName(String replaceMetaDatabaseName) {
		this.replaceMetaDatabaseName = replaceMetaDatabaseName;
	}

	/**
	 * @return the replaceWorkDatabaseName
	 */
	public String getReplaceWorkDatabaseName() {
		return replaceWorkDatabaseName;
	}

	/**
	 * @param replaceWorkDatabaseName the replaceWorkDatabaseName to set
	 */
	public void setReplaceWorkDatabaseName(String replaceWorkDatabaseName) {
		this.replaceWorkDatabaseName = replaceWorkDatabaseName;
	}

	/**
	 * 导出目录
	 * 
	 * @return
	 */
	public String getExportPath() {
		return UPath.getGroupPath() + "/exports/" + _Id;
	}

	/**
	 * 导入目录
	 * 
	 * @return
	 */
	public String getImportPath() {
		return UPath.getGroupPath() + "/imports/" + _Id;
	}

	/**
	 * @return the tables
	 */
	public List<Table> getTables() {
		return tables;
	}

}
