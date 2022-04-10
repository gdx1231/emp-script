package com.gdxsoft.easyweb.h5Storage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 利用h5缓存css/js资源
 * 
 * @author admin
 *
 */
public class Resources {
	private static Logger LOGGER = LoggerFactory.getLogger(Resources.class);
	private static HashMap<String, Resources> INSTS = new HashMap<String, Resources>();

	/**
	 * 获取单一实例
	 * 
	 * @param cfgFilePath
	 * @return
	 */
	public static Resources instance(String cfgFilePath) {
		Resources rs;
		if (INSTS.containsKey(cfgFilePath)) {
			boolean isCfgFileChanged = UFileCheck.fileChanged(cfgFilePath, 15);
			if (isCfgFileChanged) {
				// 配置文件发生变化，重新创建
				rs = newInstance(cfgFilePath);
				LOGGER.info("文件变化，新实例" + cfgFilePath);
			} else {
				rs = INSTS.get(cfgFilePath);
				// 距离上次扫描时间 15s
				if (System.currentTimeMillis() - rs.getLastScan() > 15 * 1000) {
					// 扫描资源文件是否发生变化
					scanChanged(rs);
				}
			}
		} else {
			// 创建新实例
			rs = newInstance(cfgFilePath);
			LOGGER.info("新实例" + cfgFilePath);
		}
		return rs;
	}

	private synchronized static Resources newInstance(String cfgFilePath) {
		Resources rs;
		rs = new Resources(cfgFilePath);
		rs.initResources();
		INSTS.put(cfgFilePath, rs);
		return rs;
	}

	private synchronized static void scanChanged(Resources rs) {
		rs.scanFiles();
	}

	private String cfgFilePath_; // 资源配置文件物理路径
	private HashMap<String, Tag> resources_; // 所有资源
	private long lastScan_; // 上次扫描时间

	private HashMap<String, Resource> mapOfJarResources = new HashMap<>();
	private HashMap<String, Resource> mapOfHttpResources = new HashMap<>();

	/**
	 * 对象实例化
	 * 
	 * @param cfgFilePath
	 */
	private Resources(String cfgFilePath) {
		this.cfgFilePath_ = cfgFilePath;
	}

	/**
	 * 获取资源
	 * 
	 * @param q 查询条件
	 * @return
	 */
	public JSONObject getResources(JSONObject q) {
		Iterator<?> it = q.keys();

		JSONObject rst = new JSONObject();
		JSONArray arr = new JSONArray();
		rst.put("RES", arr);
		rst.put("RST", true);
		while (it.hasNext()) {
			String key = it.next().toString();
			JSONObject tag = q.getJSONObject(key);
			Tag oTag = this.resources_.get(key);

			if (oTag == null) {
				// 不存在的资源
				continue;
			}
			Iterator<?> itGroup = tag.keys();
			while (itGroup.hasNext()) {
				String keyGroup = itGroup.next().toString();
				JSONObject group = tag.getJSONObject(keyGroup);
				Group oGroup = oTag.getGroup(keyGroup);

				for (String id : oGroup.getResources().keySet()) {
					Resource r = oGroup.getResources().get(id);

					// 返回的资源
					JSONObject jsonRes = new JSONObject();
					jsonRes.put("id", r.getId());
					jsonRes.put("path", r.getPath());
					jsonRes.put("hash", r.getHash());
					jsonRes.put("index", r.getIndex());

					jsonRes.put("tag", key); // css or js
					jsonRes.put("group", keyGroup); // 分组名称
					jsonRes.put("group_loadBy", oGroup.getLoadBy()); // 加载方式

					boolean isMatch = false;
					if (group.has(r.getId())) {
						String refHash = group.getJSONObject(id).optString("hash");
						if (refHash != null && r.getHash().equals(refHash)) {
							isMatch = true;
						}
					}

					if (!isMatch) {
						jsonRes.put("content", r.getContent());
					}
					arr.put(jsonRes);
				}
			}
		}

		return rst;
	}

	/**
	 * 扫描文件是否变化（根据文件的修改日期和文件大小 ）
	 */
	public void scanFiles() {
		List<Resource> changed = new ArrayList<Resource>();
		for (String tagKey : resources_.keySet()) {
			Tag tag = this.resources_.get(tagKey);
			for (String groupKey : tag.getGroups().keySet()) {
				Group group = tag.getGroups().get(groupKey);
				for (String id : group.getResources().keySet()) {
					Resource r = group.getResources().get(id);
					Resource r1 = this.scanFile(r);
					if (r1 != null) { // 文件有变化
						r1.setIndex(r.getIndex()); // 排序不变
						changed.add(r1);
					}
				}
			}
		}
		if (changed.size() > 0) {
			this.commitChanged(changed);
		}
		// 设置最后一次检查时间
		this.lastScan_ = System.currentTimeMillis();
	}

