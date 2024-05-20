/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.util.Date;

/**
 * @author Administrator
 * 
 */
public class DataSql {
	private MapDatabaseTypes _MapDatabaseTypes;
	private MapTypes _DestMapTypes;
	private MapTypes _SrcMapTypes;
	private MapTypes _defMapTypes;

	public DataSql() throws Exception {
		_MapDatabaseTypes = MapDatabaseTypes.instance();
	}

	public String parseTableSql(Table table, String destMapTypesName) {
		_DestMapTypes = this._MapDatabaseTypes.get(destMapTypesName
				.toLowerCase().trim());
		_defMapTypes = this._MapDatabaseTypes.getDefaultMapTypes();
		String srcDbType = table.getDatabaseType().trim().toLowerCase();
		_SrcMapTypes = this._MapDatabaseTypes.get(srcDbType);
		Date date = new Date();
		StringBuilder sql = new StringBuilder();
		sql.append("-- FROM " + _SrcMapTypes.getName() + " TO "
				+ _DestMapTypes.getName());
		sql.append("\r\n-- TABLE " + table.getName());
		sql.append("\r\n-- DATE: " + date.toString());

		sql.append("\r\nCREATE TABLE " + table.getName() + " (\r\n");
		sql.append(parseFieldSql(table));
		sql.append("\r\n);\r\n");
		return sql.toString();
	}

	/**
	 * 生成字段表达式
	 * 
	 * @param table
	 *            表的类
	 * @return
	 */
	public String parseFieldSql(Table table) {
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < table.getFields().size(); i++) {
			String fieldName = table.getFields().getFieldList().get(i);
			Field field = table.getFields().get(fieldName);
			sql.append(" \t" + this.parseFieldSql(field));
			if (i < table.getFields().size() - 1) {
				sql.append(",");
			}
			sql.append("\t--source " + field.getDatabaseType() + ","
					+ field.getColumnSize() + "," + field.getDecimalDigits()
					+ "," + field.getDescription() + "\r\n");

		}
		return sql.toString();
	}

	/**
	 * 生成单个字段表达式
	 * 
	 * @param field
	 * @return
	 */
	public String parseFieldSql(Field field) {
		String s1 = field.getName() + " " + parseFieldTypeSql(field);
		if (field.isIdentity()) {
			if (this._DestMapTypes.isIdentity()) {
				s1 += " IDENTITY ";
			}
		}
		if ((!field.isNull()) || field.isPk()) {
			s1 += " NOT NULL";
		}
		return s1;
	}

	public String parseFieldTypeSql(Field field) {
		MapType destMapType = getDestMapType(field);
		String lexp = destMapType.getLExp();
		String s1 = destMapType.getName();
		if (lexp.equals("()")) {
			s1 += "(" + field.getMapLength() + ")";
		} else if (lexp.equals("(,)")) {
			s1 += "(" + field.getMapLength() + "," + field.getDecimalDigits()
					+ ")";
		}
		return s1;
	}

	public String parseTablePk(Table table) {
		String pk = "ALTER TABLE " + table.getName() + "\r\n\tADD CONSTRAINT "
				+ table.getPk().getPkName() + " PRIMARY KEY (";
		for (int i = 0; i < table.getPk().getPkFields().size(); i++) {

			pk += "\r\n\t\t" + table.getPk().getPkFields().get(i).getName();
			if (i < table.getPk().getPkFields().size() - 1) {
				pk += ", ";
			}
		}
		pk += "\r\n\t);\r\n";
		return pk;
	}

	public String parseTableFk(Table table) {
		// ALTER TABLE [dbo].[CRM_SALES_RELATION] ADD
		// CONSTRAINT [FK_CRM_SALES_RELATION_CRM_SALES] FOREIGN KEY
		// (
		// [SalesId]
		// ) REFERENCES [dbo].[CRM_SALES] (
		// [SalesId]
		// )
		String s1 = "";
		for (int m = 0; m < table.getFks().size(); m++) {
			TableFk fk = table.getFks().get(m);

			s1 += "ALTER TABLE " + table.getName() + "\r\n\tADD CONSTRAINT "
					+ fk.getFkName() + " FOREIGN KEY (";
			for (int i = 0; i < fk.getFkFields().size(); i++) {

				s1 += "\r\n\t\t" + fk.getFkFields().get(i).getName();
				if (i < table.getPk().getPkFields().size() - 1) {
					s1 += ", ";
				}
			}
			s1 += "\r\n\t) REFERENCES " + fk.getPk().getTableName() + "(";
			for (int i = 0; i < fk.getPk().getPkFields().size(); i++) {

				s1 += "\r\n\t\t" + fk.getFkFields().get(i).getName();
				if (i < fk.getPk().getPkFields().size() - 1) {
					s1 += ", ";
				}
			}
			s1 += "\r\n\t);\r\n";
		}
		return s1;
	}

	public String parseTableIndex(Table table) {
		// CREATE INDEX [idx_aa_1] ON [dbo].[aa]([t_datetime] DESC )
		String s1 = "";
		for (int m = 0; m < table.getIndexes().size(); m++) {
			TableIndex index = table.getIndexes().get(m);
			if(index.getIndexName().equals(table.getPk().getPkName())){
				//主键不用建立
				continue;
			}
			if (index.isUnique()) {
				s1 += "CREATE UNIQUE INDEX " + index.getIndexName() + " ON "
						+ table.getName() + " (";

			} else {
				s1 += "CREATE INDEX " + index.getIndexName() + " ON "
						+ table.getName() + " (";
			}
			for (int i = 0; i < index.getIndexFields().size(); i++) {

				s1 += "\r\n\t\t" + index.getIndexFields().get(i).getName();
				if (i < index.getIndexFields().size() - 1) {
					s1 += ", ";
				}
			}
			s1 += "\r\n\t);\r\n";
		}
		return s1;
	}

	private MapType getStandradMapType(Field field) {
		String fieldType = field.getDatabaseType().toLowerCase().trim();
		String f1 = fieldType.split(" ")[0];
		MapType mt = this._SrcMapTypes.get(f1);
		return this._defMapTypes.get(mt.getStandard());
	}

	private MapType getDestMapType(Field field) {
		MapType stdMapType = getStandradMapType(field);

		String fieldType = field.getDatabaseType().toLowerCase().trim();
		String f1 = fieldType.split(" ")[0];
		MapType srcMapType = this._SrcMapTypes.get(f1);
		String stdName = stdMapType.getName();

		MapType destMapType = null;
		if (_SrcMapTypes.getName().equals(_DestMapTypes.getName())) {
			field.setMapLength(field.getColumnSize());
			return srcMapType;
		}

		java.util.Iterator<String> it = _DestMapTypes.keySet().iterator();
		while (it.hasNext()) {
			MapType mt = _DestMapTypes.get(it.next());
			if (mt.isDefault() && mt.getStandard().equals(stdName)) {
				destMapType = mt;
				break;
			}
		}

		if (destMapType == null) {// 目的不存在，用默认的映射
			destMapType = _DestMapTypes.getDefaultMapType();
		} else {
			// 目的映射的长度判断
			if (destMapType.getMax() > 0
					&& destMapType.getMax() < field.getColumnSize()) {
				// 目的映射换成替代类型
				destMapType = _DestMapTypes.get(destMapType.getOther());
			}
		}
		if (field.getCharOctetLength() > 0) {
			field.setMapLength(field.getCharOctetLength());
		} else {
			field.setMapLength(field.getColumnSize());
		}
		return destMapType;
	}

}
