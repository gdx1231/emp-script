package com.gdxsoft.easyweb.define.database;

import com.gdxsoft.easyweb.define.group.SqlTable;

public class Triger {

	private Table _Table;

	public Triger(Table table) {
		_Table = table;
	}

	public Triger(String tableName, String schemaName,
			String connectionConfigName) {
		Table table = new Table(tableName, schemaName, connectionConfigName);
		_Table = table;

	}

	public String getUpdateHistroy() {
		return getUpdateHistoryMySQL();
	}

	String getUpdateHistoryMySQL() {
		String hisName = _Table.getName() + "_EWAHIS";
		StringBuilder sb = new StringBuilder();
		
		SqlTable st=new SqlTable();
		try {
			st.createSqlTable(_Table, _Table.getDatabaseType());
			StringBuilder sbCr=new StringBuilder();
			String s1=st.getCreate().trim();
			s1=s1.replace(_Table.getName().toUpperCase(), hisName);
			sbCr.append(s1+";\r\n");
			sbCr.append("ALTER TABLE "+hisName+" ADD COLUMN _EWA_HIS_ID_ INT NOT NULL AUTO_INCREMENT,\r\n");
			sbCr.append("\tADD COLUMN _EWA_HIS_DT_ DATETIME,\r\n");
			sbCr.append("\tADD COLUMN _EWA_HIS_ADMID_ INT,\r\n");
			sbCr.append("\tADD PRIMARY KEY (_EWA_HIS_ID_) ;\r\n");
			

			sb.append(sbCr);
			
		} catch (Exception e) {
			sb.append(e.toString());
		}
		
		
		
		sb.append("delimiter //\r\n");
		sb.append("CREATE TRIGGER TR_" + _Table.getName()
				+ "_HIS BEFORE UPDATE ON " + _Table.getName());
		sb.append("\r\n");

		
		
		sb.append("FOR EACH ROW\r\n");
		sb.append("BEGIN\r\n");

		String hisInsert0 = "\tINSERT INTO " + hisName + "(_EWA_HIS_DT_";
		String hisInsert1 = " VALUES(NOW()";
		int pkIndex = 0;
		for (int i = 0; i < _Table.getFields().getFieldList().size(); i++) {
			String fieldName = _Table.getFields().getFieldList().get(i);
			Field field = _Table.getFields().get(fieldName);
			if (field.isPk()) {
				if (pkIndex > 0) {
					hisInsert0 += ", ";
					hisInsert1 += ", ";
				}
				hisInsert0 += fieldName;
				hisInsert1 += "old." + fieldName;
			}
		}

		sb.append(hisInsert0+")" + hisInsert1+");\r\n");
		
		for (int i = 0; i < _Table.getFields().getFieldList().size(); i++) {
			String n = _Table.getFields().getFieldList().get(i);
			Field field = _Table.getFields().get(n);
			if (field.isPk() || field.isIdentity()) {
				continue;
			}
			sb.append("\t-- UPDATE (" + n + ")\r\n");
			sb.append("\tIF new." + n + " <> old." + n + " THEN\r\n");
			sb.append("\t\tUPDATE " + hisName + " SET " + n + "=old." + n
					+ " WHERE _EWA_HIS_ID_=pHIS_ID;\r\n");
			sb.append("\tEND IF;\r\n\r\n");
		}

		sb.append("END;//\r\n");
		return sb.toString();
	}

}
