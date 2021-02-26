package com.gdxsoft.easyweb.data.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTColumns;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;

public class TxtExport implements IExport {

	/**
	 * 导出TXT文件
	 * 
	 * @param rs
	 * @param destTxtName
	 *            txt文件名称
	 * @throws Exception
	 */
	private DTColumns _Cols;
	private String _Lang;

	public TxtExport(String lang) {
		this._Lang = lang;
	}

	public File export(ResultSet rs, String destTxtName) throws Exception {
		String txtFields = this.createFields(rs);
		StringBuilder sb = new StringBuilder();
		File f = new File(destTxtName + ".txt");
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		Writer fw = null;
		try {
			fw = new OutputStreamWriter(fos, "utf-8");
			fw.write(txtFields);
			fw.flush();
			while (rs.next()) {
				for (int i = 1; i <= _Cols.getCount(); i++) {
					Object o = rs.getObject(i);
					if (i > 1) {
						sb.append("\t");
					}
					createFieldData(o, _Cols.getColumn(i - 1), sb);
				}
				sb.append("\r\n");
				if (sb.length() > 10000) {
					fw.write(sb.toString());
					fw.flush();
					sb.setLength(0);
				}
			}
			fw.write(sb.toString());
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
		String name = UFile.zipFile(f.getAbsolutePath());
		f.delete();
		return new File(name);
	}

	private void createFieldData(Object o, DTColumn col, StringBuilder sb) throws Exception {
		String v1;
		if (o == null) {
			return;
		}
		String type = col.getTypeName().toUpperCase();
		if (type.indexOf("BIN") >= 0 || type.indexOf("IMAGE") >= 0) {
			v1 = Utils.byte2hex((byte[]) o);
		} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
			java.util.Date t1 = (java.util.Date) o;
			String d = com.gdxsoft.easyweb.utils.UFormat.formatDate("datetime", t1, this._Lang);
			if (d.indexOf(" 00:00:00") > 0) {
				d = d.substring(0, d.indexOf(" "));
			}
			v1 = d;
		} else {
			v1 = o.toString();
		}
		if (v1 == null) {
			v1 = "";
		}
		v1 = v1.replace("\t", " ");
		sb.append(v1);
	}

	private String createFields(ResultSet rs) {
		DTTable tb = new DTTable();
		tb.initColumns(rs);
		return this.createFields(tb);
	}

	private String createFields(DTTable table) {
		DTColumns cols = table.getColumns();
		_Cols = cols;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cols.getCount(); i++) {
			DTColumn col = cols.getColumn(i);
			if (i > 0) {
				sb.append("\t");
			}
			sb.append(col.getName());
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public File export(DTTable table, String destTxtName) throws Exception {
		String txtFiles = this.createFields(table);
		StringBuilder sb = new StringBuilder();
		File f = new File(destTxtName + ".txt");
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			fw.write(txtFiles);
			fw.flush();
			for (int m = 0; m < table.getCount(); m++) {
				for (int i = 0; i < _Cols.getCount(); i++) {
					Object o = table.getCell(m, i).getValue();
					if (i > 0) {
						sb.append("\t");
					}
					this.createFieldData(o, _Cols.getColumn(i - 1), sb);
				}
				sb.append("\r\n");
				if (sb.length() > 10000) {
					fw.write(sb.toString());
					fw.flush();
					sb.setLength(0);
				}
			}
			fw.write(sb.toString());
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
		String name = UFile.zipFile(f.getAbsolutePath());
		return new File(name);
	}
}