	private void commitChanged(List<Resource> changed) {
		for (int i = 0; i < changed.size(); i++) {
			Resource r1 = changed.get(i);
			r1.getGroup().addResource(r1);
		}
	}

	/**
	 * 检查文件变化
	 * 
	 * @param r
	 */
	public Resource scanFile(Resource r) {
		boolean isHttp = false;
		if (r.isJarResource()) {
			if (!mapOfJarResources.containsKey(r.getId())) {
				// notify the client delete this resource
				r.setHash("DELETED");
				r.setContent("/* the resource " + r.getPath() + " has been deleted */");
				LOGGER.info(r.getContent());
				return r;
			}

			if (r.getHash().equals(mapOfJarResources.get(r.getId()).getHash())) {
				return null;
			}

		} else if (r.getAllPath().startsWith("http:") || r.getAllPath().startsWith("https:")) {
			if (!mapOfHttpResources.containsKey(r.getId())) {
				// notify the client delete this resource
				r.setHash("DELETED");
				r.setContent("/* the resource " + r.getAllPath() + " has been deleted */");
				LOGGER.info(r.getContent());
				return r;
			}
			if (r.getHash().equals(mapOfHttpResources.get(r.getId()).getHash())) {
				return null;
			}
		} else {
			File f = new File(r.getAllPath());
			if (!f.exists()) {
				// notify the client delete this resource
				r.setHash("DELETED");
				r.setContent("/* the file has been deleted. " + r.getPath() + " */");
				LOGGER.info(r.getContent());
				return r;
			}

			String h = this.getFileHash(f);
			if (r.getHash().equals(h)) {
				return null;
			}
			LOGGER.info("CHANGED: " + r.getAllPath());
		}
		// 克隆一个对象
		Resource r1 = new Resource();
		r1.setAllPath(r.getAllPath());
		r1.setGroup(r.getGroup());
		r1.setId(r.getId());
		r1.setPath(r.getPath());
		r1.setSource(r.getSource());
		r1.setJarResource(r.isJarResource());

		if (r1.isJarResource()) {
			Resource ref = mapOfJarResources.get(r.getId());
			r1.setContent(ref.getContent());
			r1.setHash(ref.getHash());
		} else if (isHttp) {
			Resource ref = mapOfHttpResources.get(r.getId());
			r1.setContent(ref.getContent());
			r1.setHash(ref.getHash());
		} else {
			this.loadResourceContent(r1);
		}
		return r1;
	}

	private String readFileText(String filePath) {

		String cnt;
		try {
			cnt = UFile.readFileText(filePath);
		} catch (Exception err) {
			cnt = "/* ERROR: " + err.getMessage() + " */";
			LOGGER.info("ERROR " + err);
		}

		return cnt;
	}

	/**
	 * 初始化资源
	 */
	private void initResources() {
		resources_ = new HashMap<>();
		mapOfJarResources = new HashMap<>();

		Document doc;
		try {
			doc = UXml.retDocument(this.cfgFilePath_);
		} catch (Exception err) {
			return;
		}
		NodeList nlGroup = doc.getElementsByTagName("group");

		int index = 0;
		for (int i = 0; i < nlGroup.getLength(); i++) {
			Element eleGroup = (Element) nlGroup.item(i);
			String root = eleGroup.getAttribute("root");
			String name = eleGroup.getAttribute("name");
			String loadBy = eleGroup.getAttribute("loadBy");
			//String classLoader = eleGroup.getAttribute("classLoader");// 读取资源所用的class全名
			// css or js
			String tag = eleGroup.getParentNode().getNodeName();

			Tag jsonTag;
			if (resources_.containsKey(tag)) {
				jsonTag = resources_.get(tag);
			} else {
				jsonTag = new Tag();
				jsonTag.setName(tag);
				resources_.put(tag, jsonTag);
			}

			// 分组
			Group jsonGroup = jsonTag.getGroup(name);
			if (jsonGroup == null) {
				jsonGroup = new Group();
				jsonGroup.setName(name);
				jsonGroup.setLoadBy(loadBy);
				jsonTag.addGroup(jsonGroup);
			}

			// 具体资源
			NodeList nls = eleGroup.getElementsByTagName("r");
			for (int m = 0; m < nls.getLength(); m++) {
				Element ele = (Element) nls.item(m);
				Resource r = this.loadContent(root, ele);
				// 总顺序号
				r.setIndex(index);
				index++;
				jsonGroup.addResource(r);
			}
		}

		this.lastScan_ = System.currentTimeMillis();
	}

