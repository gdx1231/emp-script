package com.gdxsoft.easyweb.script.display.action;

import java.io.File;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ActionCombine extends ActionBase implements IAction {

	private MTable _Uploads;

	public ActionCombine() {

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
				if (!(tag.equalsIgnoreCase("swffile") || tag
						.equalsIgnoreCase("image"))) {
					continue;
				}
				if (!item.testName("Upload")) {
					continue;
				}
				String dataType = item.getSingleValue("DataItem", "DataType");
				if (dataType == null || !dataType.equalsIgnoreCase("binary")) {
					continue;
				}
				UserXItemValue u = item.getItem("Upload").getItem(0);
				if (u.testName("UpSaveMethod")) {
					String upSaveMethod = u.getItem("UpSaveMethod");
					if (upSaveMethod != null
							&& upSaveMethod.equalsIgnoreCase("WithFrame")) {
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
	 * @see
	 * com.gdxsoft.easyweb.script.display.IAction#executeCallSql(java.lang.String
	 * )
	 */
	public void executeCallSql(String name) throws Exception {
		this.initUploadParas();
		this.createUploadsParas();

		UserXItemValues sqlset = super.getUserConfig().getUserActionItem()
				.getItem("SqlSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String sqlExp = sqlItem.getItem("Sql");
		String[] sqlArray = sqlExp.split(";");

		String transType = sqlItem.getItem("TransType");

		boolean isTrans = transType.equalsIgnoreCase("yes") ? true : false;
		DataConnection cnn = super.getItemValues().getSysParas().getDataConn();
		if (isTrans) {
			cnn.transBegin();
		}
		int runInc = 0; // 执行次数
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) { // 空语句
				continue;
			}
			String sqlType = sqlItem.getItem("SqlType");
			this.executeSql(runInc, sql, sqlType, name);
			if (cnn.getErrorMsg() != null && cnn.getErrorMsg().length() > 0) {
				if (isTrans) {
					cnn.transRollback();
				}
				cnn.close();
				throw new Exception(cnn.getErrorMsg());
			}
			runInc++;
		}
		if (isTrans) {
			cnn.transCommit();
		}

		this.setChkErrorMsg(null);

		MTable pvs = super.getRequestValue().getPageValues().getTagValues(
				PageValueTag.DTTABLE);
		Object o = pvs.get("EWA_ERR_OUT");
		if (o != null) {
			PageValue pv = (PageValue) o;
			if (pv.getValue() != null) {
				String v = pv.getValue().toString();
				if (v != null && v.trim().length() > 0) {
					this.setChkErrorMsg(v);
				}
			}
		}

		this.executeSessionsCookies(sqlItem);
	}

	/**
	 * 生成上传文件参数，用于数据库保存
	 */
	void createUploadsParas() {
		for (int i = 0; i < this._Uploads.getCount(); i++) {
			UserXItem item = (UserXItem) this._Uploads.getByIndex(i);
			try {
				this.createUploadPara(item);
			} catch (Exception err) {
				System.err.println(this + ":" + err.getMessage());
			}
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
				p = super.getItemValues().replaceParameters(p, false);
			}
		}
		String fileName = UPath.getPATH_UPLOAD() + File.separator + p
				+ File.separator + super.getRequestValue().getString("UP_NAME");

		File f = new File(fileName);
		if (f.exists() && f.isFile() && f.canRead()) {
			byte[] buf = UFile.readFileBytes(f.getAbsolutePath());
			// 替换参数，转换成文件二进制
			rv.changeValue(uploadName, buf, "binary", buf.length);

			// 文件保存名称
			rv.addValue(uploadName + "_NAME", f.getName());

			// 文件物理地址
			rv.addValue(uploadName + "_PATH", f.getAbsolutePath());

			String url = f.getAbsolutePath().replace(
					UPath.getRealContextPath(), "");
			// 文件URL
			rv.addValue(uploadName + "_URL", url);
			// 文件字节
			rv.addValue(uploadName + "_SIZE", buf.length);
			// 文件扩展名
			rv.addValue(uploadName + "_EXT", UFile.getFileExt(f.getName()));
		}
	}

	void executeSql(int runInc, String sql, String sqlType, String name) {

		if (runInc > 0 && sql.toUpperCase().indexOf("SELECT") == 0) {
			// 执行过程中有其它Select过程
			super.executeSqlQuery(sql);
			DTTable dt = (DTTable) super.getDTTables().getLast();
			if (dt.getCount() == 1) {
				super.addDTTableToRequestValue(dt);
			}
			dt.setName(name);
		} else if (sqlType.equals("query")) {// 查询
			super.executeSqlQuery(sql);
			DTTable dt = (DTTable) super.getDTTables().get(
					super.getDTTables().size() - 1);
			dt.setName(name);
			if (dt.getCount() == 1) {
				super.addDTTableToRequestValue(dt);
			}
		} else if (sqlType.equals("procedure")) {// 存储过程
			super.executeSqlProcdure(sql);
		} else {// 更新
			super.executeSqlUpdate(sql);
		}

	}
}
