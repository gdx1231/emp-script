package com.gdxsoft.easyweb.data.export;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTColumns;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.UFile;
import com.svcon.jdbf.DBFWriter;
import com.svcon.jdbf.JDBField;

public class DbfExport implements IExport {

	/**
	 * 导出DBF文件
	 * 
	 * @param rs
	 * @param destDbfName
	 *            dbf文件名称
	 * @throws Exception
	 */
	public File export(ResultSet rs, String destDbfName) throws Exception {
		JDBField[] dbfFields = this.createFields(rs);
		File f=new File(destDbfName + ".dbf");
		DBFWriter writer = new DBFWriter(f.getAbsolutePath(), dbfFields,
				"gb2312");
		try {
			while (rs.next()) {
				Object[] val = new Object[dbfFields.length];
				for (int i = 0; i < dbfFields.length; i++) {
					if (dbfFields[i].getType() == 'C') {
						int len = dbfFields[i].getLength();
						String v = rs.getString(i+1);
						if (v == null) {
							val[i] = v;
						} else {
							if (v.length() > len) {
								v = v.substring(0, len);
							}
							val[i] = v;
						}
					} else {
						val[i] = rs.getObject(i + 1);
					}
				}
				writer.addRecord(val);
			}
		} catch (Exception e) {
			System.err.println("DBFExport:" + e.getMessage());
			throw e;
		} finally {
			writer.close();
		}
		String name = UFile.zipFile(f.getAbsolutePath());
		f.delete();
		return new File(name);
	}

	private JDBField[] createFields(ResultSet rs) throws Exception {
		ResultSetMetaData md = rs.getMetaData();
		JDBField[] ff = new JDBField[md.getColumnCount()];
		for (int i = 1; i <= md.getColumnCount(); i++) {
			String name = md.getColumnName(i);
			if (name == null || name.trim().equals("")) {
				name = md.getColumnLabel(i);
			}
			String type = md.getColumnTypeName(i).toUpperCase();
			int len = md.getColumnDisplaySize(i);
			int prec = md.getPrecision(i);
			int scale = md.getScale(i);
			if (name.getBytes().length > 10) {
				name = "F" + i;
			}
			if (type.indexOf("NUM") >= 0 || type.indexOf("INT") >= 0) {
				ff[i - 1] = new JDBField(name, 'N', prec, scale);
			} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
				ff[i - 1] = new JDBField(name, 'D', 8, 0);
			} else {
				len = len >= 254 ? 253 : len;
				ff[i - 1] = new JDBField(name, 'C', len, 0);
			}
		}
		return ff;
	}

	public File export(DTTable table, String destDbfName) throws Exception {
		JDBField[] dbfFields = this.createFields(table);
		DBFWriter writer = new DBFWriter(destDbfName + ".dbf", dbfFields,
				"gb2312");
		try {
			for (int i = 0; i < table.getCount(); i++) {
				Object[] val = new Object[dbfFields.length];
				for (int m = 0; m < dbfFields.length; i++) {
					val[i] = table.getCell(i, m).getValue();
				}
				writer.addRecord(val);
			}
		} catch (Exception e) {
			System.err.println("DBFExport:" + e.getMessage());
			throw e;
		} finally {
			writer.close();
		}
		String name = UFile.zipFile(destDbfName + ".dbf");
		return new File(name);

	}

	private JDBField[] createFields(DTTable table) throws Exception {
		DTColumns cols = table.getColumns();
		JDBField[] ff = new JDBField[cols.getCount()];
		for (int i = 1; i <= cols.getCount(); i++) {
			DTColumn col = cols.getColumn(i);
			String name = col.getName();
			String type = col.getTypeName().toUpperCase();
			int len = col.getLength();
			int prec = col.getPrecision();
			int scale = col.getScale();

			if (type.indexOf("NUM") >= 0 || type.indexOf("INT") >= 0) {
				ff[i] = new JDBField(name, 'N', prec, scale);
			} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
				ff[i] = new JDBField(name, 'D', 8, 0);
			} else {
				len = len > 254 ? 253 : len;
				ff[i] = new JDBField(name, 'C', len, 0);
			}
		}
		return ff;
	}
}
