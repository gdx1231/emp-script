package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;

/**
 * 表_EWA_WF_CNN操作类
 * 
 * @author gdx 时间：Sat Jul 11 2020 20:59:03 GMT+0800 (中国标准时间)
 */
public class EwaWfCnnDao extends ClassDaoBase<EwaWfCnn> implements IClassDao<EwaWfCnn> {

	private static String SQL_INSERT = "INSERT INTO _EWA_WF_CNN(WF_UNIT_FROM, WF_UNIT_TO, WF_ID, WF_REF_ID, WF_CNN_ORD, WF_CNN_LOGIC, WF_CNN_NAME, WF_CNN_MEMO) 	VALUES(@WF_UNIT_FROM, @WF_UNIT_TO, @WF_ID, @WF_REF_ID, @WF_CNN_ORD, @WF_CNN_LOGIC, @WF_CNN_NAME, @WF_CNN_MEMO)";
	public static String TABLE_NAME = "_EWA_WF_CNN";
	public static String[] KEY_LIST = { "WF_UNIT_FROM", "WF_UNIT_TO", "WF_ID", "WF_REF_ID" };
	public static String[] FIELD_LIST = { "WF_UNIT_FROM", "WF_UNIT_TO", "WF_ID", "WF_REF_ID", "WF_CNN_ORD",
			"WF_CNN_LOGIC", "WF_CNN_NAME", "WF_CNN_MEMO" };

	public EwaWfCnnDao() {
		// 设置数据库连接配置名称，在 ewa_conf.xml中定义
		// super.setConfigName("ow_main");
		super.setInstanceClass(EwaWfCnn.class);
		super.setTableName(TABLE_NAME);
		super.setFields(FIELD_LIST);
		super.setKeyFields(KEY_LIST);
		super.setSqlInsert(SQL_INSERT);
	}

	public ArrayList<EwaWfCnn> getCnnsByJSon(String jsonString) {
		ArrayList<EwaWfCnn> al = new ArrayList<EwaWfCnn>();
		try {
			JSONObject json = new JSONObject("{\"tmp\":"+jsonString+"}");
			JSONArray jsons =json.getJSONArray("tmp");
			
			//json.toJSONArray(jsons);
			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);
				EwaWfCnn unit = this.mapJson(o);
				al.add(unit);
			}
		} catch (JSONException e) {
			return null;
		}
		return al;
	}
	 
	private EwaWfCnn mapJson(JSONObject o) {
		EwaWfCnn unit = new EwaWfCnn();
		for (int i = 0; i < FIELD_LIST.length; i++) {
			String f = FIELD_LIST[i];
			if (o.has(f)) {
				Object v;
				try {
					v = o.get(f);
					unit.setField(f, v);
				} catch (JSONException e) {
					System.out.println(e.toString());
				}
			}

		}
		return unit;
	}
	
	/**
	 * 生成一条记录
	 * 
	 * @param para 表_EWA_WF_CNN的映射类
	 * 
	 * @return 是否成功
	 */

	public boolean newRecord(EwaWfCnn para) {

		RequestValue rv = this.createRequestValue(para);

		return super.executeUpdate(SQL_INSERT, rv);

	}

	/**
	 * 生成一条记录
	 * 
	 * @param para         表_EWA_WF_CNN的映射类
	 * @param updateFields 变化的字段Map
	 * @return
	 */
	public boolean newRecord(EwaWfCnn para, HashMap<String, Boolean> updateFields) {
		String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
		if (sql == null) { // 没有可更新数据
			return false;
		}
		RequestValue rv = this.createRequestValue(para);
		return super.executeUpdate(sql, rv);
	}

	/**
	 * 根据主键返回一条记录
	 * 
	 * @param paraWfUnitFrom 来源
	 * @param paraWfUnitTo   目标
	 * @param paraWfId       流程
	 * @param paraWfRefId    参考来源
	 * @return 记录类(EwaWfCnn)
	 */
	public EwaWfCnn getRecord(String paraWfUnitFrom, String paraWfUnitTo, String paraWfId, String paraWfRefId) {
		RequestValue rv = new RequestValue();
		rv.addValue("WF_UNIT_FROM", paraWfUnitFrom, "String", 36);
		rv.addValue("WF_UNIT_TO", paraWfUnitTo, "String", 36);
		rv.addValue("WF_ID", paraWfId, "String", 36);
		rv.addValue("WF_REF_ID", paraWfRefId, "String", 36);
		String sql = super.getSqlSelect();
		ArrayList<EwaWfCnn> al = super.executeQuery(sql, rv, new EwaWfCnn(), FIELD_LIST);
		if (al.size() > 0) {
			EwaWfCnn o = al.get(0);
			al.clear();
			return o;
		} else {
			return null;
		}
	}

	/**
	 * 根据主键删除一条记录
	 * 
	 * @param paraWfUnitFrom 来源
	 * @param paraWfUnitTo   目标
	 * @param paraWfId       流程
	 * @param paraWfRefId    参考来源
	 * @return 是否成功
	 */
	public boolean deleteRecord(String paraWfUnitFrom, String paraWfUnitTo, String paraWfId, String paraWfRefId) {
		RequestValue rv = new RequestValue();
		rv.addValue("WF_UNIT_FROM", paraWfUnitFrom, "String", 36);
		rv.addValue("WF_UNIT_TO", paraWfUnitTo, "String", 36);
		rv.addValue("WF_ID", paraWfId, "String", 36);
		rv.addValue("WF_REF_ID", paraWfRefId, "String", 36);
		return super.executeUpdate(super.createDeleteSql(), rv);
	}
}