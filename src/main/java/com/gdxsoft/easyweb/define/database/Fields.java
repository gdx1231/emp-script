/**
 *
 */
package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Administrator
 *
 */
public class Fields extends HashMap<String, Field> {

	/**
	 *
	 */
	private static final long serialVersionUID = -4894257715150499521L;


	private ArrayList<String> _FieldList=new ArrayList<String>();
	private String _TableName;
	private String _Pk;
	private ArrayList<Field> _PkFields = new ArrayList<Field>();
	private boolean _PkInitialized = false;  // 主键是否已初始化

	public Fields(){
	}

	public ArrayList<String> getFieldList() {
		return _FieldList;
	}
	
	/**
	 * 检查主键是否已初始化
	 * @return true 表示已初始化，false 表示未初始化
	 */
	public boolean isPkInitialized() {
		return _PkInitialized;
	}
	
	/**
	 * 设置主键初始化状态
	 * @param initialized true 表示已初始化，false 表示未初始化
	 */
	public void setPkInitialized(boolean initialized) {
		_PkInitialized = initialized;
	}
	
	/**
	 * 设置表名
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		_TableName = tableName;
	}
	
	/**
	 * 设置主键字段名
	 * @param pk
	 */
	public void setPk(String pk) {
		_Pk = pk;
	}
	
	/**
	 * 添加主键字段
	 * @param field
	 */
	public void addPkField(Field field) {
		_PkFields.add(field);
	}
	
	/**
	 * 获取主键字段列表
	 * @return
	 */
	public ArrayList<Field> getPkFields() {
		return _PkFields;
	}
	
	/**
	 * 获取主键参数表达式
	 * @return
	 */
	public String GetPkParas() {
		if (_PkFields.isEmpty()) {
			return "";
		}
		ArrayList<String> ss = new ArrayList<String>();
		for (Field f : _PkFields) {
			ss.add(f.getName() + "=@" + f.getName());
		}
		return "&" + String.join("&", ss);
	}
	
	/**
	 * 获取主键 WHERE 条件
	 * @param prefix 表名前缀
	 * @return
	 */
	public String GetSqlPk(String prefix) {
		if (_PkFields.isEmpty()) {
			return "1>2 -- table not defined pk";
		}
		ArrayList<String> ss = new ArrayList<String>();
		for (Field f : _PkFields) {
			String fieldName = (prefix != null ? prefix + "." : "") + f.getName();
			ss.add(fieldName + " = @" + f.getName());
		}
		return String.join("\n\tAND ", ss);
	}
	
	/**
	 * 获取状态字段名
	 * @return
	 */
	public String GetStatusField() {
		// 目前只有 ListFrame 有状态字段，由调用方设置
		return null;
	}
	
	/**
	 * 获取修改日期字段名
	 * @return
	 */
	public String GetMDateField() {
		for (String fieldName : _FieldList) {
			if (fieldName.toUpperCase().indexOf("_MDATE") > 0) {
				return fieldName;
			}
		}
		return null;
	}
	
	/**
	 * 获取创建日期字段名
	 * @return
	 */
	public String GetCDateField() {
		for (String fieldName : _FieldList) {
			if (fieldName.toUpperCase().indexOf("_CDATE") > 0) {
				return fieldName;
			}
		}
		return null;
	}
	
	/**
	 * 获取自增字段名
	 * @return
	 */
	public String GetIdentityField() {
		for (String fieldName : _FieldList) {
			Field f = this.get(fieldName);
			if (f != null && f.isIdentity()) {
				return fieldName;
			}
		}
		return null;
	}
	
	/**
	 * ListFrame 的 SELECT 查询 SQL
	 * @param statusField 状态字段名
	 * @param includeRecycle 是否包含回收站
	 * @return
	 */
	public String GetSqlSelectLF(String statusField, boolean includeRecycle) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT A.* FROM ").append(_TableName).append(" A WHERE 1=1");
		
		if (includeRecycle && statusField != null) {
			sb.append("\n\t-- ewa_test @EWA_RECYCLE is null");
			sb.append("\n\tAND A.").append(statusField).append(" = 'USED'");
			sb.append("\n\t-- ewa_test @EWA_RECYCLE = '1'");
			sb.append("\n\tAND A.").append(statusField).append(" = 'DEL'");
			sb.append("\n\t-- ewa_test");
		}
		
		// 默认按自增字段或修改日期排序
		String orderField = GetIdentityField();
		if (orderField == null) {
			orderField = GetMDateField();
		}
		if (orderField != null) {
			sb.append("\nORDER BY A.").append(orderField).append(" DESC");
		}
		
