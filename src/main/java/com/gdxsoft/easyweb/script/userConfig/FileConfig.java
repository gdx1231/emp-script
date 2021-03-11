package com.gdxsoft.easyweb.script.userConfig;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.utils.UFile;

public class FileConfig extends ConfigBase implements IConfig, Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6001786273284276308L;
	private static Logger LOGER = LoggerFactory.getLogger(FileConfig.class);

	public FileConfig() {
		super();
	}
	
	public FileConfig(ConfScriptPath scriptPath, String xmlName, String itemName) {
		super(scriptPath, xmlName, itemName);
	}

	private File getXmlFile() {
		String root = super.getScriptPath().getPath();
		File filePath = new File(root + super.getFixedXmlName());
		return filePath;
	}

	@Override
	public String getPath() {
		return this.getXmlFile().getAbsolutePath();
	}

	@Override
	public Document loadConfiguration() throws Exception {
		File f = this.getXmlFile();
		if (!f.exists()) {
			String err = "The configuration " + super.getXmlName() + " not exists";
			LOGER.error(err);
			throw new Exception(err);
		}
		String xmlDocument = UFile.readFileText(this.getPath());
		return super.getDocumentByXmlString(xmlDocument);
	}

	@Override
	public boolean checkConfigurationExists() {
		return this.getXmlFile().exists();
	}

}
