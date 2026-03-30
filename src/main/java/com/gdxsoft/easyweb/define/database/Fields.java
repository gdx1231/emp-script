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
	
	/**
	 * 获取 Tree 加载 SQL
	 * @return SELECT * FROM table ORDER BY level, order
	 */
	public String GetSqlTreeLoad() {
		String tableName = this._TableName;
		String levelField = findFieldBySuffix("_LVL");
		String orderField = findFieldBySuffix("_ORD");
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName);
		
		// 添加 ORDER BY
		if (levelField != null || orderField != null) {
			sql.append(" ORDER BY ");
			if (levelField != null) {
				sql.append(levelField);
				if (orderField != null) {
					sql.append(", ").append(orderField);
				}
			} else if (orderField != null) {
				sql.append(orderField);
			}
		}
		
		return sql.toString();
	}
	
	/**
	 * 获取 Tree 删除节点 SQL
	 * @return DELETE FROM table WHERE id = @id
	 */
	public String GetSqlTreeNodeDelete() {
		String pkField = getPrimaryKeyField();
		if (pkField == null || pkField.isEmpty()) {
			return "-- Primary key not found";
		}
		return "DELETE FROM " + this._TableName + " WHERE " + pkField + " = @" + pkField;
	}
	
	/**
	 * 获取 Tree 重命名节点 SQL
	 * @return UPDATE table SET name = @name WHERE id = @id
	 */
	public String GetSqlTreeNodeRename() {
		String pkField = getPrimaryKeyField();
		String nameField = findFieldBySuffix("_NAME");
		
		if (pkField == null || pkField.isEmpty()) {
			return "-- Primary key not found";
		}
		if (nameField == null || nameField.isEmpty()) {
			return "-- Name field not found";
		}
		
		return "UPDATE " + this._TableName + " SET " + nameField + " = @" + nameField + 
			   " WHERE " + pkField + " = @" + pkField;
	}
	
	/**
	 * 获取 Tree 新增节点 SQL
	 * 参考 SQL Server 模板，使用 CASE WHEN 适配不同数据库:
	 * INSERT INTO table (name, pid, level, order, cdate, mdate, status)
	 * SELECT @text, CASE WHEN @parent_key IS NULL THEN 0 ELSE @parent_key END,
	 *        CASE WHEN MAX(level) IS NULL THEN -1 ELSE MAX(level) END+1,
	 *        CASE WHEN MAX(order) IS NULL THEN 0 ELSE MAX(order) END+1,
	 *        @date, @date, 'USED'
	 * FROM table WHERE pid = @parent_key
	 * @return INSERT INTO table (...) SELECT ... FROM table WHERE ...
	 */
	public String GetSqlTreeNodeNew() {
		String pkField = getPrimaryKeyField();
		String parentField = findFieldBySuffix("_PID");
		String levelField = findFieldBySuffix("_LVL");
		String orderField = findFieldBySuffix("_ORD");
		String nameField = findFieldBySuffix("_NAME");
		String cdateField = findFieldBySuffix("_CDATE");
		String mdateField = findFieldBySuffix("_MDATE");
		String statusField = findFieldBySuffix("_STATUS");
		
		if (pkField == null || pkField.isEmpty()) {
			return "-- Primary key not found";
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(this._TableName).append(" (");
		
		ArrayList<String> fields = new ArrayList<String>();
		ArrayList<String> selectFields = new ArrayList<String>();
		ArrayList<String> fromTables = new ArrayList<String>();
		ArrayList<String> whereConditions = new ArrayList<String>();
		
		// 名称字段
		if (nameField != null) {
			fields.add(nameField);
			selectFields.add("@EWA_TREE_TEXT " + nameField);
		}
		
		// 父 ID 字段 - 使用 CASE WHEN 适配不同数据库
		if (parentField != null) {
			fields.add(parentField);
			selectFields.add("CASE WHEN @EWA_TREE_PARENT_KEY IS NULL THEN 0 ELSE @EWA_TREE_PARENT_KEY END " + parentField);
		}
		
		// 层级字段 - 使用 CASE WHEN 适配不同数据库
		if (levelField != null) {
			fields.add(levelField);
			selectFields.add("CASE WHEN MAX(pp." + levelField + ") IS NULL THEN -1 ELSE MAX(pp." + levelField + ") END+1 " + levelField);
			fromTables.add(this._TableName + " pp");
		}
		
		// 排序字段 - 使用 CASE WHEN 适配不同数据库
		if (orderField != null) {
			fields.add(orderField);
			selectFields.add("CASE WHEN MAX(pc." + orderField + ") IS NULL THEN 0 ELSE MAX(pc." + orderField + ") END+1 " + orderField);
			if (fromTables.isEmpty()) {
				fromTables.add(this._TableName + " pc");
			} else {
				// 使用 LEFT JOIN
				String joinTable = this._TableName + " pc ON pc." + parentField + "=pp." + pkField;
				if (!fromTables.get(0).contains("JOIN")) {
					fromTables.set(0, fromTables.get(0) + " LEFT JOIN " + joinTable);
				}
			}
		}
		
		// 创建时间字段
		if (cdateField != null) {
			fields.add(cdateField);
			selectFields.add("@SYS_DATE " + cdateField);
		}
		
		// 修改时间字段
		if (mdateField != null) {
			fields.add(mdateField);
			selectFields.add("@SYS_DATE " + mdateField);
		}
		
		// 状态字段
		if (statusField != null) {
			fields.add(statusField);
			selectFields.add("'USED' " + statusField);
		}
		
		// WHERE 条件
		if (parentField != null && !fromTables.isEmpty()) {
			whereConditions.add("WHERE pp." + parentField + "= @EWA_TREE_PARENT_KEY");
		}
		
		// 构建 SQL
		sql.append(String.join(", ", fields));
		sql.append(") \nSELECT \t ");
		sql.append(String.join(",\n\t ", selectFields));
		
		if (!fromTables.isEmpty()) {
			sql.append("\nFROM ").append(String.join(" ", fromTables));
		}
		
		if (!whereConditions.isEmpty()) {
			sql.append("\n").append(String.join(" ", whereConditions));
		}
		
		sql.append("\n-- auto ").append(pkField);
		
		return sql.toString();
	}
	
	/**
	 * 查找带有指定后缀的字段
	 */
	private String findFieldBySuffix(String suffix) {
		for (String fieldName : this._FieldList) {
			if (fieldName.toUpperCase().endsWith(suffix.toUpperCase())) {
				return fieldName;
			}
		}
		return null;
	}
	
	/**
	 * 获取主键字段名
	 */
	private String getPrimaryKeyField() {
		if (this._PkFields != null && !this._PkFields.isEmpty()) {
			return this._PkFields.get(0).getName();
		}
		// 尝试查找以 _ID 结尾的字段
		for (String fieldName : this._FieldList) {
			if (fieldName.toUpperCase().endsWith("_ID")) {
				return fieldName;
			}
		}
		return null;
	}
}
