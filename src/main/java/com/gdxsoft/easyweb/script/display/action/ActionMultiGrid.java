package com.gdxsoft.easyweb.script.display.action;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;

public class ActionMultiGrid extends ActionBase implements IAction {

	public void executeCallSql(String name) throws Exception {
		UserXItemValues sqlset = super.getUserConfig().getUserActionItem()
				.getItem("SqlSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String sqlExp = sqlItem.getItem("Sql");
		String[] sqlArray = sqlExp.split(";");

//		String transType = sqlItem.getItem("TransType");
//		boolean isTrans = transType.equalsIgnoreCase("yes") ? true : false;
		DataConnection cnn = super.getItemValues().getSysParas().getDataConn();
//		if (isTrans) {
//			cnn.transBegin();
//		}
		int runInc = 0; // 执行次数
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) { // 空语句
				continue;
			}
			String sqlType = sqlItem.getItem("SqlType");
			if (runInc > 0 &&  DataConnection.checkIsSelect(sql)) {
				// 执行过程中有其它Select过程
				super.executeSqlQuery(sql);
			} else if (sqlType.equals("query")) {// 查询
				super.executeSqlQuery(sqlArray[i]);

			} else if (sqlType.equals("procedure")) {// 存储过程
				super.executeSqlProcdure(sql);
			} else {// 更新
				super.executeSqlUpdate(sql);
			}
			if (cnn.getErrorMsg() != null && cnn.getErrorMsg().length() > 0) {
//				if (isTrans) {
//					cnn.transRollback();
//				}
//				cnn.close();
				throw new Exception(cnn.getErrorMsg());
			}
			runInc++;
		}
//		if (isTrans) {
//			cnn.transCommit();
//		}

		this.executeSessionsCookies(sqlItem);
	}
}
