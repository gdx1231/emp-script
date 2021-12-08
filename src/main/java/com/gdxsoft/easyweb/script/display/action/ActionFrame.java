package com.gdxsoft.easyweb.script.display.action;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ActionFrame extends ActionBase implements IAction {

	private MTable _Uploads;

	public ActionFrame() {

	}

	private void initUploadParas() {
		if (super.getHtmlClass() == null) {
			return;
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
				// String dataType = item.getSingleValue("DataItem",
				// "DataType");
				// if (dataType == null || !dataType.equalsIgnoreCase("binary"))
				// {
				// continue;
				// }
				UserXItemValue u = item.getItem("Upload").getItem(0);
				if (u.testName("UpSaveMethod")) {
					String upSaveMethod = u.getItem("UpSaveMethod");
					if (upSaveMethod != null) {
						_Uploads.add(item.getName(), item);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(this + ": " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeCallSql(java.lang.
	 * String )
	 */
	public void executeCallSql(String name) throws Exception {
		this.initUploadParas();
		this.createUploadsParas();

		 super.executeCallSql(name);
	}

	/**
	 * 生成上传文件参数，用于数据库保存
	 */
	void createUploadsParas() {
		RequestValue rv = super.getRequestValue();
		if (rv.getString("____createUploadPara____") != null) {
			return;
		}
		for (int i = 0; i < this._Uploads.getCount(); i++) {
			UserXItem item = (UserXItem) this._Uploads.getByIndex(i);
			try {
				this.createUploadPara(item);
			} catch (Exception err) {
				removeUpload(item);
				System.err.println(this + ":" + err.getMessage());
			}
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
		if (u.testName("UpPath")) {
			String p1 = u.getItem("UpPath");
			if (p1.trim().length() > 0) {
				p = super.getItemValues().replaceParameters(p1, false);
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
				arr = new JSONArray(json);
				if (arr.length() == 0) {
					removeUpload(item);
					return;
				}
				// [{"ISREAL":true,"UP_SIZE":731,"UP_URL":"/img_tmps//GRP_FIN_MAIN/c8a1b3da-4622-4841-9fff-6b43970d72f0_0.png","CT":"/img_tmps/","UP_NAME":"c8a1b3da-4622-4841-9fff-6b43970d72f0_0.png","UP_LOCAL_NAME":"bg.png","UP_UNID":"c8a1b3da-4622-4841-9fff-6b43970d72f0"}]

				// 由于上传可能返回多个文件属性,因此取第一个JSON
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
			System.out.println("InValid upName:" + upName);
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
		if (f.exists() && f.isFile() && f.canRead()) {
			byte[] buf = UFile.readFileBytes(f.getAbsolutePath());

			if (dataType != null && dataType.equalsIgnoreCase("binary")) {
				// 替换参数，转换成文件二进制
				if (rv.getPageValues().getValue(uploadName) == null) {
					rv.addValue(uploadName, buf, "binary", buf.length);
				} else {
					rv.changeValue(uploadName, buf, "binary", buf.length);
				}

			} else {
				if (item1 != null) {
					// html5upload
					// 字符串类型表示存储url地址
					String file_url = item1.getString("UP_URL");
					if (rv.getPageValues().getValue(uploadName) == null) {
						rv.addValue(uploadName, file_url);
					} else {
						rv.changeValue(uploadName, file_url, "string", file_url.length());
					}
				} else {
					// swfupload
				}
			}

			// 文件md5
			rv.addValue(uploadName + "_MD5", Utils.md5(buf));

			// 文件保存名称
			rv.addValue(uploadName + "_NAME", f.getName());

			// 文件物理地址( 完整路径)
			rv.addValue(uploadName + "_PATH", f.getAbsolutePath());

			// 去除UPath.getPATH_UPLOAD() 的路径
			rv.addValue(uploadName + "_PATH_SHORT", shortPath);
			// 文件字节
			rv.addValue(uploadName + "_SIZE", f.length());
			// 文件字节
			rv.addValue(uploadName + "_LENGTH", f.length());
			// 文件扩展名
			rv.addValue(uploadName + "_EXT", UFile.getFileExt(f.getName()));

			if (item1 != null) {
				// 上传文件(1或多个)的UNID
				rv.addValue(uploadName + "_UP_UNID", item1.getString("UP_UNID"));
				// 上传文件(1或多个)的UNID
				rv.addValue(uploadName + "_JSON", arr.toString());
				// 文件URL
				rv.addValue(uploadName + "_URL", item1.getString("UP_URL"));

				// 上传文件的Url前缀
				rv.addValue(uploadName + "_CT", item1.getString("CT"));

				// 上传文件的本地名称
				rv.addValue(uploadName + "_LOCAL_NAME", item1.getString("UP_LOCAL_NAME"));

			}
		}
	}
	 
}
