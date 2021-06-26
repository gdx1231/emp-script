package com.gdxsoft.easyweb.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.servlets.RestfulResult;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfRestfuls {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfRestfuls.class);
	private static ConfRestfuls INST = null;

	private static long PROP_TIME = 0;

	private static Map<String, Map<String, ConfRestful>> CONFS;

	public static ConfRestfuls getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createConfs();
		return INST;
	}

	private synchronized static ConfRestfuls createConfs() {

		ConfRestfuls sps = new ConfRestfuls();

		CONFS = new ConcurrentHashMap<>();

		if (UPath.getCfgXmlDoc() == null) {
			return sps;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();
		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("restfuls");
		// 没有配置
		if (nl.getLength() == 0) {
			return sps;
		}

		Element item = (Element) nl.item(0);
		String path = item.getAttribute("path");
		if (path == null) {
			return sps;
		}
		path = path.trim();
		sps.path = path;
		sps.xml = UXml.asXml(item);

		// Access-Control-Allow-Origin， CORS policy
		String cors = item.getAttribute("cors");
		sps.setCors(cors);

		if (path.toLowerCase().startsWith("jdbc:")) {
			// 配置在数据库中
			sps.setJdbc(true);
			sps.setDataSource(path.substring(5));
		} else {
			createConfs(item);
		}
		return sps;
	}

	private static void createConfs(Element parentItem) {
		String pathParent = parentItem.getAttribute("path").trim();
		int inc = 0;
		while (pathParent.endsWith("/")) {
			pathParent = pathParent.substring(0, pathParent.length() - 1);
			inc++;
			if (inc == 1000) { // 疯了？
				break;
			}
		}

		NodeList nl = parentItem.getElementsByTagName("restful");

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			createRestfulConf(pathParent, item);
		}

	}

	private static void createRestfulConf(String pathParent, Element item) {

		NodeList methods = item.getChildNodes();
		for (int i = 0; i < methods.getLength(); i++) {
			Node methodItem = methods.item(i);

			if (methodItem.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			ConfRestful restful = new ConfRestful();

			UObjectValue uo = new UObjectValue();
			uo.setObject(restful);
			uo.setAllValue(item);

			// full path
			String restfulPath = pathParent + "/" + restful.getPath().trim();
			int inc = 0;
			while (restfulPath.indexOf("//") >= 0) {
				restfulPath = restfulPath.replace("//", "/");
				inc++;
				if (inc == 1000) { // 疯了？
					break;
				}
			}
			inc = 0;
			while (restfulPath.endsWith("/")) {
				restfulPath = restfulPath.substring(0, restfulPath.length() - 1);
				inc++;
				if (inc == 1000) { // 疯了？
					break;
				}
			}

			restful.setRestfulPath(restfulPath);

			restful.setPathDirsDepth(restfulPath.split("/").length);

			uo.setAllValue((Element) methodItem);

			restful.setXml(UXml.asXml(methodItem));

			restful.setMethod(methodItem.getNodeName().toUpperCase());

			Map<String, ConfRestful> map;
			if (!CONFS.containsKey(restfulPath)) {
				map = new HashMap<String, ConfRestful>();
				CONFS.put(restfulPath, map);
			} else {
				map = CONFS.get(restfulPath);
			}

			// http method, PUT/GET/POST/PATCH/DELETE
			String httpMethod = restful.getMethod();
			if (map.containsKey(httpMethod)) {
				LOGGER.warn("The repeat being overwrited: {} with {}", map.get(httpMethod).getXml(), restful.getXml());
			} else {
				LOGGER.info("Add restful ->{}: {}", restful.getRestfulPath(), restful.getXml());
			}
			map.put(httpMethod, restful);
		}

	}

	/**
	 * 根据 path 和 method 获取restful配置
	 * 
	 * @param path       请求地址
	 * @param httpMethod HTTP method(get/put/post/delete/patch)
	 * @param rv         RequestValue
	 * @param result     记录结果的对象
	 * @return
	 * @throws Exception
	 */
	public ConfRestful getConfRestful(String path, String httpMethod, RequestValue rv, RestfulResult<Object> result) {
		try {
			if (this.isJdbc()) {
				return this.getConfRestfulFromJdbc(path, httpMethod, rv, result);
			} else {
				return this.getConfRestfulFromEwaConf(path, httpMethod, rv, result);
			}
		} catch (Exception e) {
			result.setCode(500);
			result.setSuccess(false);
			result.setHttpStatusCode(500);
			result.setMessage(e.getMessage());
			LOGGER.error("path: {}, method: {}, error:", path, httpMethod, e.getMessage());
			return null;
		}
	}

	/**
	 * 根据 path 和 method 获取restful配置
	 * 
	 * @param path       目录
	 * @param httpMethod HTTP method(get/put/post/delete/patch)
	 * @param rv         RequestValue
	 * @param result     记录结果的对象
	 * @return
	 * @throws Exception
	 */
	public ConfRestful getConfRestfulFromJdbc(String path, String httpMethod, RequestValue rv,
			RestfulResult<Object> result) throws Exception {

		DTRow catalog = this.getJdbcRestfulCatalog(path, result);
		if (catalog == null) {
			result.setMessage("The " + path + " not found");
			result.setHttpStatusCode(404);
			result.setSuccess(false);
			return null;
		}

		DTRow methodRow = this.getJdbcRestful(catalog.getCell("cat_uid").toString(), httpMethod);

		if (methodRow == null) {
			// 找不到对象的模式method
			result.setMessage("The " + path + "(" + httpMethod + ") not implemented");
			result.setHttpStatusCode(501);// Method is not implemented
			result.setSuccess(false);
			return null;
		}

		String[] requestPathsDepth = path.split("/");

		ConfRestful conf = new ConfRestful();
		conf.setPath(catalog.getCell("cat_path").toString());
		conf.setRestfulPath(catalog.getCell("cat_path_full").toString());

		conf.setMethod(httpMethod);
		conf.setItemName(methodRow.getCell("rs_itemname").toString());
		conf.setXmlName(methodRow.getCell("rs_xmlname").toString());
		conf.setParameters(methodRow.getCell("rs_parameters").toString());
		conf.setPathDirsDepth(requestPathsDepth.length);

		String[] paths = conf.getRestfulPath().split("/");

		this.addPathParametersToRv(requestPathsDepth, paths, rv);
		return conf;
	}

	private void addPathParametersToRv(String[] requestPathsDepth, String[] paths, RequestValue rv) {
		for (int i = 0; i < requestPathsDepth.length; i++) {
			String reqPath0 = requestPathsDepth[i];
			String path0 = paths[i];
			// 目录参数
			if (this.pathIsParameter(path0)) {
				// Change path parameter to rv parameter
				String rvName = this.getPathParameterName(path0);
				String rvValue = reqPath0;

				rv.addOrUpdateValue(rvName, rvValue);
			}
		}
	}

	private DTRow getJdbcRestfulCatalog(String path, RestfulResult<Object> result) throws Exception {
		String sql = "select * from ewa_restful_catalog where cat_path_full=@path and cat_status='USED'";
		RequestValue rv1 = new RequestValue();
		rv1.addOrUpdateValue("path", path);
		DTTable tb = DTTable.getJdbcTable(sql, this.getDataSource(), rv1);
		if (!tb.isOk()) {
			throw new Exception(tb.getErrorInfo());
		}

		if (tb.getCount() > 0) {
			return tb.getRow(0);
		}
		String[] requestPathsDepth = path.split("/");

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ewa_restful_catalog where cat_status='USED'");
		int index = 0;
		for (int i = 0; i < requestPathsDepth.length; i++) {
			sb.append("\n and (p" + i + " = @p" + i + " or (p" + i + " like '{%' and p" + i + " like '%}'))");
			rv1.addOrUpdateValue("p" + i, requestPathsDepth[i]);
			index++;
		}
		for (int i = index; i < 10; i++) {
			sb.append("\n and p" + i + " is null");
		}
		tb = DTTable.getJdbcTable(sb.toString(), this.getDataSource(), rv1);
		if (!tb.isOk()) {
			throw new Exception(tb.getErrorInfo());
		}
		if (tb.getCount() > 0) {
			return tb.getRow(0);
		}
		return null;
	}

	private DTRow getJdbcRestful(String catUid, String httpMethod) {
		RequestValue rv1 = new RequestValue();
		String sql = "select * from ewa_restful where cat_uid = @cat_uid and rs_method=@httpMethod";

		rv1.addOrUpdateValue("httpMethod", httpMethod);
		rv1.addOrUpdateValue("cat_uid", catUid);

		DTTable tb = DTTable.getJdbcTable(sql, this.getDataSource(), rv1);

		if (tb.getCount() == 0) {
			return null;
		}
		return tb.getRow(0);

	}

	/**
	 * 根据 path 和 method 获取restful配置
	 * 
	 * @param path       目录
	 * @param httpMethod HTTP method(get/put/post/delete/patch)
	 * @param rv         RequestValue
	 * @param result     记录结果的对象
	 * @return
	 */
	public ConfRestful getConfRestfulFromEwaConf(String path, String httpMethod, RequestValue rv,
			RestfulResult<Object> result) {
		Map<String, Map<String, ConfRestful>> map = CONFS;
		if (map.containsKey(path)) { // 完全匹配
			Map<String, ConfRestful> mapMethod = map.get(path);
			if (mapMethod.containsKey(httpMethod)) {
				return mapMethod.get(httpMethod);
			} else {
				// 找不到对象的模式method
				result.setMessage("not implemented");
				result.setHttpStatusCode(501);// Method is not implemented
				return null;
			}
		}
		String[] requestPathsDepth = path.split("/");

		for (String restfulPath : map.keySet()) {
			String[] paths = restfulPath.split("/");
			boolean isMatched = this.findMapMethod(requestPathsDepth, paths);
			if (!isMatched) {
				continue;
			}

			// GET/POST/PUT/DELETE/PATCH
			Map<String, ConfRestful> mapMethod = map.get(restfulPath);
			if (mapMethod.containsKey(httpMethod)) {
				// path="chatRooms/{cht_rom_id}"
				this.addPathParametersToRv(requestPathsDepth, paths, rv);

				return mapMethod.get(httpMethod);
			} else {
				result.setMessage("not implemented");
				result.setHttpStatusCode(501);// Method is not implemented
				return null;
			}
		}
		result.setMessage("not found");
		result.setHttpStatusCode(404);
		return null;
	}

	/**
	 * 匹配目录
	 * 
	 * @param requestPathsDepth
	 * @param paths
	 * @return
	 */
	private boolean findMapMethod(String[] requestPathsDepth, String[] paths) {
		if (paths.length != requestPathsDepth.length) {
			return false;
		}

		for (int i = 0; i < requestPathsDepth.length; i++) {
			String reqPath0 = requestPathsDepth[i];
			String path0 = paths[i];
			if (!reqPath0.equals(path0) && !this.pathIsParameter(path0)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 路径是否为参数，例如 {userId}
	 * 
	 * @param path0 路径
	 * @return 是/否
	 */
	private boolean pathIsParameter(String path0) {
		return path0.startsWith("{") && path0.endsWith("}");
	}

	/**
	 * 获取路径的参数名称，例如 {userId}返回 userId
	 * 
	 * @param path0 路径
	 * @return 路径的参数名称
	 */
	private String getPathParameterName(String path0) {
		String rvName = path0.substring(1, path0.length() - 1);
		return rvName;
	}

	private String path;
	private String xml;
	private boolean jdbc;
	private String dataSource;
	private String cors; // Access-Control-Allow-Origin

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public boolean isJdbc() {
		return jdbc;
	}

	public void setJdbc(boolean jdbc) {
		this.jdbc = jdbc;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Access-Control-Allow-Origin
	 * 
	 * @return
	 */
	public String getCors() {
		return cors;
	}

	/**
	 * Access-Control-Allow-Origin
	 * 
	 * @param cors
	 */
	public void setCors(String cors) {
		this.cors = cors;
	}

}
