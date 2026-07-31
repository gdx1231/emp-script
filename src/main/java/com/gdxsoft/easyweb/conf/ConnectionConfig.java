/**
 * 
 */
package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.ConfValueResolvers;
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
	private List<String> _Aliases; // 别名列表，用于多个配置使用同一个连接池

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

		// 解析 alias 子节点
		this._Aliases = new ArrayList<>();
		NodeList aliasNodes = ele.getElementsByTagName("alias");
		for (int i = 0; i < aliasNodes.getLength(); i++) {
			Element aliasEle = (Element) aliasNodes.item(i);
			String aliasName = aliasEle.getAttribute("name");
			if (StringUtils.isNotBlank(aliasName)) {
				_Aliases.add(aliasName.toLowerCase());
			}
		}

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
				if ("password".equalsIgnoreCase(name)) {
					value = ConfValueResolvers.resolve(value);
				}
				_Pool.add(name, value);
			}
			resolvePoolPaths(this._Pool);
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
		resolvePoolPaths(pool);
		_Pool = pool;
	}

	/**
	 * 解析 pool 中 url 的路径表达式，将快捷路径替换为绝对路径。<br>
	 * 支持的表达式：
	 * <ul>
	 *   <li><code>~</code> / <code>@home</code> — 用户主目录 ({@code user.home})</li>
	 *   <li><code>@temp</code> — 系统临时目录 ({@code java.io.tmpdir})</li>
	 *   <li><code>@cwd</code> / <code>@pwd</code> — 当前工作目录 ({@code user.dir})</li>
	 * </ul>
	 * 示例：{@code jdbc:sqlite:~/data/test.db} → {@code jdbc:sqlite:/home/user/data/test.db}
	 *
	 * @param pool 连接池参数
	 */
	private void resolvePoolPaths(MTableStr pool) {
		if (pool == null) {
			return;
		}
		String url = pool.get("url");
		if (url == null) {
			return;
		}
		String resolved = resolvePath(url);
		if (!resolved.equals(url)) {
			LOGGER.info("Resolved pool url: {} -> {}", url, resolved);
			pool.put("url", resolved);
		}
	}

	/**
	 * 解析路径表达式，将 ~、@temp、@cwd 替换为对应的绝对路径。
	 *
	 * @param path 原始路径
	 * @return 替换后的路径
	 */
	public static String resolvePath(String path) {
		if (path == null) {
			return null;
		}
		String result = path;
		// @temp → 系统临时目录
		if (result.contains("@temp")) {
			result = result.replace("@temp", System.getProperty("java.io.tmpdir"));
		}
		// @cwd / @pwd → 当前工作目录
		if (result.contains("@cwd")) {
			result = result.replace("@cwd", System.getProperty("user.dir"));
		}
		if (result.contains("@pwd")) {
			result = result.replace("@pwd", System.getProperty("user.dir"));
		}
		// ~ / @home → 用户主目录
		if (result.contains("~")) {
			result = result.replace("~", System.getProperty("user.home"));
		}
		if (result.contains("@home")) {
			result = result.replace("@home", System.getProperty("user.home"));
		}
		return result;
	}

	public boolean isHiddenInDefine() {
		return hiddenInDefine;
	}

	public void setHiddenInDefine(boolean hiddenInDefine) {
		this.hiddenInDefine = hiddenInDefine;
	}

	/**
	 * @return the _Aliases
	 */
	public List<String> getAliases() {
		return _Aliases;
	}

}