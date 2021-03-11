package com.gdxsoft.easyweb.conf;

import java.io.Serializable;

public class ConfScriptPath implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8615816282487845786L;
	private String path;
	private String name;

	private String resourceRoot;

	/**
	 * 用户配置信息是在数据库保存
	 * 
	 * @return
	 */
	public boolean isJdbc() {
		if (path == null) {
			return false;
		}
		return path.startsWith("jdbc:");
	}

	public boolean isResources() {
		if (path == null) {
			return false;
		}
		return path.startsWith("resources:");
	}

	/**
	 * 获取用户配置信息的jdbc
	 * 
	 * @return
	 */
	public String getJdbcConfigName() {
		if (isJdbc()) {
			return path.replace("jdbc:", "").trim();
		} else {
			return null;
		}
	}

	/**
	 * Get the resource root, not ends with "/"
	 * 
	 * @return
	 */
	public String getResourcesPath() {
		// <scriptPath name="ewa" path="resources:/user.xml/" />
		if (!this.isResources()) {
			return null;
		}
		if (resourceRoot != null) {
			return resourceRoot;
		}

		String root = this.path.replace("resources:", "").trim();
		root = root.replace("\\", "/");
		while (root.indexOf("//") > 0) {
			root = root.replace("//", "/");
		}
		if (root.endsWith("/")) {
			root = root.substring(0, root.length() - 1);
		}
		resourceRoot = root;
		return resourceRoot;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
