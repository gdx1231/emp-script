package com.gdxsoft.easyweb.script.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.datasource.IClassDao;
import com.gdxsoft.easyweb.datasource.ClassDaoBase;
/** 表_EWA_WF操作类
 * @author gdx 时间：Sat Jul 11 2020 20:59:34 GMT+0800 (中国标准时间)
 */
public class EwaWfDao extends ClassDaoBase<EwaWf> implements IClassDao<EwaWf>{

 private static String SQL_INSERT="INSERT INTO _EWA_WF(WF_ID, WF_NAME, WF_MEMO, WF_STATUS, WF_TABLE, WF_FIELD, WF_PARA0, WF_PARA1, WF_PARA2, WF_PARA3, WF_PARA4, WF_PARA5, WF_PARA6, WF_PARA7, WF_PARA8, WF_PARA9, WF_NAME_EN, WF_MEMO_EN) 	VALUES(@WF_ID, @WF_NAME, @WF_MEMO, @WF_STATUS, @WF_TABLE, @WF_FIELD, @WF_PARA0, @WF_PARA1, @WF_PARA2, @WF_PARA3, @WF_PARA4, @WF_PARA5, @WF_PARA6, @WF_PARA7, @WF_PARA8, @WF_PARA9, @WF_NAME_EN, @WF_MEMO_EN)";
 public static String TABLE_NAME ="_EWA_WF";
 public static String[] KEY_LIST = { "WF_ID"   };
 public static String[] FIELD_LIST = { "WF_ID", "WF_NAME", "WF_MEMO", "WF_STATUS", "WF_TABLE", "WF_FIELD", "WF_PARA0", "WF_PARA1", "WF_PARA2", "WF_PARA3", "WF_PARA4", "WF_PARA5", "WF_PARA6", "WF_PARA7", "WF_PARA8", "WF_PARA9", "WF_NAME_EN", "WF_MEMO_EN" };
 public EwaWfDao(){ 
   // 设置数据库连接配置名称，在 ewa_conf.xml中定义
   // super.setConfigName("ow_main");
   super.setInstanceClass(EwaWf.class);
   super.setTableName(TABLE_NAME);
   super.setFields(FIELD_LIST);
   super.setKeyFields(KEY_LIST);
   super.setSqlInsert(SQL_INSERT);
 }/**
	 * 生成一条记录
	*@param para 表_EWA_WF的映射类

	 *@return 是否成功
	*/

	public boolean newRecord(EwaWf para){

		RequestValue rv=this.createRequestValue(para);

		return super.executeUpdate(SQL_INSERT, rv);

	}

/**
 * 生成一条记录
 * 
 * @param para
 *            表_EWA_WF的映射类
 * @param updateFields
 *            变化的字段Map
 * @return
 */
public boolean newRecord(EwaWf para, HashMap<String, Boolean> updateFields){
  String sql = super.sqlInsertChanged(TABLE_NAME, updateFields, para);
  if (sql == null) { //没有可更新数据
  	return false;
  }
  RequestValue rv = this.createRequestValue(para);
  return super.executeUpdate(sql, rv);
}/**
	 * 根据主键返回一条记录
	*@param paraWfId 流程编号
	*@return 记录类(EwaWf)
	*/
public EwaWf getRecord(String paraWfId){
	RequestValue rv = new RequestValue();
rv.addValue("WF_ID", paraWfId, "String", 36);
	String sql = super.getSqlSelect();
ArrayList<EwaWf> al = super.executeQuery(sql, rv, new EwaWf(), FIELD_LIST);
if(al.size()>0){
EwaWf o = al.get(0);
al.clear();
return o;
}else{
return null;
}
}
/**
	 * 根据主键删除一条记录
	*@param paraWfId 流程编号
	*@return 是否成功
	*/
public boolean deleteRecord(String paraWfId){
	RequestValue rv = new RequestValue();
rv.addValue("WF_ID", paraWfId, "String", 36);
	return super.executeUpdate(super.createDeleteSql() , rv);
}
}