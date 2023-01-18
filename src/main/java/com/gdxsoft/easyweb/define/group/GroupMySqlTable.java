/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.define.database.Field;


/**
 * @author Administrator
 *
 */
public class GroupMySqlTable extends GroupTableBase implements IGroupTable {
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableFks()
	 */
	public String createTableFks() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableIndexs()
	 */
	public String createTableIndexs() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTablePk()
	 */
	public String createTablePk() {
		StringBuilder sb = new StringBuilder();
		//ALTER TABLE `blbb_new`.`dd` CHANGE COLUMN `a` `a` DOUBLE NOT NULL DEFAULT NULL  
		//, ADD PRIMARY KEY (`a`) ;
		sb.append("ALTER TABLE " + super.getTable().getName()
				+ " ADD PRIMARY KEY (");
		int m = 0;
		for (int i = 0; i < super.getFields().size(); i++) {
			Field f = super.getFields().get(super.getFields().getFieldList().get(i));
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

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableRemarks()
	 */
	public String createTableRemarks() {
		StringBuilder sb = new StringBuilder();
		//ALTER TABLE `blbb_new`.`dd` CHANGE COLUMN `a` `a` DOUBLE NOT NULL DEFAULT NULL  
		//, ADD PRIMARY KEY (`a`) ;
		
		for (int i = 0; i < super.getFields().size(); i++) {
			Field f = super.getFields().get(super.getFields().getFieldList().get(i));
			if (f.getDescription() == null
					|| f.getDescription().equals(f.getName())) {
				continue;
			}
			String remark = f.getDescription().replace("'", "''");
			sb.append("ALTER TABLE " + super.getTable().getName()
					+ " MODIFY "+f.getName()+" "+super.createFieldType(f)+" COMMENT '"+remark+"';\r\n");
		}
		return sb.toString();
	}

 

 

}
