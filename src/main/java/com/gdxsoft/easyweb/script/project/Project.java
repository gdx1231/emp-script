package com.gdxsoft.easyweb.script.project;

import java.util.ArrayList;
import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.Descriptions;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
@Deprecated
public class Project {
	private Descriptions _DescriptionSet;
	private String _Acl;
	private String _ProjectPath;
	private ArrayList<String> _Datasources = new ArrayList<String>();
	private HashMap<String, Resource> _Resources = new HashMap<String, Resource>();
	private HashMap<String, PageInfo> _PageInfos = new HashMap<String, PageInfo>();

	/**
	 * 获取资源
	 * 
	 * @param resourceName
	 *            资源名
	 * @return 文件名
	  * @throws Exception The exception
	 */
	public String getResource(String resourceName) throws Exception {
		if (resourceName == null)
			return "";
		String name = resourceName.trim().toUpperCase();
		if (!this._Resources.containsKey(name)) {
			return "";
		}
		Resource r = this._Resources.get(name);
		String encode = r.getEncoder().trim().toUpperCase();
		String path = UPath.getRealContextPath() + UPath.PATH_PRJ_CACHE;
		String fileName;
		if (encode.equals("BASE64")) {
			fileName = UFile.createUnGZipHashFile(r.getInnerValue(), getExt(r), path, false);

		} else {
			fileName = UFile.createHashTextFile(r.getInnerValue(), getExt(r),
					path, false);
		}
		return fileName;
	}

	private String getExt(Resource r) {
		String t = r.getType().toLowerCase();
		if (t.indexOf("_") > 0) {
			return t.split("\\_")[1].trim().toLowerCase();
		} else {
			return t;
		}
	}

	/**
	 * @return the _DescriptionSet
	 */
	public Descriptions getDescriptionSet() {
		return _DescriptionSet;
	}

	/**
	 * @param descriptionSet
	 *            the _DescriptionSet to set
	 */
	public void setDescriptionSet(Descriptions descriptionSet) {
		_DescriptionSet = descriptionSet;
	}

	/**
	 * @return the _Acl
	 */
	public String getAcl() {
		return _Acl;
	}

	/**
	 * @param acl
	 *            the _Acl to set
	 */
	public void setAcl(String acl) {
		_Acl = acl;
	}

	/**
	 * @return the _ProjectPath
	 */
	public String getProjectPath() {
		return _ProjectPath;
	}

	/**
	 * @param projectPath
	 *            the _ProjectPath to set
	 */
	public void setProjectPath(String projectPath) {
		_ProjectPath = projectPath;
	}

	/**
	 * @return the _Datasources
	 */
	public ArrayList<String> getDatasources() {
		return _Datasources;
	}

	/**
	 * @param datasources
	 *            the _Datasources to set
	 */
	public void setDatasources(ArrayList<String> datasources) {
		_Datasources = datasources;
	}

	/**
	 * @return the _Resources
	 */
	public HashMap<String, Resource> getResources() {
		return _Resources;
	}

	/**
	 * @param resources
	 *            the _Resources to set
	 */
	public void setResources(HashMap<String, Resource> resources) {
		_Resources = resources;
	}

	/**
	 * @return the _PageInfos
	 */
	public HashMap<String, PageInfo> getPageInfos() {
		return _PageInfos;
	}

	/**
	 * @param pageInfos
	 *            the _PageInfos to set
	 */
	public void setPageInfos(HashMap<String, PageInfo> pageInfos) {
		_PageInfos = pageInfos;
	}

}
