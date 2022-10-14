package com.gdxsoft.easyweb.script.display;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SearchParameter;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.template.XItems;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class HtmlUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(HtmlUtils.class);
	/**
	 * 如果名称为空的话，根据值表达式，生成属性名称，例如：@USER_NAME = data-user-name<br>
	 * name = "" and value = "" return null<br>
	 * name = "" return data-xxx<br>
	 * name !="" return name
	 * 
	 * @param name  属性名称
	 * @param value 属性参数
	 * @return 属性名称
	 */
	public static String createAttNameByValue(String name, String value) {
		if (name.length() == 0 && value.length() == 0) {
			return null;
		}
		if (name.length() == 0) {
			// 以data-开头的参数名称
			return "data-" + value.replace("@", "").toLowerCase().replace("_", "-").replace(" ", "");
		} else {
			return name;
		}
	}
	/**
	 * 处理签名数据，将imageBase64转换为二进制
	 * 
	 * @param imageBase64 图片的Base64，data:image/png;base64,cfxx...
	 * @param key         字段的基本名称，会出现<br>
	 *                    key: 原始的imageBase64<br>
	 *                    key_bin: 图片的二进制<br>
	 *                    key_length: 图片的长度<br>
	 *                    key_ext: 图片的扩展名<br>
	 *                    key_md5: 图片的md5<br>
	 *                    key_path: 图片的保存完整地址，如果设定SignPath参数<br>
	 *                    Key_path_short: 图片保存地址前缀<br>
	 *                    key_url: 图片的下载地址
	 * @param rv          参数组
	 * @param uxi         签名的配置信息
	 * @param itemValues  替换地址信息用
	 * @return 是否成功
	 * @throws Exception
	 */
	public static boolean handleSignature(String imageBase64, String key, RequestValue rv, UserXItem uxi,
			ItemValues itemValues) throws Exception {
		int loc = imageBase64.indexOf(",");
		if (loc < 0) {// 没有数据
			return false;
		}
		UserXItemValues uxv = uxi.getItem("signature");
		// 保存路径
		String signPath = uxv.getItem(0).getItem("SignPath");

		// data:image/jpeg;base64,
		String metaData = imageBase64.substring(0, loc);
		String ext = metaData.split("\\:")[1].split("\\;")[0].split("\\/")[1];
		String base64 = imageBase64.substring(loc + 1);
		String keyBuf = key + "_BIN";
		String keyLength = key + "_LENGTH";
		String keyExt = key + "_EXT";
		byte[] buf = null;
		String md5 = null;
		try {
			buf = UConvert.FromBase64String(base64);
			md5 = Utils.md5(buf);
			// 转成的二进制文件
			PageValue pv = new PageValue(keyBuf, "binary", buf, buf.length);
			pv.setPVTag(PageValueTag.SYSTEM);
			rv.getPageValues().remove(keyBuf);
			rv.addValue(pv);

			// 增加数据的长度
			rv.getPageValues().remove(keyLength);
			rv.addValue(keyLength, buf.length, PageValueTag.SYSTEM);
			// 增加扩展名
			rv.getPageValues().remove(keyExt);
			rv.addValue(keyExt, ext, PageValueTag.SYSTEM);

			// 增加md5
			String keymd5 = key + "_MD5";
			rv.getPageValues().remove(keymd5);
			rv.addValue(keymd5, md5, PageValueTag.SYSTEM);

		} catch (Exception err1) {
			LOGGER.error(err1.getMessage(), err1);
			return false;
		}

		if (StringUtils.isBlank(signPath)) {
			return true;
		}

		// 保存签名到图片文件
		String signPath1 = itemValues.replaceJsParameters(signPath) + "/" + md5 + "." + ext.toLowerCase();
		String path = UPath.getPATH_UPLOAD() + "/" + signPath1;
		String url = UPath.getPATH_UPLOAD_URL() + "/" + signPath1;

		// 增加物理路径
		String keyPath = key + "_PATH";
		rv.getPageValues().remove(keyPath);
		rv.addValue(keyPath, path, PageValueTag.SYSTEM);

		// 增加短物理路径，不含上传根目录
		String keyPathShort = key + "_PATH_SHORT";
		rv.getPageValues().remove(keyPathShort);
		rv.addValue(keyPathShort, signPath1, PageValueTag.SYSTEM);

		// 增加下载路径
		String keyUrl = key + "_URL";
		rv.getPageValues().remove(keyUrl);
		rv.addValue(keyUrl, url, PageValueTag.SYSTEM);

		UFile.createBinaryFile(path, buf, true);
		return true;
	}

	/**
	 * 获取Format后的值
	 * 
	 * @param format
	 * @param oriValue
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	public static String formatValue(String format, Object oriValue, String lang) throws Exception {
		String s1 = UFormat.formatValue(format, oriValue, lang);
		return s1;
	}

	/**
	 * 获取描述
	 * 
	 * @param uvs
	 * @param tag
	 * @param lang
	 * @return
	 */
	public static String getDescription(UserXItemValues uvs, String tag, String lang) {
		try {
			UserXItemValue uv;
			if (uvs.testName(lang)) {

				uv = uvs.getItem(lang);

			} else {
				uv = uvs.getItem(0);
			}
			return uv.getItem(tag);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * 重新组合ListFrame的 SQL查询语句，用于排序和查询
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public static String createSqListFrame(String sql, HtmlCreator htmlCreator) throws Exception {
		RequestValue rv = htmlCreator.getRequestValue();
		UserConfig uc = htmlCreator.getUserConfig();
		DataConnection conn = htmlCreator.getDataConn();
		String userOrder = rv.getString(FrameParameters.EWA_LF_ORDER);
		String userSearch = rv.getString(FrameParameters.EWA_LF_SEARCH);
		if (userOrder == null && userSearch == null) {
			return sql;
		}
		String keyField = uc.getUserPageItem().getSingleValue("PageSize", "KeyField");
		if (userOrder != null) {
			String name = userOrder.split(" ")[0].trim();
			if (uc.getUserXItems().testName(name)) {
				UserXItem uxi = uc.getUserXItems().getItem(name);
				if (uxi.testName("DataItem") && uxi.getItem("DataItem").count() > 0) {
					String s1 = uxi.getItem("DataItem").getItem(0).getItem("DataField");
					String orderField = s1.trim().toUpperCase();

					if (userOrder.indexOf(" ") > 0) {
						s1 += " DESC";
					}
					userOrder = s1;
					if (keyField != null && keyField.trim().length() > 0) {
						String[] s2 = keyField.trim().toUpperCase().split(",");
						for (int i = 0; i < s2.length; i++) {
							String f = s2[i].split(" ")[0];
							if (orderField.equals(f)) { // 已经在表达式上面了
								continue;
							}
							userOrder += "," + f;
						}
					}
				} else {
					userOrder = null;
				}
			} else {
				userOrder = null;
			}
		}
		if (userSearch != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("  (1=1 ");
			String[] para = userSearch.split("@!@");
			for (int i = 1; i < para.length; i++) {
				SearchParameter lsp = new SearchParameter(para[i]);
				if (!lsp.isValid()) {
					continue;
				}
				UserXItem uxi = uc.getUserXItems().getItem(lsp.getName());
				if (!uxi.testName("DataItem")) {
					continue;
				}
				UserXItemValues us = uxi.getItem("DataItem");
				if (us.count() == 0)
					continue;
				UserXItemValue u = us.getItem(0);

				String dataType = u.getItem("DataType").trim().toUpperCase();
				String dataField = u.getItem("DataField");

				if (lsp.isDouble()) {
					if (dataType.indexOf("DATE") >= 0 || dataType.indexOf("TIME") >= 0) {
						if (!lsp.getPara1().equals("")) {
							String d1 = conn.getDateTimePara(lsp.getPara1());
							sb.append(" AND " + dataField + " >=" + d1);
						}
						if (!lsp.getPara2().equals("")) {
							String dd2 = conn.getDateTimePara(lsp.getPara2());
							sb.append(" AND " + dataField + " <=" + dd2);
						}
					} else if (dataType.indexOf("NUM") == 0 || dataType.indexOf("INT") >= 0) {
						if (!lsp.getPara1().equals(""))
							sb.append(" AND " + dataField + " >=" + lsp.getPara1());
						if (!lsp.getPara2().equals(""))
							sb.append(" AND " + dataField + " <=" + lsp.getPara2());
					}
				} else {
					sb.append(" AND " + dataField + " like '%" + lsp.getPara1().replace("'", "''") + "%'");
				}
			}
			userSearch = sb.toString() + ")";
		}
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		sql = sp.rebuildSql(userOrder, userSearch);
		return sql;
	}

	public static String createSqlTreeMore(String sql, HtmlCreator htmlCreator) throws Exception {
		UserConfig uc = htmlCreator.getUserConfig();
		UserXItemValues u = uc.getUserPageItem().getItem("Tree");
		RequestValue rv = htmlCreator.getRequestValue();
		if (u.count() == 0)
			return sql;
		UserXItemValue v = u.getItem(0);
		if (!v.getItem("LoadByLevel").equals("1")) {
			return sql;
		}
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		if ("1".equals(rv.getString(FrameParameters.EWA_TREE_MORE))) {
			String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() + " WHERE " + pkey + "=@" + key;
			String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM " + sp.getTableName() + " GROUP BY "
					+ pkey;
			String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
					+ "=B.EWAPID ";
			if (sp.getOrderBy() != null) {
				s1 += " ORDER BY " + sp.getOrderBy();
			}
			return s1;
		} else {
			String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() + " WHERE " + sp.getWhere();
			String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM " + sp.getTableName() + " GROUP BY "
					+ pkey;
			String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
					+ "=B.EWAPID ";
			if (sp.getOrderBy() != null) {
				s1 += " ORDER BY " + sp.getOrderBy();
			}
			return s1;
		}

	}

	public static XItem getXItem(UserXItem userXItem) throws Exception {
		String tagValue = userXItem.getItem("Tag").getItem(0).getItem(0);
		EwaConfig ewaConfig = EwaConfig.instance();
		XItems xItems = ewaConfig.getConfigItems().getItems();

		return xItems.getItem(tagValue);
	}
}
