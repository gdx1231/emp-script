package com.gdxsoft.easyweb.define;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;

public class FileConf2JdbcConf {
	private static Logger LOGGER = LoggerFactory.getLogger(FileConf2JdbcConf.class);

	private JdbcConfigOperation op;

	public FileConf2JdbcConf(String scriptPathName) {
		ConfScriptPath erpAus = ConfScriptPaths.getInstance().getScriptPath(scriptPathName);
		op = new JdbcConfigOperation(erpAus);
	}

	/**
	 * 转换配置文件
	 * 
	 * @param root 配置文件所在根目录
	 */
	public void startConvert(String root) {
		// 转换本地xml配置文件到数据库 EWA中
		// root = "/Users/guolei/project/user.config.xml";
		File fRoot = new File(root);
		String rootPath = fRoot.getAbsolutePath();
		handleXmlPath(root, rootPath);
	}

	/**
	 * 递归导入目录下文件
	 * 
	 * @param path
	 * @param rootPath
	 */
	public void handleXmlPath(String path, String rootPath) {
		File currPath = new File(path);
		LOGGER.info("Scan the path: {} {}", path, rootPath);
		File[] files = currPath.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				continue;
			}
			if (f.getName().endsWith(".xml")) {
				try {
					this.handleXml(f, rootPath);
				} catch (Exception err) {
					LOGGER.error(err.getMessage());
				}
			}
		}
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				handleXmlPath(f.getAbsolutePath(), rootPath);
			}
		}

	}

	/**
	 * 导入配置文件
	 * 
	 * @param xml      配置文件
	 * @param rootPath 配置文件所在的根目录
	 * @throws Exception
	 */
	public void handleXml(File xmlFile, String rootPath) throws Exception {
		LOGGER.info("Import the cfg: {}, {}", xmlFile, rootPath);
		// 删除根目录部分剩下就是 xmlname
		String xmlname = xmlFile.getAbsolutePath().substring(rootPath.length());

		// 导入配置文件
		op.importXml(xmlFile, xmlname);
	}

}