	private void loadResourceContent(Resource r) {
		String cnt;
		if (r.isJarResource()) {
			String pathResource = r.getAllPath();
			if(pathResource.startsWith("/")) {
				pathResource = pathResource.substring(1);
			}
			/*
			 * Class<?> cls = null; try { cls = Class.forName(r.getClassLoader()); } catch
			 * (ClassNotFoundException e1) { LOGGER.error("The classLoader {}, {} error ",
			 * r.getClassLoader(), pathResource); }
			 */

			//URL url = cls.getClassLoader().getResource(pathResource);
			URL url = this.getClass().getClassLoader().getResource(pathResource);
			if (url == null) {
				cnt = "The resource not exists. " + pathResource;
				LOGGER.error("The resource not exists. {},{}", r.getId(), r.getPath());
			} else {
				LOGGER.info("fetch {} from {}", r.getId(), url);
				try {
					cnt = IOUtils.toString(url, StandardCharsets.UTF_8);
				} catch (IOException e) {
					cnt = "/* Invalid read the resource, " + pathResource + e.getMessage() + " */";
					LOGGER.error(cnt);
				}
				r.setContent(cnt);
			}
			r.setHash(Utils.md5(cnt));
			// jar 文件中的资源不会变化，一次性读取到缓存中
			mapOfJarResources.put(r.getId(), r);
		} else {
			String allPath = r.getAllPath();
			LOGGER.info("fetch {} from {}", r.getId(), allPath);
			if (allPath.startsWith("http:") || allPath.startsWith("https:")) {
				UNet net = new UNet();
				net.setIsShowLog(false);
				cnt = net.doGet(allPath);
				if (net.getLastStatusCode() != 200 && net.getLastStatusCode() != 304) {
					LOGGER.error("{}, code={}", r.getAllPath(), net.getLastStatusCode());
				} else if (net.getLastStatusCode() == 200) {
					r.setContent(cnt);
					String etag = net.getResponseHeaders().get("etag");
					r.setHash(etag != null ? etag : cnt.hashCode() + "");
					// http资源无变化
					mapOfHttpResources.put(r.getId(), r);
				}
			} else {
				File f1 = new File(allPath);
				if (f1.exists()) {
					cnt = this.readFileText(allPath);
				} else {
					cnt = "/* File not exists " + allPath + " */";
					LOGGER.error(cnt);
				}
				r.setHash(this.getFileHash(f1));
				r.setContent(cnt);
			}
		}

		// 替换字体下载的绝对路径
		if (r.getId().indexOf("fontawesome") >= 0) {
			RequestValue rv = new RequestValue();
			// ewa_conf.xml 自定义的 静态文件前缀
			String rvEwaStylePath = rv.s("RV_EWA_STYLE_PATH");
			if (StringUtils.isBlank(rvEwaStylePath)) {
				rvEwaStylePath = "/EmpScriptV2";
			}
			cnt = cnt.replace("url('../", "url('" + rvEwaStylePath + "/third-party/font-awesome/font-awesome-4.7.0/");
			r.setContent(cnt);
		}
	}

	/**
	 * 加载每个资源的信息
	 * 
	 * @param root
	 * @param ele
	 * @return
	 */
	private Resource loadContent(String root,   Element ele) {
		String path = ele.getTextContent().trim();
		String id = ele.getAttribute("id");

		Resource r = new Resource();
		r.setId(id);
		if (root.startsWith("resource:")) {
			r.setJarResource(true);
			// r.setClassLoader(classLoader);
			String pathResource = root.substring("resource:".length()) + "/" + path;
			pathResource = pathResource.replace("\\", "/").replace("//", "/").replace("//", "/");
			r.setAllPath(pathResource);

			this.loadResourceContent(r);
		} else {
			r.setJarResource(false);

			String allPath = root + path;
			r.setAllPath(allPath);
			this.loadResourceContent(r);
		}

		return r;
	}

	/**
	 * 获取文件的hash, 根据文件的修改日期和文件大小
	 * 
	 * @param f
	 * @return
	 */
	public String getFileHash(File f) {
		String s = f.lastModified() + "," + f.length();
		return s;
	}

	/**
	 * 上次扫描时间
	 * 
	 * @return
	 */
	public long getLastScan() {
		return lastScan_;
	}

	/**
	 * 设置上次扫描时间
	 * 
	 * @param lastScan
	 */
	public void setLastScan(long lastScan) {
		this.lastScan_ = lastScan;
	}
}
