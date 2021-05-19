package com.gdxsoft.easyweb.script.userConfig;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.utils.UFile;

/**
 * The system inner configurations(not allow modify)
 *
 */
public class ResourceConfig extends ConfigBase implements IConfig, Serializable, Cloneable {

	public static Map<String, Map<String, String>> RESOURCE_CACHED = new ConcurrentHashMap<>();;

	private static final long serialVersionUID = -3698905095781681521L;
	private static Logger LOGGER = LoggerFactory.getLogger(ResourceConfig.class);

	/**
	 * Initialize the resources by the ConfScriptPath;
	 * 
	 * @param scriptPath
	 */
	public static void initializeResouces(ConfScriptPath scriptPath) {
		if (scriptPath.isResources()) {
			new ResourceConfig(scriptPath, null, null);
		} else {
			LOGGER.warn("Invalid ConfScriptPath type -> " + scriptPath.getPath());
		}
	}

	private boolean notExists = false;
	private boolean hasError = false;
	private String lastError = null;

	public ResourceConfig() {
		super();
	}

	public ResourceConfig(ConfScriptPath scriptPath, String xmlName, String itemName) {
		super(scriptPath, xmlName, itemName);
		String root = super.getScriptPath().getResourcesPath();

		if (RESOURCE_CACHED.containsKey(root)) {
			return;
		}

		URL url = ResourceConfig.class.getResource(root);

		if (url == null) {
			// the resource not exists
			LOGGER.warn("The resource: " + scriptPath.getName() + " NOT exists!");
			notExists = true;
			return;
		}

		String protocol = url.getProtocol();
		Map<String, String> xmlContents;
		if (protocol.equals("jar")) {
			xmlContents = this.initResourceByJar(url);
		} else {
			xmlContents = this.initResourceByFiles(url);
		}

		RESOURCE_CACHED.put(root, xmlContents);
	}

	/**
	 * Initialize the resources from files
	 * 
	 * @param url
	 * @return
	 */
	private Map<String, String> initResourceByFiles(URL url) {
		LOGGER.info("Load reource: " + url.toString());

		Map<String, String> xmlContents = new HashMap<>();

		try {
			File f1 = new File(url.toURI());
			reverseFiles(f1, f1.getAbsolutePath(), xmlContents);
		} catch (URISyntaxException e1) {
			this.hasError = true;
			LOGGER.error(e1.getMessage());
		}

		return xmlContents;
	}

	/**
	 * Reverse load files from dirs
	 * 
	 * @param parent
	 * @param rootPath
	 * @param xmlContents
	 */
	private void reverseFiles(File parent, String rootPath, Map<String, String> xmlContents) {
		String resourceRoot = super.getScriptPath().getResourcesPath();
		File[] fs = parent.listFiles();
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			if (f.isDirectory()) {
				continue;
			}
			if (!"xml".equalsIgnoreCase(UFile.getFileExt(f.getName()))) {
				continue;
			}

			String resourcePath = f.getAbsolutePath().replace(rootPath, resourceRoot);
			resourcePath = resourcePath.replace("\\", "/");
			try {
				String content = UFile.readFileText(f.getAbsolutePath());
				xmlContents.put(resourcePath, content);
				LOGGER.debug(" add -> " + resourcePath);
			} catch (IOException e) {
				LOGGER.warn("Read reource content: " + f.getAbsolutePath() + ", " + e.getMessage());
			}
		}
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			if (f.isDirectory()) {
				this.reverseFiles(f, rootPath, xmlContents);
			}
		}
	}

	/**
	 * Initialize the resources from jar file
	 * 
	 * @param url
	 * @return
	 */
	private Map<String, String> initResourceByJar(URL url) {
		LOGGER.info("Load reource: " + url.toString());
		String jarPath = url.getPath().split("\\!")[0];

		String dir = url.getPath().split("\\!")[1];
		// remove start with '/'
		String dirZip = dir.substring(1);

		Map<String, String> xmlContents = new HashMap<>();

		try {
			URL url1 = new URL(jarPath);
			File f1 = new File(url1.toURI());

			List<String> lst = UFile.getZipList(f1.getAbsolutePath());
			lst.forEach(fileName -> {
				if (!fileName.startsWith(dirZip) || !"xml".equalsIgnoreCase(UFile.getFileExt(fileName))) {
					return;
				}
				try {
					String content = UFile.readZipText(f1.getAbsolutePath(), fileName);
					xmlContents.put("/" + fileName, content);
					LOGGER.debug(" add -> " + fileName);
				} catch (IOException e) {
					LOGGER.warn("Read zip content: " + f1.getAbsolutePath() + "->" + fileName + ", " + e.getMessage());
				}
			});

		} catch (IOException | URISyntaxException e1) {
			this.hasError = true;
			LOGGER.error(e1.getMessage());
		}

		return xmlContents;
	}

	public String getPath() {
		String root = super.getScriptPath().getResourcesPath();
		return root + super.getFixedXmlName();
	}

	/**
	 * Check if the configuration file exists
	 */
	public boolean checkConfigurationExists() {
		if (this.notExists || this.hasError) {
			return false;
		}

		String root = super.getScriptPath().getResourcesPath();

		if (!RESOURCE_CACHED.containsKey(root)) {
			return false;
		}

		Map<String, String> xmlContents = RESOURCE_CACHED.get(root);
		String resourcePath = this.getPath();

		return xmlContents.containsKey(resourcePath);

	}

	/**
	 * return the configuration XML document
	 * 
	 * @param xmlName the configuration name
	 * @throws Exception
	 */
	public Document loadConfiguration() throws Exception {
		String root = super.getScriptPath().getResourcesPath();
		String resourcePath = this.getPath();

		if (!this.checkConfigurationExists()) {
			String err = "The resource " + super.getXmlName() + " not exists";
			LOGGER.error(err);
			throw new Exception(err);
		}

		Map<String, String> xmlContents = RESOURCE_CACHED.get(root);
		String xmlContent = xmlContents.get(resourcePath);
		return super.getDocumentByXmlString(xmlContent);
	}

	public boolean isNotExists() {
		return notExists;
	}

	public boolean isHasError() {
		return hasError;
	}

	public String getLastError() {
		return lastError;
	}

}
