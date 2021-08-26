package com.gdxsoft.easyweb.define;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;

public class SyncRemote {
	private static Logger LOGGER = LoggerFactory.getLogger(SyncRemote.class);
	private String _CfgKey; // 本配置的Key "CFG_" + url.hashCode()
	private UAes AES;
	private JSONObject CFGS;
	private HashMap<String, Boolean> _Filters;
	private String _FilterString;
	private String _Root;
	private JSONObject _Json;
	private String _JsonName;
	private JSONObject _HisJson;

	private JSONObject _CurCfg;

	public JSONObject getCurCfg() {
		return _CurCfg;
	}

	public JSONObject getCfgs() {
		return CFGS;
	}

	private UNet getNet() {
		UNet net = new UNet();
		net.setIsShowLog(false);
		net.setEncode("utf-8");

		return net;
	}

	/**
	 * 解密内容
	 * 
	 * @param encodeString
	 * @return
	 * @throws Exception
	 */
	public String decode(String encodeString) throws Exception {
		return AES.decrypt(encodeString);
	}

	/**
	 * 加密内容
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public String encode(String str) throws Exception {
		return AES.encrypt(str);
	}

	/**
	 * 根据配置文件ID获取配置项
	 *
	 * @param id
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JSONException
	 */
	public void initById(String id) throws ParserConfigurationException, SAXException, IOException, JSONException {

		JSONObject cfg = CFGS.getJSONObject(id);
		String root = cfg.getString("source");
		String filter = cfg.getString("filter");
		init(filter, root);

		_CurCfg = CFGS.getJSONObject(id);

	}

