package com.gdxsoft.easyweb.script.project;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.ConfigCache;
import com.gdxsoft.easyweb.script.template.Descriptions;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ProjectDao {

	private String _ProjectName;
	private Document _PrjDoc;
	static String PATH_DescriptionSet = "EwaProject/DescriptionSet";
	static String PATH_Acl = "EwaProject/Acl";
	static String PATH_ProjectPath = "EwaProject/ProjectPath";
	static String PATH_Datasources = "EwaProject/Datasources/Datasource";
	static String PATH_DatasourcesNode = "EwaProject/Datasources";

	static String PATH_PageInfos = "EwaProject/PageInfos/PageInfo";
	static String PATH_PageInfosNode = "EwaProject/PageInfos";

	static String PATH_Resources = "EwaProject/Resources/Resource";
	static String PATH_ResourcesNode = "EwaProject/Resources";

	/**
	 * 新建项目文件
	 * 
	 * @param prjName
	 *            项目名称
	 * @param dataSources
	 *            数据源
	 * @param prjPath
	 *            项目路径
	 * @param prjAcl
	 *            权限
	 * @param memo
	 *            项目说明
	 * @param lang
	 *            语言
	 */
	public void newProject(String prjName, String dataSources, String prjPath,
			String prjAcl, String memo, String lang) {
		
		//生成顺序要遵循dtd文档定义的顺序
		Document doc = UXml.createBlankDocument("EwaProject.dtd", "EwaProject");
		Element root = doc.getDocumentElement();
		root.setAttribute("Name", prjName);

		// 主信息 
		Element mainInfo = doc.createElement("MainInfo");
		root.appendChild(mainInfo);
		Element ver = doc.createElement("Version");
		ver.setTextContent("1.0");
		mainInfo.appendChild(ver);
		Element comp = doc.createElement("Company");
		mainInfo.appendChild(comp);
		Element copyright = doc.createElement("Copyright");
		copyright.setTextContent("Copyright &copy; ....");
		mainInfo.appendChild(copyright);

		// 描述
		Element des = doc.createElement("DescriptionSet");
		root.appendChild(des);
		Element set = doc.createElement("Set");
		des.appendChild(set);
		set.setAttribute("Lang", lang);
		set.setAttribute("Info", memo);
		set.setAttribute("Memo", "");

		// 数据源
		Element ds = doc.createElement("Datasources");
		root.appendChild(ds);
		if (dataSources.trim().length() > 0) {
			String[] dss = dataSources.trim().split(",");
			for (int i = 0; i < dss.length; i++) {
				Element ds0 = doc.createElement("Datasource");
				ds0.setTextContent(dss[i]);
				ds.appendChild(ds0);
			}
		}

		// 权限
		Element acl = doc.createElement("Acl");
		root.appendChild(acl);
		acl.setTextContent(prjAcl);

		// 项目路径
		Element pp = doc.createElement("ProjectPath");
		root.appendChild(pp);
		pp.setTextContent(prjPath);

		// 页面信息
		Element ps = doc.createElement("PageInfos");
		root.appendChild(ps);

		// 资源
		Element res = doc.createElement("Resources");
		root.appendChild(res);

		UXml.saveDocument(doc, UPath.getProjectPath() + prjName + ".xml");
	}

	public Project builderProject(String projectName) throws Exception {
		this._ProjectName = projectName;
		Project p = ConfigCache.getProject(this._ProjectName);
		if (p == null) {
			this.initDocument();
			p = initProject();
			// 放到缓存中
			ConfigCache.setProject(this._ProjectName, p);
		}
		return p;
	}

	/**
	 * 增加资源文件
	 * 
	 * @param filePath
	 */
	public void addResource(String filePath) {
		File f1 = new File(filePath);
		if (!f1.exists()) {
			return;
		}
		ResourceDao rd = new ResourceDao();
		rd.addResource(filePath, this._PrjDoc);
		// System.out.println(UXml.asXml(this._PrjDoc));
	}

	/**
	 * 保存项目文件
	 */
	public void saveProject() {
		UXml.saveDocument(this._PrjDoc);

	}

	private void initDocument() throws Exception {
		String projPath = UPath.getConfigPath() + "/projects/"
				+ this._ProjectName;
		this._PrjDoc = UXml.retDocument(projPath);

	}

	private Project initProject() {
		Project p = new Project();

		Node ds = UXml.retNode(this._PrjDoc, PATH_DescriptionSet);
		p.setDescriptionSet(Descriptions.instanceDescriptions(ds));

		p.setAcl(this.getParaValue(PATH_Acl));
		p.setProjectPath(this.getParaValue(PATH_ProjectPath));

		NodeList nlRes = UXml.retNodeList(this._PrjDoc, PATH_Resources);
		for (int i = 0; i < nlRes.getLength(); i++) {
			Node n = nlRes.item(i);
			Resource r = new Resource();
			r.setEncoder(UXml.retNodeValue(n, "Encode"));
			r.setName(UXml.retNodeValue(n, "Name"));
			r.setType(UXml.retNodeValue(n, "Type"));
			r.setInnerValue(UXml.retNodeText(n).trim());
			p.getResources().put(r.getName().trim().toUpperCase(), r);
		}

		NodeList nlPageInfos = UXml.retNodeList(this._PrjDoc, PATH_PageInfos);
		for (int i = 0; i < nlPageInfos.getLength(); i++) {
			Node n = nlPageInfos.item(i);
			PageInfo pi = new PageInfo();
			pi.setName(UXml.retNodeValue(n, "Name"));
			pi.setDescriptionSet(Descriptions.instanceDescriptions(n));
			p.getPageInfos().put(pi.getName().trim().toUpperCase(), pi);
		}

		NodeList nlDs = UXml.retNodeList(this._PrjDoc, PATH_Datasources);
		for (int i = 0; i < nlDs.getLength(); i++) {
			Node n = nlDs.item(i);
			p.getDatasources().add(UXml.retNodeText(n).trim());
		}
		return p;
	}

	private String getParaValue(String path) {
		Node n = this.getNode(path);
		if (n == null) {
			return null;
		} else {
			return UXml.retNodeText(n);
		}
	}

	private Node getNode(String path) {
		return UXml.retNode(this._PrjDoc, path);
	}

	/**
	 * @return the _ProjectName
	 */
	public String getProjectName() {
		return _ProjectName;
	}

	/**
	 * @param projectName
	 *            the _ProjectName to set
	 * @throws Exception
	 */
	public void setProjectName(String projectName) throws Exception {
		_ProjectName = projectName;
		this.initDocument();
	}
}
