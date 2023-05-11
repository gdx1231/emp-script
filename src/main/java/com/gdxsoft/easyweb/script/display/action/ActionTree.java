package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;

public class ActionTree extends ActionBase implements IAction {
	private static Logger LOGGER = LoggerFactory.getLogger(ActionTree.class);

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @param sqlType
	 * @param name
	 * @return false = 检查执行提交时返回的错误判断
	 */
	public boolean executeSql(String sql, String sqlType, String name) {
		boolean isSelect = DataConnection.checkIsSelect(sql);
		if (sqlType.equals("procedure")) {
			this.executeSqlProcdure(sql);
			// 检查执行提交时返回的错误判断
			return StringUtils.isBlank(this.getChkErrorMsg());
		} else if (isSelect) { // 不再判断 sqlType = query or update
			// 查询
			try {
				sql = this.createSql(sql);
			} catch (Exception e) {
				LOGGER.error("{0}, {1}, {2}, {3}", sql, sqlType, name, e.getMessage());
			}

			DTTable dt = this.executeSqlQuery(sql);
			dt.setName(name);
			// 检查执行提交时返回的错误判断
			return StringUtils.isBlank(this.getChkErrorMsg());
		} else {// 更新
			this.executeSqlUpdate(sql);
			return true;
		}
	}

	/**
	 * 生成分级加载SQL
	 * 
	 * @param sql
	 * @param u
	 * @return
	 * @throws Exception
	 */
	private String createSqlLoadByLevel(String sql, UserXItemValues u) throws Exception {
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() + " WHERE " + sp.getWhere();
		String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM " + sp.getTableName() + " GROUP BY " + pkey;
		String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
				+ "=B.EWAPID ";
		if (sp.getOrderBy() != null) {
			s1 += " ORDER BY " + sp.getOrderBy();
		}
		return s1;

	}

	/**
	 * 生成分级加载获取子节点
	 * 
	 * @param sql
	 * @param u
	 * @return
	 * @throws Exception
	 */
	private String createLoadByLevelMore(String sql, UserXItemValues u) throws Exception {
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() + " WHERE " + pkey + "=@" + key;
		String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT FROM " + sp.getTableName() + " GROUP BY " + pkey;
		String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A \r\n LEFT JOIN (" + s2 + ") B ON A." + key
				+ "=B.EWAPID ";
		if (sp.getOrderBy() != null) {
			s1 += " ORDER BY " + sp.getOrderBy();
		}
		return s1;
	}

	private String createStatusFindPkey(String sql) {
		super.executeSqlQuery(sql);
		DTTable tb = (DTTable) super.getDTTables().getLast();
		if (tb.getCount() == 0) {
			return null;
		}
		DTCell cell = tb.getCell(0, 0);
		return cell.toString();
	}

	private String createStatusFindPkeys(String sql, UserXItemValues u) throws Exception {
		ArrayList<String> keys = new ArrayList<String>();
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s1 = "SELECT " + pkey + " FROM " + sp.getTableName() + " WHERE " + key + "='";
		String keyValue = super.getRequestValue().getString(key);
		int m = 0;
		while (keyValue != null) {
			m++;
			if (m > 20) {// 循环加载？
				return null;
			}
			if (keys.contains(keyValue)) {
				break;
			}
			keys.add(keyValue);
			String s2 = s1 + keyValue.replace("'", "''") + "'";
			keyValue = this.createStatusFindPkey(s2);
		}
		if (keys.size() <= 1) {
			return null; // 指定的key值不对
		}

		StringBuilder s3 = new StringBuilder();
		s3.append("SELECT " + sp.getFields() + " FROM " + sp.getTableName() + " WHERE " + pkey + " IN (");
		for (int i = 0; i < keys.size() - 1; i++) {
			if (i > 0) {
				s3.append(",");
			}
			s3.append(keys.get(i).replace("'", "''"));
		}
		s3.append(") ");
		if (sp.getOrderBy() != null && sp.getOrderBy().trim().length() > 0) {
			s3.append(" ORDER BY " + sp.getOrderBy());
		}

		return s3.toString();
	}

	/**
	 * 获取更多的子节点数据
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public String createSql(String sql) throws Exception {
		UserConfig uc = super.getUserConfig();
		UserXItemValues u = uc.getUserPageItem().getItem("Tree");
		RequestValue rv = super.getRequestValue();
		if (u.count() == 0)
			return sql;
		UserXItemValue v = u.getItem(0);
		if (!v.getItem("LoadByLevel").equals("1")) {
			return sql;
		} else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_MORE))) {
			return this.createLoadByLevelMore(sql, u);
		} else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_STATUS))) {
			String s1 = this.createStatusFindPkeys(sql, u);
			if (s1 == null) {
				return "";
			} else {
				String s2 = this.createSqlLoadByLevel(s1, u);
				return s2;
			}
		} else {
			return this.createSqlLoadByLevel(sql, u);
		}
	}

}