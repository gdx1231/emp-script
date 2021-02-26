package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;

/**
 * 表_EWA_WF_UNIT操作类
 * 
 * @author gdx 时间：Fri Feb 15 2019 09:16:17 GMT+0800 (中国标准时间)
 */
public class EwaWfUnitDao extends ClassDaoBase<EwaWfUnit> implements IClassDao<EwaWfUnit> {

	private static String SQL_SELECT = "SELECT * FROM _EWA_WF_UNIT WHERE WF_UNIT_ID=@WF_UNIT_ID AND WF_ID=@WF_ID AND WF_REF_ID=@WF_REF_ID";
	private static String SQL_UPDATE = "UPDATE _EWA_WF_UNIT SET 	 WF_UNIT_NAME = @WF_UNIT_NAME, 	 WF_UNIT_MEMO = @WF_UNIT_MEMO, 	 WF_UNIT_TYPE = @WF_UNIT_TYPE, 	 WF_UNIT_ADM = @WF_UNIT_ADM, 	 WF_UNIT_ADM_LST = @WF_UNIT_ADM_LST, 	 WF_UNIT_PARA0 = @WF_UNIT_PARA0, 	 WF_UNIT_PARA1 = @WF_UNIT_PARA1, 	 WF_UNIT_PARA2 = @WF_UNIT_PARA2, 	 WF_UNIT_PARA3 = @WF_UNIT_PARA3, 	 WF_UNIT_PARA4 = @WF_UNIT_PARA4, 	 WF_UNIT_X = @WF_UNIT_X, 	 WF_UNIT_Y = @WF_UNIT_Y, 	 WF_UNIT_ACT_BEF = @WF_UNIT_ACT_BEF, 	 WF_UNIT_ACT_AFT = @WF_UNIT_ACT_AFT, 	 WF_UNIT_SELF_DEPT = @WF_UNIT_SELF_DEPT, 	 WF_UNIT_MEMO_EN = @WF_UNIT_MEMO_EN, 	 WF_UNIT_NAME_EN = @WF_UNIT_NAME_EN, 	 WF_UNIT_NOTIFY = @WF_UNIT_NOTIFY WHERE WF_UNIT_ID=@WF_UNIT_ID AND WF_ID=@WF_ID AND WF_REF_ID=@WF_REF_ID";
	private static String SQL_DELETE = "DELETE FROM _EWA_WF_UNIT WHERE WF_UNIT_ID=@WF_UNIT_ID AND WF_ID=@WF_ID AND WF_REF_ID=@WF_REF_ID";
	private static String SQL_INSERT = "INSERT INTO _EWA_WF_UNIT(WF_UNIT_ID, WF_ID, WF_REF_ID, WF_UNIT_NAME, WF_UNIT_MEMO, WF_UNIT_TYPE, WF_UNIT_ADM, WF_UNIT_ADM_LST, WF_UNIT_PARA0, WF_UNIT_PARA1, WF_UNIT_PARA2, WF_UNIT_PARA3, WF_UNIT_PARA4, WF_UNIT_X, WF_UNIT_Y, WF_UNIT_ACT_BEF, WF_UNIT_ACT_AFT, WF_UNIT_SELF_DEPT, WF_UNIT_MEMO_EN, WF_UNIT_NAME_EN, WF_UNIT_NOTIFY) 	VALUES(@WF_UNIT_ID, @WF_ID, @WF_REF_ID, @WF_UNIT_NAME, @WF_UNIT_MEMO, @WF_UNIT_TYPE, @WF_UNIT_ADM, @WF_UNIT_ADM_LST, @WF_UNIT_PARA0, @WF_UNIT_PARA1, @WF_UNIT_PARA2, @WF_UNIT_PARA3, @WF_UNIT_PARA4, @WF_UNIT_X, @WF_UNIT_Y, @WF_UNIT_ACT_BEF, @WF_UNIT_ACT_AFT, @WF_UNIT_SELF_DEPT, @WF_UNIT_MEMO_EN, @WF_UNIT_NAME_EN, @WF_UNIT_NOTIFY)";
	public static String TABLE_NAME = "_EWA_WF_UNIT";
	public static String[] KEY_LIST = { "WF_UNIT_ID", "WF_ID", "WF_REF_ID" };
	public static String[] FIELD_LIST = { "WF_UNIT_ID", "WF_ID", "WF_REF_ID", "WF_UNIT_NAME", "WF_UNIT_MEMO",
			"WF_UNIT_TYPE", "WF_UNIT_ADM", "WF_UNIT_ADM_LST", "WF_UNIT_PARA0", "WF_UNIT_PARA1", "WF_UNIT_PARA2",
			"WF_UNIT_PARA3", "WF_UNIT_PARA4", "WF_UNIT_X", "WF_UNIT_Y", "WF_UNIT_ACT_BEF", "WF_UNIT_ACT_AFT",
			"WF_UNIT_SELF_DEPT", "WF_UNIT_MEMO_EN", "WF_UNIT_NAME_EN", "WF_UNIT_NOTIFY" };

