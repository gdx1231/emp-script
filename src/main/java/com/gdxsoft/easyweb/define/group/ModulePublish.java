package com.gdxsoft.easyweb.define.group;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.group.dao.EwaModDownload;
import com.gdxsoft.easyweb.define.group.dao.EwaModDownloadDao;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.HandleJsonBinaryBase64Impl;
import com.gdxsoft.easyweb.utils.IHandleJsonBinary;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.USign;
import com.gdxsoft.easyweb.utils.Utils;

public class ModulePublish extends ModuleBase {
	private static Logger LOGGER = LoggerFactory.getLogger(ModulePublish.class);
	private DTTable tbPkg;
	String jdbcConfigName;

	String apiKey;
	String apiSecert;
	String apiServer;

	public ModulePublish() {
		initApi();
	}

	public JSONObject downloadFromPublishServer(JSONArray parameters) throws Exception {
		JSONObject result = this.checkApiSettings();
		if (!result.optBoolean("RST")) {
			return result;
		}

		JSONArray arr = new JSONArray();
		result.put("tokens", arr);
		// 第一步，获取所有下载用的token，每个id对应一个token
		for (int i = 0; i < parameters.length(); i++) {
			JSONObject item = parameters.getJSONObject(i);
			long id = item.optLong("id");
			// 获取下载用的token
			JSONObject tokenResult = this.getDownloadToken(id);
			arr.put(tokenResult);
		}

		JSONArray arr1 = new JSONArray();
		result.put("packages", arr1);
		// 第二步，根据token下载文件
		for (int i = 0; i < parameters.length(); i++) {
			JSONObject item = parameters.getJSONObject(i);
			long id = item.optLong("id");
			JSONObject tokenResult = arr.getJSONObject(i);
			if (UJSon.checkFalse(tokenResult)) {
				arr1.put(new JSONObject(tokenResult));
				continue;
			}
			String token = tokenResult.getString("token");
			// 下载包文件
			JSONObject downResult = this.downloadPackage(id, token);
			arr1.put(downResult);
		}

		return result;
	}

	private JSONObject downloadPackage(long dowloadModVerId, String token) {
		Map<String, String> post = new HashMap<>();
		post.put("id", dowloadModVerId + "");
		post.put("method", "download");
		post.put("token", token);

		String sign = USign.signSha1(post, "secert", apiSecert, true);
		post.put("sign", sign);

		UNet net = this.createNet();
		String url = apiServer + "/module/download";
		String downResult = net.doPost(url, post);
		return this.recordDownload(new JSONObject(downResult), url);
	}

	private JSONObject getDownloadToken(long dowloadModVerId) {
		String sql = "select pkg_md5 from ewa_mod_download where mod_ver_id = " + dowloadModVerId;
		DTTable tb = DTTable.getJdbcTable(sql, this.jdbcConfigName);

		String existsMd5s = tb.joinIds("pkg_md5", false);

		Map<String, String> post = new HashMap<>();
		post.put("id", dowloadModVerId + "");
		post.put("method", "token");
		post.put("exists_md5s", existsMd5s);

		String sign = USign.signSha1(post, "secert", apiSecert, true);
		post.put("sign", sign);

		UNet net = this.createNet();
		String url = apiServer + "/module/download";
		String downResult = net.doPost(url, post);
		return new JSONObject(downResult);
	}

	private JSONObject recordDownload(JSONObject downJson, String url) {
		if (!downJson.optBoolean("RST")) {
			return downJson;
		}
		IHandleJsonBinary handleBinary = new HandleJsonBinaryBase64Impl();
		UAes aes = this.createAes();
		EwaModDownloadDao d = new EwaModDownloadDao();
		d.setConfigName(jdbcConfigName);

		String encryptionBase64 = downJson.getString("package_encrypted");

		byte[] encryptionBuf = null;
		try {
			encryptionBuf = UConvert.FromBase64String(encryptionBase64);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return UJSon.rstFalse(e.getMessage());
		}
		byte[] packageBuf = null;
		String pkgMd5 = null;
		try {
			packageBuf = aes.decryptBytes(encryptionBuf);
			pkgMd5 = Utils.md5(packageBuf);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return UJSon.rstFalse(e.getMessage());
		}

		EwaModDownload down = new EwaModDownload();
		down.initValues(downJson.optJSONObject("module_meta"), handleBinary);
		down.initValues(downJson.optJSONObject("version_meta"), handleBinary);
		down.initValues(downJson.optJSONObject("package_meta"), handleBinary);

		if (!pkgMd5.equalsIgnoreCase(down.getPkgMd5())) {
			String err = "Pacakge md5 not equals remote: " + down.getPkgMd5() + ", decrypted: " + pkgMd5;
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		}

		down.setPkgFile(packageBuf);

		down.setModDlCdate(new Date());
		down.setModDlMdate(new Date());
		down.setModDlStatus("USED");
		down.setModDlSupId(0);

		down.setModDlUrl(url);

		d.newRecord(down);

		JSONObject rst = UJSon.rstTrue(null);
		rst.put("dlid", down.getModDlId());
		return rst;
	}