	/**
	 * 初始化，根据过滤条件和根目录
	 *
	 * @param filters 过滤条件（js or js,html）
	 * @param root    根目录
	 */
	public void init(String filters, String root) {
		_Filters = new HashMap<String, Boolean>();
		_FilterString = filters;
		String[] exps = filters.split(",");

		for (int i = 0; i < exps.length; i++) {
			String exp = exps[i].trim().toUpperCase();
			if (!exp.startsWith(".")) {
				exp = "." + exp;
			}
			if (exp.length() > 0) {
				this._Filters.put(exp, true);
			}
		}
		_Filters.put(".BAK", false);
		File ff = new File(root);
		this._Root = ff.getAbsolutePath();
		_Json = new JSONObject();
		_JsonName = _FilterString.hashCode() + "gdx" + root.hashCode() + ".json";

		// 历史扫描结果
		_HisJson = new JSONObject();
		String path = UPath.getCachedPath() + "/" + this._JsonName;
		ff = new File(path);
		if (ff.exists()) {
			LOGGER.info("读取缓存文件");
			LOGGER.info(ff.getAbsoluteFile().getAbsolutePath());
			try {
				String cnt = UFile.readFileText(path);
				_HisJson = new JSONObject(cnt);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 和历史记录进行比对, 如果一致返回历史的JSON，就不用重新获取md5
	 *
	 * @param f    文件
	 * @param hash 文件名+长度+修改日期 hash code
	 * @return
	 * @throws JSONException
	 */
	private JSONObject checkSame(File f, String hash) throws JSONException {
		String n = f.getAbsolutePath().replace(_Root, "");
		n = n.replace("\\", "/"); // linux格式
		if (this._HisJson.has(n)) {
			JSONObject o = this._HisJson.getJSONObject(n);
			if (o.getString("H").equals(hash)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * 本地和远程执行，根据filter获取目录下的文件的JSON
	 *
	 * @param path
	 * @throws JSONException
	 */
	public void getDir(String path) throws JSONException {
		if (path == null) {
			path = this._Root;
		}

		File f1 = new File(path);
		String name = f1.getName().toUpperCase();
		if (name.indexOf("_") == 0 || name.indexOf("TMP_") == 0 || name.indexOf("TEMP_") == 0
				|| name.indexOf("CVS") == 0 || name.equalsIgnoreCase(".DS_Store") || name.equalsIgnoreCase(".git")) {
			return;
		}
		File[] ff = f1.listFiles();
		if (ff == null) {
			return;
		}
		List<File> dirs = new ArrayList<File>();

		// 为了避免 OOM 限制最大文件尺寸
		int limitedMaxSize = 50 * 1024 * 1024; // 50M
		for (int i = 0; i < ff.length; i++) {
			File file = ff[i];
			// 真实的文件（非ln -s)
			File realFile = getRealFile(file);
			if (!realFile.exists()) {
				LOGGER.info("NOT EXISTS :" + file.getAbsolutePath());
				continue;
			}
			if (realFile.isDirectory()) {
				if (realFile.getName().equals("node_modules")) {
					// 取消 node 模块 2020-04-14
					continue;
				}
				dirs.add(file);
				continue;
			}
			if (realFile.getName().startsWith("application.") // SpringBoot
					|| realFile.getName().equals("ewa_conf.xml") // ewa
			) {
				LOGGER.info("Skip file: " + realFile.getAbsolutePath());
				continue;
			}

			if (!checkExts(realFile)) {
				continue;
			}

			if (realFile.length() > limitedMaxSize) {
				LOGGER.info("Skip BIG file: " + realFile.getAbsolutePath());
				continue;
			}

			String hash = getFileCacheHash(realFile);
			// 和历史记录进行比对
			JSONObject sameObj = checkSame(file, hash);
			if (sameObj != null) {
				addToJson(file, sameObj);
			} else {
				// 生成新的md5
				String md5source = UFile.createMd5(realFile);
				addToJson(file, md5source, hash);
				LOGGER.info(md5source + ":" + realFile.getAbsolutePath());
			}
			// System.out.println(ff[i].getAbsolutePath());
		}

		for (int i = 0; i < dirs.size(); i++) {
			// 真实的文件（非ln -s)
			File file = dirs.get(i);
			this.getDir(file.getAbsolutePath());
		}
	}

	/**
	 * remote执行，检查本地文件json和远程文件json的差异
	 *
	 * @param fromJson
	 * @return
	 * @throws JSONException
	 */
	public JSONObject compareFiles(String fromJson) throws JSONException {
		JSONObject diffs = new JSONObject();
		JSONObject fJson = new JSONObject(fromJson);
		Iterator<?> keys = fJson.keys();
		while (keys.hasNext()) {
			String n = keys.next().toString();
			JSONObject jsonFile = new JSONObject();
			if (this._Json.has(n)) {
				JSONObject remotef = fJson.getJSONObject(n);
				JSONObject thisf = _Json.getJSONObject(n);
				if (checkRemoteSame(remotef, thisf)) {
					continue;
				}
				String remoteMd5 = remotef.getString("M");
				String thisMd5 = thisf.getString("M");

				jsonFile.put("MD5", thisMd5);
				jsonFile.put("remoteMd5", remoteMd5);
			}
			File f = new File(this._Root + "/" + n);

			jsonFile.put("LEN", f.length());
			jsonFile.put("DATE", f.lastModified());
			jsonFile.put("PATH", f.getAbsolutePath());
			// jsonFile.put("REMOTE_MD5", value)
			diffs.put(n, jsonFile);
		}
		return diffs;
	}

	/**
	 * 检查远程与本地的文件的md5值是否一致
	 *
	 * @param remotef
	 * @param thisf
	 * @return
	 * @throws JSONException
	 */
	private boolean checkRemoteSame(JSONObject remotef, JSONObject thisf) throws JSONException {
		String remoteMd5 = remotef.getString("M");
		String thisMd5 = thisf.getString("M");
		if (remoteMd5.equals(thisMd5)) {
			return true;
		}
		return false;
	}

	/**
	 * 将一致的历史json放到记录中
	 *
	 * @param f
	 * @param o
	 * @throws JSONException
	 */
	private void addToJson(File f, JSONObject o) throws JSONException {
		String n = f.getAbsolutePath().replace(_Root, "").replace("\\", "/");
		this._Json.put(n, o);
	}

	/**
	 * 生成新的文件记录，路径，md5，hash
	 *
	 * @param f
	 * @param md5
	 * @param hash
	 * @throws JSONException
	 */
	private void addToJson(File f, String md5, String hash) throws JSONException {
		JSONObject json = new JSONObject();
		String n = f.getAbsolutePath().replace(_Root, "").replace("\\", "/");
		json.put("M", md5);
		json.put("H", hash);
		this._Json.put(n, json);
	}

	/**
	 * 检查文件的扩展名是否符合，大小写无关
	 *
	 * @param source
	 * @return
	 */
	private boolean checkExts(File source) {
		if (this._Filters.size() == 0 || this._Filters.containsKey(".*")) {
			return true;
		}
		String name = source.getName();
		int loc = name.lastIndexOf(".");
		if (loc >= 0) {
			String ext = name.substring(loc).toUpperCase();
			if (this._Filters.containsKey(ext)) {
				return this._Filters.get(ext);
			}
		}
		return false;
	}

	/**
	 * 保存扫描结果
	 *
	 * @throws IOException
	 */
	public void saveJson() throws IOException {

		String path = UPath.getCachedPath() + "/" + this._JsonName;
		LOGGER.info("保存缓存文件");
		LOGGER.info(path);
		UFile.createNewTextFile(path, this._Json.toString());
	}

	/**
	 * 获取文件表达式的hash值 路径+长度+文件修改日期
	 *
	 * @param f
	 * @return
	 */
	private String getFileCacheHash(File f) {
		String txt = f.getAbsolutePath() + "_" + f.length() + "_" + f.lastModified();
		return txt.hashCode() + "";
	}

	public JSONObject getJson() {
		return this._Json;
	}

	/**
	 * 将本地的JSON提交到远程
	 * 
	 * @throws Exception
	 */
	public String postToRemote() throws Exception {

		JSONObject paras = new JSONObject();
		String id = _CurCfg.optString("id");
		paras.put("remote_id", id);

		// System.out.println(this._Json.toString());

		paras.put("fjson", this._Json);

		// 加密内容
		String contentEncode = AES.encrypt(paras.toString());

		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("method", "recv_json"); // 模式
		vals.put("GDX", contentEncode); // 内容
		vals.put("cfg_key", this._CfgKey);
		String u = CFGS.getString("REMOTE_URL");

		UNet net = getNet();

		LOGGER.info("postToRemote");
		LOGGER.info(u);

		String rstEncode = net.doPost(u, vals);
		// 机密远程传回的参数
		String rst = decode(rstEncode);
		// System.out.println(rst);

		try {
			JSONObject arr = new JSONObject(rst);
			Iterator<?> keys = arr.keys();
			while (keys.hasNext()) {
				String key = keys.next().toString();
				// 本地文件
				File f = new File(this._Root + "/" + key);
				// 本地文件长度
				arr.getJSONObject(key).put("LEN0", f.length());
				// 本地文件修改日期
				arr.getJSONObject(key).put("DATE0", f.lastModified());
			}
			return arr.toString();
		} catch (Exception err) {
			LOGGER.error(rst);

			return err.getMessage();
		}
	}

	/**
	 * local执行，提交文件到远程
	 *
	 * @param id
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public String sendFile(String id, String name) throws Exception {
		JSONObject cfgs = this.CFGS;
		String u = cfgs.getString("REMOTE_URL");

		// UFile.createNewTextFile("/Users/admin/aa.txt", cnt);

		// 加密内容
		String contentEncode = this.encodeFile(id, name);

		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("method", "send_file"); // 模式
		vals.put("GDX", contentEncode); // 内容
		vals.put("cfg_key", this._CfgKey);

		LOGGER.info(vals.get("method"));
		LOGGER.info(vals.get("cfg_key"));

		UNet net = getNet();
		String rstEncode = net.doPost(u, vals);
		// System.out.println(rstEncode);
		// 机密远程传回的参数
		String rst = decode(rstEncode);

		return rst.trim();
	}

	public String encodeFile(String id, String name) throws Exception {
		JSONObject cfgs = this.CFGS;
		JSONObject cfg = cfgs.getJSONObject(id);
		String name1 = cfg.getString("source") + "/" + name;
		File file = new File(name1);

		// 获取真实文件的路径
		File realFile = getRealFile(file);
		String cnt = UFile.readFileBase64(realFile.getAbsolutePath());

		JSONObject paras = new JSONObject();
		paras.put("remote_id", id);
		paras.put("file", cnt);
		paras.put("name", name);

		// UFile.createNewTextFile("/Users/admin/aa.txt", cnt);

		// 加密内容
		String contentEncode = this.encode(paras.toString());

		return contentEncode;
	}

	public byte[] decodeFile(String base64Str) throws IOException {
		byte[] buf = UConvert.FromBase64String(base64Str);
		return buf;
	}

	/**
	 * remote 执行，获取文件BASE64并保存
	 *
	 * @param root
	 * @param name
	 * @param cnt
	 * @return
	 * @throws JSONException
	 */
	public String recvFile(String root, String name, String cnt) throws JSONException {
		String name1 = root + "/" + name;
		byte[] buf;
		JSONObject obj = new JSONObject();
		try {
			buf = decodeFile(cnt);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			obj.put("RST", false);
			obj.put("ERR", e.toString());

			return obj.toString();
		}
		try {
			UFile.createBinaryFile(name1, buf, true);
			obj.put("RST", true);
			obj.put("f", name1);
			obj.put("f1", name);
			return obj.toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			obj.put("RST", false);
			obj.put("ERR", e.toString());

			return obj.toString();
		}
	}

	/**
	 * 远程发送文件
	 * 
	 * @param id   本地配置索引
	 * @param root 远程根目录
	 * @param name 远程文件名称
	 * @return
	 * @throws JSONException
	 */
	public String removeSendFile(String id, String root, String name) throws JSONException {
		String name1 = root + "/" + name;
		JSONObject obj = new JSONObject();

		try {
			String cnt = UFile.readFileBase64(name1);
			obj.put("RST", true);
			obj.put("file", cnt);
			obj.put("f1", name);
			obj.put("id", id);
			return obj.toString();
		} catch (Exception e) {
			obj.put("RST", false);
			obj.put("ERR", e.toString());

			return obj.toString();
		}
	}

	/**
	 * 本地请求远程文件
	 * 
	 * @param id
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public String localRequstRemoveFile(String id, String name) throws Exception {
		JSONObject paras = new JSONObject();
		paras.put("remote_id", id);
		paras.put("name", name);
		// System.out.println(this._Json.toString());

		JSONObject cfgs = this.CFGS;
		JSONObject cfg = cfgs.getJSONObject(id);

		String u = cfgs.getString("REMOTE_URL");
		UNet net = getNet();
		HashMap<String, String> vals = new HashMap<String, String>();
		vals.put("method", "remote_send_file");
		vals.put("cfg_key", this._CfgKey);

		// 加密内容
		String contentEncode = AES.encrypt(paras.toString());
		vals.put("GDX", contentEncode); // 内容

		// 提交到远程获取文件
		String rstEncode = net.doPost(u, vals);
		// 机密远程传回的参数
		String rst = decode(rstEncode);

		JSONObject rstJson = new JSONObject(rst);
		String cnt = rstJson.getString("file"); // base64

		String root = cfg.getString("source");
		return localRecvFile(root, name, cnt);

	}

	/**
	 * 本地执行，获取文件BASE64并保存
	 *
	 * @param name
	 * @param cnt
	 * @return
	 * @throws JSONException
	 */
	private String localRecvFile(String root, String name, String cnt) throws JSONException {
		String name1 = root + "/" + name;
		byte[] buf;
		JSONObject obj = new JSONObject();
		try {
			buf = UConvert.FromBase64String(cnt);
		} catch (IOException e) {
			obj.put("RST", false);
			obj.put("ERR", e.toString());

			return obj.toString();
		}
		try {
			UFile.createBinaryFile(name1, buf, true);
			obj.put("RST", true);
			obj.put("f", name1);
			obj.put("f1", name);
			return obj.toString();
		} catch (Exception e) {
			obj.put("RST", false);
			obj.put("ERR", e.toString());

			return obj.toString();
		}
	}

	public JSONObject loadCfgs(Element syncs) {

		JSONObject objs = new JSONObject();
		objs.put("REMOTE_URL", syncs.getAttribute("url"));
		objs.put("REMOTE_CODE", syncs.getAttribute("code"));
		objs.put("REMOTE_DES", syncs.getAttribute("des"));
		NodeList nl = syncs.getElementsByTagName("remote_sync");

		/*
		 * <remote_sync name="用户 XML配置文件" id="1" source="/Volumes/work_xml/user.config.xml"
		 * target="d:/java/erp2014/user.config.xml" filter="xml" />
		 */
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String id = ele.getAttribute("id");
			JSONObject obj = new JSONObject();
			obj.put("id", id);
			obj.put("name", ele.getAttribute("name"));
			obj.put("source", ele.getAttribute("source"));
			obj.put("target", ele.getAttribute("target"));
			obj.put("filter", ele.getAttribute("filter"));
			objs.put(id, obj);
		}
		CFGS = objs;
		this.initAes();
		return CFGS;
	}

	/**
	 * 初始化同步加密解密模块
	 */
	private void initAes() {
		try {
			String code = CFGS.getString("REMOTE_CODE");
			AES = new UAes();
			// Compatible with old version
			AES.setCipherName(UAes.AES_128_CBC);
			AES.setPaddingMethod(UAes.NoPadding);
			AES.setUsingBc(false);

			AES.createKey(code.getBytes("utf-8"));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	/**
	 * 获取本配置的Key
	 * 
	 * @return the _CfgKey
	 */
	public String getCfgKey() {
		return _CfgKey;
	}

	/**
	 * 本配置的Key
	 * 
	 * @param _CfgKey the _CfgKey to set
	 */
	public void setCfgKey(String _CfgKey) {
		this._CfgKey = _CfgKey;
	}

	private File getRealFile(File from) {

		File pfile;
		int inc = 0;
		try {
			pfile = from.getCanonicalFile();
			while (!pfile.getAbsolutePath().equals(from.getAbsolutePath())) {
				inc++;
				if (inc > 10) {
					return from;
				}
				pfile = pfile.getCanonicalFile();
			}
			return pfile;
		} catch (IOException e) {
			return from;
		}

	}
}
