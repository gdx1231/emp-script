package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.define.database.Fields;
import com.gdxsoft.easyweb.define.database.Table;

public interface IGroupTable {

	/**
	 * 初始化表
	 * @param schemaName schema
	 * @param tableName 表名
	 * @param cnn 连接名
	 */
	public abstract void initTable(String schemaName, String tableName,
			String cnn);

	/**
	 * 生成外键
	 * @return
	 */
	public abstract String createTableFks();

	/**
	 * 生成索引
	 * @return
	 */
	public abstract String createTableIndexs();

	/**
	 * 生成表备注
	 * @return
	 */
	public abstract String createTableRemarks();

	/**
	 * 生成创建表主体
	 * @return
	 */
	public abstract String createTableBody();

	/**
	 * 生成主键
	 * @return
	 */
	public abstract String createTablePk();

	/**
	 * @return the _Table
	 */
	public abstract Table getTable();

	/**
	 * @param table the _Table to set
	 */
	public abstract void setTable(Table table);

	/**
	 * @return the _Fields
	 */
	public abstract Fields getFields();

}