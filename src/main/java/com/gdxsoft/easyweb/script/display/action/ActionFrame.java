package com.gdxsoft.easyweb.script.display.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.FileUpload;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.uploader.UploadUtils;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ActionFrame extends ActionBase implements IAction {
	private static Logger LOGGER = LoggerFactory.getLogger(ActionFrame.class);
	private MTable _Uploads;

	public ActionFrame() {

	}

	/**
	 * Initialize upload files parameters
	 */
	private boolean initUploadParas() {
		if (super.getHtmlClass() == null) {
			return false;
		}
		UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
		_Uploads = new MTable();
		try {
			for (int i = 0; i < items.count(); i++) {
				UserXItem item = items.getItem(i);
				String tag = item.getSingleValue("tag");
				if (!(tag.equalsIgnoreCase("swffile") || tag.equalsIgnoreCase("image")
						|| tag.equalsIgnoreCase("h5upload"))) {
					continue;
				}
				if (!item.testName("Upload")) {
					continue;
				}

				UserXItemValue u = item.getItem("Upload").getItem(0);
				if (u.testName("UpSaveMethod")) {
					String upSaveMethod = u.getItem("UpSaveMethod");
					if (upSaveMethod != null) {
						_Uploads.add(item.getName(), item);
					}
				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Init upload parametes error: {}", e.getMessage());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeCallSql(java.lang.
	 * String )
	 */
	public void executeCallSql(String name) throws Exception {
		if (this.initUploadParas()) {
			this.createUploadsParas();
		}
		super.executeCallSql(name);
	}

	/**
	 * 生成上传文件参数，用于数据库保存
	 * @throws Exception 
	 */
	void createUploadsParas() throws Exception {
		RequestValue rv = super.getRequestValue();
		if (rv.getString("____createUploadPara____") != null) {
			return;
		}
		for (int i = 0; i < this._Uploads.getCount(); i++) {
			UserXItem item = (UserXItem) this._Uploads.getByIndex(i);
			//try {
				this.createUploadPara(item);
			//} catch (Exception err) {
			//	removeUpload(item);
			//	LOGGER.warn("Create upload parameters err: {}", err.getMessage());
			//	return;
			//}
		}
	}

	void removeUpload(UserXItem item) {
		String uploadName = item.getName();
		RequestValue rv = super.getRequestValue();
		if (rv.getPageValues().getValue(uploadName) == null) {
			rv.addValue(uploadName, null);
		} else {
			rv.changeValue(uploadName, null, "binary", 0);
		}
	}

	/**
	 * 生成上传文件参数，用于数据库保存
	 * 
	 * @param item
	 * @throws Exception
	 */
	void createUploadPara(UserXItem item) throws Exception {
		RequestValue rv = super.getRequestValue();
		String uploadName = item.getName();
		UserXItemValue u = item.getItem("Upload").getItem(0);
		String p = Upload.DEFAULT_UPLOAD_PATH;
		String p1 = Upload.DEFAULT_UPLOAD_PATH;
		if (u.testName("UpPath")) {
			p1 = u.getItem("UpPath");
			if (p1.trim().length() > 0) {
				p = super.getItemValues().replaceParameters(p1, false);
			}
		}

		boolean upJsonEncyrpt = true;
		// UpJsonEncyrpt 返回Json是否加密
		if (u.testName("UpJsonEncyrpt")) {
			String val = u.getItem("UpJsonEncyrpt");
			if ("no".equalsIgnoreCase(val)) {
				upJsonEncyrpt = false; // 不加密
			}
		}

		String upName = rv.getString("UP_NAME");
		JSONObject item1 = null;
		JSONArray arr = null;

		// 数据类型
		String dataType = item.getSingleValue("DataItem", "DataType");
		String tag = item.getSingleValue("Tag");
		if (upName == null) {
			String json = rv.getString(uploadName);
			if (json == null || json.trim().length() == 0) {
				removeUpload(item);
				return;
			}
			if (json.trim().startsWith("[")) {// json表达式
				JSONArray arrEncrypted = new JSONArray(json);
				if (arrEncrypted.length() == 0) {
					removeUpload(item);
					return;
				}
				if (upJsonEncyrpt) { // 返回Json加密（默认）
					arr = new JSONArray();
					for (int i = 0; i < arrEncrypted.length(); i++) {
						JSONObject encryptUploadJson = arrEncrypted.getJSONObject(i);
						if (encryptUploadJson.has("UP")) { // AES 解密 json数据
							String decrypt = UAes.getInstance().decrypt(encryptUploadJson.getString("UP"));
							JSONObject decryptedJson = new JSONObject(decrypt);
							arr.put(decryptedJson);
						}
					}
				} else { // 返回Json使用明码
					arr = arrEncrypted;
				}
				if (arr.length() == 0) {
					removeUpload(item);
					return;
				}
				// [{"ISREAL":true,"UP_SIZE":731,"UP_URL":"/img_tmps//GRP_FIN_MAIN/c8a1b3da-4622-4841-9fff-6b43970d72f0_0.png","CT":"/img_tmps/","UP_NAME":"c8a1b3da-4622-4841-9fff-6b43970d72f0_0.png","UP_LOCAL_NAME":"bg.png","UP_UNID":"c8a1b3da-4622-4841-9fff-6b43970d72f0"}]
				// 由于上传可能返回多个文件属性,因此取第一个JSON

				// 如果是同时上传多个文件，此信息无效
				item1 = arr.getJSONObject(0);
				if (item1.has("UP_NAME")) {
					upName = item1.getString("UP_NAME");
				}
			} else {
				if (dataType != null && dataType.equalsIgnoreCase("binary")) {
					if (tag.equals("swffile")) {
						// 修改时候如果没有上传信息，则将参数设置为空null,
						// 在update要判断是否由参数存在
						rv.getPageValues().remove(uploadName);
					}
				}
				// 当ht5upload 单个上传时候 如果取的是地址本身, 表示没有修改， 不需要映射文件对象;
				return;
			}
		}
		if (upName == null) {
			removeUpload(item);
			return;
		}

		if (upName.indexOf("./") >= 0 || upName.indexOf(".\\") >= 0) { // 非法路径字符
			LOGGER.warn("InValid upName:" + upName);
			removeUpload(item);
			return;

		}
		// 去除UPath.getPATH_UPLOAD() 的路径
		String p_short = p.replace("\\", "/").replace("//", "/");
		String shortPath = p_short + (p_short.endsWith("/") ? "" : "/") + upName;
		// 完整路径
		String fileName = UPath.getPATH_UPLOAD()
				+ (UPath.getPATH_UPLOAD().endsWith("/") || UPath.getPATH_UPLOAD().endsWith("\\") ? "" : File.separator)
				+ shortPath;

		File f = new File(fileName);

		// 添加增加标识
		rv.addValue("____createUploadPara____", "ADDED");

		if (!f.exists()) {
			// 文件被删除- 配置项指定了 删除文件
			// 保留上传的json参数
			if (arr != null) {
				rv.addValue(uploadName + "_JSON", arr.toString());
			}
			if(u.testName("UpDelete")) {
				String isDel=u.getItem("UpDelete");
				if("yes".equalsIgnoreCase(isDel)) {
					return;
				}
			}
			LOGGER.warn("The uploaded file not exists: {}", f.getAbsolutePath());
			LOGGER.warn("Pls check item defined UpPath = {}", p1);
			throw new Exception("The uploaded file not exists: " + f.getAbsolutePath() + ", UpPath=" + p1);
		}
		if (!f.isFile()) {
			if (arr != null) {
				rv.addValue(uploadName + "_JSON", arr.toString());
			}
			LOGGER.warn("The uploaded file is DIRECTORY: {}", f.getAbsolutePath());
			return;
		}
		if (!f.canRead()) {
			if (arr != null) {
				rv.addValue(uploadName + "_JSON", arr.toString());
			}
			LOGGER.warn("The uploaded file CAN'T READ: {}", f.getAbsolutePath());
			return;
		}

		// 构建 FileUpload 并委托到 UploadUtils 执行参数映射
		FileUpload fu = new FileUpload();
		fu.setSavePath(f.getAbsolutePath());
		fu.setSaveFileName(f.getName());
		fu.setExt(UFile.getFileExt(f.getName()));
		fu.setLength((int) f.length());
		if (item1 != null) {
			fu.setFileUrl(item1.getString("UP_URL"));
			fu.setUnid(item1.getString("UP_UNID"));
			fu.setUserLocalPath(item1.optString("UP_LOCAL_NAME", null));
		}

		List<FileUpload> alFiles = new ArrayList<FileUpload>();
		alFiles.add(fu);
		UploadUtils.createUploadPara(uploadName, dataType, alFiles, p, rv);

		// 覆盖 _JSON 为 ActionFrame 解密后的原始格式（保留 ISREAL 等字段）
		if (arr != null) {
			rv.addValue(uploadName + "_JSON", arr.toString());
		}

		// debug 日志
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_MD5=" + UFile.md5(f));
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_NAME=" + f.getName());
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_PATH=" + f.getAbsolutePath());
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_PATH_SHORT=" + shortPath);
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_SIZE=" + f.length());
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_LENGTH=" + f.length());
		super.getDebugFrames().addDebug(this, "Upload", uploadName + "_EXT=" + UFile.getFileExt(f.getName()));
		if (item1 != null) {
			super.getDebugFrames().addDebug(this, "Upload", uploadName + "_UP_UNID=" + item1.getString("UP_UNID"));
			super.getDebugFrames().addDebug(this, "Upload", uploadName + "_URL=" + item1.getString("UP_URL"));
			super.getDebugFrames().addDebug(this, "Upload", uploadName + "_CT=" + item1.getString("CT"));
			super.getDebugFrames().addDebug(this, "Upload",
					uploadName + "_LOCAL_NAME=" + item1.getString("UP_LOCAL_NAME"));
		}
	}

}