	public ArrayList<EwaWfUnit> getUnitsByJSon(String jsonString) {
		ArrayList<EwaWfUnit> al = new ArrayList<EwaWfUnit>();
		try {
			String s1 = "{\"tmp\":" + jsonString + "}";
			JSONObject json = new JSONObject(s1);
			JSONArray jsons = json.getJSONArray("tmp");

			// json.toJSONArray(jsons);
			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);
				EwaWfUnit unit = this.mapJson(o);
				al.add(unit);
			}
		} catch (JSONException e) {
			return null;
		}
		return al;
	}

	private EwaWfUnit mapJson(JSONObject o) {
		EwaWfUnit unit = new EwaWfUnit();
		unit.initValues(o);
//		for (int i = 0; i < FIELD_LIST.length; i++) {
//			String f = FIELD_LIST[i];
//			if (o.has(f)) {
//				Object v;
//				try {
//					v = o.get(f);
//					unit.setField(f, v);
//				} catch (JSONException e) {
//					System.out.println(e.toString());
//				}
//			}
//
//		}
		return unit;
	}

	public EwaWfUnitDao() {
		// 设置数据库连接配置名称，在 ewa_conf.xml中定义
		// super.setConfigName("ow_main");
	}

	/**
	 * 生成一条记录
	 * 
	 * @param para 表_EWA_WF_UNIT的映射类
	 * 
	 * @return 是否成功
	 */

	public boolean newRecord(EwaWfUnit para) {

		RequestValue rv = this.createRequestValue(para);

		return super.executeUpdate(SQL_INSERT, rv);

	}

	/**
	 * 生成一条记录
	 * 
	 * @param para         表_EWA_WF_UNIT的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean newRecord(EwaWfUnit para, HashMap<String, Boolean> updateFields) {
		String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		return super.executeUpdate(sql, rv);
	}

	/**
	 * 更新一条记录
	 * 
	 * @param para 表_EWA_WF_UNIT的映射类
	 * @return 是否成功
	 */
	public boolean updateRecord(EwaWfUnit para) {

		RequestValue rv = this.createRequestValue(para);
		return super.executeUpdate(SQL_UPDATE, rv);
	}

	/**
	 * 更新一条记录
	 * 
	 * @param para         表_EWA_WF_UNIT的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean updateRecord(EwaWfUnit para, HashMap<String, Boolean> updateFields) {
		// 没定义主键的话不能更新
		if (KEY_LIST.length == 0) {
			return false;
		}
		String sql = super.sqlUpdateChanged(TABLE_NAME, KEY_LIST, updateFields);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		return super.executeUpdate(sql, rv);
	}

	public String getSqlDelete() {
		return SQL_DELETE;
	}

	public String[] getSqlFields() {
		return FIELD_LIST;
	}

	public String getSqlSelect() {
		return "SELECT * FROM _EWA_WF_UNIT where 1=1";
	}

	public String getSqlUpdate() {
		return SQL_UPDATE;
	}

	public String getSqlInsert() {
		return SQL_INSERT;
	}

	/**
	 * 根据主键返回一条记录
	 * 
	 * @param paraWfUnitId 编号
	 * @param paraWfId     流程编号
	 * @param paraWfRefId  来源
	 * @return 记录类(EwaWfUnit)
	 */
	public EwaWfUnit getRecord(String paraWfUnitId, String paraWfId, String paraWfRefId) {
		RequestValue rv = new RequestValue();
		rv.addValue("WF_UNIT_ID", paraWfUnitId, "String", 36);
		rv.addValue("WF_ID", paraWfId, "String", 36);
		rv.addValue("WF_REF_ID", paraWfRefId, "String", 36);
		ArrayList<EwaWfUnit> al = super.executeQuery(SQL_SELECT, rv, new EwaWfUnit(), FIELD_LIST);
		if (al.size() > 0) {
			EwaWfUnit o = al.get(0);
			al.clear();
			return o;
		} else {
			return null;
		}
	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @return 记录集合
	 */
	public ArrayList<EwaWfUnit> getRecords(String whereString) {
		String sql = "SELECT * FROM _EWA_WF_UNIT WHERE " + whereString;
		return super.executeQuery(sql, new EwaWfUnit(), FIELD_LIST);
	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @param fields      指定返回的字段
	 * @return 记录集合
	 */
	public ArrayList<EwaWfUnit> getRecords(String whereString, List<String> fields) {
		String sql = super.createSelectSql(TABLE_NAME, whereString, fields);
		String[] arrFields = new String[fields.size()];
		arrFields = fields.toArray(arrFields);
		return super.executeQuery(sql, new EwaWfUnit(), arrFields);
	}

	/**
	 * 根据查询条件返回多条记录（限制为500条）
	 * 
	 * @param whereString 查询条件，注意过滤“'”符号，避免SQL注入攻击
	 * @param pkFieldName 主键
	 * @param pageSize    每页记录数
	 * @param currentPage 当前页
	 * @return 记录集合
	 */
	public ArrayList<EwaWfUnit> getRecords(String whereString, String pkFieldName, int pageSize, int currentPage) {
		String sql = "SELECT * FROM _EWA_WF_UNIT WHERE " + whereString;
		return super.executeQuery(sql, new EwaWfUnit(), FIELD_LIST, pkFieldName, pageSize, currentPage);
	}

	/**
	 * 根据主键删除一条记录
	 * 
	 * @param paraWfUnitId 编号
	 * @param paraWfId     流程编号
	 * @param paraWfRefId  来源
	 * @return 是否成功
	 */
	public boolean deleteRecord(String paraWfUnitId, String paraWfId, String paraWfRefId) {
		RequestValue rv = new RequestValue();
		rv.addValue("WF_UNIT_ID", paraWfUnitId, "String", 36);
		rv.addValue("WF_ID", paraWfId, "String", 36);
		rv.addValue("WF_REF_ID", paraWfRefId, "String", 36);
		return super.executeUpdate(SQL_DELETE, rv);
	}

	public RequestValue createRequestValue(EwaWfUnit para) {
		RequestValue rv = new RequestValue();
		rv.addValue("WF_UNIT_ID", para.getWfUnitId(), "String", 36); // 编号
		rv.addValue("WF_ID", para.getWfId(), "String", 36); // 流程编号
		rv.addValue("WF_REF_ID", para.getWfRefId(), "String", 36); // 来源
		rv.addValue("WF_UNIT_NAME", para.getWfUnitName(), "String", 50); // 名称
		rv.addValue("WF_UNIT_MEMO", para.getWfUnitMemo(), "String", 500); // 备注
		rv.addValue("WF_UNIT_TYPE", para.getWfUnitType(), "String", 20); // 类型
		rv.addValue("WF_UNIT_ADM", para.getWfUnitAdm(), "String", 20); // 操作者类型
		rv.addValue("WF_UNIT_ADM_LST", para.getWfUnitAdmLst(), "String", 2050); // 操作者列表
		rv.addValue("WF_UNIT_PARA0", para.getWfUnitPara0(), "String", 500); // WF_UNIT_PARA0
		rv.addValue("WF_UNIT_PARA1", para.getWfUnitPara1(), "String", 500); // WF_UNIT_PARA1
		rv.addValue("WF_UNIT_PARA2", para.getWfUnitPara2(), "String", 500); // WF_UNIT_PARA2
		rv.addValue("WF_UNIT_PARA3", para.getWfUnitPara3(), "String", 500); // 检查参考
		rv.addValue("WF_UNIT_PARA4", para.getWfUnitPara4(), "String", 500); // 执行SQL参考
		rv.addValue("WF_UNIT_X", para.getWfUnitX(), "Integer", 10); // X坐标
		rv.addValue("WF_UNIT_Y", para.getWfUnitY(), "Integer", 10); // Y坐标
		rv.addValue("WF_UNIT_ACT_BEF", para.getWfUnitActBef(), "String", 1073741823); // 执行前检查
		rv.addValue("WF_UNIT_ACT_AFT", para.getWfUnitActAft(), "String", 1073741823); // 执行后事件
		rv.addValue("WF_UNIT_SELF_DEPT", para.getWfUnitSelfDept(), "String", 1); // 是否本部门
		rv.addValue("WF_UNIT_MEMO_EN", para.getWfUnitMemoEn(), "String", 1000); // 英文备注
		rv.addValue("WF_UNIT_NAME_EN", para.getWfUnitNameEn(), "String", 200); // 英文名称
		rv.addValue("WF_UNIT_NOTIFY", para.getWfUnitNotify(), "String", 20); // 通知
		return rv;
	}
}