/**
 * 
 */
package com.gdxsoft.easyweb.conf;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * @author Administrator
 * 
 */
public class ConnectionConfig {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectionConfig.class);
	private String _Name;
	private String _Type;
	private String _ConnectionString;
	private String _SchemaName;
	private MTableStr _Pool;
	private boolean hiddenInDefine; // 在数据库类映射中隐含此数据库连接

	public ConnectionConfig(Node node) {
		this.setObj(node);
	}

	private void setObj(Node node) {
		Element ele = (Element) node;
		Map<String, String> params = UXml.getElementAttributes(ele, true);

		this._Name = params.get("name") == null ? "" : params.get("name").toLowerCase();
		this._ConnectionString = params.get("connectionstring");
		this._Type = params.get("type");
		this._SchemaName = params.get("schemaname");

		// 2021-04-11 在数据库类映射中隐含此数据库连接
		this.hiddenInDefine = Utils.cvtBool(params.containsKey("hiddenindefine"));

		if (StringUtils.isBlank(_Name) || StringUtils.isBlank(_ConnectionString) || StringUtils.isBlank(_Type)
				|| StringUtils.isBlank(_SchemaName)) {
			LOGGER.warn("Invalid database cfg -> " + UXml.asXml(node));
			return;
		}

		// self defined database pool parameter
		if (ele.getElementsByTagName("pool").getLength() > 0) {
			this._Pool = new MTableStr();
			Element p = (Element) ele.getElementsByTagName("pool").item(0);
			for (int i = 0; i < p.getAttributes().getLength(); i++) {
				Node att = p.getAttributes().item(i);
				String name = att.getNodeName().trim();
				String value = att.getNodeValue().trim();
				if ("password".equalsIgnoreCase(name) && value.startsWith("file://")) {
					value = this.getPasswordFromFile(value);
				}
				_Pool.add(name, value);
			}
		}
	}

	/**
	 * Read password from file
	 * @param value starts with "file://"
	 * @return the password 
	 */
	private String getPasswordFromFile(String value) {
		if (!value.startsWith("file://")) {
			return value;
		}

		String filePath = value.substring(7);

		try {
			String content = UFile.readFileText(filePath);
			LOGGER.debug("Read db password from: {}", value);
			
			return content.trim();
		} catch (IOException e) {
			LOGGER.error("Read password fail from: {} {}", value, e.getLocalizedMessage());
			return e.getLocalizedMessage();
		}
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * 数据库类型，如MSSQL，ORACLE ...
	 * 
	 * @return the _Type
	 */
	public String getType() {
		return _Type;
	}

	/**
	 * @return the _ConnectionString
	 */
	public String getConnectionString() {
		return _ConnectionString;
	}

	/**
	 * @return the _SchemaName
	 */
	public String getSchemaName() {
		return _SchemaName;
	}

	public ConnectionConfig() {
		// create structure;
	}

	public void setName(String _Name) {
		this._Name = _Name;
	}

	public void setType(String _Type) {
		this._Type = _Type;
	}

	public void setConnectionString(String _ConnectionString) {
		this._ConnectionString = _ConnectionString;
	}

	public void setSchemaName(String _SchemaName) {
		this._SchemaName = _SchemaName;
	}

	/**
	 * @return the _Pool
	 */
	public MTableStr getPool() {
		return _Pool;
	}

	/**
	 * @param pool the _Pool to set
	 */
	public void setPool(MTableStr pool) {
		_Pool = pool;
	}

	public boolean isHiddenInDefine() {
		return hiddenInDefine;
	}

	public void setHiddenInDefine(boolean hiddenInDefine) {
		this.hiddenInDefine = hiddenInDefine;
	}

}