	public JSONObject publishToServer(long moduleVerId) throws Exception {
		JSONObject result = this.checkApiSettings();
		if (!result.optBoolean("RST")) {
			return result;
		}
		byte[] packageBuffer = this.getExportModuleFile(moduleVerId);

		if (packageBuffer == null) {
			LOGGER.error("No package record in ewa_mod_package");
			return UJSon.rstFalse("No package record in ewa_mod_package");
		}

		String md5 = this.tbPkg.getCell(0, "pkg_md5").toString();

		JSONObject uploadToken = this.getUploadTokenFromRemote(md5);
		if (UJSon.checkFalse(uploadToken)) {
			return uploadToken;
		}
		String token = uploadToken.optString("token");

		Map<String, String> parameters = new HashMap<>();
		parameters.put("md5", md5);
		parameters.put("code", this.moduleCode);
		parameters.put("version", this.moduleVersion);
		parameters.put("aes", "gcm256");
		parameters.put("token", token);
		parameters.put("method", "upload");

		String sign = USign.signSha1(parameters, "secert", apiSecert, true);
		parameters.put("sign", sign);
		parameters.put("api_version", "1");
		// 加密文件
		UAes aes = new UAes(this.apiSecert, null, UAes.AES_256_GCM);
		byte encryptionPackage[] = aes.encryptBytes(packageBuffer);
		parameters.put("package", UConvert.ToBase64String(encryptionPackage));

		UNet net = this.createNet();
		String url = apiServer + "/module/upload";
		String postResult = net.doPost(url, parameters);

		return new JSONObject(postResult);
	}

	private JSONObject getUploadTokenFromRemote(String md5) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("md5", md5);
		parameters.put("code", this.moduleCode);
		parameters.put("version", this.moduleVersion);
		parameters.put("method", "token");

		String sign = USign.signSha1(parameters, "secert", apiSecert, true);
		parameters.put("sign", sign);

		UNet net = this.createNet();
		String url = apiServer + "/module/upload";
		String postResult = net.doPost(url, parameters);

		return new JSONObject(postResult);
	}

	public UNet createNet() {
		UNet net = new UNet();
		net.setUserAgent("UNet/ewa-2021-11-30");
		net.addHeader("apiKey", apiKey);

		return net;
	}

	public UAes createAes() {
		UAes aes = new UAes(this.apiSecert, null, UAes.AES_256_GCM);
		return aes;
	}

	public byte[] getExportModuleFile(long moduleVerId) throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("modVerId", moduleVerId);
		String sql2 = "select mod_ver, mod_code, mod_ver_id from ewa_mod_ver where mod_ver_id = @modVerId";
		DTTable tbModVer = DTTable.getJdbcTable(sql2, this.jdbcConfigName, rv);
		if (tbModVer.getCount() == 0) {
			return null;
		}
		this.moduleCode = tbModVer.getCell(0, "mod_code").toString();
		this.moduleVersion = tbModVer.getCell(0, "mod_ver").toString();

		String sqlExists = "select * from ewa_mod_package where mod_ver_id=@modVerId ";
		DTTable tbExistsPkg = DTTable.getJdbcTable(sqlExists, this.jdbcConfigName, rv);
		if (tbExistsPkg.getCount() == 0) {
			return null;
		}
		tbPkg = tbExistsPkg;
		return (byte[]) tbExistsPkg.getCell(0, "pkg_file").getValue();
	}

	private void initApi() {
		ConfDefine d = ConfDefine.getInstance();
		if (d == null) {
			return;
		}
		if (!d.isDefine()) {
			return;
		}
		apiKey = d.getApiKey();
		apiSecert = d.getApiSecert();
		apiServer = d.getApiServer();

		ConfScriptPaths.getInstance().getLst().forEach(conf -> {
			if (this.jdbcConfigName == null && conf.isJdbc()) {
				// 获取第一个jdbc连接池
				this.jdbcConfigName = conf.getJdbcConfigName();
			}
		});
	}

	public JSONObject checkApiSettings() {
		ConfDefine d = ConfDefine.getInstance();
		if (d == null) {
			return UJSon.rstFalse("The did not allow");
		}
		if (!d.isDefine()) {
			return UJSon.rstFalse("The did not allow");
		}
		if (StringUtils.isBlank(apiKey)) {
			return UJSon.rstFalse("The apiKey not defined in the ewa_conf.xml -> define");
		}
		if (StringUtils.isBlank(apiSecert)) {
			return UJSon.rstFalse("The apiSecert not defined in the ewa_conf.xml -> define");
		}
		if (StringUtils.isBlank(apiServer)) {
			return UJSon.rstFalse("The apiServer not defined in the ewa_conf.xml -> define");
		}

		return UJSon.rstTrue(null);
	}

	/**
	 * @return the tbPkg
	 */
	public DTTable getTbPkg() {
		return tbPkg;
	}

	/**
	 * @return the jdbcConfigName
	 */
	public String getJdbcConfigName() {
		return jdbcConfigName;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @return the apiSecert
	 */
	public String getApiSecert() {
		return apiSecert;
	}

	/**
	 * @return the apiServer
	 */
	public String getApiServer() {
		return apiServer;
	}
}
