package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;

import com.gdxsoft.easyweb.data.DTCell;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlPart;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ActionTree extends ActionBase implements IAction {

	public void executeCallSql(String name) throws Exception {
		ActionSqlSetItem sqlItem = super.retActionSqlSetItem(name);
		String[] sqlArray = sqlItem.getSqlArray();
		String transType = sqlItem.getTransType();
		boolean isTrans = transType.equalsIgnoreCase("yes") ? true : false;
		DataConnection cnn=super.getItemValues().getSysParas().getDataConn();
		
		if (isTrans) {
			cnn.transBegin();
		}
		int runInc = 0; // 执行次数
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) {
				// 空语句
				continue;
			}
			String sqlType = sqlItem.getSqlType();
			if (runInc > 0 &&  DataConnection.checkIsSelect(sql)) {
				// 执行过程中有其它Select过程

				cnn.executeQuery(sql);
				DTTable tb = new DTTable(); // 映射到自定义数据表
				tb.initData(cnn.getLastResult().getResultSet());
				if (tb.isOk()) {
					super.getDTTables().add(tb);
					//放到全局参数中
					if (tb.getCount() == 1) {
						super.addDTTableToRequestValue(tb);
					}
				}
			} else if (sqlType.equals("query")) {// 查询
				// 获取不同类型SQL
				sql = this.createSql(sqlArray[i]);
				if (!(sql == null || sql.length() == 0)) {
					cnn.executeQuery(sql);
					DTTable tb = new DTTable(); // 映射到自定义数据表
					tb.initData(cnn.getLastResult().getResultSet());
					if (tb.isOk()) {
						super.getDTTables().add(tb);
						//放到全局参数中
						if (tb.getCount() == 1) {
							super.addDTTableToRequestValue(tb);
						}
					}
				}
			} else if (sqlType.equals("procedure")) {// 存储过程
				super.executeSqlProcdure(sql);
			} else {// 更新
				super.executeSqlUpdate(sql);
			}
			if (cnn.getErrorMsg() != null
					&& cnn.getErrorMsg().length() > 0) {
				if (isTrans) {
					cnn.transRollback();
				}
				cnn.close();
				throw new Exception(cnn.getErrorMsg());
			}
			this.setChkErrorMsg(null);

			MTable pvs = super.getRequestValue().getPageValues().getTagValues(PageValueTag.DTTABLE);
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
			runInc++;
		}
		if (isTrans) {
			cnn.transCommit();
		}
		super.executeSessionsCookies(sqlItem.getUserXItemValue());
	}

	/**
	 * 生成分级加载SQL
	 * 
	 * @param sql
	 * @param u
	 * @return
	 * @throws Exception
	 */
	private String createSqlLoadByLevel(String sql, UserXItemValues u)
			throws Exception {
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName()
				+ " WHERE " + sp.getWhere();
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

	/**
	 * 生成分级加载获取子节点
	 * 
	 * @param sql
	 * @param u
	 * @return
	 * @throws Exception
	 */
	private String createLoadByLevelMore(String sql, UserXItemValues u)
			throws Exception {
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName()
				+ " WHERE " + pkey + "=@" + key;
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

	private String createStatusFindPkey(String sql) {
		super.executeSqlQuery(sql);
		DTTable tb =(DTTable) super.getDTTables().getLast();
		if(tb.getCount()==0){
			return null;
		}
		DTCell cell = tb.getCell(0, 0);
		return cell.toString();
	}

	private String createStatusFindPkeys(String sql, UserXItemValues u)
			throws Exception {
		ArrayList<String> keys = new ArrayList<String>();
		UserXItemValue v = u.getItem(0);
		SqlPart sp = new SqlPart();
		sp.setSql(sql);
		String key = v.getItem("Key");
		String pkey = v.getItem("ParentKey");

		String s1 = "SELECT " + pkey + " FROM " + sp.getTableName() + " WHERE "
				+ key + "='";
		String keyValue = super.getRequestValue().getString(key);
		int m = 0;
		while (keyValue != null) {
			m++;
			if (m > 20) {// 循环加载？
				return null;
			}
			if(keys.contains(keyValue)){
				break;
			}
			keys.add(keyValue);
			String s2 = s1 + keyValue.replace("'", "''") + "'";
			keyValue = this.createStatusFindPkey(s2);
		}
		if(keys.size()<=1){
			return null; //指定的key值不对
		}
		
		StringBuilder s3 = new StringBuilder();
		s3.append("SELECT " + sp.getFields() + " FROM " + sp.getTableName()
				+ " WHERE " + pkey + " IN (");
		for (int i = 0; i < keys.size()-1; i++) {
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
	private String createSql(String sql) throws Exception {
		UserConfig uc = super.getUserConfig();
		UserXItemValues u = uc.getUserPageItem().getItem("Tree");
		RequestValue rv = super.getRequestValue();
		if (u.count() == 0)
			return sql;
		UserXItemValue v = u.getItem(0);
		if (!v.getItem("LoadByLevel").equals("1")) {
			return sql;
		} else if (rv.getString("EWA_TREE_MORE") != null
				&& rv.getString("EWA_TREE_MORE").equals("1")) {
			return this.createLoadByLevelMore(sql, u);
		} else if (rv.getString("EWA_TREE_STATUS") != null
				&& rv.getString("EWA_TREE_STATUS").equals("1")) {
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