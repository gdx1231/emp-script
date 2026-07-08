package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.gdxsoft.easyweb.script.restful.RestfulResult;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfRestfuls {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfRestfuls.class);
	private static ConfRestfuls INST = null;

	private static long PROP_TIME = 0;

	private static Map<String, Map<String, ConfRestful>> CONFS;

	/**
	 * ewa_restful_catalog 表中 p0..pN 列的最大深度。当前部署的 schema 中实际可用列数为 10（即 p0..p9）。
	 * 请求路径段数超过此深度时，无法用 schema 完整约束；具体决策由
	 * {@link #getJdbcRestfulCatalog(String, RestfulResult)} 处理。
	 */
	private static final int SCHEMA_MAX_DEPTH = 10;

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
				// C1: 用 LinkedHashMap 保留 restfulPath 的声明顺序，便于 getConfRestfulFromEwaConf 按声明优先级匹配
				map = new LinkedHashMap<String, ConfRestful>();
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

		// schema 列数上限：ewa_restful_catalog 已部署的 schema 中 p0..p(SCHEMA_MAX_DEPTH-1) 实际可用列数。
		// 超过此深度的请求路径无法用 schema 完整约束 p10+ 列；这种情况下直接跳过模糊匹配，
		// 让精确匹配 SQL（cat_path_full 在本方法第一段已执行）单独判定。
		// 调用方在没找到时返回 404，符合 HTTP 语义。
		int schemaMaxDepth = SCHEMA_MAX_DEPTH;
		if (requestPathsDepth.length > schemaMaxDepth) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ewa_restful_catalog where cat_status='USED'");
		for (int i = 0; i < requestPathsDepth.length; i++) {
			sb.append("\n and (p" + i + " = @p" + i + " or (p" + i + " like '{%' and p" + i + " like '%}'))");
			rv1.addOrUpdateValue("p" + i, requestPathsDepth[i]);
		}
		// 路径段数小于 schema 上限时，对未覆盖的更高段位追加"必须为 null"约束，
		// 防止"短路径"被"长路径"的目录偶然匹配（与历史行为保持一致）。
		for (int i = requestPathsDepth.length; i < schemaMaxDepth; i++) {
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

		// C7: 模糊匹配不再在遇到第一个 method-miss 时立即返回 501，而是收集所有路径匹配的候选，
		// 然后按优先级（路径段数越多越具体，声明靠前越优先）挑选最合适的，实现 method 支持的命中。
		// 结构：score 越大越优先；(score, restfulPath, paths[])
		List<Object[]> candidates = new ArrayList<>();
		for (String restfulPath : map.keySet()) {
			String[] paths = restfulPath.split("/");
			if (!this.findMapMethod(requestPathsDepth, paths)) {
				continue;
			}
			candidates.add(new Object[] { this.scorePathMatch(paths), restfulPath, paths });
		}
		// 按 score 降序（paths.length * 1000 - 字面量段数；高 score = 路径越具体且声明靠前）
		candidates.sort((a, b) -> Integer.compare((Integer) b[0], (Integer) a[0]));

		for (Object[] cand : candidates) {
			String restfulPath = (String) cand[1];
			String[] paths = (String[]) cand[2];
			Map<String, ConfRestful> mapMethod = map.get(restfulPath);
			if (mapMethod.containsKey(httpMethod)) {
				// path="chatRooms/{cht_rom_id}" —— 回填路径参数到 rv
				this.addPathParametersToRv(requestPathsDepth, paths, rv);
				return mapMethod.get(httpMethod);
			}
		}
		if (!candidates.isEmpty()) {
			// 路径全部匹配但都缺 method，返回 501
			result.setMessage("not implemented");
			result.setHttpStatusCode(501);
			return null;
		}
		result.setMessage("not found");
		result.setHttpStatusCode(404);
		return null;
	}

	/**
	 * 路径匹配评分。分数越高越优先： - 路径段总数 × 1000（段数越多越具体） - 加上字面量段数（同等段数下，字面量越多越具体）
	 */
	private int scorePathMatch(String[] paths) {
		int literalCount = 0;
		for (String p : paths) {
			if (!this.pathIsParameter(p)) {
				literalCount++;
			}
		}
		return paths.length * 1000 + literalCount;
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
