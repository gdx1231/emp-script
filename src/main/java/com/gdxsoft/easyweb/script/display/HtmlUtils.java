package com.gdxsoft.easyweb.script.display;

import java.awt.image.BufferedImage;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfValidOp;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SearchParameter;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.ValidCode1;
import com.gdxsoft.easyweb.script.ValidSlidePuzzle;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.template.XItems;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.validOp.IOp;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class HtmlUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(HtmlUtils.class);

	/**
	 * 幂等性，在FrameFrame.handleIdempotance，将值放到hidden中，同时放到session中<br>
	 * HtmlCreateor.checkIdempotence 在提交时判断此值是否存在，<br>
	 * 如果存在则继续，同时删除session中的值<br>
	 * 不存在，则提示信息
	 * 
	 * @param htmlClass
	 * @return
	 * @throws Exception
	 */
	public static boolean checkIdempotence(HtmlClass htmlClass) throws Exception {
		List<UserXItem> lst = htmlClass.getUserConfig().getIdempotenceXItems();
		if (lst.size() == 0) {
			return true;
		}

		IOp op = ConfValidOp.getInstance().getOp();
		for (int i = 0; i < lst.size(); i++) {
			UserXItem uxi = lst.get(i);
			op.init(htmlClass, uxi);

			if (op.checkOnlyOnce()) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 创建拼图验证的json
	 * 
	 * @param htmlClass
	 * @return
	 * @throws Exception
	 */
	public static JSONObject createValidSildePuzzle(HtmlClass htmlClass) {
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		String name = rv.s("EWA_TRIGGER_VALID_NAME");

		List<UserXItem> items = htmlClass.getUserConfig().getSlidePuzzleXItems();
		UserXItem itemSildePuzzle = null;
		for (int i = 0; i < items.size(); i++) {
			UserXItem item = items.get(i);
			if (item.getName().equals(name)) {
				itemSildePuzzle = item;
				break;
			}
		}
		if (itemSildePuzzle == null) {
			return UJSon.rstFalse(name + ", NOT fund ValidSildePuzzle");
		}

		int width = 400;
		if (rv.isNotBlank("ewa_trigger_valid_width")) {
			try {
				width = rv.getInt("ewa_trigger_valid_width");
				if (width > 460) {
					width = 400;
				} else {
					width = width - 24;
				}
			} catch (Exception err) {

			}
		}
		if (width < 200) {
			width = 400;
		}

		IOp op = ConfValidOp.getInstance().getOp();
		op.init(htmlClass, itemSildePuzzle);

		ValidSlidePuzzle vsp = new ValidSlidePuzzle();
		vsp.setBigWidth(width);

		try {
			vsp.randomImg(ValidSlidePuzzle.getDefImgs());
		} catch (Exception err) {
			return UJSon.rstFalse(err.getMessage());
		}
		JSONObject sysValue = vsp.toJsonSys();
		op.setGeneratedValue(sysValue.toString());
		op.save();

		JSONObject json = vsp.toJsonWeb();
		return json;
	}

	public static JSONObject verifyValidSildePuzzle(HtmlClass htmlClass) throws Exception {
		if (htmlClass.getUserConfig().getSlidePuzzleXItems().size() == 0) {
			return UJSon.rstTrue();
		}
		// 和EWA_FrameClass.CreateAjax一致
		String afterTag = "_TRIGGER_VALID_RESULT";

		long thresholdMills = 1000 * 60; // 60s
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		List<UserXItem> items = htmlClass.getUserConfig().getSlidePuzzleXItems();
		for (int i = 0; i < items.size(); i++) {
			UserXItem item = items.get(i);

			String name = item.getName() + afterTag;
			String encrypt = rv.s(name);
			try {
				JSONObject valid = new JSONObject(UAes.getInstance().decrypt(encrypt));

				long t = valid.getLong("t");
				String name1 = valid.getString("name");
				if (!item.getName().equals(name1)) {
					return UJSon.rstFalse("invalid name " + name1);
				}
				long diff = System.currentTimeMillis() - t;

				if (diff > thresholdMills) {
					return UJSon.rstFalse("overtime, " + diff);
				}
			} catch (Exception err) {
				return UJSon.rstFalse(err.getMessage());
			}
		}

		return UJSon.rstTrue();

	}

	/**
	 * 创建拼图验证的结果
	 * 
	 * @param htmlClass
	 * @return
	 * @throws Exception
	 */
	public static JSONObject checkValidSildePuzzle(HtmlClass htmlClass) throws Exception {
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		String name = rv.s("EWA_TRIGGER_VALID_NAME");
		List<UserXItem> items = htmlClass.getUserConfig().getSlidePuzzleXItems();
		UserXItem itemSildePuzzle = null;
		for (int i = 0; i < items.size(); i++) {
			UserXItem item = items.get(i);
			if (item.getName().equals(name)) {
				itemSildePuzzle = item;
				break;
			}
		}
		if (itemSildePuzzle == null) {
			return UJSon.rstFalse(name + ", NOT fund ValidSildePuzzle");
		}

		IOp op = ConfValidOp.getInstance().getOp();
		op.init(htmlClass, itemSildePuzzle);

		String sv = op.getSysValue();
		JSONObject sysVal = new JSONObject(sv);

		double left = -1;
		double top = -1;
		try {
			left = rv.getDouble("left");
			// double left1 = rv.getDouble("left1");
			top = rv.getDouble("top");
			// double top1 = rv.getDouble("top1");

			int leftSys = sysVal.optInt("posX");
			int posY = sysVal.optInt("posY");

			if (Math.abs(left - leftSys) <= 5 && Math.abs(top - posY) < 1) {
				op.removeSysValue();

				long t = System.currentTimeMillis();
				JSONObject valid = new JSONObject();
				valid.put("t", t);
				valid.put("name", name);
				String encrypt = UAes.getInstance().encrypt(valid.toString());

				return UJSon.rstTrue().put("VALID", encrypt);
			} else {
				JSONObject result = UJSon.rstFalse("不行");
				int inc = sysVal.has("inc") ? sysVal.optInt("inc") : 0;
				inc++;
				if (inc == 3) {
					JSONObject newConfig = createValidSildePuzzle(htmlClass);
					result.put("newConfig", newConfig);
				} else {
					sysVal.put("inc", inc);
					op.save(sysVal.toString());
				}

				return result;
			}
		} catch (Exception err) {
			return UJSon.rstFalse(err.getMessage());
		}
	}

	/**
	 * 检查验证码
	 * 
	 * @param htmlClass
	 * @return
	 * @throws Exception
	 */
	public static boolean checkValidCode(HtmlClass htmlClass) throws Exception {
		UserXItem uxiValid = htmlClass.getUserConfig().getValidXItem();
		if (uxiValid == null) { // 没有验证码定义
			return true;
		}
		IOp op = ConfValidOp.getInstance().getOp();
		op.init(htmlClass, uxiValid);

		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		PageValue pv = rv.getPageValues().getPageValue(FrameParameters.EWA_VALIDCODE_CHECK);
		// 不检查验证码，用于手机应用或AJAX调用
		if (pv != null && "NOT_CHECK".equals(pv.toString())) {
			if (pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS // 限定参数来源于htmlControl的paras
					|| pv.getPVTag() == PageValueTag.SYSTEM // 限定参数来源于SYSTEM
					|| pv.getPVTag() == PageValueTag.SESSION // 限定参数来源于session
			) {
				op.removeSysValue();
				return true;
			} else {
				LOGGER.info("Invalid pageValueTag {} to skip validcode", pv.getPVTag());
			}
		}

		return op.checkOnlyOnce();
	}

	/**
	 * 创建本配置的验证码
	 * 
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage createValidCode(HtmlClass htmlClass) throws Exception {
		UserXItem uxi_vc = htmlClass.getUserConfig().getValidXItem();
		if (uxi_vc == null) {
			return null; // 没有配置验证码
		}

		int len = 6;
		String vcType = "string";
		boolean isNumberCode = false;// 默认是数字验证码
		if (uxi_vc != null) {
			if (uxi_vc.testName("MaxMinLength")) {
				try {
					len = Integer.parseInt(uxi_vc.getSingleValue("MaxMinLength", "MaxLength"));
				} catch (Exception err) {
				}
			}
			if (uxi_vc.testName("DataItem")) {
				vcType = uxi_vc.getSingleValue("DataItem", "DataType");
			}

			isNumberCode = !vcType.equalsIgnoreCase("string");
		}

		if (len > 10) {
			len = 10;
		} else if (len < 4) {
			len = 4;
		}

		ValidCode1 vc = new ValidCode1(len, isNumberCode);
		BufferedImage image = vc.createCode();

		IOp op = ConfValidOp.getInstance().getOp();
		op.init(htmlClass, uxi_vc);
		// 保存验证码值到系统中
		op.setGeneratedValue(vc.getRandomNumber());
		op.save();

		return image;
	}

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
		String signPath = "";
		if (uxv.count() > 0) {
			// 保存路径
			signPath = uxv.getItem(0).getItem("SignPath");
		}
		signPath = signPath.trim();

		// data:image/jpeg;base64,
		String metaData = imageBase64.substring(0, loc);
		String ext = metaData.split("\\:")[1].split("\\;")[0].split("\\/")[1];
		String base64 = imageBase64.substring(loc + 1);

		byte[] buf = null;
		String md5 = null;
		try {
			buf = UConvert.FromBase64String(base64);
			md5 = Utils.md5(buf);
			// 转成的二进制文件
			String keyBuf = key + "_BIN";
			PageValue pv = new PageValue(keyBuf, "binary", buf, buf.length);
			pv.setPVTag(PageValueTag.SYSTEM);
			rv.getPageValues().remove(keyBuf);
			rv.addValue(pv);

			// 增加数据的长度
			String keyLength = key + "_LENGTH";
			rv.getPageValues().remove(keyLength);
			rv.addValue(keyLength, buf.length, PageValueTag.SYSTEM);

			// 增加扩展名
			String keyExt = key + "_EXT";
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
