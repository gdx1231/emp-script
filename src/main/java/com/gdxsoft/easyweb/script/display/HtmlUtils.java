package com.gdxsoft.easyweb.script.display;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SearchParameter;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.template.XItems;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UFormat;

public class HtmlUtils {

	/**
	 * 获取Format后的值
	 * 
	 * @param format
	 * @param oriValue
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	public static String formatValue(String format, Object oriValue, String lang)
			throws Exception {
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
	public static String getDescription(UserXItemValues uvs, String tag,
			String lang) {
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
	public static String createSqListFrame(String sql, HtmlCreator htmlCreator)
			throws Exception {
		RequestValue rv = htmlCreator.getRequestValue();
		UserConfig uc = htmlCreator.getUserConfig();
		DataConnection conn = htmlCreator.getDataConn();
		String userOrder = rv.getString("EWA_LF_ORDER");
		String userSearch = rv.getString("EWA_LF_SEARCH");
		if (userOrder == null && userSearch == null) {
			return sql;
		}
		String keyField = uc.getUserPageItem().getSingleValue("PageSize",
				"KeyField");
		if (userOrder != null) {
			String name = userOrder.split(" ")[0].trim();
			if (uc.getUserXItems().testName(name)) {
				UserXItem uxi = uc.getUserXItems().getItem(name);
				if (uxi.testName("DataItem")
						&& uxi.getItem("DataItem").count() > 0) {
					String s1 = uxi.getItem("DataItem").getItem(0).getItem(
							"DataField");
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
					if (dataType.indexOf("DATE") >= 0
							|| dataType.indexOf("TIME") >= 0) {
						if (!lsp.getPara1().equals("")) {
							String d1 = conn.getDateTimePara(lsp.getPara1());
							sb.append(" AND " + dataField + " >=" + d1);
						}
						if (!lsp.getPara2().equals("")) {
							String dd2 = conn.getDateTimePara(lsp.getPara2());
							sb.append(" AND " + dataField + " <=" + dd2);
						}
					} else if (dataType.indexOf("NUM") == 0
							|| dataType.indexOf("INT") >= 0) {
						if (!lsp.getPara1().equals(""))
							sb.append(" AND " + dataField + " >="
									+ lsp.getPara1());
						if (!lsp.getPara2().equals(""))
							sb.append(" AND " + dataField + " <="
									+ lsp.getPara2());
					}
				} else {
					sb.append(" AND " + dataField + " like '%"
							+ lsp.getPara1().replace("'", "''") + "%'");
				}
			}
			userSearch = sb.toString() + ")";
		}
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		sql = sp.rebuildSql(userOrder, userSearch);
		return sql;
	}

	public static String createSqlTreeMore(String sql, HtmlCreator htmlCreator)
			throws Exception {
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

		if (rv.getString("EWA_TREE_MORE") != null
				&& rv.getString("EWA_TREE_MORE").equals("1")) {
			String s3 = "SELECT " + sp.getFields() + " FROM "
					+ sp.getTableName() + " WHERE " + pkey + "=@" + key;
			String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM "
					+ sp.getTableName() + " GROUP BY " + pkey;
			String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3
					+ ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
					+ "=B.EWAPID ";
			if (sp.getOrderBy() != null) {
				s1 += " ORDER BY " + sp.getOrderBy();
			}
			return s1;
		} else {
			String s3 = "SELECT " + sp.getFields() + " FROM "
					+ sp.getTableName() + " WHERE " + sp.getWhere();
			String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM "
					+ sp.getTableName() + " GROUP BY " + pkey;
			String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3
					+ ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
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
