package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
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
	public static String UNIT_CUR_ID = "SYS_STA_TAG";
	/**
	 * 下个节点
	 */
	public static String UNIT_NEXT_ID = "SYS_STA_VAL";
	/**
	 * 来源编号
	 */
	public static String WF_REF_ID = "SYS_STA_RID";
	/**
	 * 来源
	 */
	public static String WF_REF_TABLE = "SYS_STA_TABLE";
	/**
	 * 发布版本号
	 */
	public static String WF_VERSION = "EWA_WF_DLV_VER";

	/**
	 * 是否流程完成标准名称
	 */
	public static String WF_IS_END = "EWA_WF_IS_END";

	/**
	 * 下个节点标准名称
	 */
	public static String WF_NEXT = "EWA_WF_NEXT";

	private HashMap<String, EwaWfUnit> _Units = new HashMap<String, EwaWfUnit>();
	private HashMap<String, EwaWfCnn> _Cnns = new HashMap<String, EwaWfCnn>();
	private HashMap<Integer, HashMap<String, String>> _LogicTables = new HashMap<Integer, HashMap<String, String>>();
	private EwaWf _Wf;
	// private DTTable _TbDlv; // 流程发布数据

	public void doPost(RequestValue rv) throws Exception {
		String flowIsEnd = "Y";
		OrgSqls sqls = OrgSqls.instance();

		String curUnitName = rv.getString(UNIT_CUR_ID).trim();
		if (curUnitName == null) {
			throw new Exception("参数当前节点" + UNIT_CUR_ID + "没有传递");
		}
		EwaWfUnit unitCur = this._Units.get(curUnitName);
		if (unitCur == null) {
			throw new Exception("参数当前节点" + UNIT_CUR_ID + "不存在");
		}

		// 参数下一个节点
		String curUnitNext = rv.getString(UNIT_NEXT_ID).trim();
		if (curUnitNext == null) {
			throw new Exception("参数下一个节点" + UNIT_CUR_ID + "没有传递");
		}
		EwaWfUnit unitNext = this._Units.get(curUnitNext);
		if (unitNext == null) {
			throw new Exception("参数下一个节点" + curUnitNext + "不存在");
		}

		// boolean is_back_router = false;
		// 检查流转是否合法
		// String cnnKey = curUnitName + "|" + curUnitNext;
		// if (!this._Cnns.containsKey(cnnKey)) {
		// String cnnKeyback = curUnitNext + "|" + curUnitName; // 打回操作
		// if (this._Cnns.containsKey(cnnKeyback)) {
		// // is_back_router = true;
		// } else {
		// throw new Exception("没有从" + curUnitName + "到" + curUnitNext +
		// "流转的定义");
		// }
		// }

		// 检查流转的定义
		if (!this.checkFlowValid(unitCur, unitNext)) {
			throw new Exception("没有从" + curUnitName + "到" + curUnitNext + "流转的定义");
		}

		DataConnection cnn = new DataConnection();
		cnn.setRequestValue(rv);

		OrgSqls o = OrgSqls.instance();
		String sql = o.getSql("WF_LOG_GET");

		DTTable table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		String startAdmin = null;
		if (table.getCount() > 0) {
			startAdmin = table.getCell(0, "adm_id").toString();
		}
		// 检查是否可流转
		boolean is_can_run = false;
		is_can_run = this.checkIsCanRun1(unitCur.getWfUnitAdm(), unitCur.getWfUnitAdmLst(), unitCur.getWfUnitSelfDept(),
				startAdmin, cnn);

		cnn.close();
		if (!is_can_run) {
			StringBuilder err1 = new StringBuilder();
			err1.append("审批权限不对：WfUnitAdm=");
			err1.append(unitCur.getWfUnitAdm());
			err1.append(",WfUnitAdmLst=");
			err1.append(unitCur.getWfUnitAdmLst());
			err1.append(",WfUnitSelfDept=");
			err1.append(unitCur.getWfUnitSelfDept());
			err1.append(",startAdmin=");
			err1.append(startAdmin);
			String msg = err1.toString();
			System.err.println(msg);
			throw new Exception(msg);
		}
		// 查找是否流程完成，完成条件是找不到以下一个节点开始的连接，即有输入无输出
		Iterator<String> it = this._Cnns.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key.indexOf(curUnitNext + "|") == 0) {
				flowIsEnd = null;
				break;
			}
		}
		// 设置标准名称，是否流程完成，用于程序调用
		rv.addValue(WF_IS_END, flowIsEnd);

		// 设置标准名称，参数下一个节点，用于程序调用
		rv.addValue(WF_NEXT, curUnitNext);

		// 将节点的操作类型和操作者放到参数中，用于程序调用
		rv.addValue("WF_UNIT_NAME", unitNext.getWfUnitName());
		rv.addValue("WF_UNIT_ADM", unitNext.getWfUnitAdm());
		rv.addValue("WF_UNIT_ADM_LST", unitNext.getWfUnitAdmLst());
		// 是否本部门
		rv.addValue("WF_UNIT_SELF_DEPT", unitNext.getWfUnitSelfDept());

		// String sqlCurStatus = sqls.getSql("WF_LOG_GET");

		// 获取流程的SQL
		String sqla = sqls.getSql("WF_APP_GET");
		DTTable tableApp = DTTable.getJdbcTable(sqla, cnn);
		cnn.close();

		// 当前节点
		EwaWfUnit curUnit = this.getUnit(curUnitName);
		if (cnn.getErrorMsg() != null) {
			throw new Exception(cnn.getErrorMsg());
		}
		if (tableApp.getCount() == 0) {
			throw new Exception("获取APP-无数据");
		}

		String appTable = tableApp.getCell(0, "APP_WF_TABLE").toString();
		String appField = tableApp.getCell(0, "APP_WF_FIELD").toString();
		String appPks = tableApp.getCell(0, "APP_WF_PKS").toString();
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
		String[] vals = rv.getString(WF_REF_ID).split(",");
		if (pks.length != vals.length) {
			throw new Exception("APP_PKS定义和参数值不一致" + appPks + "|" + rv.getString(WF_REF_ID));
		}

		if (this._Wf.getWfPara0() == null || this._Wf.getWfPara0().trim().length() == 0) {
			throw new Exception("生成索引数据脚本未定义");
		}

		String sqlMainStatus = "UPDATE " + appTable + " SET " + appField + "='" + curUnitNext.replace("'", "''").trim()
				+ "' WHERE ";
		for (int i = 0; i < pks.length; i++) {
			String v = vals[i].replace("'", "''").trim();
			sqlMainStatus += pks[i] + "='" + v + "'";
		}

		try {

			// boolean isOk = false;

			/*
			 * for (int i = 0; i < curUnit.getFromCnns().size(); i++) { EwaWfCnn c =
			 * curUnit.getFromCnns().get(i); if (c.getWfUnitTo().trim().equals(curUnitNext))
			 * { isOk = true; break; } } if (!isOk) { throw new Exception("下一步" +
			 * curUnitNext + ",不合法"); }
			 */

			// 执行前检查
			String strBeforeOk = this.runActBefore(cnn, curUnit, rv);
			if (strBeforeOk != null) {
				throw new Exception("不能执行\r\n" + strBeforeOk);
			}

			String sqlUpdate = sqls.getSql("WF_LOG_NEW");
			// 更新日志索引
			String sqlIdxCheck = sqls.getSql("WF_LOG_IDX_CHECK");
			DTTable tbIdxCheck = this.executeAct(cnn, sqlIdxCheck).get(0);

			// 事物开始
			cnn.transBegin();
			try {
				// 更新主表 状态
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
				this.runActAfter(cnn, curUnit, rv);

				cnn.transCommit();

				this.notification(rv, unitCur, unitNext, flowIsEnd);
			} catch (Exception e2) {
				// 回滚
				cnn.transRollback();
				throw e2;
			}

		} catch (Exception err) {
			throw err;
		} finally {
			cnn.transClose();
			cnn.close();
		}

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

	private void runActAfter(DataConnection conn, EwaWfUnit curUnit, RequestValue rv) throws Exception {
		// 执行对应的编号（WF_FUC_ID）
		String fucId = curUnit.getWfUnitPara3();
		if (fucId == null || fucId.trim().length() == 0) {
			return;
		}
		String sql = "SELECT WF_FUC_SQL,WF_FUC_ID, WF_ID, WF_FUC_NAME, WF_FUC_MEMO, WF_FUC_TAG "
				+ " FROM _EWA_WF_FUNC WHERE WF_FUC_TAG='WF_ACT_AFT' AND WF_ID=@WF_ID" + " and WF_FUC_ID='"
				+ fucId.replace("'", "''") + "'";
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

	private String runActBefore(DataConnection conn, EwaWfUnit curUnit, RequestValue rv) throws Exception {
		// 执行前检查对应的编号（WF_FUC_ID）
		String fucId = curUnit.getWfUnitPara4();
		if (fucId == null || fucId.trim().length() == 0) {
			return null;
		}
		String sql = "SELECT WF_FUC_SQL,WF_FUC_ID, WF_ID, WF_FUC_NAME, WF_FUC_MEMO, WF_FUC_TAG "
				+ " FROM _EWA_WF_FUNC WHERE WF_FUC_TAG='WF_ACT_BEF' AND WF_ID=@WF_ID" + " and WF_FUC_ID='"
				+ fucId.replace("'", "''") + "'";
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
				if (al.get(m).getCount() > 0) {
					String rst = al.get(m).getCell(0, 0).toString();
					if (rst == null) {
						rst = "NULL";
					}
					sb.a("<br>" + inc + ". " + rst);
					inc++;
				}
			}
			if (sb.length() > 0) {
				return sb.toString(); // 有数据表示错误
			}
		}
		return null;
	}

	private ArrayList<DTTable> executeAct(DataConnection conn, String sql) throws Exception {
		String[] sqls = sql.split(";");
		ArrayList<DTTable> al = new ArrayList<DTTable>();
		for (int i = 0; i < sqls.length; i++) {
			String s = sqls[i].trim();
			if (s.length() == 0) {
				continue;
			}
			// s = conn.rebuildSql(s);
			if (s.toUpperCase().startsWith("SELECT")) {
				DTTable tb = DTTable.getJdbcTable(s, conn);
				if (tb != null && tb.isOk()) {
					al.add(tb);
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
		sb.append(",\r\n\"UNIT\":");
		sb.append(tbDlv.getCell(0, "DLV_JSON_UNIT").toString());

		// 英文节点名称 2016-10-27
		String sqlEn = "SELECT WF_UNIT_ID, WF_UNIT_NAME_EN,WF_UNIT_MEMO_EN FROM _ewa_wf_unit WHERE WF_ID=@WF_ID AND (WF_REF_ID=@G_SUP_UNID  )";
		DTTable tbEN = DTTable.getJdbcTable(sqlEn, cnn);

		sb.append(",\r\n\"EN\":");
		sb.append(tbEN.toJson(rv));

		sb.append(",\r\n\"ST\":");
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
		 * 角色数据 sb.append(",\r\n\"ROLES\":"); sql = o.getSql("WF_ROLE"); table =
		 * DTTable.getJdbcTable(sql, cnn); cnn.close();
		 * 
		 * sb.append(table.toJson(rv));
		 */

		sb.append(",\r\n \"WF_CUR\":\"" + Utils.textToJscript(rv.getString("APP_WF_UNIT_CUR")) + "\"");
		sb.append(",\r\n \"WF_DLV_VER\":\"" + version + "\"");

		sql = "SELECT  XITEMS FROM _EWA_WF_SHOW WHERE  1=1 " + " AND WF_ID=@WF_ID "
				+ " AND ( WF_REF_ID=@G_SUP_UNID) AND WF_UNIT_ID=@APP_WF_UNIT_CUR";

		DTTable tableSHOW = DTTable.getJdbcTable(sql, cnn);

		sb.append(",\r\n \"WF_SHOW\":" + tableSHOW.toJson(rv) + "");
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
		String sqlPost = "select dep_pos_id, dep_pos_name from ADM_R_DEPT_POST where (dep_pos_id=@G_POST_ID or dep_pos_id=@G_POS_ID) and dep_pos_id in  ";

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
				sb.append(",\r\n");
			}
			sb.append(json);
			inc++;
		}
		sb.append("],\r\n\"UNIT\":[");

		it = this._Units.keySet().iterator();
		inc = 0;
		while (it.hasNext()) {
			String key = it.next();
			String json = this._Units.get(key).toJson();
			if (inc > 0) {
				sb.append(",\r\n");
			}
			sb.append(json);
			inc++;
		}
		sb.append("],\r\n\"ST\":");

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
		// sb.append(",\r\nLG:");
		// String logicJson = table1.toJson(rv);
		// sb.append(logicJson);
		sb.append(",\r\n\"ROLES\":");
		sql = o.getSql("WF_ROLE");
		table = DTTable.getJdbcTable(sql, cnn);
		cnn.close();

		sb.append(table.toJson(rv));

		sb.append(",\r\n \"WF_CUR\":\"" + Utils.textToJscript(rv.getString("APP_WF_UNIT_CUR")) + "\"");
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

		String sql = "SELECT * FROM _EWA_WF_DLV WHERE WF_ID=@WF_ID AND WF_REF_ID=@G_SUP_UNID ";
		if (version > 0) {
			sql += " AND DLV_VER = " + version;
		} else {
			sql += " AND DLV_CUR='Y' ";
		}
		DTTable tbDlv = DTTable.getJdbcTable(sql, "", rv);
		if (!tbDlv.isOk() || tbDlv.getCount() == 0) {
			String ss = "未发现当前的流程版本《" + rv.getString("WF_ID") + "》";
			System.err.println(ss);

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
		System.out.println(s.toString());
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
		System.out.println(s.toString());
		return s.toString();
	}

	/**
	 * 发布版本
	 */
	public boolean delived(String wfId, String refId, String targetDatabaseName) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");

		RequestValue rv = new RequestValue();
		rv.addValue("sup_id", refId);
		rv.addValue("WF_ID", wfId);
		cnn.setRequestValue(rv);

		String dbName = "";
		if (targetDatabaseName != null) {
			if (SqlUtils.isSqlServer(cnn)) {
				dbName = targetDatabaseName + "..";
			} else {
				dbName = targetDatabaseName + ".";
			}
		}
		// System.out.println("WF_ID=" + wfId);
		this.init(wfId, refId);

		String sql1 = "SELECT A.WF_UNIT_ID FROM _EWA_WF_UNIT A"
				+ " INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@SUP_ID " + " WHERE A.WF_ID=@WF_ID"
				+ " ORDER BY WF_UNIT_ID";
		DTTable tbUnitCode = DTTable.getJdbcTable(sql1, cnn);
		String jsonUnit = tbUnitCode.toJson(rv);
		String unitCode = jsonUnit.hashCode() + "";

		String sql2 = "SELECT WF_UNIT_FROM, WF_UNIT_TO FROM _EWA_WF_CNN A"
				+ " INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@SUP_ID " + " WHERE A.WF_ID=@WF_ID"
				+ " ORDER BY WF_UNIT_FROM, WF_UNIT_TO";

		DTTable tbCnnCode = DTTable.getJdbcTable(sql2, cnn);
		String jsonCnn = tbCnnCode.toJson(rv);
		String cnnCode = jsonCnn.hashCode() + "";

		// 生成验证码，如果节点数量和编号未变化+ 连接数量和前后关系没变化
		String code = unitCode + "|" + cnnCode;

		StringBuilder sbUnit = new StringBuilder();
		sbUnit.append("SELECT A.* FROM _EWA_WF_UNIT A  \n");
		sbUnit.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@SUP_ID  \n");
		sbUnit.append(" WHERE A.WF_ID=@WF_ID  \n");
		sbUnit.append(" ORDER BY WF_UNIT_ID ");
		sql1 = sbUnit.toString();
		DTTable tbUnit = DTTable.getJdbcTable(sql1, cnn);

		StringBuilder sbHis = new StringBuilder();
		sbHis.append("INSERT INTO _EWA_WF_UNIT_HIS(WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, WF_UNIT_MEMO)  \n");
		sbHis.append("	SELECT WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, WF_UNIT_MEMO FROM _EWA_WF_UNIT A  \n");
		sbHis.append(" WHERE NOT EXISTS(  \n");
		sbHis.append("	SELECT * FROM _EWA_WF_UNIT_his b where a.wf_id=b.wf_id  \n");
		sbHis.append("		and a.wf_unit_id=b.wf_unit_id  \n");
		sbHis.append("		 and a.wf_ref_id=b.wf_ref_id  \n");
		sbHis.append(") AND A.WF_REF_ID=@SUP_ID AND A.WF_ID=@WF_ID;");
		sbHis.append("\r\n ");
		if (SqlUtils.isSqlServer(cnn)) {
			sbHis.append("UPDATE _EWA_WF_UNIT_HIS SET  \n");
			sbHis.append("		_EWA_WF_UNIT_HIS.WF_UNIT_NAME=A.WF_UNIT_NAME,  \n");
			sbHis.append("		_EWA_WF_UNIT_HIS.WF_UNIT_MEMO=A.WF_UNIT_MEMO  \n");
			sbHis.append("FROM _EWA_WF_UNIT A  \n");
			sbHis.append("WHERE _EWA_WF_UNIT_HIS.WF_ID=A.WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_UNIT_ID=A.WF_UNIT_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=A.WF_REF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_ID=@WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=@SUP_ID");
		} else {
			sbHis.append("UPDATE _EWA_WF_UNIT_HIS INNER JOIN _EWA_WF_UNIT A \n");
			sbHis.append(" ON _EWA_WF_UNIT_HIS.WF_ID=A.WF_ID \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_UNIT_ID=A.WF_UNIT_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=A.WF_REF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_ID=@WF_ID  \n");
			sbHis.append("	AND _EWA_WF_UNIT_HIS.WF_REF_ID=@SUP_ID  \n");
			sbHis.append("SET	_EWA_WF_UNIT_HIS.WF_UNIT_NAME=A.WF_UNIT_NAME, \n");
			sbHis.append("		_EWA_WF_UNIT_HIS.WF_UNIT_MEMO=A.WF_UNIT_MEMO \n");

		}
		String sqlUnitHis = sbHis.toString();

		StringBuilder sb2 = new StringBuilder();
		sb2.append("SELECT A.* FROM _EWA_WF_CNN A");
		sb2.append(" INNER JOIN _EWA_WF B ON A.WF_ID=B.WF_ID AND WF_REF_ID=@SUP_ID ");
		sb2.append(" WHERE A.WF_ID = @WF_ID");
		sb2.append(" ORDER BY WF_UNIT_FROM, WF_UNIT_TO");
		sql2 = sb2.toString();

		DTTable tbCnn = DTTable.getJdbcTable(sql2, cnn);

		String sql3 = "SELECT * FROM _EWA_WF_UNIT_ADM WHERE WF_ID=@WF_ID AND WF_REF_ID=@SUP_ID ";
		DTTable tbADM = DTTable.getJdbcTable(sql3, cnn);

		String sql4 = "SELECT * FROM _EWA_WF_UNIT_LOGIC WHERE WF_ID=@WF_ID AND WF_REF_ID=@SUP_ID ";
		DTTable tbLOGIC = DTTable.getJdbcTable(sql4, cnn);

		rv.addValue("dlv_ver", -1);
		rv.addValue("DLV_JSON_UNIT", tbUnit.toJson(rv));
		rv.addValue("DLV_JSON_CNN", tbCnn.toJson(rv));
		rv.addValue("DLV_JSON_ADM", tbADM.toJson(rv));
		rv.addValue("DLV_JSON_LOGIC", tbLOGIC.toJson(rv));

		rv.addValue("DLV_CODE", code);

		sql1 = "SELECT DLV_VER FROM _EWA_WF_DLV WHERE WF_ID=@WF_ID AND WF_REF_ID=@SUP_ID AND DLV_CODE=@DLV_CODE";
		DTTable tbLast = DTTable.getJdbcTable(sql1, cnn);
		String sql = "";
		boolean isNew = true;
		if (tbLast.getCount() == 0) {
			sql = "INSERT INTO _EWA_WF_DLV(WF_ID, WF_REF_ID, DLV_VER, DLV_JSON_UNIT, DLV_CUR,DLV_UNID,"
					+ "\r\nDLV_JSON_CNN, DLV_JSON_ADM, DLV_JSON_LOGIC, DLV_DATE, DLV_CODE)"
					+ "\r\n VALUES(@WF_ID, @SUP_ID, @DLV_VER, @DLV_JSON_UNIT, 'Y',@SYS_UNID, "
					+ "\r\n@DLV_JSON_CNN, @DLV_JSON_ADM, @DLV_JSON_LOGIC, @SYS_DATE, @DLV_CODE)";

		} else {
			sql = "UPDATE _EWA_WF_DLV SET DLV_JSON_UNIT=@DLV_JSON_UNIT," + "\r\n DLV_JSON_CNN=@DLV_JSON_CNN, "
					+ "\r\n DLV_JSON_ADM=@DLV_JSON_ADM, " + "\r\n DLV_JSON_LOGIC=@DLV_JSON_LOGIC, "
					+ "\r\n DLV_DATE=@SYS_DATE,DLV_CUR='Y'," + "\r\n DLV_UNID=@SYS_UNID "
					+ "\r\n WHERE WF_ID=@WF_ID AND WF_REF_ID=@SUP_ID AND DLV_CODE=@DLV_CODE";
			isNew = false;
		}
		sql1 = "UPDATE _EWA_WF_DLV SET DLV_CUR='N' WHERE DLV_UNID<>@SYS_UNID"
				+ " AND WF_ID=@WF_ID AND WF_REF_ID=@SUP_ID";
		if (isNew) {
			sql2 = "UPDATE _EWA_WF_DLV SET DLV_VER=ISNULL(A,0)+1 FROM ("
					+ "\r\n SELECT COUNT(*) A FROM _EWA_WF_DLV WHERE WF_ID=@WF_ID" + "\r\n 	AND WF_REF_ID=@SUP_ID"
					+ "\r\n) AA WHERE DLV_UNID = @SYS_UNID";
		} else {
			sql2 = "";
		}
		OrgSqls sqls = OrgSqls.instance();
		String sqlUpdateFlow = sqls.getSql("WF_DLV_UPDATE_FLOW");
		sqlUpdateFlow = sqlUpdateFlow.replace("{dbname}", dbName);
		/// cnn.transBegin();
		try {
			this.executeAct(cnn, sql);
			this.executeAct(cnn, sql1);
			if (sql2.length() > 0)
				this.executeAct(cnn, sql2);

			sql = "SELECT DLV_VER FROM _EWA_WF_DLV WHERE DLV_UNID = @SYS_UNID";
			int DLV_VER = Integer.parseInt(this.executeAct(cnn, sql).get(0).getCell(0, 0).toString());
			rv.addValue("WF_DLV_VER", DLV_VER);

			this.executeAct(cnn, sqlUpdateFlow);

			// 记录节点的历史信息
			this.executeAct(cnn, sqlUnitHis);
			// cnn.transCommit();
			return true;
		} catch (Exception err) {
			// cnn.transRollback();
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
