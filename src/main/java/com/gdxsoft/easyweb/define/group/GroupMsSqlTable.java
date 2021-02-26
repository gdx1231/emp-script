/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.define.database.Field;

/**
 * @author Administrator
 * 
 */
public class GroupMsSqlTable extends GroupTableBase implements IGroupTable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTablePk()
	 */
	public String createTablePk() {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE " + super.getTable().getName()
				+ " WITH NOCHECK ADD CONSTRAINT PK_"
				+ super.getTable().getName() + " PRIMARY KEY CLUSTERED(");
		int m = 0;
		for (int i = 0; i < super.getFields().size(); i++) {
			Field f = super.getFields().get(
					super.getFields().getFieldList().get(i));
			if (!f.isPk()) {
				continue;
			}
			if (m > 0) {
				sb.append(",");
			}
			sb.append("\r\n\t" + f.getName());
		}
		sb.append(");\r\n");
		return sb.toString();
	}

	public String createTableRemarks() {
		// EXECUTE sp_updateextendedproperty N'MS_Description', @v, N'user',
		// N'dbo', N'table', N'CRM_COMPANY', N'column', N'CompanyID'
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < super.getFields().size(); i++) {
			Field f = super.getFields().get(
					super.getFields().getFieldList().get(i));
			if (f.getDescription() == null
					|| f.getDescription().equals(f.getName())) {
				continue;
			}
			String remark = f.getDescription().replace("'", "''");
			sb.append("EXECUTE sp_addextendedproperty  'MS_Description', '"
					+ remark + "', 'user', 'dbo', 'table', '"
					+ f.getTableName() + "', 'column', '" + f.getName()
					+ "';\r\n");
		}
		return sb.toString();
	}
}
