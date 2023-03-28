package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class EwaWfMain {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaWfMain.class);
	/**
	 * 当前节点
	 */
	public static final String UNIT_CUR_ID = "SYS_STA_TAG";
	/**
	 * 下个节点
	 */
	public static final String UNIT_NEXT_ID = "SYS_STA_VAL";
	/**
	 * 来源编号
	 */
	public static final String WF_REF_ID = "SYS_STA_RID";
	/**
	 * 来源
	 */
	public static final String WF_REF_TABLE = "SYS_STA_TABLE";
	/**
	 * 发布版本号
	 */
	public static final String WF_VERSION = "EWA_WF_DLV_VER";
	/**
	 * 是否流程完成标准名称
	 */
	public static final String WF_IS_END = "EWA_WF_IS_END";

	/**
	 * 下个节点标准名称
	 */
	public static final String WF_NEXT = "EWA_WF_NEXT";

	/**
	 * 流程到下一步
	 * 
	 * @param workflowId  工作流编号
	 * @param curUnit     当前节点
	 * @param nextUnit    下一个节点
	 * @param taskPks     任务的主键值
	 * @param appItemname 任务配置信息的ITEMNAME
	 * @param rv          rv
	 * @return
	 */
	public static String flowNext(String workflowId, String curUnit, String nextUnit, String taskPks,
			String appItemname, RequestValue rv) {
		EwaWfMain main = new EwaWfMain();
		JSONObject obj = new JSONObject();
		try {
			main.initDlv(workflowId, rv);
			main.doPost(curUnit, nextUnit, taskPks, appItemname);
			obj.put("RST", true);
			return obj.toString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			try {
				obj.put("RST", false);
				obj.put("ERR", e.getMessage());
				return obj.toString();
			} catch (JSONException e1) {
				LOGGER.error(e1.getMessage());
				return "{\"RST\":false,ERR:\"???\"}";
			}
		}
	}

	public static DTTable getAppTable(String xmlName, String itemName, DataConnection cnn) {
		String sql = "SELECT * FROM _EWA_WF_APP where APP_ITEMNAME=" + cnn.sqlParameterStringExp(itemName);
		DTTable tableApp = DTTable.getJdbcTable(sql, cnn);
		return tableApp;
	}

	private HashMap<String, EwaWfUnit> _Units = new HashMap<String, EwaWfUnit>();
	private HashMap<String, EwaWfCnn> _Cnns = new HashMap<String, EwaWfCnn>();
	private HashMap<Integer, HashMap<String, String>> _LogicTables = new HashMap<Integer, HashMap<String, String>>();
	private EwaWf _Wf;
	// private DTTable _TbDlv; // 流程发布数据

	private EwaWfUnit unitCur;
	private EwaWfUnit unitNext;
	private RequestValue rv;

	private String flowIsEnd;

	/**
	 * 执行提交，rv包含数据：<br>
	 * SYS_STA_TAG 当前节点<br>
	 * SYS_STA_VAL 下个节点<br>
	 * SYS_STA_RID 来源编号<br>
	 * SYS_STA_TABLE 来源<br>
	 * APP_XMLNAME<br>
	 * APP_ITEMNAME<br>
	 * 
	 * @param rv
	 * @throws Exception
	 */
	public void doPost() throws Exception {
		// 当前节点
		String curUnitName = rv.s(UNIT_CUR_ID).trim();
		// 参数下一个节点 SYS_STA_VAL
		String nextUnitName = rv.s(UNIT_NEXT_ID).trim();

		String pks = this.rv.s(WF_REF_ID);
		String appItemname = this.rv.s("APP_ITEMNAME");

		this.doPost(curUnitName, nextUnitName, pks, appItemname);
	}

	/**
	 * 执行下一步
	 * 
	 * @param curUnit  当前节点
	 * @param nextUnit 下一个节点
	 * @param taskPks  任务数据的主键
	 * @param rv       RequestValue
	 * @throws Exception
	 */
	public void doPost(String curUnit, String nextUnit, String taskPks) throws Exception {
		this.doPost(curUnit, nextUnit, taskPks, null);
	}

	/**
	 * 执行下一步
	 * 
	 * @param curUnit     当前节点
	 * @param nextUnit    下一个节点
	 * @param taskPks     任务数据的主键
	 * @param appItemname 任务配置信息的ITEMNAME
	 * @throws Exception
	 */
	public void doPost(String curUnit, String nextUnit, String taskPks, String appItemname) throws Exception {
		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);

		this.initUnitCurAndNext(curUnit, nextUnit);
		// 是否能执行到下一步
		this.checkCanRunByFlow(cnn, unitCur);

		String sql = "SELECT * FROM _EWA_WF_APP where wf_id=" + cnn.sqlParameterStringExp(this._Wf.getWfId());
		DTTable tableApp = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		if (tableApp.getCount() == 0) {
			throw new Exception("获取APP-无数据");
		}
		DTRow rowApp = null;
		if (tableApp.getCount() == 1 && StringUtils.isBlank(appItemname)) {
			rowApp = tableApp.getRow(0);
		} else {
			for (int i = 0; i < tableApp.getCount(); i++) {
				String appItemname0 = tableApp.getCell(i, "APP_ITEMNAME").toString();
				if (appItemname.equalsIgnoreCase(appItemname0)) {
					rowApp = tableApp.getRow(i);
				}
			}
		}
		if (rowApp == null) {
			throw new Exception("获取APP-找不到：" + appItemname);
		}

		// 更新业务表的状态
		String sqlMainStatus = this.createUpdateTaskStatus(rowApp, cnn, taskPks);
		this.rv.addOrUpdateValue(WF_REF_TABLE, this._Wf.getWfId()); // WF_REF_TABLE
		this.rv.addOrUpdateValue(UNIT_CUR_ID, curUnit);
		this.rv.addOrUpdateValue(UNIT_NEXT_ID, nextUnit);
		this.rv.addOrUpdateValue(WF_REF_ID, taskPks);

		// 事物开始
		cnn.transBegin();
		try {
			this.executeFlowData(cnn, sqlMainStatus);
			cnn.transCommit();
		} catch (Exception err) {
			// 回滚
			cnn.transRollback();
			throw err;
		} finally {
			cnn.transClose();
			cnn.close();
		}
	}

	/**
	 * 初始化当前节点和下一个节点
	 * 
	 * @param curUnitName  当前节点
	 * @param nextUnitName 下个节点
	 * @throws Exception
	 */
	private void initUnitCurAndNext(String curUnitName, String nextUnitName) throws Exception {
		if (curUnitName == null) {
			throw new Exception("参数当前节点" + UNIT_CUR_ID + "没有传递");
		}
		unitCur = this._Units.get(curUnitName);
		if (unitCur == null) {
			throw new Exception("参数当前节点" + UNIT_CUR_ID + "不存在");
		}

		// 参数下一个节点 SYS_STA_VAL
		if (nextUnitName == null) {
			throw new Exception("参数下一个节点" + UNIT_CUR_ID + "没有传递");
		}
		unitNext = this._Units.get(nextUnitName);
		if (unitNext == null) {
			throw new Exception("参数下一个节点" + nextUnitName + "不存在");
		}
		// 检查流转的定义
		if (!this.checkFlowValid(unitCur, unitNext)) {
			throw new Exception("没有从" + curUnitName + "到" + nextUnitName + "流转的定义");
		}

		// 查找是否流程完成，完成条件是找不到以下一个节点开始的连接，即有输入无输出
		// 设置标准名称，是否流程完成，用于程序调用
		this.flowIsEnd = this.checkflowEnd(this.unitNext.getWfUnitId()) ? "Y" : null;
		rv.addValue(WF_IS_END, flowIsEnd);

		// 设置标准名称，参数下一个节点，用于程序调用
		rv.addValue(WF_NEXT, this.unitNext.getWfUnitId());

		// 将节点的操作类型和操作者放到参数中，用于程序调用
		rv.addValue("WF_UNIT_NAME", unitNext.getWfUnitName());
		rv.addValue("WF_UNIT_ADM", unitNext.getWfUnitAdm());

		// 部门经理
		if ("WF_ADM_MANAGER".equalsIgnoreCase(unitNext.getWfUnitAdm())) {
			unitNext.setWfUnitAdmLst("0");
		}
		if (unitNext.getWfUnitAdmLst() != null && unitNext.getWfUnitAdmLst().trim().length() > 0) {
			rv.addValue("WF_UNIT_ADM_LST", unitNext.getWfUnitAdmLst());
		}
		// 是否本部门
		rv.addValue("WF_UNIT_SELF_DEPT", unitNext.getWfUnitSelfDept());

	}

	/**
	 * 更新业务表的状态
	 * 
	 * @param tableApp
	 * @param cnn
	 * @return
	 * @throws Exception
	 */
	private String createUpdateTaskStatus(DTRow rowApp, DataConnection cnn, String taskPks) throws Exception {
		String appTable = rowApp.getCell("APP_WF_TABLE").toString();
		String appField = rowApp.getCell("APP_WF_FIELD").toString();
		String appPks = rowApp.getCell("APP_WF_PKS").toString();
		if (appTable == null || appTable.trim().length() == 0) {
			throw new Exception("APP_TABLE未定义");
		}
		if (appField == null || appField.trim().length() == 0) {
			throw new Exception("appField未定义");
		}
		if (appPks == null || appPks.trim().length() == 0) {
			throw new Exception("APP_PKS未定义");
		}
		String[] pks = appPks.split(",");

		// SYS_STA_RID
		String[] vals = taskPks.split(",");
		if (pks.length != vals.length) {
			throw new Exception("APP_PKS定义和参数值不一致" + appPks + "|" + rv.getString(WF_REF_ID));
		}

		if (this._Wf.getWfPara0() == null || this._Wf.getWfPara0().trim().length() == 0) {
			throw new Exception("生成索引数据脚本未定义");
		}

		// 更新业务表的字段状态
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(appTable);
		sb.append(" SET ");
		sb.append(appField);
		sb.append(" = ");
		sb.append(cnn.sqlParameterStringExp(this.unitNext.getWfUnitId().trim()));
		sb.append(" WHERE ");

		for (int i = 0; i < pks.length; i++) {
			String v = vals[i].replace("'", "''").trim();
			sb.append(pks[i]);
			sb.append(" = ");
			sb.append(cnn.sqlParameterStringExp(v));
		}

		String sqlMainStatus = sb.toString();

		return sqlMainStatus;
	}

	/**
	 * 执行流程数据
	 * 
	 * @param cnn
	 * @param tableApp
	 * @throws Exception
	 */
	private void executeFlowData(DataConnection cnn, String sqlMainStatus) throws Exception {
		OrgSqls sqls = OrgSqls.instance();

		// 执行前检查，循环检查返回表，如果有数据表示有错。
		String strBeforeOk = this.runActBefore(cnn, unitCur, rv);
		if (strBeforeOk != null) {
			throw new Exception("不能执行 \n " + strBeforeOk);
		}

		String sqlUpdate = sqls.getSql("WF_LOG_NEW");
		// 更新日志索引
		String sqlIdxCheck = sqls.getSql("WF_LOG_IDX_CHECK");
		DTTable tbIdxCheck = this.executeAct(cnn, sqlIdxCheck).get(0);

		// 更新业务表的状态
		this.executeAct(cnn, sqlMainStatus);

		// 写日志
		ArrayList<DTTable> al = this.executeAct(cnn, sqlUpdate);
		int sysStaId = Integer.parseInt(al.get(0).getCell(0, 0).toString());

		rv.addValue("EWA_WF_LOG_ID", sysStaId);

		// 写日志索引
		if (tbIdxCheck.getCount() == 0) {
			String sqlIdxNew = sqls.getSql("WF_LOG_IDX_NEW").replace("[LOG_SQL]", this._Wf.getWfPara0());
			this.executeAct(cnn, sqlIdxNew);

		}

		// 更新日志状态
		String sqlLogUpdate = sqls.getSql("WF_LOG_IDX_UPDATE");

		this.executeAct(cnn, sqlLogUpdate);
		this.runActAfter(cnn, unitCur, rv);

		this.notification(rv, unitCur, unitNext, flowIsEnd);

	}

	/**
	 * 检查是否可以执行到下一步
	 * 
	 * @param cnn
	 * @param unitCur
	 * @throws Exception
	 */
	private void checkCanRunByFlow(DataConnection cnn, EwaWfUnit unitCur) throws Exception {

		OrgSqls o = OrgSqls.instance();
		String sql = o.getSql("WF_LOG_GET");

		DTTable table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		String startAdmin = null;
		if (table.getCount() > 0) {
			startAdmin = table.getCell(0, "adm_id").toString();
		}
		// 检查是否可流转
		boolean isCanRun = false;
		isCanRun = this.checkIsCanRun1(unitCur.getWfUnitAdm(), unitCur.getWfUnitAdmLst(), unitCur.getWfUnitSelfDept(),
				startAdmin, cnn);

		if (isCanRun) {
			cnn.close();
			return;
		}

		StringBuilder err1 = new StringBuilder();
		err1.append("审批权限不对：");
		if ("WF_ADM_POST".equalsIgnoreCase(unitCur.getWfUnitAdm())) {
			String sqlPost = "select dep_pos_id, dep_pos_name, dep_pos_name_en from adm_r_dept_post where dep_pos_id in  ("
					+ unitCur.getWfUnitAdmLst() + ")";

			DTTable tbPost = DTTable.getJdbcTable(sqlPost, cnn);
			if (tbPost.getCount() == 0) {
				err1.append("<br>没有岗位数据，其定义为: (").append(unitCur.getWfUnitAdmLst()).append(")");
			} else {
				String allowPosts = tbPost.joinIds("dep_pos_name", false);
				String posts = tbPost.joinIds("dep_pos_id", false);

				// 岗位关联的用户
				String sqlAdms = "select distinct  b.ADM_NAME  from adm_r_udp a \n"
						+ "inner join adm_user b on a.ADM_ID = b.ADM_ID and ADM_USR_STA_TAG='OK'"
						+ " and a.dep_pos_id in (" + posts + ")";
				DTTable tbAdms = DTTable.getJdbcTable(sqlAdms, cnn);
				String allowAdms = tbAdms.joinIds("adm_name", false);
				err1.append("<br>应为岗位：").append(allowPosts).append("<br>用户为：").append(allowAdms);
			}

		} else if ("WF_ADM_POST".equalsIgnoreCase(unitCur.getWfUnitAdm())) {
			String sqlAdms = "select adm_id, adm_name from adm_user where sup_id=@g_sup_id and adm_id in ("
					+ unitCur.getWfUnitAdmLst() + ")";
			DTTable tbAdms = DTTable.getJdbcTable(sqlAdms, cnn);
			String allowAdms = tbAdms.joinIds("adm_name", false);
			err1.append("<br>应为用户：").append(allowAdms);
		} else if ("WF_ADM_DEPT".equalsIgnoreCase(unitCur.getWfUnitAdm())) {
			String sqlDept = "select dep_id, dep_name from adm_dept where sup_id=@g_sup_id and dep_id in ("
					+ unitCur.getWfUnitAdmLst() + ")";
			DTTable tbDept = DTTable.getJdbcTable(sqlDept, cnn);
			String allowDept = tbDept.joinIds("adm_name", false);
			err1.append("<br>应为部门：").append(allowDept);
		} else if ("WF_ADM_START".equalsIgnoreCase(unitCur.getWfUnitAdm())) {
			// 启动者
			String sqlAdms = "select adm_id, adm_name from adm_user where sup_id=@g_sup_id and adm_id=" + startAdmin;
			DTTable tbAdms = DTTable.getJdbcTable(sqlAdms, cnn);
			String allowAdms = tbAdms.joinIds("adm_name", false);
			err1.append("<br>应为启动者：").append(allowAdms);
		} else if ("WF_ADM_MANAGER".equalsIgnoreCase(unitCur.getWfUnitAdm())) {
			err1.append("<br>应为部门经理");
		}
		if ("Y".equalsIgnoreCase(unitCur.getWfUnitSelfDept())) {
			err1.append("<br>同时须是本部门");
		}
		cnn.close();

//		err1.append("审批权限不对：WfUnitAdm = ");
//		err1.append(unitCur.getWfUnitAdm());
//		err1.append(", WfUnitAdmLst = ");
//		err1.append(unitCur.getWfUnitAdmLst());
//		err1.append(", WfUnitSelfDept =");
//		err1.append(unitCur.getWfUnitSelfDept());
//		err1.append(", startAdmin =");
//		err1.append(startAdmin);
		String msg = err1.toString();

		LOGGER.error(msg);

		throw new Exception(msg);
	}

	/**
	 * 查找是否流程完成，完成条件是找不到以下一个节点开始的连接，即有输入无输出
	 * 
	 * @param curUnitNext
	 * @return
	 */
	private boolean checkflowEnd(String curUnitNext) {
		String check = curUnitNext + "|";
		// 查找是否流程完成，完成条件是找不到以下一个节点开始的连接，即有输入无输出
		Iterator<String> it = this._Cnns.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key.indexOf(check) == 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 执行工作流的通知接口，定义在 _EWA_WF.WF_PARA2，类是接口IEwaWfNotification的实现
	 * 
	 * @param rv
	 * @param unitCur
	 * @param unitNext
	 */
	private void notification(RequestValue rv, EwaWfUnit unitCur, EwaWfUnit unitNext, String flowIsEnd) {
		if (this._Wf.getWfPara2() == null || this._Wf.getWfPara2().trim().length() == 0) {
			return;
		}
		if (unitNext.getWfUnitNotify() == null || !unitNext.getWfUnitNotify().toUpperCase().trim().equals("Y")) {
			return;
		}

		String className = this._Wf.getWfPara2().trim();
		UObjectValue uo = new UObjectValue();
		Object notificationClass = uo.loadClass(className, null);
		if (notificationClass == null) {
			LOGGER.error(uo.getLastErrMsg());
			return;
		}

		try {
			IEwaWfNotification impl = (IEwaWfNotification) notificationClass;
			impl.notification(rv, this.getWf(), unitCur, unitNext);
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}
	}

	/**
	 * 检查流转合法性
	 * 
	 * @param unitCur
	 * @param unitNext
	 * @return
	 */
	private boolean checkFlowValid(EwaWfUnit unitCur, EwaWfUnit unitNext) {
		// boolean is_back_router = false;
		// 检查流转是否合法
		String cnnKey = unitCur.getWfUnitId() + "|" + unitNext.getWfUnitId();
		if (this._Cnns.containsKey(cnnKey)) {
			// 正向流转
			return true;
		}
		// 打回操作
		HashMap<EwaWfUnit, Boolean> al_from_units = new HashMap<EwaWfUnit, Boolean>();
		this.getAllFroms(unitCur, al_from_units);
		for (EwaWfUnit u : al_from_units.keySet()) {
			if (u.getWfUnitId().equals(unitNext.getWfUnitId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 查找所有来源
	 * 
	 * @param unitCur
	 * @param al_from_units
	 */
	private void getAllFroms(EwaWfUnit unitCur, HashMap<EwaWfUnit, Boolean> al_from_units) {
		ArrayList<EwaWfCnn> cnns = unitCur.getToCnns();
		for (int i = 0; i < cnns.size(); i++) {
			EwaWfCnn cnn = cnns.get(i);
			String from = cnn.getWfUnitFrom();
			EwaWfUnit unitFrom = this._Units.get(from);
			if (!al_from_units.containsKey(unitFrom)) {
				al_from_units.put(unitFrom, true);
				this.getAllFroms(unitFrom, al_from_units);
			}
		}
	}

	/**
	 * 执行后处理
	 * 
	 * @param conn
	 * @param curUnit
	 * @param rv
	 * @throws Exception
	 */
	private void runActAfter(DataConnection conn, EwaWfUnit curUnit, RequestValue rv) throws Exception {
		// 执行对应的编号（WF_FUC_ID）
		String fucId = curUnit.getWfUnitPara3();
		if (fucId == null || fucId.trim().length() == 0) {
			return;
		}
		rv.addOrUpdateValue("WF_FUC_ID_AFTER", fucId);

		StringBuilder sb = new StringBuilder();
		sb.append("-- 执行后对处理，对应的编号 WF_FUC_ID: " + fucId.replace("@", "at") + " \n");
		sb.append("SELECT WF_FUC_SQL,WF_FUC_ID, WF_ID, WF_FUC_NAME, WF_FUC_MEMO, WF_FUC_TAG ");
		sb.append(" FROM _EWA_WF_FUNC WHERE WF_FUC_TAG='WF_ACT_AFT' AND WF_ID=@WF_ID");
		sb.append(" and WF_FUC_ID = @WF_FUC_ID_AFTER");
		String sql = sb.toString();
		DTTable tbActions = DTTable.getJdbcTable(sql, "", rv);

		// no check
		if (tbActions.getCount() == 0) {
			return;
		}

		for (int i = 0; i < tbActions.getCount(); i++) {
			sql = tbActions.getRow(i).getCell("WF_FUC_SQL").toString();
			executeAct(conn, sql);
		}
	}

	/**
	 * 执行前检查，循环检查返回表，如果有数据表示有错。
	 * 
	 * @param conn
	 * @param curUnit 当前的节点
	 * @param rv
	 * @return
	 * @throws Exception
	 */
	private String runActBefore(DataConnection conn, EwaWfUnit curUnit, RequestValue rv) throws Exception {
		// 执行前检查对应的编号（WF_FUC_ID）
		String fucId = curUnit.getWfUnitPara4();
		if (fucId == null || fucId.trim().length() == 0) {
			return null;
		}

		rv.addOrUpdateValue("WF_FUC_ID_BEFORE", fucId);

		StringBuilder sb1 = new StringBuilder();
		sb1.append("-- 执行前检查，对应的编号 WF_FUC_ID: " + fucId.replace("@", "at") + " \n");
		sb1.append("SELECT WF_FUC_SQL,WF_FUC_ID, WF_ID, WF_FUC_NAME, WF_FUC_MEMO, WF_FUC_TAG \n");
		sb1.append("FROM _EWA_WF_FUNC WHERE WF_FUC_TAG = 'WF_ACT_BEF' \n");
		sb1.append(" 	AND WF_ID = @WF_ID \n");
		sb1.append(" 	AND WF_FUC_ID = @WF_FUC_ID_BEFORE");
		String sql = sb1.toString();

		DTTable tbActions = DTTable.getJdbcTable(sql, "", rv);

		// no check
		if (tbActions.getCount() == 0) {
			return null;
		}

		for (int i = 0; i < tbActions.getCount(); i++) {
			sql = tbActions.getRow(i).getCell("WF_FUC_SQL").toString();
			ArrayList<DTTable> al = executeAct(conn, sql);
			if (al.size() == 0) {
				throw new Exception("执行前检查必须返回至少一个表");
			}

			MStr sb = new MStr();
			int inc = 1;
			// 循环检查返回表，如果有数据表示有错。
			for (int m = 0; m < al.size(); m++) {
				DTTable tb = al.get(m);
				for (int k = 0; k < tb.getCount(); k++) {
					String err = tb.getCell(k, 0).toString();
					if (err == null) {
						err = "NULL";
					}
					sb.a("<br>" + inc + ". " + err);
					inc++;
				}
			}
			if (sb.length() > 0) {
				return sb.toString(); // 有数据表示错误
			}
		}
		return null;
	}

	/**
	 * 执行Action
	 * 
	 * @param conn
	 * @param sql
	 * @return 所有的表
	 * @throws Exception
	 */
	private ArrayList<DTTable> executeAct(DataConnection conn, String sql) throws Exception {
		String[] sqls = sql.split(";");
		ArrayList<DTTable> al = new ArrayList<DTTable>();
		for (int i = 0; i < sqls.length; i++) {
			String s = sqls[i].trim();
			if (s.length() == 0) {
				continue;
			}
			// s = conn.rebuildSql(s);
			if (DataConnection.checkIsSelect(s)) {
				DTTable tb = DTTable.getJdbcTable(s, conn);
				if (tb != null && tb.isOk()) {
					al.add(tb);
				}
				if (tb.getCount() == 1) {
					if(tb.getColumns().testName(WF_REF_ID)) {//SYS_STA_RID
						// 避免和参数"SYS_STA_RID"冲突
					} else {
						conn.getRequestValue().addValues(tb);
					}
				}
			} else {
				conn.executeUpdate(s);
			}
			if (conn.getErrorMsg() != null) {
				throw new Exception("执行" + s + "错误，" + conn.getErrorMsg());
			}
		}
		return al;

	}

	/**
	 * 执行版本
	 * 
	 * @param rv
	 * @return
	 * @throws Exception
	 */
	public String doGetStatusDlv(RequestValue rv) throws Exception {
		// status
		OrgSqls o = OrgSqls.instance();

		StringBuilder sb = new StringBuilder();

		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);
		int version = -1;

		if (rv.getString(WF_VERSION) == null) {
			String sqlVerGet = o.getSql("WF_LOG_VER_GET");
			DTTable tbDlvVer = DTTable.getJdbcTable(sqlVerGet, cnn);

			if (tbDlvVer.getCount() > 0) {
				try {
					version = Integer.parseInt(tbDlvVer.getCell(0, 0).toString());
				} catch (Exception err) {
					System.err.println("流程：Version, " + err.getMessage());
				}
			}
		} else {
			version = rv.getInt(WF_VERSION);
		}
		String sql = "SELECT * FROM _EWA_WF_DLV WHERE WF_ID=@WF_ID AND (WF_REF_ID=@G_SUP_UNID  )";
		if (version > 0) {
			sql += " AND DLV_VER = " + version;
		} else {
			sql += " AND DLV_CUR='Y' ";
		}
		DTTable tbDlv = DTTable.getJdbcTable(sql, cnn);

		if (tbDlv.getCount() == 0) {
			throw new Exception("未找到对应的流程数据");
		}

		this.logic(cnn);

		sb.insert(0, "{\"CNN\":");
		sb.append(tbDlv.getCell(0, "DLV_JSON_CNN").toString());
		sb.append(", \n \"UNIT\":");
		sb.append(tbDlv.getCell(0, "DLV_JSON_UNIT").toString());

		// 英文节点名称 2016-10-27
		String sqlEn = "SELECT WF_UNIT_ID, WF_UNIT_NAME_EN,WF_UNIT_MEMO_EN FROM _ewa_wf_unit WHERE WF_ID=@WF_ID AND (WF_REF_ID=@G_SUP_UNID  )";
		DTTable tbEN = DTTable.getJdbcTable(sqlEn, cnn);

		sb.append(", \n \"EN\":");
		sb.append(tbEN.toJson(rv));

		sb.append(", \n \"ST\":");
		sql = o.getSql("WF_LOG_GET");

		if (StringUtils.isBlank(sql)) {
			LOGGER.error("找不到WF_LOG_GET在ewa_conf.xml的工作流配置中");
			throw new Exception("Can't found WF_LOG_GET node in the ewa_conf.xml's workflow");
		}

		DTTable table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		String j = table.toJson(rv);
		sb.append(j);

		/*
		 * 角色数据 sb.append(", \n \"ROLES\":"); sql = o.getSql("WF_ROLE"); table =
		 * DTTable.getJdbcTable(sql, cnn); cnn.close();
		 * 
		 * sb.append(table.toJson(rv));
		 */

		sb.append(", \n  \"WF_CUR\":\"" + Utils.textToJscript(rv.getString("APP_WF_UNIT_CUR")) + "\"");
		sb.append(", \n  \"WF_DLV_VER\":\"" + version + "\"");

		sql = "SELECT  XITEMS FROM _EWA_WF_SHOW WHERE  1=1 " + " AND WF_ID=@WF_ID "
				+ " AND ( WF_REF_ID=@G_SUP_UNID) AND WF_UNIT_ID=@APP_WF_UNIT_CUR";

		DTTable tableSHOW = DTTable.getJdbcTable(sql, cnn);

		sb.append(", \n  \"WF_SHOW\":" + tableSHOW.toJson(rv) + "");
		sb.append("}");
		cnn.close();

		JSONObject wf = new JSONObject(sb.toString());

		this.checkIsCanRun(wf, cnn);
		cnn.close();

		return "_EWA_WF=" + wf.toString();
	}

	private void checkIsCanRun(JSONObject wf, DataConnection cnn) {
		String wf_cur = wf.optString("WF_CUR");
		JSONObject wf_cur_unit = null;
		JSONArray units = wf.getJSONArray("UNIT");
		for (int i = 0; i < units.length(); i++) {
			JSONObject unit = units.getJSONObject(i);
			String WF_UNIT_ID = unit.optString("WF_UNIT_ID");
			if (wf_cur.equals(WF_UNIT_ID)) {
				wf_cur_unit = unit;
				break;
			}
		}

		if (wf_cur_unit == null) {
			return;
		}
		wf_cur_unit.put("IS_CAN_RUN", false);

		String WF_UNIT_ADM = wf_cur_unit.optString("WF_UNIT_ADM"); // 审批人角色
		String WF_UNIT_ADM_LST = wf_cur_unit.optString("WF_UNIT_ADM_LST"); // 审批人列表
		String WF_UNIT_SELF_DEPT = wf_cur_unit.optString("WF_UNIT_SELF_DEPT"); // 是否上下级关系
		JSONArray sts = wf.getJSONArray("ST");
		String startAdmId = null;
		if (sts.length() > 0) {
			JSONObject st = sts.getJSONObject(0);
			startAdmId = st.optString("ADM_ID");
		}
		boolean is_can_run = this.checkIsCanRun1(WF_UNIT_ADM, WF_UNIT_ADM_LST, WF_UNIT_SELF_DEPT, startAdmId, cnn);
		wf_cur_unit.put("IS_CAN_RUN", is_can_run);

		cnn.close();
	}

	/**
	 * 检查流转是否合法
	 * 
	 * @param WF_UNIT_ADM
	 * @param WF_UNIT_ADM_LST
	 * @param WF_UNIT_SELF_DEPT
	 * @param startAdmin
	 * @param cnn
	 * @return
	 */
	private boolean checkIsCanRun1(String WF_UNIT_ADM, String WF_UNIT_ADM_LST, String WF_UNIT_SELF_DEPT,
			String startAdmin, DataConnection cnn) {
		String sqlAdms = "select adm_id, adm_name from adm_user where sup_id=@g_sup_id and adm_id=@g_adm_id and adm_id in ";
		String sqlDept = "select dep_id, dep_name from adm_dept where sup_id=@g_sup_id and dep_id=@g_dep_id and dep_id in ";
		String sqlPost = "select dep_pos_id from adm_r_udp where (adm_id=@G_ADM_ID) and dep_pos_id in  ";

		StringBuilder sb1 = new StringBuilder();
		if (WF_UNIT_ADM == null || WF_UNIT_ADM.trim().length() == 0 || WF_UNIT_ADM.trim().equals("null")) {
			// 没有指定权限类型
			return true;
		} else if (WF_UNIT_ADM.equals("WF_ADM_ADM")) { // 人员
			if (WF_UNIT_ADM_LST != null && WF_UNIT_ADM_LST.trim().length() > 0) {
				sb1.append(sqlAdms);
				sb1.append(" (");
				sb1.append(WF_UNIT_ADM_LST);
				sb1.append(")");

				DTTable tb = DTTable.getJdbcTable(sb1.toString(), cnn);
				if (tb.getCount() > 0) {
					return true;
				}
			}
		} else if (WF_UNIT_ADM.equals("WF_ADM_DEPT")) { // 部门
			if (WF_UNIT_ADM_LST != null && WF_UNIT_ADM_LST.trim().length() > 0) {
				sb1.append(sqlDept);
				sb1.append(" (");
				sb1.append(WF_UNIT_ADM_LST);
				sb1.append(")");
				DTTable tb = DTTable.getJdbcTable(sb1.toString(), cnn);
				if (tb.getCount() > 0) {
					return true;
				}
			}
		} else if (WF_UNIT_ADM.equals("WF_ADM_POST")) { // 岗位
			if (WF_UNIT_ADM_LST != null && WF_UNIT_ADM_LST.trim().length() > 0) {
				sb1.append(sqlPost);
				sb1.append(" (");
				sb1.append(WF_UNIT_ADM_LST);
				sb1.append(")");
				DTTable tb = DTTable.getJdbcTable(sb1.toString(), cnn);
				if (tb.getCount() > 0) {
					return true;
				}
			}
		} else if (WF_UNIT_ADM.equals("WF_ADM_MANAGER")) { // 部门经理
			if (startAdmin != null) {
				startAdmin = startAdmin.replace("'", "''");
				if (WF_UNIT_SELF_DEPT == null || WF_UNIT_SELF_DEPT.endsWith("Y")) {// 默认本部门
					sb1.append("SELECT A.* FROM ADM_R_DEPT_POST a\n");
					sb1.append(" inner join adm_post b on a.pos_id=b.pos_id and pos_is_master='Y'\n");
					sb1.append(" INNER JOIN ADM_R_UDP C ON A.DEP_POS_ID=C.DEP_POS_ID\n");
					sb1.append(" inner join adm_user d on a.dep_id=d.adm_dep_id and d.adm_id='" + startAdmin + "'");
					sb1.append(" where C.ADM_ID= @g_adm_id ");
					DTTable tb = DTTable.getJdbcTable(sb1.toString(), cnn);
					if (tb.getCount() > 0) {
						return true;
					}
				} else {

				}
			}
		} else if (WF_UNIT_ADM.equals("WF_ADM_START")) { // 启动着
			if (startAdmin == null) { // 没有开始人员，是第一个节点
				return true;
			}
			if (startAdmin != null && startAdmin.equals(cnn.getRequestValue().s("G_ADM_ID"))) {
				return true;
			}
		}

		return false;
	}

	public String doGetStatus(RequestValue rv) throws Exception {
		StringBuilder sb = new StringBuilder();

		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);
		this.logic(cnn);

		sb.insert(0, "_EWA_WF={\"CNN\":[");
		Iterator<String> it = this._Cnns.keySet().iterator();
		int inc = 0;
		while (it.hasNext()) {
			String key = it.next();
			String json = this._Cnns.get(key).toJSON().toString();
			if (inc > 0) {
				sb.append(", \n ");
			}
			sb.append(json);
			inc++;
		}
		sb.append("], \n \"UNIT\":[");

		it = this._Units.keySet().iterator();
		inc = 0;
		while (it.hasNext()) {
			String key = it.next();
			String json = this._Units.get(key).toJson();
			if (inc > 0) {
				sb.append(", \n ");
			}
			sb.append(json);
			inc++;
		}
		sb.append("], \n \"ST\":");

		// status
		OrgSqls o = OrgSqls.instance();
		String sql = o.getSql("WF_LOG_GET");

		DTTable table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		String j = table.toJson(rv);
		sb.append(j);

		//
		// // logic
		// sql = "SELECT * FROM _EWA_WF_UNIT_LOGIC WHERE WF_ID=@WF_ID";
		// DTTable table1 = DTTable.getJdbcTable(sql, cnn);
		// cnn.close();
		//
		// sb.append(", \n LG:");
		// String logicJson = table1.toJson(rv);
		// sb.append(logicJson);
		sb.append(", \n \"ROLES\":");
		sql = o.getSql("WF_ROLE");
		table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		sb.append(table.toJson(rv));

		sb.append(", \n  \"WF_CUR\":\"" + Utils.textToJscript(rv.getString("APP_WF_UNIT_CUR")) + "\"");
		sb.append("}");
		return sb.toString();
	}

	void logic(DataConnection cnn) throws Exception {
		if (cnn.getRequestValue().getString("APP_WF_UNIT_CUR") == null) {
			return;
		}
		String curUnit = cnn.getRequestValue().getString("APP_WF_UNIT_CUR");

		EwaWfUnit unitCur = this._Units.get(curUnit);
		String sql = "SELECT * FROM _EWA_WF_UNIT_LOGIC WHERE WF_ID=@WF_ID AND WF_UNIT_ID='" + curUnit.replace("'", "''")
				+ "'";
		DTTable table1 = DTTable.getJdbcTable(sql, cnn);
		cnn.close();
		if (table1.getCount() == 0) {
			return;
		}

		for (int i = 0; i < table1.getCount(); i++) {
			this.logic(cnn, table1.getRow(i), unitCur);

		}
	}

	void logic(DataConnection cnn, DTRow r, EwaWfUnit unitCur) throws Exception {
		String sql = r.getCell("WF_LG_EXP").toString();
		int code = sql.toUpperCase().trim().hashCode();
		HashMap<String, String> map;

		if (this._LogicTables.containsKey(code)) {
			map = this._LogicTables.get(code);
		} else {
			ArrayList<DTTable> al = this.executeAct(cnn, sql);
			if (al.size() == 0) {
				return;
			}

			DTTable tb = al.get(0);
			map = new HashMap<String, String>();
			for (int i = 0; i < tb.getColumns().getCount(); i++) {
				DTColumn col = tb.getColumns().getColumn(i);
				String key = col.getName().toUpperCase().trim();
				if (map.containsKey(key)) {
					continue;
				}

				map.put(key, tb.getCell(0, col.getIndex()).toString());
			}
			this._LogicTables.put(code, map);
		}

		String lgExp = r.getCell("WF_LG_VAL").toString();
		String toId = r.getCell("WF_UNIT_TO").toString();
		MListStr alParas = Utils.getParameters(lgExp, "@");

		for (int i = 0; i < alParas.size(); i++) {
			String p = alParas.get(i);
			String v = null;
			if (map.containsKey(p.toUpperCase().trim())) {
				v = map.get(p.toUpperCase().trim());
			} else {
				v = cnn.getRequestValue().getString(v);
			}
			v = v == null ? "" : v;
			lgExp = lgExp.replace("@" + p, v);

		}

		boolean rst = ULogic.runLogic(lgExp);

		for (int i = 0; i < unitCur.getFromCnns().size(); i++) {
			EwaWfCnn conn = unitCur.getFromCnns().get(i);
			if (conn.getWfUnitTo().equals(toId) && rst) {
				conn.setIsLogicOk(true);
			}
			conn.setIsHaveLogic(true);
		}

	}

	/**
	 * 正式执行的 发布后的初始化
	 * 
	 * @param wfId
	 * @param version
	 * @param rv
	 * @throws Exception
	 */
	public void initDlv(String wfId, RequestValue rv) throws Exception {
		this.rv = rv;
		EwaWfDao daoWf = new EwaWfDao();
		_Wf = daoWf.getRecord(wfId);
		int version = -1;
		OrgSqls o = OrgSqls.instance();

		String sqlVerGet = o.getSql("WF_LOG_VER_GET");
		DTTable tbDlvVer = DTTable.getJdbcTable(sqlVerGet, "", rv);

		if (tbDlvVer.getCount() > 0) {
			try {
				version = Integer.parseInt(tbDlvVer.getCell(0, 0).toString());
			} catch (Exception err) {
				System.err.println("流程：Version, " + err.getMessage());
			}
		}

		String sql = "SELECT * FROM _EWA_WF_DLV WHERE WF_ID='" + wfId.replace("'", "''")
				+ "' AND WF_REF_ID=@G_SUP_UNID ";
		if (version > 0) {
			sql += " AND DLV_VER = " + version;
		} else {
			sql += " AND DLV_CUR='Y' ";
		}
		DTTable tbDlv = DTTable.getJdbcTable(sql, "", rv);
		if (!tbDlv.isOk() || tbDlv.getCount() == 0) {
			String ss = "未发现当前的流程版本《" + wfId + "》";
			LOGGER.error(ss);

			throw new Exception(ss);
		}

		// _TbDlv = tbDlv;

		DTRow dlvRow = tbDlv.getRow(0);

		// 添加版本号
		rv.addValue(WF_VERSION, dlvRow.getCell("DLV_VER").toString());

		EwaWfCnnDao daoCnn = new EwaWfCnnDao();
		EwaWfUnitDao daoUnit = new EwaWfUnitDao();
		ArrayList<EwaWfUnit> alUnits = daoUnit.getUnitsByJSon(dlvRow.getCell("DLV_JSON_UNIT").toString());
		ArrayList<EwaWfCnn> alCnns = daoCnn.getCnnsByJSon(dlvRow.getCell("DLV_JSON_CNN").toString());

		this.init1(alUnits, alCnns);
	}

	public void init(String wfId) {
		EwaWfDao daoWf = new EwaWfDao();
		_Wf = daoWf.getRecord(wfId);

		EwaWfUnitDao daoUnit = new EwaWfUnitDao();
		ArrayList<EwaWfUnit> alUnits = daoUnit.getRecords(" WF_ID='" + wfId.replace("'", "''") + "' AND REF_ID='");

		EwaWfCnnDao daoCnn = new EwaWfCnnDao();

		ArrayList<EwaWfCnn> alCnns = daoCnn.getRecords(" WF_ID='" + wfId.replace("'", "''") + "' AND ");

		this.init1(alUnits, alCnns);

	}

	public void init(String wfId, String refId) {
		EwaWfDao daoWf = new EwaWfDao();
		_Wf = daoWf.getRecord(wfId);

		EwaWfUnitDao daoUnit = new EwaWfUnitDao();
		ArrayList<EwaWfUnit> alUnits = daoUnit.getRecords(
				" WF_ID='" + wfId.replace("'", "''") + "' AND WF_REF_ID='" + refId.trim().replace("'", "''") + "'");

		EwaWfCnnDao daoCnn = new EwaWfCnnDao();

		ArrayList<EwaWfCnn> alCnns = daoCnn.getRecords(
				" WF_ID='" + wfId.replace("'", "''") + "' AND WF_REF_ID='" + refId.trim().replace("'", "''") + "'");

		this.init1(alUnits, alCnns);

	}

	private void init1(ArrayList<EwaWfUnit> alUnits, ArrayList<EwaWfCnn> alCnns) {
		for (int i = 0; i < alUnits.size(); i++) {
			EwaWfUnit unit = alUnits.get(i);
			_Units.put(unit.getWfUnitId().trim(), unit);
		}

		for (int i = 0; i < alCnns.size(); i++) {
			EwaWfCnn cnn = alCnns.get(i);
			String from = cnn.getWfUnitFrom().trim();
			String to = cnn.getWfUnitTo().trim();
			_Cnns.put(from + "|" + to, cnn);

			if (_Units.containsKey(from)) {
				_Units.get(from).getFromCnns().add(cnn);
			}
			if (_Units.containsKey(to)) {
				_Units.get(to).getToCnns().add(cnn);
			}
		}
	}

	public String delivedAll(String refId) {
		String sql = "SELECT WF_ID,WF_NAME FROM _EWA_WF";
		DTTable tb = DTTable.getJdbcTable(sql, "");
		MStr s = new MStr();
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			boolean rst = this.delived(r.getCell(0).toString(), refId);
			s.al(r.getCell(1).toString() + ", " + rst);
		}
		LOGGER.info(s.toString());
		return s.toString();
	}

	public String delivedAll(String refId, String targetDatabaseName) {
		String sql = "SELECT WF_ID,WF_NAME FROM _EWA_WF";
		DTTable tb = DTTable.getJdbcTable(sql, "");
		MStr s = new MStr();
		for (int i = 0; i < tb.getCount(); i++) {
			DTRow r = tb.getRow(i);
			boolean rst = this.delived(r.getCell(0).toString(), refId, targetDatabaseName);
			s.al(r.getCell(1).toString() + ", " + rst);
		}
		LOGGER.info(s.toString());
		return s.toString();
	}

	/**
	 * 发布版本
	 */
	public boolean delived(String wfId, String refId, String targetDatabaseName) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");

		RequestValue rv = new RequestValue();
		rv.addValue("ref_id", refId);
		rv.addValue("WF_ID", wfId);
		cnn.setRequestValue(rv);

		String dbName = "";
		if (targetDatabaseName != null && targetDatabaseName.trim().length() > 0) {
			if (SqlUtils.isSqlServer(cnn)) {
				dbName = targetDatabaseName + "..";
			} else {
				dbName = targetDatabaseName + ".";
			}
		}
		// System.out.println("WF_ID=" + wfId);
		this.init(wfId, refId);

		// 获取所有单元，按照id排序
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT A.WF_UNIT_ID FROM _EWA_WF_UNIT A");
		sb.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@REF_ID ");
		sb.append(" WHERE A.WF_ID=@WF_ID");
		sb.append(" ORDER BY WF_UNIT_ID");
		String sql1 = sb.toString();
		DTTable tbUnitCode = DTTable.getJdbcTable(sql1, cnn);
		String jsonUnit = tbUnitCode.toJson(rv);
		String unitCode = jsonUnit.hashCode() + "";

		// 获取所有连接，按照form，to排序
		StringBuilder sb2a = new StringBuilder();
		sb2a.append("SELECT WF_UNIT_FROM, WF_UNIT_TO FROM _EWA_WF_CNN A");
		sb2a.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@REF_ID ");
		sb2a.append(" WHERE A.WF_ID=@WF_ID");
		sb2a.append(" ORDER BY WF_UNIT_FROM, WF_UNIT_TO");
		String sql2 = sb2a.toString();

		DTTable tbCnnCode = DTTable.getJdbcTable(sql2, cnn);
		String jsonCnn = tbCnnCode.toJson(rv);
		String cnnCode = jsonCnn.hashCode() + "";

		// 生成验证码，如果节点数量和编号未变化+ 连接数量和前后关系没变化
		String code = unitCode + "|" + cnnCode;

		StringBuilder sbUnit = new StringBuilder();
		sbUnit.append("SELECT A.* FROM _EWA_WF_UNIT A  \n");
		sbUnit.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@REF_ID  \n");
		sbUnit.append(" WHERE A.WF_ID = @WF_ID \n");
		sbUnit.append("  ORDER BY WF_UNIT_ID");
		sql1 = sbUnit.toString();
		DTTable tbUnit = DTTable.getJdbcTable(sql1, cnn);

		StringBuilder sbHis = new StringBuilder();
		sbHis.append("INSERT INTO _EWA_WF_UNIT_HIS(WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, WF_UNIT_MEMO)  \n");
		sbHis.append("	SELECT WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, WF_UNIT_MEMO FROM _EWA_WF_UNIT A  \n");
		sbHis.append(" WHERE NOT EXISTS(  \n");
		sbHis.append("	SELECT * FROM _EWA_WF_UNIT_his b where a.wf_id=b.wf_id  \n");
		sbHis.append("		and a.wf_unit_id=b.wf_unit_id  \n");
		sbHis.append("		 and a.wf_ref_id=b.wf_ref_id  \n");
		sbHis.append(") AND A.WF_REF_ID=@REF_ID AND A.WF_ID=@WF_ID \n; ");
		if (SqlUtils.isSqlServer(cnn)) {
			sbHis.append("UPDATE _EWA_WF_UNIT_HIS SET  \n");
			sbHis.append("		_EWA_WF_UNIT_HIS.WF_UNIT_NAME=A.WF_UNIT_NAME,  \n");
			sbHis.append("		_EWA_WF_UNIT_HIS.WF_UNIT_MEMO=A.WF_UNIT_MEMO  \n");
			sbHis.append("FROM _EWA_WF_UNIT A  \n");
			sbHis.append("WHERE _EWA_WF_UNIT_HIS.WF_ID=A.WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_UNIT_ID=A.WF_UNIT_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=A.WF_REF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_ID = @WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=@REF_ID");
		} else {
			sbHis.append("UPDATE _EWA_WF_UNIT_HIS INNER JOIN _EWA_WF_UNIT A \n");
			sbHis.append(" ON _EWA_WF_UNIT_HIS.WF_ID=A.WF_ID \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_UNIT_ID=A.WF_UNIT_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=A.WF_REF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_ID = @WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID = @REF_ID  \n");
			sbHis.append("SET _EWA_WF_UNIT_HIS.WF_UNIT_NAME=A.WF_UNIT_NAME, \n");
			sbHis.append("	_EWA_WF_UNIT_HIS.WF_UNIT_MEMO=A.WF_UNIT_MEMO \n");
		}
		String sqlUnitHis = sbHis.toString();

		StringBuilder sb2 = new StringBuilder();
		sb2.append("SELECT A.* FROM _EWA_WF_CNN A");
		sb2.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@REF_ID ");
		sb2.append(" WHERE A.WF_ID = @WF_ID");
		sb2.append(" ORDER BY WF_UNIT_FROM, WF_UNIT_TO");
		sql2 = sb2.toString();

		DTTable tbCnn = DTTable.getJdbcTable(sql2, cnn);

		String sql3 = "SELECT * FROM _EWA_WF_UNIT_ADM WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID ";
		DTTable tbADM = DTTable.getJdbcTable(sql3, cnn);

		String sql4 = "SELECT * FROM _EWA_WF_UNIT_LOGIC WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID ";
		DTTable tbLOGIC = DTTable.getJdbcTable(sql4, cnn);

		rv.addValue("dlv_ver", -1);
		rv.addValue("DLV_JSON_UNIT", tbUnit.toJson(rv));
		rv.addValue("DLV_JSON_CNN", tbCnn.toJson(rv));
		rv.addValue("DLV_JSON_ADM", tbADM.toJson(rv));
		rv.addValue("DLV_JSON_LOGIC", tbLOGIC.toJson(rv));

		rv.addValue("DLV_CODE", code);

		int dlvVer = -1231;
		// 查找已经发布的相同的版本
		sql1 = "SELECT DLV_VER FROM _EWA_WF_DLV WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID AND DLV_CODE=@DLV_CODE";
		DTTable tbLast = DTTable.getJdbcTable(sql1, cnn);
		String sql = "";
		if (tbLast.getCount() == 0) {
			// 该版本没有发布，找已发布的最大版本号
			String sqlMaxDlv = "select max(dlv_ver) from  _EWA_WF_DLV WHERE WF_ID=@WF_ID AND WF_REF_ID=@REF_ID";
			DTTable tbMaxDlv = DTTable.getJdbcTable(sqlMaxDlv, cnn);

			if (tbMaxDlv.getCount() == 0 || tbMaxDlv.getCell(0, 0).isNull()) {
				// 版本号从1开始
				dlvVer = 1;
				rv.addValue("dlv_ver", 1);
			} else {
				dlvVer = tbMaxDlv.getCell(0, 0).toInt();
				if (dlvVer == -1) { // 以前遗留的bug
					dlvVer = 2;
				} else {
					dlvVer = dlvVer + 1;
				}
			}
			rv.addOrUpdateValue("DLV_VER", dlvVer);

			StringBuilder sba = new StringBuilder();
			sba.append("INSERT INTO _EWA_WF_DLV(WF_ID, WF_REF_ID, DLV_VER, DLV_JSON_UNIT, DLV_CUR,DLV_UNID,");
			sba.append(" \n DLV_JSON_CNN, DLV_JSON_ADM, DLV_JSON_LOGIC, DLV_DATE, DLV_CODE)");
			sba.append(" \n VALUES(@WF_ID, @REF_ID, @DLV_VER, @DLV_JSON_UNIT, 'Y',@SYS_UNID, ");
			sba.append(" \n @DLV_JSON_CNN, @DLV_JSON_ADM, @DLV_JSON_LOGIC, @SYS_DATE, @DLV_CODE)");
			sql = sba.toString();

		} else {

			dlvVer = tbLast.getCell(0, 0).toInt();

			// 相同的版本更新数据，例如角色，前后执行等
			StringBuilder sbb = new StringBuilder();
			sbb.append("UPDATE _EWA_WF_DLV SET DLV_JSON_UNIT=@DLV_JSON_UNIT,");
			sbb.append(" \n  DLV_JSON_CNN = @DLV_JSON_CNN, ");
			sbb.append(" \n  DLV_JSON_ADM = @DLV_JSON_ADM, ");
			sbb.append(" \n  DLV_JSON_LOGIC = @DLV_JSON_LOGIC, ");
			sbb.append(" \n  DLV_DATE = @SYS_DATE, DLV_CUR='Y' ");
			sbb.append(" \n  WHERE WF_ID=@WF_ID AND WF_REF_ID=@ref_id AND DLV_CODE=@DLV_CODE");
			sql = sbb.toString();
		}
		rv.addValue("WF_DLV_VER", dlvVer);

		StringBuilder sbc = new StringBuilder();
		sbc.append("UPDATE _EWA_WF_DLV SET DLV_CUR='N' WHERE DLV_VER != @WF_DLV_VER");
		sbc.append(" AND WF_ID=@WF_ID AND WF_REF_ID=@ref_id");
		sql1 = sbc.toString();

		OrgSqls sqls = OrgSqls.instance();
		String sqlUpdateFlow = sqls.getSql("WF_DLV_UPDATE_FLOW");
		sqlUpdateFlow = sqlUpdateFlow.replace("{dbname}", dbName);
		/// cnn.transBegin();
		try {
			this.executeAct(cnn, sql);
			this.executeAct(cnn, sql1);

			rv.addValue("sup_id", refId);
			this.executeAct(cnn, sqlUpdateFlow);

			// 记录节点的历史信息
			this.executeAct(cnn, sqlUnitHis);
			LOGGER.info("{}, {}, {}", wfId, refId, dlvVer);
			// cnn.transCommit();
			return true;
		} catch (Exception err) {
			// cnn.transRollback();
			LOGGER.error(err.getMessage());
			return false;
		} finally {
			cnn.close();
		}
	}

	/**
	 * 发布版本
	 */
	public boolean delived(String wfId, String refId) {
		return this.delived(wfId, refId, null);
	}

	public EwaWfUnit getUnit(String unitId) {
		if (this._Units.containsKey(unitId.trim())) {
			return this._Units.get(unitId.trim());
		} else {
			return null;
		}
	}

	/**
	 * @return the _Units
	 */
	public HashMap<String, EwaWfUnit> getUnits() {
		return _Units;
	}

	/**
	 * @return the _Cnns
	 */
	public HashMap<String, EwaWfCnn> getCnns() {
		return _Cnns;
	}

	/**
	 * @return the _Wf
	 */
	public EwaWf getWf() {
		return _Wf;
	}
}
