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
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

public class XmlExport implements IExport {
	private DTColumns _Cols;

	public File export(ResultSet rs, String destXlsName) throws Exception {
		String xmlFields = this.createFields(rs);
		StringBuilder sb = new StringBuilder();
		File f = new File(destXlsName + ".xml");
		FileOutputStream fos = new FileOutputStream(f);
		Writer fw = null;

		try {
			fw = new OutputStreamWriter(fos, "utf-8");
			fw
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root>\r\n");
			fw.write(xmlFields);
			fw.flush();
			sb.append("<data>");
			while (rs.next()) {
				sb.append("<r ");
				for (int i = 1; i <= _Cols.getCount(); i++) {
					Object o = rs.getObject(i);
					this.createFieldData(o, _Cols.getColumn(i - 1), sb);
				}
				sb.append(" />\r\n");
				if (sb.length() > 10000) {
					fw.write(sb.toString());
					fw.flush();
					sb.setLength(0);
				}
			}
			sb.append("</data></root>");
			fw.write(sb.toString());
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null)
				fw.close();
		}
		String name = UFile.zipFile(f.getAbsolutePath());
		f.delete();
		return new File(name);
	}

	private void createFieldData(Object o, DTColumn col, StringBuilder sb) {
		String v1;
		if (o == null) {
			return;
		}
		String name = col.getName();
		String type = col.getTypeName().toUpperCase();
		if (type.indexOf("BIN") >= 0 || type.indexOf("IMAGE") >= 0) {
			v1 = Utils.bytes2hex((byte[]) o);
		} else if (type.indexOf("DATE") >= 0 || type.indexOf("TIME") >= 0) {
			v1 = Utils.getDateXmlString(o);
		} else {
			v1 = o.toString();
		}
		sb.append(this.createNodeAtt(name, v1));
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
		sb.append("<fields>\r\n");
		for (int i = 0; i < cols.getCount(); i++) {
			DTColumn col = cols.getColumn(i);
			sb.append("<field ");
			sb.append(this.createNodeAtt("name", col.getName()));
			sb.append(this.createNodeAtt("length", col.getLength() + ""));
			sb.append(this.createNodeAtt("type", col.getTypeName()));
			sb.append(this.createNodeAtt("scale", col.getScale() + ""));
			sb.append(this.createNodeAtt("precision", col.getPrecision() + ""));
			sb.append(" />\r\n");
		}
		sb.append("</fields>\r\n");
		return sb.toString();
	}

	public File export(DTTable table, String destXlsName) throws Exception {
		String xmlFields = this.createFields(table);
		StringBuilder sb = new StringBuilder();
		File f = new File(destXlsName + ".xml");
		FileWriter fw = null;

		try {
			fw = new FileWriter(f);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			fw.write(xmlFields);
			fw.flush();
			sb.append("<data>");
			for (int m = 0; m < table.getCount(); m++) {
				sb.append("<r ");
				for (int i = 0; i < _Cols.getCount(); i++) {
					Object o = table.getCell(m, i).getValue();
					this.createFieldData(o, _Cols.getColumn(i - 1), sb);
				}
				sb.append(" />\r\n");
				if (sb.length() > 10000) {
					fw.write(sb.toString());
					fw.flush();
					sb.setLength(0);
				}
			}
			sb.append("</data>");
			fw.write(sb.toString());
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null)
				fw.close();
		}
		String name = UFile.zipFile(f.getAbsolutePath());
		return new File(name);
	}

	private String createNodeAtt(String attName, String attValue) {
		if (attName == null || attName.trim().length() == 0 || attValue == null) {
			return "";
		}
		String s = attName.trim() + "=\"" + UXml.createXmlValue(attValue)
				+ "\" ";
		return s;
	}

}