		return sb.toString();
	}
	
	/**
	 * 逻辑删除 SQL（更新状态为 DEL）
	 * @param statusField 状态字段名
	 * @return
	 */
	public String GetSqlDeleteA(String statusField) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(_TableName).append(" SET ").append(statusField).append("='DEL'");
		
		String mdateField = GetMDateField();
		if (mdateField != null) {
			sb.append(", ").append(mdateField).append(" = @sys_date");
		}
		
		sb.append(" WHERE ").append(GetSqlPk(null));
		
		return sb.toString();
	}
	
	/**
	 * 恢复数据 SQL（更新状态为 USED）
	 * @param statusField 状态字段名
	 * @return
	 */
	public String GetSqlRestore(String statusField) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(_TableName).append(" SET ").append(statusField).append("='USED'");
		
		String mdateField = GetMDateField();
		if (mdateField != null) {
			sb.append(", ").append(mdateField).append(" = @sys_date");
		}
		
		sb.append(" WHERE ").append(GetSqlPk(null));
		
		return sb.toString();
	}
	
	/**
	 * 物理删除 SQL
	 * @return
	 */
	public String GetSqlDelete() {
		return "DELETE FROM " + _TableName + " WHERE " + GetSqlPk(null);
	}
	
	/**
	 * 获取加载数据 SQL（单条记录）
	 * @return
	 */
	public String GetSqlSelect() {
		return "SELECT A.* FROM " + _TableName + " A WHERE " + GetSqlPk("A");
	}
	
	/**
	 * 获取更新 SQL
	 * @param statusField 状态字段名
	 * @return
	 */
	public String GetSqlUpdate(String statusField) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(_TableName).append(" SET ");
		
		ArrayList<String> sets = new ArrayList<String>();
		String cdateField = GetCDateField();
		
		for (String fieldName : _FieldList) {
			Field f = this.get(fieldName);
			if (f == null) continue;
			
			// 跳过自增字段、主键、已勾选字段
			if (f.isIdentity() || f.isPk()) {
				continue;
			}
			
			// 跳过创建日期字段
			if (cdateField != null && fieldName.toUpperCase().equals(cdateField.toUpperCase())) {
				continue;
			}
			
			// 跳过状态字段
			if (statusField != null && fieldName.toUpperCase().equals(statusField.toUpperCase())) {
				continue;
			}
			
			sets.add(fieldName + " = @" + GetPara(f));
		}
		
		sb.append("\n\t").append(String.join(",\n\t", sets));
		sb.append("\nWHERE ").append(GetSqlPk(null));
		
		return sb.toString();
	}
	
	/**
	 * 获取新增 SQL
	 * @param statusField 状态字段名
	 * @return
	 */
	public String GetSqlNew(String statusField) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		
		sb1.append("INSERT INTO ").append(_TableName).append(" (");
		sb2.append(") VALUES (");
		
		ArrayList<String> fields = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		for (String fieldName : _FieldList) {
			Field f = this.get(fieldName);
			if (f == null) continue;
			
			// 跳过自增字段
			if (f.isIdentity()) {
				continue;
			}
			
			fields.add(fieldName);
			
			if (statusField != null && fieldName.toUpperCase().equals(statusField.toUpperCase())) {
				values.add("'USED'");
			} else {
				values.add("@" + GetPara(f));
			}
		}
		
		sb1.append(String.join(", ", fields));
		sb2.append(String.join(", ", values)).append(")");
		
		return sb1.toString() + sb2.toString();
	}
	
	/**
	 * 获取参数名
	 * @param f 字段
	 * @return
	 */
	private String GetPara(Field f) {
		String name = f.getName().toUpperCase().trim();
		
		if (f.getDatabaseType().toUpperCase().contains("DATE") || f.getDatabaseType().toUpperCase().contains("TIME")) {
			return "SYS_DATE";
		} else if (name.indexOf("UNID") >= 0 && !name.equals("REF_UNID")) {
			return "SYS_UNID";
		} else if (name.equals("IP") || name.startsWith("IP_") || name.endsWith("_IP")) {
			return "SYS_REMOTEIP";
		} else if (name.equals("UA") || name.equals("USERAGENT") || name.equals("USER_AGENT")
				|| name.startsWith("USER_AGENT_") || name.endsWith("_USER_AGENT")
				|| name.startsWith("USERAGENT_") || name.endsWith("_USERAGENT")
				|| name.endsWith("_UA") || name.endsWith("UA_")) {
			return "SYS_USER_AGENT";
		} else if (name.equals("REFERER") || name.startsWith("REFERER_") || name.endsWith("_REFERER")) {
			return "SYS_REMOTE_REFERER";
		} else if (name.equals("REMOTE_URL") || name.startsWith("REMOTE_URL_") || name.endsWith("_REMOTE_URL")
				|| name.equals("JSP") || name.startsWith("JSP_") || name.endsWith("_JSP")) {
			return "SYS_REMOTE_URL_ALL";
		} else {
			return f.getName();
		}
	}
}
